<template>
  <div class="ai-resume-container" :class="{ 'eye-care': isEyeCareMode }">
    <div id="stars"></div>
    <div id="stars2"></div>
    <div id="stars3"></div>
    <!-- 科幻背景效果 -->
    <div class="cyber-background">
      <div class="grid-lines"></div>
      <!-- 添加扫描线效果 -->
      <div class="scanning-lines">
        <div class="horizontal-scan"></div>
        <div class="vertical-scan"></div>
      </div>
      <div class="floating-particles">
        <div v-for="n in 30" :key="n" class="particle" 
             :style="{ 
               '--delay': `${Math.random() * 5}s`,
               '--x-pos': `${Math.random() * 100}%`,
               '--y-pos': `${Math.random() * 100}%`,
               '--x-end': `${Math.random() * 200 - 100}px`
             }"></div>
      </div>
      <div class="energy-field"></div>
    </div>

    <!-- 主内容卡片 -->
    <div class="content-card">
      <div class="top-bar">
        <div class="holographic-header">
        <h1 class="page-title">简历评测</h1>
          <div class="title-glow"></div>
        </div>
        <div class="placeholder"></div>
      </div>

      <!-- 进度条区域 -->
      <div class="progress-section" v-if="resumeFile">
        <div class="progress-bar-wrapper">
            <div class="progress-fill" :style="{ width: `${progress}%` }"></div>
          <div class="progress-pulse"></div>
          </div>
          <span class="progress-text">{{ progress }}%</span>
      </div>

      <!-- 步骤指示器 -->
      <div class="analysis-steps" v-if="isAnalyzing || analysisComplete">
        <div v-for="(step, index) in analysisSteps" :key="index"
             class="step-item" :class="{ 'active': currentStep > index, 'current': currentStep === index + 1 }">
          <div class="step-circle">{{ index + 1 }}</div>
          <div class="step-label">{{ step }}</div>
        </div>
      </div>

      <!-- 简历上传区域 -->
      <div class="upload-section" v-if="!resumeFile && !isAnalyzing && !analysisComplete">
        <div class="job-info-banner" v-if="jobId">
          <span>正在分析与职位 <b>{{ jobName || '职位ID: ' + jobId }}</b> 的匹配度</span>
        </div>
        <div class="upload-box" 
             :class="{ 'drag-over': isDragging }">
                    <div class="upload-area"
               @click="triggerFileUpload"
               @dragover.prevent="handleDragOver"
               @dragleave.prevent="handleDragLeave"
               @drop.prevent="handleFileDrop">
            <div class="upload-icon">
              <div class="custom-upload-icon">
                <div class="resume-document">
                  <div class="document-body"></div>
                  <div class="document-header"></div>
                  <div class="document-lines">
                    <div class="line"></div>
                    <div class="line"></div>
                    <div class="line"></div>
                  </div>
                  <div class="document-fold"></div>
                </div>

              </div>
              <div class="icon-pulse"></div>
            </div>
            <h3>上传您的简历</h3>
            <p class="supported-formats">支持 PDF、Word、TXT 格式</p>
            <p class="upload-hint">点击选择文件或拖拽文件到此区域</p>
            <div class="upload-glow"></div>
          </div>
        </div>
        <p class="error-message" v-if="uploadError">{{ uploadError }}</p>
      </div>

      <!-- 文件信息与分析状态区域 -->
      <div class="analysis-section" v-else-if="resumeFile && (!analysisComplete || isAnalyzing)">
        <div class="file-preview-card">
          <div class="file-icon">
            <div class="custom-file-icon">
              <div class="file-document">
                <div class="file-body"></div>
                <div class="file-header"></div>
                <div class="file-content">
                  <div class="content-line"></div>
                  <div class="content-line"></div>
                  <div class="content-line"></div>
                </div>
                <div class="file-fold"></div>
                <div class="file-extension">{{ getFileExtension }}</div>
              </div>
            </div>
          </div>
          <div class="file-details">
            <h4>{{ resumeFile.name }}</h4>
            <p>{{ formatFileSize(resumeFile.size) }}</p>
          </div>
          <button class="remove-file-button" @click="removeFile" v-if="!isAnalyzing">
            <span aria-hidden="true">×</span>
          </button>
        </div>

        <div class="status-display">
          <div v-if="isAnalyzing" class="loading-animation-wrapper">
            <div class="cyber-loader">
              <div class="cyber-spinner"></div>
              <div class="cyber-spinner-inner"></div>
            </div>
            <p class="status-message">{{ currentStatus }}</p>
            </div>
          <div v-else-if="analysisComplete" class="analysis-complete-message">
            <span class="success-icon" aria-hidden="true">✓</span>
            <p>简历分析已完成！</p>
            </div>
            </div>

        <div class="action-buttons-analysis">
          <button
            class="view-report-button"
            @click="viewReport"
            v-if="analysisComplete"
            :disabled="isReturningToAi"
          >
            返回AIview中...
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { onLoad } from '@dcloudio/uni-app';
import { analyzeComprehensiveResume, extractResumeContent, getJobInfo, saveAiResumeHistory } from '@/utils/api/pages/ai-resume';
import { getUserSession } from '@/utils/user-session';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
import {
  enqueueAiAssessmentCompletion,
  ensureActiveAiAssessmentSession,
  getAssessmentThresholds,
  getStagePassScore,
  normalizeComprehensiveState,
  updateComprehensiveAssessmentState
} from '@/utils/comprehensive-assessment';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);

const jobId = ref(null);
const chatId = ref('');
const assessmentId = ref('');
const jobName = ref('');
const resumeFile = ref(null);
const isAnalyzing = ref(false);
const analysisComplete = ref(false);
const progress = ref(0);
const currentStep = ref(0);
const currentStatus = ref('');
const resumeScore = ref(0);
const suggestions = ref([]);
const uploadError = ref('');
const isDragging = ref(false);
const isReturningToAi = ref(false);
const hasHandledReturnToAi = ref(false);
// 雷达图相关已移除

const resumeId = ref(null);
let pollingInterval = null;
let returnToAiTimer = null;
const pollingAttempts = ref(0);

const analysisSteps = [
  '解析文档',
  '提取信息',
  '匹配分析'
];

const analysisTips = [
  '正在解析简历内容，请稍候...',
  '正在从简历中提取关键信息...',
  '正在进行职位匹配度分析...'
];

const shouldReturnToAiChat = () => {
  return String(route.query.from || '').trim() === 'ai-interview'
    || String(route.query.autoOpen || '').trim() === '1';
};

const buildNextComprehensiveStepUrl = () => {
  const safeJobId = String(jobId.value || route.query.jobId || '').trim();
  const safeSessionId = String(chatId.value || route.query.chatId || '').trim();
  const query = [];
  if (safeSessionId) {
    query.push(`chatId=${encodeURIComponent(safeSessionId)}`);
  }
  if (safeJobId) {
    query.push(`jobId=${encodeURIComponent(safeJobId)}`);
  }
  if (assessmentId.value) {
    query.push(`assessmentId=${encodeURIComponent(assessmentId.value)}`);
  }
  query.push('completedType=resume');
  query.push('mode=COMPREHENSIVE');
  query.push(`score=${encodeURIComponent(String(resumeScore.value || 0))}`);
  query.push(`timestamp=${encodeURIComponent(String(Date.now()))}`);
  query.push('autoOpen=1');
  return `/pages/ai-chat/index?${query.join('&')}`;
};

const initializeAssessmentSession = (incomingAssessmentId = '') => {
  const safeJobId = String(jobId.value || route.query.jobId || '').trim();
  if (!safeJobId) {
    assessmentId.value = '';
    return null;
  }
  const session = ensureActiveAiAssessmentSession(safeJobId, {
    assessmentId: incomingAssessmentId || route.query.assessmentId || assessmentId.value || '',
    createIfMissing: true
  });
  assessmentId.value = String(session?.assessmentId || '').trim();
  if (!assessmentId.value) {
    return session;
  }
  return updateComprehensiveAssessmentState(safeJobId, assessmentId.value, (currentState) => {
    const nextState = normalizeComprehensiveState(currentState);
    if (!nextState.resume.completed) {
      nextState.resume.inProgress = true;
      nextState.resume.startTime = nextState.resume.startTime || new Date().toISOString();
    }
    nextState.overall.startTime = nextState.overall.startTime || new Date().toISOString();
    if (nextState.overall.status === 'not_started') {
      nextState.overall.status = 'in_progress';
    }
    return nextState;
  });
};

const getFileExtension = computed(() => {
  if (!resumeFile.value) return '';
  const ext = resumeFile.value.name.split('.').pop().toLowerCase();
  return ext.toUpperCase();
});

const getScoreClass = computed(() => {
  if (resumeScore.value >= 90) return 'score-excellent';
  if (resumeScore.value >= 75) return 'score-good';
  if (resumeScore.value >= 60) return 'score-fair';
  return 'score-poor';
});

const normalizeResumeScoreValue = (value = 0) => {
  const safeScore = Math.max(0, Number(value) || 0);
  const normalizedScore = safeScore > 0 && safeScore <= 10 ? safeScore * 10 : safeScore;
  return Math.min(100, Math.max(0, normalizedScore));
};

const normalizeResumeScore = (analysisResult = {}) => {
  const rawScore = analysisResult.overallScore
    ?? analysisResult.overall_score
    ?? analysisResult.score
    ?? 0;
  return normalizeResumeScoreValue(rawScore);
};

const normalizeResumeSuggestions = (analysisResult = {}) => {
  const rawSuggestions = analysisResult.suggestions
    ?? analysisResult.improvementSuggestions
    ?? analysisResult.aiSuggestions
    ?? analysisResult.advice
    ?? [];
  if (Array.isArray(rawSuggestions)) {
    return rawSuggestions.map((item) => String(item || '').trim()).filter(Boolean);
  }
  if (typeof rawSuggestions === 'string') {
    try {
      const parsed = JSON.parse(rawSuggestions);
      if (Array.isArray(parsed)) {
        return parsed.map((item) => String(item || '').trim()).filter(Boolean);
      }
      if (Array.isArray(parsed?.suggestions)) {
        return parsed.suggestions.map((item) => String(item || '').trim()).filter(Boolean);
      }
    } catch (_) {}
    return rawSuggestions
      .split(/\n|；|;/)
      .map((item) => item.replace(/^[-*\d.、\s]+/, '').trim())
      .filter(Boolean);
  }
  return [];
};

const buildResumeAnalysisSummary = () => {
  const suggestionText = suggestions.value.length > 0
    ? suggestions.value.map((item, index) => `${index + 1}. ${item}`).join('\n')
    : '暂无 AI 建议';
  return `简历总体评分：${resumeScore.value}分。\nAI 改进建议：\n${suggestionText}`;
};

const triggerFileUpload = () => {
  uploadError.value = '';

  uni.chooseFile({
    count: 1,
    type: 'file',
    extension: ['.pdf', '.doc', '.docx', '.txt'],
    success: (res) => {
      console.log('文件选择成功', res);
      if (res.tempFiles && res.tempFiles.length > 0) {
        const file = res.tempFiles[0];
        validateAndSetFile(file);
      }
    },
    fail: (err) => {
      console.error('文件选择失败:', err);
      // 如果是用户取消，不显示错误
      if (err.errMsg && err.errMsg.includes('cancel')) {
        return;
      }
      uni.showToast({ title: uploadError.value, icon: 'none' });
    }
  });
};

const handleFileDrop = (event) => {
  isDragging.value = false;
  const file = event.dataTransfer.files[0];
  if (file) {
    validateAndSetFile(file);
  }
};

const handleDragOver = () => {
  isDragging.value = true;
};

const handleDragLeave = () => {
  isDragging.value = false;
};

const validateAndSetFile = (file) => {
  const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'text/plain'];
  if (!allowedTypes.includes(file.type) && !file.name.match(/\.(pdf|doc|docx|txt)$/i)) {
    uploadError.value = '不支持的文件格式。请上传 PDF, Word 或 TXT 文件。';
    resumeFile.value = null;
    return;
  }
  if (file.size > 5 * 1024 * 1024) { // 5MB limit
    uploadError.value = '文件大小不能超过 5MB。';
    resumeFile.value = null;
    return;
  }
  resumeFile.value = file;
  uploadError.value = '';
  resetAnalysisState();
  startAnalysis();
};

const removeFile = () => {
  resumeFile.value = null;
  resetAnalysisState();
  uploadError.value = '';
};

// 获取岗位详情
const getJobDetails = async (jobId) => {
  try {
    const response = await getJobInfo(jobId);
    
    console.log('职位详情响应:', response);
    
    if (response.code === 200 && response.data) {
      return response.data;
    } else {
      console.error('获取职位详情失败:', response.message || '未知错误');
      return null;
    }
  } catch (error) {
    console.error('获取职位详情API出错:', error);
    return null;
  }
};

// 使用讯飞大模型分析简历
const analyzeWithXunfei = async (resumeContent) => {
  try {
    // 获取职位详情
    let jobDetails = null;
    if (jobId.value) {
      jobDetails = await getJobDetails(jobId.value);
    }

    const response = await analyzeComprehensiveResume(resumeContent, jobDetails || {});

    console.log('讯飞简历分析响应:', response);

    // 检查响应状态
    if (response.code === 200 && response.data) {
        if (typeof response.data === 'object' && response.data !== null) {
          return response.data;
        }
        console.error('综合简历评测返回了非对象结果:', response.data);
        return null;
    } else {
      console.error('获取讯飞分析失败:', response.message);
      return null;
    }
  } catch (error) {
    console.error('调用讯飞API出错:', error);
    return null;
  }
};

// 从文件中读取文本内容
const extractTextFromFile = async (file) => {
  return new Promise((resolve, reject) => {
    console.log('准备提取文件内容:', {
      fileName: file.name,
      fileSize: file.size,
      fileType: file.type,
      jobId: jobId.value
    });

    // 使用后端API提取文件内容
    extractResumeContent(file)
      .then(response => {
        
        if (response && response.code === 200 && response.data) {
          console.log('成功提取简历内容，内容长度:', response.data.length);
          resolve(response.data);
        } else {
          console.error('提取简历内容失败:', response.message || '未知错误');
          throw new Error(response.message || '无法提取简历内容');
        }
      })
      .catch(error => {
        console.error('调用提取API失败:', error);
        
        uni.showModal({
          title: '文件处理失败',
          content: '无法读取简历内容，请确保文件格式正确并重试',
          showCancel: false
        });
        
        reject(error);
      });
  });
};

const startAnalysis = async () => {
  if (!resumeFile.value) {
    uni.showToast({ title: '请先上传简历文件', icon: 'none' });
    return;
  }
  
  try {
    // 显示分析中状态
    isAnalyzing.value = true;
    analysisComplete.value = false;
    progress.value = 0;
    currentStep.value = 1;
    currentStatus.value = '正在解析文档...';
    pollingAttempts.value = 0;
    clearInterval(pollingInterval);

    // 逐步更新分析进度
    const updateProgress = () => {
      setTimeout(() => {
        if (progress.value < 35) {
          progress.value += 10;
          currentStatus.value = analysisTips[0];
        } else if (progress.value < 70) {
          progress.value += 10;
          currentStep.value = 2;
          currentStatus.value = analysisTips[1];
        } else if (progress.value < 100) {
          progress.value += 5;
          currentStep.value = 3;
          currentStatus.value = analysisTips[2];
        }
        
        if (progress.value < 99 && isAnalyzing.value) {
          updateProgress();
        }
      }, 800);
    };

    // 从文件中提取文本内容（现在通过后端API完成）
    // extractTextFromFile函数内部会自己调用showLoading/hideLoading，这里不重复调用
    const resumeContent = await extractTextFromFile(resumeFile.value);
    
    if (!resumeContent || resumeContent.trim().length < 10) {
      throw new Error('无法从文件中提取有效内容');
    }
    
    console.log('成功获取简历内容，长度:', resumeContent.length);

    // 启动进度更新
    updateProgress();
    
    // 调用讯飞大模型分析简历
    const analysisResult = await analyzeWithXunfei(resumeContent);
    
    if (!analysisResult) {
      throw new Error('获取分析结果失败');
    }
    
    // 使用讯飞返回的分析结果
    resumeScore.value = normalizeResumeScore(analysisResult);
    suggestions.value = normalizeResumeSuggestions(analysisResult);
    
    // 检查分析结果是否有效
    if (resumeScore.value === 0 && suggestions.value.length === 0) {
      throw new Error('分析结果异常，评分数据无效');
    }

    isAnalyzing.value = false;
    analysisComplete.value = true;
    progress.value = 100;
    currentStep.value = 3;
    
    // 设置resumeId，如果后端没有返回
    if (!resumeId.value) {
      resumeId.value = 'local_resume_id_' + Date.now();
    }

    uni.showToast({ title: '简历分析成功！', icon: 'success' });
    scheduleReturnToAiAfterSuccess();
  } catch (error) {
    console.error('简历分析失败:', error);
    isAnalyzing.value = false;
    
    let errorMessage = '无法分析简历，请重试';
    
    if (error.message) {
      if (error.message.includes('提取有效内容')) {
        errorMessage = '无法读取简历内容，请确保文件格式正确';
      } else if (error.message.includes('数据无效')) {
        errorMessage = 'AI分析结果异常，请重试或更换简历文件';
      }
    }
    
    uni.showModal({
      title: '分析失败',
      content: errorMessage,
      showCancel: false
    });
  } finally {
  }
};


const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

const resetAnalysisState = () => {
  isAnalyzing.value = false;
  analysisComplete.value = false;
  progress.value = 0;
  currentStep.value = 0;
  currentStatus.value = '';
  resumeScore.value = 0;
  suggestions.value = [];
  resumeId.value = null;
  pollingAttempts.value = 0;
  clearInterval(pollingInterval);
  clearTimeout(returnToAiTimer);
  returnToAiTimer = null;
  isReturningToAi.value = false;
  hasHandledReturnToAi.value = false;
};

const scheduleReturnToAiAfterSuccess = () => {
  viewReport();
};

const viewReport = async () => {
  if (hasHandledReturnToAi.value) {
    return;
  }
  hasHandledReturnToAi.value = true;
  clearTimeout(returnToAiTimer);
  returnToAiTimer = null;
  isReturningToAi.value = true;
  // 立即更新简历评测模块的状态和分数
  try {
    const safeJobId = String(jobId.value || route.query.jobId || '').trim();
    const savedSession = updateComprehensiveAssessmentState(safeJobId, assessmentId.value, (currentState) => {
      const nextState = normalizeComprehensiveState(currentState);
      nextState.resume = {
        ...nextState.resume,
        completed: true,
        inProgress: false,
        score: Math.max(0, Number(resumeScore.value) || 0),
        suggestions: suggestions.value,
        analysisId: resumeId.value || `resume_${Date.now()}`,
        startTime: nextState.resume.startTime || new Date().toISOString(),
        endTime: new Date().toISOString(),
        attempts: Math.max(1, Number(nextState.resume.attempts || 0) + 1)
      };
      nextState.overall = {
        ...nextState.overall,
        startTime: nextState.overall.startTime || new Date().toISOString(),
        status: 'in_progress'
      };
      return nextState;
    });
    assessmentId.value = String(savedSession?.assessmentId || assessmentId.value || '').trim();
    enqueueAiAssessmentCompletion({
      type: 'resume_completed',
      chatId: String(chatId.value || route.query.chatId || '').trim(),
      jobId: safeJobId,
      assessmentId: assessmentId.value,
      mode: 'COMPREHENSIVE',
      score: resumeScore.value,
      suggestions: suggestions.value,
      timestamp: String(Date.now())
    });
    
    // 保存简历综合评估历史到后端
    try {
      const historyData = {
        assessmentSessionId: assessmentId.value,
        userId: Number((getUserSession && getUserSession() || {}).userId || userStore.userId || 0),
        jobId: jobId.value,
        fileName: resumeFile.value ? resumeFile.value.name : '简历文件',
        fileSize: resumeFile.value ? resumeFile.value.size : 0,
        fileType: resumeFile.value ? resumeFile.value.name.split('.').pop().toLowerCase() : 'unknown',
        overallScore: resumeScore.value,
        passScore: getStagePassScore('resume'),
        passScores: getAssessmentThresholds().stages,
        aiAnalysis: buildResumeAnalysisSummary(),
        aiSuggestions: JSON.stringify({ 
          suggestions: suggestions.value && suggestions.value.length > 0 ? suggestions.value : [
            'AI建议数据暂未生成，请重新进行简历评测'
          ]
        })
      };
      
      console.log('[AI Resume] 准备保存流程归档:', historyData);
      
      const result = await saveAiResumeHistory(historyData);
      console.log('[AI Resume] 流程归档保存成功:', result);
      
      uni.showToast({
        title: '流程已归档',
        icon: 'success',
        duration: 1000
      });
    } catch (historyError) {
      console.error('[AI Resume] 保存流程归档失败:', historyError);
      // 流程归档失败不影响主流程，只记录错误
      uni.showToast({
        title: '流程归档失败，但不影响当前流转',
        icon: 'none',
        duration: 2000
      });
    }
    
    // 多重保存当前jobId
    if (jobId.value) {
      uni.setStorageSync('currentJobId', jobId.value);
      try {
        sessionStorage.setItem('currentJobId', jobId.value);
        localStorage.setItem('currentJobId', jobId.value);
      } catch (e) {
        console.warn('保存jobId到Storage失败:', e);
      }
    }
    
    // 显示完成提示
    uni.showToast({
      title: '简历评测已完成',
      icon: 'success',
      duration: 800
    });
    
    // 修复跳转问题 - 使用多种方法确保跳转成功
    console.log('[Comprehensive Resume] 开始尝试跳转回AI面试页面');
    
    // 方法1：尝试使用uni.redirectTo直接跳转
    try {
      const targetUrl = buildNextComprehensiveStepUrl();
      uni.redirectTo({
        url: targetUrl,
        success: () => {
          console.log('[Comprehensive Resume] 完成后跳转成功 (redirectTo)');
        },
        fail: (err) => {
          console.error('[Comprehensive Resume] redirectTo跳转失败:', err);
          try {
            router.replace(targetUrl);
          } catch (routerErr) {
            window.location.href = `/#${targetUrl}`;
          }
        }
      });
    } catch (error) {
      console.error('[Comprehensive Resume] 跳转过程中出错:', error);
      
      // 备用方案：如果所有方法都失败，尝试最基本的router跳转
      router.push(buildNextComprehensiveStepUrl());
    }
    
  } catch (error) {
    console.error('[Comprehensive Resume] 更新状态失败:', error);
    uni.showToast({ title: '状态保存失败，请重试', icon: 'error' });
    
    // 即使保存失败，也尝试返回，不阻断用户流程
    router.replace(buildNextComprehensiveStepUrl());
  }
};

// 从URL和本地存储中获取jobId
const getJobIdFromUrlOrStorage = () => {
  let urlJobId = null;
  
  // 首先尝试从URL参数中获取（兼容uni-app和原生Web环境）
  try {
    const pageParams = plus?.webview?.currentWebview()?.extras;
    if (pageParams && pageParams.jobId) {
      console.log('从当前webview参数中获取到jobId:', pageParams.jobId);
      urlJobId = pageParams.jobId;
      uni.setStorageSync('currentJobId', urlJobId);
      return urlJobId;
    }
  } catch (err) {
    console.error('尝试从webview参数中获取jobId失败:', err);
  }
  
  try {
    const launchOptions = uni.getLaunchOptionsSync();
    if (launchOptions && launchOptions.query && launchOptions.query.jobId) {
      urlJobId = launchOptions.query.jobId;
      console.log('从启动参数中获取到jobId:', urlJobId);
      uni.setStorageSync('currentJobId', urlJobId);
      return urlJobId;
    }

    // 标准Web环境下解析URL参数
    const currentUrl = window.location.href;
    console.log('当前URL:', currentUrl);
    
    // 尝试处理常规的query参数
    const queryMatch = currentUrl.match(/[?&]jobId=([^&#]*)/);
    if (queryMatch && queryMatch[1]) {
      urlJobId = queryMatch[1];
      console.log('从URL参数中解析到jobId:', urlJobId);
      uni.setStorageSync('currentJobId', urlJobId);
      return urlJobId;
    }
    
    // 尝试处理hash路由中的参数
    const hashMatch = currentUrl.match(/#.*[?&]jobId=([^&#]*)/);
    if (hashMatch && hashMatch[1]) {
      urlJobId = hashMatch[1];
      console.log('从Hash URL参数中解析到jobId:', urlJobId);
      uni.setStorageSync('currentJobId', urlJobId);
      return urlJobId;
    }
  } catch (err) {
    console.error('解析URL参数失败:', err);
  }
  
  // 然后尝试从storage中获取
  try {
    const savedJobId = uni.getStorageSync('currentJobId');
    if (savedJobId) {
      console.log('从Storage获取到jobId:', savedJobId);
      return savedJobId;
    }
  } catch (error) {
    console.error('读取Storage中jobId失败:', error);
  }
  
  console.warn('未能获取到jobId，这可能影响简历分析的准确性');
  return null;
};

onLoad((options) => {
  console.log('ai-resume onLoad触发, options:', options);

  // 优先使用onLoad中传递的jobId
  if (options && options.jobId) {
    jobId.value = options.jobId;
    console.log('从onLoad获取jobId:', jobId.value);
    // 同时更新存储
    uni.setStorageSync('currentJobId', jobId.value);
    // 同时存储在sessionStorage中，确保页面刷新也能获取
    try {
      sessionStorage.setItem('currentJobId', jobId.value);
    } catch (e) {
      console.warn('保存jobId到sessionStorage失败:', e);
    }
  } else {
    // 否则从URL或存储中获取
    const id = getJobIdFromUrlOrStorage();
    if (id) {
      jobId.value = id;
    } else {
      // 如果依然没有jobId，显示提示
      uni.showToast({
        title: '未检测到职位信息，可能影响分析准确度',
        icon: 'none',
        duration: 3000
      });
    }
  }
  if (options && options.chatId) {
    chatId.value = String(options.chatId || '').trim();
  }
  initializeAssessmentSession(String(options?.assessmentId || '').trim());
  
  console.log('最终使用的jobId:', jobId.value);
  
  // 如果有jobId，预先获取职位信息以备后用
  if (jobId.value) {
    getJobDetails(jobId.value).then(details => {
      if (details) {
        console.log('成功获取职位详情:', details.name);
        // 设置显示的职位名称
        jobName.value = details.name || '未知职位';
        // 缓存职位名称，避免多次请求
        uni.setStorageSync('jobName_' + jobId.value, jobName.value);
      } else {
        // 尝试从缓存获取职位名称
        const cachedName = uni.getStorageSync('jobName_' + jobId.value);
        if (cachedName) {
          jobName.value = cachedName;
          console.log('从缓存中获取职位名称:', jobName.value);
        }
      }
    });
  }
});

onBeforeUnmount(() => {
  clearInterval(pollingInterval);
  clearTimeout(returnToAiTimer);
});

onMounted(() => {
  console.log('组件挂载 - 开始初始化');
  // 如果onLoad没有获取到jobId，在这里再次尝试
  if (!jobId.value) {
    const id = getJobIdFromUrlOrStorage();
    if (id) {
      jobId.value = id;
      console.log('onMounted中设置jobId:', jobId.value);
      
      // 尝试获取职位名称
      if (!jobName.value) {
        // 先尝试从缓存获取
        const cachedName = uni.getStorageSync('jobName_' + jobId.value);
        if (cachedName) {
          jobName.value = cachedName;
          console.log('onMounted从缓存获取职位名称:', jobName.value);
        } else {
          // 如果没有缓存，则请求API
          getJobDetails(jobId.value).then(details => {
            if (details && details.name) {
              jobName.value = details.name;
              uni.setStorageSync('jobName_' + jobId.value, jobName.value);
              console.log('onMounted从API获取职位名称:', jobName.value);
            }
          });
        }
      }
    }
  } else if (!jobName.value) {
    // jobId存在但没有jobName，尝试获取
    const cachedName = uni.getStorageSync('jobName_' + jobId.value);
    if (cachedName) {
      jobName.value = cachedName;
      console.log('使用缓存的职位名称:', jobName.value);
    } else {
      getJobDetails(jobId.value).then(details => {
        if (details && details.name) {
          jobName.value = details.name;
          uni.setStorageSync('jobName_' + jobId.value, jobName.value);
          console.log('获取到职位名称:', jobName.value);
        }
      });
    }
  }

});

</script>

<style lang="scss" scoped>
@use "sass:math";
@use "sass:string";
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css');

@function multiple-box-shadow($n) {
  $value: '#{math.random(2560)}px #{math.random(2560)}px #FFF';
  @for $i from 2 through $n {
    $value: '#{$value}, #{math.random(2560)}px #{math.random(2560)}px #FFF';
  }
  @return string.unquote($value);
}

$shadows-small: multiple-box-shadow(1000);
$shadows-medium: multiple-box-shadow(300);
$shadows-large: multiple-box-shadow(150);

.ai-resume-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #0a1929 0%, #1a1f35 100%);
  font-family: 'Arial', sans-serif;
  color: #ffffff;
  position: relative;
  overflow: hidden;
  padding: 20px;
  box-sizing: border-box;
}

#stars, #stars2, #stars3 {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: auto;
  height: auto;
  display: block;
  z-index: 0;
  transform: translateZ(0);
  background: transparent;
  pointer-events: none;
}

#stars {
  width: 1px;
  height: 1px;
  box-shadow: $shadows-small;
  animation: animStar 150s linear infinite;
}

#stars2 {
  width: 2px;
  height: 2px;
  box-shadow: $shadows-medium;
  animation: animStar 100s linear infinite;
}

#stars3 {
  width: 3px;
  height: 3px;
  box-shadow: $shadows-large;
  animation: animStar 50s linear infinite;
}

@keyframes animStar {
  from {
    transform: translateY(0px);
  }
  to {
    transform: translateY(-2560px);
  }
}

/* 科幻背景效果 */
.cyber-background {
  display: none;
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
}

.grid-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(0, 255, 255, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 255, 255, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}

.scanning-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.horizontal-scan {
  position: absolute;
    top: 50%;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, 
    rgba(0, 255, 255, 0) 0%, 
    rgba(0, 255, 255, 0.5) 20%, 
    rgba(0, 255, 255, 0.8) 50%,
    rgba(0, 255, 255, 0.5) 80%,
    rgba(0, 255, 255, 0) 100%);
  box-shadow: 0 0 10px rgba(0, 255, 255, 0.8);
  animation: horizontalScan 8s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}

@keyframes horizontalScan {
  0% { transform: translateY(-50vh); }
  50% { transform: translateY(50vh); }
  100% { transform: translateY(-50vh); }
}

.vertical-scan {
  position: absolute;
  top: 0;
  left: 50%;
  width: 2px;
  height: 100%;
  background: linear-gradient(0deg, 
    rgba(0, 255, 255, 0) 0%, 
    rgba(0, 255, 255, 0.5) 20%, 
    rgba(0, 255, 255, 0.8) 50%,
    rgba(0, 255, 255, 0.5) 80%,
    rgba(0, 255, 255, 0) 100%);
  box-shadow: 0 0 10px rgba(0, 255, 255, 0.8);
  animation: verticalScan 12s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}

@keyframes verticalScan {
  0% { transform: translateX(-50vw); }
  50% { transform: translateX(50vw); }
  100% { transform: translateX(-50vw); }
}

.floating-particles {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.particle {
  position: absolute;
  width: 2px;
  height: 2px;
  background: #00ffff;
  border-radius: 50%;
  top: var(--y-pos, 100%);
  left: var(--x-pos, 0);
    opacity: 0;
  box-shadow: 0 0 2px #00ffff;
  animation: floatParticle 10s ease-out infinite;
  animation-delay: var(--delay, 0s);
}

@keyframes floatParticle {
  0% {
    transform: translate(0, 0);
    opacity: 0;
  }
  10% {
    opacity: 0.8;
  }
  90% {
    opacity: 0.6;
  }
  100% {
    transform: translate(var(--x-end, 100px), -30vh);
    opacity: 0;
  }
}

.energy-field {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(0, 255, 255, 0.1) 0%, transparent 70%);
  animation: pulse 4s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.3; }
  50% { transform: translate(-50%, -50%) scale(1.2); opacity: 0.6; }
}

.content-card {
  position: relative;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.10);
  border: 1px solid rgba(186, 230, 253, 0.24);
  border-radius: 24px;
  backdrop-filter: blur(18px) saturate(145%);
  -webkit-backdrop-filter: blur(18px) saturate(145%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.14);
  z-index: 1;
  padding: 30px;
  animation: cardFadeIn 0.8s ease-out;
  overflow: hidden;
  color: #fff;
}

@keyframes cardFadeIn {
  from { transform: translateY(20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.top-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 30px;
  position: relative;
}

.back-button {
  display: flex;
  align-items: center;
  background: rgba(0, 255, 255, 0.2);
  border: 2px solid rgba(0, 255, 255, 0.5);
  border-radius: 8px;
  padding: 10px 18px;
  color: #00ffff;
  cursor: pointer;
  transition: all 0.3s ease;
  position: absolute;
  left: 0;
  z-index: 100;
  margin-right: 20px;
  box-shadow: 0 0 10px rgba(0, 255, 255, 0.2);
}

.back-button:hover {
  background: rgba(0, 255, 255, 0.3);
  transform: translateX(-5px);
  box-shadow: 0 0 15px rgba(0, 255, 255, 0.4);
}

.back-icon { color: #00ffff; filter: drop-shadow(0 0 8px rgba(0,255,255,0.5)); margin-right: 8px; transition: transform 0.3s ease; }
.back-button:hover .back-icon { transform: translateX(-2px); }

/* 全息标题 */
.holographic-header {
  text-align: center;
  position: relative;
  }

  .page-title {
  font-size: 2.5em;
    font-weight: bold;
  background: linear-gradient(45deg, #00ffff, #ffffff, #0080ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  margin: 0;
  letter-spacing: 2px;
  text-transform: uppercase;
  margin-right: 20px;
  text-align: left;
}

.title-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, #00ffff, #ffffff, #0080ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: blur(10px);
  opacity: 0.5;
  animation: titleGlow 3s ease-in-out infinite;
}

@keyframes titleGlow {
  0%, 100% { opacity: 0.3; filter: blur(8px); }
  50% { opacity: 0.7; filter: blur(12px); }
}

/* 进度条区域 */
.progress-section {
  margin: 30px 0;
  position: relative;
}

  .progress-bar-wrapper {
  width: 100%;
  height: 10px;
  background: rgba(0, 255, 255, 0.1);
  border-radius: 5px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #00ffff, #0080ff);
  border-radius: 5px;
  transition: width 0.5s ease;
  position: relative;
}

.progress-pulse {
  position: absolute;
  top: 0;
  right: 0;
  width: 20px;
  height: 100%;
  background: rgba(255, 255, 255, 0.8);
  filter: blur(5px);
  border-radius: 5px;
  animation: progressPulse 1.5s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes progressPulse {
  0%, 100% { opacity: 0; }
  50% { opacity: 1; }
}

.progress-text {
  position: absolute;
  right: 0;
  top: -25px;
  font-size: 16px;
    font-weight: bold;
  color: #00ffff;
}

/* 步骤指示器 */
.analysis-steps {
  display: flex;
  justify-content: space-between;
  margin: 40px 0;
  padding: 0 20px;
  position: relative;
}

.analysis-steps::before {
  content: '';
  position: absolute;
  top: 15px;
  left: 40px;
  right: 40px;
  height: 2px;
  background: rgba(0, 255, 255, 0.3);
  z-index: 1;
}

  .step-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
  z-index: 2;
  flex: 1;
}

    .step-circle {
  width: 30px;
  height: 30px;
      border-radius: 50%;
  background: rgba(0, 10, 30, 0.7);
  border: 2px solid rgba(0, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
      font-weight: bold;
  margin-bottom: 10px;
  transition: all 0.5s ease;
    }

    .step-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  text-align: center;
}

.step-item.active .step-circle {
  background: #00ffff;
  color: #000;
  border-color: #00ffff;
  box-shadow: 0 0 15px rgba(0, 255, 255, 0.8);
}

.step-item.active .step-label {
  color: #00ffff;
}

.step-item.current .step-circle {
  animation: stepPulse 1s ease-in-out infinite;
}

@keyframes stepPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

/* 护眼模式下的步骤圆圈样式 */
.eye-care .step-circle {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(76, 175, 80, 0.6);
  color: #2e7d32;
  box-shadow: 0 0 8px rgba(76, 175, 80, 0.3);
}

.eye-care .step-item.active .step-circle {
  background: rgba(76, 175, 80, 0.8);
  color: #ffffff;
  border-color: rgba(76, 175, 80, 0.8);
  box-shadow: 0 0 15px rgba(76, 175, 80, 0.6);
}

.eye-care .step-item.active .step-label {
  color: #2e7d32;
}

.eye-care .step-label {
  color: rgba(0, 0, 0, 0.7);
}

.upload-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  
  .job-info-banner {
    background: rgba(0, 255, 255, 0.1);
    border: 1px solid rgba(0, 255, 255, 0.3);
    border-radius: 8px;
    padding: 10px 15px;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    font-size: 16px;
    
    i {
      margin-right: 8px;
      color: #00ffff;
    }
    
    b {
      color: #00ffff;
      font-weight: bold;
      margin: 0 4px;
      text-shadow: 0 0 5px rgba(0, 255, 255, 0.5);
    }
  }
}

.upload-box {
  border: 4rpx dashed #a8c0ff;
  border-radius: 20rpx;
  padding: 60rpx;
  width: 80%;
  max-width: 600rpx;
  margin: 0 auto;
  background-color: #f8faff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;

  &:hover, &.drag-over {
    border-color: #6a82fb;
    background-color: #eef2ff;
    box-shadow: 0 5rpx 20rpx rgba(106, 130, 251, 0.2);
  }
}

.upload-area {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 16rpx;
  padding: 20px;
}

.upload-icon {
    font-size: 100rpx;
    color: #6a82fb;
    margin-bottom: 20rpx;
    animation: bounceIn 0.8s ease-out;
}

  h3 {
    font-size: 40rpx;
    color: #333;
    margin-bottom: 15rpx;
    font-weight: 600;
}

  .supported-formats {
    font-size: 28rpx;
    color: #666;
    margin-bottom: 10rpx;
}

.upload-hint {
    font-size: 24rpx;
    color: #999;
    margin-top: 15rpx;
  }

.error-message {
  color: #dc3545;
  margin-top: 20rpx;
  font-size: 28rpx;
  font-weight: bold;
}

@keyframes bounceIn {
  0% { transform: scale(0.3); opacity: 0; }
  50% { transform: scale(1.05); opacity: 1; }
  70% { transform: scale(0.9); }
  100% { transform: scale(1); }
}

.analysis-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.file-preview-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f0f2f5;
  padding: 30rpx 40rpx;
  border-radius: 16rpx;
  margin-bottom: 40rpx;
  width: 90%;
  max-width: 700rpx;
  box-shadow: 0 5rpx 15rpx rgba(0, 0, 0, 0.08);
}

.file-icon {
    margin-right: 25rpx;
    position: relative;
    width: 60px;
    height: 60px;
}

/* 自定义文件图标 */
.custom-file-icon {
  position: relative;
  width: 50px;
  height: 60px;
}

.file-document {
  position: relative;
  width: 40px;
  height: 50px;
  background: linear-gradient(135deg, #ffffff, #f8f9fa);
  border: 2px solid #e9ecef;
  border-radius: 4px;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
  transform: rotate(-5deg);
  overflow: hidden;
}

.file-body {
  position: absolute;
  width: 100%;
  height: 100%;
  background: #ffffff;
  border-radius: 2px;
}

.file-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 8px;
  background: linear-gradient(90deg, #00ffff, #0080ff);
  border-radius: 2px 2px 0 0;
}

.file-content {
  position: absolute;
  top: 12px;
  left: 4px;
  right: 4px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.content-line {
  height: 1px;
  background: #dee2e6;
  border-radius: 0.5px;
}

.content-line:nth-child(1) { width: 70%; }
.content-line:nth-child(2) { width: 50%; }
.content-line:nth-child(3) { width: 60%; }

.file-fold {
  position: absolute;
  top: 0;
  right: 0;
  width: 0;
  height: 0;
  border-left: 8px solid transparent;
  border-bottom: 8px solid #f8f9fa;
  border-radius: 0 2px 0 0;
}

.file-extension {
  position: absolute;
  bottom: 2px;
  right: 2px;
  font-size: 8px;
  font-weight: bold;
  color: #ffffff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.8);
  letter-spacing: 0.8px;
  background: linear-gradient(135deg, #00ffff, #0080ff);
  padding: 2px 4px;
  border-radius: 3px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  font-family: 'Courier New', monospace;
  text-transform: uppercase;
}

  .file-details {
    flex-grow: 1;
    text-align: left;

    h4 {
  margin: 0;
      font-size: 32rpx;
      color: #333;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    p {
      margin: 5rpx 0 0;
      font-size: 24rpx;
      color: #777;
    }
}

  .remove-file-button {
  background: none;
  border: none;
    color: #dc3545;
    font-size: 36rpx;
  cursor: pointer;
    margin-left: 20rpx;
    transition: transform 0.2s ease-in-out;

    &:hover {
      transform: scale(1.1);
    }
  }

.status-display {
  margin-bottom: 40rpx;
  width: 100%;

  .loading-animation-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
    gap: 20rpx;

    .cyber-loader {
      position: relative;
      width: 60rpx;
      height: 60rpx;

      .cyber-spinner {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
      border: 8rpx solid #f3f3f3;
      border-top: 8rpx solid #3498db;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      }

      .cyber-spinner-inner {
        position: absolute;
        top: 8rpx;
        left: 8rpx;
        width: 80%;
        height: 80%;
        border: 8rpx solid #f3f3f3;
        border-top: 8rpx solid #3498db;
        border-radius: 50%;
        animation: spin-inner 1s linear infinite;
      }
}

    .status-message {
      font-size: 32rpx;
      color: #555;
      font-weight: 500;
    }
  }

  .analysis-complete-message {
  display: flex;
    flex-direction: column;
  align-items: center;
    gap: 15rpx;

    .success-icon {
      font-size: 100rpx;
      color: #28a745;
      animation: fadeIn 0.5s ease-out;
}

    p {
      font-size: 36rpx;
      color: #28a745;
      font-weight: bold;
}
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes spin-inner {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.action-buttons-analysis {
  display: flex;
  gap: 20rpx;
  justify-content: center;
  width: 100%;

  button {
    padding: 20rpx 40rpx;
    border-radius: 12rpx;
    font-size: 32rpx;
    cursor: pointer;
    transition: all 0.3s ease;
    border: none;
    font-weight: bold;

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
}

  .view-report-button {
    background: linear-gradient(45deg, #28a745, #218838);
    color: white;
    padding: 12px 30px;
    font-size: 16px;
    font-weight: bold;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 4px 10px rgba(40, 167, 69, 0.3);
  }

  .view-report-button:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 15px rgba(40, 167, 69, 0.4);
  }

  .eye-care .view-report-button {
    background: linear-gradient(45deg, #3e8e41, #6aaa6d);
    box-shadow: 0 4px 10px rgba(62, 142, 65, 0.3);
  }

  .eye-care .view-report-button:hover {
    box-shadow: 0 8px 15px rgba(62, 142, 65, 0.4);
  }
}

.results-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.result-header {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 30rpx;
  margin-bottom: 40rpx;
  width: 100%;

  h2 {
    font-size: 48rpx;
    color: #00ffff;
  margin: 0;
    text-shadow: 0 0 10px rgba(0, 255, 255, 0.3);
}

.score-badge {
    padding: 15rpx 30rpx;
    border-radius: 50rpx;
    font-size: 40rpx;
    font-weight: bold;
    color: #fff;
    min-width: 150rpx;
    text-align: center;
    box-shadow: 0 5rpx 15rpx rgba(0, 0, 0, 0.2);
}

  .score-excellent { background: linear-gradient(45deg, #28a745, #218838); }
  .score-good { background: linear-gradient(45deg, #007bff, #0056b3); }
  .score-fair { background: linear-gradient(45deg, #ffc107, #e0a800); }
  .score-poor { background: linear-gradient(45deg, #dc3545, #c82333); }
}

.match-breakdown {
  width: 92%;
  max-width: 950px;
  margin: 0 auto 32px auto;

  h3 {
    font-size: 36rpx;
    color: #00ffff;
    margin-bottom: 25rpx;
    border-bottom: 2rpx solid rgba(0, 255, 255, 0.3);
    padding-bottom: 10rpx;
    text-shadow: 0 0 10px rgba(0, 255, 255, 0.3);
}

.match-item {
  display: flex;
  align-items: center;
    margin-bottom: 20rpx;
    font-size: 30rpx;

.match-label {
      min-width: 180rpx;
      color: rgba(255, 255, 255, 0.9);
      font-weight: 500;
}

    .match-bar-container {
      flex-grow: 1;
      height: 14rpx;
      background-color: #e9ecef;
      border-radius: 7rpx;
  overflow: hidden;
      margin: 0 20rpx;
}

    .match-bar-fill {
  height: 100%;
      background: linear-gradient(to right, #6a82fb, #a8c0ff);
      border-radius: 7rpx;
      transition: width 0.8s ease-out;
}

.match-value {
      font-weight: bold;
      color: #00ffff;
      min-width: 80rpx;
  text-align: right;
    }
  }
}

.suggestions-list {
  width: 92%;
  max-width: 950px;
  margin: 0 auto 32px auto;

  h3 {
    font-size: 36rpx;
    color: #00ffff;
    margin-bottom: 25rpx;
    border-bottom: 2rpx solid rgba(0, 255, 255, 0.3);
    padding-bottom: 10rpx;
    text-shadow: 0 0 10px rgba(0, 255, 255, 0.3);
  }

  ul {
  list-style: none;
    padding: 0;
    margin: 0;
  }

  li {
    background: rgba(0, 255, 255, 0.1);
    border: 1px solid rgba(0, 255, 255, 0.3);
    border-radius: 8px;
    padding: 15px;
    font-size: 14px;
    color: #fff;
    position: relative;
  display: flex;
    align-items: center;
    pointer-events: none;
  }
}

/* 护眼模式样式 */
.eye-care {
  .result-header h2 {
    color: #3e8e41;
    text-shadow: 0 0 10px rgba(62, 142, 65, 0.3);
  }

  .match-breakdown {
    h3 {
      color: #3e8e41;
      border-bottom-color: rgba(62, 142, 65, 0.3);
      text-shadow: 0 0 10px rgba(62, 142, 65, 0.3);
  }

    .match-label {
      color: rgba(51, 51, 51, 0.9);
    }

    .match-value {
      color: #3e8e41;
    }
}

  .suggestions-list {
    h3 {
      color: #3e8e41;
      border-bottom-color: rgba(62, 142, 65, 0.3);
      text-shadow: 0 0 10px rgba(62, 142, 65, 0.3);
  }

    li {
      background: rgba(62, 142, 65, 0.1);
      border-color: rgba(62, 142, 65, 0.3);
      color: #333;
    }
  }
}


@keyframes scanLine {
  0% { transform: translateY(0); opacity: 0; }
  50% { opacity: 1; }
  100% { transform: translateY(300px); opacity: 0; }
}

.upload-icon .icon-pulse {
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  border: 2px solid rgba(0, 255, 255, 0.3);
  border-radius: 50%;
  animation: iconPulse 2s ease-in-out infinite;
}

@keyframes iconPulse {
  0% { transform: scale(1); opacity: 0.7; }
  50% { transform: scale(1.2); opacity: 0.3; }
  100% { transform: scale(1); opacity: 0.7; }
}

.upload-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at center, rgba(0, 255, 255, 0.1) 0%, transparent 70%);
  animation: uploadGlow 3s ease-in-out infinite;
}

@keyframes uploadGlow {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.7; }
}

/* 护眼模式样式适配 */
.eye-care {
  background: linear-gradient(135deg, #f0f2e6 0%, #e8ecd9 50%, #dfe3d0 100%) !important;
}

.eye-care .grid-lines {
  background-image: 
    linear-gradient(rgba(62, 142, 65, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(62, 142, 65, 0.1) 1px, transparent 1px);
}

.eye-care .horizontal-scan,
.eye-care .vertical-scan {
  background: linear-gradient(90deg, 
    rgba(62, 142, 65, 0) 0%, 
    rgba(62, 142, 65, 0.3) 20%, 
    rgba(62, 142, 65, 0.5) 50%,
    rgba(62, 142, 65, 0.3) 80%,
    rgba(62, 142, 65, 0) 100%);
  box-shadow: 0 0 10px rgba(62, 142, 65, 0.3);
}

.eye-care .particle {
  background: rgba(62, 142, 65, 0.5);
  box-shadow: 0 0 2px rgba(62, 142, 65, 0.5);
}

.eye-care .energy-field {
  background: radial-gradient(circle, rgba(62, 142, 65, 0.1) 0%, transparent 70%);
}

/* 护眼模式下的自定义图标 */
.eye-care .resume-document {
  background: linear-gradient(135deg, #ffffff, #f8f9fa);
  border-color: #3e8e41;
  box-shadow: 0 4px 12px rgba(62, 142, 65, 0.2);
}

.eye-care .document-header {
  background: linear-gradient(90deg, #3e8e41, #2d5a2d);
}



.eye-care .file-document {
  background: linear-gradient(135deg, #ffffff, #f8f9fa);
  border-color: #3e8e41;
  box-shadow: 0 3px 8px rgba(62, 142, 65, 0.2);
}

.eye-care .file-header {
  background: linear-gradient(90deg, #3e8e41, #2d5a2d);
}

.eye-care .file-extension {
  color: #ffffff;
  background: linear-gradient(135deg, #3e8e41, #2d5a2d);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.8);
  border-color: rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.eye-care .content-card {
  background: rgba(248, 250, 240, 0.9);
  border-color: rgba(62, 142, 65, 0.3);
  box-shadow: 0 0 30px rgba(62, 142, 65, 0.2);
  color: #333;
}

.eye-care .back-button {
  background: rgba(76, 175, 80, 0.3);
  border-color: rgba(76, 175, 80, 0.6);
  color: #2e7d32;
  box-shadow: 0 0 12px rgba(76, 175, 80, 0.4);
}

.eye-care .back-button:hover {
  background: rgba(76, 175, 80, 0.5);
  border-color: rgba(76, 175, 80, 0.8);
  color: #1b5e20;
  box-shadow: 0 0 18px rgba(76, 175, 80, 0.6);
  transform: translateX(-5px);
}

.eye-care .back-icon { color: #2e7d32; filter: drop-shadow(0 0 8px rgba(76,175,80,0.5)); }
.eye-care .back-button:hover .back-icon { transform: translateX(-2px); }

.eye-care .page-title {
  background: linear-gradient(45deg, #3e8e41, #2d5a2d, #357a38);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.eye-care .title-glow {
  background: linear-gradient(45deg, #3e8e41, #2d5a2d, #357a38);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.suggestions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
  list-style-type: none;
  padding: 0;
  margin: 20px 0;
}

.suggestion-item {
  background: rgba(0, 255, 255, 0.1);
  border: 1px solid rgba(0, 255, 255, 0.3);
  border-radius: 8px;
  padding: 12px 15px;
  font-size: 13px;
  color: #fff;
  position: relative;
  display: flex;
  align-items: flex-start;
  min-width: 280px;
  line-height: 1.4;
}

.eye-care .suggestion-item {
  background: rgba(62, 142, 65, 0.1);
  border-color: rgba(62, 142, 65, 0.3);
  color: #333;
}

/* 移除所有可能影响的hover样式 */
.suggestion-item:hover,
.suggestion-item:active,
.suggestion-item:focus,
.eye-care .suggestion-item:hover,
.eye-care .suggestion-item:active,
.eye-care .suggestion-item:focus {
  all: unset;
  background: rgba(0, 255, 255, 0.1);
  border: 1px solid rgba(0, 255, 255, 0.3);
  border-radius: 8px;
  padding: 15px;
  font-size: 14px;
  color: #fff;
  position: relative;
  display: flex;
  align-items: center;
}

.eye-care .suggestion-item:hover,
.eye-care .suggestion-item:active,
.eye-care .suggestion-item:focus {
  background: rgba(62, 142, 65, 0.1);
  border-color: rgba(62, 142, 65, 0.3);
  color: #333;
}

.action-buttons-results {
  display: flex;
  gap: 30rpx;
  justify-content: center;
  width: 100%;

  button {
    padding: 25rpx 50rpx;
    border-radius: 15rpx;
    font-size: 36rpx;
    cursor: pointer;
    transition: all 0.3s ease;
    border: none;
    font-weight: bold;
    box-shadow: 0 5rpx 15rpx rgba(0, 0, 0, 0.1);

    &:hover {
      transform: translateY(-3rpx);
      box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.2);
    }
}

  .view-report-button {
    background: linear-gradient(45deg, #007bff, #00c6ff);
    color: #fff;
    font-size: 40rpx;
    padding: 30rpx 80rpx;
    min-width: 300rpx;
  }
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .content-card {
    padding: 30rpx;
  }

  .top-bar {
    flex-direction: column;
    .page-title {
      margin-top: 20rpx;
      font-size: 40rpx;
    }
    .placeholder {
      display: none;
    }
  }

  .analysis-steps {
    flex-wrap: wrap;
    .step-item {
      width: 50%;
      margin-bottom: 20rpx;
      &:nth-child(odd)::after {
        left: calc(100% + 10rpx);
        width: 20rpx;
      }
      &:nth-child(even)::after {
        display: none;
      }
    }
  }

  .upload-box {
    padding: 40rpx;
}

  .file-preview-card {
    flex-direction: column;
    .file-icon {
      margin-bottom: 15rpx;
      margin-right: 0;
    }
    .file-details {
      text-align: center;
    }
  }

  .action-buttons-analysis,
  .action-buttons-results {
    flex-direction: column;
    button {
      width: 100%;
    }
  }

  .result-header {
    flex-direction: column;
    gap: 20rpx;
  }

  .match-breakdown,
  .suggestions-list {
    padding: 0 20rpx;
  }
}

.upload-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  position: relative;
  width: 72px;
  height: 72px;
}

/* 自定义上传图标 */
.custom-upload-icon {
  position: relative;
  width: 80px;
  height: 80px;
  z-index: 2;
}

/* 简历文档主体 */
.resume-document {
  position: absolute;
  width: 50px;
  height: 65px;
  background: linear-gradient(135deg, #ffffff, #f8f9fa);
  border: 2px solid #e9ecef;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: rotate(-8deg);
  overflow: hidden;
}

.document-body {
  position: absolute;
  width: 100%;
  height: 100%;
  background: #ffffff;
  border-radius: 4px;
}

.document-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 12px;
  background: linear-gradient(90deg, #00ffff, #0080ff);
  border-radius: 4px 4px 0 0;
}

.document-lines {
  position: absolute;
  top: 20px;
  left: 8px;
  right: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.line {
  height: 2px;
  background: #dee2e6;
  border-radius: 1px;
}

.line:nth-child(1) { width: 80%; }
.line:nth-child(2) { width: 60%; }
.line:nth-child(3) { width: 70%; }

.document-fold {
  position: absolute;
  top: 0;
  right: 0;
  width: 0;
  height: 0;
  border-left: 15px solid transparent;
  border-bottom: 15px solid #f8f9fa;
  border-radius: 0 4px 0 0;
}



/* 悬停效果 */
.upload-box:hover .custom-upload-icon {
  transform: scale(1.05);
  transition: transform 0.3s ease;
}

.upload-box:hover .resume-document {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.25);
  transition: box-shadow 0.3s ease;
}



.file-preview-card:hover .custom-file-icon {
  transform: scale(1.05);
  transition: transform 0.3s ease;
}

.file-preview-card:hover .file-document {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transition: box-shadow 0.3s ease;
}
/* 去除icon-pulse动画和背景 */
.upload-icon .icon-pulse {
  display: none;
}
.upload-icon .icon-pulse {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 72px;
  height: 72px;
  background: radial-gradient(circle, rgba(0,255,255,0.18) 0%, rgba(0,255,255,0.05) 80%, transparent 100%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  z-index: 1;
  animation: pulse-upload 1.8s infinite;
}
@keyframes pulse-upload {
  0% { opacity: 0.7; transform: translate(-50%, -50%) scale(1); }
  70% { opacity: 0.15; transform: translate(-50%, -50%) scale(1.25); }
  100% { opacity: 0.7; transform: translate(-50%, -50%) scale(1); }
}
</style>

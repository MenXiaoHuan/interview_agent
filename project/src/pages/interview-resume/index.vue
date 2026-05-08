<template>
  <div class="resume-analyze-bg" :class="{ 'eye-care': isEyeCareMode }">

    <div id="stars"></div>
    <div id="stars2"></div>
    <div id="stars3"></div>
    
    <!-- 返回按钮 -->
    <div class="back-button" @click="goBack">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </div>
    
    <div :class="['resume-analyze-card', floatClass]">
      <h1>简历评测</h1>

      <!-- 职位选择提示 -->
      <div class="job-selection-alert" v-if="!currentJobId">
        <div class="alert-content">
          <p>请先选择职位</p>
          <button class="select-job-button" @click="goToJobSelection">选择职位</button>
        </div>
      </div>

      <div class="analysis-input-section" v-else>
        <!-- 职位信息展示 -->
        <div class="job-info-card" v-if="jobInfo">
          <h3>当前职位：</h3>
          <div class="job-details">
            <p class="job-title">{{ jobInfo.name }}</p>
            <p class="job-description-label">岗位描述/职责:</p>
            <p class="job-description-text">{{ jobInfo.description }}</p>
          </div>
        </div>

        <!-- 简历上传区域 -->
        <div class="upload-section" v-if="!isUploaded">
          <div class="upload-box" @click="handleUpload">
            <div class="upload-icon">
              <svg class="upload-svg" width="56" height="56" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="3" y="3" width="14" height="18" rx="3" stroke="currentColor" stroke-width="2"/>
                <path d="M7 7h8M7 10h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <p>点击上传简历</p>
            <p class="upload-tip">支持 PDF、Word 格式</p>
          </div>
        </div>

        <!-- 分析中/分析完成状态 -->
        <div class="analysis-status-area" v-if="isUploaded">
          <div class="analysis-status-card">
            <div class="analysis-status-row">
              <span class="analysis-status-text">{{ analysisStatus }}</span>
              <span v-if="isAnalyzing" class="dot-loading">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </span>
            </div>
            <!-- 添加分析进度展示 -->
            <div v-if="isAnalyzing" class="analysis-progress">
              <div class="progress-steps">
                <div v-for="(step, index) in analysisSteps" 
                     :key="index" 
                     class="progress-step"
                     :class="{ 'active': currentStep >= index, 'completed': currentStep > index }">
                  <div class="step-icon">
                    <i v-if="currentStep > index" class="step-check">✓</i>
                    <span v-else>{{ index + 1 }}</span>
                  </div>
                  <div class="step-label">{{ step }}</div>
                </div>
              </div>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
              </div>
              <div class="progress-tip">{{ currentTip }}</div>
            </div>
            <div class="score-display" v-if="isAnalysisComplete">
              <span class="score-label">评测得分：</span>
              <span class="score-value">{{ score }}</span>
            </div>
          </div>
          <!-- 其他分析结果和按钮 -->
          <div class="analysis-result" v-if="isAnalysisComplete">
            <h3>评测结果</h3>
            <div class="result-flex-row">
            
            
            <div class="feedback-section">
              <div class="advantage-box">
                <div class="box-header">
                  <svg class="box-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 2l2.7 5.5 6.1.9-4.4 4.3 1 6.1L12 16.9 6.6 18.8l1-6.1L3.2 8.4l6.1-.9L12 2Z" stroke="currentColor" stroke-width="1.6"/>
                  </svg>
                  <span class="box-title">优势分析</span>
                </div>
                <ul class="box-list">
                  <li class="box-item" v-for="(s,i) in advantagesList" :key="'adv-'+i">{{ s }}</li>
                </ul>
              </div>
              <div class="disadvantage-box">
                <div class="box-header">
                  <svg class="box-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 2a10 10 0 1 1 0 20 10 10 0 0 1 0-20Zm-5 10h10" stroke="currentColor" stroke-width="1.6"/>
                  </svg>
                  <span class="box-title">不足分析</span>
                </div>
                <ul class="box-list">
                  <li class="box-item" v-for="(s,i) in disadvantagesList" :key="'dis-'+i">{{ s }}</li>
                </ul>
              </div>
              <div class="suggestion-box">
                <div class="suggestion-header">
                  <svg class="suggestion-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 2a7 7 0 0 1 7 7c0 2.5-1.3 4.7-3.2 6l-.8.6V18a2 2 0 0 1-2 2h-1v1a1 1 0 0 1-1 1h-1a1 1 0 0 1-1-1v-1H8a2 2 0 0 1-2-2v-2.4l-.8-.6A7 7 0 0 1 5 9a7 7 0 0 1 7-7Z" stroke="currentColor" stroke-width="1.6"/>
                  </svg>
                  <span class="suggestion-title">改进建议</span>
                </div>
                <ul class="suggestion-list">
                  <li class="suggestion-item" v-for="(s,i) in improvementList" :key="i">{{ s }}</li>
                </ul>
              </div>
              </div>
            </div>
          </div>
          <div class="button-container">
            <template v-if="isAnalysisComplete">
            <button
                class="control-button secondary"
                @click="editResume"
              >
                修改简历
              </button>
              <button
                class="control-button primary"
                @click="completeAnalysis"
            >
              继续面试
            </button>
            </template>
          </div>
        </div>
      </div>
      <Modal
        :isVisible="showEditModal"
        @close="closeEditModal"
      >
        <template #header>
          <h3>编辑简历内容</h3>
        </template>
        <template #default>
          <textarea v-model="editResumeContent" class="resume-edit-textarea" rows="14" />
        </template>
        <template #footer>
          <button class="control-button secondary" @click="restoreResumeText" :disabled="isSaving" style="margin-right:12px;">恢复上次内容</button>
          <button class="control-button primary" @click="saveResumeEdit" :disabled="isSaving">
            <span v-if="isSaving">正在分析...</span>
            <span v-else>保存并再次分析</span>
          </button>
        </template>
      </Modal>
      <div v-if="isEditAnalyzing" class="edit-analyzing-tip">正在分析中...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, getCurrentInstance, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { uploadResume, getResumeAnalysisByXunfei, getResumeOriginal, updateResumeContent, restoreResumeContent, extractResumeContent, saveResumeHistory, getResumeHistoryList, restoreResumeContentByHistoryId, getResumeHistoryDetailById, getJobInfo } from '@/utils/api';
import { getUserSession } from '@/utils/user-session';
import { API } from '@/utils/api';

// 导入 Uni-app 页面生命周期钩子
import { onLoad, onShow, onHide } from '@dcloudio/uni-app';
import Modal from '@/components/Modal.vue';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);
const router = useRouter();

const route = useRoute();

const isUploaded = ref(false);
const isAnalyzing = ref(false);
const isAnalysisComplete = ref(false);
const score = ref(0);
const analysisStatus = ref('分析未开始');
const feedback = ref({
  advantages: '',
  disadvantages: '',
  improvementSuggestions: ''
});

const improvementList = computed(() => {
  const s = String(feedback.value.improvementSuggestions || '').trim();
  if (!s) return [];
  return s.split(/(?:；|;|。|\n)+/).map(it => String(it).trim()).filter(Boolean).slice(0, 8);
});
const advantagesList = computed(() => {
  const s = String(feedback.value.advantages || '').trim();
  if (!s) return [];
  return s.split(/(?:；|;|。|\n)+/).map(it => String(it).trim()).filter(Boolean).slice(0, 8);
});
const disadvantagesList = computed(() => {
  const s = String(feedback.value.disadvantages || '').trim();
  if (!s) return [];
  return s.split(/(?:；|;|。|\n)+/).map(it => String(it).trim()).filter(Boolean).slice(0, 8);
});

const competenciesScore = ref({
  professionalSkills: 0,
  projectExecution: 0,
  innovation: 0,
  communication: 0,
  adaptability: 0
});


// Chart.js instance

// 添加职位相关状态
const currentJobId = ref(null);
const fromAiInterview = ref(false);
const jobInfo = ref(null);
const getAiSessionId = () => {
  const routeSessionId = String(route.query.sessionId || '').trim();
  if (routeSessionId) {
    return routeSessionId;
  }
  try {
    return String(sessionStorage.getItem('activeAiConversationSessionId') || '').trim();
  } catch (_) {
    return '';
  }
};
const buildInterviewAiReturnUrl = ({ completedType = '', mode = '', score = 0, timestamp = '' } = {}) => {
  const query = [];
  query.push('autoOpen=1');
  const safeSessionId = getAiSessionId();
  const safeCompletedType = String(completedType || '').trim();
  const safeMode = String(mode || '').trim();
  const safeScore = Math.max(0, Number(score) || 0);
  const safeTimestamp = String(timestamp || '').trim();
  if (safeSessionId) {
    query.push(`sessionId=${encodeURIComponent(safeSessionId)}`);
  }
  if (safeCompletedType) {
    query.push(`completedType=${encodeURIComponent(safeCompletedType)}`);
  }
  if (safeMode) {
    query.push(`mode=${encodeURIComponent(safeMode)}`);
  }
  if (safeScore > 0) {
    query.push(`score=${encodeURIComponent(safeScore)}`);
  }
  if (safeTimestamp) {
    query.push(`timestamp=${encodeURIComponent(safeTimestamp)}`);
  }
  return `/pages/interview-ai/index${query.length ? `?${query.join('&')}` : ''}`;
};

// 添加轮询相关变量
const resumeId = ref(null);
const pollingInterval = ref(null);

// 添加分析步骤和提示
const analysisSteps = [
  '简历解析',
  '技能匹配',
  '经验评估',
  '能力分析',
  '生成报告'
];

const analysisTips = [
  '正在解析简历内容...',
  '正在分析专业技能匹配度...',
  '正在评估工作经验相关性...',
  '正在分析综合能力水平...',
  '正在生成详细分析报告...'
];

const currentStep = ref(0);
const currentTip = ref(analysisTips[0]);
const progressPercentage = ref(0);

const showEditModal = ref(false);
const editResumeContent = ref('');
const lastResumeContent = ref('');
const isSaving = ref(false);
const isEditAnalyzing = ref(false);

// 新增historyId用于同一测评流程多次分析只更新一条历史
const historyId = ref(null);

// === 新增动画 class 控制 ===
const floatClass = ref('resume-float-enter');

const parseResumeAnalysisResult = (raw) => {
  if (!raw) {
    throw new Error('AI返回内容为空');
  }
  if (typeof raw === 'object') {
    return raw;
  }
  try {
    let jsonStr = String(raw).trim();
    if (jsonStr.startsWith('```json')) {
      jsonStr = jsonStr.replace(/^```json/, '').replace(/```$/, '').trim();
    } else if (jsonStr.startsWith('```')) {
      jsonStr = jsonStr.replace(/^```/, '').replace(/```$/, '').trim();
    }
    return JSON.parse(jsonStr);
  } catch {
    throw new Error('AI返回内容格式错误，无法解析');
  }
};

// 获取职位信息
const fetchJobInfo = async (jobId) => {
  console.log('[Resume Analysis] fetchJobInfo started for jobId:', jobId);
  try {
    if (!jobId) {
      console.error('[Resume Analysis] fetchJobInfo called with missing jobId.');
      throw new Error('岗位ID缺失');
    }

    const response = await getJobInfo(jobId);
    if (response.code === 200 && response.data) {
      jobInfo.value = response.data; // 直接赋值整个 data 对象
      console.log('[Resume Analysis] Job info fetched successfully:', jobInfo.value);
    } else {
      throw new Error(response.message || '获取岗位信息失败');
    }
  } catch (err) {
    console.error('[Resume Analysis] Error fetching job info:', err);
    uni.showToast({
      title: err.message || '获取职位信息失败',
      icon: 'none'
    });
  }
};

// 跳转到职位选择页面
const goToJobSelection = () => {
  uni.navigateTo({
    url: '/pages/job-selection/index'
  });
};

// 返回面试界面页面
const goBack = () => {
  if (fromAiInterview.value && currentJobId.value) {
    uni.redirectTo({
      url: buildInterviewAiReturnUrl()
    });
    return;
  }
  if (currentJobId.value) {
    uni.redirectTo({
      url: `/pages/interview-interface/index?jobId=${currentJobId.value}`,
      success: () => {
        console.log('Redirected to interview interface successfully');
      },
      fail: (err) => {
        console.error('Failed to redirect to interview interface:', err);
        uni.showToast({
          title: '跳转失败，请重试',
          icon: 'none'
        });
      }
    });
  } else {
    // 如果没有jobId，则返回上一页
    uni.navigateBack();
  }
};

// 监听分析完成后绘制图表 (改为通用描述)

// 处理文件上传
const handleUpload = () => {
  console.log('[Resume Analysis] handleUpload triggered.');
  if (!currentJobId.value) {
    console.warn('[Resume Analysis] handleUpload called without currentJobId.');
    uni.showToast({
      title: '请先选择职位',
      icon: 'none'
    });
    return;
  }

  uni.chooseFile({
    count: 1,
    type: 'file',
    extension: ['.pdf', '.doc', '.docx'],
    success: (res) => {
      console.log('[Resume Analysis] File chosen successfully.', res);
      const file = res.tempFiles[0];
      if (file.size > 10 * 1024 * 1024) {
        uni.showToast({
          title: '文件大小不能超过10MB',
          icon: 'none'
        });
        return;
      }
      startAnalysis(file);
    },
    fail: (err) => {
      console.error('[Resume Analysis] Choose file failed:', err);
      uni.showToast({
        title: '选择文件失败',
        icon: 'none'
      });
    }
  });
};

// 开始分析
const startAnalysis = async (file) => {
  console.log('[Resume Analysis] startAnalysis started.');
  if (!currentJobId.value) {
    uni.showToast({ title: '请先选择职位', icon: 'none' });
    isAnalyzing.value = false;
    return;
  }
  if (!file) {
    uni.showToast({ title: '请先上传简历', icon: 'none' });
    isAnalyzing.value = false;
    return;
  }
  isAnalyzing.value = true;
  analysisStatus.value = '正在分析中';
  isUploaded.value = true;
  currentStep.value = 0;
  progressPercentage.value = 0;
  // 启动进度模拟
  const stepDuration = 6000;
  const totalSteps = analysisSteps.length;
  const progressInterval = setInterval(() => {
    if (currentStep.value < totalSteps - 1) {
      currentStep.value++;
      currentTip.value = analysisTips[currentStep.value];
      progressPercentage.value = ((currentStep.value + 1) / totalSteps) * 100;
    } else {
      clearInterval(progressInterval);
    }
  }, stepDuration);
  try {
    // 1. 直接提取简历文本
    let resumeContent = '';
    try {
      const extractRes = await extractResumeContent(file);
      if (extractRes.code === 200 && extractRes.data) {
        resumeContent = extractRes.data;
      } else {
        throw new Error(extractRes.message || '简历内容提取失败');
      }
    } catch (e) {
      throw new Error('简历内容提取失败');
    }
    // 2. 拼接岗位描述（仅传业务内容，格式约束由后端 skill 统一维护）
    let jobDesc = '';
    if (jobInfo.value) {
      jobDesc = `岗位名称：${jobInfo.value.name}\n岗位描述：${jobInfo.value.description}`;
    }
    // 3. 调用新版讯飞接口
    const analysisResponse = await getResumeAnalysisByXunfei(jobDesc, resumeContent);
    // 4. 解析结构化结果（兼容字符串或对象）
    const analysisResult = parseResumeAnalysisResult(analysisResponse.data);
    // 严格赋值
    score.value = analysisResult.score || 0;
    feedback.value = {
      advantages: analysisResult.feedback?.advantages || '',
      disadvantages: analysisResult.feedback?.disadvantages || '',
      improvementSuggestions: analysisResult.feedback?.improvement_suggestions || ''
    };
    const cs1 = analysisResult.competencies_score || {};
    competenciesScore.value = {
      professionalSkills: cs1.professional_skills ?? 0,
      projectExecution: cs1.project_execution ?? 0,
      innovation: cs1.innovation ?? 0,
      communication: cs1.communication ?? 0,
      adaptability: cs1.adaptability ?? 0
    };
    
    isAnalyzing.value = false;
    isAnalysisComplete.value = true;
    analysisStatus.value = '分析已完成';
    clearInterval(progressInterval);
    currentStep.value = totalSteps - 1;
    progressPercentage.value = 100;
    currentTip.value = '分析完成！';
    lastResumeContent.value = resumeContent;
  } catch (err) {
    clearInterval(progressInterval);
    uni.showToast({
      title: err.message || '分析失败，请重试',
      icon: 'none',
      duration: 3000
    });
    isAnalyzing.value = false;
  }
};

// 完成评测 (改为继续面试逻辑)
const completeAnalysis = async () => {
  try {
    // 保留本地状态更新
    analysisStatus.value = '已完成';
    uni.setStorageSync('resumeAnalysis', {
      completed: true,
      isAnalysisComplete: true,
      score: score.value,
      feedback: feedback.value,
      jobId: currentJobId.value
    });

    // 发送完成事件（如有父页面监听）
    const app = getApp && getApp();
    if (app && app.globalData && app.globalData.eventChannel) {
      const eventData = {
        module: 'resume',
        score: score.value,
        status: '已完成'
      };
      app.globalData.eventChannel.emit('moduleComplete', eventData);
    }

    uni.showToast({
      title: '简历评测完成',
      icon: 'success',
      duration: 1500
    });

    // 保存历史（单次）
    try { await saveResumeHistoryOnce() } catch (e) { console.warn('保存历史失败:', e) }

    setTimeout(() => {
      if (fromAiInterview.value) {
        try {
          localStorage.setItem('resumeJustCompleted', 'true');
          localStorage.setItem('resumeCompletedTimestamp', Date.now().toString());
          localStorage.setItem('resumeCompletedJobId', String(currentJobId.value || ''));
          localStorage.setItem('lastResumeScore', String(score.value || 0));
          localStorage.setItem('lastResumeScoreJobId', String(currentJobId.value || ''));
        } catch (e) {
          console.warn('写入 AI 返回标记失败:', e);
        }
        const timestamp = Date.now();
        uni.redirectTo({
          url: buildInterviewAiReturnUrl({
            completedType: 'resume',
            mode: 'SPECIAL',
            score: score.value,
            timestamp
          })
        });
        return;
      }
      uni.redirectTo({
        url: `/pages/interview-interface/index?jobId=${currentJobId.value}`,
        success: () => {
          console.log('[Resume Analysis] Redirected to interview interface successfully.');
        },
        fail: (err) => {
          console.error('[Resume Analysis] Failed to redirect to interview interface:', err);
          uni.showToast({
            title: '跳转失败，请重试',
            icon: 'none'
          });
        }
      });
    }, 1500);

    // 同步 interviewState 的 resumeAnalysis 状态，保证主界面能正确感知
    updateInterviewState();
  } catch (err) {
    console.error('[Resume Analysis] Error completing analysis:', err);
    uni.showToast({
      title: '操作失败',
      icon: 'none'
    });
  }
};

// 重置状态 (保留，但在当前逻辑下只会在 onLoad 中 jobId 不匹配时调用)
const resetState = () => {
  console.log('[Resume Analysis] resetState started.');
  isUploaded.value = false;
  isAnalysisComplete.value = false;
  score.value = 0;
  feedback.value = {
    advantages: '',
    disadvantages: '',
    improvementSuggestions: ''
  };
  
  analysisStatus.value = '未开始';

  try {
    // 清除本地存储的状态，只清除与当前简历分析页相关的键
    uni.removeStorageSync('resumeAnalysis'); // 假设只存储一个简历分析结果
    console.log('[Resume Analysis] resumeAnalysis storage cleared.');
  } catch (e) {
    console.error('[Resume Analysis] Error clearing resumeAnalysis storage:', e);
  }
  console.log('[Resume Analysis] resetState finished.');
};

// 组件卸载时清理
onUnmounted(() => {
  console.log('[Resume Analysis] Component onUnmounted triggered.');
  if (pollingInterval.value) {
    clearInterval(pollingInterval.value);
  }
  
});

// Uni-app 页面生命周期钩子 - onLoad 是页面加载时触发
// 负责获取 jobId 并处理缺失情况
onLoad((options) => {
  console.log('[Resume Analysis] onLoad started with options:', options);
  fromAiInterview.value = options?.from === 'ai-interview';

  let jobId = options.jobId; // 优先从 options 中获取 jobId

  // 如果 options 中没有，尝试从本地存储获取
  if (!jobId) {
    jobId = uni.getStorageSync('currentJobId');
    console.log('[Resume Analysis] Job ID obtained from localStorage:', jobId);
  }

  if (jobId) {
    currentJobId.value = jobId; // 将获取到的 jobId 赋值给响应式变量
    console.log('[Resume Analysis] Final Job ID obtained in onLoad:', jobId);
    // 在 onLoad 中不调用 API，只确保 jobId 被正确设置
    // fetchJobInfo(jobId); // 移除这里的 API 调用
  } else {
    console.error('[Resume Analysis] Job ID is missing from options and localStorage.');
    // 在 onLoad 中处理 jobId 缺失的逻辑
    // 显示错误提示
    uni.showToast({
      title: '岗位信息丢失，请重新选择岗位',
      icon: 'none',
      duration: 2000
    });
    // 跳转回岗位选择页面
    // 使用 setTimeout 确保 toast 显示后跳转
    setTimeout(() => {
      // 使用 router.replace 避免在返回栈中留下当前页面
      // 需要导入 router
      // import { useRouter } from 'vue-router';
      // const router = useRouter();
      router.replace('/pages/job-selection/index');
    }, 2000);
    console.log('[Resume Analysis] onLoad finished with missing jobId.');
    return; // 没有 jobId，终止后续加载
  }
  console.log('[Resume Analysis] onLoad finished successfully with jobId.');
});

// 生命周期钩子 - onMounted 在组件挂载后触发
// 负责调用 API 获取数据和加载保存的状态
onMounted(async () => {
  console.log('[Resume Analysis] onMounted started.');
  // 确保 currentJobId 已经被 onLoad 赋值后再调用 API
  if (currentJobId.value) {
    console.log('[Resume Analysis] currentJobId available in onMounted:', currentJobId.value);
    // 不再尝试加载保存的状态
    // loadSavedState();

    // 总是获取岗位信息，因为页面加载后总是显示初始上传状态
    console.log('[Resume Analysis] Fetching job info on mounted.');
    await fetchJobInfo(currentJobId.value); // 在 onMounted 中调用 fetchJobInfo

    // 确保页面状态是初始未上传状态
    isUploaded.value = false;
    isAnalysisComplete.value = false;
    score.value = 0;
    feedback.value = { advantages: '', disadvantages: '', improvementSuggestions: '' };
    analysisStatus.value = '未开始';

  } else {
    // 如果 onLoad 中未能获取 jobId，且未跳转，理论上不会执行到这里
    console.error('[Resume Analysis] currentJobId is missing in onMounted. Should have been handled by onLoad.');
    // 可以在这里添加备用错误处理或日志
  }
  // 在 onMounted 结束时，isUploaded 始终为 false，显示上传区域
  console.log('[Resume Analysis] onMounted finished.');
  nextTick(() => {
    setTimeout(() => {
      floatClass.value = 'resume-float-enter resume-float-enter-active';
    }, 20);
  });
});

// 生命周期钩子 - onShow 在页面显示时触发
// 可以在页面从其他页面返回时更新数据或状态
onShow(() => {
  console.log('[Resume Analysis] onShow started.');
  // 如果需要，可以在这里重新加载状态或检查 jobId
  // 例如，从文件选择器返回后可能需要刷新状态
  // loadSavedState(); // 如果需要，取消注释这一行
  console.log('[Resume Analysis] onShow finished.');
});

// 生命周期钩子 - onHide 在页面隐藏时触发
onHide(() => {
  console.log('[Resume Analysis] onHide started.');
  // 如果分析完成保存当前状态 (不再检查 isCompleted)
  if (isAnalysisComplete.value) {
    uni.setStorageSync('resumeAnalysis', {
      completed: true, // 保存时总是标记为已完成（因为完成分析即算完成模块）
      isAnalysisComplete: true,
      score: score.value,
      feedback: feedback.value,
      jobId: currentJobId.value // 确保保存 jobId
    });
    console.log('[Resume Analysis] Intermediate state saved in onHide.');
  }
  console.log('[Resume Analysis] onHide finished.');
});

// 加载保存的状态
const loadSavedState = () => {
  console.log('[Resume Analysis] loadSavedState started.');
  try {
    // 确保 currentJobId 已经被设置
    if (!currentJobId.value) {
      console.warn('[Resume Analysis] loadSavedState called without currentJobId. Skipping state load.');
      // 如果没有 jobId，直接返回，不再加载任何旧状态
      return;
    }

    // 从本地存储加载简历分析状态
    const savedData = uni.getStorageSync('resumeAnalysis');
    console.log('[Resume Analysis] Loading resumeAnalysis state:', savedData);

    if (savedData) {
      // 检查保存的简历分析状态是否与当前职位匹配，不匹配则重置
      if (savedData.jobId !== currentJobId.value) {
        console.warn('[Resume Analysis] Saved state jobId mismatch (', savedData.jobId, ' !== ', currentJobId.value, '), resetting.');
        resetState(); // 调用 resetState 清除当前状态和存储
        return; // 重置后不再加载旧状态
      }

      // 加载匹配的保存状态
      console.log('[Resume Analysis] Loading matching resumeAnalysis state.');

      // 加载分析完成状态
      if (savedData.isAnalysisComplete) {
        isUploaded.value = true; // 已上传，显示分析区域
        isAnalysisComplete.value = true;
        score.value = savedData.score;
        feedback.value = {
          advantages: savedData.feedback?.advantages || '',
          disadvantages: savedData.feedback?.disadvantages || '',
          improvementSuggestions: savedData.feedback?.improvementSuggestions || ''
        };
        analysisStatus.value = '分析完成';
        console.log('[Resume Analysis] Loaded analysis complete state.');

        

      } else {
        // 如果加载的状态不是 isAnalysisComplete (例如只有部分数据或旧格式)，则重置
        console.log('[Resume Analysis] Saved data found but not analysis complete, resetting.');
        resetState();
      }

    } else {
      // 没有找到保存的状态，保持初始状态或重置
      console.log('[Resume Analysis] No saved state found.');
      // 此时页面应该已经是初始状态，无需额外重置
    }
    console.log('[Resume Analysis] loadSavedState finished successfully. isAnalysisComplete:', isAnalysisComplete.value);
  } catch (err) {
    console.error('[Resume Analysis] Error in loadSavedState:', err);
    // 发生错误时，只重置当前模块的状态，不影响全局interviewState
    resetState(); // 重置当前模块状态
    uni.showToast({
      title: '加载简历分析状态失败', // 修改提示文本
      icon: 'none'
    });
  }
};

// 修改简历按钮点击事件
const editResume = async () => {
  if (historyId.value) {
    // 有历史ID时，优先请求后端获取简历内容
    try {
      const res = await getResumeHistoryDetailById(historyId.value);
      if (res.code === 200 && res.data && res.data.resumeContent !== undefined) {
        editResumeContent.value = res.data.resumeContent || '';
      } else {
        editResumeContent.value = lastResumeContent.value || '';
      }
    } catch {
      editResumeContent.value = lastResumeContent.value || '';
    }
  } else {
    // 没有历史ID时用本地内容
    editResumeContent.value = lastResumeContent.value || '';
  }
  showEditModal.value = true;
};

const closeEditModal = () => {
  showEditModal.value = false;
};

const restoreResumeText = async () => {
  if (!historyId.value) return;
  await restoreResumeByHistoryId(historyId.value);
};

// 保存并再次分析（简历内容修改）
const saveResumeEdit = async () => {
  if (!historyId.value || isSaving.value) return;
  isSaving.value = true;
  isEditAnalyzing.value = true;
  uni.showLoading({ title: '正在分析中...', mask: true }); // 显示全局 loading
  try {
    let jobDesc = '';
    if (jobInfo.value) {
      jobDesc = `岗位名称：${jobInfo.value.name}\n岗位描述：${jobInfo.value.description}`;
    }
    const analysisResponse = await getResumeAnalysisByXunfei(jobDesc, editResumeContent.value);
    const analysisResult = parseResumeAnalysisResult(analysisResponse.data);
    score.value = analysisResult.score || 0;
    feedback.value = {
      advantages: analysisResult.feedback?.advantages || '',
      disadvantages: analysisResult.feedback?.disadvantages || '',
      improvementSuggestions: analysisResult.feedback?.improvement_suggestions || ''
    };
    const cs2 = analysisResult.competencies_score || {};
    competenciesScore.value = {
      professionalSkills: cs2.professional_skills ?? 0,
      projectExecution: cs2.project_execution ?? 0,
      innovation: cs2.innovation ?? 0,
      communication: cs2.communication ?? 0,
      adaptability: cs2.adaptability ?? 0
    };
    
    lastResumeContent.value = editResumeContent.value;
    isAnalyzing.value = false;
    isAnalysisComplete.value = true;
    analysisStatus.value = '分析完成';
    lastResumeContent.value = editResumeContent.value;
    showEditModal.value = false;
    uni.showToast({ title: '分析完成！', icon: 'success' });
    updateInterviewState();
  } catch (e) {
    uni.showToast({ title: e.message || '保存失败，请稍后重试', icon: 'none' });
  } finally {
    isSaving.value = false;
    isEditAnalyzing.value = false;
    uni.hideLoading(); // 关闭全局 loading
  }
};

// 保存历史（仅在继续面试时保存一次）
const saveResumeHistoryOnce = async () => {
  const jobId = Number(currentJobId.value || 0);
  const userId = Number((getUserSession() || {}).userId || 0);
  const payload = {
    id: historyId.value,
    jobId,
    userId,
    resumeContent: lastResumeContent.value || '',
    score: score.value,
    feedback: {
      advantages: feedback.value.advantages,
      disadvantages: feedback.value.disadvantages,
      improvementSuggestions: feedback.value.improvementSuggestions
    },
    competenciesScore: {
      professionalSkills: competenciesScore.value?.professionalSkills ?? 0,
      projectExecution: competenciesScore.value?.projectExecution ?? 0,
      innovation: competenciesScore.value?.innovation ?? 0,
      communication: competenciesScore.value?.communication ?? 0,
      adaptability: competenciesScore.value?.adaptability ?? 0
    }
  };
  const historyIdStr = await saveResumeHistory(payload);
  if (historyIdStr) { historyId.value = historyIdStr }
  updateInterviewState();
};

// 恢复简历内容（通过历史ID）
const restoreResumeByHistoryId = async (resumeHistoryId) => {
  const res = await restoreResumeContentByHistoryId(resumeHistoryId);
  if (res.code === 200 && res.data && res.data.content) {
    editResumeContent.value = res.data.content;
    lastResumeContent.value = res.data.content;
    uni.showToast({ title: '已恢复历史简历内容', icon: 'success' });
  } else {
    uni.showToast({ title: res.message || '恢复失败', icon: 'none' });
  }
};

// 查询历史（可用于历史列表等）
const fetchResumeHistoryList = async () => {
  const u = getUserSession() || {};
  const userId = Number(u.userId || 0);
  const res = await getResumeHistoryList(userId, 'SPECIAL');
  if (res.code === 200 && Array.isArray(res.data)) {
    // 这里可以赋值到历史列表变量
    // resumeHistoryList.value = res.data;
  }
};

// 同步 interviewState 的 resumeAnalysis 状态，保证主界面能正确感知
const updateInterviewState = () => {
  let state = {};
  try {
    const saved = uni.getStorageSync('interviewState');
    state = saved ? JSON.parse(saved) : {};
  } catch { state = {}; }
  state.resumeAnalysis = {
    completed: true,
    score: score.value
  };
  uni.setStorageSync('interviewState', JSON.stringify(state));
};

// 导出页面生命周期钩子
defineExpose({
  onLoad,
  onShow,
  onHide
});
</script>

<style scoped lang="scss">
@use 'sass:math';
@use 'sass:string';

.resume-analyze-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a1929 0%, #1a1f35 100%);
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 60px;
  overflow: hidden;
}
.resume-analyze-bg.eye-care {
  background: linear-gradient(135deg, #1c2a1f 0%, #2a3c2e 100%);
}

/* 护眼模式下的卡片颜色调整 */
.eye-care .resume-analyze-card {
  background: rgba(127, 176, 105, 0.15);
  border: 1px solid rgba(127, 176, 105, 0.25);
  box-shadow: 0 8px 32px rgba(127, 176, 105, 0.2), 0 1.5px 6px rgba(0,0,0,0.15);
}

.eye-care .job-info-card,
.eye-care .analysis-status-card,
.eye-care .result-item,
.eye-care .upload-box {
  background: rgba(144, 198, 149, 0.12) !important;
  border: 1px solid rgba(127, 176, 105, 0.25);
  box-shadow: 0 2px 8px rgba(127, 176, 105, 0.15);
}

.eye-care .upload-box:hover {
  background-color: rgba(127, 176, 105, 0.15) !important;
}

.eye-care .analysis-status-text,
.eye-care .score-value,
.eye-care .analysis-result h3,
.eye-care .progress-tip {
  color: #7fb069 !important;
}

.eye-care .progress-step.active .step-icon,
.eye-care .progress-step.completed .step-icon {
  background: #7fb069 !important;
  border-color: #7fb069 !important;
  color: #fff !important;
}

.eye-care .progress-step.active .step-label,
.eye-care .progress-step.completed .step-label {
  color: #7fb069 !important;
}

/* 护眼模式下所有步骤图标的颜色调整 */
.eye-care .progress-step .step-icon {
  color: #7fb069 !important;
  background: rgba(127, 176, 105, 0.1) !important;
  border-color: rgba(127, 176, 105, 0.3) !important;
}

.eye-care .progress-step .step-label {
  color: #7fb069 !important;
}

/* 护眼模式下未激活步骤的样式 */
.eye-care .progress-step:not(.active):not(.completed) .step-icon {
  background: rgba(127, 176, 105, 0.05) !important;
  border-color: rgba(127, 176, 105, 0.2) !important;
  color: #7fb069 !important;
}

.eye-care .progress-step:not(.active):not(.completed) .step-label {
  color: rgba(127, 176, 105, 0.7) !important;
}

.eye-care .progress-fill {
  background: linear-gradient(90deg, #7fb069, #90c695);
}

.eye-care .control-button.primary {
  background: linear-gradient(90deg, #7fb069 0%, #90c695 100%);
}

.eye-care .control-button.secondary {
  color: #7fb069;
  border-color: #7fb069;
}

.eye-care .control-button.secondary:hover {
  background: rgba(127, 176, 105, 0.15);
  color: #7fb069;
  border-color: #90c695;
}

/* 护眼模式下的字体颜色调整 */
.eye-care .resume-analyze-card h1 {
  color: #fff !important;
}

.eye-care .job-info-card h3,
.eye-care .job-details .job-title,
.eye-care .job-details .job-description-label,
.eye-care .job-details .job-description-text {
  color: #fff !important;
}

.eye-care .analysis-status-label {
  color: #fff !important;
}

.eye-care .result-item h4 {
  color: #7fb069 !important;
}

.eye-care .result-item p {
  color: rgba(255, 255, 255, 0.9) !important;
}

.eye-care .upload-icon {
  color: #7fb069 !important;
}

.eye-care .upload-tip {
  color: rgba(255, 255, 255, 0.8) !important;
}

/* 护眼模式下的星空效果 */
.eye-care .stars1 {
  filter: drop-shadow(0 0 6px #a8c69f88);
  opacity: 0.35;
}

.eye-care .stars2 {
  filter: drop-shadow(0 0 8px #7fb06988);
  opacity: 0.22;
}

.eye-care .stars3 {
  filter: drop-shadow(0 0 10px #a8c69f88);
  opacity: 0.15;
}

/* 星空box-shadow星点实现 */
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

.resume-analyze-card {
  background: rgba(20,24,40,0.92);
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(74,111,255,0.18), 0 1.5px 6px rgba(0,0,0,0.18);
  max-width: 900px;
  width: 96%;
  margin: 40px auto;
  padding: 48px 40px 40px 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  /* animation: fade-in 0.7s; 移除原有动画 */
  position: relative;
  z-index: 1;
}
.job-info-card,
.analysis-status-card,
.result-item,
.upload-box {
  background: rgba(30,34,54,0.85) !important;
  color: #fff !important;
  border: 1px solid rgba(124,77,255,0.18);
  box-shadow: 0 2px 8px rgba(74,111,255,0.10);
}
.upload-box:hover {
  background-color: rgba(124, 77, 255, 0.08) !important;
}
.upload-tip { color: #b3e5fc !important; }
.analysis-status-label,
.analysis-status-text,
.analysis-status-row,
.result-item h4,
.result-item p,
.job-info-card h3,
.job-details .job-title,
.job-details .job-description-label,
.job-details .job-description-text {
  color: #fff !important;
  text-shadow: 0 1px 4px rgba(0,0,0,0.18);
}
.button-container .control-button.primary {
  background: linear-gradient(90deg, #7c4dff 0%, #00e5ff 100%);
  color: #fff;
}
.button-container .control-button.secondary {
  background: rgba(255,255,255,0.12);
  color: #7c4dff;
  border: 2px solid #7c4dff;
}
.button-container .control-button.secondary:hover {
  background: rgba(124,77,255,0.18);
  color: #fff;
  border-color: #00e5ff;
}
.analysis-input-section, .analysis-status-area, .analysis-result, .feedback-section {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.button-container {
  margin-top: 40px;
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 28px;
}
.control-button.primary {
  width: 220px;
  font-size: 1.15rem;
  padding: 16px 0;
  border-radius: 10px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(74,111,255,0.08);
  background: linear-gradient(90deg, #4A6FFF 0%, #6B8CFF 100%);
  color: #fff;
  border: none;
  margin-top: 0;
  transition: background 0.25s, box-shadow 0.2s, transform 0.1s;
  text-align: center;
}
.control-button.secondary {
  width: 220px;
  font-size: 1.15rem;
  padding: 16px 0;
  border-radius: 10px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(74,111,255,0.08);
  background: #fff;
  color: #4A6FFF;
  border: 2px solid #4A6FFF;
  margin-top: 0;
  margin-right: 0;
  transition: background 0.25s, color 0.2s, border 0.2s, box-shadow 0.2s, transform 0.1s;
  text-align: center;
}
.control-button.secondary:hover {
  background: #f4f7ff;
  color: #3A5FEF;
  border-color: #3A5FEF;
  box-shadow: 0 8px 24px rgba(74,111,255,0.12);
  transform: translateY(-2px) scale(1.03);
}
.control-button.secondary:active {
  background: #e6edfa;
  color: #3A5FEF;
  border-color: #3A5FEF;
  transform: scale(0.97);
}
.control-button.primary:hover:not(:disabled) {
  background: linear-gradient(90deg, #5C7CFF 0%, #7CA6FF 100%);
  box-shadow: 0 8px 24px rgba(74,111,255,0.16);
  transform: translateY(-2px) scale(1.03);
}
.control-button.primary:active {
  background: linear-gradient(90deg, #3A5FEF 0%, #4A6FFF 100%);
  transform: scale(0.97);
}
.job-info-card {
  width: 100%; /* 占满容器宽度 */
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.job-info-card h3 {
  color: #333;
  margin: 0 0 15px 0; /* 增加标题和内容的间距 */
  font-size: 18px; /* 调整标题字体大小 */
}

.job-details .job-title {
  font-weight: bold;
  font-size: 16px; /* 调整职位名称字体大小 */
  color: #4A6FFF; /* 职位名称使用品牌色 */
  margin-bottom: 10px; /* 调整职位名称和描述标签的间距 */
}

.job-details .job-description-label {
  font-weight: 600; /* 岗位描述/职责标签加粗 */
  color: #555; /* 标签颜色 */
  margin-bottom: 5px; /* 调整标签和描述文本的间距 */
}

.job-details .job-description-text {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}

.upload-section {
  width: 100%; /* 占满容器宽度 */
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.upload-box {
  width: 100%;
  max-width: 500px; /* 增加最大宽度 */
  height: 200px;
  border: 2px dashed #4A6FFF;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-box:hover {
  background-color: rgba(74, 111, 255, 0.05);
}

.upload-icon { margin-bottom: 10px; }
.upload-svg { color: #eaf2ff; filter: drop-shadow(0 0 8px rgba(124,77,255,0.35)); }

.upload-tip {
  color: #666;
  font-size: 0.9em;
  margin-top: 5px;
}

/* 添加一个容器来包裹分析状态和结果，以便在布局中控制 */
.analysis-status-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  margin-top: 20px;
  max-width: 800px; /* Increased max-width for a wider container */
  margin-left: auto;
  margin-right: auto;
}

.analysis-status-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(74, 111, 255, 0.08);
  padding: 36px 20px 28px 20px; /* Add horizontal padding */
  margin-bottom: 30px;
  min-height: 90px;
}

.analysis-status-row {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.35rem;
  font-weight: 600;
  color: #222;
  letter-spacing: 1px;
}

.analysis-status-label {
  font-size: 1.2em;
  font-weight: bold;
  color: #333;
}

.analysis-status-text {
  font-size: 1.4em;
  font-weight: bold;
  color: #4A6FFF;
}

.score-display {
  margin-top: 18px;
  color: #4A6FFF;
  font-weight: 700;
  text-align: center;
}

.score-label {
  color: #666;
}

.score-value {
  color: #4A6FFF;
  font-weight: 900;
  font-size: 2.4rem;
  margin-left: 12px;
}

.analysis-result {
  width: 100%;
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-top: 2px solid #4A6FFF;
  padding-top: 30px;
}

/* 调整：让雷达图在上方，反馈内容在下方 */
.result-flex-row {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  gap: 40px;
}

 

.feedback-section {
  width: 100%;
  max-width: 600px;
  margin-top: 0;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 18px;
}

.result-item {
  width: 100%;
  background-color: #fff;
  padding: 24px 28px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  line-height: 2.1;
  letter-spacing: 0.03em;
  font-size: 1.08em;
  color: #444;
  word-break: break-word;
  overflow-wrap: break-word;
  transition: box-shadow 0.2s;
}

.result-item h4 {
  color: #4A6FFF !important;
  font-size: 1.15em;
  font-weight: 700;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.result-item p {
  color: #fff !important;
  font-size: 0.98em;
  font-weight: 400;
  line-height: 1.7;
  margin: 0 0 2px 0;
  word-break: break-word;
  overflow-wrap: break-word;
  letter-spacing: 0.01em;
  padding: 0;
}

.suggestion-box {
  width: 100%;
  background: linear-gradient(180deg, rgba(30,34,54,0.85), rgba(30,34,54,0.72));
  border: 1px solid rgba(124,77,255,0.25);
  border-left: 4px solid #4A6FFF;
  border-radius: 12px;
  padding: 18px 18px 16px 18px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.22);
}
.suggestion-header { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.suggestion-icon { color: #4A6FFF; filter: drop-shadow(0 0 6px rgba(74,111,255,0.35)); }
.suggestion-title { color: #eaf2ff; font-weight: 700; letter-spacing: 1px; }
.suggestion-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 8px; }
.suggestion-item { color: #eaf2ff; background: rgba(124,77,255,0.12); border: 1px solid rgba(124,77,255,0.22); border-radius: 10px; padding: 10px 12px; line-height: 1.6; }

.eye-care .suggestion-box { background: rgba(127,176,105,0.12); border-color: rgba(127,176,105,0.28); border-left-color: #7fb069; box-shadow: 0 6px 18px rgba(0,0,0,0.18); }
.eye-care .suggestion-icon { color: #7fb069; filter: drop-shadow(0 0 6px rgba(127,176,105,0.35)); }
.eye-care .suggestion-title { color: #fffbea; }
.eye-care .suggestion-item { color: #153023; background: rgba(127,176,105,0.12); border-color: rgba(127,176,105,0.28); }

.advantage-box, .disadvantage-box { width: 100%; background: linear-gradient(180deg, rgba(30,34,54,0.85), rgba(30,34,54,0.72)); border: 1px solid rgba(124,77,255,0.25); border-radius: 12px; padding: 18px 18px 16px 18px; box-shadow: 0 6px 18px rgba(0,0,0,0.22); }
.advantage-box { border-left: 4px solid #4A6FFF; }
.disadvantage-box { border-left: 4px solid #ff9100; }
.box-header { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.advantage-box .box-icon { color: #4A6FFF; filter: drop-shadow(0 0 6px rgba(74,111,255,0.35)); }
.disadvantage-box .box-icon { color: #ff9100; filter: drop-shadow(0 0 6px rgba(255,145,0,0.35)); }
.box-title { color: #eaf2ff; font-weight: 700; letter-spacing: 1px; }
.box-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 8px; }
.box-item { color: #eaf2ff; background: rgba(124,77,255,0.12); border: 1px solid rgba(124,77,255,0.22); border-radius: 10px; padding: 10px 12px; line-height: 1.6; }
.disadvantage-box .box-item { background: rgba(255,145,0,0.10); border-color: rgba(255,145,0,0.25); }
.eye-care .advantage-box, .eye-care .disadvantage-box { background: rgba(127,176,105,0.12); border-color: rgba(127,176,105,0.28); box-shadow: 0 6px 18px rgba(0,0,0,0.18); }
.eye-care .advantage-box { border-left-color: #7fb069; }
.eye-care .disadvantage-box { border-left-color: #7fb069; }
.eye-care .box-icon { color: #7fb069; filter: drop-shadow(0 0 6px rgba(127,176,105,0.35)); }
.eye-care .box-title { color: #fffbea; }
.eye-care .box-item { color: #153023; background: rgba(127,176,105,0.12); border-color: rgba(127,176,105,0.28); }

@media (max-width: 1100px) {
  .result-flex-row {
    gap: 24px;
  }
  .feedback-section {
    max-width: 100%;
  }
  .result-item {
    padding: 20px;
    font-size: 1em;
  }
  .result-item p {
    font-size: 0.95em;
  }
}

.job-selection-alert {
  background-color: #fff3cd;
  border: 1px solid #ffeeba;
  border-radius: 8px;
  padding: 20px;
  margin: 20px 0;
  text-align: center;
}

.alert-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.select-job-button {
  background: linear-gradient(45deg, #4A6FFF 0%, #6B8CFF 100%);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.select-job-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(74, 111, 255, 0.2);
}

.dot-loading {
  display: inline-flex;
  align-items: center;
  margin-left: 6px;
  vertical-align: middle;
  height: 18px;
}
.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  margin: 0 2px;
  background: #4A6FFF;
  border-radius: 50%;
  animation: dot-bounce 1.2s infinite both;
}
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes dot-bounce {
  0%, 80%, 100% { transform: scale(0.7); opacity: 0.7; }
  40% { transform: scale(1.2); opacity: 1; }
}

 

.analysis-result h3 {
  color: #4A6FFF;
  margin: 0 0 20px 0; /* Increase bottom margin */
}

.analysis-progress {
  margin-top: 30px;
  width: 100%;
  padding: 0 20px;
}

.progress-steps {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  position: relative;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 1;
}

.step-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #23263a !important;
  color: #bfc6e6 !important;
  border: 2px solid #bfc6e6 !important;
  opacity: 1 !important;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.progress-step.active .step-icon,
.progress-step.completed .step-icon {
  background: #4A6FFF !important;
  color: #fff !important;
  border: 2px solid #4A6FFF !important;
}

.progress-step .step-label {
  color: #bfc6e6 !important;
}

.progress-step.active .step-label,
.progress-step.completed .step-label {
  color: #4A6FFF !important;
}

.progress-bar {
  height: 4px;
  background: #e0e0e0;
  border-radius: 2px;
  margin: 20px 0;
  position: relative;
  overflow: hidden;
}

.progress-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: linear-gradient(90deg, #4A6FFF, #6B8CFF);
  border-radius: 2px;
  transition: width 0.3s ease;
}

.progress-tip {
  text-align: center;
  color: #4A6FFF;
  font-size: 15px;
  font-weight: 500;
  margin-top: 15px;
  min-height: 24px;
}

/* 添加动画效果 */
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.progress-step.active .step-icon {
  animation: pulse 1.5s infinite;
}

/* 优化加载动画 */
.dot-loading {
  display: inline-flex;
  align-items: center;
  margin-left: 10px;
}

.dot {
  width: 8px;
  height: 8px;
  margin: 0 4px;
  background: #4A6FFF;
  border-radius: 50%;
  animation: dot-bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes dot-bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .progress-steps {
    flex-wrap: wrap;
    justify-content: center;
    gap: 20px;
  }
  
  .progress-step {
    flex: 0 0 33.33%;
  }
  
  .step-label {
    font-size: 12px;
  }
}

@media (max-width: 1000px) {
  .resume-analyze-card {
    max-width: 100vw;
    padding: 24px 4vw 24px 4vw;
  }
}

.resume-edit-textarea {
  width: 100%;
  min-height: 450px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  font-size: 1.08em;
  color: #333;
  resize: vertical;
  background: #f8faff;
  margin-bottom: 10px;
  box-sizing: border-box;
  overflow-wrap: break-word;
  word-break: break-word;
  overflow: hidden !important; /* 强制隐藏滚动条 */
  scrollbar-width: none !important; /* Firefox */
  -ms-overflow-style: none !important; /* IE 10+ */
}

.resume-edit-textarea::-webkit-scrollbar {
  width: 0 !important;
  height: 0 !important;
  display: none !important;
  background: transparent !important;
}

/* 字体高对比度优化，确保所有内容在深色背景下清晰可读 */
.resume-analyze-card h1,
.resume-analyze-card h2,
.resume-analyze-card h3,
.resume-analyze-card h4,
.analysis-status-label,
.analysis-status-text,
.score-label,
.score-value,
.result-item h4,
.result-item p,
.job-info-card h3,
.job-details .job-title,
.job-details .job-description-label,
.job-details .job-description-text,
.analysis-status-row,
.analysis-result h3,
.button-container .control-button,
.progress-tip,
.progress-step .step-label,
.progress-step .step-icon,
.project-info,
.team-name {
  color: #fff !important;
  text-shadow: 0 1px 4px rgba(0,0,0,0.18);
}
.score-value,
.progress-tip,
.analysis-result h3 {
  color: #4A6FFF !important;
}
.progress-step .step-icon.active,
.progress-step .step-icon.completed {
  color: #fff !important;
  background: #4A6FFF !important;
  border-color: #4A6FFF !important;
}
.progress-step .step-label.active,
.progress-step .step-label.completed {
  color: #4A6FFF !important;
}
.edit-analyzing-tip {
  text-align: center;
  color: #4A6FFF;
  font-size: 1.1em;
  margin-top: 18px;
  font-weight: 600;
}



.resume-float-enter {
  opacity: 0;
  transform: translateY(40px);
  transition: opacity 0.5s cubic-bezier(0.4,0,0.2,1), transform 0.5s cubic-bezier(0.4,0,0.2,1);
}
.resume-float-enter.resume-float-enter-active {
  opacity: 1;
  transform: translateY(0);
}

/* 返回按钮样式 */
.back-button {
  position: fixed;
  top: 20px;
  left: 20px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: rgba(124, 77, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(124, 77, 255, 0.2);
  transition: all 0.3s ease;
  cursor: pointer;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);

  &:hover {
    transform: scale(1.05);
    background: rgba(124, 77, 255, 0.2);
    border-color: rgba(124, 77, 255, 0.3);
    box-shadow: 
      0 0 20px rgba(124, 77, 255, 0.2),
      0 0 40px rgba(0, 229, 255, 0.1);
  }

  svg {
    color: #7c4dff;
    filter: drop-shadow(0 0 5px rgba(124, 77, 255, 0.5));
  }
}

/* 护眼模式下的返回按钮样式 */
.eye-care .back-button {
  background: rgba(127, 176, 105, 0.1);
  border-color: rgba(127, 176, 105, 0.2);

  &:hover {
    background: rgba(127, 176, 105, 0.2);
    border-color: rgba(127, 176, 105, 0.3);
    box-shadow: 
      0 0 20px rgba(127, 176, 105, 0.2),
      0 0 40px rgba(144, 198, 149, 0.1);
  }

  svg {
    color: #7fb069;
    filter: drop-shadow(0 0 5px rgba(127, 176, 105, 0.5));
  }
}
</style>

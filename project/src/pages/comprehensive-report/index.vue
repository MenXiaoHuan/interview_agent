<template>
  <div class="comprehensive-report-container" :class="{ 'eye-care': isEyeCareMode }">
    <!-- 科幻背景效果 -->
    <div class="cyber-background">
      <div class="grid-lines"></div>
      <div class="scanning-lines">
        <div class="horizontal-scan"></div>
        <div class="vertical-scan"></div>
        <div class="diagonal-scan"></div>
        <div class="radar-scan"></div>
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
      <div class="data-streams">
        <div v-for="n in 8" :key="n" class="data-stream" 
             :style="{ '--stream-delay': `${n * 0.5}s` }"></div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 全息投影标题 -->
      <div class="holographic-header">
        <div class="title-container">
          <h1 class="main-title">
            <span class="title-text">{{ reportTitle }}</span>
            <div class="title-glow"></div>
            <div class="title-scan-line"></div>
          </h1>
          <p class="subtitle">AI驱动的多维度职业能力评估报告</p>
        </div>
      </div>

      <!-- 报告内容卡片 -->
      <div class="report-hologram">
        <div class="hologram-frame">
          <!-- 整体总分部分 - 仅在overall报告中显示 -->
          <div class="overall-score-section" v-if="reportType === 'overall'">
            <div class="score-hologram">
              <div class="score-ring-container">
                <div class="outer-ring"></div>
                <div class="inner-ring"></div>
                <div class="score-display" :class="getScoreClass">
                  <div class="score-value">{{ displayScore }}</div>
                  <div class="score-label">{{ displayScoreLabel }}</div>
                  <div class="score-glow"></div>
                </div>
              </div>
            </div>
            <div class="overall-feedback">
              <h2 class="feedback-title">{{ overallFeedbackTitle }}</h2>
              <p class="feedback-text">{{ overallFeedbackText }}</p>
            </div>
          </div>

          <!-- 单项报告的分数显示 - 在单项报告中显示 -->
          <div class="single-module-score-section" v-else>
            <div class="score-hologram">
              <div class="score-ring-container">
                <div class="outer-ring"></div>
                <div class="inner-ring"></div>
                <div class="score-display" :class="getScoreClass">
                  <div class="score-value">{{ displayScore }}</div>
                  <div class="score-label">{{ displayScoreLabel }}</div>
                  <div class="score-glow"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 模块概览部分 - 仅在overall报告中显示 -->
          <div class="module-summary-section" v-if="reportType === 'overall'">
            <h3 class="section-heading">
              <span class="heading-text">模块表现概览</span>
              <div class="heading-line"></div>
            </h3>
            <div class="module-grid">
              <div class="module-hologram-item" :class="getModuleStatusClass(resumeCompleted)">
                <div class="module-frame">
                  <div class="module-icon">📄</div>
                  <h4 class="module-title">简历评测</h4>
                  <div class="module-score">{{ resumeScore }}</div>
                  <div class="module-weight">权重: 35%</div>
                  <div class="module-status">{{ resumeStatusText }}</div>
                  <div class="module-glow"></div>
                </div>
              </div>
              <div class="module-hologram-item" :class="getModuleStatusClass(questionsCompleted)">
                <div class="module-frame">
                  <div class="module-icon">📝</div>
                  <h4 class="module-title">试题作答</h4>
                  <div class="module-score">{{ questionsScore }}</div>
                  <div class="module-weight">权重: 30%</div>
                  <div class="module-status">{{ questionsStatusText }}</div>
                  <div class="module-glow"></div>
                </div>
              </div>
              <div class="module-hologram-item" :class="getModuleStatusClass(audioCompleted)">
                <div class="module-frame">
                  <div class="module-icon">🎭</div>
                  <h4 class="module-title">场景评测</h4>
                  <div class="module-score">{{ audioScore }}</div>
                  <div class="module-weight">权重: 35%</div>
                  <div class="module-status">{{ audioStatusText }}</div>
                  <div class="module-glow"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 详细分析部分 -->
          <div class="detailed-analysis-section">
            <h3 class="section-heading">
              <span class="heading-text">详细分析与改进建议</span>
              <div class="heading-line"></div>
            </h3>
            <template v-if="aiAnalysisLoading">
              <div class="analysis-loading">
                <div class="loading-container">
                  <div class="loading-hologram">
                    <div class="loading-text">
                      <div class="loading-title">AI正在分析您的测评数据...</div>
                      <div class="loading-subtitle">正在生成个性化发展建议</div>
                      <div class="loading-tips">
                        <div class="tip-item" v-for="(tip, index) in loadingTips" :key="index" :class="{ active: currentTipIndex === index }">
                          {{ tip }}
                        </div>
                      </div>
                    </div>
                    <div class="loading-progress">
                      <div class="progress-bar">
                        <div class="progress-fill" :style="{ width: loadingProgress + '%' }"></div>
                      </div>
                      <div class="progress-text">{{ Math.round(loadingProgress) }}%</div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
            <template v-else-if="aiAnalysisResult">
              <div class="analysis-hologram">
                <div class="analysis-frame">
                  <div class="analysis-header">
                    <div class="header-icon">
                      <SmartIcon name="fas fa-lightbulb" />
                    </div>
                    <h4 class="analysis-title">综合发展建议</h4>
                  </div>
                  <div class="analysis-content">
                    <p class="analysis-text">{{ aiAnalysisResult.feedbackText }}</p>
                    <div class="strength-section" v-if="aiAnalysisResult.strengthAnalysis?.strengths?.length">
                      <h5 class="subsection-title">🌟 您的优势</h5>
                      <div class="suggestion-list">
                        <div class="suggestion-item" v-for="strength in aiAnalysisResult.strengthAnalysis.strengths" :key="strength">
                          <div class="suggestion-icon">💡</div>
                          <span>{{ strength }}</span>
                        </div>
                      </div>
                    </div>
                    <div class="improvement-section" v-if="aiAnalysisResult.improvementAnalysis?.improvements?.length">
                      <h5 class="subsection-title">🎯 改进建议</h5>
                      <div class="suggestion-list">
                        <div class="suggestion-item" v-for="improvement in aiAnalysisResult.improvementAnalysis.improvements" :key="improvement">
                          <div class="suggestion-icon">💡</div>
                          <span>{{ improvement }}</span>
                        </div>
                      </div>
                    </div>
                    <div class="learning-path-section" v-if="aiAnalysisResult.learningPathRecommendation">
                      <h5 class="subsection-title">📚 学习路线推荐</h5>
                      <p class="learning-path-text">{{ aiAnalysisResult.learningPathRecommendation }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </div>

          <!-- 报告底部 -->
          <div class="report-footer">
            <button class="complete-button" @click="saveReportAndJump" :disabled="aiAnalysisLoading || saveLoading">
              <div class="button-content">
                <span class="button-text">{{ aiAnalysisLoading ? '正在生成报告...' : '保存报告' }}</span>
                <div class="button-glow"></div>
              </div>
            </button>
            <p class="footer-text">&copy; 2026 梦之队 All rights reserved.</p>
          </div>
        </div>
        <div class="hologram-glow"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { onLoad } from '@dcloudio/uni-app';
import { getComprehensiveReportAnalysis, saveComprehensiveReportHistory } from '@/utils/api';
import { useUserStore } from '@/stores/user';
import { useJobStore } from '@/stores/job';
import { storeToRefs } from 'pinia';
import SmartIcon from '@/components/SmartIcon.vue';
import {
  createDefaultComprehensiveState,
  clearActiveComprehensiveAssessment,
  ensureActiveComprehensiveAssessmentSession,
  normalizeComprehensiveState,
  updateComprehensiveAssessmentState
} from '@/utils/comprehensive-assessment';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const jobStore = useJobStore();
const { isEyeCareMode } = storeToRefs(userStore);

// 状态变量
const jobId = ref(null);
const reportType = ref('overall'); // 'overall', 'resume', 'questions', 'audio'
const reportId = ref(null); // Corresponding ID for individual reports
const assessmentId = ref('');

const resumeScore = ref(0);
const questionsScore = ref(0);
const audioScore = ref(0);

const resumeCompleted = ref(false);
const questionsCompleted = ref(false);
const audioCompleted = ref(false);

// AI分析相关状态
const aiAnalysisLoading = ref(false);
const aiAnalysisResult = ref(null);

// 加载相关状态
const loadingProgress = ref(0);
const currentTipIndex = ref(0);
const loadingTips = ref([
  '正在分析您的技能匹配度...',
  '正在评估您的学习潜力...',
  '正在制定个性化发展路线...',
  '正在生成具体可执行的建议...',
  '正在优化学习计划...',
  '正在分析您的职业发展方向...',
  '正在制定详细的学习时间表...',
  '正在评估您的技能提升空间...',
  '正在生成针对性的改进建议...',
  '正在完善您的职业发展规划...'
]);

const totalScore = computed(() => {
  const resumeWeight = 0.35;    // 35%
  const questionsWeight = 0.30; // 30%
  const audioWeight = 0.35;     // 35%

  let calculatedScore = 0;
  if (resumeCompleted.value) {
    calculatedScore += resumeScore.value * resumeWeight;
  }
  if (questionsCompleted.value) {
    calculatedScore += questionsScore.value * questionsWeight;
  }
  if (audioCompleted.value) {
    calculatedScore += audioScore.value * audioWeight;
  }
  // Round to nearest integer
  return Math.round(calculatedScore);
});

// New computed properties for dynamic score display
const displayScore = computed(() => {
  switch (reportType.value) {
    case 'resume': return resumeScore.value;
    case 'questions': return questionsScore.value;
    case 'audio': return audioScore.value;
    default: return totalScore.value; // 'overall' or fallback
  }
});

const displayScoreLabel = computed(() => {
  switch (reportType.value) {
    case 'resume': return '简历得分';
    case 'questions': return '试题得分';
    case 'audio': return '场景得分';
    default: return '总分';
  }
});

const reportTitle = computed(() => {
  switch (reportType.value) {
    case 'resume': return '简历评测报告';
    case 'questions': return '试题作答报告';
    case 'audio': return '场景评测报告';
    default: return '综合能力测评报告';
  }
});

// Computed properties for UI display
const getScoreClass = computed(() => {
  if (displayScore.value >= 90) return 'score-excellent';
  if (displayScore.value >= 75) return 'score-good';
  if (displayScore.value >= 60) return 'score-fair';
  return 'score-poor';
});

const overallFeedbackTitle = computed(() => {
  if (displayScore.value >= 90) return '表现卓越，潜力无限！';
  if (displayScore.value >= 75) return '表现良好，持续进步！';
  if (displayScore.value >= 60) return '基础扎实，尚需磨练。';
  return '需要加强，继续努力！';
});

const overallFeedbackText = computed(() => {
  if (displayScore.value >= 90) return '您在各项能力测评中均展现出非凡的实力和巨大的潜力，完全具备胜任目标岗位的条件，未来可期！';
  if (displayScore.value >= 75) return '您在核心能力方面表现出色，展现了较强的综合素质。通过针对性训练，您的表现将更加令人瞩目。';
  if (displayScore.value >= 60) return '您已具备一定的基础能力，但在某些方面仍有提升空间。建议根据报告建议，进行专项学习和练习。';
  return '您的表现仍需大幅提升。请认真研读报告中的各项建议，并制定详细的学习计划，以弥补不足。';
});

const getModuleStatusClass = (isCompleted) => {
  return isCompleted ? 'status-completed' : 'status-pending';
};

const resumeStatusText = computed(() => resumeCompleted.value ? '已完成' : '未完成');
const questionsStatusText = computed(() => questionsCompleted.value ? '已完成' : '未完成');
const audioStatusText = computed(() => audioCompleted.value ? '已完成' : '未完成');

// Methods
const buildInterviewAiCompletionUrl = () => {
  const query = [];
  const safeChatId = String(route.query.chatId || '').trim();
  const safeJobId = String(jobId.value || '').trim();
  if (safeChatId) {
    query.push(`chatId=${encodeURIComponent(safeChatId)}`);
  }
  if (safeJobId) {
    query.push(`jobId=${encodeURIComponent(safeJobId)}`);
  }
  query.push('autoOpen=1');
  query.push('completedType=report');
  query.push('mode=COMPREHENSIVE');
  query.push(`score=${encodeURIComponent(String(totalScore.value || 0))}`);
  query.push(`timestamp=${encodeURIComponent(String(Date.now()))}`);
  return `/pages/interview-ai/index?${query.join('&')}`;
};

// 新增保存loading状态
const saveLoading = ref(false);

// 新增保存并跳转函数
const saveReportAndJump = async () => {
  saveLoading.value = true;
  try {
    // 直接保存AI分析结果到历史（假设aiAnalysisResult已存在）
    await saveComprehensiveReport(aiAnalysisResult.value);
    updateInterviewState();
    updateComprehensiveAssessmentState(
      jobId.value,
      assessmentId.value,
      createDefaultComprehensiveState(),
      { createIfMissing: false }
    );
    clearActiveComprehensiveAssessment(jobId.value);
    assessmentId.value = '';

    uni.showToast({
      title: '报告已保存',
      icon: 'success',
      duration: 1500
    });
    setTimeout(() => {
      aiAnalysisResult.value = null;
      router.replace(buildInterviewAiCompletionUrl());
    }, 1000);
  } catch (error) {
    uni.showToast({
      title: '保存失败，请重试',
      icon: 'none',
      duration: 2000
    });
  } finally {
    saveLoading.value = false;
  }
};

// 执行综合分析
const performComprehensiveAnalysis = async (jobInfo) => {
  try {
    const response = await getComprehensiveReportAnalysis({
      jobName: jobInfo?.name || '未知职位',
      jobDescription: jobInfo?.description || '无详细描述',
      resumeScore: resumeCompleted.value ? resumeScore.value : 0,
      questionScore: questionsCompleted.value ? questionsScore.value : 0,
      scenarioScore: audioCompleted.value ? audioScore.value : 0,
      totalScore: totalScore.value
    });
    
    if (response.code === 200) {
      let result;
      try {
        if (!response.data || typeof response.data !== 'object') {
          throw new Error('AI返回结果不是对象');
        }
        result = response.data;
        
        // 验证必要字段
        const requiredFields = ['feedbackText', 'strengthAnalysis', 'improvementAnalysis', 'learningPathRecommendation'];
        const missingFields = requiredFields.filter(field => !result[field]);
        
        if (missingFields.length > 0) {
          throw new Error(`缺少必要字段: ${missingFields.join(', ')}`);
        }
        
        // 验证嵌套字段
        if (!Array.isArray(result.strengthAnalysis?.strengths) || result.strengthAnalysis.strengths.length === 0) {
          throw new Error('strengthAnalysis.strengths 必须是非空数组');
        }
        
        if (!Array.isArray(result.improvementAnalysis?.improvements) || result.improvementAnalysis.improvements.length === 0) {
          throw new Error('improvementAnalysis.improvements 必须是非空数组');
        }
        
        if (!result.learningPathRecommendation || result.learningPathRecommendation.trim() === '') {
          throw new Error('learningPathRecommendation 不能为空');
        }
        
        return result;
      } catch (parseError) {
        console.error('AI响应解析失败:', parseError);
        console.error('原始响应:', response.data);
        throw new Error(`AI响应格式错误: ${parseError.message}`);
      }
    } else {
      throw new Error(response.message || 'AI分析失败');
    }
  } catch (error) {
    console.error('AI分析失败:', error);
    throw error;
  }
};

// 保存综合评估报告
const saveComprehensiveReport = async (analysisResult) => {
  try {
    const userId = userStore.userId;
    if (!userId) {
      throw new Error('用户未登录');
    }
    // 构建新接口参数
    const params = {
      userId: parseInt(userId),
      jobId: parseInt(jobId.value),
      overallScore: totalScore.value,
      resumeScore: resumeScore.value,
      questionScore: questionsScore.value,
      scenarioScore: audioScore.value,
      strengthAnalysis: analysisResult.strengthAnalysis?.strengths?.join('\n') || '',
      improvementAnalysis: analysisResult.improvementAnalysis?.improvements?.join('\n') || '',
      learningRoute: analysisResult.learningPathRecommendation || ''
    };
    await saveComprehensiveReportHistory(params);
    console.log('综合评估报告保存成功');
  } catch (error) {
    console.error('保存综合评估报告失败:', error);
    throw error;
  }
};

// 更新面试状态
const updateInterviewState = () => {
  try {
    const existingInterviewState = uni.getStorageSync('interviewState');
    let interviewState = existingInterviewState ? JSON.parse(existingInterviewState) : {};

    interviewState.aiInterview = {
      completed: true,
      score: totalScore.value,
      jobId: jobId.value,
      lastCompletionTime: new Date().toISOString()
    };
    uni.setStorageSync('interviewState', JSON.stringify(interviewState));
    console.log('[Comprehensive Report] AI Interview overall state saved:', interviewState.aiInterview);
  } catch (error) {
    console.error('[Comprehensive Report] Error saving AI Interview overall state:', error);
  }
};

// Lifecycle hooks
// 2. 页面加载时自动生成综合报告
onLoad(async (options) => {
  console.log('[Comprehensive Report] onLoad options:', options);
  if (options.jobId) {
    jobId.value = options.jobId;
    console.log('[Comprehensive Report] Received Job ID:', jobId.value);
  }
  // Set report type and ID from query parameters
  if (options.type) {
    reportType.value = options.type;
    console.log('[Comprehensive Report] Received Report Type:', reportType.value);
  }
  if (options.id) {
    reportId.value = options.id;
    console.log('[Comprehensive Report] Received Report ID:', reportId.value);
  }
  assessmentId.value = String(options.assessmentId || '').trim();

  // 如果没有职位信息，尝试从job store获取
  if (!uni.getStorageSync('currentJobInfo') && jobId.value) {
    console.log('[Comprehensive Report] No job info in storage, trying to get from job store');
    try {
      await jobStore.fetchJobDetail(jobId.value);
      const jobInfo = jobStore.getCurrentJob;
      if (jobInfo) {
        uni.setStorageSync('currentJobInfo', JSON.stringify(jobInfo));
        console.log('[Comprehensive Report] Job info saved to storage:', jobInfo);
      }
    } catch (error) {
      console.error('[Comprehensive Report] Failed to get job info from store:', error);
    }
  }

  await loadReportDetails();
  // 注意：AI分析现在只在 loadReportDetails 中调用，避免重复调用
});

const loadReportDetails = async () => {
  try {
    const session = ensureActiveComprehensiveAssessmentSession(jobId.value, {
      assessmentId: assessmentId.value,
      createIfMissing: false
    });
    assessmentId.value = String(session?.assessmentId || assessmentId.value || '').trim();
    if (session?.state) {
      const state = normalizeComprehensiveState(session.state);
      console.log('[Comprehensive Report] Loaded state from storage:', state);

      resumeScore.value = state.resume?.score || 0;
      questionsScore.value = state.questions?.score || 0;
      audioScore.value = state.audio?.score || 0;

      resumeCompleted.value = state.resume?.completed || false;
      questionsCompleted.value = state.questions?.completed || false;
      audioCompleted.value = state.audio?.completed || false;

      console.log('[Comprehensive Report] Updated individual scores and completion status from storage.');
      
      // 如果是综合报告且所有模块都已完成，自动进行AI分析
      if (reportType.value === 'overall' && resumeCompleted.value && questionsCompleted.value && audioCompleted.value) {
        await performInitialAnalysis();
      }
    } else {
      console.log('[Comprehensive Report] No comprehensive assessment session found in storage.');
    }
  } catch (error) {
    console.error('[Comprehensive Report] Error loading state from localStorage:', error);
  }
};

// 初始AI分析
const performInitialAnalysis = async () => {
  try {
    aiAnalysisLoading.value = true;
    loadingProgress.value = 0;
    currentTipIndex.value = 0;
    
    // 启动加载动画
    let progressUpdateCount = 0;
    const progressInterval = setInterval(() => {
      if (loadingProgress.value < 85) { // 只增长到85%，等待AI响应
        loadingProgress.value += Math.random() * 5; // 降低增长速度
        progressUpdateCount++;
        
        // 词条在进度条从0%到85%期间只显示一轮（10个词条）
        // 预计需要17-20次更新才能达到85%
        // 每1.7-2次进度更新切换一次词条，确保在85%时完成一轮
        const tipSwitchThreshold = Math.ceil(17 / loadingTips.value.length); // 17次更新 / 10个词条 ≈ 1.7
        if (progressUpdateCount % tipSwitchThreshold === 0 && currentTipIndex.value < loadingTips.value.length - 1) {
          currentTipIndex.value = currentTipIndex.value + 1;
        }
      }
    }, 1000); // 增加间隔时间
    
    // 获取职位信息
    const jobInfoStr = uni.getStorageSync('currentJobInfo');
    const jobInfo = jobInfoStr ? JSON.parse(jobInfoStr) : { name: '', description: '' };
    
    // 获取各模块的详细信息
    const session = ensureActiveComprehensiveAssessmentSession(jobId.value, {
      assessmentId: assessmentId.value,
      createIfMissing: false
    });
    const state = normalizeComprehensiveState(session?.state);
    
    // 增加等待时间，确保进度条有足够时间显示完整的一轮词条
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    // 调用AI进行综合分析
    const analysisResult = await performComprehensiveAnalysis(jobInfo, state);
    
    // AI响应后，进度条达到100%
    loadingProgress.value = 100;
    clearInterval(progressInterval);
    
    // 确保显示最后一个词条
    currentTipIndex.value = loadingTips.value.length - 1;
    
    // 等待一小段时间让用户看到100%
    await new Promise(resolve => setTimeout(resolve, 500));
    
    // 更新AI分析结果显示
    aiAnalysisResult.value = analysisResult;
    
    console.log('[Comprehensive Report] Initial AI analysis completed');
    
  } catch (error) {
    console.error('[Comprehensive Report] Initial AI analysis failed:', error);
    // 显示错误提示给用户
    uni.showToast({
      title: error.message || 'AI分析失败，请重试',
      icon: 'none',
      duration: 3000
    });
  } finally {
    aiAnalysisLoading.value = false;
    // 不清零进度条，避免重复显示
    currentTipIndex.value = 0;
  }
};

</script>

<style lang="scss" scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css');

.comprehensive-report-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 50%, #16213e 100%);
  font-family: 'Arial', sans-serif;
  color: #fff;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  box-sizing: border-box;

  &.eye-care {
    background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 50%, #d4edda 100%);
    color: #333;
  }
}

// 科幻背景效果
.cyber-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;

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
    animation: grid-move 20s linear infinite;
  }

  .scanning-lines {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;

    .horizontal-scan {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 2px;
      background: linear-gradient(90deg, transparent, #00ffff, transparent);
      animation: scan-horizontal 8s linear infinite;
    }

    .vertical-scan {
      position: absolute;
      top: 0;
      left: 0;
      width: 2px;
      height: 100%;
      background: linear-gradient(180deg, transparent, #00ffff, transparent);
      animation: scan-vertical 6s linear infinite;
    }

    .diagonal-scan {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: linear-gradient(45deg, transparent 49%, #00ffff 50%, transparent 51%);
      animation: scan-diagonal 10s linear infinite;
    }

    .radar-scan {
      position: absolute;
      top: 50%;
      left: 50%;
      width: 200px;
      height: 200px;
      border: 2px solid rgba(0, 255, 255, 0.3);
      border-radius: 50%;
      transform: translate(-50%, -50%);
      animation: radar-scan 4s linear infinite;
    }
  }

  .floating-particles {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;

    .particle {
      position: absolute;
      width: 4px;
      height: 4px;
      background: #00ffff;
      border-radius: 50%;
      animation: particle-float 5s infinite ease-in-out;
      animation-delay: var(--delay);
      left: var(--x-pos);
      top: var(--y-pos);
    }
  }

  .energy-field {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 300px;
    height: 300px;
    border: 1px solid rgba(0, 255, 255, 0.2);
    border-radius: 50%;
    transform: translate(-50%, -50%);
    animation: energy-pulse 3s ease-in-out infinite;
  }

  .data-streams {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;

    .data-stream {
      position: absolute;
      width: 1px;
      height: 100px;
      background: linear-gradient(180deg, transparent, #00ffff, transparent);
      animation: data-stream 3s linear infinite;
      animation-delay: var(--stream-delay);
      left: calc(var(--stream-delay) * 10%);
    }
  }
}

// 主内容区域
.main-content {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 1200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
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
  left: 48px;
  top: 32px;
  z-index: 100;
  margin-right: 15px;
  box-shadow: 0 0 10px rgba(0, 255, 255, 0.2);

.back-button:hover {
  background: rgba(0, 255, 255, 0.3);
  transform: translateX(-5px);
  box-shadow: 0 0 15px rgba(0, 255, 255, 0.4);
}

/* 自定义返回箭头 */
.custom-back-arrow {
  position: relative;
  width: 16px;
  height: 16px;
  margin-right: 8px;
  transition: transform 0.3s ease;
}

.custom-back-arrow::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  width: 12px;
  height: 2px;
  background: #00ffff;
  transform: translateY(-50%);
  border-radius: 1px;
}

.custom-back-arrow::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  width: 0;
  height: 0;
  border-top: 4px solid transparent;
  border-bottom: 4px solid transparent;
  border-right: 6px solid #00ffff;
  transform: translateY(-50%);
}

.back-button:hover .custom-back-arrow {
  transform: translateX(-2px);
}
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

.eye-care .custom-back-arrow::before {
  background: #2e7d32;
}

.eye-care .custom-back-arrow::after {
  border-right-color: #2e7d32;
}

.eye-care .back-button:hover .custom-back-arrow::before {
  background: #1b5e20;
}

.eye-care .back-button:hover .custom-back-arrow::after {
  border-right-color: #1b5e20;
}

// 全息投影标题
.holographic-header {
  text-align: center;
  margin-bottom: 40px;

  .title-container {
    position: relative;
    display: inline-block;

    .main-title {
      position: relative;
      font-size: 48px;
      font-weight: bold;
      margin: 0;
      background: linear-gradient(45deg, #00ffff, #ff00ff, #ffff00);
      background-size: 200% 200%;
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      animation: title-glow 3s ease-in-out infinite;

      .title-text {
        position: relative;
        z-index: 2;
      }

      .title-glow {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: linear-gradient(45deg, #00ffff, #ff00ff);
        filter: blur(20px);
        opacity: 0.5;
        z-index: 1;
      }

      .title-scan-line {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 2px;
        background: linear-gradient(90deg, transparent, #00ffff, transparent);
        animation: scan-line 2s linear infinite;
      }
    }

    .subtitle {
      font-size: 18px;
      color: rgba(255, 255, 255, 0.7);
      margin-top: 10px;
      font-weight: 300;
    }
  }
}

// 报告全息卡片
.report-hologram {
  position: relative;
  width: 100%;
  max-width: 1000px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(0, 255, 255, 0.3);
  border-radius: 20px;
  padding: 40px;
  backdrop-filter: blur(20px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);

  .hologram-frame {
    position: relative;
    z-index: 2;
  }

  .hologram-glow {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(45deg, rgba(0, 255, 255, 0.1), rgba(255, 0, 255, 0.1));
    border-radius: 20px;
    filter: blur(30px);
    z-index: 1;
  }
}

// 分数显示区域
.overall-score-section,
.single-module-score-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
  margin-bottom: 40px;
}

.score-hologram {
  position: relative;

  .score-ring-container {
    position: relative;
    width: 300px;
    height: 300px;
    display: flex;
    justify-content: center;
    align-items: center;

    .outer-ring {
      position: absolute;
      width: 100%;
      height: 100%;
      border: 3px solid rgba(0, 255, 255, 0.3);
      border-radius: 50%;
      animation: ring-rotate 10s linear infinite;
    }

    .inner-ring {
      position: absolute;
      width: 80%;
      height: 80%;
      border: 2px solid rgba(255, 0, 255, 0.3);
      border-radius: 50%;
      animation: ring-rotate 8s linear infinite reverse;
    }

    .score-display {
      position: relative;
      width: 200px;
      height: 200px;
      border-radius: 50%;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      background: rgba(0, 0, 0, 0.3);
      border: 2px solid rgba(0, 255, 255, 0.5);
      backdrop-filter: blur(10px);

      &.score-excellent {
        border-color: #00ff00;
        box-shadow: 0 0 30px rgba(0, 255, 0, 0.5);
      }

      &.score-good {
        border-color: #00ffff;
        box-shadow: 0 0 30px rgba(0, 255, 255, 0.5);
      }

      &.score-fair {
        border-color: #ffff00;
        box-shadow: 0 0 30px rgba(255, 255, 0, 0.5);
      }

      &.score-poor {
        border-color: #ff0000;
        box-shadow: 0 0 30px rgba(255, 0, 0, 0.5);
      }

      .score-value {
        font-size: 60px;
        font-weight: bold;
        color: #fff;
        text-shadow: 0 0 10px currentColor;
      }

      .score-label {
        font-size: 16px;
        color: rgba(255, 255, 255, 0.8);
        margin-top: 5px;
      }

      .score-glow {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        border-radius: 50%;
        background: radial-gradient(circle, rgba(0, 255, 255, 0.2), transparent);
        animation: score-pulse 2s ease-in-out infinite;
      }
    }
  }
}

.overall-feedback {
  text-align: center;
  max-width: 600px;

  .feedback-title {
    font-size: 28px;
    color: #00ffff;
    margin-bottom: 15px;
    text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
  }

  .feedback-text {
    font-size: 16px;
    color: rgba(255, 255, 255, 0.8);
    line-height: 1.6;
  }
}

// 模块概览区域
.module-summary-section {
  width: 100%;
  margin-bottom: 40px;
  padding-top: 30px;
  border-top: 1px solid rgba(0, 255, 255, 0.3);

  .section-heading {
    text-align: center;
    margin-bottom: 30px;
    position: relative;

    .heading-text {
      font-size: 24px;
      color: #00ffff;
      font-weight: bold;
      text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
    }

    .heading-line {
      position: absolute;
      bottom: -10px;
      left: 50%;
      transform: translateX(-50%);
      width: 100px;
      height: 2px;
      background: linear-gradient(90deg, transparent, #00ffff, transparent);
    }
  }

  .module-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 20px;
  }

  .module-hologram-item {
    position: relative;
    transition: all 0.3s ease;

    &.status-completed {
      .module-frame {
        border-color: #00ff00;
        box-shadow: 0 0 20px rgba(0, 255, 0, 0.3);
      }
    }

    &.status-pending {
      .module-frame {
        border-color: #ffff00;
        box-shadow: 0 0 20px rgba(255, 255, 0, 0.3);
      }
    }

    .module-frame {
      position: relative;
      background: rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(0, 255, 255, 0.3);
      border-radius: 15px;
      padding: 25px;
      text-align: center;
      backdrop-filter: blur(10px);
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
      }

      .module-icon {
        font-size: 40px;
        margin-bottom: 15px;
      }

      .module-title {
        font-size: 18px;
        color: #fff;
        font-weight: bold;
        margin-bottom: 10px;
      }

      .module-score {
        font-size: 32px;
        color: #00ffff;
        font-weight: bold;
        margin-bottom: 5px;
        text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
      }

      .module-weight {
        font-size: 12px;
        color: rgba(255, 255, 255, 0.6);
        margin-bottom: 10px;
      }

      .module-status {
        font-size: 12px;
        padding: 5px 15px;
        border-radius: 15px;
        font-weight: 500;
        background: rgba(0, 255, 255, 0.1);
        color: #00ffff;
        border: 1px solid rgba(0, 255, 255, 0.3);
      }

      .module-glow {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        border-radius: 15px;
        background: radial-gradient(circle, rgba(0, 255, 255, 0.1), transparent);
        opacity: 0;
        transition: opacity 0.3s ease;
      }

      &:hover .module-glow {
        opacity: 1;
      }
    }
  }
}

// 详细分析区域
.detailed-analysis-section {
  width: 100%;
  margin-bottom: 40px;
  padding-top: 30px;
  border-top: 1px solid rgba(0, 255, 255, 0.3);
  display: flex;
  flex-direction: column;
  gap: 20px;

  .section-heading {
    text-align: center;
    margin-bottom: 30px;
    position: relative;

    .heading-text {
      font-size: 24px;
      color: #00ffff;
      font-weight: bold;
      text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
    }

    .heading-line {
      position: absolute;
      bottom: -10px;
      left: 50%;
      transform: translateX(-50%);
      width: 100px;
      height: 2px;
      background: linear-gradient(90deg, transparent, #00ffff, transparent);
    }
  }

  .analysis-hologram {
    position: relative;

    .analysis-frame {
      background: rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(0, 255, 255, 0.3);
      border-radius: 15px;
      padding: 25px;
      backdrop-filter: blur(10px);
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
      }

      .analysis-header {
        display: flex;
        align-items: center;
        gap: 15px;
        margin-bottom: 20px;

        .header-icon {
          width: 40px;
          height: 40px;
          background: rgba(0, 255, 255, 0.1);
          border: 1px solid rgba(0, 255, 255, 0.3);
          border-radius: 50%;
          display: flex;
          justify-content: center;
          align-items: center;
          color: #00ffff;
          font-size: 18px;
        }

        .analysis-title {
          font-size: 20px;
          color: #fff;
          font-weight: bold;
          margin: 0;
        }
      }

      .analysis-content {
        .analysis-text {
          font-size: 16px;
          color: rgba(255, 255, 255, 0.8);
          line-height: 1.6;
          margin-bottom: 20px;
        }

        .suggestion-list {
          display: flex;
          flex-direction: column;
          gap: 15px;

          .suggestion-item {
            display: flex;
            align-items: center;
            gap: 15px;
            padding: 15px;
            background: rgba(0, 255, 255, 0.05);
            border: 1px solid rgba(0, 255, 255, 0.2);
            border-radius: 10px;
            transition: all 0.3s ease;

            &:hover {
              background: rgba(0, 255, 255, 0.1);
              transform: translateX(5px);
            }

            .suggestion-icon {
              font-size: 20px;
              min-width: 30px;
            }

            span {
              font-size: 14px;
              color: rgba(255, 255, 255, 0.9);
              line-height: 1.5;
            }
          }
        }

        // 新增样式
        .strength-section,
        .improvement-section,
        .learning-path-section {
          margin-top: 20px;
          padding-top: 15px;
          border-top: 1px solid rgba(0, 255, 255, 0.2);
        }

        .subsection-title {
          font-size: 16px;
          font-weight: 600;
          color: #00ffff;
          margin-bottom: 15px;
          display: flex;
          align-items: center;
          gap: 8px;
        }

        .learning-path-text {
          font-size: 14px;
          color: rgba(255, 255, 255, 0.9);
          line-height: 1.6;
          background: rgba(0, 255, 255, 0.05);
          padding: 15px;
          border-radius: 8px;
          border-left: 3px solid #00ffff;
        }
      }
    }
  }
}

// 报告底部
.report-footer {
  text-align: center;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 1px solid rgba(0, 255, 255, 0.3);

  .complete-button {
    position: relative;
    padding: 15px 40px;
    background: linear-gradient(45deg, #00ffff, #ff00ff);
    border: none;
    border-radius: 25px;
    color: #000;
    font-size: 18px;
    font-weight: bold;
    cursor: pointer;
    transition: all 0.3s ease;
    overflow: hidden;

    &:hover:not(:disabled) {
      transform: translateY(-3px);
      box-shadow: 0 10px 30px rgba(0, 255, 255, 0.4);
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
      transform: none;
    }

    &:disabled:hover {
      transform: none;
      box-shadow: 0 4px 15px rgba(0, 255, 255, 0.2);
    }

    .button-content {
      position: relative;
      z-index: 2;
    }

    .button-glow {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: linear-gradient(45deg, #00ffff, #ff00ff);
      filter: blur(20px);
      opacity: 0.5;
      z-index: 1;
    }
  }

  .footer-text {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.5);
    margin-top: 20px;
  }
}

// 动画定义
@keyframes grid-move {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}

@keyframes scan-horizontal {
  0% { transform: translateY(-100%); }
  100% { transform: translateY(100vh); }
}

@keyframes scan-vertical {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100vw); }
}

@keyframes scan-diagonal {
  0% { transform: translate(-100%, -100%); }
  100% { transform: translate(100vw, 100vh); }
}

@keyframes radar-scan {
  0% { transform: translate(-50%, -50%) rotate(0deg); }
  100% { transform: translate(-50%, -50%) rotate(360deg); }
}

@keyframes particle-float {
  0%, 100% { transform: translateY(0px) translateX(0px); opacity: 0; }
  50% { transform: translateY(-20px) translateX(var(--x-end)); opacity: 1; }
}

@keyframes energy-pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.3; }
  50% { transform: translate(-50%, -50%) scale(1.2); opacity: 0.6; }
}

@keyframes data-stream {
  0% { transform: translateY(-100px); opacity: 0; }
  50% { opacity: 1; }
  100% { transform: translateY(100vh); opacity: 0; }
}

@keyframes title-glow {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

@keyframes scan-line {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

@keyframes ring-rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes score-pulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.6; }
}

/* 护眼模式样式 */
.eye-care {
  background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 50%, #d4edda 100%) !important;
  color: #333 !important;
}

.eye-care .cyber-background {
  .grid-lines {
    background-image: 
      linear-gradient(rgba(76, 175, 80, 0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba(76, 175, 80, 0.03) 1px, transparent 1px) !important;
    animation: grid-move 30s linear infinite !important;
  }

  .scanning-lines {
    .horizontal-scan {
      background: linear-gradient(90deg, transparent, rgba(76, 175, 80, 0.2), transparent) !important;
      animation: scan-horizontal 12s linear infinite !important;
    }

    .vertical-scan {
      background: linear-gradient(180deg, transparent, rgba(76, 175, 80, 0.2), transparent) !important;
      animation: scan-vertical 10s linear infinite !important;
    }

    .diagonal-scan {
      background: linear-gradient(45deg, transparent 49%, rgba(76, 175, 80, 0.2) 50%, transparent 51%) !important;
      animation: scan-diagonal 15s linear infinite !important;
    }

    .radar-scan {
      border-color: rgba(76, 175, 80, 0.1) !important;
      animation: radar-scan 6s linear infinite !important;
    }
  }

  .floating-particles .particle {
    background: rgba(76, 175, 80, 0.3) !important;
    animation: particle-float 8s infinite ease-in-out !important;
  }

  .energy-field {
    border-color: rgba(76, 175, 80, 0.1) !important;
    animation: energy-pulse 4s ease-in-out infinite !important;
  }

  .data-streams .data-stream {
    background: linear-gradient(180deg, transparent, rgba(76, 175, 80, 0.2), transparent) !important;
    animation: data-stream 4s linear infinite !important;
  }
}



.eye-care .back-button {
  background: rgba(76, 175, 80, 0.3) !important;
  border-color: rgba(76, 175, 80, 0.6) !important;
  color: #2e7d32 !important;
  box-shadow: 0 0 12px rgba(76, 175, 80, 0.4) !important;
}

.eye-care .back-button:hover {
  background: rgba(76, 175, 80, 0.5) !important;
  border-color: rgba(76, 175, 80, 0.8) !important;
  color: #1b5e20 !important;
  box-shadow: 0 0 18px rgba(76, 175, 80, 0.6) !important;
  transform: translateX(-5px) !important;
}

.eye-care .custom-back-arrow::before {
  background: #2e7d32 !important;
}

.eye-care .custom-back-arrow::after {
  border-right-color: #2e7d32 !important;
}

.eye-care .back-button:hover .custom-back-arrow::before {
  background: #1b5e20 !important;
}

.eye-care .back-button:hover .custom-back-arrow::after {
  border-right-color: #1b5e20 !important;
}

.eye-care .holographic-header {
  .title-container {
    .main-title {
      background: linear-gradient(45deg, #2e7d32, #4caf50, #66bb6a) !important;
      -webkit-background-clip: text !important;
      -webkit-text-fill-color: transparent !important;
    }

    .title-glow {
      background: linear-gradient(45deg, #2e7d32, #4caf50) !important;
    }

    .title-scan-line {
      background: linear-gradient(90deg, transparent, #2e7d32, transparent) !important;
    }

    .subtitle {
      color: rgba(46, 125, 50, 0.8) !important;
    }
  }
}

.eye-care .report-hologram {
  background: rgba(255, 255, 255, 0.95) !important;
  border-color: rgba(76, 175, 80, 0.4) !important;
  box-shadow: 0 20px 40px rgba(76, 175, 80, 0.2) !important;
  color: #333 !important;
}

.eye-care .report-hologram .hologram-glow {
  background: linear-gradient(45deg, rgba(76, 175, 80, 0.1), rgba(129, 199, 132, 0.1)) !important;
}

.eye-care .score-hologram {
  .score-ring-container {
    .outer-ring {
      border-color: rgba(76, 175, 80, 0.4) !important;
    }

    .inner-ring {
      border-color: rgba(129, 199, 132, 0.4) !important;
    }

    .score-display {
      background: rgba(255, 255, 255, 0.95) !important;
      border-color: rgba(76, 175, 80, 0.6) !important;
      color: #333 !important;

      &.score-excellent {
        border-color: #2e7d32 !important;
        box-shadow: 0 0 30px rgba(76, 175, 80, 0.4) !important;
      }

      &.score-good {
        border-color: #4caf50 !important;
        box-shadow: 0 0 30px rgba(76, 175, 80, 0.4) !important;
      }

      &.score-fair {
        border-color: #8bc34a !important;
        box-shadow: 0 0 30px rgba(139, 195, 74, 0.4) !important;
      }

      &.score-poor {
        border-color: #f44336 !important;
        box-shadow: 0 0 30px rgba(244, 67, 54, 0.4) !important;
      }

      .score-value {
        color: #333 !important;
        text-shadow: 0 0 10px rgba(76, 175, 80, 0.4) !important;
      }

      .score-label {
        color: rgba(46, 125, 50, 0.8) !important;
      }

      .score-glow {
        background: radial-gradient(circle, rgba(76, 175, 80, 0.2), transparent) !important;
      }
    }
  }
}

.eye-care .overall-feedback {
  .feedback-title {
    color: #2e7d32 !important;
    text-shadow: 0 0 10px rgba(76, 175, 80, 0.4) !important;
  }

  .feedback-text {
    color: rgba(46, 125, 50, 0.9) !important;
  }
}

.eye-care .module-summary-section {
  border-top-color: rgba(76, 175, 80, 0.4) !important;

  .section-heading {
    .heading-text {
      color: #2e7d32 !important;
      text-shadow: 0 0 10px rgba(76, 175, 80, 0.4) !important;
    }

    .heading-line {
      background: linear-gradient(90deg, transparent, #2e7d32, transparent) !important;
    }
  }

  .module-hologram-item {
    &.status-completed {
      .module-frame {
        border-color: #2e7d32 !important;
        box-shadow: 0 0 20px rgba(76, 175, 80, 0.3) !important;
      }
    }

    &.status-pending {
      .module-frame {
        border-color: #ff9800 !important;
        box-shadow: 0 0 20px rgba(255, 152, 0, 0.3) !important;
      }
    }

    .module-frame {
      background: rgba(255, 255, 255, 0.95) !important;
      border-color: rgba(76, 175, 80, 0.4) !important;
      color: #333 !important;

      &:hover {
        box-shadow: 0 10px 30px rgba(76, 175, 80, 0.2) !important;
      }

      .module-title {
        color: #333 !important;
      }

      .module-score {
        color: #2e7d32 !important;
        text-shadow: 0 0 10px rgba(76, 175, 80, 0.4) !important;
      }

      .module-weight {
        color: rgba(46, 125, 50, 0.7) !important;
      }

      .module-status {
        background: rgba(76, 175, 80, 0.2) !important;
        color: #2e7d32 !important;
        border-color: rgba(76, 175, 80, 0.4) !important;
      }

      .module-glow {
        background: radial-gradient(circle, rgba(76, 175, 80, 0.1), transparent) !important;
      }
    }
  }
}

.eye-care .detailed-analysis-section {
  border-top-color: rgba(76, 175, 80, 0.4) !important;

  .section-heading {
    .heading-text {
      color: #2e7d32 !important;
      text-shadow: 0 0 10px rgba(76, 175, 80, 0.4) !important;
    }

    .heading-line {
      background: linear-gradient(90deg, transparent, #2e7d32, transparent) !important;
    }
  }

  .analysis-hologram {
    .analysis-frame {
      background: rgba(255, 255, 255, 0.95) !important;
      border-color: rgba(76, 175, 80, 0.4) !important;
      color: #333 !important;

      &:hover {
        box-shadow: 0 10px 30px rgba(76, 175, 80, 0.2) !important;
      }

      .analysis-header {
        .header-icon {
          background: rgba(76, 175, 80, 0.2) !important;
          border-color: rgba(76, 175, 80, 0.4) !important;
          color: #2e7d32 !important;
        }

        .analysis-title {
          color: #333 !important;
        }
      }

      .analysis-content {
        .analysis-text {
          color: rgba(46, 125, 50, 0.9) !important;
        }

        .suggestion-list {
          .suggestion-item {
            background: rgba(76, 175, 80, 0.1) !important;
            border-color: rgba(76, 175, 80, 0.3) !important;
            color: #333 !important;

            &:hover {
              background: rgba(76, 175, 80, 0.2) !important;
            }

            span {
              color: rgba(46, 125, 50, 0.9) !important;
            }
          }
        }

        .subsection-title {
          color: #2e7d32 !important;
        }

        .learning-path-text {
          color: rgba(46, 125, 50, 0.9) !important;
          background: rgba(76, 175, 80, 0.1) !important;
          border-left-color: #2e7d32 !important;
        }
      }
    }
  }
}

.eye-care .report-footer {
  border-top-color: rgba(76, 175, 80, 0.4) !important;

  .complete-button {
    background: linear-gradient(45deg, #4caf50, #66bb6a) !important;
    color: #ffffff !important;
    box-shadow: 0 4px 15px rgba(76, 175, 80, 0.3) !important;

    &:hover:not(:disabled) {
      background: linear-gradient(45deg, #66bb6a, #81c784) !important;
      box-shadow: 0 10px 30px rgba(76, 175, 80, 0.4) !important;
    }

    &:disabled {
      background: linear-gradient(45deg, #bdbdbd, #e0e0e0) !important;
      color: #757575 !important;
      box-shadow: 0 2px 8px rgba(189, 189, 189, 0.3) !important;
    }

    .button-glow {
      background: linear-gradient(45deg, #4caf50, #66bb6a) !important;
    }
  }

  .footer-text {
    color: rgba(46, 125, 50, 0.6) !important;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .main-content {
    padding: 10px;
  }

  .holographic-header .title-container .main-title {
    font-size: 32px;
  }

  .score-hologram .score-ring-container {
    width: 250px;
    height: 250px;

    .score-display {
      width: 150px;
      height: 150px;

      .score-value {
        font-size: 40px;
      }
    }
  }

  .module-grid {
    grid-template-columns: 1fr;
  }

  .report-hologram {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .holographic-header .title-container .main-title {
    font-size: 24px;
  }

  .score-hologram .score-ring-container {
    width: 200px;
    height: 200px;

    .score-display {
      width: 120px;
      height: 120px;

      .score-value {
        font-size: 32px;
      }
    }
  }

  .report-hologram {
    padding: 15px;
  }
}

// 加载动画样式
.analysis-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;

  .loading-container {
    width: 100%;
    max-width: 600px;
  }

  .loading-hologram {
    background: rgba(0, 0, 0, 0.3);
    border: 1px solid rgba(0, 255, 255, 0.3);
    border-radius: 20px;
    padding: 40px;
    backdrop-filter: blur(10px);
    text-align: center;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: linear-gradient(45deg, rgba(0, 255, 255, 0.1), rgba(255, 0, 255, 0.1));
      border-radius: 20px;
      filter: blur(20px);
      z-index: -1;
    }



    .loading-text {
      margin-bottom: 30px;

      .loading-title {
        font-size: 24px;
        color: #00ffff;
        margin-bottom: 10px;
        text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
        animation: title-pulse 2s ease-in-out infinite;
      }

      .loading-subtitle {
        font-size: 16px;
        color: rgba(255, 255, 255, 0.7);
        margin-bottom: 20px;
      }

      .loading-tips {
        .tip-item {
          font-size: 14px;
          color: rgba(255, 255, 255, 0.6);
          margin-bottom: 8px;
          opacity: 0.3;
          transition: all 0.5s ease;

          &.active {
            opacity: 1;
            color: #00ffff;
            text-shadow: 0 0 5px rgba(0, 255, 255, 0.5);
            transform: scale(1.05);
          }
        }
      }
    }

    .loading-progress {
      .progress-bar {
        width: 100%;
        height: 8px;
        background: rgba(0, 255, 255, 0.2);
        border-radius: 4px;
        overflow: hidden;
        margin-bottom: 10px;
        position: relative;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background: linear-gradient(90deg, transparent, rgba(0, 255, 255, 0.3), transparent);
          animation: progress-shine 2s linear infinite;
        }

        .progress-fill {
          height: 100%;
          background: linear-gradient(90deg, #00ffff, #ff00ff);
          border-radius: 4px;
          transition: width 0.3s ease;
          position: relative;
          z-index: 1;
        }
      }

      .progress-text {
        font-size: 16px;
        color: #00ffff;
        font-weight: bold;
        text-shadow: 0 0 5px rgba(0, 255, 255, 0.5);
      }
    }
  }
}

// 护眼模式下的加载动画样式
.eye-care .analysis-loading {
  .loading-hologram {
    background: rgba(255, 255, 255, 0.95) !important;
    border-color: rgba(76, 175, 80, 0.4) !important;
    color: #333 !important;

    &::before {
      background: linear-gradient(45deg, rgba(76, 175, 80, 0.1), rgba(129, 199, 132, 0.1)) !important;
    }



    .loading-text {
      .loading-title {
        color: #2e7d32 !important;
        text-shadow: 0 0 10px rgba(76, 175, 80, 0.4) !important;
      }

      .loading-subtitle {
        color: rgba(46, 125, 50, 0.8) !important;
      }

      .loading-tips {
        .tip-item {
          color: rgba(46, 125, 50, 0.6) !important;

          &.active {
            color: #2e7d32 !important;
            text-shadow: 0 0 5px rgba(76, 175, 80, 0.4) !important;
          }
        }
      }
    }

    .loading-progress {
      .progress-bar {
        background: rgba(76, 175, 80, 0.2) !important;

        &::before {
          background: linear-gradient(90deg, transparent, rgba(76, 175, 80, 0.3), transparent) !important;
        }

        .progress-fill {
          background: linear-gradient(90deg, #4caf50, #66bb6a) !important;
        }
      }

      .progress-text {
        color: #2e7d32 !important;
        text-shadow: 0 0 5px rgba(76, 175, 80, 0.4) !important;
      }
    }
  }
}

// 加载动画关键帧

@keyframes title-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

@keyframes progress-shine {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}
</style> 

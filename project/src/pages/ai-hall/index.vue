<template>
  <div class="ai-hall-container" :class="{ 'eye-care': isEyeCareMode }">
    <div id="stars"></div>
    <div id="stars2"></div>
    <div id="stars3"></div>
    <div v-if="isTransitioning" class="page-transition-overlay" :class="{ 'eye-care': isEyeCareMode }"></div>
    
    <div class="content-container compact-hall-layout">
      <div class="page-header">
        <div class="back-button" @click="handleBack">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1>面试大厅</h1>
      </div>

      <!-- AI 模拟面试模块 -->
      <div class="module-section">
        <h2 class="section-title">AI 模拟面试</h2>
        <div class="modules-list">
          <div
            class="module-card interview-ai-module ai-view-entry-card glass-ai-card"
            role="button"
            tabindex="0"
            @click="goToInterviewAI"
            @keydown.enter="goToInterviewAI"
          >
            <div class="module-info">
              <div class="ai-badge"><svg class="badge-svg" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="5" y="9" width="14" height="8" rx="4" stroke="currentColor" stroke-width="2"/><circle cx="10" cy="13" r="1.3" fill="currentColor"/><circle cx="14" cy="13" r="1.3" fill="currentColor"/><path d="M12 8V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg><span>AI</span></div>
              <h3>AIview</h3>
              <div class="module-details">
                <p class="module-description">统一承接简历投递、试题作答与三轮面试，用一个对话入口串起岗位理解、过程承接、阶段反馈和最终建议。</p>
                <p class="module-click-hint">从这里开始完整 AI 模拟面试</p>
                <div class="competency-indicators">
                  <span>简历投递</span>
                  <span>试题作答</span>
                  <span>一面</span>
                  <span>二面</span>
                  <span>三面</span>
                </div>
              </div>
            </div>
            <div class="module-icon ai-icon">
              <svg class="module-svg" width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="5" y="8" width="14" height="9" rx="4" stroke="currentColor" stroke-width="2"/>
                <circle cx="9" cy="12" r="1.5" fill="currentColor"/>
                <circle cx="15" cy="12" r="1.5" fill="currentColor"/>
                <path d="M12 7V5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M8 18h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="ai-view-tooltip ai-view-dynamic-island" role="tooltip">
              <span class="island-orb"></span>
              <span class="island-text">开始 AI 面试</span>
              <span class="island-arrow">↗</span>
            </div>
            <div class="ai-glow"></div>
          </div>
        </div>
      </div>

      <div class="module-section insights-module-section">
        <h2 class="section-title">历史洞察</h2>
        <div v-if="insightsError" class="insights-empty">
          {{ insightsError }}
        </div>
        <div v-else class="insights-content">
          <div class="insight-stages-grid">
            <div
              v-for="stage in insightStages"
              :key="stage.stageKey"
              class="insight-stage-card glass-insight-card"
              :class="`status-${stage.status}`"
              role="button"
              tabindex="0"
              @click="openInsightDetail(stage)"
              @keydown.enter="openInsightDetail(stage)"
            >
              <div class="stage-topline">
                <span>{{ stage.stageName }}</span>
                <em>{{ getStageStatusLabel(stage.status) }}</em>
              </div>
              <div class="stage-score">
                <strong>{{ formatScore(stage.averageScore) }}</strong>
                <span>平均分</span>
              </div>
              <div class="stage-meta">
                <span>近 7 天 {{ formatCount(stage.attemptCount) }} 次</span>
                <span>最高 {{ formatScore(stage.maxScore) }}</span>
              </div>
              <p>规则建议：{{ stage.advice }}</p>
            </div>
          </div>
        </div>
      </div>

      <transition name="insight-detail-pop">
        <div
          v-if="selectedInsightStage"
          class="insight-detail-overlay"
          @click.self="closeInsightDetail"
        >
          <div class="insight-detail-modal">
            <button class="insight-detail-close" type="button" @click="closeInsightDetail">×</button>
            <p class="insight-detail-kicker">历史洞察详情</p>
            <h3>{{ selectedInsightStage.stageName }}</h3>
            <div class="insight-detail-score">
              <strong>{{ formatScore(selectedInsightStage.averageScore) }}</strong>
              <span>平均分</span>
            </div>
            <div class="insight-detail-metrics">
              <div>
                <span>近 7 天次数</span>
                <strong>{{ formatCount(selectedInsightStage.attemptCount) }}</strong>
              </div>
              <div>
                <span>历史最高分</span>
                <strong>{{ formatScore(selectedInsightStage.maxScore) }}</strong>
              </div>
              <div>
                <span>状态判断</span>
                <strong>{{ getStageStatusLabel(selectedInsightStage.status) }}</strong>
              </div>
            </div>
            <p class="insight-detail-advice">规则建议：{{ selectedInsightStage.advice }}</p>
          </div>
        </div>
      </transition>

      <view class="project-description-section">
        <h2 class="team-name">关于我们 · 梦之队</h2>
        <p class="project-info">
          我们是「梦之队」，很高兴为您提供服务(OmO)<br> 进入 AIview 系统后，平台将围绕目标岗位完整串联简历投递、在线笔试、初面、复面、终面全流程；各环节结束后自动回流 AI 对话链路，实时输出针对性测评反馈、综合能力判定与后续行动指引，打造全程无断点的沉浸式面试体验。
        </p>
      </view>

    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router'; // 导入 useRouter 和 useRoute
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
import { fetchAiviewJobInsights } from '@/utils/api/pages/ai-hall';

const router = useRouter(); // 初始化 router
const route = useRoute(); // 初始化 route
const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);
const isTransitioning = ref(false);

// 添加当前职位ID的状态
const currentJobId = ref(null);
const insights = ref(null);
const isInsightsLoading = ref(false);
const insightsError = ref('');
const selectedInsightStage = ref(null);

const fallbackInsightStages = [
  { stageKey: 'resume', stageName: '简历投递', attemptCount: 0, participantCount: 0, averageScore: null, maxScore: null, status: 'insufficient-data', advice: '简历投递近 7 天暂无有效样本，建议先引导候选人完成该环节以形成趋势判断。' },
  { stageKey: 'question', stageName: '试题作答', attemptCount: 0, participantCount: 0, averageScore: null, maxScore: null, status: 'insufficient-data', advice: '试题作答近 7 天暂无有效样本，建议先引导候选人完成该环节以形成趋势判断。' },
  { stageKey: 'round_1', stageName: '一面', attemptCount: 0, participantCount: 0, averageScore: null, maxScore: null, status: 'insufficient-data', advice: '一面近 7 天暂无有效样本，建议先引导候选人完成该环节以形成趋势判断。' },
  { stageKey: 'round_2', stageName: '二面', attemptCount: 0, participantCount: 0, averageScore: null, maxScore: null, status: 'insufficient-data', advice: '二面近 7 天暂无有效样本，建议先引导候选人完成该环节以形成趋势判断。' },
  { stageKey: 'round_3', stageName: '三面', attemptCount: 0, participantCount: 0, averageScore: null, maxScore: null, status: 'insufficient-data', advice: '三面近 7 天暂无有效样本，建议先引导候选人完成该环节以形成趋势判断。' }
];

const insightStages = computed(() => insights.value?.stages?.length ? insights.value.stages : fallbackInsightStages);

const openInsightDetail = (stage) => {
  selectedInsightStage.value = stage;
};

const closeInsightDetail = () => {
  selectedInsightStage.value = null;
};

// 从路由或本地存储加载当前职位ID
const loadCurrentJobId = () => {
  const jobIdFromRoute = route.query.jobId;
  const jobIdFromStorage = uni.getStorageSync('currentJobId');

  if (jobIdFromRoute) {
    // 如果URL中有jobId，以URL的为准并更新localStorage
    currentJobId.value = jobIdFromRoute;
    uni.setStorageSync('currentJobId', jobIdFromRoute);
    console.log('[Interview Interface] Loaded jobId from route:', jobIdFromRoute);
    loadAiviewInsights();
  } else if (jobIdFromStorage) {
    // 如果URL中没有，但localStorage有，则从localStorage加载
    currentJobId.value = jobIdFromStorage;
    console.log('[Interview Interface] Loaded jobId from storage:', jobIdFromStorage);
    loadAiviewInsights();
  } else {
    // 如果都没有，提示用户选择职位
    console.warn('[Interview Interface] No jobId found, redirecting to job selection.');
    uni.showToast({
      title: '请先选择职位',
      icon: 'none',
      duration: 2000
    });
    setTimeout(() => {
      router.replace('/pages/job-selection/index');
    }, 2000);
  }
};

const loadAiviewInsights = async () => {
  const jobId = currentJobId.value;
  if (!jobId) {
    insights.value = null;
    insightsError.value = '请先选择岗位后查看 AI 洞察。';
    return;
  }

  isInsightsLoading.value = true;
  insightsError.value = '';
  try {
    const response = await fetchAiviewJobInsights(jobId);
    insights.value = response?.data || null;
  } catch (error) {
    console.error('[AIview Insights] Failed to load insights:', error);
    insights.value = null;
    insightsError.value = 'AI 洞察暂时不可用，不影响进入 AIview 面试流程。';
  } finally {
    isInsightsLoading.value = false;
  }
};

const getStageStatusLabel = (status) => {
  const labels = {
    'insufficient-data': '样本不足',
    'at-risk': '需优先处理',
    'needs-attention': '待提升',
    stable: '表现稳定'
  };
  return labels[status] || '待观察';
};

const formatScore = (score) => {
  if (score === null || score === undefined) {
    return '--';
  }
  return Number(score).toFixed(1);
};

const formatCount = (count) => {
  if (count === null || count === undefined) {
    return 0;
  }
  return Number(count) || 0;
};

// 智能体面试导航
const goToInterviewAI = () => {
  const jobId = currentJobId.value;
  if (!jobId) {
    uni.showToast({
      title: '请先选择岗位',
      icon: 'none',
      duration: 2000
    });
    router.replace('/pages/job-selection/index');
    return;
  }

  // 确保在跳转前将 currentJobId 保存到本地存储
  uni.setStorageSync('currentJobId', jobId);

  isTransitioning.value = true;
  setTimeout(() => {
    uni.navigateTo({
      url: '/pages/ai-chat/index?jobId=' + jobId + '&autoOpen=0',
      success: function() {},
      fail: function(err) {
      console.error('Navigation failed:', err);
      uni.showToast({
        title: '跳转失败，请重试',
        icon: 'none'
      });
      }
    });
  }, 180);
};

// 初始化护眼模式
const initEyeCareMode = () => {
  try {
    const savedEyeCareMode = localStorage.getItem('eyeCareMode');
    if (savedEyeCareMode === 'true') {
      isEyeCareMode.value = true;
    }
  } catch (error) {
    console.error('Error initializing eye care mode:', error);
  }
};

// 组件挂载时加载状态和用户信息
onMounted(() => {
  console.log('[Lifecycle] Component mounted');
  loadCurrentJobId(); // 加载当前职位ID
  initEyeCareMode();
});

// 定义页面生命周期钩子
const onLoad = () => {
  console.log('[Lifecycle] Page loaded');
};

const onShow = () => {
  console.log('[Lifecycle] Page shown');
  loadCurrentJobId(); // 确保从其他页面返回时也能重新检查jobId
};

// 页面隐藏时保存状态
const onHide = () => {
  console.log('[Lifecycle] Page hidden');
};

// 添加返回按钮处理函数
const handleBack = () => {
  const jobId = currentJobId.value;
  isTransitioning.value = true;
  setTimeout(() => {
    if (jobId) {
      uni.reLaunch({ url: `/pages/job-selection/index?jobId=${jobId}` });
    } else {
      uni.reLaunch({ url: '/pages/job-selection/index' });
    }
  }, 180);
};

// 导出页面生命周期钩子
defineExpose({
  onLoad,
  onShow,
  onHide
});
</script>

<style lang="scss">
@use "sass:math";
@use "sass:string";
.ai-hall-container {
  min-height: 100vh;
  padding: 16px;
  background: linear-gradient(135deg, #0a1929 0%, #1a1f35 100%);
  position: relative;
  overflow: hidden;

  &.eye-care {
    background: linear-gradient(135deg, #1c2a1f 0%, #2a3c2e 100%);

    &::before {
      background-image: 
        linear-gradient(rgba(144, 198, 149, 0.03) 1px, transparent 1px),
        linear-gradient(90deg, rgba(144, 198, 149, 0.03) 1px, transparent 1px);
      opacity: 0.2;
    }
  }
}

.page-transition-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  pointer-events: none;
  background: radial-gradient(circle at 50% 50%, rgba(124,77,255,0.18), rgba(0,0,0,0.55));
  animation: fadeOutSoft 220ms ease forwards;
}
@keyframes fadeOutSoft { from { opacity: 0; } to { opacity: 1; } }
.ai-hall-container.eye-care .page-transition-overlay {
  background: radial-gradient(circle at 50% 50%, rgba(127,176,105,0.18), rgba(0,0,0,0.40));
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.compact-hall-layout {
  max-width: 1120px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 16px 0;
  margin-bottom: 24px;

  h1 {
    font-size: 32px;
    font-weight: 600;
    background: linear-gradient(135deg, #7c4dff 0%, #00e5ff 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    text-shadow: 0 0 20px rgba(124, 77, 255, 0.3);
    position: relative;
    z-index: 1;

    &::after {
      content: '';
      position: absolute;
      bottom: -8px;
      left: 50%;
      transform: translateX(-50%);
      width: 60%;
      height: 2px;
      background: linear-gradient(90deg, transparent, #7c4dff, #00e5ff, transparent);
    }
  }

  .back-button {
    position: absolute;
    left: 20px;
    top: 50%;
    transform: translateY(-50%);
  cursor: pointer;
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

    &:hover {
      transform: translateY(-50%) scale(1.05);
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
}

.compact-hall-layout .page-header {
  padding: 6px 0 10px;
  margin-bottom: 10px;

  h1 {
    font-size: 26px;
  }
}

.module-section {
  margin-bottom: 24px;

  .section-title {
    font-size: 20px;
    font-weight: 600;
    color: #ffffff;
    margin-bottom: 16px;
    padding-left: 16px;
    position: relative;
    display: flex;
    align-items: center;
    text-shadow: 0 0 10px rgba(124, 77, 255, 0.3);

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 4px;
      height: 20px;
      background: linear-gradient(to bottom, #7c4dff, #00e5ff);
      border-radius: 2px;
    }

    &::after {
      content: '';
      flex: 1;
      height: 1px;
      background: linear-gradient(90deg, rgba(124, 77, 255, 0.5), transparent);
      margin-left: 20px;
    }
  }
}

.compact-hall-layout .module-section {
  margin-bottom: 14px;

  .section-title {
    margin-bottom: 10px;
    font-size: 17px;
  }
}

.modules-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  padding: 0 4px;
}

.compact-hall-layout .modules-list,
.compact-hall-layout .insight-stages-grid {
  gap: 10px;
}

.compact-hall-layout .module-card {
  padding: 16px;
}

.module-card {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 16px;
  padding: 20px;
  position: relative;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(124, 77, 255, 0.1);
  backdrop-filter: blur(10px);
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, 
      rgba(124, 77, 255, 0.05) 0%, 
      rgba(0, 229, 255, 0.05) 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
  transform: translateY(-5px);
    border-color: rgba(124, 77, 255, 0.3);
    box-shadow: 
      0 10px 30px rgba(0, 0, 0, 0.2),
      0 0 20px rgba(124, 77, 255, 0.1),
      0 0 40px rgba(0, 229, 255, 0.1);

    &::before {
      opacity: 1;
    }

    .module-icon {
      transform: scale(1.1) rotate(10deg);
      opacity: 1;
    }
  }

  .module-info {
    position: relative;
    z-index: 1;

    h3 {
  font-size: 18px;
      font-weight: 600;
      color: #ffffff;
      margin: 0 0 12px;
      text-shadow: 0 0 10px rgba(124, 77, 255, 0.3);
    }

    .module-description {
      color: rgba(255, 255, 255, 0.7);
      font-size: 13px;
      line-height: 1.6;
      margin-bottom: 16px;
}

.module-status, .module-score {
      font-size: 13px;
      color: rgba(255, 255, 255, 0.6);
      margin: 6px 0;
      display: flex;
      align-items: center;
      gap: 8px;

      &::before {
        content: "";
        display: inline-block;
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: linear-gradient(135deg, #7c4dff, #00e5ff);
        box-shadow: 0 0 10px rgba(124, 77, 255, 0.5);
      }
    }
}

.module-icon {
    position: absolute;
    right: 20px;
    top: 20px;
  font-size: 24px;
    opacity: 0.7;
    transition: all 0.3s ease;
    filter: drop-shadow(0 0 10px rgba(124, 77, 255, 0.5));
  }
  .module-svg { color: #eaf2ff; }
}

.interview-ai-module {
  background: rgba(255, 255, 255, 0.10);
  color: #ffffff;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(18px) saturate(145%);
  -webkit-backdrop-filter: blur(18px) saturate(145%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.18);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background:
      linear-gradient(135deg, rgba(30, 64, 175, 0.46), rgba(14, 165, 233, 0.36)),
      radial-gradient(circle at 18% 20%, rgba(255, 255, 255, 0.16) 0%, transparent 28%),
      radial-gradient(circle at 82% 82%, rgba(125, 211, 252, 0.22) 0%, transparent 30%);
    z-index: 0;
  }

  &::after {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    right: -50%;
    bottom: -50%;
    background: linear-gradient(45deg,
      rgba(255, 255, 255, 0.08) 0%,
      rgba(147, 197, 253, 0.10) 50%,
      rgba(255, 255, 255, 0.06) 100%);
    transform: rotate(45deg);
    animation: shimmer 6s linear infinite;
    z-index: 1;
  }

  &:hover {
    transform: translateY(-8px) scale(1.02);
    box-shadow: 
      0 20px 40px rgba(0, 0, 0, 0.3),
      0 0 20px rgba(124, 77, 255, 0.2),
      0 0 40px rgba(0, 229, 255, 0.2);

    .ai-glow {
      opacity: 1;
      transform: scale(1.2);
    }

    .ai-badge {
      background: rgba(255, 255, 255, 0.3);
      box-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
    }

    .competency-indicators span {
      animation: pulse 2s infinite;
      &:nth-child(2n) {
        animation-delay: 0.5s;
      }
    }
  }

  .module-info {
  position: relative;
    z-index: 2;

    h3 {
      font-size: 24px;
      font-weight: 600;
      color: #ffffff;
      margin: 0 0 16px;
      text-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .module-description {
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
      line-height: 1.6;
      margin-bottom: 20px;
      text-shadow: 0 0 8px rgba(0, 0, 0, 0.2);
}

.glass-ai-card {
  background: rgba(255, 255, 255, 0.10);
  border-color: rgba(255, 255, 255, 0.18);
}
}

.ai-badge {
    display: inline-flex;
    align-items: center;
    padding: 4px 10px;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 20px;
  font-size: 12px;
    font-weight: 600;
    color: #ffffff;
    margin-bottom: 12px;
    backdrop-filter: blur(4px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    transition: all 0.3s ease;
    box-shadow: 0 0 10px rgba(255, 255, 255, 0.1);

    .badge-svg { margin-right: 6px; }
  }

  .competency-indicators {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 16px;

    span {
      padding: 6px 12px;
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 20px;
      font-size: 12px;
      color: #ffffff;
      backdrop-filter: blur(4px);
      transition: all 0.3s ease;
  position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(
          90deg,
          transparent,
          rgba(255, 255, 255, 0.2),
          transparent
        );
        transition: 0.5s;
      }

      &:hover::before {
        left: 100%;
      }
    }
  }

  .ai-icon {
    position: absolute;
    right: 20px;
    top: 20px;
    font-size: 28px;
    opacity: 0.9;
    filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.3));
    animation: float 3s ease-in-out infinite;
}

.ai-glow {
  position: absolute;
  right: -50px;
    top: -50px;
    width: 120px;
    height: 120px;
    background: radial-gradient(
      circle,
      rgba(124, 77, 255, 0.3) 0%,
      rgba(0, 229, 255, 0.3) 50%,
      transparent 70%
    );
    filter: blur(20px);
    opacity: 0.6;
    transition: all 0.5s ease;
    animation: pulse 3s ease-in-out infinite;
  }
}

.ai-view-entry-card {
  cursor: pointer;
  outline: none;

  &:hover,
  &:focus-visible {
    border-color: rgba(186, 230, 253, 0.9);
    box-shadow:
      0 0 0 3px rgba(186, 230, 253, 0.24),
      0 18px 42px rgba(14, 116, 144, 0.25);
  }

  &:hover .ai-view-tooltip,
  &:focus-visible .ai-view-tooltip {
    gap: 8px;
    width: 132px;
    opacity: 1;
    max-width: 180px;
    transform: translateY(0) scale(1);
  }

  &:hover .island-text,
  &:focus-visible .island-text {
    width: auto;
    max-width: 92px;
    opacity: 1;
  }

  &:hover .island-arrow,
  &:focus-visible .island-arrow {
    width: 18px;
    opacity: 1;
  }

  .module-click-hint {
    color: rgba(255, 255, 255, 0.82);
    font-size: 13px;
    margin: -8px 0 14px;
  }
}

.ai-view-tooltip {
  position: absolute;
  right: 16px;
  bottom: 16px;
  z-index: 3;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  width: 34px;
  height: 34px;
  max-width: 34px;
  padding: 0;
  overflow: hidden;
  border-radius: 999px;
  background:
    linear-gradient(135deg, rgba(15, 23, 42, 0.78), rgba(8, 47, 73, 0.62)),
    radial-gradient(circle at 18% 22%, rgba(125, 211, 252, 0.34), transparent 38%);
  border: 1px solid rgba(186, 230, 253, 0.22);
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  color: #ffffff;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  opacity: 0.82;
  pointer-events: none;
  transform: translateY(4px) scale(0.96);
  transition: width 0.28s ease, max-width 0.28s ease, gap 0.2s ease, opacity 0.2s ease, transform 0.2s ease, border-color 0.2s ease;
  box-shadow:
    0 14px 32px rgba(15, 23, 42, 0.28),
    0 0 22px rgba(56, 189, 248, 0.14);
}

.ai-view-dynamic-island {
  transform-origin: right bottom;
}

.island-orb {
  flex: 0 0 auto;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #67e8f9;
  box-shadow:
    0 0 0 5px rgba(103, 232, 249, 0.12),
    0 0 18px rgba(103, 232, 249, 0.72);
  animation: islandPulse 1.8s ease-in-out infinite;
}

.island-text {
  width: 0;
  max-width: 0;
  overflow: hidden;
  opacity: 0;
  white-space: nowrap;
  transition: max-width 0.24s ease, opacity 0.18s ease;
}

.island-arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: 0;
  height: 18px;
  overflow: hidden;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.14);
  color: #ffffff;
  font-size: 11px;
  opacity: 0;
  transition: width 0.2s ease, opacity 0.18s ease;
}

.insights-module-section {
  margin-top: 24px;
}

.insights-empty {
  color: #0f172a;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(186, 230, 253, 0.7);
  border-radius: 14px;
  padding: 16px;
  font-size: 14px;
}

.insight-stages-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
  padding: 0 4px;
}

.insight-stage-card {
  min-height: 142px;
  padding: 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.24);
  backdrop-filter: blur(18px) saturate(150%);
  -webkit-backdrop-filter: blur(18px) saturate(150%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.22);
  color: #ffffff;
  cursor: pointer;
  transition: transform 0.24s ease, border-color 0.24s ease, box-shadow 0.24s ease;

  p {
    display: -webkit-box;
    margin: 10px 0 0;
    overflow: hidden;
    color: rgba(255, 255, 255, 0.84);
    font-size: 12px;
    line-height: 1.55;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 3;
    line-clamp: 3;
  }

  &:hover,
  &:focus-visible {
    transform: translateY(-4px) scale(1.015);
    border-color: rgba(186, 230, 253, 0.48);
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.24),
      0 18px 34px rgba(15, 23, 42, 0.24);
    outline: none;
  }
}

.glass-insight-card {
  background: rgba(255, 255, 255, 0.18);
}

.insight-detail-overlay {
  position: fixed;
  inset: 0;
  z-index: 1200;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(3, 7, 18, 0.46);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.insight-detail-modal {
  position: relative;
  width: min(520px, 92vw);
  padding: 26px;
  border: 1px solid rgba(186, 230, 253, 0.28);
  border-radius: 24px;
  background:
    linear-gradient(135deg, rgba(15, 23, 42, 0.90), rgba(12, 74, 110, 0.74)),
    radial-gradient(circle at 15% 10%, rgba(125, 211, 252, 0.26), transparent 34%);
  box-shadow:
    0 26px 80px rgba(0, 0, 0, 0.34),
    inset 0 1px 0 rgba(255, 255, 255, 0.16);
  color: #ffffff;
}

.insight-detail-close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 30px;
  height: 30px;
  border: 0;
  border-radius: 0;
  background: transparent;
  color: #ffffff;
  cursor: pointer;
  font-size: 20px;
  line-height: 1;
}

.insight-detail-kicker {
  margin: 0 0 8px;
  color: rgba(186, 230, 253, 0.82);
  font-size: 13px;
  font-weight: 700;
}

.insight-detail-modal h3 {
  margin: 0 0 18px;
  color: #ffffff;
  font-size: 24px;
}

.insight-detail-score {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 18px;

  strong {
    font-size: 44px;
    line-height: 1;
  }

  span {
    color: rgba(255, 255, 255, 0.78);
    font-size: 13px;
  }
}

.insight-detail-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 18px;

  div {
    padding: 12px;
    border-radius: 14px;
    background: rgba(255, 255, 255, 0.10);
  }

  span {
    display: block;
    margin-bottom: 6px;
    color: rgba(255, 255, 255, 0.68);
    font-size: 12px;
  }

  strong {
    color: #ffffff;
    font-size: 16px;
  }
}

.insight-detail-advice {
  margin: 0;
  color: rgba(255, 255, 255, 0.86);
  font-size: 14px;
  line-height: 1.75;
}

.insight-detail-pop-enter-active,
.insight-detail-pop-leave-active {
  transition: opacity 0.24s ease;
}

.insight-detail-pop-enter-active .insight-detail-modal,
.insight-detail-pop-leave-active .insight-detail-modal {
  transition: transform 0.28s cubic-bezier(0.2, 0.8, 0.2, 1), opacity 0.22s ease;
}

.insight-detail-pop-enter-from,
.insight-detail-pop-leave-to {
  opacity: 0;
}

.insight-detail-pop-enter-from .insight-detail-modal,
.insight-detail-pop-leave-to .insight-detail-modal {
  opacity: 0;
  transform: scale(0.88) translateY(18px);
}

.stage-topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 14px;

  span {
    color: #ffffff;
    font-weight: 700;
  }

  em {
    font-style: normal;
    font-size: 11px;
    color: #075985;
    background: #e0f2fe;
    border-radius: 999px;
    padding: 4px 8px;
    white-space: nowrap;
  }
}

.stage-score {
  display: flex;
  align-items: baseline;
  gap: 8px;

  strong {
    color: #ffffff;
    font-size: 28px;
    line-height: 1;
  }

  span {
    color: rgba(255, 255, 255, 0.82);
    font-size: 12px;
  }
}

.stage-meta {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-top: 12px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 12px;
}

.status-at-risk {
  border-color: rgba(251, 146, 60, 0.7);
}

.status-at-risk .stage-topline em {
  color: #9a3412;
  background: #ffedd5;
}

.status-needs-attention .stage-topline em {
  color: #1d4ed8;
  background: #dbeafe;
}

.status-stable .stage-topline em {
  color: #047857;
  background: #d1fae5;
}

@media (max-width: 980px) {
  .insight-stages-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .insight-stages-grid {
    grid-template-columns: 1fr;
  }
}

@keyframes shimmer {
  0% {
    transform: rotate(45deg) translateX(-100%);
  }
  100% {
    transform: rotate(45deg) translateX(100%);
  }
}

@keyframes islandPulse {
  0%,
  100% {
    transform: scale(0.92);
    opacity: 0.72;
  }
  50% {
    transform: scale(1.08);
    opacity: 1;
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
  100% {
    transform: translateY(0px);
  }
}

@keyframes floatBig {
  0% {
    transform: translateY(100vh) rotate(0deg) scale(1);
  }
  50% {
    transform: translateY(50vh) rotate(180deg) scale(1.1);
  }
  100% {
    transform: translateY(-20vh) rotate(360deg) scale(1);
  }
}

@keyframes floatMedium {
  0% {
    transform: translateY(100vh) rotate(0deg) scale(1);
  }
  50% {
    transform: translateY(50vh) rotate(-180deg) scale(0.9);
  }
  100% {
    transform: translateY(-20vh) rotate(-360deg) scale(1);
  }
}

@keyframes floatSmall {
  0% {
    transform: translateY(100vh) rotate(0deg) scale(1);
  }
  33% {
    transform: translateY(66vh) rotate(120deg) scale(1.1);
  }
  66% {
    transform: translateY(33vh) rotate(240deg) scale(0.9);
  }
  100% {
    transform: translateY(-20vh) rotate(360deg) scale(1);
  }
}

@keyframes glowPulse {
  0% {
    filter: drop-shadow(0 0 5px rgba(124, 77, 255, 0.2));
  }
  50% {
    filter: drop-shadow(0 0 10px rgba(0, 229, 255, 0.3));
  }
  100% {
    filter: drop-shadow(0 0 5px rgba(124, 77, 255, 0.2));
  }
}

/* Starfield background effect */
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

.project-description-section {
  text-align: center;
  padding: 32px 20px 20px;
  color: rgba(255, 255, 255, 0.5);
  position: relative;
  z-index: 1;
  max-width: 700px;
  margin: 0 auto;
}

.team-name {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.75);
  margin-bottom: 12px;
  letter-spacing: 1px;
}

.project-info {
  font-size: 13px;
  line-height: 1.8;
}

.eye-care-mode .project-description-section {
  color: rgba(219, 232, 217, 0.6);
}

.eye-care-mode .team-name {
  color: rgba(219, 232, 217, 0.85);
}
</style> 

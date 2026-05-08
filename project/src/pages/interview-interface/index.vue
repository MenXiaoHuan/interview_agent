<template>
  <div class="interview-interface-container" :class="{ 'eye-care': isEyeCareMode }">
    <div id="stars"></div>
    <div id="stars2"></div>
    <div id="stars3"></div>
    <div v-if="isTransitioning" class="page-transition-overlay" :class="{ 'eye-care': isEyeCareMode }"></div>
    
    <div class="content-container">
      <div class="page-header">
        <div class="back-button" @click="handleBack">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1>模拟面试</h1>
      </div>

      <!-- 综合测评模块 -->
      <div class="module-section">
        <h2 class="section-title">综合测评</h2>
        <div class="modules-list">
          <div class="module-card interview-ai-module" @click="goToInterviewAI">
            <div class="module-info">
              <div class="ai-badge"><svg class="badge-svg" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="5" y="9" width="14" height="8" rx="4" stroke="currentColor" stroke-width="2"/><circle cx="10" cy="13" r="1.3" fill="currentColor"/><circle cx="14" cy="13" r="1.3" fill="currentColor"/><path d="M12 8V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg><span>AI</span></div>
              <h3>智能体面试</h3>
              <div class="module-details">
                <p class="module-description">多模态模拟面试全流程</p>
                <div class="competency-indicators">
                  <span>专业知识水平</span>
                  <span>技能匹配度</span>
                  <span>语言表达能力</span>
                  <span>逻辑思维能力</span>
                  <span>创新能力</span>
                  <span>应变抗压能力</span>
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
            <div class="ai-glow"></div>
          </div>
        </div>
      </div>

      <!-- 专项测评模块 -->
      <div class="module-section">
        <h2 class="section-title">专项测评</h2>
        <div class="modules-list">
          <!-- 简历评测模块 -->
          <div class="module-card" @click="goToInterviewResume">
            <div class="module-info">
              <h3>简历评测</h3>
              <div class="module-details">
                <p class="module-description">简历文本与职位匹配度分析</p>
                <p class="module-status">状态: {{ resumeAnalysisStatus }}</p>
                <p class="module-score">得分: {{ resumeScore }}</p>
              </div>
            </div>
            <div class="module-icon">
              <svg class="module-svg" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M7 3h7l5 5v13H7V3z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                <path d="M14 3v6h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </div>

          <!-- 试题作答模块 -->
          <div class="module-card" @click="goToQuestionsAnswering">
            <div class="module-info">
              <h3>试题作答</h3>
              <div class="module-details">
                <p class="module-description">专业知识与技能测试</p>
                <p class="module-status">状态: {{ questionsStatus }}</p>
                <p class="module-score">得分: {{ questionsScore }}</p>
              </div>
            </div>
            <div class="module-icon">
              <svg class="module-svg" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="4" y="4" width="14" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M8 8h6M8 12h6M8 16h4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M18 6l2 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </div>

          <!-- 场景评测模块 -->
          <div class="module-card" @click="goToScenarioAssessment">
            <div class="module-info">
              <h3>场景评测</h3>
              <div class="module-details">
                <p class="module-description">表情发音分析与文本相关性评估</p>
                <p class="module-status">状态: {{ audioAssessmentStatus }}</p>
                <p class="module-score">得分: {{ audioScore }}</p>
              </div>
            </div>
            <div class="module-icon">
              <svg class="module-svg" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/>
                <path d="M4 20c1.8-3.2 5-5 8-5s6.2 1.8 8 5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
          </div>


        </div>
      </div>

      <view class="project-description-section">
        <h2 class="team-name">关于我们 · 梦之队</h2>
        <p class="project-info">
          我们是「梦之队」，致力于打造尖端的"多模态模拟面试智能体"。本项目利用先进AI技术，从简历、试题、场景等多维度综合评估您的面试表现，助您在求职路上脱颖而出。
        </p>
      </view>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router'; // 导入 useRouter 和 useRoute
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
import { DEFAULT_AVATAR } from '@/utils/avatar';

const router = useRouter(); // 初始化 router
const route = useRoute(); // 初始化 route
const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);
const isTransitioning = ref(false);

// 模块完成状态
const isResumeAnalysisCompleted = ref(false);
const isQuestionsCompleted = ref(false);
const isScenarioAssessmentCompleted = ref(false);

// 模块分数
const resumeScoreValue = ref(0);
const questionsScoreValue = ref(0);
const scenarioScoreValue = ref(0);

// 模块状态文本显示
const resumeAnalysisStatus = computed(() => 
  isResumeAnalysisCompleted.value ? '已完成' : '未开始'
);
const questionsStatus = computed(() => 
  isQuestionsCompleted.value ? '已完成' : '未开始'
);
const audioAssessmentStatus = computed(() => 
  isScenarioAssessmentCompleted.value ? '已完成' : '未开始'
);

// 模块分数显示
const resumeScore = computed(() => resumeScoreValue.value);
const questionsScore = computed(() => questionsScoreValue.value);
const audioScore = computed(() => scenarioScoreValue.value);

// 判断是否可以生成报告
const canGenerateReport = computed(() => 
  isResumeAnalysisCompleted.value &&
  isQuestionsCompleted.value &&
  isScenarioAssessmentCompleted.value
);

// 智能体面试状态
const aiInterviewStatus = ref('尚未开始');

// 用户头像状态和相关逻辑
const userAvatar = ref(DEFAULT_AVATAR);

// 添加当前职位ID的状态
const currentJobId = ref(null);

// 处理头像加载错误
const handleAvatarError = (event) => {
  event.target.src = DEFAULT_AVATAR;
};

// 从本地存储获取用户信息并设置头像
const getUserInfoAndAvatar = () => {
  try {
    const userInfo = uni.getStorageSync('userInfo');
    if (userInfo) {
      const parsedInfo = JSON.parse(userInfo);
      // userAvatar.value = parsedInfo.avatar && parsedInfo.avatar !== '' ? parsedInfo.avatar : '/static/PP.png';
    } else {
      // userAvatar.value = '/static/PP.png';
    }
  } catch (error) {
    // userAvatar.value = '/static/PP.png';
    console.error('获取用户信息失败:', error);
  }
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
  } else if (jobIdFromStorage) {
    // 如果URL中没有，但localStorage有，则从localStorage加载
    currentJobId.value = jobIdFromStorage;
    console.log('[Interview Interface] Loaded jobId from storage:', jobIdFromStorage);
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

// 跳转到个人中心
const goToPersonalCenter = () => {
  router.push('/pages/personal-center/index');
};

// 保存状态到本地存储
const saveState = () => {
  const state = {
    resumeAnalysis: {
      completed: isResumeAnalysisCompleted.value,
      score: resumeScoreValue.value
    },
    questions: {
      completed: isQuestionsCompleted.value,
      score: questionsScoreValue.value
    },
    scenarioAssessment: {
      completed: isScenarioAssessmentCompleted.value,
      score: scenarioScoreValue.value
    }
  };
  try {
    uni.setStorageSync('interviewState', JSON.stringify(state));
    console.log('State saved successfully:', state);
  } catch (error) {
    console.error('Error saving state:', error);
  }
};

// 从本地存储加载状态
const loadState = () => {
  try {
    const savedState = uni.getStorageSync('interviewState');
    console.log('[State Loading] Attempting to load state from storage');
    
    if (!savedState) {
      console.log('[State Loading] No saved state found, initializing default state');
      // 初始化默认状态
      const defaultState = {
        resumeAnalysis: { completed: false, score: 0 },
        questions: { completed: false, score: 0 },
        scenarioAssessment: { completed: false, score: 0 }
      };
      uni.setStorageSync('interviewState', JSON.stringify(defaultState));
      return;
    }

    console.log('[State Loading] Found saved state:', savedState);
    const state = JSON.parse(savedState);
    console.log('[State Loading] Parsed state:', state);

    // 确保所有必要的状态字段都存在
    if (!state.resumeAnalysis) state.resumeAnalysis = { completed: false, score: 0 };
    if (!state.questions) state.questions = { completed: false, score: 0 };
    if (!state.scenarioAssessment) state.scenarioAssessment = { completed: false, score: 0 };

    // 更新状态
    isResumeAnalysisCompleted.value = state.resumeAnalysis.completed || false;
    resumeScoreValue.value = state.resumeAnalysis.score || 0;
    isQuestionsCompleted.value = state.questions.completed || false;
    questionsScoreValue.value = state.questions.score || 0;
    isScenarioAssessmentCompleted.value = state.scenarioAssessment.completed || false;
    scenarioScoreValue.value = state.scenarioAssessment.score || 0;

    console.log('[State Loading] Updated component state:', {
      resume: { completed: isResumeAnalysisCompleted.value, score: resumeScoreValue.value },
      questions: { completed: isQuestionsCompleted.value, score: questionsScoreValue.value },
      scenario: { completed: isScenarioAssessmentCompleted.value, score: scenarioScoreValue.value }
    });

  } catch (error) {
    console.error('[State Loading] Error loading state:', error);
    // 发生错误时重置状态
    resetState();
  }
};

// 重置状态
const resetState = () => {
  console.log('[State Reset] Resetting all states to default values');
  isResumeAnalysisCompleted.value = false;
  resumeScoreValue.value = 0;
  isQuestionsCompleted.value = false;
  questionsScoreValue.value = 0;
  isScenarioAssessmentCompleted.value = false;
  scenarioScoreValue.value = 0;

  // 保存重置后的状态
  const defaultState = {
    resumeAnalysis: { completed: false, score: 0 },
    questions: { completed: false, score: 0 },
    scenarioAssessment: { completed: false, score: 0 }
  };
  
  try {
    uni.setStorageSync('interviewState', JSON.stringify(defaultState));
    console.log('[State Reset] Successfully saved default state to storage');
  } catch (error) {
    console.error('[State Reset] Error saving default state:', error);
  }
};

// 更新模块状态和分数
const updateModuleState = (module, completed, score) => {
  switch (module) {
    case 'resume':
      isResumeAnalysisCompleted.value = completed;
      resumeScoreValue.value = score;
      break;
    case 'questions':
      isQuestionsCompleted.value = completed;
      questionsScoreValue.value = score;
      break;
    case 'scenario':
      isScenarioAssessmentCompleted.value = completed;
      scenarioScoreValue.value = score;
      break;
  }
  saveState();
};

// 导航到简历评测页面
const goToInterviewResume = () => {
  if (!currentJobId.value) {
    uni.showToast({
      title: '请先选择职位',
      icon: 'none',
      duration: 2000
    });
    router.replace('/pages/job-selection/index');
    return;
  }

  // 确保在跳转前将 currentJobId 保存到本地存储
  uni.setStorageSync('currentJobId', currentJobId.value);

  isTransitioning.value = true;
  setTimeout(() => {
    uni.navigateTo({
      url: '/pages/interview-resume/index?jobId=' + currentJobId.value,
      success: function(res) {
        getApp().globalData = getApp().globalData || {};
        getApp().globalData.eventChannel = res.eventChannel;

        res.eventChannel.on('moduleComplete', (data) => {
          console.log('Resume analysis completed:', data);
          if (data.module === 'resume' && data.score !== undefined) {
            updateModuleState('resume', true, data.score);
          }
        });
      },
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

// 导航到试题作答页面
const goToQuestionsAnswering = () => {
  const jobId = currentJobId.value; // 从响应式变量获取 jobId
  if (!jobId) {
    uni.showToast({
      title: '岗位信息丢失，请先选择岗位', // 修改提示文本
      icon: 'none',
      duration: 2000
    });
    return; // 没有 jobId，终止跳转
  }

  // 在跳转前将 jobId 存入本地存储 (可选，作为备用)
  try {
    uni.setStorageSync('currentJobId', jobId);
    console.log('[Interview Interface] Job ID stored in localStorage:', jobId);
  } catch (e) {
    console.error('[Interview Interface] Failed to save jobId to localStorage:', e);
  }

  isTransitioning.value = true;
  setTimeout(() => {
    uni.navigateTo({
      url: '/pages/interview-questions/index?jobId=' + jobId,
      success: function(res) {
          console.log('[Interview Interface] Navigated to questions page with jobId:', jobId);
      },
      fail: function(err) {
        console.error('[Interview Interface] Navigation failed to questions page:', err);
         uni.showToast({
          title: '跳转试题作答页面失败',
          icon: 'none'
        });
      }
    });
  }, 180);
};

// 导航到场景评测页面
const goToScenarioAssessment = () => {
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
      url: '/pages/interview-scenario/index?jobId=' + jobId,
      success: function(res) {
      // 存储事件通道到全局数据
      getApp().globalData = getApp().globalData || {};
      getApp().globalData.eventChannel = res.eventChannel;
      
      // 监听模块完成事件
      res.eventChannel.on('moduleComplete', (data) => {
        console.log('Scenario assessment completed:', data);
        if (data.module === 'audio' && data.score !== undefined) {
          updateModuleState('scenario', true, data.score);
        }
      });
      },
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
      url: '/pages/interview-ai/index?jobId=' + jobId + '&autoOpen=0',
      success: function(res) {
      // 存储事件通道到全局数据
      getApp().globalData = getApp().globalData || {};
      getApp().globalData.eventChannel = res.eventChannel;
      
      // 监听模块完成事件
      res.eventChannel.on('moduleComplete', (data) => {
        console.log('AI interview completed:', data);
        if (data.module === 'ai' && data.score !== undefined) {
          updateModuleState('ai', true, data.score);
        }
      });
      },
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
  loadState(); // 加载面试状态
  getUserInfoAndAvatar(); // 获取用户头像
  loadCurrentJobId(); // 加载当前职位ID
  initEyeCareMode();
});

// 定义页面生命周期钩子
const onLoad = () => {
  console.log('[Lifecycle] Page loaded');
  loadState(); // 加载面试状态
  // loadCurrentJobId(); // onLoad可能比onMounted早，也可以在这里调用
};

const onShow = () => {
  console.log('[Lifecycle] Page shown');
  loadState(); // 加载面试状态
  loadCurrentJobId(); // 确保从其他页面返回时也能重新检查jobId
};

// 页面隐藏时保存状态
const onHide = () => {
  console.log('[Lifecycle] Page hidden, saving current state');
  saveState();
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
.interview-interface-container {
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
.interview-interface-container.eye-care .page-transition-overlay {
  background: radial-gradient(circle at 50% 50%, rgba(127,176,105,0.18), rgba(0,0,0,0.40));
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
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

.modules-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  padding: 0 4px;
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
  background: linear-gradient(135deg, #1a237e 0%, #1976d2 100%);
  color: #ffffff;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      radial-gradient(circle at 20% 20%, rgba(124, 77, 255, 0.2) 0%, transparent 25%),
      radial-gradient(circle at 80% 80%, rgba(0, 229, 255, 0.2) 0%, transparent 25%);
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
      rgba(124, 77, 255, 0.1) 0%, 
      rgba(0, 229, 255, 0.1) 50%, 
      rgba(124, 77, 255, 0.1) 100%);
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

@keyframes shimmer {
  0% {
    transform: rotate(45deg) translateX(-100%);
  }
  100% {
    transform: rotate(45deg) translateX(100%);
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

<template>
  <div class="job-selection-cosmic-container" :class="{ 'eye-care-mode': isEyeCareMode }">
    <!-- 星空box-shadow星点层 -->
    <div id="stars"></div>
    <div id="stars2"></div>
    <div id="stars3"></div>
    <!-- 气泡动画 -->
    <div class="bubbles">
      <div v-for="n in 20" :key="n" class="bubble"></div>
    </div>
    <div v-if="isTransitioning" class="page-transition-overlay" :class="{ 'eye-care-mode': isEyeCareMode }"></div>
    <div class="cosmic-content-container">
      <div class="cosmic-header-row">
        <div class="back-button" @click="handleBack" aria-label="返回" role="button">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="cosmic-page-header">
          <h1><span class="cosmic-title-text">岗位选择</span><span class="cosmic-title-star">✦</span></h1>
        </div>
        <div class="cosmic-avatar-wrapper">
          <img :src="userAvatar" alt="用户头像" class="cosmic-user-avatar" @error="handleAvatarError">
        </div>
      </div>
      <div class="cosmic-selection-card">
        <p class="cosmic-guide-text">请选择你感兴趣的岗位类别，开启专属面试体验！</p>
        <!-- 选择区域 -->
        <div class="cosmic-select-group">
          <label>一级分类</label>
          <FancySelect
            v-model="selectedPrimary"
            :options="[{id: '', name: '请选择'}, ...topLevelCategories]"
            placeholder="请选择"
            :disabled="false"
            :eyeCareMode="isEyeCareMode"
            @update:modelValue="handlePrimaryChange"
          />
        </div>
        <div class="cosmic-select-group">
          <label>二级分类</label>
          <FancySelect
            v-model="selectedSecondary"
            :options="[{id: '', name: '请选择'}, ...subCategories]"
            placeholder="请选择"
            :disabled="!selectedPrimary"
            :eyeCareMode="isEyeCareMode"
            @update:modelValue="handleSecondaryChange"
          />
        </div>
        <div class="cosmic-select-group">
          <label>岗位</label>
          <FancySelect
            v-model="selectedJob"
            :options="[{id: '', name: '请选择'}, ...jobs]"
            placeholder="请选择"
            :disabled="!selectedSecondary"
            :eyeCareMode="isEyeCareMode"
          />
        </div>
        <div v-if="isLoading" class="loading-overlay">
          <div class="loading-spinner">加载中...</div>
        </div>
        <div class="cosmic-start-btn-wrap">
          <button @click="handleStartInterview" class="cosmic-start-btn">
            <span>开始面试</span>
            <span class="btn-star">✦</span>
          </button>
        </div>
        <div class="cosmic-explore-hint">
          <span>🪐探索你的专属职业宇宙</span>
      </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useJobStore } from '@/stores/job';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
import FancySelect from '@/components/FancySelect.vue';
import { getUserSession } from '@/utils/user-session';
import { applyAvatarFallback, resolveUserAvatar } from '@/utils/avatar';

const router = useRouter();
const jobStore = useJobStore();
const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);

// 本地状态
const selectedPrimary = ref('');
const selectedSecondary = ref('');
const selectedJob = ref('');
const userAvatar = ref(resolveUserAvatar(getUserSession()));

// 计算属性
const topLevelCategories = computed(() => jobStore.topLevelCategories);

const subCategories = computed(() => {
  if (!selectedPrimary.value) return [];
  return jobStore.getSubCategories(selectedPrimary.value);
});

const jobs = ref([]);

const isLoading = computed(() => jobStore.isLoading);

// 方法
const handlePrimaryChange = () => {
  selectedSecondary.value = '';
  selectedJob.value = '';
  jobStore.setCurrentCategory(null);
  console.log('Selected primary category:', selectedPrimary.value);
  console.log('Available sub-categories:', subCategories.value);
  jobs.value = [];
};

const handleSecondaryChange = async () => {
  if (!selectedSecondary.value) {
    selectedJob.value = '';
    jobs.value = [];
    return;
  }
  
  try {
    // 显示加载状态
    jobStore.loading.jobs = true;
    
    const category = subCategories.value.find(cat => cat.id === selectedSecondary.value);
    if (category) {
      // 直接调用获取岗位列表的方法
      await jobStore.fetchJobsByCategory(category.id);
      jobs.value = [...jobStore.jobs];
      console.log('获取到的岗位列表:', jobs.value);
    } else {
      jobs.value = [];
    }
    
    // 校验选中项
    if (!jobs.value.find(job => job.id === selectedJob.value)) {
      selectedJob.value = '';
    }
  } catch (error) {
    console.error('获取岗位列表失败:', error);
    jobs.value = [];
    selectedJob.value = '';
    uni.showToast({
      title: '获取岗位列表失败',
      icon: 'none',
      duration: 2000
    });
  } finally {
    jobStore.loading.jobs = false;
  }
};

const handleBack = () => {
  isTransitioning.value = true;
  setTimeout(() => {
    uni.reLaunch({ url: '/pages/home/index' });
  }, 180);
};

const handleStartInterview = () => {
  // 验证一级分类
  if (!selectedPrimary.value) {
    uni.showToast({
      title: '请选择一级分类',
      icon: 'none',
      duration: 2000
    });
    return;
  }

  // 验证二级分类
  if (!selectedSecondary.value) {
    uni.showToast({
      title: '请选择二级分类',
      icon: 'none',
      duration: 2000
    });
    return;
  }

  // 验证具体岗位
  if (!selectedJob.value) {
    uni.showToast({
      title: '请选择具体岗位',
      icon: 'none',
      duration: 2000
    });
    return;
  }

  // 初始化所有面试数据
  const defaultSpecializedState = {
    resumeAnalysis: { completed: false, score: 0 },
    questions: { completed: false, score: 0 },
    audioAssessment: { completed: false, score: 0 }
  };

  const defaultComprehensiveState = {
    resume: {
      completed: false,
      inProgress: false,
      score: 0,
      analysisId: null,
      startTime: null,
      endTime: null,
      attempts: 0
    },
    questions: {
      completed: false,
      inProgress: false,
      score: 0,
      interviewId: null,
      startTime: null,
      endTime: null,
      attempts: 0
    },
    audio: {
      completed: false,
      inProgress: false,
      score: 0,
      assessmentId: null,
      startTime: null,
      endTime: null,
      attempts: 0
    },
    overall: {
      startTime: null,
      endTime: null,
      totalScore: 0,
      status: 'not_started'
    }
  };

  try {
    // 清除所有相关的本地存储，确保新面试从头开始
    uni.removeStorageSync('interviewState');
    uni.removeStorageSync('resumeAnalysis');
    uni.removeStorageSync('interviewQuestions');
    uni.removeStorageSync('comprehensiveReport');
    uni.removeStorageSync('comprehensiveTestState');
    uni.removeStorageSync('currentJobId');
    uni.removeStorageSync('currentJobInfo');

    // 设置新的初始状态和当前选中的岗位ID/信息
    uni.setStorageSync('interviewState', JSON.stringify(defaultSpecializedState));
    uni.setStorageSync('comprehensiveTestState', JSON.stringify(defaultComprehensiveState));
    uni.setStorageSync('currentJobId', selectedJob.value);
    
    // 获取选中的岗位完整信息（如果JobStore中可用）
    const jobInfo = jobs.value.find(job => job.id === selectedJob.value);
    if(jobInfo) {
        uni.setStorageSync('currentJobInfo', JSON.stringify(jobInfo));
    }

    console.log('Starting interview with selections:', {
      primaryCategory: selectedPrimary.value,
      secondaryCategory: selectedSecondary.value,
      jobId: selectedJob.value
    });

    // 使用 uni.navigateTo 进行跳转，并携带参数
    isTransitioning.value = true;
    setTimeout(() => {
      uni.navigateTo({
        url: `/pages/interview-interface/index?jobId=${selectedJob.value}`,
        success: () => {
          console.log('Navigation to interview interface successful');
          uni.showToast({
            title: '初始化完成',
            icon: 'success',
            duration: 1000,
          });
        },
        fail: (err) => {
          console.error('Navigation failed:', err);
          uni.showToast({
            title: '跳转失败，请重试',
            icon: 'none'
          });
        }
      });
    }, 180);

  } catch (err) {
    console.error('Error initializing interview state or navigating:', err);
    uni.showToast({
      title: '初始化或跳转失败',
      icon: 'none',
      duration: 2000
    });
  }
};

// 设置头像逻辑全部注释掉，仅显示PP.png
// const getUserAvatar = () => {
//   try {
//     const userInfo = uni.getStorageSync('userInfo');
//     if (userInfo) {
//       const parsedInfo = JSON.parse(userInfo);
//       userAvatar.value = parsedInfo.avatar && parsedInfo.avatar !== '' ? parsedInfo.avatar : '/static/PP.png';
//     } else {
//       userAvatar.value = '/static/PP.png';
//     }
//   } catch (error) {
//     userAvatar.value = '/static/PP.png';
//   }
// };

// 处理头像加载错误
const handleAvatarError = (event) => {
  applyAvatarFallback(event, getUserSession());
};

// 生命周期钩子
onMounted(async () => {
  userAvatar.value = resolveUserAvatar(getUserSession());
  try {
    await jobStore.fetchCategories();
    console.log('Categories loaded:', jobStore.categoryTree);
    
    // 检查URL参数并设置默认选择
    await checkAndSetDefaultSelection();
  } catch (error) {
    console.error('初始化分类数据失败:', error);
    uni.showToast({
      title: '获取分类数据失败',
      icon: 'none',
      duration: 2000
    });
  }
  // getUserAvatar(); // 移除头像加载逻辑
  initEyeCareMode();
});

const isTransitioning = ref(false);

// 页面显示时的处理
const onShow = () => {
  console.log('[Job Selection] Page shown');
  // 每次页面显示时都检查URL参数
  checkAndSetDefaultSelection();
};

// 页面加载时的处理
const onLoad = (options) => {
  console.log('[Job Selection] Page loaded with options:', options);
  // 页面加载时检查参数
  if (options && options.jobId) {
    console.log('JobId found in onLoad options:', options.jobId);
    // 延迟执行，确保分类数据已加载
    setTimeout(() => {
      checkAndSetDefaultSelection();
    }, 100);
  }
};

// 选择职位并开始面试
const selectJob = (job) => {
  try {
    // 保存选中的职位信息
    uni.setStorageSync('currentJobId', job.id);
    uni.setStorageSync('currentJobInfo', JSON.stringify(job));
    
    // 跳转到面试界面
    uni.navigateTo({
      url: `/pages/interview-interface/index?jobId=${job.id}`,
      success: () => {
        console.log('Navigation to interview interface successful');
      },
      fail: (err) => {
        console.error('Navigation failed:', err);
        uni.showToast({
          title: '跳转失败，请重试',
          icon: 'none'
        });
      }
    });
  } catch (error) {
    console.error('Error saving job selection:', error);
    uni.showToast({
      title: '保存职位信息失败',
      icon: 'none'
    });
  }
};

// 检查并设置默认选择
const checkAndSetDefaultSelection = async () => {
  try {
    // 使用uni-app的方式获取URL参数
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];
    let jobIdFromUrl = currentPage.options?.jobId;
    
    console.log('Current page options:', currentPage.options);
    console.log('JobId from URL (uni-app):', jobIdFromUrl);
    
    // 如果uni-app方式没有获取到，尝试其他方式
    if (!jobIdFromUrl) {
      try {
        // 尝试从URL中解析参数
        const url = window.location.href;
        const urlParams = new URLSearchParams(window.location.search);
        jobIdFromUrl = urlParams.get('jobId');
        console.log('JobId from URL (window.location):', jobIdFromUrl);
      } catch (e) {
        console.log('Failed to get jobId from window.location:', e);
      }
    }
    
    if (!jobIdFromUrl) {
      console.log('No jobId in URL parameters, keeping default state');
      return;
    }
    
    console.log('Found jobId in URL:', jobIdFromUrl);
    
    // 根据jobId查找对应的岗位信息
    const jobInfo = await findJobById(jobIdFromUrl);
    if (!jobInfo) {
      console.log('Job not found for jobId:', jobIdFromUrl);
      return;
    }
    
    console.log('Found job info:', jobInfo);
    
    // 根据岗位信息设置默认选择
    await setDefaultSelections(jobInfo);
    
  } catch (error) {
    console.error('Error checking and setting default selection:', error);
  }
};

// 根据jobId查找岗位信息
const findJobById = async (jobId) => {
  try {
    console.log('Searching for job with ID:', jobId, 'Type:', typeof jobId);
    
    // 遍历所有分类查找对应的岗位
    for (const primaryCategory of jobStore.topLevelCategories) {
      console.log('Checking primary category:', primaryCategory.name, 'ID:', primaryCategory.id);
      const subCategories = jobStore.getSubCategories(primaryCategory.id);
      
      for (const subCategory of subCategories) {
        console.log('Checking sub category:', subCategory.name, 'ID:', subCategory.id);
        // 获取该二级分类下的岗位列表
        await jobStore.fetchJobsByCategory(subCategory.id);
        const jobs = jobStore.jobs;
        console.log('Jobs in this category:', jobs.length);
        
        // 尝试多种匹配方式
        const foundJob = jobs.find(job => {
          console.log('Comparing job ID:', job.id, 'with search ID:', jobId);
          return job.id == jobId || job.id === parseInt(jobId) || job.id === jobId.toString();
        });
        
        if (foundJob) {
          console.log('Found job:', foundJob.name, 'ID:', foundJob.id);
          return {
            job: foundJob,
            primaryCategory: primaryCategory,
            subCategory: subCategory
          };
        }
      }
    }
    console.log('Job not found for ID:', jobId);
    return null;
  } catch (error) {
    console.error('Error finding job by ID:', error);
    return null;
  }
};

// 设置默认选择
const setDefaultSelections = async (jobInfo) => {
  try {
    // 设置一级分类
    selectedPrimary.value = jobInfo.primaryCategory.id;
    console.log('Set primary category:', jobInfo.primaryCategory.name);
    
    // 设置二级分类
    selectedSecondary.value = jobInfo.subCategory.id;
    console.log('Set secondary category:', jobInfo.subCategory.name);
    
    // 获取岗位列表并设置岗位选择
    await jobStore.fetchJobsByCategory(jobInfo.subCategory.id);
    jobs.value = [...jobStore.jobs];
    
    // 设置岗位选择
    selectedJob.value = jobInfo.job.id;
    console.log('Set job selection:', jobInfo.job.name);
    
    console.log('Default selections set successfully');
  } catch (error) {
    console.error('Error setting default selections:', error);
  }
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

// 页面隐藏时的处理
const onHide = () => {
  console.log('[Job Selection] Page hidden');
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
.job-selection-cosmic-container {
  min-height: 100vh;
  width: 100vw;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0a1929 0%, #1a1f35 100%);
  transition: background 0.4s;
}
.job-selection-cosmic-container.eye-care-mode {
  background: linear-gradient(135deg, #1c2a1f 0%, #2a3c2e 100%);
}
// 星空box-shadow星点实现
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
  from { transform: translateY(0px); }
  to { transform: translateY(-2560px); }
}
.cosmic-content-container {
  position: relative;
  z-index: 3;
  width: 100%;
  max-width: 420px;
  margin: 16px auto 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}
.cosmic-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
  margin-bottom: 18px;
  padding: 0 16px;
}
.cosmic-avatar-wrapper {
  position: static;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.cosmic-user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 2px solid #7c4dff;
  box-shadow: 0 0 12px 2px #4a6fff55, 0 0 0 4px #fff0;
  object-fit: cover;
  background: #222;
  z-index: 2;
  animation: avatarFloat 4s ease-in-out infinite alternate;
  cursor: default;
}
@keyframes avatarFloat {
  0% { transform: translateY(0); }
  100% { transform: translateY(-8px); }
}
.cosmic-page-header {
  margin: 0;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}
.back-button {
  margin-right: 42px;
  margin-left: 52px;
  position: relative;
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
  z-index: 5;
}
.back-button:hover {
  transform: scale(1.05);
  background: rgba(124, 77, 255, 0.2);
  border-color: rgba(124, 77, 255, 0.3);
  box-shadow: 0 0 20px rgba(124, 77, 255, 0.2), 0 0 40px rgba(0, 229, 255, 0.1);
}
.back-button svg {
  color: #7c4dff;
  filter: drop-shadow(0 0 5px rgba(124, 77, 255, 0.5));
}

/* 护眼模式下返回按钮适配 */
.job-selection-cosmic-container.eye-care-mode .back-button {
  background: rgba(127, 176, 105, 0.12);
  border-color: rgba(127, 176, 105, 0.25);
}
.job-selection-cosmic-container.eye-care-mode .back-button:hover {
  background: rgba(127, 176, 105, 0.18);
  border-color: rgba(127, 176, 105, 0.35);
  box-shadow: 0 0 20px rgba(127, 176, 105, 0.25), 0 0 40px rgba(144, 198, 149, 0.12);
}
.job-selection-cosmic-container.eye-care-mode .back-button svg {
  color: #7fb069;
  filter: drop-shadow(0 0 5px rgba(127, 176, 105, 0.5));
}
.cosmic-title-text {
  font-size: 2.1rem;
  font-weight: 700;
  background: linear-gradient(90deg, #7c4dff 10%, #00e5ff 90%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 18px #7c4dff44, 0 0 8px #00e5ff33;
  letter-spacing: 2px;
}
.cosmic-title-star {
  font-size: 1.5rem;
  color: #7c4dff;
  margin-left: 8px;
  margin-right: 20px;
  vertical-align: middle;
  text-shadow: 0 0 12px #7c4dff99;
}
.cosmic-selection-card {
  width: 100%;
  margin: 0 auto;
  background: none !important;
  border-radius: 32px;
  box-shadow: 0 8px 40px 0 #7c4dff22, 0 1.5px 6px #00e5ff11;
  border: 2px solid rgba(124,77,255,0.18);
  backdrop-filter: blur(16px);
  padding: 48px 32px 32px 32px;
  position: relative;
  overflow: visible;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: box-shadow 0.3s, background 0.3s, border 0.3s;
}
.cosmic-selection-card:hover {
  box-shadow: 0 16px 60px 0 #7c4dff44, 0 2px 12px #00e5ff22;
  border-color: #7c4dff88;
}
.cosmic-guide-text {
  color: #a8c6ff;
  font-size: 1.1rem;
  margin-bottom: 28px;
  font-weight: 500;
  letter-spacing: 1px;
  text-align: center;
}
.cosmic-select-group {
  width: 100%;
  margin-bottom: 22px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  position: relative;
}
.cosmic-select-group label {
  color: #b6cfff;
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 8px;
  letter-spacing: 1px;
}
.cosmic-select-group select {
  width: 100%;
  padding: 14px 40px 14px 18px;
  border: 2px solid #7c4dff;
  border-radius: 20px;
  font-size: 1rem;
  color: #eaf2ff;
  background: rgba(30, 40, 70, 0.92);
  appearance: none;
  box-shadow: 0 4px 24px #7c4dff22, 0 1.5px 6px #00e5ff22;
  outline: none;
  transition: border 0.3s, box-shadow 0.3s, background 0.3s;
  position: relative;
  z-index: 2;
  background-image: url('data:image/svg+xml;utf8,<svg fill="none" stroke="cyan" stroke-width="3" viewBox="0 0 24 24" width="24" height="24" xmlns="http://www.w3.org/2000/svg"><path d="M6 9l6 6 6-6"/></svg>');
  background-repeat: no-repeat;
  background-position: right 18px center;
  background-size: 22px 22px;
  padding-right: 48px;
}
.cosmic-select-group select:focus, .cosmic-select-group select:hover {
  border-color: #00e5ff;
  box-shadow: 0 0 0 4px #00e5ff44, 0 4px 24px #7c4dff44;
  background: rgba(30, 40, 90, 0.98);
}
.cosmic-select-group select:disabled {
  opacity: 0.5;
  background: #222c;
  color: #888;
  border-color: #3a4a6f;
}
.cosmic-select-group option {
  background: #232a4a;
  color: #eaf2ff;
  font-weight: 500;
  border-radius: 12px;
  padding: 10px 18px;
}
.cosmic-select-group select option:checked,
.cosmic-select-group select option:focus,
.cosmic-select-group select option:hover {
  background: linear-gradient(90deg, #7c4dff 0%, #00e5ff 100%);
  color: #fff;
}
.cosmic-start-btn-wrap {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-top: 18px;
}
.cosmic-start-btn {
  padding: 14px 48px;
  font-size: 1.15rem;
  color: #fff;
  background: linear-gradient(90deg, #7c4dff 0%, #00e5ff 100%);
  background-size: 200% 100%;
  background-position: left center;
  border: none;
  border-radius: 16px;
  cursor: pointer;
  font-weight: bold;
  letter-spacing: 2px;
  box-shadow: 0 6px 24px #7c4dff33;
  position: relative;
  overflow: hidden;
  transition: box-shadow 0.4s;
  display: flex;
  align-items: center;
  gap: 8px;
}
.cosmic-start-btn::before {
  content: '';
  position: absolute;
  left: -60%;
  top: 0;
  width: 60%;
  height: 100%;
  background: linear-gradient(120deg, rgba(255,255,255,0.18) 0%, rgba(255,255,255,0.04) 100%);
  filter: blur(2px);
  opacity: 0;
  transition: opacity 0.3s, left 0.5s;
  pointer-events: none;
}
.cosmic-start-btn:hover {
  box-shadow: 0 0 24px 4px #7c4dff88, 0 12px 32px #00e5ff33;
  transform: translateY(-2px);
}
.cosmic-start-btn:hover::before {
  left: 110%;
  opacity: 1;
  transition: opacity 0.3s, left 0.5s;
}
.cosmic-start-btn:active {
  box-shadow: 0 2px 8px #7c4dff55, 0 1px 2px #00e5ff22;
  filter: brightness(0.98);
}
.btn-star {
  color: #00e5ff;
  font-size: 1.3em;
  text-shadow: 0 0 8px #00e5ff99;
}
.cosmic-explore-hint {
  margin-top: 32px;
  text-align: center;
  color: #a8c6ff;
  font-size: 1.08rem;
  letter-spacing: 1px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  opacity: 0.85;
}
.explore-icon {
  font-size: 1.3em;
  filter: drop-shadow(0 0 6px #7c4dff88);
}
.page-transition-overlay { position: fixed; inset: 0; z-index: 9999; pointer-events: none; background: radial-gradient(circle at 50% 50%, rgba(124,77,255,0.18), rgba(0,0,0,0.55)); animation: fadeOutSoft 220ms ease forwards; }
@keyframes fadeOutSoft { from { opacity: 0; } to { opacity: 1; } }
.job-selection-cosmic-container.eye-care-mode .page-transition-overlay { background: radial-gradient(circle at 50% 50%, rgba(127,176,105,0.18), rgba(0,0,0,0.40)); }
.loading-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(20,24,40,0.85);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
  border-radius: 24px;
}
.loading-spinner {
  color: #7c4dff;
  font-size: 1.1rem;
  letter-spacing: 2px;
}
@media (max-width: 600px) {
  .cosmic-header-row {
    flex-direction: column;
    gap: 8px;
  }
  .cosmic-content-container { max-width: 98vw; margin-top: 24px; }
  .cosmic-selection-card { padding: 18px 6px 18px 6px; border-radius: 16px; }
  .cosmic-avatar-wrapper { position: static; }
  .cosmic-user-avatar { width: 40px; height: 40px; }
  .cosmic-page-header { margin-top: 0; }
}
.job-selection-cosmic-container.eye-care-mode .stars1 {
  filter: drop-shadow(0 0 6px #a8c69f88);
  opacity: 0.35;
}
.job-selection-cosmic-container.eye-care-mode .stars2 {
  filter: drop-shadow(0 0 8px #7fb06988);
  opacity: 0.22;
}
.job-selection-cosmic-container.eye-care-mode .stars3 {
  filter: drop-shadow(0 0 10px #a8c69f88);
  opacity: 0.15;
}
.job-selection-cosmic-container.eye-care-mode .cosmic-glow {
  background: radial-gradient(ellipse at center, rgba(127,176,105,0.10) 0%, rgba(44,60,46,0) 80%);
}
.job-selection-cosmic-container.eye-care-mode .cosmic-selection-card {
  background: none !important;
}
.job-selection-cosmic-container.eye-care-mode .cosmic-guide-text,
.job-selection-cosmic-container.eye-care-mode .cosmic-explore-hint,
.job-selection-cosmic-container.eye-care-mode .cosmic-select-group label {
  color: #a8c69f;
}
.job-selection-cosmic-container.eye-care-mode .cosmic-select-group select {
  background: rgba(144, 198, 149, 0.08);
  color: #fff;
  border-color: rgba(127, 176, 105, 0.4);
  box-shadow: 0 4px 24px rgba(127, 176, 105, 0.15), 0 1.5px 6px rgba(144, 198, 149, 0.1);
}

/* 护眼模式下的卡片颜色优化 */
.job-selection-cosmic-container.eye-care-mode .cosmic-selection-card {
  background: rgba(144, 198, 149, 0.08) !important;
  border: 2px solid rgba(127, 176, 105, 0.25);
  box-shadow: 0 8px 40px 0 rgba(127, 176, 105, 0.15), 0 1.5px 6px rgba(144, 198, 149, 0.1);
}

.job-selection-cosmic-container.eye-care-mode .cosmic-selection-card:hover {
  box-shadow: 0 16px 60px 0 rgba(127, 176, 105, 0.25), 0 2px 12px rgba(144, 198, 149, 0.15);
  border-color: rgba(127, 176, 105, 0.4);
}

.job-selection-cosmic-container.eye-care-mode .cosmic-user-avatar {
  border-color: #7fb069;
  box-shadow: 0 0 12px 2px rgba(127, 176, 105, 0.3), 0 0 0 4px transparent;
}

.job-selection-cosmic-container.eye-care-mode .cosmic-title-text {
  background: linear-gradient(90deg, #7fb069 10%, #90c695 90%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 18px rgba(127, 176, 105, 0.3), 0 0 8px rgba(144, 198, 149, 0.2);
}

.job-selection-cosmic-container.eye-care-mode .cosmic-title-star {
  color: #7fb069;
  text-shadow: 0 0 12px rgba(127, 176, 105, 0.6);
}

.job-selection-cosmic-container.eye-care-mode .cosmic-start-btn {
  background: linear-gradient(90deg, #7fb069 0%, #90c695 100%);
  box-shadow: 0 6px 24px rgba(127, 176, 105, 0.2);
}

.job-selection-cosmic-container.eye-care-mode .cosmic-start-btn:hover {
  box-shadow: 0 0 24px 4px rgba(127, 176, 105, 0.4), 0 12px 32px rgba(144, 198, 149, 0.2);
}

.job-selection-cosmic-container.eye-care-mode .cosmic-start-btn:active {
  box-shadow: 0 2px 8px rgba(127, 176, 105, 0.3), 0 1px 2px rgba(144, 198, 149, 0.15);
}

.job-selection-cosmic-container.eye-care-mode .btn-star {
  color: #fff;
  text-shadow: 0 0 8px rgba(255, 255, 255, 0.8), 0 0 12px rgba(144, 198, 149, 0.6);
  opacity: 0.9;
}

.job-selection-cosmic-container.eye-care-mode .cosmic-select-group select:focus,
.job-selection-cosmic-container.eye-care-mode .cosmic-select-group select:hover {
  border-color: #90c695;
  box-shadow: 0 0 0 4px rgba(144, 198, 149, 0.3), 0 4px 24px rgba(127, 176, 105, 0.25);
  background: rgba(44, 60, 46, 0.95);
}

.job-selection-cosmic-container.eye-care-mode .cosmic-select-group select option {
  background: rgba(44, 60, 46, 0.9);
  color: #dbe8d9;
}

.job-selection-cosmic-container.eye-care-mode .cosmic-select-group select option:checked,
.job-selection-cosmic-container.eye-care-mode .cosmic-select-group select option:focus,
.job-selection-cosmic-container.eye-care-mode .cosmic-select-group select option:hover {
  background: linear-gradient(90deg, #7fb069 0%, #90c695 100%);
  color: #fff;
}

.job-selection-cosmic-container.eye-care-mode .loading-spinner {
  color: #7fb069;
}
</style> 

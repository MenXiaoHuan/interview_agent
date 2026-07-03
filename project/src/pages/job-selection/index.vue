<template>
  <div
    class="job-selection-cosmic-container"
    :class="{ 'eye-care-mode': isEyeCareMode }"
  >
    <div
      class="job-selection-back"
      @click="handleBack"
      aria-label="返回"
      role="button"
    >
      <svg
        width="22"
        height="22"
        viewBox="0 0 24 24"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          d="M15 18L9 12L15 6"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
    </div>
    <div id="stars"></div>
    <div id="stars2"></div>
    <div id="stars3"></div>
    <div class="bubbles">
      <div v-for="n in 20" :key="n" class="bubble"></div>
    </div>
    <div
      v-if="isTransitioning"
      class="page-transition-overlay"
      :class="{ 'eye-care-mode': isEyeCareMode }"
    ></div>

    <div class="job-selection-layout">
      <section class="job-selection-showcase">
        <div class="job-selection-showcase-inner">
          <div class="job-selection-brand">
            <div class="job-selection-brand-name">AI Interview Agent</div>
            <div class="job-selection-brand-subtitle">智能求职实战平台</div>
          </div>

          <div class="job-selection-showcase-content">
            <h1>岗位选择与训练启动</h1>
            <p>
              从岗位分类筛选到进入专属面试流程，先锁定目标岗位，再开启后续 AI
              面试、专项题、简历评测与综合报告沉淀。
            </p>

            <div class="job-selection-feature-list">
              <div class="job-selection-feature-item">
                <span class="job-selection-feature-dot"></span>
                <span>逐级筛选岗位分类，快速定位目标职业方向</span>
              </div>
              <div class="job-selection-feature-item">
                <span class="job-selection-feature-dot"></span>
                <span>自动带入岗位上下文，进入对应训练链路</span>
              </div>
              <div class="job-selection-feature-item">
                <span class="job-selection-feature-dot"></span>
                <span>开始后直接衔接面试、评测与结果复盘流程</span>
              </div>
            </div>
          </div>

          <div class="job-selection-showcase-copy">
            © 2026 AI Interview Agent
          </div>
        </div>
      </section>

      <section class="job-selection-panel">
        <div class="job-selection-panel-header">
          <div class="job-selection-panel-eyebrow">训练入口</div>
          <h2 class="job-selection-panel-title">岗位选择</h2>
          <p class="job-selection-panel-subtitle">
            请选择目标岗位类别并进入专属面试体验
          </p>
        </div>

        <div class="job-selection-panel-body">
          <div class="job-selection-form-group">
            <label>一级分类</label>
            <FancySelect
              v-model="selectedPrimary"
              :options="[{ id: '', name: '请选择' }, ...topLevelCategories]"
              placeholder="请选择"
              :disabled="false"
              :eyeCareMode="isEyeCareMode"
              @update:modelValue="handlePrimaryChange"
            />
          </div>

          <div class="job-selection-form-group">
            <label>二级分类</label>
            <FancySelect
              v-model="selectedSecondary"
              :options="[{ id: '', name: '请选择' }, ...subCategories]"
              placeholder="请选择"
              :disabled="!selectedPrimary"
              :eyeCareMode="isEyeCareMode"
              @update:modelValue="handleSecondaryChange"
            />
          </div>

          <div class="job-selection-form-group">
            <label>岗位</label>
            <FancySelect
              v-model="selectedJob"
              :options="[{ id: '', name: '请选择' }, ...jobs]"
              placeholder="请选择"
              :disabled="!selectedSecondary"
              :eyeCareMode="isEyeCareMode"
            />
          </div>

          <div class="loading-inline" :class="{ visible: isLoading }">
            加载中...
          </div>

          <button @click="handleStartInterview" class="job-selection-start-btn">
            <span>开始面试</span>
            <span class="btn-star">✦</span>
          </button>

          <div class="job-selection-panel-hint">
            选择岗位后将自动进入对应训练流程
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useJobStore } from "@/stores/job";
import { useUserStore } from "@/stores/user";
import { storeToRefs } from "pinia";
import FancySelect from "@/components/FancySelect.vue";
import { getUserSession } from "@/utils/user-session";
import { applyAvatarFallback, resolveUserAvatar } from "@/utils/avatar";
import { createNewComprehensiveAssessmentSession } from "@/utils/comprehensive-assessment";

const router = useRouter();
const jobStore = useJobStore();
const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);

// 本地状态
const selectedPrimary = ref("");
const selectedSecondary = ref("");
const selectedJob = ref("");
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
  selectedSecondary.value = "";
  selectedJob.value = "";
  jobStore.setCurrentCategory(null);
  console.log("Selected primary category:", selectedPrimary.value);
  console.log("Available sub-categories:", subCategories.value);
  jobs.value = [];
};

const handleSecondaryChange = async () => {
  if (!selectedSecondary.value) {
    selectedJob.value = "";
    jobs.value = [];
    return;
  }

  try {
    // 显示加载状态
    jobStore.loading.jobs = true;

    const category = subCategories.value.find(
      (cat) => cat.id === selectedSecondary.value,
    );
    if (category) {
      // 直接调用获取岗位列表的方法
      await jobStore.fetchJobsByCategory(category.id);
      jobs.value = [...jobStore.jobs];
      console.log("获取到的岗位列表:", jobs.value);
    } else {
      jobs.value = [];
    }

    // 校验选中项
    if (!jobs.value.find((job) => job.id === selectedJob.value)) {
      selectedJob.value = "";
    }
  } catch (error) {
    console.error("获取岗位列表失败:", error);
    jobs.value = [];
    selectedJob.value = "";
    uni.showToast({
      title: "获取岗位列表失败",
      icon: "none",
      duration: 2000,
    });
  } finally {
    jobStore.loading.jobs = false;
  }
};

const handleBack = () => {
  isTransitioning.value = true;
  setTimeout(() => {
    uni.reLaunch({ url: "/pages/home/index" });
  }, 180);
};

const handleStartInterview = () => {
  // 验证一级分类
  if (!selectedPrimary.value) {
    uni.showToast({
      title: "请选择一级分类",
      icon: "none",
      duration: 2000,
    });
    return;
  }

  // 验证二级分类
  if (!selectedSecondary.value) {
    uni.showToast({
      title: "请选择二级分类",
      icon: "none",
      duration: 2000,
    });
    return;
  }

  // 验证具体岗位
  if (!selectedJob.value) {
    uni.showToast({
      title: "请选择具体岗位",
      icon: "none",
      duration: 2000,
    });
    return;
  }

  // 初始化所有面试数据
  const defaultSpecializedState = {
    resumeAnalysis: { completed: false, score: 0 },
    questions: { completed: false, score: 0 },
    audioAssessment: { completed: false, score: 0 },
  };

  const defaultComprehensiveState = {
    resume: {
      completed: false,
      inProgress: false,
      score: 0,
      analysisId: null,
      startTime: null,
      endTime: null,
      attempts: 0,
    },
    questions: {
      completed: false,
      inProgress: false,
      score: 0,
      interviewId: null,
      startTime: null,
      endTime: null,
      attempts: 0,
    },
    audio: {
      completed: false,
      inProgress: false,
      score: 0,
      assessmentId: null,
      startTime: null,
      endTime: null,
      attempts: 0,
    },
    overall: {
      startTime: null,
      endTime: null,
      totalScore: 0,
      status: "not_started",
    },
  };

  try {
    // 清除所有相关的本地存储，确保新面试从头开始
    uni.removeStorageSync("interviewState");
    uni.removeStorageSync("resumeAnalysis");
    uni.removeStorageSync("interviewQuestions");
    uni.removeStorageSync("comprehensiveReport");
    uni.removeStorageSync("currentJobId");
    uni.removeStorageSync("currentJobInfo");

    // 设置新的初始状态和当前选中的岗位ID/信息
    uni.setStorageSync(
      "interviewState",
      JSON.stringify(defaultSpecializedState),
    );
    uni.setStorageSync("currentJobId", selectedJob.value);
    createNewComprehensiveAssessmentSession(selectedJob.value);

    // 获取选中的岗位完整信息（如果JobStore中可用）
    const jobInfo = jobs.value.find((job) => job.id === selectedJob.value);
    if (jobInfo) {
      uni.setStorageSync("currentJobInfo", JSON.stringify(jobInfo));
    }

    console.log("Starting interview with selections:", {
      primaryCategory: selectedPrimary.value,
      secondaryCategory: selectedSecondary.value,
      jobId: selectedJob.value,
    });

    // 使用 uni.navigateTo 进行跳转，并携带参数
    isTransitioning.value = true;
    setTimeout(() => {
      uni.navigateTo({
        url: `/pages/interview-interface/index?jobId=${selectedJob.value}`,
        success: () => {
          console.log("Navigation to interview interface successful");
          uni.showToast({
            title: "初始化完成",
            icon: "success",
            duration: 1000,
          });
        },
        fail: (err) => {
          console.error("Navigation failed:", err);
          uni.showToast({
            title: "跳转失败，请重试",
            icon: "none",
          });
        },
      });
    }, 180);
  } catch (err) {
    console.error("Error initializing interview state or navigating:", err);
    uni.showToast({
      title: "初始化或跳转失败",
      icon: "none",
      duration: 2000,
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
    console.log("Categories loaded:", jobStore.categoryTree);

    // 检查URL参数并设置默认选择
    await checkAndSetDefaultSelection();
  } catch (error) {
    console.error("初始化分类数据失败:", error);
    uni.showToast({
      title: "获取分类数据失败",
      icon: "none",
      duration: 2000,
    });
  }
  // getUserAvatar(); // 移除头像加载逻辑
  initEyeCareMode();
});

const isTransitioning = ref(false);

// 页面显示时的处理
const onShow = () => {
  console.log("[Job Selection] Page shown");
  // 每次页面显示时都检查URL参数
  checkAndSetDefaultSelection();
};

// 页面加载时的处理
const onLoad = (options) => {
  console.log("[Job Selection] Page loaded with options:", options);
  // 页面加载时检查参数
  if (options && options.jobId) {
    console.log("JobId found in onLoad options:", options.jobId);
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
    uni.setStorageSync("currentJobId", job.id);
    uni.setStorageSync("currentJobInfo", JSON.stringify(job));

    // 跳转到面试界面
    uni.navigateTo({
      url: `/pages/interview-interface/index?jobId=${job.id}`,
      success: () => {
        console.log("Navigation to interview interface successful");
      },
      fail: (err) => {
        console.error("Navigation failed:", err);
        uni.showToast({
          title: "跳转失败，请重试",
          icon: "none",
        });
      },
    });
  } catch (error) {
    console.error("Error saving job selection:", error);
    uni.showToast({
      title: "保存职位信息失败",
      icon: "none",
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

    console.log("Current page options:", currentPage.options);
    console.log("JobId from URL (uni-app):", jobIdFromUrl);

    // 如果uni-app方式没有获取到，尝试其他方式
    if (!jobIdFromUrl) {
      try {
        // 尝试从URL中解析参数
        const url = window.location.href;
        const urlParams = new URLSearchParams(window.location.search);
        jobIdFromUrl = urlParams.get("jobId");
        console.log("JobId from URL (window.location):", jobIdFromUrl);
      } catch (e) {
        console.log("Failed to get jobId from window.location:", e);
      }
    }

    if (!jobIdFromUrl) {
      console.log("No jobId in URL parameters, keeping default state");
      return;
    }

    console.log("Found jobId in URL:", jobIdFromUrl);

    // 根据jobId查找对应的岗位信息
    const jobInfo = await findJobById(jobIdFromUrl);
    if (!jobInfo) {
      console.log("Job not found for jobId:", jobIdFromUrl);
      return;
    }

    console.log("Found job info:", jobInfo);

    // 根据岗位信息设置默认选择
    await setDefaultSelections(jobInfo);
  } catch (error) {
    console.error("Error checking and setting default selection:", error);
  }
};

// 根据jobId查找岗位信息
const findJobById = async (jobId) => {
  try {
    console.log("Searching for job with ID:", jobId, "Type:", typeof jobId);

    // 遍历所有分类查找对应的岗位
    for (const primaryCategory of jobStore.topLevelCategories) {
      console.log(
        "Checking primary category:",
        primaryCategory.name,
        "ID:",
        primaryCategory.id,
      );
      const subCategories = jobStore.getSubCategories(primaryCategory.id);

      for (const subCategory of subCategories) {
        console.log(
          "Checking sub category:",
          subCategory.name,
          "ID:",
          subCategory.id,
        );
        // 获取该二级分类下的岗位列表
        await jobStore.fetchJobsByCategory(subCategory.id);
        const jobs = jobStore.jobs;
        console.log("Jobs in this category:", jobs.length);

        // 尝试多种匹配方式
        const foundJob = jobs.find((job) => {
          console.log("Comparing job ID:", job.id, "with search ID:", jobId);
          return (
            job.id == jobId ||
            job.id === parseInt(jobId) ||
            job.id === jobId.toString()
          );
        });

        if (foundJob) {
          console.log("Found job:", foundJob.name, "ID:", foundJob.id);
          return {
            job: foundJob,
            primaryCategory: primaryCategory,
            subCategory: subCategory,
          };
        }
      }
    }
    console.log("Job not found for ID:", jobId);
    return null;
  } catch (error) {
    console.error("Error finding job by ID:", error);
    return null;
  }
};

// 设置默认选择
const setDefaultSelections = async (jobInfo) => {
  try {
    // 设置一级分类
    selectedPrimary.value = jobInfo.primaryCategory.id;
    console.log("Set primary category:", jobInfo.primaryCategory.name);

    // 设置二级分类
    selectedSecondary.value = jobInfo.subCategory.id;
    console.log("Set secondary category:", jobInfo.subCategory.name);

    // 获取岗位列表并设置岗位选择
    await jobStore.fetchJobsByCategory(jobInfo.subCategory.id);
    jobs.value = [...jobStore.jobs];

    // 设置岗位选择
    selectedJob.value = jobInfo.job.id;
    console.log("Set job selection:", jobInfo.job.name);

    console.log("Default selections set successfully");
  } catch (error) {
    console.error("Error setting default selections:", error);
  }
};

// 初始化护眼模式
const initEyeCareMode = () => {
  try {
    const savedEyeCareMode = localStorage.getItem("eyeCareMode");
    if (savedEyeCareMode === "true") {
      isEyeCareMode.value = true;
    }
  } catch (error) {
    console.error("Error initializing eye care mode:", error);
  }
};

// 页面隐藏时的处理
const onHide = () => {
  console.log("[Job Selection] Page hidden");
};

// 导出页面生命周期钩子
defineExpose({
  onLoad,
  onShow,
  onHide,
});
</script>

<style scoped lang="scss">
@use "sass:math";
@use "sass:string";
.job-selection-cosmic-container {
  min-height: 100vh;
  width: 100vw;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  background:
    radial-gradient(
      circle at top left,
      rgba(0, 229, 255, 0.12),
      transparent 22%
    ),
    radial-gradient(
      circle at bottom right,
      rgba(124, 77, 255, 0.18),
      transparent 24%
    ),
    radial-gradient(1200px 700px at 50% 0%, #12203a, #0a1929);
  transition: background 0.4s;
}
.job-selection-cosmic-container.eye-care-mode {
  background: linear-gradient(135deg, #1c2a1f 0%, #2a3c2e 100%);
}
// 星空box-shadow星点实现
@function multiple-box-shadow($n) {
  $value: "#{math.random(2560)}px #{math.random(2560)}px #FFF";
  @for $i from 2 through $n {
    $value: "#{$value}, #{math.random(2560)}px #{math.random(2560)}px #FFF";
  }
  @return string.unquote($value);
}
$shadows-small: multiple-box-shadow(1000);
$shadows-medium: multiple-box-shadow(300);
$shadows-large: multiple-box-shadow(150);
#stars,
#stars2,
#stars3 {
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
.job-selection-layout {
  position: relative;
  z-index: 3;
  width: 100%;
  min-height: 100vh;
  display: flex;
  overflow: hidden;
}

.job-selection-showcase {
  width: 45%;
  padding: 64px 52px 44px;
  color: #f4f8ff;
  background:
    linear-gradient(160deg, rgba(18, 32, 58, 0.9), rgba(11, 24, 46, 0.86)),
    linear-gradient(135deg, rgba(0, 229, 255, 0.08), rgba(124, 77, 255, 0.12));
  position: relative;
}

.job-selection-showcase::after {
  content: "";
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 22% 18%, rgba(0, 255, 255, 0.1), transparent 18%),
    radial-gradient(
      circle at 78% 82%,
      rgba(124, 77, 255, 0.14),
      transparent 22%
    );
  pointer-events: none;
}

.job-selection-showcase-inner {
  position: relative;
  z-index: 1;
  display: flex;
  min-height: 100%;
  flex-direction: column;
  justify-content: flex-start;
  padding-top: 28px;
}

.job-selection-brand-name {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}

.job-selection-brand-subtitle {
  margin-top: 6px;
  margin-bottom: 54px;
  font-size: 14px;
  opacity: 0.82;
}

.job-selection-showcase-content h1 {
  margin: 0 0 18px;
  font-size: clamp(34px, 4vw, 44px);
  line-height: 1.28;
}

.job-selection-showcase-content p {
  max-width: 460px;
  font-size: 16px;
  line-height: 1.85;
  opacity: 0.92;
  margin-bottom: 38px;
}

.job-selection-feature-list {
  display: grid;
  gap: 16px;
}

.job-selection-feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  line-height: 1.6;
}

.job-selection-feature-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  flex: 0 0 auto;
  background: linear-gradient(135deg, #00e5ff, #7c4dff);
  box-shadow: 0 0 16px rgba(0, 229, 255, 0.32);
}

.job-selection-showcase-copy {
  margin-top: auto;
  padding-top: 40px;
  font-size: 13px;
  opacity: 0.72;
}

.job-selection-panel {
  width: 55%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 84px 40px;
  background: rgba(9, 19, 36, 0.8);
}

.job-selection-panel-header {
  width: min(420px, 100%);
  margin-bottom: 24px;
}

.job-selection-panel-body {
  width: min(420px, 100%);
  padding: 28px 24px 22px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(112, 143, 186, 0.22);
  box-shadow: 0 10px 32px rgba(2, 8, 20, 0.18);
  display: flex;
  flex-direction: column;
}

.job-selection-panel-eyebrow {
  font-size: 13px;
  font-weight: 600;
  color: #00e5ff;
  letter-spacing: 0.04em;
  opacity: 0.9;
}

.job-selection-panel-title {
  margin: 8px 0 8px;
  font-size: 26px;
  color: #f5f9ff;
  line-height: 1.2;
  letter-spacing: 0.01em;
}

.job-selection-panel-subtitle {
  font-size: 12px;
  line-height: 1.7;
  color: #8ea8cf;
}

.job-selection-form-group {
  width: 100%;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
}

.job-selection-form-group label {
  display: block;
  margin-bottom: 10px;
  font-size: 12px;
  color: #9fb4d9;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.loading-inline {
  width: 100%;
  min-height: 18px;
  margin: 0 0 14px;
  color: #a8c6ff;
  font-size: 13px;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.2s ease;
}

.loading-inline.visible {
  opacity: 1;
  visibility: visible;
}

.job-selection-start-btn {
  width: 100%;
  min-height: 48px;
  justify-content: center;
  padding: 0 24px;
  font-size: 16px;
  color: #061324;
  background: linear-gradient(90deg, #00e5ff 0%, #00bfff 100%);
  background-size: 200% 100%;
  background-position: left center;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 700;
  letter-spacing: 0.06em;
  box-shadow: 0 10px 26px rgba(0, 200, 255, 0.22);
  position: relative;
  overflow: hidden;
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease,
    background-position 0.5s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

.job-selection-start-btn::before {
  content: "";
  position: absolute;
  left: -60%;
  top: 0;
  width: 60%;
  height: 100%;
  background: linear-gradient(
    120deg,
    rgba(255, 255, 255, 0.18) 0%,
    rgba(255, 255, 255, 0.04) 100%
  );
  filter: blur(2px);
  opacity: 0;
  transition:
    opacity 0.3s,
    left 0.5s;
  pointer-events: none;
}

.job-selection-start-btn:hover {
  box-shadow: 0 14px 32px rgba(0, 200, 255, 0.28);
  transform: translateY(-2px);
}

.job-selection-start-btn:hover::before {
  left: 110%;
  opacity: 1;
  transition:
    opacity 0.3s,
    left 0.5s;
}

.job-selection-panel-hint {
  width: 100%;
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: #9fb4d9;
  line-height: 1.7;
}

.job-selection-back {
  position: fixed;
  top: 18px;
  left: 18px;
  z-index: 9;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 4px;
}
.job-selection-back svg {
  color: #dfe9ff;
  filter: drop-shadow(0 0 8px rgba(0, 229, 255, 0.22));
  transition:
    transform 0.2s ease,
    opacity 0.2s ease;
}
.job-selection-back:hover svg {
  transform: translateX(-2px);
  opacity: 0.9;
}
.page-transition-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  pointer-events: none;
  background: radial-gradient(
    circle at 50% 50%,
    rgba(124, 77, 255, 0.18),
    rgba(0, 0, 0, 0.55)
  );
  animation: fadeOutSoft 220ms ease forwards;
}
@keyframes fadeOutSoft {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
.job-selection-cosmic-container.eye-care-mode .page-transition-overlay {
  background: radial-gradient(
    circle at 50% 50%,
    rgba(127, 176, 105, 0.18),
    rgba(0, 0, 0, 0.4)
  );
}
.job-selection-panel :deep(.fancy-select) {
  width: 100%;
}
.job-selection-panel :deep(.select-wrap) {
  min-height: 48px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(112, 143, 186, 0.32);
  box-shadow: none;
  padding: 0 14px;
}
.job-selection-panel :deep(.select-wrap.open),
.job-selection-panel :deep(.select-wrap:hover) {
  border-color: #00e5ff;
  box-shadow: 0 0 0 3px rgba(0, 229, 255, 0.1);
  background: rgba(255, 255, 255, 0.06);
}
.job-selection-panel :deep(.select-display) {
  min-height: 48px;
  padding: 0;
  color: #f5f9ff;
}
.job-selection-panel :deep(.label) {
  color: #f5f9ff;
}
.job-selection-panel :deep(.placeholder) {
  color: #7f95bb;
}
.job-selection-panel :deep(.arrow) {
  color: #00e5ff;
}
.job-selection-panel :deep(.options) {
  background: rgba(11, 24, 46, 0.98);
  border: 1px solid rgba(112, 143, 186, 0.32);
  border-radius: 14px;
}
.job-selection-panel :deep(.opt) {
  color: #d7e7ff;
}
.job-selection-panel :deep(.opt.active) {
  background: rgba(0, 229, 255, 0.12);
  color: #00e5ff;
}

.job-selection-cosmic-container.eye-care-mode .job-selection-showcase {
  background:
    linear-gradient(160deg, rgba(33, 48, 35, 0.94), rgba(23, 37, 26, 0.9)),
    linear-gradient(135deg, rgba(127, 176, 105, 0.1), rgba(144, 198, 149, 0.08));
}
.job-selection-cosmic-container.eye-care-mode .job-selection-panel {
  background: rgba(27, 42, 31, 0.88);
}
.job-selection-cosmic-container.eye-care-mode .job-selection-panel-body {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(127, 176, 105, 0.24);
  box-shadow: 0 10px 28px rgba(18, 32, 20, 0.18);
}
.job-selection-cosmic-container.eye-care-mode .job-selection-back svg {
  color: #dbe8d9;
  filter: drop-shadow(0 0 8px rgba(127, 176, 105, 0.3));
}
.job-selection-cosmic-container.eye-care-mode .job-selection-panel-eyebrow {
  color: #90c695;
}
.job-selection-cosmic-container.eye-care-mode .job-selection-panel-subtitle,
.job-selection-cosmic-container.eye-care-mode .job-selection-panel-hint {
  color: #a8c69f;
}
.job-selection-cosmic-container.eye-care-mode .job-selection-form-group label {
  color: #dbe8d9;
}
.job-selection-cosmic-container.eye-care-mode .job-selection-start-btn {
  background: linear-gradient(90deg, #7fb069 0%, #90c695 100%);
  box-shadow: 0 6px 24px rgba(127, 176, 105, 0.2);
}
.job-selection-cosmic-container.eye-care-mode .job-selection-start-btn:hover {
  box-shadow:
    0 0 24px 4px rgba(127, 176, 105, 0.4),
    0 12px 32px rgba(144, 198, 149, 0.2);
}
.job-selection-cosmic-container.eye-care-mode .btn-star {
  color: #fff;
  text-shadow:
    0 0 8px rgba(255, 255, 255, 0.8),
    0 0 12px rgba(144, 198, 149, 0.6);
  opacity: 0.9;
}
.job-selection-cosmic-container.eye-care-mode
  .job-selection-panel
  :deep(.select-wrap) {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(127, 176, 105, 0.35);
}
.job-selection-cosmic-container.eye-care-mode
  .job-selection-panel
  :deep(.select-wrap.open),
.job-selection-cosmic-container.eye-care-mode
  .job-selection-panel
  :deep(.select-wrap:hover) {
  border-color: #90c695;
  box-shadow: 0 0 0 4px rgba(144, 198, 149, 0.2);
  background: rgba(255, 255, 255, 0.1);
}
.job-selection-cosmic-container.eye-care-mode
  .job-selection-panel
  :deep(.label) {
  color: #fffbea;
}
.job-selection-cosmic-container.eye-care-mode
  .job-selection-panel
  :deep(.placeholder) {
  color: #c5d8be;
}
.job-selection-cosmic-container.eye-care-mode
  .job-selection-panel
  :deep(.arrow) {
  color: #90c695;
}
.job-selection-cosmic-container.eye-care-mode
  .job-selection-panel
  :deep(.options) {
  background: rgba(33, 48, 35, 0.98);
  border-color: rgba(127, 176, 105, 0.35);
}
.job-selection-cosmic-container.eye-care-mode .job-selection-panel :deep(.opt) {
  color: #dbe8d9;
}
.job-selection-cosmic-container.eye-care-mode
  .job-selection-panel
  :deep(.opt.active) {
  background: rgba(127, 176, 105, 0.16);
  color: #fffbea;
}

@media (max-width: 960px) {
  .job-selection-layout {
    min-height: auto;
    flex-direction: column;
  }

  .job-selection-showcase,
  .job-selection-panel {
    width: 100%;
  }

  .job-selection-showcase {
    padding: 36px 28px 28px;
  }

  .job-selection-brand-subtitle {
    margin-bottom: 36px;
  }

  .job-selection-showcase-content p {
    max-width: none;
  }

  .job-selection-panel {
    padding: 40px 32px 42px;
  }

  .job-selection-panel-body {
    padding: 24px 20px 20px;
  }
}

@media (max-width: 640px) {
  .job-selection-showcase {
    padding: 28px 20px 24px;
  }

  .job-selection-panel {
    padding: 32px 20px 36px;
  }

  .job-selection-panel-title {
    font-size: 26px;
  }

  .job-selection-panel-body {
    padding: 20px 16px 18px;
    border-radius: 18px;
  }
}
</style>

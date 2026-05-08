<template>
  <div class="interview-questions-container" :class="{ 'eye-care': isEyeCareMode }">
    <!-- 星空背景效果 -->
    <view id="stars" :style="{ boxShadow: starShadow1 }"></view>
    <view id="stars2" :style="{ boxShadow: starShadow2 }"></view>
    <view id="stars3" :style="{ boxShadow: starShadow3 }"></view>
    
    <!-- 返回按钮 -->
    <div class="back-button" @click="goBack">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </div>
    
    <div class="content-container">
      <div class="page-header">
        <h1>试题作答</h1>
        <div class="job-info">
          <span class="job-name">{{ jobInfo.name }}</span>
          <span class="job-description">{{ jobInfo.description }}</span>
        </div>
      </div>

      <div v-if="loading" class="loading-overlay">
        <div class="loading-spinner">
          <div class="spinner"></div>
          <div class="loading-text">加载题目中...</div>
        </div>
      </div>

      <div v-else-if="error" class="error-message">
        <div class="error-icon">⚠️</div>
        <div class="error-text">{{ error }}</div>
        <button @click="retryLoading" class="retry-button">
          <span class="retry-icon">↻</span>
          重试
        </button>
    </div>

      <div v-else>
        <div v-if="showResultView" class="result-content">
          <div class="result-header">
            <h1 class="answer-review-title big">作答结果</h1>
            <div class="job-info">
              <span class="job-name result-job-info">{{ jobInfo.name }}</span>
              <span class="job-description">{{ jobInfo.description }}</span>
            </div>
          </div>
          <div class="score-section">
            <div class="score-summary-vertical">
              <div class="score-main">
                <span class="score-label">总分</span>
                <span class="score-value">{{ result.totalScore }}</span>
              </div>
            </div>
          </div>
          <div class="divider"></div>
          <div class="answer-review-title">答题回顾</div>
          <div class="questions-review">
            <template v-if="result.questions && result.questions.length > 0">
              <div 
                v-for="question in result.questions" 
                :key="question.questionId" 
                class="question-item"
                :class="{ 'wrong-question-item': !question.isCorrect }"
              >
                <div class="question-header">
                  <span class="question-number">第{{ question.originalOrder }}题</span>
                  <div class="question-content">{{ question.question }}</div>
                </div>
                <div class="options-list">
                  <div
                    v-for="(option, optIndex) in question.options"
                    :key="optIndex"
                    class="option-item"
                    :class="{
                      'selected': !question.isCorrect && question.userAnswer.trim() === option.trim(),
                      'correct': option.trim() === question.correctAnswer.trim(),
                      'wrong': !question.isCorrect && question.userAnswer.trim() === option.trim()
                    }"
                  >
                    <span class="option-label">{{ String.fromCharCode(65 + optIndex) }}.</span>
                    <span class="option-content">{{ option }}</span>
                    <span v-if="question.userAnswer.trim() === option.trim()" class="user-answer-marker">
                      {{ question.isCorrect ? '(您的选择 & 正确答案)' : '(您的选择)' }}
                    </span>
                    <span v-else-if="option.trim() === question.correctAnswer.trim()" class="correct-answer-marker">(正确答案)</span>
                  </div>
                </div>
                <div v-if="!question.isCorrect" class="explanation">
                  <div class="explanation-title">答案解析：</div>
                  <div class="explanation-content">{{ question.explanation || '暂无解析' }}</div>
                </div>
              </div>
            </template>
            <div v-else class="no-questions">暂无答题记录</div>
          </div>
          <div class="button-group">
            <button @click="continueInterview" class="home-button">继续面试</button>
          </div>
        </div>

        <div v-else class="questions-container">
        <div class="progress-bar">
          <div class="progress-text">题目进度: {{ currentIndex + 1 }}/10</div>
          <div class="progress-track">
            <div 
              class="progress-fill" 
              :style="{ width: `${(currentIndex + 1) * 10}%` }"
            ></div>
      </div>
    </div>

        <transition name="slide-fade" mode="out-in">
          <div :key="currentIndex" class="question-card">
            <div class="question-content">
              {{ currentQuestion.question }}
      </div>

          <div class="options-list">
              <div
                v-for="(option, index) in currentQuestion.options"
                :key="index"
                class="option-item"
                   :class="{
                  'selected': userAnswers[currentIndex] === option,
                  'correct': showResult && option === currentQuestion.correctAnswer,
                  'wrong': showResult && userAnswers[currentIndex] === option && option !== currentQuestion.correctAnswer
                }"
                @click="selectAnswer(option)"
              >
                {{ option }}
              </div>
            </div>

            <div v-if="showResult" class="explanation">
              <div class="explanation-title">答案解析：</div>
              <div class="explanation-content">{{ currentQuestion.explanation }}</div>
            </div>
          </div>
        </transition>

        <div class="button-group">
          <button 
            v-if="currentIndex > 0" 
            @click="prevQuestion"
            class="nav-button prev-button"
          >
            上一题
          </button>

          <button 
            v-if="currentIndex < 9 && !showResult" 
            @click="nextQuestion"
            class="nav-button next-button"
          >
            下一题
          </button>

          <button 
            v-if="currentIndex === 9 && !showResult" 
            @click="submitInterview"
            class="submit-button"
            :disabled="isSubmitting"
          >
            {{ isSubmitting ? '提交中...' : '提交' }}
          </button>
        </div>
      </div>
    </div>
  </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getInterviewQuestions, submitInterviewAnswers, getJobInfo, getInterviewResult } from '@/utils/api';
import { onLoad } from '@dcloudio/uni-app';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);

// 新增一个响应式变量来存储从 onLoad 获取的 jobId
const currentJobId = ref(null);
const fromAiInterview = ref(false);
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

// 状态管理
const loading = ref(true);
const error = ref('');
const questions = ref([]);
const currentIndex = ref(0);
const userAnswers = ref(new Array(10).fill(''));
const showResult = ref(false);
const isSubmitting = ref(false);
const jobInfo = ref({
  name: '',
  description: ''
});

// 计算属性
const currentQuestion = computed(() => questions.value[currentIndex.value] || {});

// 动态生成星空box-shadow（提前定义，确保模板可用）
function genStarBoxShadow(count, maxW, maxH) {
  let arr = [];
  for (let i = 0; i < count; i++) {
    const x = Math.floor(Math.random() * maxW);
    const y = Math.floor(Math.random() * maxH);
    arr.push(`${x}px ${y}px #fff`);
  }
  return arr.join(', ');
}

const starShadow1 = ref('');
const starShadow2 = ref('');
const starShadow3 = ref('');

onMounted(() => {
  // 增加星点数量
  starShadow1.value = genStarBoxShadow(1000, 2000, 4000);
  starShadow2.value = genStarBoxShadow(400, 2000, 4000);
  starShadow3.value = genStarBoxShadow(200, 2000, 4000);
});

// 获取岗位信息
const fetchJobInfo = async () => {
  try {
    const jobId = currentJobId.value;
    if (!jobId) {
      throw new Error('岗位ID缺失');
    }

    const response = await getJobInfo(jobId);
    if (response.code === 200 && response.data) {
      jobInfo.value = {
        name: response.data.name || '',
        description: response.data.description || '',
        requirements: response.data.requirements || ''
      };
    } else {
      throw new Error(response.message || '获取岗位信息失败');
    }
  } catch (err) {
    console.error('获取岗位信息失败:', err);
    error.value = err.message || '获取岗位信息失败，请重试';
    uni.showToast({
      title: error.value,
      icon: 'none',
      duration: 2000
    });
  }
};

// 获取面试题目
const fetchQuestions = async () => {
  loading.value = true;
  error.value = '';
  try {
    const jobId = currentJobId.value;
    if (!jobId) {
      throw new Error('岗位ID缺失');
    }

    const response = await getInterviewQuestions(jobId);
    if (response.code === 200 && response.data) {
      // 遍历题目列表，处理选项和答案
      questions.value = response.data.map(question => {
        try {
          // 处理选项，确保是数组格式
          let options = [];
          if (typeof question.options === 'string') {
            try {
              options = JSON.parse(question.options);
            } catch (e) {
              console.error('Failed to parse options JSON:', e);
              options = [];
            }
          } else if (Array.isArray(question.options)) {
            options = question.options;
          }
          
          // 随机排序选项
          options = options.sort(() => Math.random() - 0.5);
          
          return {
            ...question,
            options,
            correctAnswer: question.correctAnswer || '',
            score: question.score || 10 // 默认分值10分
          };
  } catch (e) {
          console.error('Failed to process question:', question.id, e);
          return {
            ...question,
            options: [],
            correctAnswer: '',
            score: 10
          };
        }
      });

      // 初始化用户答案数组
      userAnswers.value = new Array(questions.value.length).fill('');
      console.log('Questions loaded:', questions.value);
    } else {
      throw new Error(response.message || '获取题目失败');
    }
  } catch (err) {
    console.error('获取题目失败:', err);
    error.value = err.message || '获取题目失败，请重试';
    uni.showToast({
      title: error.value,
      icon: 'none',
      duration: 2000
    });
  } finally {
    loading.value = false;
  }
};

// 重试加载
const retryLoading = () => {
  fetchQuestions();
};

// 选择答案
const selectAnswer = (option) => {
  if (showResult.value) return;
  userAnswers.value[currentIndex.value] = option;
};

// 上一题
const prevQuestion = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
    showResult.value = false;
        }
};

// 下一题
const nextQuestion = () => {
  if (currentIndex.value < 9) {
    currentIndex.value++;
    showResult.value = false;
    }
};

// 提交面试
const submitInterview = async () => {
  if (isSubmitting.value) return;
  
  // 检查是否所有题目都已作答
  const unansweredQuestions = userAnswers.value.filter(answer => !answer).length;
  
  if (unansweredQuestions > 0) {
    uni.showModal({
      title: '确认提交',
      content: `您还有 ${unansweredQuestions} 道题目未作答，确定要提交吗？`,
      confirmText: '确定提交',
      cancelText: '继续作答',
      success: (res) => {
        if (res.confirm) {
          doSubmitAnswers();
        }
      }
    });
  } else {
    doSubmitAnswers();
  }
};

// 执行提交答案
const doSubmitAnswers = async () => {
  isSubmitting.value = true;
  try {
    const jobId = currentJobId.value;
    if (!jobId) {
      throw new Error('岗位ID缺失');
    }

    // 构建符合API要求的提交数据
    const submitData = {
      jobId,
      answers: questions.value.map((question, index) => ({
        questionId: question.id,
        answer: userAnswers.value[index] || ''
      }))
    };

    const response = await submitInterviewAnswers(submitData);
        
    if (response.code === 200 && response.data) {
      uni.showToast({
        title: '提交成功',
        icon: 'success',
        duration: 1500,
        success: () => {
          setTimeout(async () => {
            await fetchResultInline(response.data.interviewId, jobId);
            showResultView.value = true;
          }, 1200);
        }
      });
    } else {
      throw new Error(response.message || '提交答案失败');
    }
  } catch (err) {
    console.error('提交答案失败:', err);
    error.value = err.message || '提交答案失败，请重试';
    uni.showToast({
      title: error.value,
      icon: 'none',
      duration: 2000
    });
  } finally {
    isSubmitting.value = false;
  }
};

// 获取结果数据并在当前页展示
const result = ref({ totalScore: 0, correctCount: 0, correctRate: 0, questions: [] });
const showResultView = ref(false);
const fetchResultInline = async (interviewId, jobId) => {
  try {
    const res = await getInterviewResult(interviewId);
    if (res.code === 200 && res.data) {
      const data = res.data;
      const qs = Array.isArray(data.questions) ? data.questions : [];
      result.value = {
        totalScore: data.totalScore || 0,
        correctCount: data.correctCount || 0,
        correctRate: parseFloat(data.correctRate) || 0,
        questions: qs.map((q, idx) => ({
          ...q,
          originalOrder: idx + 1,
          questionId: q.id || q.questionId || idx,
          question: q.question || '',
          options: Array.isArray(q.options) ? q.options : [],
          correctAnswer: q.correctAnswer || '',
          userAnswer: q.userAnswer || '',
          isCorrect: !!q.isCorrect,
          score: q.score || 0,
          explanation: q.explanation || ''
        }))
      };
      // 写入状态供历史逻辑使用
      try {
        const interviewStateString = uni.getStorageSync('interviewState');
        let state = interviewStateString ? JSON.parse(interviewStateString) : {};
        state.questions = state.questions || {};
        state.questions.completed = true;
        state.questions.interviewId = interviewId;
        state.questions.score = result.value.totalScore;
        uni.setStorageSync('interviewState', JSON.stringify(state));
      } catch {}
    } else {
      throw new Error(res.message || '获取作答结果失败');
    }
  } catch (e) {
    uni.showToast({ title: e.message || '获取作答结果失败', icon: 'none' });
  }
};

const continueInterview = () => {
  if (fromAiInterview.value && currentJobId.value) {
    try {
      localStorage.setItem('questionsJustCompleted', 'true');
      localStorage.setItem('questionsCompletedTimestamp', Date.now().toString());
      localStorage.setItem('questionsCompletedJobId', String(currentJobId.value || ''));
    } catch (e) {}
    uni.redirectTo({
      url: buildInterviewAiReturnUrl({
        completedType: 'questions',
        mode: 'SPECIAL',
        score: result.value?.totalScore || 0,
        timestamp: Date.now()
      })
    });
    return;
  }
  if (currentJobId.value) {
    uni.redirectTo({ url: `/pages/interview-interface/index?jobId=${currentJobId.value}` });
  } else {
    router.replace('/pages/interview-interface/index');
  }
};

// uni-app 页面生命周期钩子 - onLoad 是页面加载时触发
// 接收页面跳转带来的参数 options
onLoad((options) => {
  console.log('[onLoad] Page loaded with options:', options);
  fromAiInterview.value = options?.from === 'ai-interview';

  let jobId = options.jobId; // 优先从 options 中获取 jobId
  
  // 如果 options 中没有，尝试从本地存储获取
  if (!jobId) {
    try {
      jobId = uni.getStorageSync('currentJobId');
      console.log('[onLoad] Job ID obtained from localStorage:', jobId);
    } catch (e) {
      console.error('Failed to read jobId from localStorage:', e);
    }
  }
  
  if (jobId) {
    currentJobId.value = jobId;
    console.log('[onLoad] Final Job ID obtained:', jobId);
    // 每次进入或刷新都重新拉取题目，不使用历史结果
    showResultView.value = false;
    showResult.value = false;
    currentIndex.value = 0;
    questions.value = [];
    userAnswers.value = new Array(10).fill('');

    // 成功获取 jobId 后，清除本地存储中的值，避免下次干扰
    // 如果上面成功跳转，这一步不会执行
    try {
      uni.removeStorageSync('currentJobId');
      console.log('Job ID removed from localStorage.');
    } catch (e) {
      console.error('Failed to remove jobId from localStorage:', e);
    }
    // 始终拉取最新题目
    console.log('[onLoad] Proceeding to fetch job info and questions (always refresh).');
    fetchJobInfo();
    fetchQuestions();

  } else {
    console.error('[onLoad] Job ID is missing from options and localStorage.');
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
      router.replace('/pages/job-selection/index');
    }, 2000);
  }
});

// 生命周期钩子 - onMounted 在组件挂载后触发
// 负责调用 API 获取数据和加载保存的状态
onMounted(async () => {
  // onMounted 中的逻辑已经移到 onLoad 中处理 jobId 存在的情况
  // 此处可以清空或保留一些不需要在 onLoad 中立即执行的初始化逻辑
  console.log('[onMounted] Component mounted.');
});

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
</script>

<style scoped>
.interview-questions-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a1929 0%, #1a1f35 100%);
  padding: 80px 20px 20px 20px;
  position: relative;
  overflow: visible;
}
html, body {
  min-height: 100vh;
  overflow-y: auto;
  overflow-x: hidden;
}

.content-container {
  max-width: 800px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  padding: 20px;
  position: relative;
  z-index: 1;
  animation: fade-in 0.7s;
  border: 1px solid rgba(124, 77, 255, 0.1);
  backdrop-filter: blur(10px);
}

/* 定义动画 */
@keyframes fade-in {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  color: #ffffff;
  font-size: 24px;
  margin-bottom: 10px;
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

.job-info {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.job-name {
  font-weight: bold;
  margin-right: 10px;
  color: rgba(255, 255, 255, 0.9);
}

.progress-bar {
  margin-bottom: 20px;
}

.progress-text {
  text-align: center;
  margin-bottom: 10px;
  color: rgba(255, 255, 255, 0.8);
}

.progress-track {
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #7c4dff 0%, #00e5ff 100%);
  transition: width 0.5s ease-in-out;
}

.question-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(124, 77, 255, 0.1);
  backdrop-filter: blur(5px);
}

.question-content {
  font-size: 16px;
  color: #ffffff;
  margin-bottom: 20px;
  line-height: 1.6;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  padding: 14px 20px;
  border: 1px solid rgba(124, 77, 255, 0.2);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  transform-origin: center;
  background-color: rgba(255, 255, 255, 0.03);
  color: #ffffff;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(5px);
}

.option-item:hover:not(.selected):not(.correct):not(.wrong) {
  border-color: #7c4dff;
  box-shadow: 0 4px 12px rgba(124, 77, 255, 0.2);
  transform: translateY(-3px);
  background-color: rgba(124, 77, 255, 0.1);
}

.option-item.selected {
  border-color: #7c4dff;
  background-color: rgba(124, 77, 255, 0.2);
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(124, 77, 255, 0.3);
  transform: scale(1.01);
}

.option-item.correct {
  border-color: #52c41a;
  background-color: rgba(82, 196, 26, 0.2);
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);
}

.option-item.wrong {
  border-color: #ff4d4f;
  background-color: rgba(255, 77, 79, 0.2);
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.3);
}

.option-item.selected.wrong {
  border-color: #ff4d4f;
  background-color: rgba(255, 77, 79, 0.2);
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.3);
}

.explanation {
  margin-top: 20px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 6px;
  border: 1px solid rgba(124, 77, 255, 0.1);
}

.explanation-title {
  font-weight: bold;
  margin-bottom: 10px;
  color: #ffffff;
}

.explanation-content {
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
}

.button-group {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  margin-top: 30px;
}

.nav-button, .submit-button {
  padding: 12px 30px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
  position: relative;
  overflow: hidden;
}

.nav-button::before, .submit-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.nav-button:hover::before, .submit-button:hover::before {
  opacity: 1;
}

.prev-button {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  border: 1px solid rgba(124, 77, 255, 0.3);
  box-shadow: 0 2px 4px rgba(124, 77, 255, 0.1);
  backdrop-filter: blur(5px);
}

.prev-button:hover {
  background: rgba(124, 77, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(124, 77, 255, 0.2);
  border-color: rgba(124, 77, 255, 0.4);
}

.prev-button:active {
  background: rgba(124, 77, 255, 0.3);
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(124, 77, 255, 0.1);
  border-color: rgba(124, 77, 255, 0.5);
}

.next-button {
  background: linear-gradient(135deg, #7c4dff 0%, #00e5ff 100%);
  color: white;
  box-shadow: 0 2px 4px rgba(124, 77, 255, 0.2);
}

.next-button:hover {
  background: linear-gradient(135deg, #6a3de8 0%, #00d4e6 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(124, 77, 255, 0.3);
}

.next-button:active {
  background: linear-gradient(135deg, #5a2dd9 0%, #00c3d3 100%);
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(124, 77, 255, 0.2);
}

.submit-button {
  background: linear-gradient(135deg, #10B981 0%, #34D399 100%);
  color: white;
  min-width: 120px;
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.2);
}

.submit-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #059669 0%, #10B981 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.submit-button:active:not(:disabled) {
  background: linear-gradient(135deg, #047857 0%, #059669 100%);
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.2);
}

.submit-button:disabled {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.5);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  opacity: 0.8;
}

/* 添加按钮点击波纹效果 */
.nav-button::after, .submit-button::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 5px;
  height: 5px;
  background: rgba(255, 255, 255, 0.5);
  opacity: 0;
  border-radius: 100%;
  transform: scale(1, 1) translate(-50%, -50%);
  transform-origin: 50% 50%;
}

.nav-button:active::after, .submit-button:active::after {
  animation: ripple 0.6s ease-out;
}

@keyframes ripple {
  0% {
    transform: scale(0, 0);
    opacity: 0.5;
  }
  100% {
    transform: scale(20, 20);
    opacity: 0;
  }
}

.loading-overlay {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.loading-spinner {
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-top: 4px solid #7c4dff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

.loading-text {
  color: rgba(255, 255, 255, 0.8);
  font-size: 16px;
}

/* 结果视图样式（参考结果页） */
.result-header { text-align: center; margin-bottom: 30px; }
.score-section, .score-block { background: none !important; box-shadow: none !important; border: none !important; padding: 0 !important; }
.score-summary-vertical { display: flex; flex-direction: column; align-items: center; justify-content: center; margin-bottom: 32px; gap: 12px; }
.score-main { display: flex; align-items: baseline; gap: 10px; }
.score-value { font-size: 64px; color: #fff; font-weight: 900; text-shadow: 0 0 18px #4a6fff, 0 0 2px #fff; letter-spacing: 2px; }
.score-label { font-size: 28px; color: #4a6fff; font-weight: 700; text-shadow: 0 0 8px #4a6fff88; }
.score-stats { margin-top: 8px; color: #b3c7f9; font-size: 20px; text-align: center; font-weight: 500; display: flex; flex-direction: row; gap: 32px; justify-content: center; align-items: center; }
.divider { height: 2px; background: linear-gradient(90deg, #4a6fff 0%, #7c4dff 100%); border: none; margin: 24px 0; border-radius: 2px; }
.questions-review { margin-top: 24px; }
.question-item { background: #f8f9fa; border-radius: 8px; padding: 20px; margin-bottom: 20px; border: 1px solid transparent; }
.wrong-question-item { border-color: #ffccc7; background-color: #fff1f0; }
.question-header { display: flex; gap: 10px; margin-bottom: 15px; }
.question-number { color: #4A6FFF; font-weight: bold; min-width: 60px; flex-shrink: 0; }
.option-label { font-weight: bold; color: #666; min-width: 24px; margin-right: 8px; flex-shrink: 0; }
.option-content { flex: 1; }
.user-answer-marker { color: #ff4d4f; font-size: 0.9em; margin-left: 8px; flex-shrink: 0; }
.correct-answer-marker { color: #52c41a; font-size: 0.9em; margin-left: 8px; flex-shrink: 0; }
.answer-review-title { background: linear-gradient(90deg, #4a6fff 0%, #7c4dff 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; color: #fff; font-size: 1.6rem; font-weight: 700; margin-bottom: 18px; text-shadow: 0 0 8px #4a6fff88; letter-spacing: 1px; }
.answer-review-title.big { font-size: 2.2rem; }
.home-button { padding: 14px 40px; font-size: 20px; color: white; background: linear-gradient(45deg, #4A6FFF 0%, #6B8CFF 100%); background-size: 200% 100%; background-position: left center; border: none; border-radius: 10px; cursor: pointer; transition: all 0.3s ease, background-position 0.5s ease; box-shadow: 0 6px 20px rgba(74, 111, 255, 0.3); font-weight: bold; text-transform: uppercase; letter-spacing: 1px; overflow: hidden; position: relative; }
.home-button:hover { background-position: right center; box-shadow: 0 8px 25px rgba(74, 111, 255, 0.4); transform: translateY(-3px); }

/* 结果列表配色增强，确保文本清晰可读 */
.result-content .option-item { background: #ffffff; color: #333; border: 2px solid transparent; border-radius: 12px; padding: 14px 18px; }
.result-content .option-item.correct { background: #e8f5e9; color: #2e7d32; border-color: #2e7d32; }
.result-content .option-item.wrong { background: #ffebee; color: #c62828; border-color: #c62828; }
.result-content .option-item.selected { background: #fff3e0; color: #e65100; border-color: #e65100; }
.result-content .user-answer-marker { background: #ffe0b2; color: #bf360c; padding: 2px 8px; border-radius: 10px; }
.result-content .correct-answer-marker { background: #c8e6c9; color: #1b5e20; padding: 2px 8px; border-radius: 10px; }
/* 提升题干与解析的对比度 */
.result-content .question-content { color: #1f2942; font-weight: 600; line-height: 1.7; }
.result-content .explanation { background: #ffffff; border: 1px solid #e5e7eb; border-radius: 10px; }
.result-content .explanation-title { color: #1f2942; font-weight: 700; }
.result-content .explanation-content { color: #333; }

.error-message {
  text-align: center;
  padding: 40px;
  background: rgba(255, 77, 79, 0.1);
  border-radius: 12px;
  border: 1px solid rgba(255, 77, 79, 0.3);
  backdrop-filter: blur(10px);
}

.error-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.error-text {
  color: #ff4d4f;
  font-size: 16px;
  margin-bottom: 20px;
}

.retry-button {
  display: inline-flex;
  align-items: center;
  padding: 10px 20px;
  background: #7c4dff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-button:hover {
  background: #6a3de8;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(124, 77, 255, 0.3);
}

.retry-icon {
  margin-right: 8px;
  font-size: 18px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 动态星空背景 */
#stars, #stars2, #stars3 {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 100%;
  display: block;
  z-index: 0;
  transform: translateZ(0);
  background: transparent;
}

#stars {
  width: 1px;
  height: 1px;
  /* box-shadow 由JS动态生成并绑定 */
  animation: animStar 150s linear infinite;
}

#stars2 {
  width: 2px;
  height: 2px;
  /* box-shadow 由JS动态生成并绑定 */
  animation: animStar 100s linear infinite;
}

#stars3 {
  width: 3px;
  height: 3px;
  /* box-shadow 由JS动态生成并绑定 */
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

/* 护眼模式样式 */
.eye-care .bubble {
  background-color: rgba(127, 176, 105, 0.1);
}

/* 护眼模式下的页面背景 */
.eye-care.interview-questions-container {
  background: linear-gradient(135deg, #1c2a1f 0%, #2a3c2e 100%);
}

.eye-care .content-container {
  background: rgba(127, 176, 105, 0.03);
  border-color: rgba(127, 176, 105, 0.1);
}

.eye-care .question-card {
  background: rgba(127, 176, 105, 0.05);
  border-color: rgba(127, 176, 105, 0.1);
}

.eye-care .option-item {
  background-color: rgba(127, 176, 105, 0.03);
  border-color: rgba(127, 176, 105, 0.2);
  color: rgba(255, 255, 255, 0.9);
}

.eye-care .option-item:hover:not(.selected):not(.correct):not(.wrong) {
  border-color: #7fb069;
  background-color: rgba(127, 176, 105, 0.1);
}

.eye-care .option-item.selected {
  border-color: #7fb069;
  background-color: rgba(127, 176, 105, 0.2);
}

.eye-care .progress-fill {
  background: linear-gradient(135deg, #7fb069 0%, #a8c69f 100%);
}

.eye-care .next-button {
  background: linear-gradient(135deg, #7fb069 0%, #a8c69f 100%);
}

.eye-care .next-button:hover {
  background: linear-gradient(135deg, #6a9a5a 0%, #7fb069 100%);
}

.eye-care .next-button:active {
  background: linear-gradient(135deg, #5a8a4a 0%, #6a9a5a 100%);
}

/* 护眼模式下的卡片颜色调整 */
.eye-care .content-container {
  background: rgba(144, 198, 149, 0.08);
  border: 1px solid rgba(127, 176, 105, 0.25);
  box-shadow: 0 10px 30px rgba(127, 176, 105, 0.15);
}

.eye-care .question-card {
  background: rgba(144, 198, 149, 0.12);
  border: 1px solid rgba(127, 176, 105, 0.25);
  box-shadow: 0 2px 8px rgba(127, 176, 105, 0.15);
}

.eye-care .option-item {
  background: rgba(144, 198, 149, 0.08);
  border: 1px solid rgba(127, 176, 105, 0.25);
  color: #fff !important;
}

.eye-care .option-item:hover:not(.selected):not(.correct):not(.wrong) {
  background: rgba(127, 176, 105, 0.15);
  border-color: rgba(127, 176, 105, 0.4);
  box-shadow: 0 4px 12px rgba(127, 176, 105, 0.2);
}

.eye-care .option-item.selected {
  background: rgba(127, 176, 105, 0.2);
  border-color: rgba(127, 176, 105, 0.4);
  box-shadow: 0 2px 8px rgba(127, 176, 105, 0.3);
}

.eye-care .option-item.correct {
  background: rgba(127, 176, 105, 0.25);
  border-color: rgba(127, 176, 105, 0.4);
  box-shadow: 0 2px 8px rgba(127, 176, 105, 0.3);
}

.eye-care .option-item.wrong {
  background: rgba(255, 77, 79, 0.15);
  border-color: rgba(255, 77, 79, 0.3);
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.2);
}

.eye-care .explanation {
  background: rgba(144, 198, 149, 0.08);
  border: 1px solid rgba(127, 176, 105, 0.25);
}

.eye-care .question-content {
  color: #fff !important;
}

.eye-care .explanation-title {
  color: #fff !important;
}

.eye-care .explanation-content {
  color: rgba(255, 255, 255, 0.9) !important;
}

.eye-care .loading-overlay {
  background: rgba(144, 198, 149, 0.08);
  border: 1px solid rgba(127, 176, 105, 0.25);
}

.eye-care .spinner {
  border-color: rgba(127, 176, 105, 0.1);
  border-top-color: #7fb069;
}

.eye-care .error-message {
  background: rgba(255, 77, 79, 0.08);
  border: 1px solid rgba(255, 77, 79, 0.25);
}

.eye-care .prev-button {
  background: rgba(127, 176, 105, 0.1);
  border-color: rgba(127, 176, 105, 0.3);
}

.eye-care .prev-button:hover {
  background: rgba(127, 176, 105, 0.2);
  border-color: rgba(127, 176, 105, 0.4);
  box-shadow: 0 4px 12px rgba(127, 176, 105, 0.2);
}

.eye-care .submit-button {
  background: linear-gradient(135deg, #7fb069 0%, #90c695 100%);
}

.eye-care .submit-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #6a9a5a 0%, #7fb069 100%);
}

.eye-care .submit-button:active:not(:disabled) {
  background: linear-gradient(135deg, #5a8a4a 0%, #6a9a5a 100%);
}

.eye-care .retry-button {
  background: #7fb069;
}

.eye-care .retry-button:hover {
  background: #6a9a5a;
  box-shadow: 0 4px 12px rgba(127, 176, 105, 0.3);
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

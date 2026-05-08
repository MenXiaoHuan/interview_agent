<template>
  <div class="comprehensive-questions-container" :class="{ 'eye-care': isEyeCareMode }">
    <div v-if="isLoadingQuestions" class="ai-loading-mask">
      <div class="ai-loading-content">
        <div class="ai-spinner"></div>
        <div class="ai-loading-text">AI正在为你生成专属试题，请稍候…</div>
      </div>
    </div>
    <div v-else-if="aiError" class="ai-error-mask">
      <div class="ai-error-card">
        <div class="ai-error-title">AI出题失败</div>
        <div class="ai-error-message">{{ errorMessage }}</div>
        <button
          class="retry-btn"
          :disabled="isRetrying"
          @click="retryGenerateQuestions"
        >
          <span v-if="isRetrying" class="loading-spinner"></span>
          <span v-else>重试</span>
        </button>
      </div>
    </div>
    <!-- 动态背景 -->
    <div class="animated-background">
      <div class="gradient-sphere"></div>
      <div class="particles"></div>
      <div class="scanning-lines">
        <div class="horizontal-scan"></div>
        <div class="vertical-scan"></div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="content-wrapper">
      <!-- 顶部导航 -->
      <div class="top-nav">
        <div class="back-button" @click="handleBack">
          <svg class="back-icon" width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 6l-6 6 6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M21 12H9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <span>返回</span>
        </div>
        <div class="timer">
          <SmartIcon name="fas fa-clock" />
          <span>{{ formatTime(remainingTime) }}</span>
        </div>
      </div>

      <!-- 题目导航 -->
      <div class="question-nav" v-if="!isComplete && questions.length > 0">
        <div class="nav-items">
          <div 
            v-for="(q, index) in questions" 
            :key="index"
            class="nav-item"
            :class="{
              'current': currentQuestionIndex === index,
              'answered': q.answered
            }"
            @click="goToQuestion(index)"
          >
            {{ index + 1 }}
          </div>
        </div>
      </div>

      <!-- 题目内容 -->
      <div class="question-content" v-if="!isComplete && questions.length > 0">
        <div class="question-header">
          <div class="question-type">
            {{ currentQuestion.type || '' }}
          </div>
          <div class="question-number">
            第 {{ currentQuestionIndex + 1 }}/{{ questions.length }} 题
          </div>
        </div>

        <div class="question-body">
          <div class="question-text">
            {{ currentQuestion.text || '' }}
          </div>

          <!-- 选择题选项 -->
          <div class="options-list" v-if="currentQuestion.type === '单选题' || currentQuestion.type === '多选题'">
            <div 
              v-for="(option, index) in currentQuestion.options || []" 
              :key="index"
              class="option-item"
              :class="{
                'selected': isOptionSelected(option),
                'correct': showAnswer && option.isCorrect,
                'incorrect': showAnswer && isOptionSelected(option) && !option.isCorrect
              }"
              @click="selectOption(option)"
            >
              <div class="option-marker">
                {{ String.fromCharCode(65 + index) }}
              </div>
              <div class="option-text">
                {{ (option.text || '').replace(/^[A-Da-d][.．、\s]/, '') }}
              </div>
            </div>
          </div>

          <!-- 填空题输入 -->
          <div class="fill-blank" v-if="currentQuestion.type === '填空题'">
            <input 
              type="text" 
              v-model="currentQuestion.userAnswer"
              placeholder="请输入答案"
              class="blank-input"
            >
          </div>

          <!-- 场景题输入 -->
          <div class="scenario-question" v-if="currentQuestion.type === '场景题'">
            <textarea 
              v-model="currentQuestion.userAnswer"
              placeholder="请根据场景作答..."
              class="scenario-textarea"
            ></textarea>
          </div>

          <!-- 编程题编辑器 -->
          <div class="code-editor" v-if="currentQuestion.type === '编程题'">
            <div class="editor-header">
              <div class="language-selector">
                <select v-model="currentQuestion.language">
                  <option value="javascript">JavaScript</option>
                  <option value="python">Python</option>
                  <option value="java">Java</option>
                </select>
              </div>
              <div class="editor-actions">
                <button class="action-button" @click="runCode">
                  <SmartIcon name="fas fa-play" />
                  运行
                </button>
              </div>
            </div>
            <div class="editor-content">
              <textarea 
                v-model="currentQuestion.code"
                class="code-textarea"
                placeholder="在这里编写你的代码..."
              ></textarea>
            </div>
            <div class="output-panel" v-if="currentQuestion.output">
              <div class="output-header">输出结果</div>
              <div class="output-content">
                {{ currentQuestion.output }}
              </div>
            </div>
          </div>
        </div>

        <div class="question-footer">
          <div class="action-buttons">
            <button 
              class="prev-button"
              @click="prevQuestion"
              v-if="currentQuestionIndex > 0"
            >
              上一题
            </button>
            <button 
              class="next-button"
              @click="nextQuestion"
              v-if="!isLastQuestion"
            >
              下一题
            </button>
            <!-- 交卷按钮只在最后一题显示 -->
            <button
              v-if="isLastQuestion && !isComplete && !isLoadingQuestions"
              class="submit-exam-button"
              @click="onSubmit"
              style="margin-left: 16px; background: #00ffff; color: #222; border-radius: 8px; padding: 0 24px; font-weight: bold;"
            >
              交卷
            </button>
          </div>
        </div>
      </div>

      <!-- 完成页面 -->
      <div class="completion-page" v-if="isComplete">
        <div class="completion-card">
          <h2>试题作答完成</h2>
          <div class="completion-stats">
            <div class="stat-item">
              <div class="stat-label">总用时</div>
              <div class="stat-value">{{ formatTime(totalTime) }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">总分</div>
              <div class="stat-value">{{ score }}</div>
            </div>
          </div>

          <!-- 题目回顾区域 -->
          <div class="questions-review">
            <div v-for="(question, index) in questions" :key="index" class="question-review-item">
              <div class="question-header">
                <span class="question-number">第 {{ index + 1 }} 题</span>
                <span class="question-type">{{ question.type }}</span>
                <span class="question-score">得分：{{ question.score }}</span>
              </div>
              
              <div class="question-content">
                <div class="question-text">{{ question.text }}</div>
                <!-- 单选题选项展示优化，防止前缀重复和undefined报错 -->
                <div v-if="question.type === '单选题' && question.options">
                  <div class="options-list">
                    <div
                      v-for="(opt, optIdx) in question.options"
                      :key="optIdx"
                      :class="['option', { correct: opt.text === question.answer, chosen: opt.text === question.userAnswer }]"
                    >
                      <span>
                        {{ String.fromCharCode(65 + optIdx) }}. {{ (opt.text || '').replace(/^[A-Da-d][.．、\s]/, '') }}
                        <span v-if="opt.text === question.userAnswer">（你的选择）</span>
                        <span v-if="opt.text === question.answer">（正确答案）</span>
                      </span>
                    </div>
                  </div>
                  <div style="margin-top: 8px;">
                    正确答案：{{ getOptionLabel(question.options, question.answer) }}{{ (question.answer || '').replace(/^[A-Da-d][.．、\s]/, '') }}
                  </div>
                </div>
                <!-- 其它题型答案展示 -->
                <div v-else>
                  <div class="user-answer">
                    <strong>你的答案：</strong>
                    <span>{{ question.userAnswer }}</span>
                  </div>
                  <div class="correct-answer">
                    <strong>正确答案：</strong>
                    <span>{{ question.answer }}</span>
                  </div>
                </div>
                
                <!-- AI解析部分 -->
                <div class="analysis-section">
                  <button 
                    v-if="!analysisResults[index]"
                    @click="getQuestionAnalysis(index)"
                    class="analysis-button"
                    :class="{ 'loading': isAnalyzing }"
                  >
                    获取AI详细解析
                  </button>
                  <div v-else class="analysis-content">
                    <div class="analysis-title">AI解析：</div>
                    <div class="analysis-text">{{ analysisResults[index] }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="completion-actions">
            <button class="complete-button" @click="goToAudioAssessment">
              完成试题作答
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 全局AI加载遮罩 -->
    <div v-if="isAnalyzing" class="ai-global-mask">
      <div class="ai-global-loading-content">
        <div class="ai-spinner"></div>
        <div class="ai-loading-text">{{ analyzingText }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import request from '@/utils/request';
import { generateComprehensiveQuestions, scoreScenarioQuestions, getQuestionAnalysisByAi } from '@/utils/api';
import { getJobInfo, saveComprehensiveQuestionHistory } from '@/utils/api';
import { getUserSession } from '@/utils/user-session';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
import SmartIcon from '@/components/SmartIcon.vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);

// 状态变量
const questions = ref([]); // 题目由AI生成
const currentQuestionIndex = ref(0);
const remainingTime = ref(1200); // 20分钟（单位：秒）
const isComplete = ref(false);
const showAnswer = ref(false);
const totalTime = ref(0);
const startTime = ref(Date.now());
const isLoadingQuestions = ref(false);
const aiError = ref(false);
const errorMessage = ref('');
const isAnalyzing = ref(false); // 是否正在AI分析中
const analysisResults = ref({}); // 存储每道题的AI解析结果
const analyzingText = ref('AI批阅中...');

// 将 accuracy 和 score 改为 ref 变量，用于存储写死的分数
const accuracy = ref(0);
const score = ref(0);

// 计算属性
const currentQuestion = computed(() => {
  if (questions.value.length === 0) return {};
  return questions.value[currentQuestionIndex.value] || {};
});

let timer = null;
const isTimerStarted = ref(false); // 标记倒计时是否已开始

// 检查是否是最后一题
const isLastQuestion = computed(() => {
  return currentQuestionIndex.value === questions.value.length - 1;
});

const buildInterviewAiReturnUrl = (extraQuery = {}) => {
  const query = [];
  query.push('autoOpen=1');
  let safeSessionId = String(route.query.sessionId || '').trim();
  if (!safeSessionId) {
    try {
      safeSessionId = String(sessionStorage.getItem('activeAiConversationSessionId') || '').trim();
    } catch (_) {
      safeSessionId = '';
    }
  }
  const safeJobId = String(route.query.jobId || '').trim();
  if (safeSessionId) {
    query.push(`sessionId=${encodeURIComponent(safeSessionId)}`);
  }
  if (safeJobId) {
    query.push(`jobId=${encodeURIComponent(safeJobId)}`);
  }
  Object.entries(extraQuery).forEach(([key, value]) => {
    const safeValue = String(value || '').trim();
    if (safeValue) {
      query.push(`${encodeURIComponent(key)}=${encodeURIComponent(safeValue)}`);
    }
  });
  return `/pages/interview-ai/index${query.length ? `?${query.join('&')}` : ''}`;
};

// 检查未完成的题目
function getUnansweredQuestions() {
  return questions.value.reduce((acc, q, index) => {
    if (!q.answered) {
      acc.push(index + 1);
    }
    return acc;
  }, []);
}

// 获取岗位信息并调用AI生成题目
const generateQuestionsByAI = async () => {
  isLoadingQuestions.value = true;
  aiError.value = false;
  errorMessage.value = '';

  try {
    // 获取职位信息
    const jobId = route.query.jobId;
    if (!jobId) {
      throw new Error('未获取到岗位ID');
    }
    
    const jobInfo = await getJobInfo(jobId);
    const jobName = jobInfo.data?.name || jobInfo.data?.title || '未知岗位';
    const jobDesc = jobInfo.data?.description || jobInfo.data?.detail || '';

    const aiRes = await generateComprehensiveQuestions(jobName, jobDesc);

    // 检查响应数据
    if (!aiRes.data) {
      throw new Error('AI返回数据为空，请重试');
    }

    let aiData;
    try {
      if (typeof aiRes.data !== 'object' || aiRes.data === null) {
        throw new Error('AI返回数据格式不正确，请重试');
      }
      aiData = aiRes.data;

      // 验证数据格式
      if (!aiData['选择题'] || !aiData['填空题'] || !aiData['场景题']) {
        throw new Error('AI返回数据格式不正确，缺少必要的题目类型');
      }

      if (!Array.isArray(aiData['选择题']) || !Array.isArray(aiData['填空题']) || !Array.isArray(aiData['场景题'])) {
        throw new Error('AI返回数据格式不正确，题目数据必须是数组');
      }

      // 验证题目数量
      if (aiData['选择题'].length !== 5 || aiData['填空题'].length !== 2 || aiData['场景题'].length !== 1) {
        throw new Error('AI返回题目数量不正确，请重试');
      }
    } catch (parseError) {
      console.error('解析AI返回数据失败:', parseError);
      throw new Error('解析AI返回数据失败: ' + parseError.message);
    }

    // 解析AI返回的题目，转换为页面可用结构
    const aiQuestions = [];
    
    // 选择题
    (aiData['选择题'] || []).forEach((q, index) => {
      if (!q.question || !Array.isArray(q.options) || !q.answer) {
        throw new Error(`第${index + 1}个选择题数据格式不正确`);
      }
      // 找到正确答案的索引
      const answerIndex = q.answer.charCodeAt(0) - 65; // 'A'->0, 'B'->1...
      const answerText = q.options[answerIndex];
      aiQuestions.push({
        type: '单选题',
        text: q.question,
        options: (q.options || []).map((opt, idx) => ({
          text: opt,
          isCorrect: idx === answerIndex
        })),
        answer: q.answer, // 字母
        answerText,       // 正确选项文本
        answered: false,
        userAnswer: null,
        score: q.score || 10
      });
    });

    // 填空题
    (aiData['填空题'] || []).forEach((q, index) => {
      if (!q.question || !q.answer) {
        throw new Error(`第${index + 1}个填空题数据格式不正确`);
      }
      aiQuestions.push({
        type: '填空题',
        text: q.question,
        answer: q.answer,
        answered: false,
        userAnswer: '',
        score: q.score || 10
      });
    });

    // 场景题
    (aiData['场景题'] || []).forEach((q, index) => {
      if (!q.question || !q.answer) {
        throw new Error(`第${index + 1}个场景题数据格式不正确`);
      }
      aiQuestions.push({
        type: '场景题',
        text: q.question,
        answer: q.answer,
        answered: false,
        userAnswer: '',
        score: q.score || 30
      });
    });

    if (aiQuestions.length === 0) {
      throw new Error('未能生成有效题目，请重试');
    }

    questions.value = aiQuestions;
    // 题目生成完毕，启动倒计时
    if (!isTimerStarted.value) {
      startTimer();
    }
  } catch (err) {
    aiError.value = true;
    questions.value = [];
    
    if (err.code === 'ECONNABORTED') {
      errorMessage.value = '请求超时，请检查网络连接';
    } else if (err.response) {
      if (err.response.status === 500) {
        errorMessage.value = 'AI服务器内部错误，请重试';
      } else if (err.response.status === 429) {
        errorMessage.value = 'AI服务请求过于频繁，请稍后重试';
      } else {
        errorMessage.value = err.response.data?.message || '服务器响应错误';
      }
    } else if (err.message) {
      errorMessage.value = err.message;
    } else {
      errorMessage.value = '生成题目时发生未知错误';
    }
    
    console.error('AI出题失败', err);
  } finally {
    isLoadingQuestions.value = false;
  }
};

function startTimer() {
  isTimerStarted.value = true;
  remainingTime.value = 1200; // 20分钟
  startTime.value = Date.now();
  
  // 保存开始时间到本地存储，用于计算总用时
  const startTimeISO = new Date().toISOString();
  uni.setStorageSync('questionsStartTime', startTimeISO);
  localStorage.setItem('questionsStartTime', startTimeISO);
  
  if (timer) clearInterval(timer);
  timer = setInterval(() => {
    if (remainingTime.value > 0 && !isComplete.value) {
      remainingTime.value--;
    } else {
      clearInterval(timer);
      if (!isComplete.value) {
        completeTest();
      }
    }
  }, 1000);
}

// 监听器：当填空题输入内容时，标记为已回答
watch(() => currentQuestion.value.userAnswer, (newValue, oldValue) => {
  if (!currentQuestion.value || !('userAnswer' in currentQuestion.value)) return;
  if (currentQuestion.value.type === '填空题' && newValue !== null && newValue.trim() !== '') {
    currentQuestion.value.answered = true;
  } else if (currentQuestion.value.type === '填空题' && (newValue === null || newValue.trim() === '')) {
    currentQuestion.value.answered = false;
  }
});

// 方法
const formatTime = (seconds) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

const goToQuestion = (index) => {
  currentQuestionIndex.value = index;
};

const selectOption = (option) => {
  if (currentQuestion.value.type === '单选题') {
    currentQuestion.value.userAnswer = option.text;
  } else if (currentQuestion.value.type === '多选题') {
    if (!currentQuestion.value.userAnswer) {
      currentQuestion.value.userAnswer = [];
    }
    const index = currentQuestion.value.userAnswer.indexOf(option.text);
    if (index === -1) {
      currentQuestion.value.userAnswer.push(option.text);
    } else {
      currentQuestion.value.userAnswer.splice(index, 1);
    }
  }
  currentQuestion.value.answered = true;
};

const isOptionSelected = (option) => {
  if (currentQuestion.value.type === '单选题') {
    return currentQuestion.value.userAnswer === option.text;
  }
  if (currentQuestion.value.type === '多选题') {
    return currentQuestion.value.userAnswer?.includes(option.text);
  }
  return false;
};


const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--;
  }
};

const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++;
  } else {
    completeTest();
  }
};

const runCode = () => {
  // 实现代码运行逻辑
  currentQuestion.value.output = '运行结果将显示在这里...';
};

const submitCode = () => {
  // 实现代码提交逻辑
  currentQuestion.value.answered = true;
  // After submitting, move to the next question
  nextQuestion();
};

const reviewAnswers = () => {
  showAnswer.value = true;
  uni.showToast({
    title: '现在可以查看答案了',
    icon: 'none',
    duration: 1500
  });
  console.log('Reviewing answers...');
};

const completeTest = () => {
  isComplete.value = true;
  totalTime.value = Math.floor((Date.now() - startTime.value) / 1000);

  // 写死分析分数和正确率，用于显示在完成页面
  accuracy.value = 85; // 示例：写死的正确率
  score.value = 90;    // 示例：写死的得分

  // 这里不再进行本地存储更新和页面跳转，这些操作将由"下一步"按钮触发
  uni.showToast({ title: '答题完成，请点击下一步查看结果', icon: 'none', duration: 2000 });
};

const handleBack = () => {
  if (!isComplete.value) {
    uni.showModal({
      title: '提示',
      content: '确定要退出测试吗？已答题目将不会保存。',
      success: (res) => {
        if (res.confirm) {
          router.replace(buildInterviewAiReturnUrl());
        }
      }
    });
  } else {
    router.replace(buildInterviewAiReturnUrl());
  }
};

const getJobIdFromUrlOrStorage = () => {
  let urlJobId = null;
  try {
    const pageParams = plus?.webview?.currentWebview()?.extras;
    if (pageParams && pageParams.jobId) {
      urlJobId = pageParams.jobId;
      uni.setStorageSync('currentJobId', urlJobId);
      return urlJobId;
    }
  } catch (err) {}
  try {
    const launchOptions = uni.getLaunchOptionsSync();
    if (launchOptions && launchOptions.query && launchOptions.query.jobId) {
      urlJobId = launchOptions.query.jobId;
      uni.setStorageSync('currentJobId', urlJobId);
      return urlJobId;
    }
    const currentUrl = window.location.href;
    const queryMatch = currentUrl.match(/[?&]jobId=([^&#]*)/);
    if (queryMatch && queryMatch[1]) {
      urlJobId = queryMatch[1];
      uni.setStorageSync('currentJobId', urlJobId);
      return urlJobId;
    }
    const hashMatch = currentUrl.match(/#.*[?&]jobId=([^&#]*)/);
    if (hashMatch && hashMatch[1]) {
      urlJobId = hashMatch[1];
      uni.setStorageSync('currentJobId', urlJobId);
      return urlJobId;
    }
  } catch (err) {}
  try {
    const savedJobId = uni.getStorageSync('currentJobId');
    if (savedJobId) {
      return savedJobId;
    }
  } catch (error) {}
  return null;
};

const goToAudioAssessment = async () => {
  try {
    // jobId 全链路字符串且去除空格
    let jobId = (route.query.jobId || getJobIdFromUrlOrStorage() || '').toString().trim();
    const timestamp = Date.now();
    const endTime = new Date().toISOString();
    const startTime = uni.getStorageSync('questionsStartTime') || new Date().toISOString();
    const aiReturnUrl = buildInterviewAiReturnUrl({
      completedType: 'questions',
      mode: 'COMPREHENSIVE',
      score: score.value,
      timestamp
    });
    
    // 计算总用时（秒）
    const startTimeObj = new Date(startTime);
    const endTimeObj = new Date(endTime);
    const totalTime = Math.floor((endTimeObj - startTimeObj) / 1000);
    
    // 先读取旧状态
    let old = {};
    const savedState = uni.getStorageSync('comprehensiveTestState');
    if (savedState) {
      old = JSON.parse(savedState);
    }
    // 构造新状态，questions 字段只用当前作答
    let state = {
      resume: old.resume || {
        completed: false,
        inProgress: false,
        score: 0,
        analysisId: null,
        startTime: null,
        endTime: null,
        attempts: 0
      },
      questions: {
      completed: true,
      inProgress: false,
        score: score.value,
        interviewId: 'interview_' + timestamp,
      endTime: endTime,
        startTime: old.questions?.startTime || startTime,
        attempts: (old.questions?.attempts || 0) + 1
      },
      audio: old.audio || {
        completed: false,
        inProgress: false,
        score: 0,
        assessmentId: null,
        startTime: null,
        endTime: null,
        attempts: 0
      },
      overall: old.overall || {
        startTime: new Date().toISOString(),
        endTime: null,
        totalScore: 0,
        status: 'in_progress'
      },
      lastUpdated: new Date().toISOString(),
      jobId: jobId
    };
    
    // 保存问题综合评估历史到后端
    try {
      const historyData = {
        userId: Number((getUserSession() || {}).userId || userStore.userId || 0),
        jobId: jobId,
        startTime: startTime,
        endTime: endTime,
        totalTime: totalTime,
        overallScore: score.value,
        aiSuggestions: JSON.stringify({ 
          suggestions: [
            `试题作答得分：${score.value}分`,
            `总用时：${Math.floor(totalTime / 60)}分${totalTime % 60}秒`,
            `答题准确率：${accuracy.value}%`,
            '建议继续完成其他评测模块以获得完整的综合评估',
            '建议多练习相关岗位的专业知识',
            '注意答题时间的合理分配'
          ] 
        })
      };
      
      console.log('[Comprehensive Questions] 准备保存历史记录:', historyData);
      
      const result = await saveComprehensiveQuestionHistory(historyData);
      console.log('[Comprehensive Questions] 历史记录保存成功:', result);
      
      // 显示保存成功提示
      uni.showToast({
        title: '历史记录已保存',
        icon: 'success',
        duration: 1000
      });
      
    } catch (historyError) {
      console.error('[Comprehensive Questions] 保存历史记录失败:', historyError);
      // 历史记录保存失败不影响主流程，只记录错误
      uni.showToast({
        title: '历史记录保存失败，但不影响评测结果',
        icon: 'none',
        duration: 2000
      });
    }
    
    // 写入 currentJobId
    if (jobId) {
      uni.setStorageSync('currentJobId', jobId);
      try {
        sessionStorage.setItem('currentJobId', jobId);
        localStorage.setItem('currentJobId', jobId);
      } catch (e) {}
    }
  uni.showToast({ title: '试题作答已完成', icon: 'success', duration: 1500 });
  setTimeout(() => {
      try {
        uni.redirectTo({
          url: aiReturnUrl,
          success: () => {},
          fail: (err) => {
            uni.navigateBack({
              delta: 1,
              success: () => {
                try {
                  uni.$emit && uni.$emit('questionsCompleted', {
                    score: score.value,
                    timestamp,
                    sessionId: String(route.query.sessionId || '').trim()
                  });
                } catch (emitError) {}
              },
              fail: (backErr) => {
                try {
                  router.replace(aiReturnUrl);
                } catch (routerErr) {
                  setTimeout(() => {
                    window.location.href = `/#${aiReturnUrl}`;
                  }, 500);
                }
              }
            });
          }
        });
      } catch (error) {
        setTimeout(() => {
          router.replace(aiReturnUrl);
        }, 2000);
    }
  }, 1500);
  } catch (error) {
    uni.showToast({ title: '状态保存失败，请重试', icon: 'error' });
    setTimeout(() => {
      router.replace(buildInterviewAiReturnUrl());
    }, 2000);
  }
};

// 工具函数：获取选项标签
function getOptionLabel(options, answer) {
  const idx = options.findIndex(opt => opt.text === answer);
  return idx >= 0 ? String.fromCharCode(65 + idx) + '. ' : '';
}

// 交卷前未作答校验逻辑修正
function checkUnanswered() {
  return questions.value.filter(q => {
    if (q.type === '单选题' || q.type === '填空题') {
      return !q.userAnswer || q.userAnswer === '';
    }
    if (q.type === '场景题') {
      return !q.userAnswer || q.userAnswer.trim() === '';
    }
    return false;
  });
}

// 交卷按钮点击事件
async function onSubmit() {
  const unanswered = checkUnanswered();
  if (unanswered.length > 0) {
    uni.showModal({
      title: '提示',
      content: `还有${unanswered.length}道题未作答，是否确认交卷？`,
      success: async (res) => {
        if (res.confirm) {
          await handleExamSubmission();
        }
      }
    });
    } else {
    await handleExamSubmission();
  }
}

// 处理交卷逻辑
async function handleExamSubmission() {
  if (timer) clearInterval(timer);
  isAnalyzing.value = true;
  analyzingText.value = 'AI批阅中...';

  // 1. 前端自动判分（选择题/填空题）
  let totalScore = 0;
  let localQuestions = questions.value.map(q => {
    if (q.type === '单选题') {
      // 选择题：比较选项文本是否一致
      const isCorrect = q.userAnswer === q.answerText;
      const score = isCorrect ? q.score : 0;
      totalScore += score;
      return {
        ...q,
        score,
        feedback: isCorrect ? '回答正确' : '回答错误',
        showOptions: true // 用于解析界面显示选项
      };
    } else if (q.type === '填空题') {
      // 填空题：关键词完全匹配得分，否则0分（如需部分得分可扩展）
      const userAns = (q.userAnswer || '').trim();
      const stdAns = (q.answer || '').trim();
      let score = 0;
      let feedback = '回答错误';
      if (userAns && stdAns) {
        if (userAns === stdAns) {
          score = q.score;
          feedback = '回答正确';
        }
      }
      totalScore += score;
      return {
        ...q,
        score,
        feedback
      };
    }
    // 场景题暂不处理，后续AI判分
    return { ...q };
  });

  // 2. 收集所有场景题，准备AI判分
  const scenarioQuestions = localQuestions
    .map((q, idx) => ({ ...q, idx }))
    .filter(q => q.type === '场景题');

  let aiScores = [];
  if (scenarioQuestions.length > 0) {
    try {
      const aiRes = await scoreScenarioQuestions(
        scenarioQuestions.map(q => ({
          question: q.text,
          userAnswer: q.userAnswer || '未作答',
          maxScore: q.score
        }))
      );
      const aiData = aiRes.data;
      aiScores = Array.isArray(aiData) ? aiData : (aiData.examData || []);
    } catch (error) {
      console.error('AI场景题评分失败:', error);
      aiScores = scenarioQuestions.map(() => ({ score: 0, analysis: 'AI评分失败' }));
    }
  }

  // 3. 合并AI评分结果回原题目
  aiScores.forEach((ai, i) => {
    const idx = scenarioQuestions[i].idx;
    const max = Number(scenarioQuestions[i].score || scenarioQuestions[i].maxScore || 30);
    const raw = Number(ai?.score ?? 0);
    const clamped = Math.max(0, Math.min(max, Math.round(raw)));
    localQuestions[idx].score = clamped;
    localQuestions[idx].feedback = ai.analysis || '无解析';
    totalScore += clamped;
  });

  // 4. 更新题目和分数
  questions.value = localQuestions;
  score.value = Math.max(0, Math.min(100, Math.round(totalScore)));
  accuracy.value = Math.round((score.value / 100) * 100);
  isAnalyzing.value = false;
  isComplete.value = true;
  totalTime.value = Math.floor((Date.now() - startTime.value) / 1000);
}

// AI解析单个题目
async function getQuestionAnalysis(questionIndex) {
  if (analysisResults.value[questionIndex]) {
    return; // 如果已有解析结果，直接返回
  }

  const question = questions.value[questionIndex];
  isAnalyzing.value = true;
  analyzingText.value = 'AI解析中...';

  try {
    const aiRes = await getQuestionAnalysisByAi({
      type: question.type,
      question: question.text,
      options: question.options?.map(opt => opt.text),
      answer: question.answer
    });

    let analysis = aiRes.data;
    if (typeof analysis === 'string') {
      // 移除可能的 markdown 格式和多余符号
      analysis = analysis.replace(/```[\s\S]*?```/g, '')
                        .replace(/#+\s/g, '')
                        .replace(/\*\*/g, '')
                        .replace(/\n\n/g, '\n')
                        .trim();
    }

    // 更新解析结果
    analysisResults.value = {
      ...analysisResults.value,
      [questionIndex]: analysis
    };

  } catch (error) {
    console.error('获取题目解析失败:', error);
    uni.showToast({
      title: '获取解析失败，请重试',
      icon: 'none'
    });
  } finally {
    isAnalyzing.value = false;
  }
}

const isRetrying = ref(false);

async function retryGenerateQuestions() {
  isRetrying.value = true;
  errorMessage.value = '';
  try {
    await generateQuestionsByAI();
    aiError.value = false; // 成功后关闭错误卡片
  } catch (e) {
    // errorMessage 已在 generateQuestionsByAI 内部设置
  } finally {
    isRetrying.value = false;
  }
}

onMounted(() => {
  generateQuestionsByAI();
});

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer);
  }
});
</script>

<style scoped>
.comprehensive-questions-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #02071E 0%, #050B30 50%, #070D35 100%);
  font-family: 'Arial', sans-serif;
  color: #E2E8F0;
  position: relative;
  overflow: hidden;
  padding: 60rpx;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: center;
}

.eye-care {
  background: linear-gradient(135deg, #f0f2e6 0%, #e8ecd9 50%, #dfe3d0 100%) !important;
}

/* 增强动态背景效果 */
.animated-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
}

.gradient-sphere {
  position: absolute;
  width: 1200rpx;
  height: 1200rpx;
  background: radial-gradient(circle, 
    rgba(0, 255, 255, 0.15) 0%, 
    rgba(0, 255, 255, 0.1) 30%,
    rgba(0, 255, 255, 0) 70%
  );
  top: -400rpx;
  right: -400rpx;
  animation: float 20s infinite ease-in-out;
  filter: blur(30px);
}

.particles {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(0, 255, 255, 0.15) 1px, transparent 1px),
    radial-gradient(circle at 75% 75%, rgba(0, 255, 255, 0.15) 1px, transparent 1px);
  background-size: 40rpx 40rpx;
  animation: particleFloat 30s infinite linear;
  opacity: 0.6;
}

/* 增强扫描线效果 */
.scanning-lines {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.horizontal-scan {
  position: absolute;
  height: 2px;
  width: 100%;
  background: linear-gradient(90deg, 
    rgba(0, 255, 255, 0) 0%, 
    rgba(0, 255, 255, 0.7) 50%,
    rgba(0, 255, 255, 0) 100%
  );
  top: 50%;
  animation: horizontalScan 8s infinite linear;
  box-shadow: 0 0 15px rgba(0, 255, 255, 0.5);
}

.vertical-scan {
  position: absolute;
  width: 2px;
  height: 100%;
  background: linear-gradient(180deg, 
    rgba(0, 255, 255, 0) 0%, 
    rgba(0, 255, 255, 0.7) 50%,
    rgba(0, 255, 255, 0) 100%
  );
  left: 50%;
  animation: verticalScan 12s infinite linear;
  box-shadow: 0 0 15px rgba(0, 255, 255, 0.5);
}

/* 优化内容区域 */
.content-wrapper {
  position: relative;
  z-index: 1;
  max-width: 1200rpx;
  width: 100%;
  margin: 0 auto;
  padding: 40rpx;
  background: rgba(2, 7, 30, 0.7);
  backdrop-filter: blur(20px);
  border-radius: 24rpx;
  border: 1px solid rgba(0, 255, 255, 0.2);
  box-shadow: 0 0 30px rgba(0, 255, 255, 0.1);
  display: block;
}

.eye-care .content-wrapper {
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid rgba(76, 175, 80, 0.3);
  box-shadow: 0 0 30px rgba(76, 175, 80, 0.15);
}

/* 优化顶部导航 */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  margin-bottom: 30rpx;
  border-bottom: 1px solid rgba(0, 255, 255, 0.2);
}

.back-button {
  display: flex;
  align-items: center;
  gap: 15rpx;
  color: #00ffff;
  cursor: pointer;
  font-weight: 500;
  font-size: 32rpx;
  transition: all 0.3s ease;
  padding: 12rpx 24rpx;
  border-radius: 8rpx;
  background: rgba(0, 255, 255, 0.1);
  border: 1px solid rgba(0, 255, 255, 0.3);
}

.back-button:hover {
  transform: translateX(-8rpx);
  background: rgba(0, 255, 255, 0.2);
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.3);
}

/* 自定义返回箭头 */
.back-icon { color: #00ffff; filter: drop-shadow(0 0 8px rgba(0,255,255,0.5)); transition: transform 0.3s ease; }
.back-button:hover .back-icon { transform: translateX(-2px); }

.timer {
  display: flex;
  align-items: center;
  gap: 15rpx;
  color: #00ffff;
  font-weight: 500;
  font-size: 32rpx;
  background: rgba(0, 255, 255, 0.1);
  padding: 12rpx 24rpx;
  border-radius: 8rpx;
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.2);
  border: 1px solid rgba(0, 255, 255, 0.3);
  animation: pulse 2s infinite ease-in-out;
}

/* 优化题目导航 */
.question-nav {
  background: rgba(2, 7, 30, 0.8);
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 0 25px rgba(0, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.eye-care .question-nav {
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid rgba(76, 175, 80, 0.3);
  box-shadow: 0 0 25px rgba(76, 175, 80, 0.15);
}

.nav-items {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80rpx, 1fr));
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.nav-item {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 255, 255, 0.1);
  border: 1px solid rgba(0, 255, 255, 0.3);
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nav-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 255, 255, 0.2);
}

.nav-item.current {
  background: rgba(0, 255, 255, 0.3);
  border-color: #00ffff;
  color: #00ffff;
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.3);
}

.nav-item.answered {
  background: rgba(0, 255, 255, 0.2);
  border-color: rgba(0, 255, 255, 0.5);
}

/* 统一按钮样式 */
.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20rpx;
  margin-top: 40rpx;
}

.action-buttons button {
  padding: 16rpx 32rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 10rpx;
  border: 1px solid rgba(0, 255, 255, 0.3);
  white-space: nowrap;
  min-width: 100rpx;
}

.prev-button,
.next-button {
  background: rgba(0, 255, 255, 0.1);
  color: #00ffff;
}

.prev-button:hover, .next-button:hover {
  background: rgba(0, 255, 255, 0.2);
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.2);
  transform: translateY(-2px);
}

.prev-button:disabled {
  background: linear-gradient(90deg, #232a3a 0%, #3a3f4b 100%) !important;
  color: #888 !important;
  cursor: not-allowed !important;
  border: 1px solid #444 !important;
  box-shadow: none !important;
  opacity: 1 !important;
}

.submit-exam-button {
  background: linear-gradient(135deg, #00ffff 0%, #00ccff 100%) !important;
  color: #02071E !important;
  font-weight: bold !important;
  padding: 16rpx 40rpx !important;
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.3);
  border: none !important;
}

.submit-exam-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 25px rgba(0, 255, 255, 0.4);
}

.flag-button {
  background: transparent;
  color: #00ffff;
  border: 1px solid rgba(0, 255, 255, 0.3);
}


.flag-button.flagged .custom-flag-icon {
  animation: flagWave 0.6s ease-in-out;
}

.flag-button.flagged .custom-flag-icon::after {
  background: linear-gradient(45deg, currentColor, rgba(0, 255, 255, 0.8));
  border-left-color: transparent;
  background-clip: content-box;
}

@keyframes flagWave {
  0%, 100% { transform: rotate(0deg) scale(1); }
  25% { transform: rotate(-8deg) scale(1.05); }
  75% { transform: rotate(8deg) scale(1.05); }
}

/* 优化题目内容区域 */
.question-content {
  background: rgba(2, 7, 30, 0.6);
  border-radius: 16rpx;
  padding: 50rpx;
  margin-top: 30rpx;
  border: 1px solid rgba(0, 255, 255, 0.2);
  box-shadow: 0 0 30px rgba(0, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.question-header {
  margin-bottom: 30rpx;
  padding-bottom: 20rpx;
  border-bottom: 1px solid rgba(0, 255, 255, 0.2);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-type {
  color: #00ffff;
  font-size: 28rpx;
  padding: 8rpx 20rpx;
  background: rgba(0, 255, 255, 0.1);
  border-radius: 20rpx;
  border: 1px solid rgba(0, 255, 255, 0.3);
}

.question-number {
  color: #A0AEC0;
  font-size: 28rpx;
}

.question-text {
  font-size: 32rpx;
  line-height: 1.6;
  margin-bottom: 40rpx;
  color: #fff;
}

/* 选项样式优化 */
.options-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx;
  background: rgba(0, 255, 255, 0.05);
  border: 1px solid rgba(0, 255, 255, 0.2);
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.3s ease;
}

.option-item:hover {
  background: rgba(0, 255, 255, 0.1);
  transform: translateX(5px);
}

.option-item.selected {
  background: rgba(0, 255, 255, 0.2);
  border-color: #00ffff;
}

.option-marker {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 255, 255, 0.1);
  border-radius: 50%;
  font-size: 24rpx;
  color: #00ffff;
}

/* 添加动画效果 */
@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-30px, 30px); }
}

@keyframes particleFloat {
  0% { transform: translateY(0); }
  100% { transform: translateY(-40px); }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}

@keyframes glow {
  0%, 100% { box-shadow: 0 0 20px rgba(0, 255, 255, 0.3); }
  50% { box-shadow: 0 0 30px rgba(0, 255, 255, 0.5); }
}

/* 优化填空题输入框 */
.blank-input {
  width: 100%;
  padding: 20rpx;
  background: rgba(0, 255, 255, 0.05);
  border: 1px solid rgba(0, 255, 255, 0.3);
  border-radius: 8rpx;
  color: #fff;
  font-size: 28rpx;
  transition: all 0.3s ease;
}

.blank-input:focus {
  background: rgba(0, 255, 255, 0.1);
  border-color: #00ffff;
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.2);
}

/* 优化场景题文本框 */
.scenario-textarea {
  width: 100%;
  min-height: 300rpx;
  padding: 20rpx;
  background: rgba(0, 255, 255, 0.05);
  border: 1px solid rgba(0, 255, 255, 0.3);
  border-radius: 8rpx;
  color: #fff;
  font-size: 28rpx;
  line-height: 1.6;
  resize: vertical;
  transition: all 0.3s ease;
}

.scenario-textarea:focus {
  background: rgba(0, 255, 255, 0.1);
  border-color: #00ffff;
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.2);
}

/* AI加载效果优化 */
.ai-loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(2, 7, 30, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(10px);
}

.ai-loading-content {
  text-align: center;
  color: #00ffff;
}

.ai-spinner {
  width: 100rpx;
  height: 100rpx;
  border: 4rpx solid rgba(0, 255, 255, 0.3);
  border-top-color: #00ffff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20rpx;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.ai-loading-text {
  font-size: 32rpx;
  animation: pulse 2s infinite ease-in-out;
}

@keyframes horizontalScan {
  0% { top: 0; opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { top: 100%; opacity: 0; }
}

@keyframes verticalScan {
  0% { left: 0; opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { left: 100%; opacity: 0; }
}

.completion-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.completion-card {
  background: rgba(2, 7, 30, 0.7);
  border-radius: 25rpx;
  padding: 80rpx;
  text-align: center;
  box-shadow: 0 0 40rpx rgba(0, 255, 255, 0.2);
  max-width: 2000rpx;
  width: 100%;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 255, 255, 0.3);
}

.eye-care .completion-card {
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid rgba(76, 175, 80, 0.3);
  box-shadow: 0 0 40rpx rgba(76, 175, 80, 0.2);
}

@keyframes circle-pulse {
  0%, 100% { 
    box-shadow: 
      0 0 20px rgba(0, 255, 255, 0.8),
      inset 0 0 20px rgba(0, 255, 255, 0.1);
  }
  50% { 
    box-shadow: 
      0 0 30px rgba(0, 255, 255, 1),
      inset 0 0 30px rgba(0, 255, 255, 0.2);
  }
}

@keyframes line-draw {
  0% {
    height: 0;
    opacity: 0;
  }
  100% {
    height: 55rpx;
    opacity: 1;
  }
}

@keyframes pulse-glow {
  0%, 100% { 
    filter: brightness(1) drop-shadow(0 0 20px rgba(0, 255, 255, 0.8));
  }
  50% { 
    filter: brightness(1.2) drop-shadow(0 0 30px rgba(0, 255, 255, 1));
  }
}

.completion-card h2 {
  color: #E2E8F0;
  font-size: 40rpx;
  margin-bottom: 40rpx;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.3);
}

.eye-care .completion-card h2 {
  color: #2e7d32;
  font-size: 42rpx;
  font-weight: 600;
  text-shadow: 0 0 12rpx rgba(76, 175, 80, 0.4);
  letter-spacing: 1rpx;
}

.completion-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30rpx;
  margin-bottom: 50rpx;
}

.stat-item {
  text-align: center;
}

.stat-label {
  color: #A0AEC0;
  font-size: 28rpx;
  margin-bottom: 10rpx;
}

.stat-value {
  color: #00ffff;
  font-size: 40rpx;
  font-weight: 700;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.4);
}

.completion-actions {
  display: flex;
  gap: 20rpx;
  justify-content: center;
}

.review-button, .complete-button {
  flex: 1;
  padding: 20rpx;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: none;
}

.review-button {
  background: rgba(0, 255, 255, 0.05);
  color: #00ffff;
  border: 2px solid rgba(0, 255, 255, 0.3);
  box-shadow: 0 0 10rpx rgba(0, 255, 255, 0.1);
}

.review-button:hover {
  background: rgba(0, 255, 255, 0.1);
  transform: translateY(-3rpx);
  box-shadow: 0 0 20rpx rgba(0, 255, 255, 0.2);
}

.complete-button {
  background: linear-gradient(45deg, #00ffff, #0080ff);
  color: #02071E;
  box-shadow: 0 0 15rpx rgba(0, 255, 255, 0.2);
}

.complete-button:hover {
  background: linear-gradient(45deg, #33ffff, #33a0ff);
  transform: translateY(-3rpx);
  box-shadow: 0 0 25rpx rgba(0, 255, 255, 0.3);
}

.questions-review {
  margin-top: 30rpx;
  width: 100%;
}

.question-review-item {
  background: rgba(2, 7, 30, 0.7);
  border-radius: 24rpx;
  padding: 36rpx;
  margin-bottom: 36rpx;
  border: 1px solid rgba(0, 255, 255, 0.2);
  max-width: 90%;
  margin-left: auto;
  margin-right: auto;
  }

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
  padding-bottom: 16rpx;
  border-bottom: 1px solid rgba(0, 255, 255, 0.2);
  }

  .question-content {
  margin-top: 16rpx;
  }

.user-answer, .correct-answer {
  margin-top: 12rpx;
  }

.analysis-section {
  margin-top: 20px;
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  padding: 15px;
}

.analysis-button {
  background-color: #00c8ff;
  border: none;
  border-radius: 8px;
  color: #000;
  padding: 16px 32px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 18px;
  white-space: nowrap;
  min-width: 200px;
}

.analysis-button:hover {
  background-color: #00b0e0;
}

.analysis-button.loading {
  background-color: #666;
  cursor: not-allowed;
}

.analysis-content {
  background-color: rgba(0, 0, 0, 0.3);
  border-radius: 8px;
  padding: 15px;
}

.analysis-title {
  font-weight: bold;
  color: #00c8ff;
  margin-bottom: 10px;
  font-size: 16px;
}

.analysis-text {
  color: #e0e0e0;
  line-height: 1.6;
  white-space: pre-line;
  font-size: 14px;
  text-align: left;
}

/* 全局AI加载遮罩 */
.ai-global-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(2, 7, 30, 0.92);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(8px);
  }

.ai-global-loading-content {
  text-align: center;
  color: #00ffff;
  }

.option.correct {
  color: #00ff99;
  font-weight: bold;
}
.option.chosen {
  border-bottom: 2px solid #00ffff;
  }

.ai-error-mask {
  position: fixed;
  z-index: 1000;
  left: 0; top: 0; right: 0; bottom: 0;
  background: rgba(10, 20, 40, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  }
.ai-error-card {
  background: rgba(30, 40, 60, 0.95);
  border-radius: 18px;
  box-shadow: 0 4px 32px #00ffff44;
  padding: 40px 48px 32px 48px;
  min-width: 340px;
  text-align: center;
  }
.ai-error-title {
  font-size: 1.5rem;
  color: #00ffff;
  margin-bottom: 18px;
  font-weight: bold;
  letter-spacing: 2px;
  }
.ai-error-message {
  color: #fff;
  margin-bottom: 28px;
  font-size: 1.1rem;
  word-break: break-all;
}
.retry-btn {
  background: linear-gradient(90deg, #00ffff 0%, #00bfff 100%);
  color: #222;
  border: none;
  border-radius: 8px;
  padding: 10px 36px;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.2s;
  box-shadow: 0 2px 12px #00ffff33;
  outline: none;
  position: relative;
  }
.retry-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  }
.loading-spinner {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid #00ffff;
  border-top: 2px solid transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  vertical-align: middle;
  margin-right: 8px;
  }
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 护眼模式样式 */
.eye-care {
  background: linear-gradient(135deg, #f0f2e6 0%, #e8ecd9 50%, #dfe3d0 100%) !important;
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

.eye-care .custom-flag-icon::before {
  background: #2e7d32;
  box-shadow: 0 0 2px rgba(76, 175, 80, 0.3);
}

.eye-care .custom-flag-icon::after {
  border-left-color: #2e7d32;
  filter: drop-shadow(0 0 2px rgba(76, 175, 80, 0.3));
}

.eye-care .flag-button:hover .custom-flag-icon::before {
  box-shadow: 0 0 4px rgba(76, 175, 80, 0.5);
}

.eye-care .flag-button:hover .custom-flag-icon::after {
  filter: drop-shadow(0 0 4px rgba(76, 175, 80, 0.5));
}

.eye-care .question-content {
  background: rgba(248, 252, 248, 0.95);
  border: 2px solid rgba(76, 175, 80, 0.3);
  color: #2e7d32;
  box-shadow: 0 4rpx 20rpx rgba(76, 175, 80, 0.1);
}

.eye-care .question-type {
  color: #1b5e20;
  background: rgba(76, 175, 80, 0.2);
  border: 2px solid rgba(76, 175, 80, 0.4);
  font-weight: 600;
}

.eye-care .option {
  background: rgba(248, 252, 248, 0.95);
  border: 2px solid rgba(76, 175, 80, 0.3);
  color: #2e7d32;
  transition: all 0.3s ease;
}

.eye-care .option:hover {
  background: rgba(76, 175, 80, 0.15);
  border-color: rgba(76, 175, 80, 0.6);
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.2);
}

.eye-care .option.correct {
  color: #1b5e20;
  background: rgba(76, 175, 80, 0.2);
  border-color: rgba(76, 175, 80, 0.6);
  font-weight: 600;
}

.eye-care .option.chosen {
  border-bottom-color: #1b5e20;
  border-bottom-width: 3px;
}

.eye-care .nav-item {
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid rgba(76, 175, 80, 0.4);
  color: #2e7d32;
  font-weight: 600;
  font-size: 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(76, 175, 80, 0.2);
}

.eye-care .nav-item:hover {
  background: rgba(76, 175, 80, 0.2);
  border-color: rgba(76, 175, 80, 0.7);
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.3);
  color: #1b5e20;
}

.eye-care .nav-item.current {
  background: rgba(76, 175, 80, 0.4);
  border-color: rgba(76, 175, 80, 0.8);
  color: #ffffff;
  box-shadow: 0 0 20rpx rgba(76, 175, 80, 0.5);
  font-weight: 700;
  font-size: 32rpx;
}

.eye-care .nav-item.answered {
  background: rgba(76, 175, 80, 0.5);
  border-color: rgba(76, 175, 80, 0.8);
  color: #ffffff;
  font-weight: 700;
  font-size: 32rpx;
  box-shadow: 0 0 15rpx rgba(76, 175, 80, 0.4);
}

.eye-care .nav-item.flagged {
  background: rgba(255, 193, 7, 0.5);
  border-color: rgba(255, 193, 7, 0.8);
  color: #ffffff;
  font-weight: 700;
  font-size: 32rpx;
  box-shadow: 0 0 15rpx rgba(255, 193, 7, 0.4);
}

.eye-care .question-header {
  background: rgba(248, 252, 248, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
  color: #2e7d32;
}

.eye-care .question-number {
  color: #1b5e20;
  font-weight: 600;
}

.eye-care .question-text {
  color: #2e7d32;
  font-weight: 500;
  line-height: 1.8;
}

.eye-care .option-item {
  background: rgba(248, 252, 248, 0.95);
  border: 2px solid rgba(76, 175, 80, 0.3);
  color: #2e7d32;
  transition: all 0.3s ease;
}

.eye-care .option-item:hover {
  background: rgba(76, 175, 80, 0.15);
  border-color: rgba(76, 175, 80, 0.6);
  transform: translateX(5rpx);
  box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.2);
}

.eye-care .option-item.selected {
  background: rgba(76, 175, 80, 0.3);
  border-color: rgba(76, 175, 80, 0.7);
  color: #1b5e20;
  font-weight: 600;
  box-shadow: 0 0 15rpx rgba(76, 175, 80, 0.3);
}

.eye-care .option-item.correct {
  background: rgba(76, 175, 80, 0.25);
  border-color: rgba(76, 175, 80, 0.6);
  color: #1b5e20;
  font-weight: 600;
}

.eye-care .option-item.incorrect {
  background: rgba(244, 67, 54, 0.25);
  border-color: rgba(244, 67, 54, 0.6);
  color: #d32f2f;
  font-weight: 600;
}

.eye-care .option-marker {
  background: rgba(76, 175, 80, 0.4);
  color: #ffffff;
  border: 2px solid rgba(76, 175, 80, 0.6);
  font-weight: 700;
  font-size: 26rpx;
  box-shadow: 0 2rpx 8rpx rgba(76, 175, 80, 0.3);
}

.eye-care .option-text {
  color: #2e7d32;
  font-weight: 500;
}

.eye-care .blank-input {
  background: rgba(240, 248, 240, 0.95);
  border: 2px solid rgba(76, 175, 80, 0.4);
  color: #2e7d32;
  border-radius: 12rpx;
  padding: 24rpx;
  font-size: 30rpx;
  text-align: center;
  display: block;
  margin: 0 auto;
  max-width: 80%;
  transition: all 0.3s ease;
}

.eye-care .blank-input:focus {
  border-color: rgba(76, 175, 80, 0.8);
  box-shadow: 0 0 15rpx rgba(76, 175, 80, 0.4);
  background: rgba(255, 255, 255, 0.98);
  transform: scale(1.02);
}

.eye-care .blank-input::placeholder {
  color: rgba(46, 125, 50, 0.6);
  text-align: center;
}

.eye-care .scenario-textarea {
  background: rgba(240, 248, 240, 0.95);
  border: 2px solid rgba(76, 175, 80, 0.4);
  color: #2e7d32;
  border-radius: 12rpx;
  padding: 30rpx;
  font-size: 30rpx;
  line-height: 1.8;
  text-align: center;
  display: block;
  margin: 0 auto;
  max-width: 90%;
  min-height: 350rpx;
  resize: vertical;
  transition: all 0.3s ease;
}

.eye-care .scenario-textarea:focus {
  border-color: rgba(76, 175, 80, 0.8);
  box-shadow: 0 0 20rpx rgba(76, 175, 80, 0.4);
  background: rgba(255, 255, 255, 0.98);
  transform: scale(1.01);
}

.eye-care .scenario-textarea::placeholder {
  color: rgba(46, 125, 50, 0.6);
  text-align: center;
}

.eye-care .code-editor {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
}

.eye-care .editor-header {
  background: rgba(76, 175, 80, 0.15);
  border-color: rgba(76, 175, 80, 0.3);
}

.eye-care .language-selector select {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
  color: #333;
}

.eye-care .action-button {
  background: rgba(76, 175, 80, 0.3);
  border-color: rgba(76, 175, 80, 0.6);
  color: #2e7d32;
}

.eye-care .action-button:hover {
  background: rgba(76, 175, 80, 0.5);
  border-color: rgba(76, 175, 80, 0.8);
  color: #1b5e20;
}

.eye-care .code-textarea {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
  color: #333;
}

.eye-care .output-panel {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
}

.eye-care .output-header {
  background: rgba(76, 175, 80, 0.15);
  color: #2e7d32;
}

.eye-care .output-content {
  color: #333;
}

.eye-care .flag-button {
  background: rgba(255, 193, 7, 0.3);
  border-color: rgba(255, 193, 7, 0.6);
  color: #f57c00;
}

.eye-care .flag-button:hover {
  background: rgba(255, 193, 7, 0.5);
  border-color: rgba(255, 193, 7, 0.8);
  color: #e65100;
}

.eye-care .flag-button.flagged {
  background: rgba(255, 193, 7, 0.5);
  border-color: rgba(255, 193, 7, 0.8);
  color: #e65100;
}

.eye-care .prev-button,
.eye-care .next-button {
  background: rgba(76, 175, 80, 0.3);
  border-color: rgba(76, 175, 80, 0.6);
  color: #2e7d32;
}

.eye-care .prev-button:hover,
.eye-care .next-button:hover {
  background: rgba(76, 175, 80, 0.5);
  border-color: rgba(76, 175, 80, 0.8);
  color: #1b5e20;
}

.eye-care .submit-exam-button {
  background: linear-gradient(45deg, #4caf50, #2e7d32) !important;
  color: #fff !important;
  box-shadow: 0 0 15px rgba(76, 175, 80, 0.3) !important;
}

.eye-care .submit-exam-button:hover {
  background: linear-gradient(45deg, #66bb6a, #388e3c) !important;
  box-shadow: 0 0 20px rgba(76, 175, 80, 0.4) !important;
}

.eye-care .completion-card {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
  color: #333;
}

.eye-care .completion-icon {
  color: #2e7d32;
}

.eye-care .stat-label {
  color: #666;
}

.eye-care .stat-value {
  color: #2e7d32;
  text-shadow: 0 0 10px rgba(76, 175, 80, 0.3);
}

.eye-care .question-review-item {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
  color: #333;
}

.eye-care .question-review-item .question-header {
  border-color: rgba(76, 175, 80, 0.3);
}

.eye-care .question-review-item .question-number {
  color: #2e7d32;
}

.eye-care .question-review-item .question-type {
  color: #2e7d32;
}

.eye-care .question-review-item .question-score {
  color: #2e7d32;
}

.eye-care .question-review-item .question-text {
  color: #333;
}

.eye-care .user-answer,
.eye-care .correct-answer {
  color: #333;
}

.eye-care .analysis-section {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
}

.eye-care .analysis-button {
  background: #4caf50;
  color: #fff;
  border-radius: 8px;
  padding: 16px 32px;
  font-size: 18px;
  white-space: nowrap;
  min-width: 200px;
}

.eye-care .analysis-button:hover {
  background: #66bb6a;
}

.eye-care .analysis-content {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
}

.eye-care .analysis-title {
  color: #2e7d32;
}

.eye-care .analysis-text {
  color: #333;
}

.eye-care .complete-button {
  background: linear-gradient(45deg, #4caf50, #2e7d32) !important;
  color: #fff !important;
  box-shadow: 0 0 15px rgba(76, 175, 80, 0.3) !important;
}

.eye-care .complete-button:hover {
  background: linear-gradient(45deg, #66bb6a, #388e3c) !important;
  box-shadow: 0 0 20px rgba(76, 175, 80, 0.4) !important;
}

.eye-care .ai-loading-mask {
  background: rgba(240, 242, 230, 0.92);
}

.eye-care .ai-loading-content {
  color: #2e7d32;
}

.eye-care .ai-loading-text {
  color: #2e7d32;
}

.eye-care .ai-error-mask {
  background: rgba(240, 242, 230, 0.7);
}

.eye-care .ai-error-card {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
}

.eye-care .ai-error-title {
  color: #2e7d32;
}

.eye-care .ai-error-message {
  color: #333;
}

.eye-care .retry-btn {
  background: linear-gradient(90deg, #4caf50, #2e7d32) !important;
  color: #fff !important;
  box-shadow: 0 0 12px rgba(76, 175, 80, 0.3) !important;
}

.eye-care .retry-btn:hover {
  background: linear-gradient(90deg, #66bb6a, #388e3c) !important;
}

.eye-care .loading-spinner {
  border-color: #2e7d32;
  border-top-color: transparent;
}

.eye-care .ai-global-mask {
  background: rgba(240, 242, 230, 0.92);
}

.eye-care .ai-global-loading-content {
  color: #2e7d32;
}

.eye-care .timer {
  color: #2e7d32;
}

.eye-care .timer i {
  color: #2e7d32;
}
</style>

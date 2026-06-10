<template>
  <div v-if="customLoading" class="ai-loading-mask" :class="{ 'eye-care': isEyeCareMode }">
    <div class="ai-loading-content">
      <div class="ai-spinner"></div>
      <div class="ai-loading-text">{{ customLoadingText }}</div>
      
      <!-- 面试小建议 -->
      <div v-if="currentTip" class="interview-tip-container">
        <div class="tip-card">
          <div class="tip-header">
            <span class="tip-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2l2.5 5.2 5.7.7-4.2 3.8 1.2 5.6L12 15.8 6.8 17.3 8 11.7 3.8 7.9l5.7-.7L12 2z" stroke="currentColor" stroke-width="1.8" fill="none"/>
                <circle cx="12" cy="12" r="9.2" stroke="currentColor" stroke-width="1.2" opacity="0.3"/>
              </svg>
            </span>
            <span class="tip-title">{{ currentTip.title }}</span>
          </div>
          <div class="tip-content">{{ currentTip.content }}</div>
          <div class="tip-progress">
            <div class="tip-dots">
              <span 
                v-for="dotIdx in 3"
                :key="dotIdx"
                class="tip-dot"
                :class="{ active: getDotActive(dotIdx) }"
              ></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div class="scene-assessment-container" :class="{ 'eye-care': isEyeCareMode }">
    <!-- 动态背景和效果 -->
    <div class="sci-fi-background">
      <div class="grid-overlay"></div>
      <div class="radial-glow"></div>
      <div class="scanning-line"></div>
      <div class="floating-particles"></div>
    </div>

    <!-- 主内容区 -->
    <div class="content-wrapper">
      <!-- 顶部导航 -->
      <div class="top-nav">
        <div class="timer">
          <SmartIcon name="fas fa-clock" />
          <span>{{ formatTime(remainingTime) }}</span>
        </div>
      </div>

      <!-- 场景评测内容 -->
      <div class="scene-assessment-card" v-if="!isComplete && currentQuestion">
        <div class="card-header">
          <h2 class="card-title">场景评测</h2>
          <p class="card-subtitle">请根据以下工作场景进行语音回答，系统将评估您的应变能力与表达能力</p>
        </div>

        <div class="scene-display">
          <div class="progress-indicator">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: `${(currentQuestionIndex + 1) / totalQuestions * 100}%` }"></div>
            </div>
            <div class="question-number">场景 {{ currentQuestionIndex + 1 }}/{{ totalQuestions }}</div>
          </div>
          
          <div class="scene-card">
            <div class="scene-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="5" y="7" width="14" height="11" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M9 7V6a3 3 0 0 1 3-3h0a3 3 0 0 1 3 3v1" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M5 11h14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="scene-text">
              {{ currentQuestion.text }}
            </div>
          </div>
        </div>

        <div class="recorder-section">
          <div class="audio-visualizer" :class="{ 'recording': isRecording }">
            <div class="bar" v-for="n in 30" :key="n" :style="{ height: `${(Math.random() * 70) + 10}px` }"></div>
          </div>
          <div class="recording-status-wrapper">
            <div class="recording-timer" v-if="isRecording">
              <div class="custom-recording-indicator">
                <div class="recording-dot"></div>
              </div>
              录音中... {{ formatTime(recordedTime) }}
            </div>
            <div class="recording-status" v-if="!isRecording && audioPath">
              <SmartIcon name="fas fa-check" /> 已录制 {{ formatTime(audioDuration) }}
            </div>
          </div>

          <div class="controls">
            <button 
              class="control-button record-toggle-button"
              :class="{ 'start': !isRecording, 'stop': isRecording }"
              @click="isRecording ? stopRecording() : startRecording()"
              :disabled="isProcessing"
            >
              <div class="custom-button-icon">
                <svg v-if="!isRecording" width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="9" y="5" width="6" height="10" rx="3" stroke="currentColor" stroke-width="2"/>
                  <path d="M6 11v1a6 6 0 0 0 12 0v-1" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M12 22v-3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="7" y="7" width="10" height="10" rx="2" stroke="currentColor" stroke-width="2"/>
                </svg>
              </div>
              {{ isRecording ? '停止录音' : '开始录音' }}
            </button>
          </div>
        </div>

        <div class="action-buttons">
          <button 
            class="next-button glow-effect"
            @click="nextQuestion"
            :disabled="!questions[currentQuestionIndex].audioFile || isProcessing"
          >
            <span class="button-text">{{ currentQuestionIndex < totalQuestions - 1 ? '下一个场景' : '提交答案' }}</span>
            <span class="glow-container">
              <span class="glow"></span>
            </span>
          </button>
        </div>
      </div>
      <div v-else-if="!isComplete && !currentQuestion" class="scene-loading-fallback">
        题目加载中...
      </div>

      <!-- 完成页面 -->
      <div class="completion-page" v-if="isComplete">
        <div class="completion-card">
          <div class="completion-header">
            <h2>场景评测</h2>
          </div>
          
          <div class="result-summary">
            
            
            <div class="completion-stats">
              <div class="stat-item time" style="grid-area: time;">
                <div class="stat-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="13" r="7" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 13V9M12 13L16 11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    <path d="M10 4h4M12 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                </div>
                <div class="stat-details">
                  <div class="stat-label">耗时</div>
                  <div class="stat-value">{{ formatTime(totalTime) }}</div>
                </div>
              </div>
              <div class="stat-item highlight total" style="grid-area: total;">
                <div class="stat-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 2l2.8 6 6.2.8-4.5 4.1 1.3 6.1L12 16.8 6.2 19.9 7.5 13.9 3 8.8l6.2-.8L12 2z" stroke="currentColor" stroke-width="2" fill="none"/>
                  </svg>
                </div>
                <div class="stat-details">
                  <div class="stat-label">总评分</div>
                  <div class="stat-value">{{ overallScore }}分</div>
                </div>
              </div>
              <div class="stat-item fluency" style="grid-area: fluency;">
                <div class="stat-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 4a4 4 0 0 1 4 4v3a4 4 0 0 1-8 0V8a4 4 0 0 1 4-4Z" stroke="currentColor" stroke-width="2"/>
                    <path d="M6 11v1a6 6 0 0 0 12 0v-1" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                </div>
                <div class="stat-details">
                  <div class="stat-label">流畅度得分</div>
                  <div class="stat-value">{{ fluencyScore }}分</div>
                </div>
              </div>
              <div class="stat-item emotion" style="grid-area: emotion;">
                <div class="stat-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 21s-6-4.5-8-7.5C2.2 11.5 3 8.5 5.5 7.5S9 8 12 11c3-3 5.5-3.5 7.5-3.5s3.3 4 1 6.5C18 16.5 12 21 12 21Z" stroke="currentColor" stroke-width="2" fill="none"/>
                  </svg>
                </div>
                <div class="stat-details">
                  <div class="stat-label">情感得分</div>
                  <div class="stat-value">{{ emotionScore }}分</div>
                </div>
              </div>
              <div class="stat-item relevance" style="grid-area: relevance;">
                <div class="stat-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="7" stroke="currentColor" stroke-width="2"/>
                    <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 5v2M12 17v2M5 12h2M17 12h2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                </div>
                <div class="stat-details">
                  <div class="stat-label">相关性得分</div>
                  <div class="stat-value">{{ relevanceScore }}分</div>
                </div>
              </div>
              <div class="stat-item adaptability" style="grid-area: adaptability;">
                <div class="stat-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M4 6h3l3 5-3 5H4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M20 6l-4 3M20 18l-4-3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                </div>
                <div class="stat-details">
                  <div class="stat-label">适应性得分</div>
                  <div class="stat-value">{{ adaptabilityScore }}分</div>
                </div>
              </div>
            </div>

            <div class="overall-key-box" v-if="aiAssessment.overallComment || (aiAssessment.keyImprovements && aiAssessment.keyImprovements.length)">
              <h3 class="assessment-title">综合评价与改进点</h3>
              <div class="overall-comment" v-if="aiAssessment.overallComment">{{ aiAssessment.overallComment }}</div>
              <div class="key-improvements" v-if="aiAssessment.keyImprovements && aiAssessment.keyImprovements.length">
                <div class="improvement-item" v-for="(improvement, index) in aiAssessment.keyImprovements" :key="index">
                  <div class="improvement-icon">
                    <SmartIcon name="fas fa-lightbulb" />
                  </div>
                  <div class="improvement-text">{{ improvement }}</div>
                </div>
              </div>
            </div>
            
            
            <!-- 新增：题目详细分析 -->
            <div class="question-analysis" v-if="aiAssessment.questions && aiAssessment.questions.length > 0">
              <h3 class="assessment-title">详细分析</h3>
              
              <div class="question-card" v-for="(q, qIndex) in aiAssessment.questions" :key="qIndex">
                <div class="question-header" @click="toggleQuestion(qIndex)">
                  <div class="question-title">问题 {{ qIndex + 1 }}</div>
                  <div class="question-score">{{ q.score || 0 }}分</div>
                  <div class="question-toggle">
                    <SmartIcon :name="expandedQuestionIndex === qIndex ? 'fas fa-chevron-up' : 'fas fa-chevron-down'" />
                  </div>
                </div>
                
                <div class="question-details" v-show="expandedQuestionIndex === qIndex">
                  <div class="question-content">{{ q.question }}</div>
                  <div class="answer-content">
                    <div class="answer-label">您的回答：</div>
                    <div class="answer-text">{{ q.userAnswer || '(无回答)' }}</div>
                  </div>
                  
                  <div class="feedback-section">
                    <div class="feedback-title">点评：</div>
                    <div class="feedback-content">{{ q.comment }}</div>
                    
                    <div class="feedback-strengths" v-if="q.strengths && q.strengths.length">
                      <div class="feedback-subtitle">优势：</div>
                      <ul class="feedback-list">
                        <li v-for="(strength, sIndex) in q.strengths" :key="sIndex">{{ strength }}</li>
                      </ul>
                    </div>
                    
                    <div class="feedback-weaknesses" v-if="q.weaknesses && q.weaknesses.length">
                      <div class="feedback-subtitle">不足：</div>
                      <ul class="feedback-list">
                        <li v-for="(weakness, wIndex) in q.weaknesses" :key="wIndex" class="weakness-item">{{ weakness }}</li>
                      </ul>
                    </div>
                    
                    <div class="feedback-improvement" v-if="q.improvement">
                      <div class="feedback-subtitle">改进建议：</div>
                      <div class="improvement-content">{{ q.improvement }}</div>
                    </div>
                    
                    
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="completion-actions">
            <button class="complete-button full-width" @click="goToMainComprehensivePage">
              <SmartIcon name="fas fa-check" /> 完成场景评测
            </button>
          </div>
        </div>
        <div class="ai-assessment-result" v-if="aiAssessmentResult && showRawJson">
          <pre>{{ aiAssessmentResult }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineOptions({ inheritAttrs: false })
import { ref, computed, onMounted, onBeforeUnmount, onUnmounted, watch, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { generateScenarioQuestionByAi, analyzeComprehensiveScenarioAudio, synthesizeSpeech, uploadAudioFile, createTranscriptionTask, queryTranscriptionTask, saveComprehensiveScenarioHistory, BASE_URL } from '@/utils/api';
import { getUserSession } from '@/utils/user-session';
import SmartIcon from '@/components/SmartIcon.vue';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
import { getAudioProcessorWorkletUrl } from '@/utils/audio-processor';
import {
  ensureActiveComprehensiveAssessmentSession,
  normalizeComprehensiveState,
  updateComprehensiveAssessmentState
} from '@/utils/comprehensive-assessment';


const router = useRouter();
const route = useRoute();

// 状态变量
const remainingTime = ref(1800); // 30分钟
const isComplete = ref(false);
const isRecording = ref(false);
const isProcessing = ref(false);
const audioPath = ref('');
const recordedTime = ref(0);
const audioDuration = ref(0);
const currentQuestionIndex = ref(0);
const totalQuestions = ref(5); // 5个场景题
const scenarioAngles = [
  '需求澄清与沟通',
  '跨部门协作与推进',
  '故障/突发问题应急',
  '方案权衡与取舍理由',
  '复盘改进与风险控制'
];
const totalTime = ref(0);
const startTime = ref(Date.now());
const customLoading = ref(false);
const customLoadingText = ref('AI生成中...');
const uploadStep = ref(null); // 上传步骤标识：uploading, transcribing, assessing
const showRawJson = ref(false); // 是否显示原始JSON
const expandedQuestionIndex = ref(-1); // 当前展开的问题索引

// 面试小建议相关
const currentTip = ref('');
const tipIndex = ref(0);
const tipInterval = ref(null);

// 评测结果
const fluencyScore = ref(0);
const emotionScore = ref(0);
const relevanceScore = ref(0);
const adaptabilityScore = ref(0); // 新增应变能力评分
const overallScore = ref(0);

const questions = ref([]);
const userAskInput = ref('');
const aiAssessmentResult = ref(''); // 原始AI返回
const aiAssessment = ref({ // 结构化结果
  dimensions: {},
  pronunciation: {},
  overallComment: '',
  questions: [],
  totalScore: 0
});
const assessmentId = ref('');





// 计算属性
const currentQuestion = computed(() => questions.value[currentQuestionIndex.value]);

const buildInterviewAiReturnUrl = (extraQuery = {}) => {
  const query = [];
  const shouldAutoOpen = String(route.query.autoOpen || '').trim() === '1';
  query.push(`autoOpen=${shouldAutoOpen ? '1' : '0'}`);
  let safeSessionId = '';
  if (shouldAutoOpen) {
    safeSessionId = String(route.query.chatId || '').trim();
    if (!safeSessionId) {
      try {
        safeSessionId = String(sessionStorage.getItem('activeAiConversationChatId') || '').trim();
      } catch (_) {
        safeSessionId = '';
      }
    }
  }
  const safeJobId = String(route.query.jobId || '').trim();
  if (safeSessionId) {
    query.push(`chatId=${encodeURIComponent(safeSessionId)}`);
  }
  if (safeJobId) {
    query.push(`jobId=${encodeURIComponent(safeJobId)}`);
  }
  if (assessmentId.value) {
    query.push(`assessmentId=${encodeURIComponent(assessmentId.value)}`);
  }
  Object.entries(extraQuery).forEach(([key, value]) => {
    const safeValue = String(value ?? '').trim();
    if (safeValue) {
      query.push(`${encodeURIComponent(key)}=${encodeURIComponent(safeValue)}`);
    }
  });
  return `/pages/interview-ai/index${query.length ? `?${query.join('&')}` : ''}`;
};

const shouldReturnToAiChat = () => {
  return String(route.query.from || '').trim() === 'ai-interview'
    || String(route.query.autoOpen || '').trim() === '1';
};

const buildComprehensiveReportUrl = () => {
  const safeJobId = String(route.query.jobId || '').trim();
  const query = [];
  if (safeJobId) {
    query.push(`jobId=${encodeURIComponent(safeJobId)}`);
  }
  if (assessmentId.value) {
    query.push(`assessmentId=${encodeURIComponent(assessmentId.value)}`);
  }
  query.push('type=overall');
  const returnToAi = shouldReturnToAiChat();
  query.push(`from=${encodeURIComponent(returnToAi ? 'ai-interview' : 'interview-main')}`);
  query.push(`autoOpen=${returnToAi ? '1' : '0'}`);
  if (returnToAi) {
    const safeSessionId = String(route.query.chatId || '').trim();
    if (safeSessionId) {
      query.push(`chatId=${encodeURIComponent(safeSessionId)}`);
    }
  }
  return `/pages/comprehensive-report/index?${query.join('&')}`;
};

const initializeAssessmentSession = () => {
  const safeJobId = String(route.query.jobId || getJobIdFromUrlOrStorage() || '').trim();
  if (!safeJobId) {
    assessmentId.value = '';
    return null;
  }
  const session = ensureActiveComprehensiveAssessmentSession(safeJobId, {
    assessmentId: route.query.assessmentId || assessmentId.value || '',
    createIfMissing: true
  });
  assessmentId.value = String(session?.assessmentId || '').trim();
  if (!assessmentId.value) {
    return session;
  }
  return updateComprehensiveAssessmentState(safeJobId, assessmentId.value, (currentState) => {
    const nextState = normalizeComprehensiveState(currentState);
    if (!nextState.audio.completed) {
      nextState.audio.inProgress = true;
      nextState.audio.startTime = nextState.audio.startTime || new Date().toISOString();
    }
    nextState.overall.startTime = nextState.overall.startTime || new Date().toISOString();
    if (nextState.overall.status === 'not_started') {
      nextState.overall.status = 'in_progress';
    }
    return nextState;
  });
};

// 方法
const formatTime = (seconds) => {
  const minutes = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

let recorderManager = null;
let innerTimer = null;
let mediaRecorder = null;
let audioChunks = [];

let currentAudio = null; // 顶部声明，供全局访问

// 面试小建议数据
const interviewTips = [
  {
    icon: '🎯',
    title: '面试小贴士',
    content: '面试前深呼吸3次，能有效缓解紧张情绪，让你的声音更加稳定自信！'
  },
  {
    icon: '💡',
    title: '表达技巧',
    content: '回答问题时，先总结要点，再详细展开，这样逻辑更清晰，面试官更容易理解。'
  },
  {
    icon: '😊',
    title: '表情管理',
    content: '保持适度的微笑，眼神要自然交流，避免过度紧张或过于放松的表情。'
  },
  {
    icon: '🗣️',
    title: '语速控制',
    content: '重要内容可以适当放慢语速，给面试官思考和记录的时间，展现你的专业素养。'
  },
  {
    icon: '🎭',
    title: '应变技巧',
    content: '遇到不熟悉的问题时，可以诚实表达，然后分享相关的经验或学习能力。'
  },
  {
    icon: '🌟',
    title: '自信展现',
    content: '用"我"开头表达观点，展现个人能力和成就，但要注意谦虚和客观。'
  },
  {
    icon: '📝',
    title: '准备建议',
    content: '面试前准备3-5个能体现你核心能力的具体案例，关键时刻能派上大用场！'
  },
  {
    icon: '🎪',
    title: '互动技巧',
    content: '适当与面试官互动，比如点头确认理解，提问澄清，展现你的沟通能力。'
  },
  {
    icon: '⚡',
    title: '时间管理',
    content: '回答问题时注意时间控制，核心观点1-2分钟，详细说明3-5分钟最合适。'
  },
  {
    icon: '🎨',
    title: '个性化表达',
    content: '在专业的基础上，适当展现个人特色和独特见解，让面试官记住你！'
  },
  {
    icon: '🎤',
    title: '录音技巧',
    content: '录音时保持适当距离，避免喷麦，语速要均匀，停顿要自然。'
  },
  {
    icon: '🔊',
    title: '音量控制',
    content: '保持适中的音量，既不要太小听不清，也不要太大显得急躁。'
  },
  {
    icon: '🎵',
    title: '语调变化',
    content: '适当运用语调变化，重点内容可以稍微提高音调，增加表达力。'
  },
  {
    icon: '⏱️',
    title: '准备时间',
    content: '听到问题后，可以稍作思考再回答，这比匆忙回答更显得专业。'
  },
  {
    icon: '🎪',
    title: '场景适应',
    content: '根据不同的面试场景调整表达方式，正式场合要严谨，轻松场合可以活泼一些。'
  },
  {
    icon: '🧠',
    title: '思维整理',
    content: '回答前在心里快速整理思路，用STAR法则：情境、任务、行动、结果。'
  },
  {
    icon: '🎭',
    title: '情绪管理',
    content: '保持积极的心态，把面试当作一次展示自己的机会，而不是考试。'
  },
  {
    icon: '📊',
    title: '数据支撑',
    content: '用具体的数据和例子支撑你的观点，比如"我提高了30%的效率"。'
  },
  {
    icon: '🎯',
    title: '目标明确',
    content: '回答要围绕问题核心，避免跑题，确保每个观点都服务于你的主要论点。'
  },
  {
    icon: '🌟',
    title: '亮点突出',
    content: '在回答中突出你的独特优势，让面试官记住你的与众不同之处。'
  },
  {
    icon: '🤝',
    title: '团队合作',
    content: '强调团队合作能力，用"我们"和"我"结合，展现个人能力和团队精神。'
  },
  {
    icon: '📈',
    title: '成长思维',
    content: '展现你的学习能力和成长潜力，让面试官看到你的发展空间。'
  },
  {
    icon: '🎪',
    title: '压力应对',
    content: '遇到压力问题时，保持冷静，用具体例子说明你的抗压能力。'
  },
  {
    icon: '💼',
    title: '职业规划',
    content: '清晰表达你的职业规划，让面试官看到你的目标和决心。'
  },
  {
    icon: '🎨',
    title: '创新思维',
    content: '展现你的创新能力和解决问题的独特方法，让面试官眼前一亮。'
  }
];

// 随机打乱建议数组的函数
function shuffleTips(tips) {
  const shuffled = [...tips];
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  return shuffled;
}

const hasEffectiveAnswer = (value) => {
  const normalized = String(value || '').trim();
  if (!normalized) {
    return false;
  }
  return !/^(?:\(无回答\)|无回答|未作答|暂无回答|跳过|不知道|不清楚)$/i.test(normalized);
};

// 确保显示所有5个题目的函数
function ensureAllQuestions(aiQuestions) {
  const allQuestions = [];
  
  // 确保有5个题目
  for (let i = 0; i < 5; i++) {
    // 查找AI返回的题目中是否有对应的题目
    const aiQuestion = aiQuestions.find(q => {
      // 通过题目内容匹配，或者通过索引匹配
      return q.question === questions.value[i]?.text || 
             q.userAnswer === questions.value[i]?.userAnswer;
    });
    
    if (aiQuestion) {
      // 如果找到AI分析的题目，使用AI的结果
      allQuestions.push({
        question: questions.value[i]?.text || `场景题${i+1}`,
        userAnswer: hasEffectiveAnswer(questions.value[i]?.userAnswer) ? questions.value[i]?.userAnswer : '',
        score: aiQuestion.score || 0,
        comment: aiQuestion.comment || '暂无点评',
        strengths: aiQuestion.strengths || [],
        weaknesses: aiQuestion.weaknesses || [],
        improvement: aiQuestion.improvement || '暂无改进建议',
        facialTips: aiQuestion.facialTips || '保持自然表情',
        rateTips: aiQuestion.rateTips || '语速适中'
      });
    } else {
      // 如果没有找到AI分析的题目，创建默认题目
      allQuestions.push({
        question: questions.value[i]?.text || `场景题${i+1}`,
        userAnswer: hasEffectiveAnswer(questions.value[i]?.userAnswer) ? questions.value[i]?.userAnswer : '',
        score: 0,
        comment: '该题目未进行分析',
        strengths: [],
        weaknesses: [],
        improvement: '暂无改进建议',
        facialTips: '保持自然表情',
        rateTips: '语速适中'
      });
    }
  }
  
  return allQuestions;
}

// 面试小建议相关方法
const shuffledTips = ref([]);

const startTipRotation = () => {
  if (tipInterval.value) {
    clearInterval(tipInterval.value);
  }
  
  // 每次开始轮播时重新随机打乱建议顺序
  shuffledTips.value = shuffleTips(interviewTips);
  
  // 立即显示第一个建议
  currentTip.value = shuffledTips.value[0];
  tipIndex.value = 0;
  
  // 每3秒切换一个建议
  tipInterval.value = setInterval(() => {
    tipIndex.value = (tipIndex.value + 1) % shuffledTips.value.length;
    currentTip.value = shuffledTips.value[tipIndex.value];
  }, 3000);
};

const stopTipRotation = () => {
  if (tipInterval.value) {
    clearInterval(tipInterval.value);
    tipInterval.value = null;
  }
  currentTip.value = '';
};

// 初始化录音管理器
const initRecorder = () => {
  // #ifndef H5
  recorderManager = uni.getRecorderManager();

  recorderManager.onStart(() => {
    console.log('录音开始');
    isRecording.value = true;
    recordedTime.value = 0;
    innerTimer = setInterval(() => {
      recordedTime.value++;
    }, 1000);
  });

  recorderManager.onStop((res) => {
    console.log('录音停止', res);
    isRecording.value = false;
    clearInterval(innerTimer);
    audioPath.value = res.tempFilePath;
    // 获取音频时长 (这里需要一个真实的方法获取时长，或者从后端返回)
    // 模拟获取时长
    uni.getBackgroundAudioManager().onCanplay(() => {
      audioDuration.value = Math.floor(uni.getBackgroundAudioManager().duration);
    });
    uni.getBackgroundAudioManager().src = audioPath.value;
    
    // 小程序/APP端：确保音频文件为wav格式
    // 由于已经设置了 format: 'wav'，这里直接使用 tempFilePath
    // 但需要确保上传时文件扩展名和MIME类型正确
    if (res.tempFilePath) {
      // 将文件路径转换为Blob，确保MIME类型为audio/wav
      uni.getFileSystemManager().readFile({
        filePath: res.tempFilePath,
        success: (readRes) => {
          const wavBlob = new Blob([readRes.data], { type: 'audio/wav' });
          questions.value[currentQuestionIndex.value].audioFile = wavBlob;
        },
        fail: (err) => {
          console.error('读取录音文件失败:', err);
          // 如果读取失败，仍然使用原始文件路径
          questions.value[currentQuestionIndex.value].audioFile = res.tempFilePath;
        }
      });
    }
  });

  recorderManager.onError((res) => {
    console.error('录音错误', res);
    isRecording.value = false;
    clearInterval(innerTimer);
    uni.showToast({ title: '录音失败，请重试', icon: 'none' });
  });
  // #endif
};

const startRecording = async () => {
  // #ifdef H5
  if (isProcessing.value) return;
  
  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      audio: {
        sampleRate: 16000,
        channelCount: 1,
        echoCancellation: true,
        noiseSuppression: true
      }
    });
    
    const audioContext = new AudioContext({
      sampleRate: 16000
    });

    await audioContext.audioWorklet.addModule(getAudioProcessorWorkletUrl());
    const source = audioContext.createMediaStreamSource(stream);
    
    const processor = new AudioWorkletNode(audioContext, 'audio-processor');
    
    const localAudioChunks = [];
    let isRecordingInternal = false;
    
    processor.port.onmessage = (event) => {
      if (isRecordingInternal && event.data.audioData) {
        const inputData = event.data.audioData;
        const pcmData = new Int16Array(inputData.length);
        for (let i = 0; i < inputData.length; i++) {
          pcmData[i] = Math.max(-32768, Math.min(32767, Math.round(inputData[i] * 32767)));
        }
        localAudioChunks.push(pcmData);
      }
    };
    
    source.connect(processor);
    processor.connect(audioContext.destination);
    
    mediaRecorder = {
      stream,
      audioContext,
      processor,
      source,
      audioChunks: localAudioChunks,
      start: () => {
        localAudioChunks.length = 0;
        isRecordingInternal = true;
      },
      stop: () => {
        isRecordingInternal = false;
        
        const totalLength = localAudioChunks.reduce((acc, chunk) => acc + chunk.length, 0);
        const mergedPCM = new Int16Array(totalLength);
        let offset = 0;
        for (const chunk of localAudioChunks) {
          mergedPCM.set(chunk, offset);
          offset += chunk.length;
        }
        
        const wavBlob = pcmToWav(mergedPCM, {
          sampleRate: 16000,
          numChannels: 1,
          bitDepth: 16
        });
        
        audioPath.value = URL.createObjectURL(wavBlob);
        questions.value[currentQuestionIndex.value].audioFile = wavBlob;
      }
    };
    
    mediaRecorder.start();
    isRecording.value = true;
    recordedTime.value = 0;
    innerTimer = setInterval(() => { recordedTime.value++; }, 1000);
  } catch (e) {
    console.error('Error accessing microphone:', e);
    uni.showToast({ title: '无法获取麦克风权限', icon: 'none' });
  }
  // #endif

  // #ifndef H5
  // 原有小程序/APP端逻辑
  audioPath.value = '';
  recordedTime.value = 0;
  audioDuration.value = 0;
  recorderManager.start({
    format: 'wav',
    duration: 120000
  });
  // #endif
};

// stopRecording 只保存音频，不转写
const stopRecording = () => {
  // #ifdef H5
  if (mediaRecorder && isRecording.value) {
    mediaRecorder.stop();
    isRecording.value = false;
    clearInterval(innerTimer);
    audioDuration.value = recordedTime.value;
    // H5端：audioPath 和转写在 stop 里已处理
    // 音频格式转换在 stop 回调中已完成
  }
  // #endif

  // #ifndef H5
  recorderManager.stop();
  // #endif
};

// PCM转WAV工具函数
function pcmToWav(pcmData, options) {
  const numChannels = options.numChannels || 1;
  const sampleRate = options.sampleRate || 16000;
  const bitDepth = options.bitDepth || 16;
  const bytesPerSample = bitDepth / 8;
  const blockAlign = numChannels * bytesPerSample;
  const buffer = new ArrayBuffer(44 + pcmData.length * bytesPerSample);
  const view = new DataView(buffer);
  writeString(view, 0, 'RIFF');
  view.setUint32(4, 36 + pcmData.length * bytesPerSample, true);
  writeString(view, 8, 'WAVE');
  writeString(view, 12, 'fmt ');
  view.setUint32(16, 16, true);
  view.setUint16(20, 1, true);
  view.setUint16(22, numChannels, true);
  view.setUint32(24, sampleRate, true);
  view.setUint32(28, sampleRate * blockAlign, true);
  view.setUint16(32, blockAlign, true);
  view.setUint16(34, bitDepth, true);
  writeString(view, 36, 'data');
  view.setUint32(40, pcmData.length * bytesPerSample, true);
  const offset = 44;
  if (bitDepth === 16) {
    for (let i = 0; i < pcmData.length; i++) {
      view.setInt16(offset + i * 2, pcmData[i], true);
    }
  }
  return new Blob([buffer], { type: 'audio/wav' });
}
function writeString(view, offset, string) {
  for (let i = 0; i < string.length; i++) {
    view.setUint8(offset + i, string.charCodeAt(i));
  }
}

// base64转Blob工具函数（参考interview-scenario）
const base64ToBlob = (base64, mimeType) => {
  const byteCharacters = atob(base64);
  const byteArrays = [];

  for (let offset = 0; offset < byteCharacters.length; offset += 512) {
    const slice = byteCharacters.slice(offset, offset + 512);
    const byteNumbers = new Array(slice.length);
    
    for (let i = 0; i < slice.length; i++) {
      byteNumbers[i] = slice.charCodeAt(i);
    }
    
    const byteArray = new Uint8Array(byteNumbers);
    byteArrays.push(byteArray);
  }

  return new Blob(byteArrays, { type: mimeType });
};

// 上传并转写音频函数
async function uploadAndTranscribeAudio(audioFile, silent = false) {
  try {
    if (!audioFile) {
      throw new Error('音频文件为空');
    }

    if (!silent) {
      uploadStep.value = 'uploading';
      isProcessing.value = true;
    }

    // 使用原生fetch直接调用接口
    const token = uni.getStorageSync('token');
    if (!token) {
      throw new Error('请先登录');
    }

    // 步骤1: 上传音频文件
    const formData = new FormData();
    
    // 确保上传的是wav格式文件，使用base64上传（参考interview-scenario）
    if (audioFile instanceof Blob) {
      // 如果是Blob对象，转换为base64上传
      const reader = new FileReader();
      const base64Promise = new Promise((resolve, reject) => {
        reader.onload = () => {
          const base64data = reader.result.split(',')[1];
          resolve(base64data);
        };
        reader.onerror = reject;
      });
      reader.readAsDataURL(audioFile);
      const base64data = await base64Promise;
      
      const audioBlob = base64ToBlob(base64data, 'audio/wav');
      formData.append('file', audioBlob, 'recording.wav');
    } else if (typeof audioFile === 'string') {
      // 如果是文件路径（小程序端），需要读取文件内容
      // 这里需要特殊处理小程序端的文件路径
      formData.append('file', audioFile);
    } else {
      formData.append('file', audioFile);
    }
    
    const uploadResponse = await fetch(`${BASE_URL}/api/transcription/upload`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    });

    if (!uploadResponse.ok) {
      throw new Error('音频上传失败: ' + uploadResponse.statusText);
    }

    const uploadResult = await uploadResponse.json();
    if (uploadResult.code !== 0) {
      throw new Error(uploadResult.message || '音频上传失败');
    }

    const audioUrl = uploadResult.data.url;
    if (!audioUrl) {
      throw new Error('上传成功但未返回音频URL');
    }

    if (!silent) {
      uploadStep.value = 'transcribing';
    }

    // 步骤2: 创建转写任务
    const taskResponse = await fetch(`${BASE_URL}/api/transcription/create-task`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: `audioUrl=${encodeURIComponent(audioUrl)}`
    });

    if (!taskResponse.ok) {
      throw new Error('创建转写任务失败: ' + taskResponse.statusText);
    }

    const taskResult = await taskResponse.json();
    if (taskResult.code !== 0) {
      throw new Error(taskResult.message || '创建转写任务失败');
    }

    const taskId = taskResult.data.task_id;
    if (!taskId) {
      throw new Error('创建转写任务成功但未返回任务ID');
    }

    // 步骤3: 轮询查询转写结果
    let attempts = 0;
    const maxAttempts = 30; // 最多查询30次，每次间隔2秒
    let transcription = '';

    while (attempts < maxAttempts) {
      const statusResponse = await fetch(`${BASE_URL}/api/transcription/query-task/${taskId}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!statusResponse.ok) {
        throw new Error('查询任务状态失败: ' + statusResponse.statusText);
      }

      const statusResult = await statusResponse.json();
      if (statusResult.code !== 0) {
        throw new Error(statusResult.message || '查询任务状态失败');
      }

      if (statusResult.data.task_status === '3' || statusResult.data.task_status === '4') {
        // 转写完成，处理结果
        transcription = processTranscriptionResult(statusResult.data);
        break;
      } else if (statusResult.data.task_status === '-1') {
        throw new Error('转写任务失败: ' + (statusResult.data.message || '未知错误'));
      }

      await new Promise(resolve => setTimeout(resolve, 2000));
      attempts++;
    }

    if (!transcription && attempts >= maxAttempts) {
      throw new Error('转写超时，请稍后重试');
    }

    if (!silent) {
      isProcessing.value = false;
    }
    
    return transcription;
  } catch (error) {
    console.error('上传并转写音频失败:', error);
    if (!silent) {
      isProcessing.value = false;
    }
    throw error;
  }
}

// 处理转写结果，提取文本
function processTranscriptionResult(result) {
  if (!result.result || !result.result.lattice) {
    return '转写结果为空';
  }

  const textSegments = result.result.lattice
    .map(item => {
      const json = item.json_1best;
      if (!json || !json.st || !json.st.rt) return '';
      
      return json.st.rt
        .map(rt => rt.ws
          .map(ws => ws.cw
            .map(cw => cw.w)
            .join(''))
          .join(''))
        .join('');
    })
    .filter(text => text.length > 0);

  return textSegments.join(' ');
}

// 替换 playQuestionTTS，完全复用 interview-scenario 的实现
async function playQuestionTTS(text) {
  try {
    const token = uni.getStorageSync('token');
    if (!token) {
      throw new Error('请先登录');
    }

    console.log('开始请求语音合成...');
    const response = await fetch(`${BASE_URL}/api/speech/synthesize`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        text,
        vcn: 'x4_yezi',
        pitch: 50,
        speed: 50,
        tte: 'UTF8'
      })
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('语音合成请求失败:', response.status, errorText);
      throw new Error(`语音合成失败: ${response.status} ${errorText}`);
    }

    // 处理JSON响应
    const contentType = response.headers.get('content-type');
    console.log('响应Content-Type:', contentType);

    // 检查是否为JSON响应
    if (!contentType || !contentType.includes('application/json')) {
      const errorText = await response.text();
      console.error('服务器返回了非JSON数据:', errorText);
      throw new Error('服务器返回了非预期的数据格式');
    }

    // 解析JSON响应
    const jsonResponse = await response.json();
    console.log('API响应:', jsonResponse);

    // 检查API响应状态
    if (jsonResponse.code !== 200) {
      console.error('API返回错误:', jsonResponse.message);
      throw new Error(jsonResponse.message || '语音合成失败');
    }

    // 从响应中获取Base64音频数据
    const base64Audio = jsonResponse.data;
    if (!base64Audio) {
      throw new Error('API响应中缺少音频数据');
    }

    // 解码Base64为二进制数据
    const binaryString = atob(base64Audio);
    const arrayBuffer = new ArrayBuffer(binaryString.length);
    const uint8Array = new Uint8Array(arrayBuffer);

    for (let i = 0; i < binaryString.length; i++) {
      uint8Array[i] = binaryString.charCodeAt(i);
    }

    console.log('PCM数据大小:', arrayBuffer.byteLength);

    if (arrayBuffer.byteLength === 0) {
      throw new Error('服务器返回了空的音频数据');
    }

    // 转换PCM为WAV格式
    const wavBlob = pcmToWav(new Int16Array(arrayBuffer), {
      sampleRate: 16000,
      numChannels: 1,
      bitDepth: 16
    });
    console.log('WAV数据大小:', wavBlob.size);

    if (wavBlob.size <= 44) {
      throw new Error('生成的WAV文件没有音频数据');
    }

    return new Promise((resolve, reject) => {
      const audioUrl = URL.createObjectURL(wavBlob);
      console.log('创建音频URL:', audioUrl);

      const audio = new Audio();
      audio.volume = 1.0;

      audio.oncanplaythrough = () => {
        console.log('音频可以播放');
        const playPromise = audio.play();
        if (playPromise !== undefined) {
          playPromise.then(() => {
            console.log('开始播放音频');
          }).catch(error => {
            if (error.name !== 'AbortError') {
              console.error('播放失败:', error);
              reject(error);
            }
          });
        }
      };

      audio.onended = () => {
        console.log('音频播放结束');
        URL.revokeObjectURL(audioUrl);
        resolve();
      };

      audio.onerror = (error) => {
        console.error('音频错误:', error);
        URL.revokeObjectURL(audioUrl);
        reject(new Error('音频播放失败'));
      };

      audio.src = audioUrl;
      audio.load();
    });

  } catch (error) {
    console.error('播放问题失败:', error);
    uni.showToast({
      title: error.message || '播放问题失败',
      icon: 'none',
      duration: 2000
    });
    throw error;
  }
}

// 生成题目音频并返回 audioUrl，不播放 - 参考 interview-scenario 的完整实现
async function generateQuestionAudio(text) {
  try {
    const token = uni.getStorageSync('token');
    if (!token) {
      throw new Error('请先登录');
    }

    console.log('开始请求语音合成...');
    const response = await fetch(`${BASE_URL}/api/speech/synthesize`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        text,
        vcn: 'x4_yezi',
        pitch: 50,
        speed: 50,
        tte: 'UTF8'
      })
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('语音合成请求失败:', response.status, errorText);
      throw new Error(`语音合成失败: ${response.status} ${errorText}`);
    }

    // 处理JSON响应
    const contentType = response.headers.get('content-type');
    console.log('响应Content-Type:', contentType);

    // 检查是否为JSON响应
    if (!contentType || !contentType.includes('application/json')) {
      const errorText = await response.text();
      console.error('服务器返回了非JSON数据:', errorText);
      throw new Error('服务器返回了非预期的数据格式');
    }

    // 解析JSON响应
    const jsonResponse = await response.json();
    console.log('API响应:', jsonResponse);

    // 检查API响应状态
    if (jsonResponse.code !== 200) {
      console.error('API返回错误:', jsonResponse.message);
      throw new Error(jsonResponse.message || '语音合成失败');
    }

    // 从响应中获取Base64音频数据
    const base64Audio = jsonResponse.data;
    if (!base64Audio) {
      throw new Error('API响应中缺少音频数据');
    }

    // 解码Base64为二进制数据
    const binaryString = atob(base64Audio);
    const arrayBuffer = new ArrayBuffer(binaryString.length);
    const uint8Array = new Uint8Array(arrayBuffer);

    for (let i = 0; i < binaryString.length; i++) {
      uint8Array[i] = binaryString.charCodeAt(i);
    }

    console.log('PCM数据大小:', arrayBuffer.byteLength);

    if (arrayBuffer.byteLength === 0) {
      throw new Error('服务器返回了空的音频数据');
    }

    // 转换PCM为WAV格式
    const wavBlob = pcmToWav(new Int16Array(arrayBuffer), {
      sampleRate: 16000,
      numChannels: 1,
      bitDepth: 16
    });
    console.log('WAV数据大小:', wavBlob.size);

    if (wavBlob.size <= 44) {
      throw new Error('生成的WAV文件没有音频数据');
    }

    return URL.createObjectURL(wavBlob);
  } catch (error) {
    console.error('生成题目音频失败:', error);
    throw error;
  }
}

// 修改initQuestions，生成题目时缓存音频
async function initQuestions() {
  const jobInfo = getJobInfo();
  const aiQuestions = [];
  
  // 开始显示面试小建议
  startTipRotation();
  
  for (let i = 0; i < 5; i++) {
    customLoading.value = true;
    customLoadingText.value = `AI正在为你生成第${i+1}道场景题，请稍候…`;
    const angle = scenarioAngles[i % scenarioAngles.length];
    const res = await generateScenarioQuestionByAi({
      jobName: jobInfo.name || '未知岗位',
      jobRequirements: jobInfo.requirements || '无',
      angle
    });
    const text = res.data || `AI场景题${i+1}`;
    let audioUrl = '';
    try {
      audioUrl = await generateQuestionAudio(text);
    } catch (e) {
      audioUrl = '';
    }
    aiQuestions.push({ id: i+1, text, audioUrl, answered: false, userAnswer: '' });
    await new Promise(r => setTimeout(r, 300));
  }
  // 停止面试小建议轮播
  stopTipRotation();
  customLoading.value = false;
  questions.value = aiQuestions;

  // 主动播放第一题音频（如果有）
  if (aiQuestions[0] && aiQuestions[0].audioUrl) {
    if (currentAudio) {
      currentAudio.pause();
      currentAudio = null;
    }
    currentAudio = new Audio(aiQuestions[0].audioUrl);
    currentAudio.play();
  }
}




// nextQuestion 只切题，不做转写
const nextQuestion = async () => {
  if (!questions.value[currentQuestionIndex.value].audioFile) {
    uni.showToast({ title: '请先录音再进行下一步', icon: 'none' });
    return;
  }
  if (currentQuestionIndex.value < totalQuestions.value - 1) {
    currentQuestionIndex.value++;
    audioPath.value = '';
    recordedTime.value = 0;
    audioDuration.value = 0;
  } else {
    handleAssessmentCompletion();
  }
};

// 构建评估载荷的辅助函数 - 用于构造AI分析的请求
function buildAssessmentPayload() {
  return questions.value.map(q => ({
    question: q.text,
    answer: hasEffectiveAnswer(q.userAnswer) ? String(q.userAnswer || '').trim() : '',
    hasAnswer: hasEffectiveAnswer(q.userAnswer)
  }));
}

// handleAssessmentCompletion: 批量转写所有题目音频，全部完成后再分析
const handleAssessmentCompletion = async () => {
  customLoading.value = true;
  customLoadingText.value = 'ai正在综合评分，请稍后...';
  
  // 开始显示面试小建议
  startTipRotation();
  
  try {
    // 严格按顺序转写所有题目的录音
    for (let i = 0; i < questions.value.length; i++) {
      const q = questions.value[i];
      if (q.audioFile) {
        try {
          // 第一步：上传音频
          customLoadingText.value = `ai正在综合评分，请稍后...（正在上传第${i+1}题音频）`;
          
          // 创建转写流程的Promise，确保完全完成后才继续
          await new Promise(async (resolve, reject) => {
            try {
              // 使用原生fetch直接调用接口
              const token = uni.getStorageSync('token');
              if (!token) {
                throw new Error('请先登录');
              }

              // 步骤1: 上传音频文件
              customLoadingText.value = `ai正在综合评分，请稍后...（正在上传第${i+1}题音频）`;
              // 更新建议显示，让用户了解当前进度
              // 注释掉进度提示，保持建议内容纯净
              // if (currentTip.value) {
              //   currentTip.value = {
              //     ...currentTip.value,
              //     content: `${currentTip.value.content} (正在处理第${i+1}题)`
              //   };
              // }
              const formData = new FormData();
              formData.append('file', q.audioFile);
              
              const uploadResponse = await fetch(`${BASE_URL}/api/transcription/upload`, {
                method: 'POST',
                headers: {
                  'Authorization': `Bearer ${token}`
                },
                body: formData
              });

              if (!uploadResponse.ok) {
                throw new Error('音频上传失败: ' + uploadResponse.statusText);
              }

              const uploadResult = await uploadResponse.json();
              if (uploadResult.code !== 0) {
                throw new Error(uploadResult.message || '音频上传失败');
              }

              const audioUrl = uploadResult.data.url;
              if (!audioUrl) {
                throw new Error('上传成功但未返回音频URL');
              }

              // 步骤2: 创建转写任务
              customLoadingText.value = `ai正在综合评分，请稍后...（正在创建第${i+1}题转写任务）`;
              await new Promise(r => setTimeout(r, 500)); // 短暂延迟

              const taskResponse = await fetch(`${BASE_URL}/api/transcription/create-task`, {
                method: 'POST',
                headers: {
                  'Authorization': `Bearer ${token}`,
                  'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `audioUrl=${encodeURIComponent(audioUrl)}`
              });

              if (!taskResponse.ok) {
                throw new Error('创建转写任务失败: ' + taskResponse.statusText);
              }

              const taskResult = await taskResponse.json();
              if (taskResult.code !== 0) {
                throw new Error(taskResult.message || '创建转写任务失败');
              }

              const taskId = taskResult.data.task_id;
              if (!taskId) {
                throw new Error('创建转写任务成功但未返回任务ID');
              }

              // 步骤3: 优化轮询查询转写结果
              customLoadingText.value = `ai正在综合评分，请稍后...（正在转写第${i+1}题音频）`;
              let attempts = 0;
              const maxAttempts = 20; // 减少最大尝试次数
              const initialDelay = 1000; // 初始延迟1秒
              const maxDelay = 3000; // 最大延迟3秒

              while (attempts < maxAttempts) {
                // 使用指数退避策略，减少不必要的频繁查询
                const delay = Math.min(initialDelay * Math.pow(1.2, attempts), maxDelay);
                await new Promise(resolve => setTimeout(resolve, delay));
                
                const statusResponse = await fetch(`${BASE_URL}/api/transcription/query-task/${taskId}`, {
                  headers: {
                    'Authorization': `Bearer ${token}`
                  }
                });

                if (!statusResponse.ok) {
                  throw new Error('查询任务状态失败: ' + statusResponse.statusText);
                }

                const statusResult = await statusResponse.json();
                if (statusResult.code !== 0) {
                  throw new Error(statusResult.message || '查询任务状态失败');
                }

                if (statusResult.data.task_status === '3' || statusResult.data.task_status === '4') {
                  // 转写完成，处理结果
                  q.userAnswer = processTranscriptionResult(statusResult.data);
                  console.log(`题目${i+1}转写完成:`, q.userAnswer);
                  break;
                } else if (statusResult.data.task_status === '-1') {
                  throw new Error('转写任务失败: ' + (statusResult.data.message || '未知错误'));
                }

                attempts++;
                customLoadingText.value = `ai正在综合评分，请稍后...（转写第${i+1}题进度 ${attempts}/${maxAttempts}）`;
                // 更新建议显示进度
              }

              if (!q.userAnswer && attempts >= maxAttempts) {
                throw new Error('转写超时，请稍后重试');
              }
              
              // 完全完成后才解决Promise
              resolve();
  } catch (e) {
              reject(e);
            }
          }).catch(e => {
            console.error(`题目${i+1}转写失败:`, e);
            q.userAnswer = '';
          });

          // 减少题目间的延迟，提高处理速度
          await new Promise(resolve => setTimeout(resolve, 300));
          
        } catch (e) {
          console.error(`题目${i+1}处理错误:`, e);
          q.userAnswer = '';
        }
      } else {
        q.userAnswer = '';
      }
    }

    // 所有转写完成后，开始AI分析
    customLoadingText.value = 'ai正在综合评分，请稍后...';

    // 构造所有题目的payload
    const analysisPayload = buildAssessmentPayload();
    
    // 调用讯飞大模型API，增加重试机制
    let aiResponse = null;
    let retryCount = 0;
    const maxRetries = 2;
    
    while (retryCount <= maxRetries) {
      try {
        customLoadingText.value = `AI正在综合评分，请稍候…${retryCount > 0 ? `(重试${retryCount}/${maxRetries})` : ''}`;
        aiResponse = await analyzeComprehensiveScenarioAudio(analysisPayload);
        
        if (aiResponse && aiResponse.code === 200) {
          break; // 成功获取响应，跳出重试循环
        } else {
          throw new Error(aiResponse?.message || 'AI分析失败');
        }
      } catch (error) {
        retryCount++;
        console.warn(`AI分析第${retryCount}次尝试失败:`, error);
        
        if (retryCount > maxRetries) {
          // 所有重试都失败了，抛出错误
          throw new Error(`AI分析失败: ${error.message || '网络超时'}`);
        }
        
        // 等待一段时间后重试
        await new Promise(resolve => setTimeout(resolve, 2000));
      }
    }
    
    aiAssessmentResult.value = aiResponse.data || '';
    
    let parsedResult = null;
    if (aiResponse.data && typeof aiResponse.data === 'object') {
      parsedResult = aiResponse.data;
    } else {
      console.error('AI返回结果不是对象:', aiResponse.data);
    }
    
    // 验证和标准化解析结果
    if (parsedResult && parsedResult.dimensions) {
      // 确保所有必需字段都存在
      const standardizedResult = {
        totalScore: parsedResult.totalScore ?? 0,
        dimensions: {
          fluency: parsedResult.dimensions.fluency ?? 0,
          relevance: parsedResult.dimensions.relevance ?? 0,
          adaptability: parsedResult.dimensions.adaptability ?? 0,
          professionalism: parsedResult.dimensions.professionalism ?? 0,
          completeness: parsedResult.dimensions.completeness ?? 0
        },
        detailScores: {
          fluency: parsedResult.detailScores?.fluency ?? 0,
          emotion: parsedResult.detailScores?.emotion ?? 0,
          relevance: parsedResult.detailScores?.relevance ?? 0,
          adaptability: parsedResult.detailScores?.adaptability ?? 0
        },
        pronunciation: {
          score: parsedResult.pronunciation?.score ?? 0,
          comment: parsedResult.pronunciation?.comment ?? "发音评价"
        },
        facialExpression: {
          score: parsedResult.facialExpression?.score ?? 0,
          suggestions: parsedResult.facialExpression?.suggestions ?? ["保持自然表情", "展现自信微笑"]
        },
        speechRate: {
          currentRate: parsedResult.speechRate?.currentRate ?? "中",
          suggestions: parsedResult.speechRate?.suggestions ?? ["语速适中", "注意停顿"]
        },
        overallComment: parsedResult.overallComment ?? "整体表现良好",
        questions: ensureAllQuestions(parsedResult.questions ?? []),
        keyImprovements: parsedResult.keyImprovements ?? []
      };

      standardizedResult.questions = standardizedResult.questions.map((item, index) => {
        const hasAnswer = hasEffectiveAnswer(questions.value[index]?.userAnswer || item?.userAnswer || '');
        if (hasAnswer) {
          return item;
        }
        return {
          ...item,
          score: 0,
          comment: '本题未作答，未计分。',
          strengths: [],
          weaknesses: ['未提供有效回答'],
          improvement: '先完成作答，再根据题目核心补充结构化回答。'
        };
      });

      const answeredCount = standardizedResult.questions.filter((item) => hasEffectiveAnswer(item?.userAnswer || '')).length;
      if (answeredCount === 0) {
        standardizedResult.dimensions = {
          fluency: 0,
          relevance: 0,
          adaptability: 0,
          professionalism: 0,
          completeness: 0
        };
        standardizedResult.detailScores = {
          fluency: 0,
          emotion: 0,
          relevance: 0,
          adaptability: 0
        };
        standardizedResult.overallComment = '本次场景评测未检测到有效作答，因此不计分。';
        standardizedResult.keyImprovements = ['先完成每道场景题作答，再查看 AI 点评。'];
      }
      
      aiAssessment.value = standardizedResult;
      const sumScore = (standardizedResult.questions || []).reduce((s, q) => s + Number(q?.score || 0), 0);
      standardizedResult.totalScore = sumScore;
      
      // 更新评分
      fluencyScore.value = standardizedResult.dimensions.fluency;
      relevanceScore.value = standardizedResult.dimensions.relevance;
      adaptabilityScore.value = standardizedResult.dimensions.adaptability;
      emotionScore.value = standardizedResult.detailScores.emotion;
      overallScore.value = Math.max(0, Math.min(999, Math.round(sumScore)));
      
      console.log('AI分析结果解析成功:', standardizedResult);
    } else {
      // 创建默认评估结果，确保图表能显示
      console.warn('AI解析失败，使用默认结果');
      aiAssessment.value = {
        totalScore: 0,
        dimensions: {
          fluency: 0,
          relevance: 0,
          adaptability: 0,
          professionalism: 0,
          completeness: 0
        },
        detailScores: {
          fluency: 0,
          emotion: 0,
          relevance: 0,
          adaptability: 0
        },
        pronunciation: { score: 0, comment: "发音评价" },
        facialExpression: { score: 0, suggestions: ["保持自然表情"] },
        speechRate: { currentRate: "中", suggestions: ["语速适中"] },
        overallComment: '分析完成',
        questions: ensureAllQuestions([]),
        keyImprovements: []
      };
      fluencyScore.value = 0;
      relevanceScore.value = 0;
      adaptabilityScore.value = 0;
      emotionScore.value = 0;
      overallScore.value = 0;
    }
    
    // 停止面试小建议轮播
    stopTipRotation();
    
    // 标记评测完成
    isComplete.value = true;
    totalTime.value = Math.floor((Date.now() - startTime.value) / 1000);

    
    const safeJobId = String(route.query.jobId || getJobIdFromUrlOrStorage() || '').trim();
    const savedSession = updateComprehensiveAssessmentState(safeJobId, assessmentId.value, (currentState) => {
      const nextState = normalizeComprehensiveState(currentState);
      nextState.audio = {
        ...nextState.audio,
        completed: true,
        inProgress: false,
        score: Math.max(0, Math.min(100, parseInt(overallScore.value, 10) || 0)),
        assessmentId: nextState.audio.assessmentId || `scene_assessment_${Date.now()}`,
        startTime: nextState.audio.startTime || new Date(Date.now() - totalTime.value * 1000).toISOString(),
        endTime: new Date().toISOString(),
        attempts: Math.max(1, Number(nextState.audio.attempts || 0) + 1)
      };
      nextState.overall = {
        ...nextState.overall,
        startTime: nextState.overall.startTime || new Date(Date.now() - totalTime.value * 1000).toISOString(),
        status: 'in_progress'
      };
      return nextState;
    });
    assessmentId.value = String(savedSession?.assessmentId || assessmentId.value || '').trim();
  } catch (e) {
    console.error('评测完成处理失败:', e);
    uni.showToast({
      title: '评测处理失败: ' + (e.message || '未知错误'),
      icon: 'none',
      duration: 2000
    });
  } finally {
    // 停止面试小建议轮播
    stopTipRotation();
    customLoading.value = false;
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

const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);

const goToMainComprehensivePage = async () => {
  try {
    // 健壮获取 jobId
    let jobId = (route.query.jobId || getJobIdFromUrlOrStorage() || '').toString().trim();
    const userId = Number((getUserSession() || {}).userId || userStore.userId || 0);
    const timestamp = Date.now();
    const targetUrl = buildComprehensiveReportUrl();
    const savedSession = ensureActiveComprehensiveAssessmentSession(jobId, {
      assessmentId: assessmentId.value,
      createIfMissing: false
    });
    const savedState = normalizeComprehensiveState(savedSession?.state);
    // 组装保存历史参数
    // 使用ISO格式的时间字符串，符合API要求
    const endTime = new Date().toISOString();
    const startTimeISO = savedState.audio?.startTime || new Date(Date.now() - totalTime.value * 1000).toISOString();
    const params = {
      userId: parseInt(userId),
      jobId: parseInt(jobId),
      startTime: startTimeISO,
      endTime: endTime,
      totalTime: parseInt(totalTime.value),
      overallScore: parseInt(overallScore.value),
      fluencyScore: parseInt(fluencyScore.value),
      emotionScore: parseInt(emotionScore.value),
      relevanceScore: parseInt(relevanceScore.value),
      adaptabilityScore: parseInt(adaptabilityScore.value),
      aiAssessment: aiAssessmentResult.value || JSON.stringify(aiAssessment.value)
    };
    try {
      // 验证必要参数
      if (!params.userId || !params.jobId) {
        throw new Error('用户ID或职位ID缺失');
      }
      
      await saveComprehensiveScenarioHistory(params);
    } catch (e) {
      console.error('保存场景评测历史失败:', e);
      uni.showToast({ title: '历史保存失败: ' + (e.message || '未知错误'), icon: 'none', duration: 2000 });
    }
    // 写入 currentJobId
    if (jobId) {
      uni.setStorageSync('currentJobId', jobId);
      try {
        sessionStorage.setItem('currentJobId', jobId);
        localStorage.setItem('currentJobId', jobId);
      } catch (e) {}
    }
    // 弹窗提示成功
    uni.showToast({
      title: '完成场景评测成功',
      icon: 'success',
      duration: 1200
    });
    setTimeout(() => {
      try {
        uni.reLaunch({
          url: targetUrl,
          success: () => {
            uni.hideLoading();
          },
          fail: (err) => {
            uni.hideLoading();
            uni.redirectTo({
              url: targetUrl,
              fail: () => {
                uni.showToast({ title: '导航失败，请手动返回', icon: 'none' });
              }
            });
          }
        });
      } catch (error) {
        setTimeout(() => {
          router.replace(targetUrl);
        }, 2000);
      }
    }, 800);
  } catch (error) {
    uni.showToast({ title: '状态保存失败，请重试', icon: 'error' });
    setTimeout(() => {
      router.replace(buildComprehensiveReportUrl());
    }, 2000);
  }
};

function getJobInfo() {
  // 可根据实际情况从store/props/页面参数获取岗位信息
  // 这里假设从localStorage获取
  try {
    const job = uni.getStorageSync('currentJobInfo');
    if (job) return typeof job === 'string' ? JSON.parse(job) : job;
  } catch (e) {}
  return {};
}

// 定时器
let mainTimer = null;

onMounted(() => {
  initializeAssessmentSession();
  // #ifndef H5
  initRecorder();
  // #endif

  // 启动主计时器
  mainTimer = setInterval(() => {
    if (remainingTime.value > 0 && !isComplete.value) {
      remainingTime.value--;
    } else {
      clearInterval(mainTimer);
      if (!isComplete.value) {
        handleAssessmentCompletion(); // 时间到自动完成评测，并显示报告
      }
    }
  }, 1000);

  initQuestions();
  
});

onBeforeUnmount(() => {
  if (mainTimer) {
    clearInterval(mainTimer);
  }
  if (innerTimer) {
    clearInterval(innerTimer);
  }
  
  // 释放录音资源
  // #ifdef H5
  if (mediaRecorder && isRecording.value) {
    mediaRecorder.stop();
    isRecording.value = false;
  }
  if (mediaRecorder) {
    if (mediaRecorder.source) {
      mediaRecorder.source.disconnect();
    }
    if (mediaRecorder.processor) {
      mediaRecorder.processor.disconnect();
    }
    if (mediaRecorder.audioContext) {
      mediaRecorder.audioContext.close();
    }
    if (mediaRecorder.stream) {
      mediaRecorder.stream.getTracks().forEach(track => track.stop());
    }
  }
  // #endif
  
  // #ifndef H5
  if (recorderManager) {
    recorderManager.stop(); // 确保停止录音
  }
  // #endif
  
  if (currentAudio) {
    currentAudio.pause();
    currentAudio = null;
  }
  
  // 清理所有题目的 audioUrl
  if (questions.value && Array.isArray(questions.value)) {
    questions.value.forEach(q => {
      if (q.audioUrl) {
        URL.revokeObjectURL(q.audioUrl);
        q.audioUrl = '';
      }
    });
  }
  
});

// 额外的清理保障
onUnmounted(() => {
  // 确保所有录音资源都被释放
  // #ifdef H5
  if (mediaRecorder) {
    if (mediaRecorder.stream) {
      mediaRecorder.stream.getTracks().forEach(track => track.stop());
    }
    if (mediaRecorder.audioContext && mediaRecorder.audioContext.state !== 'closed') {
      mediaRecorder.audioContext.close();
    }
  }
  // #endif
  
  // 清理所有音频URL
  if (audioPath.value) {
    URL.revokeObjectURL(audioPath.value);
  }
});

// methods: 新增重录
function reRecord() {
  audioPath.value = '';
  recordedTime.value = 0;
  audioDuration.value = 0;
  questions.value[currentQuestionIndex.value].userAnswer = '';
  questions.value[currentQuestionIndex.value].answered = false;
}

// 题目切换时自动语音播报
watch(currentQuestionIndex, (newIdx) => {
  if (currentAudio) {
    currentAudio.pause();
    currentAudio = null;
  }
  const q = questions.value[newIdx];
  if (q && q.audioUrl) {
    currentAudio = new Audio(q.audioUrl);
    currentAudio.play();
  }
});

function toggleQuestion(index) {
  expandedQuestionIndex.value = expandedQuestionIndex.value === index ? -1 : index;
}


// 新增：小圆点高亮逻辑
function getDotActive(dotIdx) {
  // dotIdx: 1,2,3
  // 只显示3个点，当前建议高亮中间的点
  if (!shuffledTips.value.length) return false;
  if (shuffledTips.value.length <= 3) {
    return tipIndex.value === dotIdx - 1;
  }
  if (tipIndex.value === 0) return dotIdx === 1; // 第一个建议，高亮第1个点
  if (tipIndex.value === shuffledTips.value.length - 1) return dotIdx === 3; // 最后一个建议，高亮第3个点
  return dotIdx === 2; // 其他情况高亮中间点
}
</script>

<style scoped>
/* 基础容器样式 */
.scene-assessment-container {
  min-height: 100vh;
  background: #030b14;
  color: #edf2f7;
  font-family: 'Arial', sans-serif;
  position: relative;
  overflow: hidden;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx;
}

/* 科幻背景效果 */
.sci-fi-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
}

.grid-overlay {
  position: absolute;
  width: 200%;
  height: 200%;
  top: -50%;
  left: -50%;
  background-image: 
    linear-gradient(rgba(0, 245, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 245, 255, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
  transform: perspective(500px) rotateX(60deg);
  animation: grid-move 20s linear infinite;
}

.radial-glow {
  position: absolute;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at center, rgba(0, 128, 255, 0.15) 0%, rgba(0, 0, 0, 0) 70%);
  opacity: 0.7;
}

.scanning-line {
  position: absolute;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, 
    rgba(0, 245, 255, 0) 0%,
    rgba(0, 245, 255, 0.5) 20%,
    rgba(0, 245, 255, 0.8) 50%,
    rgba(0, 245, 255, 0.5) 80%,
    rgba(0, 245, 255, 0) 100%);
  box-shadow: 0 0 10px rgba(0, 245, 255, 0.8);
  top: 0;
  animation: scanning 8s linear infinite;
  z-index: 1;
}

.floating-particles {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(0, 245, 255, 0.1) 1px, transparent 1px),
    radial-gradient(circle at 75% 75%, rgba(0, 245, 255, 0.1) 1px, transparent 1px);
  background-size: 100px 100px;
  animation: float-particles 60s linear infinite;
}

@keyframes grid-move {
  0% { background-position: 0 0; }
  100% { background-position: 40px 40px; }
}

@keyframes scanning {
  0%, 100% { top: 0; opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  0%, 100% { top: 0; }
  50% { top: 100%; }
}

@keyframes float-particles {
  0% { background-position: 0 0; }
  100% { background-position: 100px 100px; }
}



/* 主内容区样式 */
.content-wrapper {
  position: relative;
  z-index: 10;
  max-width: 1100rpx;
  width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 40rpx;
  /* 修正：去掉max-height和overflow，内容自适应高度 */
  max-height: none !important;
  height: auto !important;
  overflow: visible !important;
}

/* 自定义滚动条样式 */
.content-wrapper::-webkit-scrollbar {
  width: 8px;
}

.content-wrapper::-webkit-scrollbar-track {
  background: rgba(0, 245, 255, 0.1);
  border-radius: 4px;
}

.content-wrapper::-webkit-scrollbar-thumb {
  background: rgba(0, 245, 255, 0.3);
  border-radius: 4px;
  transition: background 0.3s ease;
}

.content-wrapper::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 245, 255, 0.5);
}

/* 顶部导航 */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  color: #edf2f7;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 15rpx;
  color: #00f5ff;
  cursor: pointer;
  font-weight: 500;
  font-size: 32rpx;
  transition: all 0.3s ease;
  text-shadow: 0 0 10px rgba(0, 245, 255, 0.5);
}

.back-button:hover {
  text-shadow: 0 0 15px rgba(0, 245, 255, 0.8);
}

/* 自定义返回箭头 */
.back-icon { color: #00f5ff; filter: drop-shadow(0 0 8px rgba(0,245,255,0.5)); transition: transform 0.3s ease; }
.back-button:hover .back-icon { transform: translateX(-2px); }

.timer {
  display: flex;
  align-items: center;
  gap: 15rpx;
  color: #00f5ff;
  font-weight: 500;
  font-size: 32rpx;
  background-color: rgba(0, 245, 255, 0.1);
  padding: 10rpx 20rpx;
  border-radius: 30rpx;
  border: 1px solid rgba(0, 245, 255, 0.3);
  box-shadow: 0 0 15px rgba(0, 245, 255, 0.2);
  backdrop-filter: blur(5px);
}

/* 场景评测卡片 */
.scene-assessment-card {
  background: rgba(5, 20, 35, 0.7);
  border-radius: 20rpx;
  padding: 50rpx;
  box-shadow: 0 0 30rpx rgba(0, 245, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 245, 255, 0.1);
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.card-header {
  text-align: center;
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 48rpx;
  color: #00f5ff;
  margin-bottom: 15rpx;
  font-weight: 600;
  text-shadow: 0 0 15px rgba(0, 245, 255, 0.5);
  letter-spacing: 2px;
}

.card-subtitle {
  font-size: 28rpx;
  color: #a0aec0;
  line-height: 1.6;
}

/* 场景显示区域 */
.scene-display {
  display: flex;
  flex-direction: column;
  gap: 25rpx;
  margin-bottom: 20rpx;
}

.progress-indicator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  padding: 10rpx 0;
}

.progress-bar {
  flex: 1;
  height: 6rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #00f5ff, #0077ff);
  border-radius: 3rpx;
  transition: width 0.5s ease;
  box-shadow: 0 0 10px rgba(0, 245, 255, 0.5);
}

/* 护眼模式样式 */
.eye-care {
  background: linear-gradient(135deg, #f0f2e6 0%, #e8ecd9 50%, #dfe3d0 100%) !important;
}

.eye-care .scene-assessment-container {
  background: linear-gradient(135deg, #f0f2e6 0%, #e8ecd9 50%, #dfe3d0 100%) !important;
}

/* 护眼模式下的进度条样式 */
.eye-care .progress-bar {
  background: rgba(255, 255, 255, 0.3);
}

.eye-care .progress-fill {
  background: linear-gradient(90deg, #4caf50, #2e7d32);
  box-shadow: 0 0 10px rgba(76, 175, 80, 0.5);
}

.eye-care .question-number {
  color: #2e7d32;
  text-shadow: 0 0 10px rgba(76, 175, 80, 0.4);
}

.eye-care .back-button {
  background: transparent;
  border: none;
  color: #2e7d32;
}

.eye-care .back-button:hover {
  background: transparent;
  border: none;
  color: #1b5e20;
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

.eye-care .timer {
  color: #2e7d32;
}

.eye-care .timer i {
  color: #2e7d32;
}

.eye-care .scene-assessment-card {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
  color: #333;
}

.eye-care .card-title {
  color: #2e7d32;
}

.eye-care .card-subtitle {
  color: #666;
}

.eye-care .scene-card {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(76, 175, 80, 0.3);
  color: #333;
}

.eye-care .scene-icon i {
  color: #2e7d32;
  text-shadow: 0 0 15px rgba(76, 175, 80, 0.8);
}

.eye-care .scene-text {
  color: #333;
}

.eye-care .audio-visualizer {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(76, 175, 80, 0.3);
}

.eye-care .audio-visualizer.recording {
  border-color: rgba(76, 175, 80, 0.6);
  box-shadow: 0 0 15px rgba(76, 175, 80, 0.4);
}

.eye-care .audio-visualizer .bar {
  background: rgba(76, 175, 80, 0.6);
}

.eye-care .recording-status-wrapper {
  color: #2e7d32;
}

.eye-care .recording-timer {
  color: #2e7d32;
}

.eye-care .recording-status {
  color: #2e7d32;
}

.eye-care .recording-status i {
  color: #2e7d32;
}

.eye-care .control-button {
  background: rgba(76, 175, 80, 0.3);
  border-color: rgba(76, 175, 80, 0.6);
  color: #2e7d32;
}

.eye-care .control-button:hover {
  background: rgba(76, 175, 80, 0.5);
  border-color: rgba(76, 175, 80, 0.8);
  color: #1b5e20;
}

.eye-care .control-button.start {
  background: rgba(76, 175, 80, 0.3);
  border-color: rgba(76, 175, 80, 0.6);
  color: #2e7d32;
}

.eye-care .control-button.stop {
  background: rgba(244, 67, 54, 0.3);
  border-color: rgba(244, 67, 54, 0.6);
  color: #d32f2f;
}

.eye-care .control-button.start:hover {
  background: rgba(76, 175, 80, 0.5);
  border-color: rgba(76, 175, 80, 0.8);
  color: #1b5e20;
}

.eye-care .control-button.stop:hover {
  background: rgba(244, 67, 54, 0.5);
  border-color: rgba(244, 67, 54, 0.8);
  color: #b71c1c;
}

.eye-care .custom-button-icon {
  color: #2e7d32;
}

.eye-care .control-button.stop .custom-button-icon {
  color: #d32f2f;
}

.eye-care .control-button.start:hover .custom-button-icon {
  color: #1b5e20;
}

.eye-care .control-button.stop:hover .custom-button-icon {
  color: #b71c1c;
}

.eye-care .microphone-icon {
  background: #2e7d32;
  border-color: #2e7d32;
  box-shadow: 0 2rpx 8rpx rgba(46, 125, 50, 0.3);
}

.eye-care .microphone-icon::before {
  border-color: #2e7d32;
}

.eye-care .microphone-icon::after {
  background: #2e7d32;
}

.eye-care .control-button.stop .microphone-icon {
  background: #d32f2f;
  border-color: #d32f2f;
  box-shadow: 0 2rpx 8rpx rgba(211, 47, 47, 0.3);
}

.eye-care .control-button.stop .microphone-icon::before {
  border-color: #d32f2f;
}

.eye-care .control-button.stop .microphone-icon::after {
  background: #d32f2f;
}

.eye-care .playback-controls {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(76, 175, 80, 0.3);
}

.eye-care .playback-button {
  background: rgba(76, 175, 80, 0.3);
  border-color: rgba(76, 175, 80, 0.6);
  color: #2e7d32;
}

.eye-care .playback-button:hover {
  background: rgba(76, 175, 80, 0.5);
  border-color: rgba(76, 175, 80, 0.8);
  color: #1b5e20;
}

.eye-care .playback-button i {
  color: #2e7d32;
}

.eye-care .playback-button:hover i {
  color: #1b5e20;
}

.eye-care .submit-button {
  background: linear-gradient(45deg, #4caf50, #2e7d32) !important;
  color: #fff !important;
  box-shadow: 0 0 15px rgba(76, 175, 80, 0.3) !important;
}

.eye-care .submit-button:hover {
  background: linear-gradient(45deg, #66bb6a, #388e3c) !important;
  box-shadow: 0 0 20px rgba(76, 175, 80, 0.4) !important;
}

.eye-care .completion-page {
  background: rgba(240, 242, 230, 0.95);
  color: #333;
}

.eye-care .completion-card {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
  color: #333;
  box-shadow: 0 0 50rpx rgba(76, 175, 80, 0.2);
}

.eye-care .completion-header h2 {
  color: #2e7d32;
  text-shadow: 0 0 20rpx rgba(76, 175, 80, 0.6);
  font-weight: 700;
  font-size: 54rpx;
}

.eye-care .completion-stats {
  color: #333;
}

.eye-care .stat-item {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
  color: #333;
  box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.1);
}

.eye-care .stat-item:hover {
  background: rgba(76, 175, 80, 0.1);
  border-color: rgba(76, 175, 80, 0.4);
  box-shadow: 0 8rpx 25rpx rgba(76, 175, 80, 0.2);
}

.eye-care .stat-label {
  color: #555;
  font-weight: 600;
}

.eye-care .stat-value {
  color: #2e7d32;
  text-shadow: 0 0 15rpx rgba(76, 175, 80, 0.4);
}

.eye-care .stat-icon {
  color: #2e7d32;
  text-shadow: 0 0 10rpx rgba(76, 175, 80, 0.4);
}


.eye-care .key-assessments {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
}

.eye-care .improvement-item {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
  color: #333;
}

.eye-care .improvement-item:hover {
  background: rgba(76, 175, 80, 0.1);
  border-color: rgba(76, 175, 80, 0.4);
  box-shadow: 0 5rpx 15rpx rgba(76, 175, 80, 0.2);
}

.eye-care .improvement-icon {
  color: #1b5e20;
  text-shadow: 0 0 10rpx rgba(76, 175, 80, 0.4);
}

.eye-care .improvement-text {
  color: #333;
  font-weight: 500;
}

.eye-care .complete-button {
  background: linear-gradient(45deg, #2e7d32, #1b5e20) !important;
  color: #ffffff !important;
  box-shadow: 0 0 20rpx rgba(76, 175, 80, 0.4) !important;
  font-weight: 700;
  text-shadow: 0 0 10rpx rgba(255, 255, 255, 0.3);
  border: 2px solid rgba(76, 175, 80, 0.5) !important;
}

.eye-care .complete-button:hover {
  background: linear-gradient(45deg, #388e3c, #2e7d32) !important;
  box-shadow: 0 0 30rpx rgba(76, 175, 80, 0.6) !important;
  transform: translateY(-3rpx);
}

.eye-care .review-button {
  background: rgba(76, 175, 80, 0.1);
  color: #1b5e20;
  border: 2px solid rgba(76, 175, 80, 0.4);
  font-weight: 600;
}

.eye-care .review-button:hover {
  background: rgba(76, 175, 80, 0.2);
  color: #0d4a0d;
  border-color: rgba(76, 175, 80, 0.6);
  box-shadow: 0 0 20rpx rgba(76, 175, 80, 0.3);
}

.eye-care .overall-assessment {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
  color: #333;
}

.eye-care .assessment-title {
  color: #1b5e20;
  font-weight: 700;
  text-shadow: 0 0 10rpx rgba(76, 175, 80, 0.4);
}

.eye-care .assessment-content {
  color: #333;
}

.eye-care .overall-comment {
  color: #333;
  font-weight: 500;
  line-height: 1.8;
}

.eye-care .suggestion-item {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
  color: #333;
}

.eye-care .suggestion-item:hover {
  background: rgba(76, 175, 80, 0.1);
  border-color: rgba(76, 175, 80, 0.5);
}

.eye-care .suggestion-icon {
  color: #1b5e20;
  text-shadow: 0 0 10rpx rgba(76, 175, 80, 0.4);
}

.eye-care .suggestion-text {
  color: #333;
  font-weight: 500;
}

.eye-care .tip-card {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(76, 175, 80, 0.3);
  color: #333;
  box-shadow: 0 8px 32px rgba(76, 175, 80, 0.2);
}

.eye-care .tip-title {
  color: #2e7d32;
}

.eye-care .tip-content {
  color: #333;
}

.eye-care .tip-icon {
  color: #2e7d32;
  filter: drop-shadow(0 0 10px rgba(76, 175, 80, 0.5));
}

.eye-care .tip-dot {
  background: rgba(76, 175, 80, 0.3);
}

.eye-care .tip-dot.active {
  background: #2e7d32;
  box-shadow: 0 0 10px rgba(76, 175, 80, 0.8);
}

.eye-care .ai-loading-mask {
  background: rgba(240, 242, 230, 0.95);
}

.eye-care .ai-loading-content {
  color: #2e7d32;
}

.eye-care .ai-loading-text {
  color: #2e7d32;
  font-weight: 600;
}

.eye-care .ai-spinner {
  border-color: rgba(76, 175, 80, 0.3);
  border-top-color: #2e7d32;
  box-shadow: 0 0 20px rgba(76, 175, 80, 0.4);
}

/* 护眼模式下的下一个场景按钮样式 */
.eye-care .next-button {
  background: linear-gradient(135deg, #4caf50 0%, #66bb6a 50%, #81c784 100%) !important;
  color: #ffffff !important;
  border: 2px solid rgba(76, 175, 80, 0.6) !important;
  box-shadow: 0 4rpx 15rpx rgba(76, 175, 80, 0.3) !important;
  text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.2) !important;
  font-weight: 600 !important;
}

.eye-care .next-button:hover {
  background: linear-gradient(135deg, #66bb6a 0%, #81c784 50%, #a5d6a7 100%) !important;
  transform: translateY(-3rpx) !important;
  box-shadow: 0 8rpx 25rpx rgba(76, 175, 80, 0.5) !important;
  border-color: rgba(76, 175, 80, 0.8) !important;
}

.eye-care .next-button:disabled {
  background: linear-gradient(135deg, #bdbdbd 0%, #e0e0e0 100%) !important;
  color: #757575 !important;
  border-color: rgba(189, 189, 189, 0.6) !important;
  box-shadow: 0 2rpx 8rpx rgba(189, 189, 189, 0.3) !important;
  cursor: not-allowed !important;
}

.eye-care .next-button .glow {
  background: radial-gradient(circle at center, rgba(76, 175, 80, 0.6) 0%, rgba(129, 199, 132, 0.3) 30%, transparent 70%) !important;
}

.eye-care .next-button:hover .glow {
  opacity: 0.8 !important;
  animation: rotate 4s linear infinite !important;
}

.question-number {
  font-size: 28rpx;
  color: #00f5ff;
  font-weight: 600;
  white-space: nowrap;
  text-shadow: 0 0 10px rgba(0, 245, 255, 0.4);
}

.scene-card {
  background: rgba(10, 30, 50, 0.6);
  border-radius: 15rpx;
  padding: 40rpx;
  display: flex;
  flex-direction: column;
  gap: 25rpx;
  border: 1px solid rgba(0, 245, 255, 0.2);
  box-shadow: 0 0 20px rgba(0, 245, 255, 0.1);
  position: relative;
  overflow: hidden;
}

.scene-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(45deg, transparent, rgba(0, 245, 255, 0.03), transparent);
  animation: shine 3s infinite linear;
  z-index: 1;
}

@keyframes shine {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.scene-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 15rpx;
  position: relative;
  z-index: 2;
}

.scene-icon i {
  font-size: 60rpx;
  color: #00f5ff;
  text-shadow: 0 0 15px rgba(0, 245, 255, 0.8);
}

.scene-text {
  font-size: 34rpx;
  color: #edf2f7;
  line-height: 1.7;
  letter-spacing: 0.5px;
  position: relative;
  z-index: 2;
  text-align: left;
}

/* 录音区域 */
.recorder-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30rpx;
}

.audio-visualizer {
  width: 100%;
  max-width: 600rpx;
  height: 120rpx;
  background: rgba(5, 15, 30, 0.5);
  border-radius: 15rpx;
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  padding: 0 10rpx;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 245, 255, 0.1);
}

.audio-visualizer.recording {
  border-color: rgba(0, 245, 255, 0.5);
  box-shadow: 0 0 20rpx rgba(0, 245, 255, 0.3);
}

.audio-visualizer .bar {
  width: 6rpx;
  background: rgba(160, 174, 192, 0.3);
  border-radius: 3rpx;
  margin: 0 2rpx;
  height: 10rpx;
  transition: height 0.1s ease;
}

.audio-visualizer.recording .bar {
  background: linear-gradient(to top, #00f5ff, #0077ff);
  animation: waveform 1s infinite ease-in-out alternate;
  box-shadow: 0 0 5px rgba(0, 245, 255, 0.8);
}

@keyframes waveform {
  0% { transform: scaleY(0.1); }
  100% { transform: scaleY(1); }
}

/* Ensure each bar moves differently */
.audio-visualizer.recording .bar:nth-child(2n) { animation-delay: 0.1s; }
.audio-visualizer.recording .bar:nth-child(3n) { animation-delay: 0.15s; }
.audio-visualizer.recording .bar:nth-child(4n) { animation-delay: 0.2s; }
.audio-visualizer.recording .bar:nth-child(5n) { animation-delay: 0.25s; }
.audio-visualizer.recording .bar:nth-child(6n) { animation-delay: 0.3s; }

.recording-status-wrapper {
  min-height: 50rpx;
  display: flex;
  justify-content: center;
}

.recording-timer, .recording-status {
  font-size: 32rpx;
  color: #00f5ff;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 18px;
}

.custom-recording-indicator {
  display: inline-flex;
  align-items: center;
  margin-right: 8rpx;
}

.recording-dot {
  width: 12rpx;
  height: 12rpx;
  background: #ff4757;
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(255, 71, 87, 0.8);
  animation: recording-pulse 1s infinite ease-in-out;
}

@keyframes recording-pulse {
  0%, 100% { 
    opacity: 1;
    box-shadow: 0 0 10px rgba(255, 71, 87, 0.8);
  }
  50% { 
    opacity: 0.5;
    box-shadow: 0 0 20px rgba(255, 71, 87, 1);
  }
}

.custom-button-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24rpx;
  height: 24rpx;
  margin-right: 8rpx;
}

.microphone-icon {
  position: relative;
  width: 24rpx;
  height: 24rpx;
  background: currentColor;
  border-radius: 50%;
  border: 2rpx solid currentColor;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
}

.microphone-icon::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8rpx;
  height: 8rpx;
  background: transparent;
  border: 2rpx solid currentColor;
  border-radius: 50%;
}

.microphone-icon::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 4rpx;
  height: 4rpx;
  background: currentColor;
  border-radius: 50%;
}

.stop-icon {
  width: 16rpx;
  height: 16rpx;
  background: currentColor;
  border-radius: 2rpx;
}

/* 控制按钮 */
.controls {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 30rpx;
  margin-top: 20rpx;
}

.control-button {
  padding: 25rpx 50rpx;
  border-radius: 15rpx;
  font-size: 32rpx;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  display: flex;
  align-items: center;
  gap: 15rpx;
  border: none;
  position: relative;
  overflow: hidden;
}

.control-button::after {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #00f5ff, #0077ff, #00f5ff);
  z-index: -1;
  border-radius: 15rpx;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.control-button:hover::after {
  opacity: 1;
}

.record-toggle-button {
  &.start {
    background: rgba(40, 167, 69, 0.2);
    color: #4ade80;
    border: 1px solid rgba(74, 222, 128, 0.3);
  }
  
  &.stop {
    background: rgba(220, 53, 69, 0.2);
    color: #f87171;
    border: 1px solid rgba(248, 113, 113, 0.3);
  }
}

.control-button.play-button {
  background: rgba(0, 245, 255, 0.1);
  color: #00f5ff;
  border: 1px solid rgba(0, 245, 255, 0.3);
}

.control-button:hover:not(:disabled) {
  transform: translateY(-5rpx);
  box-shadow: 0 0 20rpx rgba(0, 245, 255, 0.4);
}

.control-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
  filter: grayscale(0.5);
}

/* 操作按钮区域 */
.action-buttons {
  margin-top: 30rpx;
  text-align: center;
}

.next-button {
  padding: 25rpx 60rpx;
  border-radius: 15rpx;
  font-size: 36rpx;
  font-weight: 700;
  background: linear-gradient(90deg, #0a2540 0%, #003366 100%);
  color: #00f5ff;
  border: 1px solid rgba(0, 245, 255, 0.3);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  overflow: hidden;
  z-index: 1;
}

/* 发光按钮效果 */
.glow-effect {
  position: relative;
}

.glow-effect .button-text {
  position: relative;
  z-index: 1;
}

.glow-container {
  position: absolute;
  display: block;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  overflow: hidden;
}

.glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at center, rgba(0, 245, 255, 0.8) 0%, rgba(0, 119, 255, 0.3) 30%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
  transform-origin: center;
}

.next-button:hover .glow {
  opacity: 1;
  animation: rotate 4s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.next-button:hover {
  transform: translateY(-5rpx);
  background: rgba(0, 245, 255, 0.25);
  box-shadow: 0 0 30rpx rgba(0, 245, 255, 0.5);
  text-shadow: 0 0 10px rgba(0, 245, 255, 0.8);
}

.next-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #222c38;
  color: #aaa;
  border-color: #333;
}

/* 完成页面样式 */
.completion-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  /* 修正：去掉max-height和overflow，内容自适应高度 */
  max-height: none !important;
  height: auto !important;
  overflow: visible !important;
  width: 100%;
  padding: 20rpx;
  border-radius: 0; /* 移除边框圆角 */
  margin: 0;
  box-sizing: border-box;
}

.completion-card {
  background: rgba(5, 20, 35, 0.85);
  border-radius: 30rpx;
  padding: 80rpx;
  box-shadow: 0 0 50rpx rgba(0, 245, 255, 0.25);
  backdrop-filter: blur(25px);
  border: 2px solid rgba(0, 245, 255, 0.2);
  max-width: 900rpx;
  width: 100%;
  position: relative;
  overflow: visible; /* 改为visible，避免裁剪内部内容 */
}

.completion-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -50%;
  width: 200%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 245, 255, 0.5), transparent);
  animation: scan-line 4s linear infinite;
}

.completion-card::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: -50%;
  width: 200%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 245, 255, 0.5), transparent);
  animation: scan-line 4s linear infinite reverse;
}

@keyframes scan-line {
  0% { transform: translateX(-50%); }
  100% { transform: translateX(50%); }
}

.completion-header {
  text-align: center;
  margin-bottom: 40rpx;
}



.completion-header h2 {
  color: #ffffff;
  font-size: 52rpx;
  text-shadow: 0 0 20px rgba(0, 245, 255, 0.6);
  letter-spacing: 3px;
  font-weight: 700;
}

.result-summary {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
  margin-bottom: 50rpx;
}

/* 评测结果统计 */
.completion-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-areas: 'time total emotion' 'relevance fluency adaptability';
  gap: 20rpx;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: rgba(0, 245, 255, 0.1);
  border-radius: 20rpx;
  padding: 30rpx;
  transition: all 0.3s ease;
  border: 2px solid rgba(0, 245, 255, 0.2);
  box-shadow: 0 4rpx 12rpx rgba(0, 245, 255, 0.1);
}

.stat-item:hover {
  background: rgba(0, 245, 255, 0.15);
  transform: translateY(-5rpx);
  box-shadow: 0 8rpx 25rpx rgba(0, 245, 255, 0.25);
  border-color: rgba(0, 245, 255, 0.3);
}

.stat-item.highlight {
  background: rgba(0, 245, 255, 0.2);
  border: 2px solid rgba(0, 245, 255, 0.4);
  box-shadow: 0 0 20rpx rgba(0, 245, 255, 0.3);
}

.stat-icon {
  font-size: 44rpx;
  color: #00f5ff;
  min-width: 44rpx;
  text-align: center;
  text-shadow: 0 0 10rpx rgba(0, 245, 255, 0.5);
}

.stat-details {
  flex: 1;
}

.stat-label {
  color: #ffffff;
  font-size: 26rpx;
  margin-bottom: 10rpx;
  font-weight: 500;
}

.stat-value {
  color: #ffffff;
  font-size: 40rpx;
  font-weight: 700;
  text-shadow: 0 0 15rpx rgba(0, 245, 255, 0.4);
}

.highlight .stat-value {
  color: #00f5ff;
  text-shadow: 0 0 15rpx rgba(0, 245, 255, 0.6);
}

/* 完成操作按钮 */
.completion-actions {
  display: flex;
  gap: 30rpx;
  justify-content: center;
}

.review-button, .complete-button {
  padding: 25rpx;
  border-radius: 15rpx;
  font-size: 32rpx;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  flex: 1;
}

.review-button {
  background: rgba(0, 245, 255, 0.05);
  color: #00f5ff;
  border: 1px solid rgba(0, 245, 255, 0.3);
}

.review-button:hover {
  background: rgba(0, 245, 255, 0.15);
  transform: translateY(-3rpx);
  box-shadow: 0 0 20px rgba(0, 245, 255, 0.3);
}

.complete-button {
  background: rgba(0, 245, 255, 0.2);
  color: #edf2f7;
  border: 1px solid rgba(0, 245, 255, 0.4);
  position: relative;
  overflow: hidden;
}

.complete-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(0, 245, 255, 0.3), transparent);
  animation: shine 3s infinite;
}

.complete-button:hover {
  background: rgba(0, 245, 255, 0.3);
  transform: translateY(-3rpx);
  box-shadow: 0 0 20px rgba(0, 245, 255, 0.4);
}

.complete-button.full-width {
  width: 100%;
  font-size: 18px;
  padding: 15px;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .scene-assessment-container {
    padding: 20rpx;
  }
  
  .content-wrapper {
    gap: 30rpx;
  }
  
  .top-nav {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .timer {
    margin-top: 15rpx;
  }
  
  .scene-assessment-card {
    padding: 40rpx;
    gap: 30rpx;
  }
  
  .card-title {
    font-size: 40rpx;
  }
  
  .scene-text {
    font-size: 30rpx;
  }
  
  .controls {
    flex-direction: column;
    gap: 20rpx;
  }
  
  .control-button {
    width: 100%;
  }
  
  .next-button {
    width: 100%;
    font-size: 32rpx;
  }
  
  .completion-card {
    padding: 30rpx 20rpx; /* 减小左右内边距 */
    margin: 0 auto;
    box-sizing: border-box;
  }
  
  .completion-icon {
    font-size: 80rpx;
  }
  
  .completion-header h2 {
    font-size: 36rpx;
  }
  
  .completion-stats {
    grid-template-columns: 1fr;
    grid-template-areas: 'total' 'time' 'emotion' 'relevance' 'fluency' 'adaptability';
    gap: 15rpx;
  }
  
  .completion-actions {
    flex-direction: column;
    gap: 15rpx;
  }
  
}
.ai-loading-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(2, 7, 30, 0.92);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(8px);
}
.ai-loading-content {
  text-align: center;
  color: #00ffff;
  padding: 40px 48px 32px 48px;
  min-width: 320px;
  background: none;
  box-shadow: none;
  border-radius: 0;
}
.ai-spinner {
  margin: 0 auto 18px auto;
  width: 48px; height: 48px;
  border: 5px solid #00ffff44;
  border-top: 5px solid #00ffff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  box-shadow: 0 0 30px #00ffff88;
}
@keyframes spin {
  0% { transform: rotate(0deg);}
  100% { transform: rotate(360deg);}
}
.ai-loading-text {
  color: #00ffff;
  font-size: 20px;
  font-weight: bold;
  margin-top: 8px;
  letter-spacing: 2px;
}
.scene-loading-fallback {
  color: #00f5ff;
  font-size: 28px;
  text-align: center;
  margin-top: 120px;
  letter-spacing: 2px;
}
.transcribed-answer {
  margin: 18px 0 0 0;
  color: #00ffff;
  font-size: 18px;
  background: rgba(0,255,255,0.08);
  border-radius: 8px;
  padding: 12px 18px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.transcribed-answer button {
  margin-left: 18px;
  background: #00ffff;
  color: #222;
  border: none;
  border-radius: 6px;
  padding: 4px 16px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
}
.transcribed-answer button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.ai-assessment-result {
  background: rgba(0,255,255,0.08);
  color: #00ffff;
  border-radius: 10px;
  padding: 18px 24px;
  margin: 24px 0 0 0;
  font-size: 18px;
  white-space: pre-wrap;
  line-height: 1.7;
}
.pronunciation-score {
  margin: 18px 0 0 0;
  color: #00ffff;
  font-size: 18px;
  display: flex;
  gap: 18px;
  align-items: center;
}
.pronunciation-comment {
  color: #a0eaff;
  font-size: 16px;
  margin-left: 12px;
}
.overall-comment {
  margin: 24px 0 0 0;
  color: #00ffff;
  font-size: 18px;
  background: rgba(0,255,255,0.06);
  border-radius: 8px;
  padding: 12px 18px;
}
.question-review-list {
  margin: 32px 0 0 0;
}
.question-review-item {
  background: rgba(0,255,255,0.04);
  border-radius: 8px;
  padding: 16px 18px;
  margin-bottom: 18px;
}
.question-title {
  color: #00ffff;
  font-weight: bold;
  margin-bottom: 6px;
  cursor: pointer;
  user-select: none;
  display: flex;
  align-items: center;
  gap: 8px;
}
.question-title span {
  font-size: 16px;
  color: #a0eaff;
}
.user-answer {
  color: #fff;
  margin-bottom: 4px;
}
.ai-comment {
  color: #a0eaff;
  margin-bottom: 4px;
}
.ai-answer {
  color: #ffd700;
  margin-top: 4px;
}
.ai-answer button {
  background: #00ffff;
  color: #222;
  border: none;
  border-radius: 6px;
  padding: 4px 16px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
  margin-top: 6px;
}
.ai-answer button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.answer-analysis-panel {
  background: rgba(0,245,255,0.06);
  border-radius: 16px;
  margin: 32px auto 0 auto;
  padding: 32px 24px;
  max-width: 600px;
  box-shadow: 0 2px 24px #00ffff22;
  color: #00f5ff;
}
.answer-analysis-panel h3 {
  color: #00ffff;
  margin-top: 18px;
  margin-bottom: 8px;
  font-size: 20px;
}
.analysis-section {
  margin-bottom: 18px;
  background: rgba(0,245,255,0.04);
  border-radius: 10px;
  padding: 12px 16px;
}

/* 完成页面样式 */
.completion-page {
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

.completion-page::-webkit-scrollbar {
  width: 8px;
}

.completion-page::-webkit-scrollbar-track {
  background: rgba(0, 245, 255, 0.1);
  border-radius: 4px;
}

.completion-page::-webkit-scrollbar-thumb {
  background: rgba(0, 245, 255, 0.3);
  border-radius: 4px;
  transition: background 0.3s ease;
}

.completion-page::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 245, 255, 0.5);
}


/* 新增样式：关键改进点 */
.overall-key-box {
  margin: 40px 0 20px;
  background: rgba(5, 20, 35, 0.6);
  border-radius: 15px;
  padding: 25px;
  border: 1px solid rgba(0, 245, 255, 0.15);
}

.assessment-title {
  color: #00f5ff;
  font-size: 24px;
  margin-bottom: 15px;
  font-weight: 600;
  letter-spacing: 1px;
  text-shadow: 0 0 10px rgba(0, 245, 255, 0.5);
}

.key-improvements {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.improvement-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  background: rgba(0, 245, 255, 0.05);
  border-radius: 10px;
  padding: 15px;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 245, 255, 0.1);
}

.improvement-item:hover {
  background: rgba(0, 245, 255, 0.1);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 245, 255, 0.15);
}

.improvement-icon {
  color: #00f5ff;
  font-size: 18px;
  min-width: 30px;
  padding-top: 3px;
}

.improvement-text {
  color: #edf2f7;
  font-size: 16px;
  line-height: 1.6;
  flex: 1;
}

/* 新增样式：整体评价 */
/* 合并后复用整体评价样式 */

.overall-comment {
  color: #edf2f7;
  font-size: 16px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.overall-key-box .overall-comment {
  margin: 8px 0 18px;
  padding: 14px 16px;
  background: rgba(10, 30, 50, 0.6);
  border-radius: 10px;
  border: 1px solid rgba(0, 245, 255, 0.12);
}
.overall-key-box .key-improvements { margin-top: 6px; }

/* 新增样式：题目详细分析 */
.question-analysis {
  margin: 30px 0;
  background: rgba(5, 20, 35, 0.6);
  border-radius: 15px;
  padding: 25px;
  border: 1px solid rgba(0, 245, 255, 0.15);
}

.question-card {
  background: rgba(10, 30, 50, 0.6);
  border-radius: 10px;
  margin-bottom: 20px;
  border: 1px solid rgba(0, 245, 255, 0.1);
  overflow: hidden;
  transition: all 0.3s ease;
}

.question-card:hover {
  box-shadow: 0 0 20px rgba(0, 245, 255, 0.15);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: rgba(0, 245, 255, 0.05);
  cursor: pointer;
  transition: background 0.3s ease;
}

.question-header:hover {
  background: rgba(0, 245, 255, 0.1);
}

.question-title {
  font-weight: 600;
  color: #edf2f7;
  font-size: 18px;
}

.question-score {
  background: rgba(0, 245, 255, 0.2);
  color: #00f5ff;
  padding: 5px 12px;
  border-radius: 20px;
  font-weight: 600;
}

.question-toggle {
  color: #00f5ff;
  font-size: 16px;
  transition: transform 0.3s ease;
}

.question-details {
  padding: 20px;
  border-top: 1px solid rgba(0, 245, 255, 0.1);
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.question-content {
  color: #00f5ff;
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px dashed rgba(0, 245, 255, 0.2);
}

.answer-content {
  margin-bottom: 20px;
}

.answer-label {
  color: #a0aec0;
  margin-bottom: 10px;
  font-size: 14px;
}

.answer-text {
  color: #edf2f7;
  font-size: 16px;
  line-height: 1.6;
  padding: 15px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  border-left: 3px solid rgba(0, 245, 255, 0.5);
}

.feedback-section {
  margin-top: 25px;
}

.feedback-title {
  color: #00f5ff;
  font-size: 18px;
  margin-bottom: 10px;
  font-weight: 600;
}

.feedback-content {
  color: #edf2f7;
  font-size: 16px;
  line-height: 1.7;
  margin-bottom: 20px;
}

.feedback-subtitle {
  color: #a0aec0;
  font-size: 14px;
  margin: 15px 0 10px;
}

.feedback-list {
  list-style-type: none;
  padding: 0;
  margin: 0 0 15px;
}

.feedback-list li {
  color: #edf2f7;
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 10px;
  padding-left: 24px;
  position: relative;
}

.feedback-list li:before {
  content: '✓';
  color: #4ade80;
  position: absolute;
  left: 0;
  font-weight: bold;
}

.weakness-item:before {
  content: '!';
  color: #f87171;
}

.improvement-content {
  color: #edf2f7;
  font-size: 15px;
  line-height: 1.7;
  padding: 15px;
  background: rgba(0, 245, 255, 0.05);
  border-radius: 8px;
}

/* 新增样式：面部表情建议 */
.facial-expression-assessment {
  margin: 30px 0;
  background: rgba(5, 20, 35, 0.6);
  border-radius: 15px;
  padding: 25px;
  border: 1px solid rgba(0, 245, 255, 0.15);
}

.facial-suggestions {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.facial-content {
  color: #edf2f7;
  font-size: 15px;
  line-height: 1.7;
  padding: 15px;
  background: rgba(255, 193, 7, 0.1);
  border-radius: 8px;
  border-left: 3px solid rgba(255, 193, 7, 0.5);
}

/* 新增样式：语速建议 */
.speech-rate-assessment {
  margin: 30px 0;
  background: rgba(5, 20, 35, 0.6);
  border-radius: 15px;
  padding: 25px;
  border: 1px solid rgba(0, 245, 255, 0.15);
}

.current-rate {
  color: #a0eaff;
  font-size: 14px;
  font-weight: normal;
  margin-left: 10px;
}

.rate-suggestions {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.rate-content {
  color: #edf2f7;
  font-size: 15px;
  line-height: 1.7;
  padding: 15px;
  background: rgba(76, 175, 80, 0.1);
  border-radius: 8px;
  border-left: 3px solid rgba(76, 175, 80, 0.5);
}

/* 新增样式：问题详情中的面部表情和语速建议 */
.feedback-facial,
.feedback-rate {
  margin-top: 20px;
}

.feedback-facial .feedback-subtitle,
.feedback-rate .feedback-subtitle {
  color: #a0aec0;
  font-size: 14px;
  margin: 15px 0 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.feedback-facial .feedback-subtitle i,
.feedback-rate .feedback-subtitle i {
  color: #00f5ff;
  font-size: 16px;
}

.facial-content,
.rate-content {
  color: #edf2f7;
  font-size: 15px;
  line-height: 1.7;
  padding: 15px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  border-left: 3px solid rgba(0, 245, 255, 0.3);
}

/* 建议项样式 */
.suggestion-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  background: rgba(0, 245, 255, 0.05);
  border-radius: 10px;
  padding: 15px;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 245, 255, 0.1);
}

.suggestion-item:hover {
  background: rgba(0, 245, 255, 0.1);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 245, 255, 0.15);
}

.suggestion-icon {
  color: #00f5ff;
  font-size: 18px;
  min-width: 30px;
  padding-top: 3px;
}

.suggestion-text {
  color: #edf2f7;
  font-size: 16px;
  line-height: 1.6;
  flex: 1;
}

/* 面试小建议样式 */
.interview-tip-container {
  margin-top: 30px;
  animation: slideInUp 0.6s ease-out;
}

.tip-card {
  background: rgba(0, 245, 255, 0.1);
  border: 2px solid rgba(0, 245, 255, 0.3);
  border-radius: 20px;
  padding: 25px;
  width: 500px;
  height: 200px;
  margin: 0 auto;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 245, 255, 0.2);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.tip-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 245, 255, 0.3);
}

.tip-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  flex-shrink: 0;
}

.tip-icon {
  font-size: 32px;
  filter: drop-shadow(0 0 10px rgba(0, 245, 255, 0.5));
}

.tip-title {
  color: #00f5ff;
  font-size: 20px;
  font-weight: 600;
  text-shadow: 0 0 10px rgba(0, 245, 255, 0.5);
}

.tip-content {
  color: #edf2f7;
  font-size: 16px;
  line-height: 1.8;
  margin-bottom: 20px;
  text-align: center;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  word-wrap: break-word;
  hyphens: auto;
}

.tip-progress {
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}

.tip-dots {
  display: flex;
  gap: 8px;
}

.tip-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(0, 245, 255, 0.3);
  transition: all 0.3s ease;
}

.tip-dot.active {
  background: #00f5ff;
  box-shadow: 0 0 10px rgba(0, 245, 255, 0.8);
  transform: scale(1.2);
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .tip-card {
    width: 90vw;
    max-width: 400px;
    height: 180px;
    padding: 20px;
  }
  
  .tip-title {
    font-size: 18px;
  }
  
  .tip-content {
    font-size: 14px;
  }
  
  .tip-icon {
    font-size: 28px;
  }
}

/* 优化loading mask样式 */
.ai-loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 20px;
}
</style>

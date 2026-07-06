<template>
  <div class="ai-interview-container dedicated-page" :class="{ 'eye-care': isEyeCareMode }">
    <div id="stars"></div>
    <div id="stars2"></div>
    <div id="stars3"></div>
    <div v-if="isTransitioning" class="page-transition-overlay" :class="{ 'eye-care': isEyeCareMode }"></div>

    <!-- AI助手对话界面 -->
    <div class="ai-assistant" :class="{ 'active': showAIChat }">
      <div class="ai-chat-shell" v-if="showAIChat">
        <div class="ai-chat-container ai-chat-glass-panel" :class="{ 'sidebar-collapsed': sidebarCollapsed, 'welcome-mode': !hasConversationStarted }">
          <aside class="chat-sidebar">
            <div class="sidebar-header">
              <div v-if="!sidebarCollapsed" class="sidebar-brand">AIview</div>
              <div class="sidebar-toolbar">
                <button class="sidebar-icon-btn sidebar-toggle-btn" @click.stop="toggleSidebar" type="button" :aria-label="sidebarCollapsed ? '展开侧边栏' : '收起侧边栏'">
                  <svg class="toolbar-icon-svg" width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                    <path d="M8.5 5V19" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                    <path v-if="sidebarCollapsed" d="M15 12L12 9M15 12L12 15" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                    <path v-else d="M12.5 12L15.5 9M12.5 12L15.5 15" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <span class="tooltip-bubble tooltip-right">{{ sidebarCollapsed ? "展开侧边栏" : "收起侧边栏" }}</span>
                </button>
              </div>
            </div>
            <button class="sidebar-primary-action" @click="createNewSession()" type="button" :aria-label="sidebarCollapsed ? '新建对话' : '创建新对话'">
              <svg class="toolbar-icon-svg" width="19" height="19" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                <path d="M4.5 8A2.5 2.5 0 0 1 7 5.5H10.2L11.8 7.2H15.2A2.3 2.3 0 0 1 17.5 9.5V15.5A2.5 2.5 0 0 1 15 18H7A2.5 2.5 0 0 1 4.5 15.5V8Z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
                <path d="M17.5 6V11" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                <path d="M15 8.5H20" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
              </svg>
              <span v-if="!sidebarCollapsed">新建对话</span>
              <span class="tooltip-bubble tooltip-right">新建对话</span>
            </button>
            <div class="sidebar-section-label" v-if="!sidebarCollapsed && orderedConversationRecords.length > 0">
              最近对话
            </div>
            <div class="session-list" v-if="!sidebarCollapsed">
              <div
                v-for="session in orderedConversationRecords"
                :key="session.id"
                class="session-item"
                :class="{ active: session.id === activeConversationId }"
                @click="switchConversation(session.id)"
              >
                <button class="session-delete-btn" @click.stop="confirmDeleteConversationRecord(session.id)" title="删除会话">×</button>
                <div class="session-main">
                  <div class="session-title">{{ getConversationDisplayTitle(session) }}</div>
                </div>
              </div>
            </div>
          </aside>
          <div class="chat-main">
            <div class="chat-header">
              <button class="chat-back-btn" @click="handleBack" type="button" aria-label="返回模块入口">
                <svg class="toolbar-icon-svg" width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                  <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span class="tooltip-bubble tooltip-right">返回上一界面</span>
              </button>
              <div v-if="displayConversationTitle" class="header-left">
                <div class="chat-title-group">
                  <span class="chat-session-name">{{ displayConversationTitle }}</span>
                </div>
              </div>
              <div class="threshold-settings-wrap">
                <button class="threshold-settings-btn threshold-dynamic-island" :class="{ 'is-open': showThresholdSettings }" type="button" @click="toggleThresholdSettings" aria-label="通过标准设置">
                  <span class="threshold-island-orb" aria-hidden="true">✦</span>
                  <span class="threshold-island-copy">
                    <strong class="threshold-island-title">通过标准</strong>
                  </span>
                </button>
                <teleport to="body">
                <div v-if="showThresholdSettings" class="threshold-settings-panel">
                  <div class="threshold-settings-header">
                    <strong>AIview 通过标准</strong>
                    <span>按 100 分制判断，未配置模块使用默认分。</span>
                  </div>
                  <label class="threshold-field">
                    <span>默认通过分</span>
                    <input v-model.number="thresholdDraft.defaultPassScore" type="number" min="0" max="100" />
                  </label>
                  <div class="threshold-stage-grid">
                    <label v-for="stage in thresholdStageOptions" :key="stage.key" class="threshold-field threshold-stage-field">
                      <span>{{ stage.label }}</span>
                      <input v-model.number="thresholdDraft.stages[stage.key]" type="number" min="0" max="100" />
                    </label>
                  </div>
                  <div class="threshold-actions">
                    <button type="button" class="threshold-reset-btn" @click="resetThresholdSettings">恢复默认</button>
                    <button type="button" class="threshold-save-btn" @click="saveThresholdSettings">保存</button>
                  </div>
                </div>
                </teleport>
              </div>
            </div>

            <div class="chat-home-panel" v-if="!hasConversationStarted">
              <div class="chat-home-title welcome-stream-text">{{ homeWelcomeText }}</div>
            </div>

            <div class="chat-messages" ref="chatMessages" @scroll="handleChatMessagesScroll" v-else>
              <div v-for="(message, index) in aiMessages" :key="index" class="message" :class="message.type">
                <div class="message-bubble" :class="{ streaming: message.streaming, thinking: message.thinking }">
                  <div v-if="message.thinking && !message.content" class="inline-thinking">
                    <span class="inline-thinking-text">正在思考</span>
                    <span class="inline-thinking-dots">
                      <i></i>
                      <i></i>
                      <i></i>
                    </span>
                  </div>
                  <div v-else class="message-content" :class="{ streaming: message.streaming }" v-html="formatMessage(message.content)"></div>
                </div>
                <div class="message-time">{{ message.time }}</div>
              </div>
              <div class="error-message" :class="{ 'service-unavailable': isAiServiceUnavailable }" v-if="errorMessage">
                <span>{{ errorMessage }}</span>
                <button class="retry-btn" @click="retry">重试</button>
              </div>
              <div ref="chatMessagesEnd" class="chat-messages-end"></div>
            </div>

            <div class="chat-input">
              <div v-if="assistantActions.length > 0" class="recommended-action-card">
                <div class="recommended-action-text">
                  <div class="recommended-action-title">推荐操作</div>
                  <div class="recommended-action-desc">按当前 AI 面试进度，建议直接执行下一步。</div>
                </div>
                <div class="chat-quick-actions">
                  <button
                    v-for="action in assistantActions"
                    :key="action.type"
                    class="recommended-action-btn"
                    type="button"
                    @click="runAssistantAction(action)"
                  >
                    {{ action.label }}
                  </button>
                </div>
              </div>
              <div class="input-wrapper">
                <textarea
                  v-model="userInput"
                  @keydown.enter="handleEnterKey"
                  @input="autoResizeMessageInput"
                  :placeholder="hasConversationStarted ? '继续输入消息，按 Enter 发送' : '输入你的目标，按 Enter 开启会话'"
                  class="message-input"
                  ref="messageInputRef"
                  rows="1"
                ></textarea>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="confirmDialog.visible" class="confirm-overlay" @click.self="closeConfirmDialog">
      <div class="confirm-modal-shell" :class="`confirm-${confirmDialog.tone || 'neutral'}`">
        <div class="confirm-modal-header" :class="`confirm-header-${confirmDialog.tone || 'neutral'}`">
          <div class="confirm-title-group">
            <span class="confirm-title">{{ confirmDialog.title }}</span>
            <span class="confirm-subtitle">{{ confirmDialog.tone === 'danger' ? '危险操作' : '操作确认' }}</span>
          </div>
          <button class="confirm-close-btn" @click="closeConfirmDialog" type="button" aria-label="关闭">×</button>
        </div>
        <div class="confirm-body">
          <div class="confirm-icon-wrap" :class="`confirm-icon-${confirmDialog.tone || 'neutral'}`">
            <span>{{ confirmDialog.tone === 'danger' ? '!' : '?' }}</span>
          </div>
          <p class="confirm-message">{{ confirmDialog.message }}</p>
        </div>
        <div class="confirm-actions">
          <button class="confirm-cancel-btn" @click="closeConfirmDialog">取消</button>
          <button class="confirm-primary-btn" :class="`confirm-primary-${confirmDialog.tone || 'neutral'}`" @click="handleConfirmAction">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
import {
  getInterviewAssistantReply,
  extractResumeContent,
  analyzeComprehensiveResume,
  getJobInfo,
  getJobList,
  fetchAgentConversationList,
  upsertAgentConversation,
  deleteAgentConversationById
} from '@/utils/api/pages/ai-chat';
import {
  canAdvanceFromResume,
  clearActiveComprehensiveAssessment,
  consumeAiAssessmentCompletion,
  createNewAiAssessmentSession,
  createDefaultComprehensiveState,
  ensureActiveAiAssessmentSession,
  getAssessmentThresholds,
  getStagePassScore,
  hasStagePassed,
  normalizeComprehensiveStage,
  normalizeComprehensiveState,
  resetAssessmentThresholds,
  saveAssessmentThresholds,
  updateComprehensiveAssessmentState
} from '@/utils/comprehensive-assessment';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);
const isTransitioning = ref(false);
const ASSISTANT_CONVERSATION_AGENT_KEY = 'ai-interview-assistant';
const DEFAULT_SESSION_TITLE = '会话';
const EMPTY_SESSION_PREVIEW = '点击开始对话';
const HOME_WELCOME_MESSAGE = '有什么我能帮你的吗？';
const ACTIVE_ASSISTANT_CHAT_STORAGE_KEY = 'activeAiConversationChatId';
const thresholdStageOptions = [
  { key: 'resume', label: '简历评测' },
  { key: 'questions', label: '试题作答' },
  { key: 'round_1', label: '技术一面' },
  { key: 'round_2', label: '技术二面' },
  { key: 'round_3', label: 'HR 三面' }
];
const assessmentThresholds = ref(getAssessmentThresholds());
const thresholdDraft = ref(getAssessmentThresholds());
const showThresholdSettings = ref(false);
const syncThresholdDraft = () => {
  thresholdDraft.value = JSON.parse(JSON.stringify(assessmentThresholds.value));
};
const toggleThresholdSettings = () => {
  if (!showThresholdSettings.value) {
    syncThresholdDraft();
  }
  showThresholdSettings.value = !showThresholdSettings.value;
};
const saveThresholdSettings = () => {
  assessmentThresholds.value = saveAssessmentThresholds(thresholdDraft.value);
  syncThresholdDraft();
  showThresholdSettings.value = false;
  comprehensiveState.value = normalizeComprehensiveState(comprehensiveState.value);
  persistConversationState();
};
const resetThresholdSettings = () => {
  assessmentThresholds.value = resetAssessmentThresholds();
  syncThresholdDraft();
  comprehensiveState.value = normalizeComprehensiveState(comprehensiveState.value);
  persistConversationState();
};
const smoothPush = (url) => { isTransitioning.value = true; setTimeout(() => { router.push(url); }, 180); };
const smoothReLaunch = (url) => { isTransitioning.value = true; setTimeout(() => { uni.reLaunch({ url }); }, 180); };
const formatDateTimeUtc8 = (value = Date.now()) => {
  const date = value instanceof Date ? value : new Date(value);
  const utcTime = date.getTime() + date.getTimezoneOffset() * 60000;
  const local = new Date(utcTime + 8 * 60 * 60000);
  const pad = (num) => String(num).padStart(2, '0');
  return `${local.getFullYear()}-${pad(local.getMonth() + 1)}-${pad(local.getDate())}T${pad(local.getHours())}:${pad(local.getMinutes())}:${pad(local.getSeconds())}+08:00`;
};
const buildInterviewAiUrl = ({
  chatId = '',
  completedType = '',
  mode = '',
  score = '',
  timestamp = '',
  jobId = '',
  assessmentId = '',
  autoOpen
} = {}) => {
  const query = [];
  const safeChatId = String(chatId || '').trim();
  const safeCompletedType = String(completedType || '').trim();
  const safeMode = String(mode || '').trim();
  const safeScore = String(score || '').trim();
  const safeTimestamp = String(timestamp || '').trim();
  const safeJobId = String(jobId || '').trim();
  const safeAssessmentId = String(assessmentId || '').trim();
  if (safeChatId) {
    query.push(`chatId=${encodeURIComponent(safeChatId)}`);
  }
  if (safeJobId) {
    query.push(`jobId=${encodeURIComponent(safeJobId)}`);
  }
  if (safeAssessmentId) {
    query.push(`assessmentId=${encodeURIComponent(safeAssessmentId)}`);
  }
  if (safeCompletedType) {
    query.push(`completedType=${encodeURIComponent(safeCompletedType)}`);
  }
  if (safeMode) {
    query.push(`mode=${encodeURIComponent(safeMode)}`);
  }
  if (safeScore) {
    query.push(`score=${encodeURIComponent(safeScore)}`);
  }
  if (safeTimestamp) {
    query.push(`timestamp=${encodeURIComponent(safeTimestamp)}`);
  }
  if (autoOpen === true) {
    query.push('autoOpen=1');
  } else if (autoOpen === false) {
    query.push('autoOpen=0');
  }
  return `/pages/ai-chat/index${query.length ? `?${query.join('&')}` : ''}`;
};

const getStoredAssistantConversationChatId = () => {
  try {
    return String(sessionStorage.getItem(ACTIVE_ASSISTANT_CHAT_STORAGE_KEY) || '').trim();
  } catch (_) {
    return '';
  }
};

const setStoredAssistantConversationChatId = (chatId = '') => {
  const safeChatId = String(chatId || '').trim();
  try {
    if (safeChatId) {
      sessionStorage.setItem(ACTIVE_ASSISTANT_CHAT_STORAGE_KEY, safeChatId);
    } else {
      sessionStorage.removeItem(ACTIVE_ASSISTANT_CHAT_STORAGE_KEY);
    }
  } catch (_) {}
};

// AI对话相关状态
const showAIChat = ref(true);
const hasInteractedWithAssistant = ref(false);
const aiMessages = ref([]);
const userInput = ref('');
const chatMessages = ref(null);
const chatMessagesEnd = ref(null);
const isChatAutoScrollEnabled = ref(true);
const previousChatScrollTop = ref(0);
const ignoreChatScrollEventUntil = ref(0);
const selectedEvaluationMode = ref('');
const isResumeUploading = ref(false);
const assistantActions = ref([]);
const eventLogs = ref([]);
const pendingSuggestedJob = ref(null);
const lastAssistantDecision = ref({
  intent: 'general_consultation',
  targetModule: 'NONE',
  targetMode: 'UNKNOWN',
  targetJobHint: '',
  targetJobConfirmed: false,
  completionAcknowledged: false
});
const assistantConversationScopeId = ref('');
const hasShownWelcomeMessage = ref(false);
// 会话持久化列表：用于左侧历史栏展示与切换。
const conversationRecords = ref([]);
const inMemoryDraftSession = ref(null);
const activeConversationId = ref('');
const sidebarCollapsed = ref(false);
const homeWelcomeText = ref('');
const AVAILABLE_ASSISTANT_ACTION_TYPES = [
  'go_resume',
  'go_questions',
  'go_round_1',
  'go_round_2',
  'go_round_3',
  'restart_comprehensive'
];
const ASSISTANT_ACTION_LABELS = {
  go_resume: '开始简历评测',
  go_questions: '进入试题作答',
  go_round_1: '进入技术一面',
  go_round_2: '进入技术二面',
  go_round_3: '进入 HR 三面',
  restart_comprehensive: '重新开始 AI 面试'
};
const CHAT_AUTO_SCROLL_THRESHOLD = 120;
const CHAT_AUTO_SCROLL_RESUME_THRESHOLD = 24;
const JOB_SWITCH_INTENT_PATTERN = /(切换|换成|换到|改成|改为|转为|转成|转到)/;
const JOB_REFERENCE_PATTERN = /(行业|岗位|职位|方向|做测试|做算法|测试岗|算法岗|测试行业|算法行业)/;

// 全局的 jobId (从面试界面带过来)
const currentJobId = ref(null);
let cachedJobList = null;
const persistedSessionIds = new Set();
const deletedSessionIds = new Set();
const pendingAssistantSessionIds = new Set();
let persistSessionTimer = null;
const hasRestoredConversationState = ref(false);
const isEnteringPageFlow = ref(false);
let lastHandledPendingActionKey = '';
const DRAFT_SESSION_ID_PREFIX = 'draft:';

const isPersistentConversationId = (conversationId = '') => {
  const safeConversationId = String(conversationId || '').trim();
  return safeConversationId.startsWith('chat:') || /^\d{12,}$/.test(safeConversationId);
};

const buildDraftConversationId = (draftId = '') => {
  const safeDraftId = String(draftId || '').trim();
  return safeDraftId || `${DRAFT_SESSION_ID_PREFIX}${Date.now()}:${Math.random().toString(36).slice(2, 8)}`;
};

const getActiveConversationUiId = () => {
  return String(activeConversationRecord.value?.id || activeConversationId.value || '').trim();
};

const getActivePersistentSessionId = (fallback = '') => {
  const activeRecordChatId = String(activeConversationRecord.value?.chatId || '').trim();
  if (isPersistentConversationId(activeRecordChatId)) {
    return activeRecordChatId;
  }
  const uiId = getActiveConversationUiId();
  if (isPersistentConversationId(uiId)) {
    return uiId;
  }
  const scopedSessionId = String(assistantConversationScopeId.value || '').trim();
  if (isPersistentConversationId(scopedSessionId)) {
    return scopedSessionId;
  }
  const storedSessionId = getStoredAssistantConversationChatId();
  if (isPersistentConversationId(storedSessionId)) {
    return storedSessionId;
  }
  const safeFallback = String(fallback || '').trim();
  return isPersistentConversationId(safeFallback) ? safeFallback : '';
};

const collectConversationSessionKeys = (sessionLike = {}) => {
  return Array.from(new Set(
    [
      String(sessionLike?.id || '').trim(),
      String(sessionLike?.chatId || '').trim()
    ].filter(Boolean)
  ));
};

const sessionMatchesAnyKey = (sessionLike = {}, keys = []) => {
  const safeKeys = new Set(
    Array.from(keys || [])
      .map((item) => String(item || '').trim())
      .filter(Boolean)
  );
  return collectConversationSessionKeys(sessionLike).some((item) => safeKeys.has(item));
};

const findConversationRecordByKey = (key = '') => {
  const safeKey = String(key || '').trim();
  if (!safeKey) {
    return null;
  }
  const historyRecord = conversationRecords.value.find((item) => sessionMatchesAnyKey(item, [key]));
  if (historyRecord) {
    return historyRecord;
  }
  return sessionMatchesAnyKey(inMemoryDraftSession.value, [safeKey])
    ? inMemoryDraftSession.value
    : null;
};

const activateConversationByKey = (key = '') => {
  const targetSession = findConversationRecordByKey(key);
  if (targetSession) {
    applySessionState(targetSession);
  }
  return targetSession;
};

const markPendingAssistantSessions = (...keys) => {
  keys
    .flat()
    .map((item) => String(item || '').trim())
    .filter(Boolean)
    .forEach((item) => pendingAssistantSessionIds.add(item));
};

const clearPendingAssistantSessions = (...keys) => {
  keys
    .flat()
    .map((item) => String(item || '').trim())
    .filter(Boolean)
    .forEach((item) => pendingAssistantSessionIds.delete(item));
};

const hasPendingAssistantForSession = (sessionLike = {}) => {
  return collectConversationSessionKeys(sessionLike).some((item) => pendingAssistantSessionIds.has(item));
};

const createLocalThinkingPlaceholder = () => ({
  type: 'ai',
  content: '',
  time: new Date().toLocaleTimeString(),
  thinking: true,
  streaming: false
});

const syncRouteWithPersistentSession = (fallback = '') => {
  const safeJobId = String(currentJobId.value || route.query.jobId || '').trim();
  const safeAssessmentId = String(currentComprehensiveAssessmentId.value || route.query.assessmentId || '').trim();
  if (!showAIChat.value) {
    if (String(route.query.chatId || '').trim() || String(route.query.completedType || '').trim() || String(route.query.mode || '').trim() || String(route.query.timestamp || '').trim() || String(route.query.autoOpen || '').trim() !== '0') {
      router.replace(buildInterviewAiUrl({
        jobId: safeJobId,
        assessmentId: safeAssessmentId,
        autoOpen: false
      }));
    }
    return;
  }
  const targetSessionId = getActivePersistentSessionId(fallback);
  const currentRouteSessionId = String(route.query.chatId || '').trim();
  if (!targetSessionId || currentRouteSessionId === targetSessionId) {
    return;
  }
  router.replace(buildInterviewAiUrl({
    chatId: targetSessionId,
    jobId: safeJobId,
    assessmentId: safeAssessmentId,
    autoOpen: showAIChat.value
  }));
};

const shouldAutoOpenAiChatOnEntry = () => {
  return true;
};

const normalizeJobText = (value) => String(value || '')
  .toLowerCase()
  .replace(/\s+/g, '')
  .trim();

const extractJobSwitchTokens = (question) => {
  const cleaned = String(question || '')
    .replace(JOB_SWITCH_INTENT_PATTERN, ' ')
    .replace(/(当前|现在|这个|一下|一下子|帮我|请|想要|我想|我要|岗位|职位|方向|做|去做|评测|测评)/g, ' ')
    .replace(/[，。！？、,.!?()（）/]/g, ' ');
  return cleaned
    .split(/\s+/)
    .map((item) => item.trim())
    .filter((item) => item.length >= 2);
};

const fetchAllJobs = async () => {
  if (Array.isArray(cachedJobList) && cachedJobList.length > 0) {
    return cachedJobList;
  }
  const response = await getJobList();
  const list = Array.isArray(response?.data) ? response.data : [];
  cachedJobList = list;
  return list;
};

const findBestMatchedJob = async (question) => {
  const rawQuestion = String(question || '');
  if (!JOB_SWITCH_INTENT_PATTERN.test(rawQuestion) && !JOB_REFERENCE_PATTERN.test(rawQuestion)) {
    return null;
  }

  const jobs = await fetchAllJobs();
  if (!jobs.length) {
    return null;
  }

  const normalizedQuestion = normalizeJobText(rawQuestion);
  const tokens = extractJobSwitchTokens(rawQuestion);
  let bestJob = null;
  let bestScore = 0;

  jobs.forEach((job) => {
    const normalizedName = normalizeJobText(job?.name);
    const normalizedDesc = normalizeJobText(job?.description);
    let score = 0;

    if (!normalizedName) {
      return;
    }

    if (normalizedQuestion.includes(normalizedName)) {
      score += 300;
    }

    tokens.forEach((token) => {
      const normalizedToken = normalizeJobText(token);
      if (!normalizedToken) {
        return;
      }
      if (normalizedName.includes(normalizedToken)) {
        score += 120 + normalizedToken.length * 10;
      }
      if (normalizedDesc.includes(normalizedToken)) {
        score += 30 + normalizedToken.length * 4;
      }
    });

    if (score > bestScore) {
      bestScore = score;
      bestJob = job;
    }
  });

  return bestScore >= 80 ? bestJob : null;
};

const findMentionedJobFromText = async (text) => {
  const jobs = await fetchAllJobs();
  if (!jobs.length) {
    return null;
  }
  const normalizedText = normalizeJobText(text);
  if (!normalizedText) {
    return null;
  }

  let bestJob = null;
  let bestScore = 0;
  jobs.forEach((job) => {
    const normalizedName = normalizeJobText(job?.name);
    if (!normalizedName) {
      return;
    }
    let score = 0;
    if (normalizedText.includes(normalizedName)) {
      score += 300;
    }
    const compactName = normalizedName
      .replace(/工程师|岗位|职位|方向/g, '')
      .trim();
    if (compactName && normalizedText.includes(compactName)) {
      score += 180;
    }
    if (score > bestScore) {
      bestScore = score;
      bestJob = job;
    }
  });
  return bestScore >= 100 ? bestJob : null;
};

const resolveJobByHint = async (hint) => {
  const safeHint = String(hint || '').trim();
  if (!safeHint) {
    return null;
  }
  const directMatch = await findMentionedJobFromText(safeHint);
  if (directMatch?.id) {
    return directMatch;
  }
  return findBestMatchedJob(`切换到${safeHint}岗位`);
};

const normalizeTargetModuleValue = (value) => normalizeComprehensiveStage(value);

const mapModuleFromActionType = (actionType) => {
  switch (String(actionType || '').trim()) {
    case 'go_resume':
      return 'resume';
    case 'go_questions':
      return 'questions';
    case 'go_round_1':
      return 'round_1';
    case 'go_round_2':
      return 'round_2';
    case 'go_round_3':
      return 'round_3';
    default:
      return '';
  }
};

const resolveTargetModuleForAction = (action) => {
  const decisionModule = normalizeTargetModuleValue(lastAssistantDecision.value?.targetModule);
  if (decisionModule) {
    return decisionModule;
  }
  return mapModuleFromActionType(action?.type);
};

const resolveSuggestedJobForAction = async () => {
  const decision = lastAssistantDecision.value || {};
  const decisionIntent = String(decision.intent || '').trim();
  const decisionJobHint = String(decision.targetJobHint || '').trim();
  const decisionJobConfirmed = Boolean(decision.targetJobConfirmed);

  if (decisionJobHint) {
    const matchedByDecision = await resolveJobByHint(decisionJobHint);
    if (matchedByDecision?.id) {
      pendingSuggestedJob.value = matchedByDecision;
      return matchedByDecision;
    }
  }

  if (decisionJobConfirmed && decisionIntent !== 'switch_job') {
    pendingSuggestedJob.value = null;
    return null;
  }

  const latestUserQuestion = [...aiMessages.value]
    .reverse()
    .find((item) => item?.type === 'user' && String(item?.content || '').trim());
  if (latestUserQuestion?.content) {
    const matchedByIntent = await findBestMatchedJob(latestUserQuestion.content);
    if (matchedByIntent?.id) {
      pendingSuggestedJob.value = matchedByIntent;
      return matchedByIntent;
    }
  }

  if (pendingSuggestedJob.value?.id) {
    return pendingSuggestedJob.value;
  }

  const recentTexts = aiMessages.value
    .slice(-8)
    .map((item) => String(item?.content || '').trim())
    .filter(Boolean)
    .reverse();

  for (const text of recentTexts) {
    const mentionedJob = await findMentionedJobFromText(text);
    if (mentionedJob?.id) {
      pendingSuggestedJob.value = mentionedJob;
      return mentionedJob;
    }
  }

  return null;
};

const switchCurrentJobContext = async (job, options = {}) => {
  const previousJobId = String(currentJobId.value || activeConversationRecord.value?.jobId || '').trim();
  const nextJobId = String(job?.id || '').trim();
  if (!nextJobId || nextJobId === previousJobId) {
    return false;
  }

  const snapshot = syncCurrentSessionState();
  if (shouldPersistConversationToServer(snapshot)) {
    await persistConversationToServer(snapshot, nextJobId);
  }
  currentJobId.value = nextJobId;
  uni.setStorageSync('currentJobId', nextJobId);
  uni.setStorageSync('currentJobInfo', JSON.stringify(job));
  try {
    sessionStorage.setItem('currentJobId', nextJobId);
    localStorage.setItem('currentJobId', nextJobId);
    localStorage.setItem('currentJobInfo', JSON.stringify(job));
  } catch (error) {
    console.warn('保存切换后的岗位信息失败:', error);
  }

  syncComprehensiveAssessmentState({
    jobId: nextJobId,
    createIfMissing: false
  });
  pendingSuggestedJob.value = null;
  const current = activeConversationRecord.value;
  if (current) {
    const updatedSession = createSessionState({
      ...current,
      jobId: nextJobId
    });
    upsertConversationRecord(updatedSession);
    applySessionState(updatedSession);
  }
  syncRouteWithPersistentSession();
  appendSessionEvent('job_switched', {
    source: String(options?.source || '').trim(),
    fromJobId: previousJobId,
    toJobId: nextJobId,
    targetJobHint: String(job?.name || '').trim(),
    meta: {
      jobName: String(job?.name || '').trim()
    }
  }, { persist: false });
  await nextTick();
  scrollToBottom('auto', true);
  return true;
};

const trySwitchJobFromQuestion = async (question) => {
  try {
    const matchedJob = await findBestMatchedJob(question);
    if (!matchedJob) {
      return null;
    }
    const changed = await switchCurrentJobContext(matchedJob, { source: 'user_question' });
    return {
      changed,
      job: matchedJob
    };
  } catch (error) {
    console.warn('根据对话切换岗位失败:', error);
    return null;
  }
};

const normalizeStoredAssistantActions = (actions) => {
  if (!Array.isArray(actions)) {
    return [];
  }
  return actions
    .filter((item) => AVAILABLE_ASSISTANT_ACTION_TYPES.includes(item?.type))
    .map((item) => ({
      type: item.type,
      label: String(item?.label || ASSISTANT_ACTION_LABELS[item.type] || '')
    }))
    .filter((item) => item.label)
    .slice(0, 3);
};

const normalizeStoredEventLogs = (logs) => {
  if (!Array.isArray(logs)) {
    return [];
  }
  return logs
    .filter((item) => item && typeof item === 'object')
    .map((item) => ({
      id: String(item.id || `event:${Date.now()}:${Math.random().toString(36).slice(2, 8)}`),
      type: String(item.type || '').trim(),
      time: String(item.time || formatDateTimeUtc8()),
      source: String(item.source || '').trim(),
      chatId: String(item.chatId || '').trim(),
      jobId: String(item.jobId || '').trim(),
      module: String(item.module || '').trim(),
      mode: String(item.mode || '').trim(),
      actionType: String(item.actionType || '').trim(),
      label: String(item.label || '').trim(),
      completionType: String(item.completionType || '').trim(),
      score: Number(item.score || 0) || 0,
      reason: String(item.reason || '').trim(),
      fromJobId: String(item.fromJobId || '').trim(),
      toJobId: String(item.toJobId || '').trim(),
      targetJobHint: String(item.targetJobHint || '').trim(),
      intent: String(item.intent || '').trim(),
      reply: String(item.reply || '').trim(),
      meta: item.meta && typeof item.meta === 'object' ? item.meta : {}
    }))
    .filter((item) => item.type)
    .slice(-200);
};

const appendSessionEvent = (type, payload = {}, options = {}) => {
  const safeType = String(type || '').trim();
  if (!safeType) {
    return null;
  }
  const {
    persist = true,
    replaceLast = false
  } = options;
  const event = {
    id: `event:${Date.now()}:${Math.random().toString(36).slice(2, 8)}`,
    type: safeType,
    time: formatDateTimeUtc8(),
    chatId: String(payload?.chatId || getActivePersistentSessionId()).trim(),
    jobId: String(payload?.jobId ?? currentJobId.value ?? activeConversationRecord.value?.jobId ?? '').trim(),
    module: String(payload?.module || '').trim(),
    mode: String(payload?.mode || selectedEvaluationMode.value || '').trim(),
    source: String(payload?.source || '').trim(),
    actionType: String(payload?.actionType || '').trim(),
    label: String(payload?.label || '').trim(),
    completionType: String(payload?.completionType || '').trim(),
    score: Number(payload?.score || 0) || 0,
    reason: String(payload?.reason || '').trim(),
    fromJobId: String(payload?.fromJobId || '').trim(),
    toJobId: String(payload?.toJobId || '').trim(),
    targetJobHint: String(payload?.targetJobHint || '').trim(),
    intent: String(payload?.intent || '').trim(),
    reply: String(payload?.reply || '').trim(),
    meta: payload?.meta && typeof payload.meta === 'object' ? payload.meta : {}
  };
  const nextLogs = normalizeStoredEventLogs(eventLogs.value);
  if (replaceLast && nextLogs.length > 0 && nextLogs[nextLogs.length - 1]?.type === safeType) {
    nextLogs[nextLogs.length - 1] = event;
  } else {
    nextLogs.push(event);
  }
  eventLogs.value = nextLogs.slice(-200);
  if (persist) {
    persistConversationState();
  }
  return event;
};

const hasStartedConversation = (messages = []) => {
  return Array.isArray(messages) && messages.some((item) => (
    (item?.type === 'user' && String(item?.content || '').trim())
    || (item?.type === 'ai' && String(item?.content || '').trim())
  ));
};

const createSessionTitleFromMessages = (messages = []) => {
  const firstUserMessage = Array.isArray(messages)
    ? messages.find((item) => item?.type === 'user' && String(item?.content || '').trim())
    : null;
  const seed = String(firstUserMessage?.content || '').trim();
  if (!seed) {
    return DEFAULT_SESSION_TITLE;
  }
  return seed.length > 16 ? `${seed.slice(0, 16)}...` : seed;
};

const buildUntitledSessionTitle = () => {
  const untitledCount = conversationRecords.value.filter((item) => String(item?.title || '').startsWith(DEFAULT_SESSION_TITLE)).length;
  return untitledCount === 0 ? DEFAULT_SESSION_TITLE : `${DEFAULT_SESSION_TITLE} ${untitledCount + 1}`;
};

const hasSessionFirstQuestion = (session) => {
  return hasStartedConversation(session?.aiMessages || []);
};

const buildConversationDisplayDedupKey = (session) => {
  const safeMessages = sanitizeConversationMessages(session?.aiMessages || []);
  if (safeMessages.length === 0) {
    return `id:${String(session?.id || '').trim()}`;
  }
  return JSON.stringify({
    title: String(session?.title || '').trim(),
    jobId: String(session?.jobId || '').trim(),
    selectedEvaluationMode: String(session?.selectedEvaluationMode || '').trim(),
    messages: safeMessages.map((item) => ({
      type: item.type,
      content: item.content
    }))
  });
};

const formatEventTimeToMessageTime = (value) => {
  const text = String(value || '').trim();
  if (!text) {
    return new Date().toLocaleTimeString();
  }
  const parsed = new Date(text);
  return Number.isNaN(parsed.getTime()) ? text : parsed.toLocaleTimeString();
};

const rebuildMessagesFromEvents = (messages = [], logs = []) => {
  const safeMessages = sanitizeConversationMessages(messages);
  const safeLogs = normalizeStoredEventLogs(logs);
  const existingAssistantCount = safeMessages.filter((item) => item.type === 'ai').length;
  const missingAssistantReplies = safeLogs
    .filter((item) => item.type === 'assistant_reply' && item.reply)
    .slice(existingAssistantCount)
    .map((item) => ({
      type: 'ai',
      content: item.reply,
      time: formatEventTimeToMessageTime(item.time)
    }));

  if (missingAssistantReplies.length === 0) {
    return safeMessages;
  }

  const merged = [];
  let replyIndex = 0;
  for (let index = 0; index < safeMessages.length; index += 1) {
    const current = safeMessages[index];
    const next = safeMessages[index + 1];
    merged.push(current);
    if (current.type === 'user' && (!next || next.type === 'user') && replyIndex < missingAssistantReplies.length) {
      merged.push(missingAssistantReplies[replyIndex]);
      replyIndex += 1;
    }
  }

  while (replyIndex < missingAssistantReplies.length) {
    merged.push(missingAssistantReplies[replyIndex]);
    replyIndex += 1;
  }

  return merged;
};

const sanitizeConversationMessages = (messages = []) => {
  if (!Array.isArray(messages)) {
    return [];
  }
  return messages
    .filter((item) => item && item.type)
    .map((item) => ({
      type: item.type === 'user' ? 'user' : 'ai',
      content: String(item.content || ''),
      time: String(item.time || new Date().toLocaleTimeString())
    }))
    .filter((item) => item.content.trim());
};

const createSessionState = (overrides = {}) => {
  const persistentCandidate = String(overrides.chatId || overrides.id || '').trim();
  const chatId = isPersistentConversationId(persistentCandidate) ? persistentCandidate : '';
  const conversationId = chatId || buildDraftConversationId(String(overrides.id || '').trim());
  const safeMessages = rebuildMessagesFromEvents(overrides.aiMessages || [], overrides.eventLogs || []);
  return {
    id: conversationId,
    chatId,
    jobId: String(overrides.jobId || currentJobId.value || '').trim(),
    title: String(overrides.title || createSessionTitleFromMessages(safeMessages) || buildUntitledSessionTitle()),
    preview: String(overrides.preview || EMPTY_SESSION_PREVIEW),
    hasInteractedWithAssistant: overrides.hasInteractedWithAssistant ?? hasStartedConversation(safeMessages),
    hasShownWelcomeMessage: overrides.hasShownWelcomeMessage ?? false,
    selectedEvaluationMode: String(overrides.selectedEvaluationMode || ''),
    assistantActions: normalizeStoredAssistantActions(overrides.assistantActions || []),
    eventLogs: normalizeStoredEventLogs(overrides.eventLogs || []),
    aiMessages: safeMessages,
    firstQuestionAt: String(overrides.firstQuestionAt || ''),
    createdAt: String(overrides.createdAt || formatDateTimeUtc8()),
    updatedAt: String(overrides.updatedAt || formatDateTimeUtc8())
  };
};

const sortSessions = (sessions = []) => {
  return [...sessions].sort((left, right) => {
    const rightTime = new Date(right.updatedAt || right.firstQuestionAt || right.createdAt || 0).getTime();
    const leftTime = new Date(left.updatedAt || left.firstQuestionAt || left.createdAt || 0).getTime();
    return rightTime - leftTime;
  });
};

const upsertConversationRecord = (session, replaceIds = []) => {
  const safeSession = createSessionState(session || {});
  const idsToReplace = new Set(
    [
      ...collectConversationSessionKeys(safeSession),
      ...replaceIds
    ]
      .map((item) => String(item || '').trim())
      .filter(Boolean)
  );
  conversationRecords.value = sortSessions([
    safeSession,
    ...conversationRecords.value.filter((item) => !sessionMatchesAnyKey(item, idsToReplace))
  ]);
  if (sessionMatchesAnyKey(inMemoryDraftSession.value, idsToReplace)) {
    inMemoryDraftSession.value = null;
  }
  return safeSession;
};

const resolveRouteJobId = () => String(route.query.jobId || '').trim();

const applySessionState = (session) => {
  const safeSession = createSessionState(session || {});
  const routeJobId = resolveRouteJobId();
  activeConversationId.value = safeSession.id;
  setStoredAssistantConversationChatId(safeSession.chatId || '');
  currentJobId.value = routeJobId || String(safeSession.jobId || '').trim();
  assistantConversationScopeId.value = isPersistentConversationId(safeSession.chatId)
    ? safeSession.chatId
    : '';
  aiMessages.value = safeSession.aiMessages;
  if (hasPendingAssistantForSession(safeSession) && !aiMessages.value.some((item) => item?.thinking)) {
    aiMessages.value = [...aiMessages.value, createLocalThinkingPlaceholder()];
  }
  assistantActions.value = safeSession.assistantActions;
  eventLogs.value = safeSession.eventLogs;
  pendingSuggestedJob.value = null;
  lastAssistantDecision.value = {
    intent: 'general_consultation',
    targetModule: 'NONE',
    targetMode: 'UNKNOWN',
    targetJobHint: '',
    targetJobConfirmed: Boolean(safeSession.jobId),
    completionAcknowledged: false
  };
  hasInteractedWithAssistant.value = Boolean(safeSession.hasInteractedWithAssistant);
  hasShownWelcomeMessage.value = Boolean(safeSession.hasShownWelcomeMessage);
  selectedEvaluationMode.value = String(safeSession.selectedEvaluationMode || '');
  syncComprehensiveAssessmentState({
    jobId: currentJobId.value,
    assessmentId: currentComprehensiveAssessmentId.value || route.query.assessmentId || '',
    createIfMissing: false
  });
  if (hasStartedConversation(safeSession.aiMessages)) {
    stopHomeWelcomeTyping();
  } else {
    startHomeWelcomeTyping(true);
  }
};

const activeConversationRecord = computed(() => {
  const historyRecord = conversationRecords.value.find((item) => item.id === activeConversationId.value);
  if (historyRecord) {
    return historyRecord;
  }
  return String(inMemoryDraftSession.value?.id || '').trim() === String(activeConversationId.value || '').trim()
    ? inMemoryDraftSession.value
    : null;
});

const orderedConversationRecords = computed(() => {
  const seen = new Set();
  return sortSessions(conversationRecords.value.filter((item) => hasSessionFirstQuestion(item)))
    .filter((item) => {
      const dedupKey = buildConversationDisplayDedupKey(item);
      if (seen.has(dedupKey)) {
        return false;
      }
      seen.add(dedupKey);
      return true;
    });
});

const hasConversationStarted = computed(() => {
  return hasStartedConversation(aiMessages.value);
});

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

let homeWelcomeTypingSeq = 0;

function stopHomeWelcomeTyping() {
  homeWelcomeTypingSeq += 1;
  homeWelcomeText.value = '';
}

watch(
  [showAIChat, hasConversationStarted],
  ([visible, started]) => {
    if (visible && !started) {
      startHomeWelcomeTyping(true);
      return;
    }
    stopHomeWelcomeTyping();
  },
  { immediate: true }
);

watch(
  () => [route.query.completedType, route.query.timestamp, route.query.chatId],
  async ([completedType, timestamp], [previousCompletedType, previousTimestamp]) => {
    const safeCompletedType = String(completedType || '').trim();
    if (!safeCompletedType) {
      return;
    }
    if (
      safeCompletedType === String(previousCompletedType || '').trim()
      && String(timestamp || '').trim() === String(previousTimestamp || '').trim()
    ) {
      return;
    }
    await nextTick();
    await handlePendingAssistantAction();
  }
);

const displayConversationTitle = computed(() => {
  const title = String(activeConversationRecord.value?.title || '').trim();
  if (!title || title === DEFAULT_SESSION_TITLE || title.startsWith(`${DEFAULT_SESSION_TITLE} `)) {
    return '';
  }
  return title;
});

const getConversationDisplayTitle = (session) => {
  const title = String(session?.title || '').trim();
  if (!title || title === DEFAULT_SESSION_TITLE || title.startsWith(`${DEFAULT_SESSION_TITLE} `)) {
    return DEFAULT_SESSION_TITLE;
  }
  return title;
};

const hasSessionContextToClear = computed(() => {
  return aiMessages.value.length > 1
    || hasConversationStarted.value
    || assistantActions.value.length > 0
    || eventLogs.value.length > 0;
});

const confirmDialog = ref({
  visible: false,
  type: '',
  tone: 'neutral',
  targetSessionId: '',
  title: '',
  message: ''
});

const findReusableDraftSession = () => {
  return inMemoryDraftSession.value && !hasSessionFirstQuestion(inMemoryDraftSession.value)
    ? inMemoryDraftSession.value
    : null;
};

const createNewSession = (options = {}) => {
  const { activate = true } = options;
  const reusableSession = findReusableDraftSession();
  if (reusableSession) {
    if (activate) {
      applySessionState(reusableSession);
    }
    return reusableSession;
  }
  const session = createSessionState({ title: buildUntitledSessionTitle() });
  inMemoryDraftSession.value = session;
  if (activate) {
    applySessionState(session);
  }
  return session;
};

const buildCurrentSessionSnapshot = () => {
  const existing = activeConversationRecord.value;
  const hasRealConversationStarted = hasStartedConversation(aiMessages.value);
  const persistentSessionId = isPersistentConversationId(existing?.chatId || existing?.id)
    ? String(existing?.chatId || existing?.id || '').trim()
    : getActivePersistentSessionId();
  const resolvedSessionId = hasRealConversationStarted
    ? (persistentSessionId || String(existing?.id || getActiveConversationUiId() || '').trim() || buildDraftConversationId())
    : (String(existing?.id || getActiveConversationUiId() || '').trim() || buildDraftConversationId());
  const firstQuestionAt = existing?.firstQuestionAt || (hasStartedConversation(aiMessages.value) ? formatDateTimeUtc8() : '');
  const title = hasRealConversationStarted
    ? createSessionTitleFromMessages(aiMessages.value)
    : (existing?.title || buildUntitledSessionTitle());
  return createSessionState({
    id: resolvedSessionId,
    chatId: persistentSessionId,
    jobId: String(existing?.jobId || currentJobId.value || '').trim(),
    title,
    preview: EMPTY_SESSION_PREVIEW,
    hasInteractedWithAssistant: hasInteractedWithAssistant.value,
    hasShownWelcomeMessage: hasShownWelcomeMessage.value,
    selectedEvaluationMode: selectedEvaluationMode.value,
    assistantActions: assistantActions.value,
    eventLogs: eventLogs.value,
    aiMessages: aiMessages.value,
    firstQuestionAt,
    createdAt: existing?.createdAt || formatDateTimeUtc8(),
    updatedAt: formatDateTimeUtc8()
  });
};

const syncCurrentSessionState = () => {
  const snapshot = buildCurrentSessionSnapshot();
  const previousActiveId = getActiveConversationUiId();
  activeConversationId.value = snapshot.id;
  if (!hasSessionFirstQuestion(snapshot) && !persistedSessionIds.has(snapshot.id)) {
    inMemoryDraftSession.value = snapshot;
    return snapshot;
  }
  return upsertConversationRecord(snapshot, previousActiveId && previousActiveId !== snapshot.id ? [previousActiveId] : []);
};

const applyPersistedConversation = (serverSession, localSnapshot = null) => {
  const persistedSession = createSessionState(serverSession || localSnapshot || {});
  const idsToReplace = [
    localSnapshot?.id,
    localSnapshot?.chatId
  ]
    .map((item) => String(item || '').trim())
    .filter(Boolean)
    .filter((item) => item !== persistedSession.id);
  const mergedSession = upsertConversationRecord(persistedSession, idsToReplace);
  persistedSessionIds.add(mergedSession.id);
  deletedSessionIds.delete(mergedSession.id);
  const localActiveId = String(localSnapshot?.id || '').trim();
  if (!localActiveId || String(activeConversationId.value || '').trim() === localActiveId || String(activeConversationId.value || '').trim() === String(mergedSession.id || '').trim()) {
    applySessionState(mergedSession);
  }
  syncRouteWithPersistentSession(mergedSession.chatId || mergedSession.id);
  return mergedSession;
};

// 将当前前端会话快照转换为后端会话持久化请求。
const buildConversationUpsertRequest = (snapshot, overrideJobId = null) => ({
  chatId: isPersistentConversationId(snapshot?.chatId || snapshot?.id)
    ? String(snapshot?.chatId || snapshot?.id || '').trim()
    : null,
  agentKey: ASSISTANT_CONVERSATION_AGENT_KEY,
  jobId: Number(String((overrideJobId ?? snapshot?.jobId ?? currentJobId.value) || '').trim()) || null,
  title: snapshot?.title || '',
  selectedEvaluationMode: snapshot?.selectedEvaluationMode || '',
  hasInteractedWithAssistant: Boolean(snapshot?.hasInteractedWithAssistant),
  hasShownWelcomeMessage: Boolean(snapshot?.hasShownWelcomeMessage),
  firstQuestionAt: String(snapshot?.firstQuestionAt || '').trim() || null,
  assistantActions: Array.isArray(snapshot?.assistantActions) ? snapshot.assistantActions : [],
  eventLogs: Array.isArray(snapshot?.eventLogs) ? snapshot.eventLogs : [],
  aiMessages: sanitizeConversationMessages(snapshot?.aiMessages)
});

// 只有已经开始交互或已存在远端记录的会话，才会触发后端持久化。
const shouldPersistConversationToServer = (snapshot) => {
  if (!snapshot?.id) {
    return false;
  }
  if (deletedSessionIds.has(String(snapshot.chatId || snapshot.id || '').trim())) {
    return false;
  }
  return hasSessionFirstQuestion(snapshot)
    || persistedSessionIds.has(String(snapshot.chatId || snapshot.id || '').trim());
};

// 整包上报一个会话快照，后端会拆分写入 session/message/event/memory 四表。
const persistConversationToServer = async (snapshot, overrideJobId = null) => {
  if (!snapshot?.id) {
    return snapshot;
  }
  const conversationId = String(snapshot.chatId || snapshot.id || '').trim();
  if (deletedSessionIds.has(conversationId)) {
    return snapshot;
  }
  if (!hasSessionFirstQuestion(snapshot)) {
    return snapshot;
  }
  const payload = buildConversationUpsertRequest(snapshot, overrideJobId);
  try {
    const response = await upsertAgentConversation(payload);
    const savedSnapshot = response?.data ? applyPersistedConversation(response.data, snapshot) : snapshot;
    persistedSessionIds.add(String(savedSnapshot?.chatId || savedSnapshot?.id || '').trim());
    return savedSnapshot;
  } catch (error) {
    console.warn('保存 AI 会话到后端失败:', error);
    return snapshot;
  }
};

const flushConversationPersistence = async (snapshot, overrideJobId = null) => {
  if (!snapshot?.id) {
    return snapshot;
  }
  if (persistSessionTimer) {
    clearTimeout(persistSessionTimer);
    persistSessionTimer = null;
  }
  if (shouldPersistConversationToServer(snapshot)) {
    return persistConversationToServer(snapshot, overrideJobId);
  }
  return snapshot;
};

// 对会话持久化做轻量防抖，避免输入过程中频繁写库。
const scheduleConversationPersistence = (snapshot, overrideJobId = null) => {
  if (!shouldPersistConversationToServer(snapshot)) {
    return;
  }
  if (persistSessionTimer) {
    clearTimeout(persistSessionTimer);
  }
  persistSessionTimer = setTimeout(() => {
    persistSessionTimer = null;
    persistConversationToServer(snapshot, overrideJobId);
  }, 120);
};

// 同步当前页面会话状态到本地内存，并按需上报后端。
const persistConversationState = () => {
  try {
    const snapshot = syncCurrentSessionState();
    scheduleConversationPersistence(snapshot);
  } catch (error) {
    console.warn('保存 AI 会话状态失败:', error);
  }
};

const persistConversationStateNow = async () => {
  try {
    const snapshot = syncCurrentSessionState();
    return flushConversationPersistence(snapshot);
  } catch (error) {
    console.warn('立即保存 AI 会话状态失败:', error);
    return null;
  }
};

// 页面进入时恢复服务端会话列表。
// 若 URL 指定了 chatId，则打开对应历史会话；否则默认进入新的草稿会话。
const restoreConversationState = async () => {
  assistantConversationScopeId.value = '';
  aiMessages.value = [];
  assistantActions.value = [];
  eventLogs.value = [];
  pendingSuggestedJob.value = null;
  hasInteractedWithAssistant.value = false;
  hasShownWelcomeMessage.value = false;
  selectedEvaluationMode.value = '';
  persistedSessionIds.clear();
  conversationRecords.value = [];
  inMemoryDraftSession.value = null;
  activeConversationId.value = '';
  sidebarCollapsed.value = false;
  showAIChat.value = shouldAutoOpenAiChatOnEntry();

  try {
    const response = await fetchAgentConversationList(ASSISTANT_CONVERSATION_AGENT_KEY);
    const remoteSessions = Array.isArray(response?.data)
      ? response.data.map((item) => createSessionState(item))
      : [];
    if (remoteSessions.length > 0) {
      persistedSessionIds.clear();
      remoteSessions.forEach((item) => {
        if (item?.id) {
          persistedSessionIds.add(item.id);
        }
      });
      conversationRecords.value = sortSessions(remoteSessions);
      const routeSessionId = String(route.query.chatId || '').trim()
        || (String(route.query.completedType || '').trim() ? getStoredAssistantConversationChatId() : '');
      const historySession = findConversationRecordByKey(routeSessionId);
      if (historySession) {
        applySessionState(historySession);
      } else if (routeSessionId) {
        const placeholderSession = createSessionState({
          id: '',
          chatId: routeSessionId,
          title: buildUntitledSessionTitle()
        });
        inMemoryDraftSession.value = placeholderSession;
        applySessionState(placeholderSession);
      } else {
        createNewSession({ activate: true });
      }
      sidebarCollapsed.value = false;
      return;
    }
    createNewSession({ activate: true });
  } catch (error) {
    console.warn('恢复 AI 会话状态失败:', error);
    createNewSession({ activate: true });
  }
};

const resetAssistantSession = () => {
  const existing = activeConversationRecord.value;
  const resetSession = createSessionState({
    id: existing?.id || getActiveConversationUiId(),
    title: buildUntitledSessionTitle(),
    createdAt: existing?.createdAt || formatDateTimeUtc8()
  });
  applySessionState(resetSession);
  if (persistedSessionIds.has(resetSession.id) || isPersistentConversationId(resetSession.id)) {
    conversationRecords.value = sortSessions([
      resetSession,
      ...conversationRecords.value.filter((item) => item.id !== resetSession.id)
    ]);
    persistConversationState();
    return;
  }
  inMemoryDraftSession.value = resetSession;
};

const switchConversation = (chatId) => {
  if (!chatId || chatId === getActiveConversationUiId()) {
    return;
  }
  syncCurrentSessionState();
  const target = conversationRecords.value.find((item) => item.id === chatId);
  if (!target) {
    return;
  }
  applySessionState(target);
  if (isPersistentConversationId(target.id || target.chatId)) {
    syncRouteWithPersistentSession(String(target.id || target.chatId || '').trim());
  }
  persistConversationState();
  nextTick(() => {
    scrollToBottom('auto', true);
  });
};

const deleteConversationRecord = async (chatId) => {
  if (persistSessionTimer) {
    clearTimeout(persistSessionTimer);
    persistSessionTimer = null;
  }
  const remaining = conversationRecords.value.filter((item) => item.id !== chatId);
  if (isPersistentConversationId(chatId)) {
    try {
      await deleteAgentConversationById(chatId);
    } catch (error) {
      console.warn('删除后端 AI 会话失败:', error);
      uni.showToast({
        title: '删除失败，请稍后重试',
        icon: 'none'
      });
      return;
    }
  }
  deletedSessionIds.add(chatId);
  persistedSessionIds.delete(chatId);
  if (remaining.length === 0) {
    conversationRecords.value = [];
    createNewSession({ activate: true });
  } else {
    conversationRecords.value = sortSessions(remaining);
    if (getActiveConversationUiId() === chatId) {
      applySessionState(conversationRecords.value[0]);
    }
  }
  if (String(getActiveConversationUiId() || '').trim() === String(chatId || '').trim()) {
    setStoredAssistantConversationChatId(getActivePersistentSessionId());
  }
  if (String(route.query.chatId || '').trim() === String(chatId || '').trim()) {
    syncRouteWithPersistentSession();
  }
  persistConversationState();
};

const closeConfirmDialog = () => {
  confirmDialog.value = {
    visible: false,
    type: '',
    tone: 'neutral',
    targetSessionId: '',
    title: '',
    message: ''
  };
};

const confirmDeleteConversationRecord = (chatId) => {
  const target = conversationRecords.value.find((item) => item.id === chatId);
  confirmDialog.value = {
    visible: true,
    type: 'delete-session',
    tone: 'danger',
    targetSessionId: chatId,
    title: '确认删除会话',
    message: `确定要删除“${target?.title || '当前会话'}”吗？删除后该会话历史将无法恢复。`
  };
};

const confirmClearChat = () => {
  confirmDialog.value = {
    visible: true,
    type: 'clear-context',
    tone: 'neutral',
    targetSessionId: getActiveConversationUiId(),
    title: '确认清空上下文',
    message: '确定要清空当前会话上下文吗？本轮消息和快捷动作会被重置，但该会话入口会保留。'
  };
};

const handleConfirmAction = () => {
  const { type, targetSessionId } = confirmDialog.value;
  closeConfirmDialog();
  if (type === 'delete-session' && targetSessionId) {
    deleteConversationRecord(targetSessionId);
    return;
  }
  if (type === 'clear-context') {
    clearChat();
  }
};

const consumeRoutePendingAssistantAction = () => {
  const completedType = String(route.query.completedType || '').trim();
  if (!completedType) {
    return null;
  }
  const pendingTypeMap = {
    resume: 'resume_completed',
    questions: 'questions_completed',
    audio: 'round_1_completed',
    round_1: 'round_1_completed',
    round_2: 'round_2_completed',
    round_3: 'round_3_completed'
  };
  const pendingType = pendingTypeMap[completedType];
  if (!pendingType) {
    return null;
  }
  const sourceSessionId = String(route.query.chatId || '').trim() || getActivePersistentSessionId() || getStoredAssistantConversationChatId();
  const sourceMode = String(route.query.mode || 'COMPREHENSIVE').trim() || 'COMPREHENSIVE';
  const hasExplicitScore = route.query.score !== undefined && String(route.query.score).trim() !== '';
  const sourceScore = Number(route.query.score || 0) || 0;
  const sourceTimestamp = String(route.query.timestamp || '').trim();
  const pendingActionKey = [pendingType, sourceSessionId, sourceMode, sourceScore, sourceTimestamp].join('|');
  if (pendingActionKey && pendingActionKey === lastHandledPendingActionKey) {
    return null;
  }
  return {
    type: pendingType,
    mode: sourceMode,
    chatId: sourceSessionId,
    score: sourceScore,
    hasScore: hasExplicitScore,
    skipStateSync: false,
    pendingActionKey
  };
};

const buildCompletionEventPendingAction = (data = {}) => {
  const eventType = String(data?.type || 'resume_completed').trim();
  const sourceSessionId = String(data?.chatId || '').trim() || getActivePersistentSessionId() || getStoredAssistantConversationChatId();
  const sourceMode = String(data?.mode || 'COMPREHENSIVE').trim() || 'COMPREHENSIVE';
  const hasExplicitScore = data?.score !== undefined && String(data?.score).trim() !== '';
  const sourceScore = Number(data?.score || 0) || 0;
  const sourceTimestamp = String(data?.timestamp || Date.now()).trim();
  const pendingActionKey = [eventType, sourceSessionId, sourceMode, sourceScore, sourceTimestamp].join('|');
  if (pendingActionKey && pendingActionKey === lastHandledPendingActionKey) {
    return null;
  }
  return {
    type: eventType,
    mode: sourceMode,
    chatId: sourceSessionId,
    score: sourceScore,
    hasScore: hasExplicitScore,
    skipStateSync: false,
    pendingActionKey,
    source: 'event'
  };
};

const buildPendingActionFromCompletionEvent = (event = {}) => {
  const eventType = String(event?.type || '').trim();
  if (!eventType) {
    return null;
  }
  const sourceSessionId = String(event?.chatId || '').trim() || getActivePersistentSessionId() || getStoredAssistantConversationChatId();
  const sourceMode = String(event?.mode || 'COMPREHENSIVE').trim() || 'COMPREHENSIVE';
  const hasExplicitScore = event?.score !== undefined && String(event?.score).trim() !== '';
  const sourceScore = Number(event?.score || 0) || 0;
  const sourceTimestamp = String(event?.timestamp || Date.now()).trim();
  const pendingActionKey = [eventType, sourceSessionId, sourceMode, sourceScore, sourceTimestamp].join('|');
  if (pendingActionKey && pendingActionKey === lastHandledPendingActionKey) {
    return null;
  }
  return {
    type: eventType,
    mode: sourceMode,
    chatId: sourceSessionId,
    score: sourceScore,
    hasScore: hasExplicitScore,
    suggestions: Array.isArray(event?.suggestions) ? event.suggestions : [],
    skipStateSync: false,
    pendingActionKey,
    source: 'completion_inbox'
  };
};

const consumeStoredPendingAssistantAction = () => {
  const storedEvent = consumeAiAssessmentCompletion({
    chatId: String(route.query.chatId || '').trim() || getActivePersistentSessionId() || getStoredAssistantConversationChatId(),
    jobId: String(route.query.jobId || currentJobId.value || '').trim(),
    assessmentId: String(currentComprehensiveAssessmentId.value || route.query.assessmentId || '').trim()
  });
  return buildPendingActionFromCompletionEvent(storedEvent);
};

const clearRoutePendingAssistantAction = () => {
  if (!String(route.query.completedType || '').trim() && !String(route.query.mode || '').trim() && !String(route.query.timestamp || '').trim()) {
    return;
  }
  const fallbackSessionId = String(route.query.chatId || '').trim() || getActivePersistentSessionId();
  router.replace(buildInterviewAiUrl({
    chatId: showAIChat.value ? fallbackSessionId : '',
    jobId: String(currentJobId.value || route.query.jobId || '').trim(),
    assessmentId: String(currentComprehensiveAssessmentId.value || route.query.assessmentId || '').trim(),
    autoOpen: showAIChat.value
  }));
};

const buildCompletionFollowupQuestion = ({ type = '', mode = '', score = 0, hasScore = false, suggestions = [] } = {}) => {
  const safeType = String(type || '').trim();
  const safeScore = Math.max(0, Number(score) || 0);
  const scoreText = hasScore ? `，得分 ${safeScore} 分` : '';
  const safeSuggestions = Array.isArray(suggestions)
    ? suggestions.map((item) => String(item || '').trim()).filter(Boolean).slice(0, 5)
    : [];
  const suggestionText = safeSuggestions.length > 0
    ? `\n本阶段 AI 已给出以下建议，请在回复中结合这些建议展开分析：\n${safeSuggestions.map((item, index) => `${index + 1}. ${item}`).join('\n')}`
    : '';
  if (safeType === 'resume_completed') {
    return `我已经完成 AI 面试中的简历评测${scoreText}。${suggestionText}\n请基于当前岗位、当前会话和 AI 面试进度，给出简历表现分析、关键改进建议和下一步操作按钮。`;
  }
  if (safeType === 'questions_completed') {
    return `我已经完成 AI 面试中的试题作答${scoreText}。${suggestionText}\n请基于当前岗位、当前会话和 AI 面试进度，分析本阶段表现，指出下一步面试准备重点，并给出可执行按钮。`;
  }
  if (safeType === 'round_1_completed') {
    return `我已经完成 AI 面试中的技术一面${scoreText}。${suggestionText}\n请基于当前岗位、当前会话和 AI 面试进度，分析本阶段表现，指出下一步面试准备重点，并给出可执行按钮。`;
  }
  if (safeType === 'round_2_completed') {
    return `我已经完成 AI 面试中的技术二面${scoreText}。${suggestionText}\n请基于当前岗位、当前会话和 AI 面试进度，分析本阶段表现，指出下一步面试准备重点，并给出可执行按钮。`;
  }
  if (safeType === 'round_3_completed') {
    return `我已经完成 AI 面试中的 HR 三面${scoreText}。${suggestionText}\n请基于当前岗位、当前会话和完整 AI 面试进度，总结本次 AI 面试整体表现，给出后续提升建议，并给出可执行按钮。`;
  }
  return '';
};

const getModuleDisplayName = (moduleType) => {
  const moduleNameMap = {
    resume: '简历评测',
    questions: '试题作答',
    round_1: '技术一面',
    round_2: '技术二面',
    round_3: 'HR 三面'
  };
  return moduleNameMap[String(moduleType || '').trim()] || '';
};

const stagePassed = (stage, stageState) => hasStagePassed(stage, stageState, assessmentThresholds.value);

const buildDeterministicAssistantActions = (state = comprehensiveState.value) => {
  const normalizedState = normalizeComprehensiveState(state);
  if (!normalizedState.resume.completed) {
    return normalizeAssistantActions([{ type: 'go_resume', label: '开始简历评测' }]);
  }
  if (!canAdvanceFromResume(normalizedState.resume, assessmentThresholds.value)) {
    return normalizeAssistantActions([{ type: 'restart_comprehensive', label: '重新开始 AI 面试' }]);
  }
  if (!normalizedState.questions.completed) {
    return normalizeAssistantActions([{ type: 'go_questions', label: '开始试题作答' }]);
  }
  if (!stagePassed('questions', normalizedState.questions)) {
    return normalizeAssistantActions([{ type: 'restart_comprehensive', label: '重新开始 AI 面试' }]);
  }
  if (!normalizedState.round_1.completed) {
    return normalizeAssistantActions([{ type: 'go_round_1', label: '进入技术一面' }]);
  }
  if (!stagePassed('round_1', normalizedState.round_1)) {
    return normalizeAssistantActions([{ type: 'restart_comprehensive', label: '重新开始 AI 面试' }]);
  }
  if (!normalizedState.round_2.completed) {
    return normalizeAssistantActions([{ type: 'go_round_2', label: '进入技术二面' }]);
  }
  if (!stagePassed('round_2', normalizedState.round_2)) {
    return normalizeAssistantActions([{ type: 'restart_comprehensive', label: '重新开始 AI 面试' }]);
  }
  if (!normalizedState.round_3.completed) {
    return normalizeAssistantActions([{ type: 'go_round_3', label: '进入 HR 三面' }]);
  }
  return normalizeAssistantActions([
    { type: 'restart_comprehensive', label: '重新开始 AI 面试' }
  ]);
};

const buildResumeSuggestionLines = (state = comprehensiveState.value) => {
  const suggestions = Array.isArray(state?.resume?.suggestions)
    ? state.resume.suggestions
    : [];
  const safeSuggestions = suggestions
    .map((item) => String(item || '').trim())
    .filter(Boolean)
    .slice(0, 5);
  if (safeSuggestions.length === 0) {
    return [];
  }
  return [
    'AI 改进建议',
    ...safeSuggestions.map((item, index) => `${index + 1}. ${item}`)
  ];
};

const formatStageProgressLine = (label, stageKey, stageState, unlocked = true) => {
  if (!stageState?.completed) {
    return `- ${label}：${unlocked ? '未开始' : '已锁定'}`;
  }
  return `- ${label}：${stagePassed(stageKey, stageState) ? '已通过' : '未通过'}`;
};

const buildLocalCompletionSummary = (pending = {}, state = comprehensiveState.value) => {
  const normalizedState = normalizeComprehensiveState(state);
  const safeType = String(pending?.type || '').trim();
  const safeScore = Math.max(0, Number(pending?.score || 0) || 0);
  const hasScore = Boolean(pending?.hasScore);
  const resumePassScore = getStagePassScore('resume', assessmentThresholds.value);
  const finishedModuleMap = {
    resume_completed: '简历评测',
    questions_completed: '试题作答',
    round_1_completed: '技术一面',
    round_2_completed: '技术二面',
    round_3_completed: 'HR 三面'
  };
  const completedStageMap = {
    resume_completed: 'resume',
    questions_completed: 'questions',
    round_1_completed: 'round_1',
    round_2_completed: 'round_2',
    round_3_completed: 'round_3'
  };
  const completedStage = completedStageMap[safeType] || '';
  const finishedModuleLabel = finishedModuleMap[safeType] || '当前评测';
  const scoreText = hasScore ? `，本次得分 ${safeScore} 分` : '';
  const progressLines = [
    formatStageProgressLine('简历评测', 'resume', normalizedState.resume, true),
    formatStageProgressLine('试题作答', 'questions', normalizedState.questions, canAdvanceFromResume(normalizedState.resume, assessmentThresholds.value)),
    formatStageProgressLine('技术一面', 'round_1', normalizedState.round_1, stagePassed('questions', normalizedState.questions)),
    formatStageProgressLine('技术二面', 'round_2', normalizedState.round_2, stagePassed('round_1', normalizedState.round_1)),
    formatStageProgressLine('HR 三面', 'round_3', normalizedState.round_3, stagePassed('round_2', normalizedState.round_2))
  ];

  if (safeType === 'resume_completed' && !canAdvanceFromResume(normalizedState.resume, assessmentThresholds.value)) {
    return [
      `已确认你已完成 AI 面试中的${finishedModuleLabel}${scoreText}。`,
      `简历评测未通过，当前得分 ${safeScore} 分，低于 ${resumePassScore} 分门槛，本次 AI 面试到此结束。`,
      ...buildResumeSuggestionLines(normalizedState),
      '当前 AI 面试进度',
      ...progressLines,
      '下一步',
      '请先优化简历，然后重新开始 AI 面试。'
    ].join('\n');
  }

  if (completedStage
    && normalizedState[completedStage]?.completed
    && !stagePassed(completedStage, normalizedState[completedStage])) {
    const passScore = getStagePassScore(completedStage, assessmentThresholds.value);
    return [
      `已确认你已完成 AI 面试中的${finishedModuleLabel}${scoreText}。`,
      `${finishedModuleLabel}未通过，当前得分 ${safeScore} 分，低于 ${passScore} 分门槛，本次 AI 面试到此结束。`,
      '当前 AI 面试进度',
      ...progressLines,
      '下一步',
      '请先复盘本阶段表现，然后重新开始 AI 面试。'
    ].join('\n');
  }

  if (safeType === 'round_3_completed') {
    return [
      `已确认你已完成 AI 面试中的${finishedModuleLabel}${scoreText}。`,
      '当前 AI 面试流程已经全部完成。',
      '当前 AI 面试进度',
      ...progressLines,
      '下一步',
      '本轮过程已经保留在当前 AI 对话中，你可以直接在这里回看，也可以重新开始一轮新的 AI 面试。'
    ].join('\n');
  }

  const nextAction = buildDeterministicAssistantActions(normalizedState)[0];
  const nextLabel = getModuleDisplayName(mapModuleFromActionType(nextAction?.type));
  return [
    `已确认你已完成 AI 面试中的${finishedModuleLabel}${scoreText}。`,
    ...(safeType === 'resume_completed' ? buildResumeSuggestionLines(normalizedState) : []),
    '当前 AI 面试进度',
    ...progressLines,
    '下一步',
    nextLabel ? `建议继续进入${nextLabel}。` : '你可以继续查看下方推荐操作。'
  ].join('\n');
};

const appendLocalCompletionFallback = async (pending = {}, fallbackReply = '') => {
  const safeReply = String(fallbackReply || buildLocalCompletionSummary(pending, comprehensiveState.value)).trim();
  assistantActions.value = buildDeterministicAssistantActions(comprehensiveState.value);
  appendSessionEvent('assistant_reply', {
    source: 'local_orchestrator',
    intent: 'stage_progression',
    module: mapModuleFromActionType(assistantActions.value[0]?.type),
    reply: safeReply,
    meta: {
      actions: assistantActions.value,
      fallback: true
    }
  }, { persist: false });
  await appendAiMessageWithTyping(safeReply);
};

const submitCompletionFollowupToAssistant = async (pending = {}, fallbackReply = '') => {
  const followupQuestion = buildCompletionFollowupQuestion(pending);
  if (!followupQuestion) {
    await appendLocalCompletionFallback(pending, fallbackReply);
    return false;
  }
  const submitted = await submitAssistantQuestion(followupQuestion, {
    appendUserMessage: false,
    fallbackReplyBuilder: () => fallbackReply,
    minThinkingDuration: 520
  });
  if (!submitted) {
    errorMessage.value = '';
    isAiServiceUnavailable.value = false;
    await appendAiMessageWithTyping(fallbackReply);
    assistantActions.value = buildDeterministicAssistantActions(comprehensiveState.value);
    persistConversationState();
    return false;
  }
  return true;
};

const handlePendingAssistantAction = async (externalPending = null) => {
  const routePending = consumeRoutePendingAssistantAction();
  const inboxPending = consumeStoredPendingAssistantAction();
  const pending = externalPending || routePending || inboxPending;
  if (!pending) {
    return;
  }
  showAIChat.value = shouldAutoOpenAiChatOnEntry();
  if (pending.chatId) {
    const targetSession = activateConversationByKey(pending.chatId);
    if (!targetSession && isPersistentConversationId(pending.chatId)) {
      const placeholderSession = createSessionState({
        id: '',
        chatId: pending.chatId,
        title: buildUntitledSessionTitle()
      });
      const mergedPlaceholderSession = upsertConversationRecord(placeholderSession, [pending.chatId]);
      applySessionState(mergedPlaceholderSession);
    }
  }
  if (pending) {
    lastHandledPendingActionKey = String(pending.pendingActionKey || '');
    if (!pending.skipStateSync) {
      applyComprehensiveCompletion(pending);
    }
    if (routePending && !externalPending) {
      clearRoutePendingAssistantAction();
    }
  }
  hasInteractedWithAssistant.value = true;
  if (pending.mode) {
    selectedEvaluationMode.value = pending.mode;
  }
  currentJobId.value = String(route.query.jobId || currentJobId.value || '').trim();
  await nextTick();
  scrollToBottom('auto', true);
  const localReply = buildLocalCompletionSummary(pending, comprehensiveState.value);
  await submitCompletionFollowupToAssistant(pending, localReply);
  const completedStageMap = {
    resume_completed: 'resume',
    questions_completed: 'questions',
    round_1_completed: 'round_1',
    round_2_completed: 'round_2',
    round_3_completed: 'round_3'
  };
  const completedStage = completedStageMap[pending.type] || '';
  if (completedStage
    && comprehensiveState.value[completedStage]?.completed
    && !stagePassed(completedStage, comprehensiveState.value[completedStage])) {
    prepareNextComprehensiveSessionAfterFailure();
  }
  await persistConversationStateNow();
};

const handleCompletionEvent = async (data = {}) => {
  const eventPending = buildCompletionEventPendingAction(data);
  if (!eventPending) {
    return;
  }
  await handlePendingAssistantAction(eventPending);
};

const enterPageFlow = async () => {
  if (isEnteringPageFlow.value) {
    return;
  }
  isEnteringPageFlow.value = true;
  try {
    checkJobId();
    if (!hasRestoredConversationState.value) {
      await restoreConversationState();
      hasRestoredConversationState.value = true;
    }
    if (showAIChat.value) {
      await nextTick();
      scrollToBottom('auto', true);
    }
    await handlePendingAssistantAction();
    if (!hasConversationStarted.value) {
      startHomeWelcomeTyping(true);
    }
  } finally {
    isEnteringPageFlow.value = false;
  }
};

// 综合测评特有的状态管理
const comprehensiveState = ref(createDefaultComprehensiveState());
const currentComprehensiveAssessmentId = ref('');

const syncComprehensiveAssessmentState = ({
  jobId = currentJobId.value,
  assessmentId = '',
  createIfMissing = false,
  forceNew = false
} = {}) => {
  const safeJobId = String(jobId || '').trim();
  if (!safeJobId) {
    currentComprehensiveAssessmentId.value = '';
    comprehensiveState.value = createDefaultComprehensiveState();
    return null;
  }
  const session = ensureActiveAiAssessmentSession(safeJobId, {
    assessmentId: assessmentId || currentComprehensiveAssessmentId.value || route.query.assessmentId || '',
    createIfMissing,
    forceNew
  });
  currentComprehensiveAssessmentId.value = String(session?.assessmentId || '').trim();
  comprehensiveState.value = normalizeComprehensiveState(session?.state);
  return session;
};

const persistComprehensiveAssessmentState = (nextState, {
  jobId = currentJobId.value,
  assessmentId = currentComprehensiveAssessmentId.value
} = {}) => {
  const safeJobId = String(jobId || '').trim();
  if (!safeJobId) {
    comprehensiveState.value = normalizeComprehensiveState(nextState);
    return null;
  }
  const savedSession = updateComprehensiveAssessmentState(
    safeJobId,
    assessmentId || route.query.assessmentId || '',
    nextState,
    { createIfMissing: true }
  );
  currentComprehensiveAssessmentId.value = String(savedSession?.assessmentId || currentComprehensiveAssessmentId.value || '').trim();
  comprehensiveState.value = normalizeComprehensiveState(savedSession?.state);
  return savedSession;
};

// AI对话功能
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value;
};

const getMessageInputElement = () => {
  const inputRef = messageInputRef.value;
  if (!inputRef) {
    return null;
  }
  if (typeof inputRef.tagName === 'string') {
    return inputRef;
  }
  const rootElement = inputRef.$el || inputRef;
  if (!rootElement) {
    return null;
  }
  if (typeof rootElement.tagName === 'string') {
    return rootElement;
  }
  return rootElement.querySelector?.('textarea') || null;
};

const focusMessageInput = () => {
  const inputElement = getMessageInputElement();
  if (inputElement && typeof inputElement.focus === 'function') {
    inputElement.focus();
  }
};

const syncMessageInputHeight = (reset = false) => {
  const inputElement = getMessageInputElement();
  if (!inputElement) {
    return;
  }
  inputElement.style.height = 'auto';
  if (reset) {
    inputElement.style.height = '56px';
    return;
  }
  const nextHeight = Math.min(Math.max(inputElement.scrollHeight || 56, 56), 220);
  inputElement.style.height = `${nextHeight}px`;
};

const autoResizeMessageInput = () => {
  syncMessageInputHeight(false);
};

const handleEnterKey = (event) => {
  if (event.shiftKey) {
    return;
  }
  event.preventDefault();
  if (userInput.value.trim()) {
    sendMessage();
  }
};

const inferEvaluationMode = (text) => {
  return String(text || '').trim() ? 'COMPREHENSIVE' : '';
};

const buildConversationHistory = () => {
  return aiMessages.value
    .slice(-20)
    .filter((item) => String(item?.content || '').trim())
    .map((item) => ({
      role: item.type === 'user' ? 'user' : 'assistant',
      content: String(item.content || '')
        .replace(/```json[\s\S]*?```/gi, '结构化结果已生成')
        .replace(/```[\s\S]*?```/g, '内容已省略')
        .replace(/\n{3,}/g, '\n\n')
        .slice(0, 800)
    }));
};

const parseAssistantPayload = (raw) => {
  const normalizePayload = (parsed) => ({
    reply: String(parsed?.reply || ''),
    actions: Array.isArray(parsed?.actions) ? parsed.actions : [],
    intent: String(parsed?.intent || 'general_consultation').trim() || 'general_consultation',
    targetModule: String(parsed?.targetModule || 'NONE').trim() || 'NONE',
    targetMode: String(parsed?.targetMode || 'UNKNOWN').trim().toUpperCase() || 'UNKNOWN',
    targetJobHint: String(parsed?.targetJobHint || '').trim(),
    targetJobConfirmed: Boolean(parsed?.targetJobConfirmed),
    completionAcknowledged: Boolean(parsed?.completionAcknowledged)
  });

  if (raw && typeof raw === 'object') {
    return normalizePayload(raw);
  }

  const text = String(raw || '').trim();
  if (!text) {
    return normalizePayload({});
  }

  let normalized = text;
  if (normalized.startsWith('```json')) {
    normalized = normalized.replace(/^```json/, '').replace(/```$/, '').trim();
  } else if (normalized.startsWith('```')) {
    normalized = normalized.replace(/^```/, '').replace(/```$/, '').trim();
  }

  try {
    const parsed = JSON.parse(normalized);
    return normalizePayload(parsed);
  } catch (_) {
    const match = normalized.match(/\{[\s\S]*\}/);
    if (match) {
      try {
        const parsed = JSON.parse(match[0]);
        return normalizePayload(parsed);
      } catch {}
    }
  }

  return normalizePayload({ reply: text, actions: [] });
};

const normalizeAssistantActions = (actions) => {
  if (!Array.isArray(actions)) {
    return [];
  }
  return actions
    .filter((item) => AVAILABLE_ASSISTANT_ACTION_TYPES.includes(item?.type))
    .map((item) => ({
      type: item.type,
      label: String(item?.label || ASSISTANT_ACTION_LABELS[item.type] || '')
    }))
    .filter((item) => item.label)
    .slice(0, 3);
};

const AI_SERVICE_UNAVAILABLE_MESSAGE = 'AI 服务当前不可用';

const resolveAiRequestError = (error) => {
  const statusCode = Number(
    error?.response?.status
    || error?.response?.data?.code
    || error?.code
    || 0
  );
  const rawMessage = String(
    error?.response?.data?.message
    || error?.message
    || ''
  ).trim();
  const normalizedMessage = rawMessage.toLowerCase();
  const isUnavailable = statusCode === 503
    || rawMessage.includes(AI_SERVICE_UNAVAILABLE_MESSAGE)
    || /余额|欠费|充值|quota|balance|billing|payment required|insufficient|rate limit|service unavailable/i.test(normalizedMessage);

  return {
    isUnavailable,
    message: isUnavailable ? AI_SERVICE_UNAVAILABLE_MESSAGE : '出错啦～请刷新界面重试OnO'
  };
};

const getAIResponse = async (userQuestion, conversationChatId = '') => {
  try {
    const response = await getInterviewAssistantReply({
      question: userQuestion,
      chatId: String(conversationChatId || getActivePersistentSessionId()).trim(),
      evaluationMode: selectedEvaluationMode.value,
      currentJobId: currentJobId.value,
      currentState: comprehensiveState.value,
      assessmentThresholds: assessmentThresholds.value,
      conversationHistory: buildConversationHistory(),
      availableClientActions: AVAILABLE_ASSISTANT_ACTION_TYPES
    });
    if (response.code !== 200) {
      throw new Error(response.message || '获取答案失败');
    }
    return parseAssistantPayload(response.data);
  } catch (error) {
    console.error('API调用失败:', error);
    const resolvedError = resolveAiRequestError(error);
    const nextError = new Error(resolvedError.message);
    nextError.response = error?.response;
    nextError.code = error?.code;
    nextError.isAiServiceUnavailable = resolvedError.isUnavailable;
    throw nextError;
  }
};

const submitAssistantQuestion = async (question, options = {}) => {
  const userQuestion = String(question || '').trim();
  if (!userQuestion) {
    return false;
  }

  const {
    appendUserMessage = true,
    fallbackReplyBuilder = null,
    minThinkingDuration = 420
  } = options;
  errorMessage.value = '';
  isAiServiceUnavailable.value = false;
  pendingSuggestedJob.value = null;

  await trySwitchJobFromQuestion(userQuestion);

  const inferredMode = inferEvaluationMode(userQuestion);
  if (inferredMode) {
    selectedEvaluationMode.value = inferredMode;
  }

  if (appendUserMessage) {
    stopHomeWelcomeTyping();
    aiMessages.value.push({
      type: 'user',
      content: userQuestion,
      time: new Date().toLocaleTimeString()
    });
  }

  const existingTitle = activeConversationRecord.value?.title || '';
  if (!existingTitle || existingTitle.startsWith(DEFAULT_SESSION_TITLE)) {
    const snapshot = buildCurrentSessionSnapshot();
    snapshot.title = createSessionTitleFromMessages(aiMessages.value);
    snapshot.preview = EMPTY_SESSION_PREVIEW;
    const previousActiveId = getActiveConversationUiId();
    upsertConversationRecord(
      snapshot,
      previousActiveId && previousActiveId !== snapshot.id ? [previousActiveId] : []
    );
  }

  assistantActions.value = [];
  hasInteractedWithAssistant.value = true;
  showAIChat.value = true;
  const requestSessionKeys = new Set(collectConversationSessionKeys(activeConversationRecord.value));
  let thinkingMessage = createAssistantThinkingMessage();
  markPendingAssistantSessions(Array.from(requestSessionKeys));
  isTyping.value = true;

  await nextTick();
  scrollToBottom('smooth', true);
  focusMessageInput();

  const requestStartedAt = Date.now();
  try {
    let requestSnapshot = syncCurrentSessionState();
    collectConversationSessionKeys(requestSnapshot).forEach((item) => requestSessionKeys.add(item));
    let conversationChatId = getActivePersistentSessionId(String(requestSnapshot?.chatId || requestSnapshot?.id || '').trim());
    if (!conversationChatId && hasSessionFirstQuestion(requestSnapshot)) {
      requestSnapshot = await flushConversationPersistence(requestSnapshot);
      collectConversationSessionKeys(requestSnapshot).forEach((item) => requestSessionKeys.add(item));
      conversationChatId = getActivePersistentSessionId(String(requestSnapshot?.chatId || requestSnapshot?.id || '').trim());
      if (conversationChatId) {
        requestSessionKeys.add(conversationChatId);
      }
      markPendingAssistantSessions(Array.from(requestSessionKeys));
      if (!aiMessages.value.some((item) => item?.thinking)) {
        thinkingMessage = createAssistantThinkingMessage();
        await nextTick();
        scrollToBottom('smooth', true);
      }
    }
    const payload = await getAIResponse(userQuestion, conversationChatId);
    const assistantReply = String(payload?.reply || '').trim()
      || (typeof fallbackReplyBuilder === 'function' ? String(fallbackReplyBuilder(payload) || '').trim() : '');
    lastAssistantDecision.value = {
      intent: payload.intent,
      targetModule: payload.targetModule,
      targetMode: payload.targetMode,
      targetJobHint: payload.targetJobHint,
      targetJobConfirmed: payload.targetJobConfirmed,
      completionAcknowledged: payload.completionAcknowledged
    };
    if (payload.targetMode) {
      selectedEvaluationMode.value = 'COMPREHENSIVE';
    }
    if (payload.targetJobHint) {
      pendingSuggestedJob.value = await resolveJobByHint(payload.targetJobHint);
    } else if (payload.intent === 'switch_job' || !payload.targetJobConfirmed) {
      pendingSuggestedJob.value = await findMentionedJobFromText(payload.reply);
    } else {
      pendingSuggestedJob.value = null;
    }
    appendSessionEvent('assistant_reply', {
      chatId: conversationChatId,
      source: 'assistant',
      intent: payload.intent,
      module: normalizeTargetModuleValue(payload.targetModule),
      mode: payload.targetMode,
      targetJobHint: payload.targetJobHint,
      reply: assistantReply,
      meta: {
        actions: normalizeAssistantActions(payload.actions),
        targetJobConfirmed: payload.targetJobConfirmed,
        completionAcknowledged: payload.completionAcknowledged
      }
    }, { persist: false });
    if (assistantReply) {
      const minimumThinkingMs = Math.max(420, Number(minThinkingDuration) || 0);
      const elapsedMs = Date.now() - requestStartedAt;
      const remainingThinkingMs = Math.max(0, minimumThinkingMs - elapsedMs);
      if (remainingThinkingMs > 0) {
        await sleep(remainingThinkingMs);
      }
      isTyping.value = false;
      if (!aiMessages.value.includes(thinkingMessage) && hasPendingAssistantForSession({
        id: getActiveConversationUiId(),
        chatId: getActivePersistentSessionId()
      })) {
        thinkingMessage = aiMessages.value.find((item) => item?.thinking) || thinkingMessage;
      }
      await appendAiMessageWithTyping(assistantReply, thinkingMessage);
    } else {
      aiMessages.value = aiMessages.value.filter((item) => item !== thinkingMessage);
      isTyping.value = false;
    }
    clearPendingAssistantSessions(Array.from(requestSessionKeys));
    assistantActions.value = normalizeAssistantActions(payload.actions);
    await nextTick();
    await ensureStreamingScrollToBottom(true);
    persistConversationState();
    return true;
  } catch (error) {
    console.error('获取AI回复失败:', error);
    aiMessages.value = aiMessages.value.filter((item) => item !== thinkingMessage);
    isTyping.value = false;
    clearPendingAssistantSessions(Array.from(requestSessionKeys));
    const resolvedError = resolveAiRequestError(error);
    isAiServiceUnavailable.value = resolvedError.isUnavailable;
    errorMessage.value = resolvedError.message;
    persistConversationState();
    await nextTick();
    scrollToBottom();
    return false;
  }
};

const sendMessage = async () => {
  console.log('sendMessage函数被调用，输入内容:', userInput.value);
  if (!userInput.value.trim()) return;
  const text = userInput.value;
  userInput.value = '';
  nextTick(() => syncMessageInputHeight(true));
  await submitAssistantQuestion(text, { appendUserMessage: true });
};

const addLocalAiMessage = async (content) => {
  if (!content) {
    return;
  }
  await appendAiMessageWithTyping(content);
};

const goToModule = async (moduleType, options = {}) => {
  const moduleNameMap = {
    resume: '简历评测',
    questions: '试题作答',
    round_1: '技术一面',
    round_2: '技术二面',
    round_3: 'HR 三面'
  };
  appendSessionEvent('module_navigation_prompted', {
    source: String(options?.source || 'assistant_action').trim(),
    module: moduleType,
    jobId: String(options?.jobId || currentJobId.value || '').trim(),
    actionType: String(options?.actionType || '').trim(),
    label: String(options?.label || '').trim()
  }, { persist: false });
  await addLocalAiMessage(`正在为你准备进入${moduleNameMap[moduleType] || '对应模块'}。`);
  persistConversationState();
  handleModuleClick(moduleType, options);
};

const runAssistantAction = async (action) => {
  if (!action?.type) {
    return;
  }

  assistantActions.value = [];
  let explicitTargetJobId = '';
  const resolvedTargetModule = resolveTargetModuleForAction(action);
  appendSessionEvent('assistant_action_clicked', {
    source: 'assistant_action',
    actionType: String(action.type || '').trim(),
    label: String(action.label || '').trim(),
    module: resolvedTargetModule,
    intent: String(lastAssistantDecision.value?.intent || '').trim(),
    targetJobHint: String(lastAssistantDecision.value?.targetJobHint || '').trim()
  }, { persist: false });

  const targetJob = await resolveSuggestedJobForAction();
  if (targetJob?.id
      && ['go_resume', 'go_questions', 'go_round_1', 'go_round_2', 'go_round_3', 'restart_comprehensive'].includes(action.type)) {
    explicitTargetJobId = String(targetJob.id).trim();
    await switchCurrentJobContext(targetJob, {
      source: 'assistant_action',
      actionType: String(action.type || '').trim()
    });
  }

  switch (action.type) {
    case 'go_resume':
      await goToModule(resolvedTargetModule || 'resume', {
        jobId: explicitTargetJobId,
        source: 'assistant_action',
        actionType: action.type,
        label: action.label
      });
      return;
    case 'go_questions':
      await goToModule(resolvedTargetModule || 'questions', {
        jobId: explicitTargetJobId,
        source: 'assistant_action',
        actionType: action.type,
        label: action.label
      });
      return;
    case 'go_round_1':
      await goToModule(resolvedTargetModule || 'round_1', {
        jobId: explicitTargetJobId,
        source: 'assistant_action',
        actionType: action.type,
        label: action.label
      });
      return;
    case 'go_round_2':
      await goToModule(resolvedTargetModule || 'round_2', {
        jobId: explicitTargetJobId,
        source: 'assistant_action',
        actionType: action.type,
        label: action.label
      });
      return;
    case 'go_round_3':
      await goToModule(resolvedTargetModule || 'round_3', {
        jobId: explicitTargetJobId,
        source: 'assistant_action',
        actionType: action.type,
        label: action.label
      });
      return;
    case 'restart_comprehensive':
      await handleRestartComprehensiveAssessment();
      return;
    default:
      return;
  }
};

const resolveJobDetails = async () => {
  if (!currentJobId.value) {
    return null;
  }
  try {
    const response = await getJobInfo(currentJobId.value);
    return response?.code === 200 ? response.data : null;
  } catch (error) {
    console.error('获取岗位详情失败:', error);
    return null;
  }
};

const normalizeResumeAnalysisText = (responseData) => {
  if (!responseData) {
    return '';
  }
  if (typeof responseData === 'string') {
    return responseData;
  }
  return JSON.stringify(responseData, null, 2);
};

const tryParseAnalysisObject = (responseData) => {
  if (!responseData) {
    return null;
  }
  if (typeof responseData === 'object') {
    return responseData;
  }
  try {
    let text = String(responseData).trim();
    if (text.startsWith('```json')) {
      text = text.replace(/^```json/, '').replace(/```$/, '').trim();
    } else if (text.startsWith('```')) {
      text = text.replace(/^```/, '').replace(/```$/, '').trim();
    }
    return JSON.parse(text);
  } catch (_) {
    return null;
  }
};

const extractResumeScoreFromAnalysis = (responseData) => {
  const parsed = tryParseAnalysisObject(responseData);
  if (!parsed || typeof parsed !== 'object') {
    return 0;
  }
  return Number(
    parsed.score ??
    parsed.overallScore ??
    parsed.overall_score ??
    0
  ) || 0;
};

const markResumeCompletedInComprehensive = (score) => {
  const safeScore = Number(score) || 0;
  const now = formatDateTimeUtc8();
  persistComprehensiveAssessmentState((currentState) => {
    const nextState = normalizeComprehensiveState(currentState);
    nextState.resume = {
      ...nextState.resume,
      completed: true,
      inProgress: false,
      score: safeScore,
      analysisId: nextState.resume.analysisId || `chat-${Date.now()}`,
      startTime: nextState.resume.startTime || now,
      endTime: now,
      attempts: Math.max(1, Number(nextState.resume.attempts || 0) + 1)
    };
    nextState.overall = {
      ...nextState.overall,
      startTime: nextState.overall.startTime || now,
      status: 'in_progress'
    };
    return nextState;
  });
  appendSessionEvent('module_completed', {
    source: 'chat_resume_analysis',
    module: 'resume',
    mode: 'COMPREHENSIVE',
    completionType: 'resume_completed',
    score: safeScore
  }, { persist: false });
  persistConversationState();
};

const buildResumeSummary = (analysisData) => {
  const parsed = tryParseAnalysisObject(analysisData);
  if (parsed && typeof parsed === 'object') {
    const score = Number(parsed.score ?? parsed.overallScore ?? parsed.overall_score ?? 0) || 0;
    const feedback = parsed.feedback || {};
    const advantages = String(feedback.advantages || '').split(/\n+/).map((item) => item.trim()).filter(Boolean)[0] || '';
    const disadvantages = String(feedback.disadvantages || '').split(/\n+/).map((item) => item.trim()).filter(Boolean)[0] || '';
    const suggestion = String(
      feedback.improvementSuggestions
      ?? feedback.improvement_suggestions
      ?? ''
    ).split(/\n+/).map((item) => item.trim()).filter(Boolean)[0] || '';

    const summaryLines = [`简历分析已完成，当前得分 ${score} 分。`];
    if (advantages) {
      summaryLines.push(`亮点：${advantages}`);
    }
    if (disadvantages) {
      summaryLines.push(`待提升：${disadvantages}`);
    }
    if (suggestion) {
      summaryLines.push(`建议：${suggestion}`);
    }
    summaryLines.push('你可以继续下一步评测，或让我根据这次结果继续给你建议。');
    return summaryLines.join('\n');
  }

  const safeText = String(normalizeResumeAnalysisText(analysisData) || '').trim();
  if (!safeText) {
    return '简历分析已完成，你可以继续追问我具体优化建议，或者点击下方模块继续后续测评。';
  }
  const cleaned = safeText
    .replace(/```json/g, '')
    .replace(/```/g, '')
    .trim();
  if (cleaned.length <= 240) {
    return `简历分析已完成，先给你结论：\n${cleaned}`;
  }
  return `简历分析已完成，先给你摘要：\n${cleaned.slice(0, 240)}...`;
};

const analyzeResumeInChat = async (file) => {
  if (!file) {
    return;
  }
  if (!currentJobId.value) {
    uni.showToast({ title: '请先选择岗位', icon: 'none' });
    return;
  }

  isResumeUploading.value = true;
  errorMessage.value = '';
  isAiServiceUnavailable.value = false;
  try {
    const effectiveMode = 'COMPREHENSIVE';
    selectedEvaluationMode.value = effectiveMode;
    await addLocalAiMessage(
      '收到简历，我会先完成 AI 面试里的简历评测，结束后会回到这里，由我继续给你推荐下一步。'
    );

    const extractRes = await extractResumeContent(file);
    if (extractRes.code !== 200 || !extractRes.data) {
      throw new Error(extractRes.message || '简历内容提取失败');
    }

    const resumeContent = extractRes.data;
    const jobDetails = await resolveJobDetails();

    const analysisResponse = await analyzeComprehensiveResume(resumeContent, jobDetails || {});

    if (analysisResponse.code !== 200 || !analysisResponse.data) {
      throw new Error(analysisResponse.message || '简历分析失败');
    }

    const summary = buildResumeSummary(analysisResponse.data);
    const resumeScore = extractResumeScoreFromAnalysis(analysisResponse.data);

    markResumeCompletedInComprehensive(resumeScore);

    await addLocalAiMessage(summary);
    await submitAssistantQuestion(
      '我刚完成了 AI 面试中的简历评测，请结合当前进度告诉我下一步，并给出可执行按钮。',
      { appendUserMessage: false }
    );
  } catch (error) {
    console.error('聊天内简历分析失败:', error);
    const resolvedError = resolveAiRequestError(error);
    isAiServiceUnavailable.value = resolvedError.isUnavailable;
    errorMessage.value = resolvedError.isUnavailable
      ? resolvedError.message
      : (error.message || '聊天内简历分析失败，请重试');
  } finally {
    isResumeUploading.value = false;
  }
};

const chooseResumeInChat = () => {
  uni.chooseFile({
    count: 1,
    type: 'file',
    extension: ['.pdf', '.doc', '.docx', '.txt'],
    success: async (res) => {
      const file = res.tempFiles?.[0];
      if (!file) {
        return;
      }
      await analyzeResumeInChat(file);
    },
    fail: (error) => {
      console.error('选择简历失败:', error);
      isAiServiceUnavailable.value = false;
      errorMessage.value = '选择简历失败，请重试';
    }
  });
};

const waitForNextPaint = () => new Promise((resolve) => {
  if (typeof requestAnimationFrame === 'function') {
    requestAnimationFrame(() => resolve());
    return;
  }
  setTimeout(resolve, 16);
});

const getChatScrollContainer = () => {
  const target = chatMessages.value;
  if (!target) {
    return null;
  }
  if (typeof target.scrollTo === 'function' || typeof target.scrollTop === 'number') {
    return target;
  }
  const root = target.$el || target;
  if (!root) {
    return null;
  }
  if (typeof root.scrollTo === 'function' || typeof root.scrollTop === 'number') {
    return root;
  }
  return root.querySelector?.('.chat-messages') || null;
};

const syncContainerToBottom = (container) => {
  if (!container) {
    return;
  }
  container.scrollTop = container.scrollHeight;
  previousChatScrollTop.value = Math.max(container.scrollTop || 0, 0);
};

const ensureStreamingScrollToBottom = async (force = false) => {
  if (force) {
    isChatAutoScrollEnabled.value = true;
  }
  if (!force && !isChatAutoScrollEnabled.value) {
    return;
  }
  ignoreChatScrollEventUntil.value = Date.now() + 240;
  const container = getChatScrollContainer();
  if (!container) {
    return;
  }
  syncContainerToBottom(container);
  await waitForNextPaint();
  syncContainerToBottom(getChatScrollContainer());
};

async function startHomeWelcomeTyping(forceRestart = false) {
  if (!forceRestart && homeWelcomeText.value === HOME_WELCOME_MESSAGE) {
    return;
  }
  const currentSeq = ++homeWelcomeTypingSeq;
  homeWelcomeText.value = '';
  const chunkSize = 2;
  for (let index = 0; index < HOME_WELCOME_MESSAGE.length; index += chunkSize) {
    if (currentSeq !== homeWelcomeTypingSeq || hasConversationStarted.value) {
      return;
    }
    homeWelcomeText.value = HOME_WELCOME_MESSAGE.slice(0, index + chunkSize);
    await sleep(40);
  }
}

const getStreamingDelay = (char, totalLength) => {
  if (char === '\n') {
    return 280;
  }
  if ('，,、'.includes(char)) {
    return 130;
  }
  if ('。！？；：.!?;:'.includes(char)) {
    return 240;
  }
  if (/\s/.test(char)) {
    return 36;
  }
  if (totalLength > 260) {
    return 32;
  }
  if (totalLength > 160) {
    return 40;
  }
  return 48;
};

const createAssistantThinkingMessage = () => {
  const message = {
    type: 'ai',
    content: '',
    time: new Date().toLocaleTimeString(),
    thinking: true,
    streaming: false
  };
  aiMessages.value.push(message);
  return message;
};

const appendAiMessageWithTyping = async (content, targetMessage = null) => {
  const fullContent = String(content || '');
  const aiMessage = targetMessage || {
    type: 'ai',
    content: '',
    time: new Date().toLocaleTimeString(),
    thinking: false,
    streaming: true
  };

  if (!targetMessage) {
    aiMessages.value.push(aiMessage);
  }
  aiMessage.thinking = false;
  aiMessage.streaming = true;
  aiMessage.content = '';

  await nextTick();
  await ensureStreamingScrollToBottom(true);

  let rendered = '';
  for (let index = 0; index < fullContent.length; index += 1) {
    rendered += fullContent[index];
    aiMessage.content = rendered;
    await nextTick();
    await ensureStreamingScrollToBottom();
    await sleep(getStreamingDelay(fullContent[index], fullContent.length));
  }
  aiMessage.streaming = false;
  aiMessage.thinking = false;
  await nextTick();
  persistConversationState();
};


const isNearChatBottom = () => {
  const container = getChatScrollContainer();
  if (!container) {
    return true;
  }
  const distanceToBottom = container.scrollHeight - container.scrollTop - container.clientHeight;
  return distanceToBottom <= CHAT_AUTO_SCROLL_THRESHOLD;
};

const handleChatMessagesScroll = () => {
  const container = getChatScrollContainer();
  if (!container) {
    isChatAutoScrollEnabled.value = true;
    previousChatScrollTop.value = 0;
    return;
  }
  const currentScrollTop = Math.max(container.scrollTop || 0, 0);
  if (Date.now() < ignoreChatScrollEventUntil.value) {
    previousChatScrollTop.value = currentScrollTop;
    return;
  }
  const movedUp = currentScrollTop < previousChatScrollTop.value - 4;
  const distanceToBottom = container.scrollHeight - currentScrollTop - container.clientHeight;

  if (movedUp) {
    // 用户手动上滑后，立即暂停自动跟随，避免被流式输出强行拉回底部。
    isChatAutoScrollEnabled.value = false;
  } else if (distanceToBottom <= CHAT_AUTO_SCROLL_RESUME_THRESHOLD) {
    // 只有用户重新回到底部附近时，才恢复自动跟随。
    isChatAutoScrollEnabled.value = true;
  }

  previousChatScrollTop.value = currentScrollTop;
};

const scrollToBottom = (behavior = 'smooth', force = false) => {
  const container = getChatScrollContainer();
  if (force) {
    isChatAutoScrollEnabled.value = true;
  }
  if (!force && !isChatAutoScrollEnabled.value) {
    return;
  }
  ignoreChatScrollEventUntil.value = Date.now() + 180;
  if (container?.scrollTo) {
    container.scrollTo({
      top: container.scrollHeight,
      behavior
    });
    requestAnimationFrame(() => {
      const latestContainer = getChatScrollContainer();
      previousChatScrollTop.value = Math.max(latestContainer?.scrollTop || 0, 0);
    });
    return;
  }
  if (container) {
    syncContainerToBottom(container);
    return;
  }
  if (chatMessagesEnd.value?.scrollIntoView) {
    chatMessagesEnd.value.scrollIntoView({
      block: 'end',
      behavior
    });
  }
};

const handleRestartComprehensiveAssessment = async () => {
  selectedEvaluationMode.value = 'COMPREHENSIVE';
  const restartedSession = resetComprehensiveState();
  assistantActions.value = buildDeterministicAssistantActions(comprehensiveState.value);
  appendSessionEvent('assistant_reply', {
    source: 'local_orchestrator',
    intent: 'restart_assessment',
    module: 'resume',
    reply: '好的，已为你重置本轮 AI 面试全部环节。所有进度已清空，现在从头开始：简历评测、试题作答、技术一面、技术二面、HR 三面。准备好后，点击下方按钮开始简历评测。',
    meta: {
      assessmentId: String(restartedSession?.assessmentId || currentComprehensiveAssessmentId.value || '').trim(),
      actions: assistantActions.value
    }
  }, { persist: false });
  await appendAiMessageWithTyping('好的，已为你重置本轮 AI 面试全部环节。所有进度已清空，现在从头开始：\n- 简历评测\n- 试题作答\n- 技术一面\n- 技术二面\n- HR 三面\n\n准备好后，点击下方按钮开始简历评测。');
  persistConversationState();
};

const resetComprehensiveState = () => {
  const safeJobId = String(currentJobId.value || '').trim();
  if (!safeJobId) {
    currentComprehensiveAssessmentId.value = '';
    comprehensiveState.value = createDefaultComprehensiveState();
    assistantActions.value = [];
    return null;
  }
  clearActiveComprehensiveAssessment(safeJobId);
  const newSession = createNewAiAssessmentSession(safeJobId);
  currentComprehensiveAssessmentId.value = String(newSession?.assessmentId || '').trim();
  comprehensiveState.value = normalizeComprehensiveState(newSession?.state);
  assistantActions.value = [];
  appendSessionEvent('assessment_restarted', {
    source: 'assistant_action',
    jobId: safeJobId,
    meta: {
      assessmentId: currentComprehensiveAssessmentId.value
    }
  }, { persist: false });
  return newSession;
};

const prepareNextComprehensiveSessionAfterFailure = () => {
  const safeJobId = String(currentJobId.value || '').trim();
  if (!safeJobId) {
    return null;
  }
  clearActiveComprehensiveAssessment(safeJobId);
  const nextSession = createNewAiAssessmentSession(safeJobId);
  currentComprehensiveAssessmentId.value = String(nextSession?.assessmentId || '').trim();
  comprehensiveState.value = normalizeComprehensiveState(nextSession?.state);
  appendSessionEvent('assessment_auto_reinitialized_after_failure', {
    source: 'local_orchestrator',
    jobId: safeJobId,
    meta: {
      assessmentId: String(nextSession?.assessmentId || '').trim()
    }
  }, { persist: false });
  return nextSession;
};

const applyComprehensiveCompletion = ({ type = '', mode = '', score = 0, suggestions = [] } = {}) => {
  const safeType = String(type || '').trim();
  const safeMode = String(mode || '').trim().toUpperCase();
  if (!safeType) {
    return;
  }
  const moduleMap = {
    resume_completed: 'resume',
    questions_completed: 'questions',
    round_1_completed: 'round_1',
    round_2_completed: 'round_2',
    round_3_completed: 'round_3'
  };
  const safeScore = Math.max(0, Number(score) || 0);
  const now = formatDateTimeUtc8();
  persistComprehensiveAssessmentState((currentState) => {
    const nextState = normalizeComprehensiveState(currentState);
    const isFinalRound = safeType === 'round_3_completed';
    const completedStage = moduleMap[safeType] || '';
    const completedStagePassed = completedStage
      ? hasStagePassed(completedStage, { score: safeScore }, assessmentThresholds.value)
      : true;
    const resumePassed = safeType === 'resume_completed'
      ? completedStagePassed
      : canAdvanceFromResume(nextState.resume, assessmentThresholds.value);
    const nextStageMap = {
      resume_completed: resumePassed ? 'questions' : 'resume',
      questions_completed: completedStagePassed ? 'round_1' : 'questions',
      round_1_completed: completedStagePassed ? 'round_2' : 'round_1',
      round_2_completed: completedStagePassed ? 'round_3' : 'round_2',
      round_3_completed: completedStagePassed ? 'completed' : 'round_3'
    };
    if (safeMode === 'COMPREHENSIVE' || !safeMode) {
      nextState.overall = {
        ...nextState.overall,
        startTime: nextState.overall.startTime || now,
        endTime: isFinalRound || !completedStagePassed ? now : nextState.overall.endTime,
        status: !completedStagePassed
          ? 'failed'
          : (isFinalRound ? 'completed' : 'in_progress'),
        currentStage: nextStageMap[safeType] || nextState.overall.currentStage,
        eliminationReason: !completedStagePassed
          ? `${completedStage}_score_below_threshold`
          : nextState.overall.eliminationReason
      };
    }
    if (safeType === 'resume_completed') {
      const safeSuggestions = Array.isArray(suggestions)
        ? suggestions.map((item) => String(item || '').trim()).filter(Boolean)
        : [];
      nextState.resume = {
        ...nextState.resume,
        completed: true,
        inProgress: false,
        score: safeScore,
        passed: resumePassed,
        suggestions: safeSuggestions.length > 0 ? safeSuggestions : nextState.resume.suggestions,
        analysisId: nextState.resume.analysisId || `resume-${Date.now()}`,
        startTime: nextState.resume.startTime || now,
        endTime: now,
        attempts: Math.max(1, Number(nextState.resume.attempts || 0) + 1)
      };
    } else if (safeType === 'questions_completed') {
      nextState.questions = {
        ...nextState.questions,
        completed: true,
        inProgress: false,
        score: safeScore,
        passed: completedStagePassed,
        interviewId: nextState.questions.interviewId || `questions-${Date.now()}`,
        startTime: nextState.questions.startTime || now,
        endTime: now,
        attempts: Math.max(1, Number(nextState.questions.attempts || 0) + 1)
      };
    } else if (safeType === 'round_1_completed' || safeType === 'round_2_completed' || safeType === 'round_3_completed') {
      const stageKey = safeType.replace('_completed', '');
      nextState[stageKey] = {
        ...nextState[stageKey],
        completed: true,
        inProgress: false,
        score: safeScore,
        passed: completedStagePassed,
        interviewId: nextState[stageKey].interviewId || `${stageKey}-${Date.now()}`,
        startTime: nextState[stageKey].startTime || now,
        endTime: now,
        attempts: Math.max(1, Number(nextState[stageKey].attempts || 0) + 1)
      };
      nextState.overall.totalScore = Math.round((
        Number(nextState.resume.score || 0)
        + Number(nextState.questions.score || 0)
        + Number(nextState.round_1.score || 0)
        + Number(nextState.round_2.score || 0)
        + Number(nextState.round_3.score || 0)
      ) / 5);
    }
    return nextState;
  });
  appendSessionEvent('module_completed', {
    source: 'module_page',
    module: moduleMap[safeType] || '',
    mode: safeMode || selectedEvaluationMode.value || '',
    completionType: safeType,
    score: safeScore
  }, { persist: false });
  persistConversationState();
};

// 检查 jobId
const checkJobId = () => {
  let jobIdFromRoute = (route.query.jobId || '').toString().trim();
  let jobIdFromStorage = (uni.getStorageSync('currentJobId') || '').toString().trim();
  if (jobIdFromRoute) {
    currentJobId.value = jobIdFromRoute;
    uni.setStorageSync('currentJobId', jobIdFromRoute);
    return;
  }
  const jobIdFromSession = String(activeConversationRecord.value?.jobId || '').trim();
  if (jobIdFromSession) {
    currentJobId.value = jobIdFromSession;
    uni.setStorageSync('currentJobId', jobIdFromSession);
  } else if (jobIdFromStorage) {
    currentJobId.value = jobIdFromStorage;
  } else {
    currentJobId.value = null;
  }
};

// 在 setup 里或最前面立即执行
checkJobId();

onShow(async () => {
    await enterPageFlow();
});

// 处理模块点击
const handleModuleClick = async (moduleType, options = {}) => {
  const navigationSource = String(options?.source || 'manual_click').trim();
  const shouldReturnToAiChat = navigationSource === 'assistant_action';
  const forcedMode = String(options?.mode || '').trim().toUpperCase();
  if (navigationSource === 'manual_click' && !forcedMode) {
    selectedEvaluationMode.value = 'COMPREHENSIVE';
  } else if (forcedMode) {
    selectedEvaluationMode.value = forcedMode;
  }
  const effectiveJobId = String(options?.jobId || currentJobId.value || activeConversationRecord.value?.jobId || '').trim();
  let currentSnapshot = syncCurrentSessionState();
  const eventPayload = {
    source: navigationSource,
    module: moduleType,
    jobId: effectiveJobId,
    mode: selectedEvaluationMode.value,
    actionType: String(options?.actionType || '').trim(),
    label: String(options?.label || '').trim()
  };

  const isComprehensiveMode = selectedEvaluationMode.value === 'COMPREHENSIVE';
  const comprehensiveSession = isComprehensiveMode
    ? syncComprehensiveAssessmentState({
        jobId: effectiveJobId,
        assessmentId: currentComprehensiveAssessmentId.value || route.query.assessmentId || '',
        createIfMissing: Boolean(effectiveJobId)
      })
    : null;
  const effectiveAssessmentId = String(
    options?.assessmentId
    || comprehensiveSession?.assessmentId
    || currentComprehensiveAssessmentId.value
    || route.query.assessmentId
    || ''
  ).trim();
  const routes = {
    resume: '/pages/ai-resume/index',
    questions: '/pages/ai-questions/index',
    round_1: '/pages/ai-scenario/index',
    round_2: '/pages/ai-scenario/index',
    round_3: '/pages/ai-scenario/index'
  };

  // 已完成模块禁止点击
  if (isComprehensiveMode && moduleType === 'resume' && comprehensiveState.value.resume.completed) {
    appendSessionEvent('module_navigation_blocked', {
      ...eventPayload,
      reason: 'resume_already_completed'
    });
    uni.showToast({
      title: '简历评测作答已结束',
      icon: 'none',
      duration: 2000
    });
    return;
  }
  if (isComprehensiveMode && moduleType === 'questions' && comprehensiveState.value.questions.completed) {
    appendSessionEvent('module_navigation_blocked', {
      ...eventPayload,
      reason: 'questions_already_completed'
    });
    uni.showToast({
      title: '试题作答已结束',
      icon: 'none',
      duration: 2000
    });
    return;
  }
  if (isComprehensiveMode && ['round_1', 'round_2', 'round_3'].includes(moduleType) && comprehensiveState.value[moduleType]?.completed) {
    appendSessionEvent('module_navigation_blocked', {
      ...eventPayload,
      reason: `${moduleType}_already_completed`
    });
    uni.showToast({
      title: `${getModuleDisplayName(moduleType)}已结束`,
      icon: 'none',
      duration: 2000
    });
    return;
  }

  // 检查前置条件
  if (isComprehensiveMode && moduleType === 'questions' && !comprehensiveState.value.resume.completed) {
    appendSessionEvent('module_navigation_blocked', {
      ...eventPayload,
      reason: 'resume_not_completed'
    });
    uni.showToast({
      title: '请先完成简历评测',
      icon: 'none',
      duration: 2000
    });
    return;
  }

  if (isComprehensiveMode && moduleType === 'questions' && !canAdvanceFromResume(comprehensiveState.value.resume, assessmentThresholds.value)) {
    appendSessionEvent('module_navigation_blocked', {
      ...eventPayload,
      reason: 'resume_score_below_threshold'
    });
    uni.showToast({
      title: `简历需达到 ${getStagePassScore('resume', assessmentThresholds.value)} 分`,
      icon: 'none',
      duration: 2000
    });
    return;
  }

  if (isComprehensiveMode && moduleType === 'round_1' && !comprehensiveState.value.questions.completed) {
    appendSessionEvent('module_navigation_blocked', {
      ...eventPayload,
      reason: 'questions_not_completed'
    });
    uni.showToast({
      title: '请先完成试题作答',
      icon: 'none',
      duration: 2000
    });
    return;
  }

  if (isComprehensiveMode && moduleType === 'round_1' && !stagePassed('questions', comprehensiveState.value.questions)) {
    appendSessionEvent('module_navigation_blocked', {
      ...eventPayload,
      reason: 'questions_score_below_threshold'
    });
    uni.showToast({
      title: `试题作答需达到 ${getStagePassScore('questions', assessmentThresholds.value)} 分`,
      icon: 'none',
      duration: 2000
    });
    return;
  }

  if (isComprehensiveMode && moduleType === 'round_2' && !comprehensiveState.value.round_1.completed) {
    appendSessionEvent('module_navigation_blocked', {
      ...eventPayload,
      reason: 'round_1_not_completed'
    });
    uni.showToast({
      title: '请先完成技术一面',
      icon: 'none',
      duration: 2000
    });
    return;
  }

  if (isComprehensiveMode && moduleType === 'round_2' && !stagePassed('round_1', comprehensiveState.value.round_1)) {
    appendSessionEvent('module_navigation_blocked', {
      ...eventPayload,
      reason: 'round_1_score_below_threshold'
    });
    uni.showToast({
      title: `技术一面需达到 ${getStagePassScore('round_1', assessmentThresholds.value)} 分`,
      icon: 'none',
      duration: 2000
    });
    return;
  }

  if (isComprehensiveMode && moduleType === 'round_3' && !comprehensiveState.value.round_2.completed) {
    appendSessionEvent('module_navigation_blocked', {
      ...eventPayload,
      reason: 'round_2_not_completed'
    });
    uni.showToast({
      title: '请先完成技术二面',
      icon: 'none',
      duration: 2000
    });
    return;
  }

  if (isComprehensiveMode && moduleType === 'round_3' && !stagePassed('round_2', comprehensiveState.value.round_2)) {
    appendSessionEvent('module_navigation_blocked', {
      ...eventPayload,
      reason: 'round_2_score_below_threshold'
    });
    uni.showToast({
      title: `技术二面需达到 ${getStagePassScore('round_2', assessmentThresholds.value)} 分`,
      icon: 'none',
      duration: 2000
    });
    return;
  }

  if (routes[moduleType]) {
    // 确保传递jobId参数
    if (effectiveJobId) {
      // 记录来源为AI面试页面
      const query = [
        `jobId=${encodeURIComponent(effectiveJobId)}`,
        `from=${encodeURIComponent(shouldReturnToAiChat ? 'ai-interview' : 'interview-main')}`,
        `autoOpen=${shouldReturnToAiChat ? '1' : '0'}`,
        `mode=${encodeURIComponent(selectedEvaluationMode.value || '')}`
      ];
      if (effectiveAssessmentId && isComprehensiveMode) {
        query.push(`assessmentId=${encodeURIComponent(effectiveAssessmentId)}`);
      }
      if (moduleType === 'round_1' || moduleType === 'round_2' || moduleType === 'round_3') {
        query.push(`round=${encodeURIComponent(moduleType)}`);
      }
      currentSnapshot = await flushConversationPersistence(currentSnapshot, effectiveJobId);
      const effectiveSessionId = getActivePersistentSessionId(String(currentSnapshot?.chatId || currentSnapshot?.id || '').trim());
      if (effectiveSessionId && shouldReturnToAiChat) {
        query.push(`chatId=${encodeURIComponent(effectiveSessionId)}`);
      }
      const routeUrl = `${routes[moduleType]}?${query.join('&')}`;
      appendSessionEvent('module_navigation_started', {
        ...eventPayload,
        meta: {
          route: routeUrl
        }
      });
      smoothPush(routeUrl);
    } else {
      await flushConversationPersistence(currentSnapshot);
      appendSessionEvent('module_navigation_started', {
        ...eventPayload,
        meta: {
          route: routes[moduleType]
        }
      });
      smoothPush(routes[moduleType]);
      console.warn(`跳转到${moduleType}页面，但没有jobId可传递`);
    }
  }
};

// 生命周期钩子
onMounted(async () => {
  await enterPageFlow();

  uni.$on('resumeCompleted', handleCompletionEvent);

});

onBeforeUnmount(() => {
  persistConversationState();
  try {
    uni.$off('resumeCompleted', handleCompletionEvent);
  } catch (e) {
    console.warn('[Comprehensive Test] 清理事件监听器失败:', e);
  }
});

// uni-app 生命周期
onLoad(() => {
});

const escapeHtml = (value) => String(value || '')
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;')
  .replace(/'/g, '&#39;');

const renderInlineMarkdown = (text) => {
  let rendered = escapeHtml(text);
  rendered = rendered.replace(/`([^`]+)`/g, '<code>$1</code>');
  rendered = rendered.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
  rendered = rendered.replace(/\*([^*]+)\*/g, '<em>$1</em>');
  rendered = rendered.replace(
    /(https?:\/\/[^\s<]+)/g,
    '<a href="$1" target="_blank" rel="noopener noreferrer">$1</a>'
  );
  return rendered;
};

const formatMessage = (content) => {
  const source = String(content || '').replace(/\r\n/g, '\n').trim();
  if (!source) {
    return '';
  }

  const lines = source.split('\n');
  const html = [];
  let inList = false;

  const closeListIfNeeded = () => {
    if (inList) {
      html.push('</ul>');
      inList = false;
    }
  };

  for (const rawLine of lines) {
    const line = rawLine.trim();

    if (!line) {
      closeListIfNeeded();
      continue;
    }

    if (/^---+$/.test(line) || /^\*\*\*+$/.test(line)) {
      closeListIfNeeded();
      html.push('<hr>');
      continue;
    }

    const headingMatch = line.match(/^(#{1,3})\s+(.*)$/);
    if (headingMatch) {
      closeListIfNeeded();
      const level = headingMatch[1].length;
      html.push(`<h${level}>${renderInlineMarkdown(headingMatch[2])}</h${level}>`);
      continue;
    }

    if (/^[-*]\s+/.test(line)) {
      if (!inList) {
        html.push('<ul>');
        inList = true;
      }
      html.push(`<li>${renderInlineMarkdown(line.replace(/^[-*]\s+/, ''))}</li>`);
      continue;
    }

    const orderedMatch = line.match(/^\d+\.\s+(.*)$/);
    if (orderedMatch) {
      if (!inList) {
        html.push('<ul>');
        inList = true;
      }
      html.push(`<li>${renderInlineMarkdown(orderedMatch[1])}</li>`);
      continue;
    }

    closeListIfNeeded();
    html.push(`<p>${renderInlineMarkdown(line)}</p>`);
  }

  closeListIfNeeded();
  return html.join('');
};

// 清空聊天记录
const clearChat = () => {
  resetAssistantSession();
  nextTick(() => {
    scrollToBottom('smooth', true);
    focusMessageInput();
  });
};

// 正在输入状态
const isTyping = ref(false);

// 消息输入框引用
const messageInputRef = ref(null);

// 错误处理相关状态
const errorMessage = ref('');
const isAiServiceUnavailable = ref(false);

// 重试上一次的请求
const retry = async () => {
  // 获取最后一条用户消息
  const lastUserMessage = [...aiMessages.value].reverse().find(msg => msg.type === 'user');
  
  if (lastUserMessage) {
    errorMessage.value = '';
    isAiServiceUnavailable.value = false;
    await submitAssistantQuestion(lastUserMessage.content, { appendUserMessage: false });
  }
};

// 返回按钮逻辑，始终跳转到/pages/ai-hall/index并带jobId
const handleBack = () => {
  // 获取jobId，优先从currentJobId、route、localStorage
  let jobId = null;
  try {
    jobId = currentJobId?.value || route?.query?.jobId || localStorage.getItem('currentJobId') || uni.getStorageSync('currentJobId');
  } catch (e) {}
  if (!jobId) jobId = 1; // fallback
  smoothReLaunch(`/pages/ai-hall/index?jobId=${jobId}`);
};
</script>

<style lang="scss" scoped>
@use "sass:math";
@use "sass:string";
/* 完全隐藏滚动条 */
* {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

*::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

@keyframes slideIn {
  from { 
    transform: translateY(20px) scale(0.95); 
    opacity: 0; 
  }
  to { 
    transform: translateY(0) scale(1); 
    opacity: 1; 
  }
}

/* 科幻背景效果 */
.ai-interview-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 50%, #16213e 100%);
  position: relative;
  overflow-x: hidden;
  overflow-y: hidden;
  font-family: 'Arial', sans-serif;
  color: #ffffff;
  height: 100vh;
  display: flex;
  flex-direction: column;
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
.ai-interview-container.eye-care .page-transition-overlay {
  background: radial-gradient(circle at 50% 50%, rgba(127,176,105,0.18), rgba(0,0,0,0.40));
}

/* AI助手界面 */
.ai-assistant {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 1000;
}

@keyframes corePulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.8; }
}

.ai-chat-shell {
  position: relative;
  z-index: 3;
  width: 100%;
  min-height: 100vh;
  display: flex;
}

.ai-chat-container {
  width: 100%;
  min-height: 100vh;
  height: 100vh;
  background:
    linear-gradient(180deg, rgba(0, 0, 0, 0.92), rgba(4, 10, 18, 0.96)),
    radial-gradient(circle at top right, rgba(0, 255, 255, 0.08), transparent 22%);
  border: none;
  border-radius: 0;
  box-shadow: none;
  display: flex;
  overflow: hidden;
  transition: background 0.3s ease;
  animation: slideIn 0.25s ease forwards;
}

.chat-sidebar {
  width: 280px;
  flex-shrink: 0;
  background: linear-gradient(180deg, rgba(2, 18, 32, 0.96), rgba(0, 8, 16, 0.92));
  border-right: 1px solid rgba(0, 255, 255, 0.16);
  display: flex;
  flex-direction: column;
}

.ai-chat-container.sidebar-collapsed .chat-sidebar {
  width: 84px;
  pointer-events: none;
  overflow: visible;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 64px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(0, 255, 255, 0.12);
  pointer-events: auto;
}

.ai-chat-container.sidebar-collapsed .sidebar-header {
  width: 64px;
  padding: 0 10px;
}

.sidebar-toolbar {
  display: inline-flex;
  align-items: center;
  gap: 0;
}

.sidebar-brand {
  font-size: 18px;
  font-weight: 700;
  line-height: 1;
  letter-spacing: -0.03em;
}

.sidebar-icon-btn {
  all: unset;
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: relative;
  color: rgba(255, 255, 255, 0.84);
  cursor: pointer;
  transition: color 0.18s ease, opacity 0.18s ease;
}

.new-chat-btn {
  all: unset;
  width: 28px;
  height: 28px;
  color: rgba(255, 255, 255, 0.9);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: color 0.18s ease, opacity 0.18s ease;
  padding: 0;
  outline: none !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  box-sizing: border-box;
}

.new-chat-btn:hover {
  color: #ffffff;
  opacity: 0.9;
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;
}

.sidebar-toggle-btn {
  all: unset;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 40px;
  pointer-events: auto;
  color: rgba(255, 255, 255, 0.9);
  width: 40px;
  height: 40px;
  cursor: pointer;
  transition: color 0.18s ease, opacity 0.18s ease;
  padding: 0;
  outline: none !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  box-sizing: border-box;
  margin: 0 auto;
  position: relative;
  z-index: 2;
}

.sidebar-toggle-btn:hover {
  color: rgba(255, 255, 255, 0.9);
  opacity: 0.9;
  border: none !important;
  box-shadow: none !important;
}

.toolbar-icon-svg {
  display: block;
}

.tooltip-bubble {
  position: absolute;
  z-index: 30;
  pointer-events: none;
  opacity: 0;
  visibility: hidden;
  white-space: nowrap;
  padding: 8px 11px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.72);
  border: 1px solid rgba(186, 230, 253, 0.18);
  backdrop-filter: blur(14px) saturate(145%);
  -webkit-backdrop-filter: blur(14px) saturate(145%);
  color: #ffffff;
  font-size: 12px;
  line-height: 1;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.24);
  transform: translateX(-50%) translateY(-4px);
  transition: opacity 0.18s ease, transform 0.18s ease, visibility 0.18s ease;
}

.tooltip-right {
  left: 50%;
  right: auto;
  top: calc(100% + 10px);
  bottom: auto;
  transform: translateX(-50%) translateY(-4px);
}

.sidebar-icon-btn:hover .tooltip-bubble,
.sidebar-icon-btn:focus-visible .tooltip-bubble,
.sidebar-primary-action:hover .tooltip-bubble,
.sidebar-primary-action:focus-visible .tooltip-bubble,
.chat-back-btn:hover .tooltip-bubble,
.chat-back-btn:focus-visible .tooltip-bubble {
  opacity: 1;
  visibility: visible;
}

.sidebar-icon-btn:hover .tooltip-right,
.sidebar-icon-btn:focus-visible .tooltip-right,
.sidebar-primary-action:hover .tooltip-right,
.sidebar-primary-action:focus-visible .tooltip-right {
  transform: translateX(-50%) translateY(0);
}


button.new-chat-btn,
button.sidebar-toggle-btn,
button.session-delete-btn {
  border: 0 !important;
  background: none !important;
  box-shadow: none !important;
  outline: none !important;
  border-radius: 0 !important;
  -webkit-appearance: none !important;
  appearance: none !important;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sidebar-primary-action {
  margin: 16px 12px 10px;
  min-height: 48px;
  border: none;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  gap: 12px;
  background: rgba(255, 255, 255, 0.92);
  color: #111827;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.18s ease;
}

.sidebar-primary-action:hover {
  background: #ffffff;
  transform: translateY(-1px);
}

.ai-chat-container.sidebar-collapsed .sidebar-primary-action {
  margin: 8px auto 0;
  width: 40px;
  height: 40px;
  min-height: 40px;
  border-radius: 0;
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 0;
  justify-content: center;
}

.sidebar-section-label {
  padding: 14px 20px 6px;
  font-size: 12px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.42);
}

.session-item {
  position: relative;
  border: 1px solid rgba(0, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.04);
  border-radius: 12px;
  min-height: 48px;
  height: 48px;
  padding: 0 28px 0 12px;
  cursor: pointer;
  transition: border-color 0.16s ease, background-color 0.16s ease;
  overflow: hidden;
}

.session-item:hover,
.session-item.active {
  border-color: rgba(0, 255, 255, 0.35);
  background: rgba(0, 255, 255, 0.08);
}

.session-main {
  min-width: 0;
  height: 100%;
  display: flex;
  align-items: center;
}

.session-title {
  color: #ffffff;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-delete-btn {
  all: unset;
  position: absolute;
  top: 50%;
  right: 10px;
  transform: translateY(-50%);
  width: auto;
  height: auto;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.72);
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  opacity: 0.52;
  pointer-events: auto;
  transition: color 0.16s ease, opacity 0.16s ease;
  outline: none !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  box-sizing: border-box;
}

.session-delete-btn:hover {
  color: #ff6f6f;
  opacity: 1;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  position: relative;
  min-height: 64px;
  padding: 0 24px 0 18px;
  border-bottom: 1px solid rgba(0, 255, 255, 0.16);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(0, 0, 0, 0.4);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-title-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.chat-session-name {
  color: rgba(255, 255, 255, 0.64);
  font-size: 12px;
}

.header-new-chat-btn {
  flex-shrink: 0;
  margin-left: auto;
  position: absolute;
  top: 18px;
  right: 22px;
}

.chat-back-btn {
  all: unset;
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: relative;
  color: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  margin-right: 12px;
  flex-shrink: 0;
  transition: color 0.18s ease, opacity 0.18s ease;
}

.chat-back-btn:hover {
  color: rgba(255, 255, 255, 0.9);
  opacity: 0.9;
}

.ai-interview-container.dedicated-page {
  background: linear-gradient(180deg, #f5f6f8 0%, #f1f3f6 100%);
}

.ai-interview-container.dedicated-page .ai-assistant {
  position: relative;
  right: auto;
  bottom: auto;
  z-index: 3;
  width: 100%;
  min-height: 100vh;
  padding: 0;
  display: flex;
}

.ai-interview-container.dedicated-page .ai-chat-shell {
  width: 100%;
  max-width: none;
  min-height: 100vh;
  margin: 0;
  display: flex;
}

.ai-interview-container.dedicated-page .ai-chat-container {
  min-height: 100vh;
  height: 100vh;
  background: transparent;
  border: none;
  border-radius: 0;
  box-shadow: none;
}

.ai-interview-container.dedicated-page .chat-sidebar {
  width: 240px;
  background: #f3f4f6;
  border-right: 1px solid #e5e7eb;
}

.ai-interview-container.dedicated-page .ai-chat-container.sidebar-collapsed .chat-sidebar {
  width: 64px;
  pointer-events: auto;
}

.ai-interview-container.dedicated-page .sidebar-header {
  justify-content: space-between;
  min-height: 56px;
  padding: 0 12px 0 16px;
  border-bottom: none;
  pointer-events: auto;
}

.ai-interview-container.dedicated-page .sidebar-brand {
  color: #111827;
}

.ai-interview-container.dedicated-page .sidebar-toolbar {
  margin-left: 0;
  width: auto;
  justify-content: flex-end;
}

.ai-interview-container.dedicated-page .ai-chat-container:not(.sidebar-collapsed) .sidebar-toolbar {
  width: auto;
  justify-content: flex-end;
}

.ai-interview-container.dedicated-page .sidebar-toggle-btn,
.ai-interview-container.dedicated-page .sidebar-icon-btn,
.ai-interview-container.dedicated-page .chat-back-btn {
  color: #6b7280;
  width: 28px;
  height: 28px;
  flex: 0 0 28px;
}

.ai-interview-container.dedicated-page .sidebar-toggle-btn:hover,
.ai-interview-container.dedicated-page .sidebar-icon-btn:hover,
.ai-interview-container.dedicated-page .chat-back-btn:hover {
  color: #6b7280;
}

.ai-interview-container.dedicated-page .sidebar-icon-btn:hover {
  background: transparent;
}

.ai-interview-container.dedicated-page .sidebar-primary-action {
  margin: 8px 12px 10px;
  min-height: 40px;
  justify-content: flex-start;
  padding: 0 10px;
  background: transparent;
  border: none;
  border-radius: 0;
  color: #111827;
  box-shadow: none;
  gap: 10px;
  font-weight: 400;
}

.ai-interview-container.dedicated-page .sidebar-primary-action:hover {
  background: rgba(17, 24, 39, 0.04);
  transform: none;
}

.ai-interview-container.dedicated-page .ai-chat-container.sidebar-collapsed .sidebar-toggle-btn,
.ai-interview-container.dedicated-page .ai-chat-container.sidebar-collapsed .sidebar-icon-btn,
.ai-interview-container.dedicated-page .ai-chat-container.sidebar-collapsed .sidebar-primary-action {
  background: transparent !important;
  border: none !important;
  border-radius: 0 !important;
  box-shadow: none !important;
}

.ai-interview-container.dedicated-page .ai-chat-container.sidebar-collapsed .sidebar-header {
  justify-content: center;
  padding: 0;
}

.ai-interview-container.dedicated-page .ai-chat-container.sidebar-collapsed .sidebar-toolbar {
  width: 100%;
  justify-content: center;
}

.ai-interview-container.dedicated-page .ai-chat-container.sidebar-collapsed .sidebar-primary-action {
  width: 28px;
  height: 28px;
  min-height: 28px;
  margin: 12px auto 0;
  padding: 0;
  justify-content: center;
  align-items: center;
}

.ai-interview-container.dedicated-page .sidebar-section-label {
  color: #9ca3af;
  padding: 12px 18px 6px;
}

.ai-interview-container.dedicated-page .session-list {
  padding: 6px 10px 16px;
  gap: 6px;
}

.ai-interview-container.dedicated-page .session-item {
  min-height: 40px;
  height: 40px;
  padding: 0 28px 0 12px;
  border: none;
  background: transparent;
  border-radius: 10px;
}

.ai-interview-container.dedicated-page .session-item:hover,
.ai-interview-container.dedicated-page .session-item.active {
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
}

.ai-interview-container.dedicated-page .session-title {
  color: #374151;
  font-weight: 500;
}

.ai-interview-container.dedicated-page .session-delete-btn {
  color: #9ca3af;
}

.ai-interview-container.dedicated-page .session-delete-btn:hover {
  color: #ef4444;
}

.ai-interview-container.dedicated-page .chat-main {
  background: #f7f7f8;
}

.ai-interview-container.dedicated-page .chat-header {
  min-height: 56px;
  padding: 0 20px 0 16px;
  border-bottom: 1px solid #eceff3;
  background: #ffffff;
}

.ai-interview-container.dedicated-page .chat-session-name {
  color: #6b7280;
}

.ai-interview-container.dedicated-page .header-new-chat-btn {
  top: 14px;
  right: 18px;
}

.ai-interview-container.dedicated-page .chat-home-panel {
  padding: 72px 48px 24px;
  gap: 18px;
  background: #f7f7f8;
}

.ai-interview-container.dedicated-page .chat-home-title {
  color: #111827;
  font-size: clamp(36px, 4vw, 48px);
  font-weight: 600;
  letter-spacing: -0.03em;
}

.ai-interview-container.dedicated-page .welcome-stream-text::after {
  background: rgba(17, 24, 39, 0.75);
}

.ai-interview-container.dedicated-page .chat-messages {
  background: #f7f7f8;
  padding: 28px 48px 18px;
}

.ai-interview-container.dedicated-page .message.user .message-bubble {
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  border: 1px solid rgba(147, 197, 253, 0.42);
  box-shadow: 0 12px 28px rgba(59, 130, 246, 0.12);
}

.ai-interview-container.dedicated-page .message.ai .message-content {
  color: #1f2937;
}

.ai-interview-container.dedicated-page .message.ai .message-time {
  color: #9ca3af;
}

.ai-interview-container.dedicated-page .message.user .message-time {
  color: rgba(30, 58, 138, 0.68);
}

.ai-interview-container.dedicated-page .message-content {
  color: #111827;
}

.ai-interview-container.dedicated-page .message-content.streaming::after {
  background: rgba(17, 24, 39, 0.7);
}

.ai-interview-container.dedicated-page .inline-thinking,
.ai-interview-container.dedicated-page .inline-thinking-text {
  color: #ffffff;
}

.ai-interview-container.dedicated-page .inline-thinking-dots i {
  background: #3b82f6;
}

.ai-interview-container.dedicated-page .chat-input {
  padding: 18px 48px 28px;
  border-top: none;
  background: #f7f7f8;
  align-items: center;
  gap: 12px;
}

.ai-interview-container.dedicated-page .input-wrapper {
  width: min(760px, 100%);
  background: #f8f8f9;
  border: 1px solid #d7dce3;
  border-radius: 30px;
  min-height: 120px;
  padding: 16px 16px 14px 18px;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 12px;
  box-shadow: none;
  transition:
    border-color 0.18s ease,
    background-color 0.18s ease,
    box-shadow 0.18s ease;
}

.ai-interview-container.dedicated-page .input-wrapper:focus-within {
  background: #ffffff;
  border-color: #b8d0ff;
  box-shadow: 0 0 0 2px rgba(191, 219, 254, 0.45);
}

.ai-interview-container.dedicated-page .message-input {
  color: #111827;
  text-shadow: none;
  width: 100%;
  min-height: 56px;
  max-height: 220px;
  padding: 0;
  resize: none;
  overflow-y: auto;
  line-height: 1.7;
  border: none;
  outline: none;
}

.ai-interview-container.dedicated-page .message-input::placeholder {
  color: #9ca3af;
  font-style: normal;
}

.ai-interview-container.dedicated-page .message-input:focus::placeholder {
  color: #cbd5e1;
}

.ai-interview-container.dedicated-page .message-input:focus {
  color: #111827;
  text-shadow: none;
}

.ai-interview-container.dedicated-page .clear-btn .clear-icon {
  color: #9ca3af;
  filter: none;
}

.ai-interview-container.dedicated-page .clear-btn:hover .clear-icon {
  color: #374151;
}

.ai-interview-container.dedicated-page {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0a1929 0%, #1a1f35 100%);
}

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
  animation: animChatStar 150s linear infinite;
}

#stars2 {
  width: 2px;
  height: 2px;
  box-shadow: $shadows-medium;
  animation: animChatStar 100s linear infinite;
}

#stars3 {
  width: 3px;
  height: 3px;
  box-shadow: $shadows-large;
  animation: animChatStar 50s linear infinite;
}

@keyframes animChatStar {
  from {
    transform: translateY(0px);
  }
  to {
    transform: translateY(-2560px);
  }
}

.ai-interview-container.dedicated-page .ai-assistant,
.ai-interview-container.dedicated-page .ai-chat-shell,
.ai-interview-container.dedicated-page .ai-chat-container {
  position: relative;
  z-index: 1;
}

.ai-interview-container.dedicated-page .ai-chat-glass-panel {
  background: transparent;
}

.ai-interview-container.dedicated-page .chat-sidebar,
.ai-interview-container.dedicated-page .chat-header,
.ai-interview-container.dedicated-page .input-wrapper,
.ai-interview-container.dedicated-page .recommended-action-card {
  background: rgba(255, 255, 255, 0.10);
  border: 1px solid rgba(186, 230, 253, 0.24);
  backdrop-filter: blur(18px) saturate(145%);
  -webkit-backdrop-filter: blur(18px) saturate(145%);
}

.ai-interview-container.dedicated-page .chat-sidebar {
  margin: 16px 0 16px 16px;
  border-radius: 22px;
  color: #ffffff;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.14);
}

.ai-interview-container.dedicated-page .sidebar-brand,
.ai-interview-container.dedicated-page .sidebar-primary-action,
.ai-interview-container.dedicated-page .session-title {
  color: #ffffff;
}

.ai-interview-container.dedicated-page .sidebar-section-label,
.ai-interview-container.dedicated-page .chat-session-name {
  color: rgba(255, 255, 255, 0.62);
}

.ai-interview-container.dedicated-page .sidebar-toggle-btn,
.ai-interview-container.dedicated-page .sidebar-icon-btn,
.ai-interview-container.dedicated-page .chat-back-btn {
  color: rgba(255, 255, 255, 0.78);
}

.ai-interview-container.dedicated-page .sidebar-primary-action:hover,
.ai-interview-container.dedicated-page .session-item:hover,
.ai-interview-container.dedicated-page .session-item.active {
  background: rgba(255, 255, 255, 0.12);
  box-shadow: none;
}

.ai-interview-container.dedicated-page .sidebar-primary-action {
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.16), rgba(125, 211, 252, 0.10));
  border: 1px solid rgba(186, 230, 253, 0.22);
}

.ai-interview-container.dedicated-page .sidebar-primary-action:hover {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.22), rgba(125, 211, 252, 0.16));
}

.ai-interview-container.dedicated-page .chat-main,
.ai-interview-container.dedicated-page .chat-home-panel,
.ai-interview-container.dedicated-page .chat-messages,
.ai-interview-container.dedicated-page .chat-input {
  background: transparent;
}

.ai-interview-container.dedicated-page .chat-header {
  margin: 16px 16px 0;
  border-radius: 18px;
}

.ai-interview-container.dedicated-page .chat-home-title {
  color: #ffffff;
  text-shadow: 0 14px 36px rgba(15, 23, 42, 0.38);
}

.ai-interview-container.dedicated-page .welcome-stream-text::after,
.ai-interview-container.dedicated-page .message-content.streaming::after {
  background: rgba(255, 255, 255, 0.82);
}

.ai-interview-container.dedicated-page .message.user .message-bubble {
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(186, 230, 253, 0.28);
  backdrop-filter: blur(16px) saturate(150%);
  -webkit-backdrop-filter: blur(16px) saturate(150%);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.18);
}

.ai-interview-container.dedicated-page .message-content,
.ai-interview-container.dedicated-page .message.user .message-content,
.ai-interview-container.dedicated-page .message.ai .message-content {
  color: #ffffff;
}

.ai-interview-container.dedicated-page .message.ai .message-time,
.ai-interview-container.dedicated-page .message.user .message-time {
  color: rgba(255, 255, 255, 0.68);
}

.ai-interview-container.dedicated-page .recommended-action-card {
  background: rgba(255, 255, 255, 0.12);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.16);
}

.ai-interview-container.dedicated-page .recommended-action-title,
.ai-interview-container.dedicated-page .recommended-action-btn {
  color: rgba(255, 255, 255, 0.92);
}

.ai-interview-container.dedicated-page .recommended-action-desc,
.ai-interview-container.dedicated-page .message-input::placeholder {
  color: rgba(255, 255, 255, 0.58);
}

.ai-interview-container.dedicated-page .input-wrapper {
  background: rgba(255, 255, 255, 0.10);
  border: 1px solid rgba(186, 230, 253, 0.24);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.12);
}

.ai-interview-container.dedicated-page .input-wrapper:focus-within {
  background: rgba(255, 255, 255, 0.14);
  border-color: rgba(186, 230, 253, 0.52);
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.16);
}

.ai-interview-container.dedicated-page .message-input,
.ai-interview-container.dedicated-page .message-input:focus {
  color: #ffffff;
}

.ai-interview-container.dedicated-page .recommended-action-btn {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(186, 230, 253, 0.28);
}


.chat-messages {
  flex: 1;
  padding: 20px 24px 30px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
  /* Hide scrollbar */
  -ms-overflow-style: none;  /* IE and Edge */
  scrollbar-width: none;  /* Firefox */
  background: rgba(0, 10, 20, 0.3);
}

.chat-messages-end {
  height: 6px;
  flex-shrink: 0;
}

.chat-home-panel {
  flex: 1;
  padding: 40px 48px 18px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
  text-align: center;
}

.chat-home-title {
  font-size: 34px;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: -0.02em;
}

.welcome-stream-text {
  min-height: 44px;
}

.welcome-stream-text::after {
  content: '';
  display: inline-block;
  width: 1px;
  height: 0.95em;
  margin-left: 4px;
  vertical-align: -0.08em;
  background: rgba(255, 255, 255, 0.85);
  animation: welcomeCaretBlink 1s steps(1) infinite;
}

@keyframes welcomeCaretBlink {
  0%, 50% { opacity: 1; }
  50.01%, 100% { opacity: 0; }
}

.chat-messages::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera*/
}

.message {
  max-width: 85%;
  position: relative;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message.user {
  align-self: flex-end;
}

.message.ai {
  align-self: flex-start;
  max-width: min(92%, 760px);
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  position: relative;
  margin-bottom: 5px;
}

.message.user .message-bubble {
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  border-bottom-right-radius: 4px;
  border: 1px solid rgba(147, 197, 253, 0.42);
  box-shadow: 0 12px 28px rgba(59, 130, 246, 0.12);
}

.message.ai .message-bubble {
  background: transparent;
  border: none;
  border-radius: 0;
  box-shadow: none;
  padding: 0;
  margin-bottom: 4px;
}

.message.ai .message-bubble.streaming {
  box-shadow: none;
}

.message.ai .message-bubble.thinking {
  min-width: 132px;
}

.message-content {
  color: #ffffff;
  line-height: 1.5;
  font-size: 15px;
  white-space: normal;
}

.message.user .message-content {
  color: #111827;
}

.message.ai .message-content {
  color: rgba(255, 255, 255, 0.94);
}

.message-content.streaming::after {
  content: '';
  display: inline-block;
  width: 1px;
  height: 1em;
  margin-left: 3px;
  vertical-align: -0.08em;
  background: rgba(255, 255, 255, 0.9);
  animation: welcomeCaretBlink 1s steps(1) infinite;
}

.inline-thinking {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.9);
  min-height: 28px;
}

.inline-thinking-text {
  font-size: 14px;
  letter-spacing: 0.01em;
}

.inline-thinking-dots {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.inline-thinking-dots i {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #72e7ff;
  display: inline-block;
  animation: thinkingDotPulse 1.2s infinite ease-in-out both;
}

.inline-thinking-dots i:nth-child(2) {
  animation-delay: 0.16s;
}

.inline-thinking-dots i:nth-child(3) {
  animation-delay: 0.32s;
}

.message-content a {
  color: #00ffff;
  text-decoration: underline;
}

.message-content :deep(p) {
  margin: 0 0 8px;
}

.message-content :deep(p:last-child) {
  margin-bottom: 0;
}

.message-content :deep(h1),
.message-content :deep(h2),
.message-content :deep(h3) {
  margin: 0 0 8px;
  font-size: 15px;
  line-height: 1.5;
}

.message-content :deep(ul) {
  margin: 0 0 8px;
  padding-left: 18px;
}

.message-content :deep(li) {
  margin-bottom: 4px;
}

.message-content :deep(code) {
  padding: 1px 4px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 13px;
}

.message-content :deep(hr) {
  border: none;
  border-top: 1px solid rgba(255, 255, 255, 0.18);
  margin: 8px 0;
}

.message-time {
  font-size: 11px;
  opacity: 0.6;
  margin-top: 2px;
  padding: 0 5px;
}

.message.ai .message-time {
  color: rgba(255, 255, 255, 0.72);
}

.message.user .message-time {
  text-align: right;
  color: rgba(30, 58, 138, 0.68);
}

.error-message {
  align-self: flex-start;
  background: rgba(255, 77, 77, 0.1);
  border: 1px solid rgba(255, 77, 77, 0.3);
  border-radius: 18px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ff4d4d;
  font-size: 14px;
  animation: fadeIn 0.3s ease;
  margin-top: 5px;
}

.error-message.service-unavailable {
  background: rgba(255, 159, 67, 0.12);
  border-color: rgba(255, 159, 67, 0.35);
  color: #ff9f43;
}

.error-message .retry-btn {
  background: none;
  border: 1px solid #ff4d4d;
  color: #ff4d4d;
  border-radius: 12px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.error-message .retry-btn:hover {
  background: rgba(255, 77, 77, 0.1);
}

.error-message.service-unavailable .retry-btn {
  border-color: #ff9f43;
  color: #ff9f43;
}

.error-message.service-unavailable .retry-btn:hover {
  background: rgba(255, 159, 67, 0.12);
}

@keyframes thinkingDotPulse {
  0%, 80%, 100% { transform: scale(0.7); opacity: 0.35; }
  40% { transform: scale(1); opacity: 1; }
}

.chat-input {
  padding: 16px 22px 20px;
  border-top: 1px solid rgba(0, 255, 255, 0.3);
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: stretch;
  background: rgba(0, 0, 0, 0.5);
}

.recommended-action-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid rgba(191, 219, 254, 0.9);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(239, 246, 255, 0.96));
  box-shadow: 0 10px 26px rgba(148, 163, 184, 0.1);
}

.recommended-action-text {
  min-width: 0;
}

.recommended-action-title {
  font-size: 13px;
  font-weight: 600;
  color: #1e3a8a;
  margin-bottom: 4px;
}

.recommended-action-desc {
  font-size: 12px;
  line-height: 1.5;
  color: #475569;
}

.recommended-action-btn {
  flex-shrink: 0;
  border: 1px solid rgba(147, 197, 253, 0.5);
  border-radius: 999px;
  background: linear-gradient(135deg, #f8fbff, #dbeafe);
  color: #0f172a;
  padding: 9px 14px;
  font-size: 12px;
  cursor: pointer;
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.recommended-action-btn:hover {
  transform: translateY(-1px);
}

.recommended-action-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  transform: none;
}

.chat-quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-btn {
  border: 1px solid rgba(0, 229, 255, 0.28);
  background: rgba(0, 229, 255, 0.08);
  color: #dffcff;
  border-radius: 999px;
  padding: 8px 12px;
  font-size: 12px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-btn:hover {
  background: rgba(0, 229, 255, 0.16);
  border-color: rgba(0, 229, 255, 0.45);
}

.quick-btn.active {
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.42), rgba(0, 229, 255, 0.35));
  border-color: rgba(0, 229, 255, 0.7);
  color: #ffffff;
}

.quick-btn.highlighted {
  background: linear-gradient(135deg, rgba(0, 229, 255, 0.28), rgba(59, 130, 246, 0.26));
  border-color: rgba(0, 229, 255, 0.85);
  color: #ffffff;
  box-shadow: 0 0 0 1px rgba(0, 229, 255, 0.18), 0 0 14px rgba(0, 229, 255, 0.2);
}

.quick-btn.upload {
  border-color: rgba(87, 255, 168, 0.35);
  background: rgba(87, 255, 168, 0.12);
}

.quick-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  position: relative;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 18px;
  padding: 0 14px;
  border: 1px solid rgba(0, 255, 255, 0.3);
  min-height: 58px;
}

.message-input {
  flex: 1;
  background: transparent;
  border: none;
  padding: 12px 10px;
  color: #ffffff;
  outline: none;
  font-size: 15px;
  font-family: inherit;
  transition: all 0.2s ease;
}

.message-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
  font-style: italic;
}

.message-input:focus::placeholder {
  color: rgba(255, 255, 255, 0.7);
}

.message-input:focus {
  color: #ffffff;
  text-shadow: 0 0 5px rgba(0, 255, 255, 0.3);
}

.input-actions {
  display: flex;
  align-items: center;
}

.action-btn {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  padding: 5px;
  transition: all 0.2s ease;
  border-radius: 4px;
}

.action-btn:hover {
  color: #ffffff;
  transform: scale(1.05);
  background: rgba(255, 255, 255, 0.1);
}

.clear-btn .clear-icon { color: #00e5ff; opacity: 0.85; filter: drop-shadow(0 0 4px rgba(0, 229, 255, 0.3)); transition: transform 0.2s ease, color 0.2s ease, opacity 0.2s ease; }
.clear-btn { display: inline-flex; align-items: center; justify-content: center; padding: 0; margin: 0; background: transparent !important; border: none !important; border-radius: 0 !important; outline: none !important; box-shadow: none !important; -webkit-appearance: none; appearance: none; cursor: pointer; }
.clear-btn:hover { background: transparent !important; box-shadow: none; }
.clear-btn:focus { outline: none; box-shadow: none; }
.clear-btn:hover .clear-icon { transform: scale(1.15); color: #7c4dff; opacity: 1; }

.action-icon {
  font-size: 18px;
}

/* 清空图标为 SVG，保持透明背景与放大效果 */

.send-btn {
  height: 40px;
  border-radius: 20px;
  background: linear-gradient(135deg, #00ffff, #0080ff);
  border: none;
  color: #ffffff !important;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(0, 255, 255, 0.3);
  padding: 0 20px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.send-icon {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
  color: #ffffff !important;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 2px 10px rgba(0, 255, 255, 0.2);
  color: #ffffff !important;
}

.send-btn:not(:disabled):hover {
  transform: scale(1.05);
  box-shadow: 0 2px 15px rgba(0, 255, 255, 0.5);
  background: linear-gradient(135deg, #00ffff, #00ccff);
  color: #ffffff !important;
}


/* 护眼模式适配 */
.eye-care .ai-avatar {
  background: linear-gradient(45deg, #3e8e41, #357a38);
  box-shadow: 0 0 20px rgba(62, 142, 65, 0.5);
}

.eye-care .ai-avatar:hover {
  box-shadow: 0 0 40px rgba(62, 142, 65, 0.8);
}

.eye-care .ai-core {
  background: #f0f2e6;
}

.eye-care .ai-pulse {
  border-color: #3e8e41;
}

.eye-care .notification-badge {
  background: #e74c3c;
}

.eye-care .ai-chat-container {
  background: linear-gradient(180deg, rgba(246, 247, 236, 0.98), rgba(236, 240, 223, 0.96));
  border-color: rgba(99, 138, 88, 0.55);
  box-shadow: 0 10px 34px rgba(92, 122, 85, 0.22);
}

.eye-care .chat-sidebar {
  background: linear-gradient(180deg, rgba(224, 232, 210, 0.98), rgba(212, 223, 197, 0.96));
  border-right-color: rgba(103, 136, 93, 0.24);
}

.eye-care .sidebar-header {
  border-bottom-color: rgba(103, 136, 93, 0.18);
}

.eye-care .new-chat-btn,
.eye-care .sidebar-toggle-btn {
  color: #4d6848;
  border: none !important;
  outline: none !important;
  background: transparent !important;
  box-shadow: none !important;
  border-radius: 0 !important;
  -webkit-appearance: none;
  appearance: none;
}

.eye-care .new-chat-btn:hover {
  color: #2f4b2e;
  border: none !important;
  outline: none !important;
  background: transparent !important;
  box-shadow: none !important;
}

.eye-care .sidebar-toggle-btn:hover {
  color: #4d6848;
  border: none !important;
  outline: none !important;
  background: transparent !important;
  box-shadow: none !important;
}

.eye-care .session-list::-webkit-scrollbar-thumb {
  background: rgba(115, 150, 103, 0.42);
}

.eye-care .session-item {
  border-color: rgba(103, 136, 93, 0.2);
  background: rgba(255, 255, 255, 0.58);
  box-shadow: 0 3px 10px rgba(110, 139, 98, 0.08);
}

.eye-care .session-item:hover,
.eye-care .session-item.active {
  border-color: rgba(88, 126, 77, 0.45);
  background: rgba(238, 245, 229, 0.95);
  box-shadow: 0 6px 16px rgba(103, 136, 93, 0.14);
}

.eye-care .session-title {
  color: #2f3d2d;
}

.eye-care .session-delete-btn {
  color: rgba(70, 82, 64, 0.78);
  border: none !important;
  outline: none !important;
  background: transparent !important;
  box-shadow: none !important;
  border-radius: 0 !important;
  -webkit-appearance: none;
  appearance: none;
}

.eye-care .session-delete-btn:hover {
  color: #b85f5f;
  opacity: 1;
  border: none !important;
  outline: none !important;
  background: transparent !important;
  box-shadow: none !important;
}

.eye-care .chat-header {
  background: linear-gradient(180deg, rgba(222, 231, 208, 0.72), rgba(233, 239, 221, 0.62));
  border-color: rgba(99, 138, 88, 0.32);
}

.eye-care .toolbar-icon-svg {
  color: inherit;
}

.eye-care .chat-session-name {
  color: rgba(74, 95, 69, 0.72);
}

.eye-care .chat-back-btn {
  color: #486244;
}

.eye-care .chat-back-btn:hover {
  color: #486244;
}

.eye-care .close-chat {
  color: #486244;
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

.eye-care .chat-messages {
  background: linear-gradient(180deg, rgba(239, 243, 229, 0.78), rgba(232, 237, 220, 0.56));
}

.eye-care .message.user .message-bubble {
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  border: 1px solid rgba(147, 197, 253, 0.42);
  box-shadow: 0 12px 28px rgba(59, 130, 246, 0.12);
}

.eye-care .message.ai .message-bubble {
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 0;
}

.eye-care .message-content {
  color: #2f352d;
}

.eye-care .message.ai .message-content {
  color: #3f4f38;
  font-weight: 500;
}

.eye-care .recommended-action-card {
  border-color: rgba(191, 219, 254, 0.9);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(239, 246, 255, 0.96));
  box-shadow: 0 10px 26px rgba(148, 163, 184, 0.1);
}

.eye-care .recommended-action-title {
  color: #1e3a8a;
}

.eye-care .recommended-action-desc {
  color: #475569;
}

.eye-care .recommended-action-btn {
  background: linear-gradient(135deg, #f8fbff, #dbeafe);
  border: 1px solid rgba(147, 197, 253, 0.5);
  color: #0f172a;
}

.eye-care .message.user .message-content {
  color: #111827;
}

.eye-care .chat-home-title,
.eye-care .welcome-stream-text {
  color: #5d6f52;
}

.eye-care .welcome-stream-text::after {
  background: rgba(93, 111, 82, 0.78);
}

.eye-care .message-content a {
  color: #486244;
}

.eye-care .message.user .message-content a {
  color: #eff8ea;
}

.eye-care .message-content :deep(code) {
  background: rgba(103, 136, 93, 0.14);
  color: #31402d;
}

.eye-care .message-content :deep(hr) {
  border-top-color: rgba(103, 136, 93, 0.28);
}

.eye-care .message.ai .message-content.streaming::after {
  background: rgba(72, 98, 68, 0.85);
}

.eye-care .message.ai .message-time {
  color: rgba(72, 98, 68, 0.6);
  opacity: 1;
}

.eye-care .message.user .message-time {
  color: rgba(30, 58, 138, 0.68);
  opacity: 1;
}

.eye-care .inline-thinking,
.eye-care .inline-thinking-text {
  color: #ffffff;
}

.eye-care .inline-thinking-dots i {
  background: #8fc7cf;
}

.eye-care .chat-quick-actions {
  gap: 10px;
}

.eye-care .quick-btn {
  border: 1px solid rgba(122, 157, 111, 0.34);
  background: rgba(250, 252, 245, 0.88);
  color: #4b5f45;
  box-shadow: none;
}

.eye-care .quick-btn:hover {
  border-color: rgba(104, 140, 94, 0.46);
  background: rgba(241, 246, 232, 0.98);
  color: #31412d;
}

.eye-care .quick-btn.active,
.eye-care .quick-btn.highlighted {
  background: linear-gradient(135deg, #dfead1, #edf4e4);
  border-color: rgba(98, 135, 89, 0.52);
  color: #31412d;
  box-shadow: none;
}

.eye-care .quick-btn.upload {
  border-color: rgba(109, 147, 99, 0.42);
  background: rgba(231, 239, 220, 0.96);
}

.eye-care .quick-btn:disabled {
  background: rgba(231, 236, 225, 0.72);
  color: rgba(75, 95, 69, 0.58);
  border-color: rgba(122, 157, 111, 0.2);
}

.eye-care .typing-indicator {
  background: rgba(255, 255, 255, 0.84);
  border-color: rgba(103, 136, 93, 0.22);
}

.eye-care .typing-indicator span {
  background: #64875d;
}

.eye-care .chat-input {
  background: rgba(233, 238, 221, 0.88);
  border-color: rgba(103, 136, 93, 0.2);
}

.eye-care .input-wrapper {
  background: rgba(245, 247, 238, 0.96);
  border-color: rgba(144, 170, 131, 0.28);
  box-shadow: none;
}

.eye-care .input-wrapper:focus-within {
  background: rgba(255, 255, 255, 0.98);
  border-color: rgba(144, 170, 131, 0.55);
  box-shadow: 0 0 0 2px rgba(144, 170, 131, 0.22);
}

.eye-care .message-input {
  color: #2f352d;
}

.eye-care .message-input::placeholder {
  color: rgba(47, 53, 45, 0.48);
  font-style: normal;
}

.eye-care .message-input:focus::placeholder {
  color: rgba(47, 53, 45, 0.32);
}

.eye-care .message-input:focus {
  color: #2f352d;
  text-shadow: none;
}

.eye-care .custom-delete-icon {
  color: rgba(47, 53, 45, 0.68);
}

.eye-care .custom-delete-icon:hover {
  color: #2f352d;
}

.eye-care .delete-lines::before,
.eye-care .delete-lines::after {
  background: rgba(255, 255, 255, 0.9);
}
/* 优化清空图标在护眼模式下的色彩 */
.eye-care .clear-btn .clear-icon { color: #7fb069; filter: drop-shadow(0 0 3px rgba(127, 176, 105, 0.25)); opacity: 0.9; }
.eye-care .clear-btn:hover .clear-icon { color: #90c695; }

.eye-care .action-btn {
  color: rgba(47, 53, 45, 0.76);
}

.eye-care .action-btn:hover {
  color: #2f352d;
}

.eye-care .send-btn {
  background: linear-gradient(135deg, #6d9363, #5d8156);
  box-shadow: 0 6px 16px rgba(93, 129, 86, 0.22);
  color: #ffffff !important;
}

.eye-care .send-btn:disabled {
  opacity: 0.72;
  cursor: not-allowed;
  background: rgba(181, 192, 170, 0.82);
  box-shadow: none;
  color: rgba(255, 255, 255, 0.82) !important;
}

.eye-care .send-btn.active:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 18px rgba(93, 129, 86, 0.28);
  color: #ffffff !important;
}

.session-list::-webkit-scrollbar {
  width: 6px;
}

.session-list::-webkit-scrollbar-thumb {
  background: rgba(0, 255, 255, 0.22);
  border-radius: 999px;
}

.threshold-settings-wrap {
  position: relative;
  margin-left: auto;
  z-index: 1201;
}

.threshold-settings-btn {
  min-height: 42px;
  width: 42px;
  min-width: 42px;
  padding: 0;
  border: 1px solid rgba(186, 230, 253, 0.28);
  border-radius: 999px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.18), rgba(125, 211, 252, 0.10)),
    radial-gradient(circle at 16% 26%, rgba(125, 211, 252, 0.28), transparent 34%);
  color: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  cursor: pointer;
  overflow: hidden;
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  box-shadow:
    0 16px 36px rgba(15, 23, 42, 0.24),
    inset 0 1px 0 rgba(255, 255, 255, 0.16);
  transition: width 0.26s ease, min-width 0.26s ease, padding 0.24s ease, gap 0.2s ease, transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.threshold-settings-btn:hover,
.threshold-settings-btn:focus-visible,
.threshold-settings-btn.is-open {
  width: 150px;
  min-width: 150px;
  padding: 0 18px 0 14px;
  gap: 12px;
  transform: translateY(-1px);
  border-color: rgba(186, 230, 253, 0.48);
  box-shadow:
    0 18px 42px rgba(14, 116, 144, 0.22),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.threshold-dynamic-island {
  transform-origin: right center;
}

.threshold-island-orb {
  width: 18px;
  height: 18px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(103, 232, 249, 0.14);
  color: #cffafe;
  font-size: 11px;
  box-shadow:
    0 0 0 6px rgba(103, 232, 249, 0.08),
    0 0 20px rgba(103, 232, 249, 0.42);
}

.threshold-island-copy {
  display: inline-flex;
  align-items: baseline;
  gap: 10px;
  white-space: nowrap;
  width: 0;
  max-width: 0;
  overflow: hidden;
  opacity: 0;
  transition: max-width 0.24s ease, opacity 0.18s ease;
}

.threshold-settings-btn:hover .threshold-island-copy,
.threshold-settings-btn:focus-visible .threshold-island-copy,
.threshold-settings-btn.is-open .threshold-island-copy {
  width: auto;
  max-width: 96px;
  opacity: 1;
}

.threshold-island-title {
  color: rgba(255, 255, 255, 0.96);
  background: linear-gradient(90deg, #ffffff, #dff7ff 48%, #8fdcff);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-shadow: 0 0 18px rgba(125, 211, 252, 0.18);
}

.threshold-settings-panel {
  position: fixed;
  top: 86px;
  right: 24px;
  width: min(360px, calc(100vw - 32px));
  max-height: min(520px, calc(100vh - 112px));
  overflow-y: auto;
  padding: 18px;
  border: 1px solid rgba(186, 230, 253, 0.30);
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.88), rgba(8, 47, 73, 0.78));
  box-shadow: 0 32px 88px rgba(2, 8, 23, 0.56), inset 0 1px 0 rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(22px) saturate(150%);
  -webkit-backdrop-filter: blur(22px) saturate(150%);
  z-index: 6000;
}

.threshold-settings-panel::before {
  content: '';
  position: absolute;
  top: -7px;
  right: 42px;
  width: 14px;
  height: 14px;
  border-left: 1px solid rgba(186, 230, 253, 0.30);
  border-top: 1px solid rgba(186, 230, 253, 0.30);
  background: rgba(12, 38, 57, 0.92);
  transform: rotate(45deg);
}

.threshold-settings-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.threshold-settings-header strong {
  color: #ffffff;
  font-size: 18px;
  letter-spacing: 0.04em;
  text-shadow: 0 0 20px rgba(125, 211, 252, 0.16);
}

.threshold-settings-header span {
  color: rgba(255, 255, 255, 0.62);
  font-size: 13px;
}

.threshold-stage-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
  margin-top: 8px;
}

.threshold-field {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.threshold-field input {
  width: 92px;
  height: 36px;
  border: 1px solid rgba(186, 230, 253, 0.24);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.10);
  color: #ffffff;
  padding: 0 12px;
  outline: none;
  font-size: 16px;
  font-weight: 800;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.threshold-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

.threshold-reset-btn,
.threshold-save-btn {
  min-width: 96px;
  height: 38px;
  border-radius: 999px;
  padding: 0 16px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 0.04em;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.threshold-reset-btn:hover,
.threshold-save-btn:hover {
  transform: translateY(-1px);
}

.threshold-reset-btn {
  border: 1px solid rgba(186, 230, 253, 0.22);
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.82);
}

.threshold-save-btn {
  border: 1px solid rgba(125, 211, 252, 0.32);
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.92), rgba(14, 165, 233, 0.82));
  color: #ffffff;
  box-shadow: 0 14px 30px rgba(14, 165, 233, 0.24);
}

.confirm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(2, 8, 23, 0.72);
  backdrop-filter: blur(18px) saturate(145%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.confirm-modal-shell {
  width: min(520px, calc(100vw - 40px));
  max-width: 520px;
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.86), rgba(8, 47, 73, 0.74));
  border: 1px solid rgba(186, 230, 253, 0.24);
  box-shadow: 0 28px 80px rgba(2, 8, 23, 0.48), inset 0 1px 0 rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(22px) saturate(150%);
  overflow: hidden;
}

.confirm-modal-header {
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 58px 18px 28px;
}

.confirm-actions {
  display: flex;
  gap: 14px;
  justify-content: flex-end;
}

.confirm-cancel-btn,
.confirm-primary-btn {
  min-width: 120px;
  height: 46px;
  border-radius: 999px;
  padding: 0 18px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 15px;
  font-weight: 600;
}

.confirm-cancel-btn {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(186, 230, 253, 0.22);
}

.confirm-primary-btn {
  color: #ffffff;
  border: 1px solid rgba(125, 211, 252, 0.32);
}

.confirm-primary-danger {
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.92), rgba(14, 165, 233, 0.82));
  box-shadow: 0 12px 30px rgba(14, 165, 233, 0.24);
}

.confirm-primary-neutral {
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.92), rgba(14, 165, 233, 0.82));
  box-shadow: 0 12px 30px rgba(14, 165, 233, 0.24);
}

.confirm-header-danger {
  background: linear-gradient(90deg, rgba(14, 165, 233, 0.14), rgba(125, 211, 252, 0.06));
}

.confirm-header-neutral {
  background: linear-gradient(90deg, rgba(14, 165, 233, 0.14), rgba(125, 211, 252, 0.06));
}

.confirm-danger {
  border-color: rgba(125, 211, 252, 0.34);
}

.confirm-neutral {
  border-color: rgba(125, 211, 252, 0.34);
}

.confirm-cancel-btn:hover,
.confirm-primary-btn:hover {
  transform: translateY(-1px);
}

.confirm-cancel-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #ffffff;
  border-color: rgba(186, 230, 253, 0.34);
}

.confirm-title-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.confirm-title {
  color: #ffffff;
  font-weight: 700;
  font-size: 18px;
}

.confirm-subtitle {
  color: rgba(186, 230, 253, 0.72);
  font-size: 12px;
  line-height: 1;
}

.confirm-close-btn {
  position: absolute;
  top: 18px;
  right: 18px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 1px solid rgba(186, 230, 253, 0.28);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.72);
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.confirm-close-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #ffffff;
  transform: scale(1.04);
}

.confirm-body {
  padding: 16px 28px 22px;
  display: flex;
  align-items: flex-start;
  gap: 18px;
}

.confirm-icon-wrap {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  font-size: 18px;
  font-weight: 700;
}

.confirm-icon-danger {
  background: rgba(14, 165, 233, 0.18);
  color: #7dd3fc;
  border: 1px solid rgba(125, 211, 252, 0.32);
}

.confirm-icon-neutral {
  background: rgba(14, 165, 233, 0.18);
  color: #7dd3fc;
  border: 1px solid rgba(125, 211, 252, 0.32);
}

.confirm-message {
  margin: 4px 0 0;
  font-size: 16px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.92);
}

.confirm-actions {
  padding: 0 28px 28px;
}

.feature-ok {
  background: linear-gradient(90deg, #00ffff, #0080ff);
  color: #fff;
  border: none;
  border-radius: 12px;
  padding: 8px 16px;
  cursor: pointer;
}
.eye-care .confirm-overlay { background: rgba(47, 53, 45, 0.36); backdrop-filter: blur(18px) saturate(130%); }
.eye-care .confirm-modal-shell { background: linear-gradient(135deg, rgba(245, 247, 238, 0.92), rgba(226, 236, 216, 0.78)); border-color: rgba(144, 170, 131, 0.32); box-shadow: 0 28px 70px rgba(47, 53, 45, 0.18), inset 0 1px 0 rgba(255, 255, 255, 0.62); }
.eye-care .confirm-cancel-btn { background: rgba(255, 255, 255, 0.42); color: #4d6848; border-color: rgba(144, 170, 131, 0.34); }
.eye-care .confirm-primary-danger { background: linear-gradient(135deg, #7fb069, #5d8156); box-shadow: 0 12px 28px rgba(93, 129, 86, 0.2); }
.eye-care .confirm-primary-neutral { background: linear-gradient(135deg, #7fb069, #5d8156); box-shadow: 0 12px 28px rgba(93, 129, 86, 0.2); }
.eye-care .confirm-header-danger { background: linear-gradient(90deg, rgba(127, 176, 105, 0.16), rgba(255, 255, 255, 0.08)); }
.eye-care .confirm-header-neutral { background: linear-gradient(90deg, rgba(127, 176, 105, 0.16), rgba(255, 255, 255, 0.08)); }
.eye-care .confirm-danger { border-color: rgba(144, 170, 131, 0.36); }
.eye-care .confirm-neutral { border-color: rgba(144, 170, 131, 0.36); }
.eye-care .confirm-title { color: #2f352d; }
.eye-care .confirm-subtitle { color: rgba(77, 104, 72, 0.72); }
.eye-care .confirm-message { color: rgba(47, 53, 45, 0.88); }
.eye-care .confirm-icon-danger { background: rgba(127, 176, 105, 0.18); color: #5d8156; border-color: rgba(144, 170, 131, 0.34); }
.eye-care .confirm-icon-neutral { background: rgba(127, 176, 105, 0.18); color: #5d8156; border-color: rgba(144, 170, 131, 0.34); }
.eye-care .confirm-close-btn { background: rgba(255, 255, 255, 0.36); color: rgba(47, 53, 45, 0.72); border-color: rgba(144, 170, 131, 0.34); }
.eye-care .confirm-close-btn:hover { background: rgba(255, 255, 255, 0.52); color: #2f352d; }
.eye-care .threshold-settings-btn { background: linear-gradient(135deg, rgba(255, 255, 255, 0.14), rgba(127, 176, 105, 0.10)); color: #ffffff; border-color: rgba(144, 198, 149, 0.28); box-shadow: 0 16px 36px rgba(18, 32, 22, 0.18), inset 0 1px 0 rgba(255, 255, 255, 0.14); }
.eye-care .threshold-island-orb { background: rgba(144, 198, 149, 0.18); color: #ffffff; box-shadow: 0 0 0 6px rgba(144, 198, 149, 0.08), 0 0 18px rgba(144, 198, 149, 0.32); }
.eye-care .threshold-island-title { color: #ffffff; background: none; -webkit-text-fill-color: #ffffff; text-shadow: 0 0 18px rgba(144, 198, 149, 0.26); }
.eye-care .threshold-settings-panel { background: linear-gradient(135deg, rgba(28, 42, 31, 0.92), rgba(42, 60, 46, 0.82)); border-color: rgba(144, 198, 149, 0.28); box-shadow: 0 28px 70px rgba(18, 32, 22, 0.32), inset 0 1px 0 rgba(255, 255, 255, 0.14); }
.eye-care .threshold-settings-panel::before { background: rgba(33, 50, 36, 0.94); border-color: rgba(144, 198, 149, 0.28); }
.eye-care .threshold-settings-header strong { color: #ffffff; }
.eye-care .threshold-settings-header span,
.eye-care .threshold-field { color: rgba(255, 255, 255, 0.74); }
.eye-care .threshold-field input { background: rgba(255, 255, 255, 0.10); color: #ffffff; border-color: rgba(144, 198, 149, 0.28); }
.eye-care .threshold-reset-btn { background: rgba(255, 255, 255, 0.10); color: #ffffff; border-color: rgba(144, 198, 149, 0.28); }
.eye-care .threshold-save-btn { background: linear-gradient(135deg, #7fb069, #5d8156); color: #ffffff; border-color: rgba(144, 198, 149, 0.34); }

/* 响应式设计 */
@media (max-width: 1200px) {
  .hub-container {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
}

@media (max-width: 768px) {
  .ai-interview-container {
    height: 100vh;
    overflow-y: auto;
  }

  .ai-chat-shell,
  .ai-chat-container {
    min-height: 100vh;
    height: 100vh;
  }
  
  .main-content {
    padding: 15px 15px;
    flex: none;
    min-height: auto;
    overflow-y: auto;
  }
  
  .main-title {
    font-size: 1.8em;
  }

  .hub-container {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .chat-header {
    padding: 12px;
  }
  
  .chat-messages {
    padding: 12px;
  }
  
  .message-bubble {
    padding: 10px 14px;
  }
  
  .chat-input {
    padding: 12px;
  }

  .main-content {
    padding: 20px 15px;
  }
  
  .assistant-text {
    font-size: 12px;
    padding: 6px 10px;
  }
  
  .tech-item {
    width: 110px;
    height: 75px;
  }

  .ai-interview-container.dedicated-page .ai-assistant {
    padding: 0;
  }

  .ai-interview-container.dedicated-page .ai-chat-shell,
  .ai-interview-container.dedicated-page .ai-chat-container {
    min-height: 100vh;
    height: 100vh;
  }

  .ai-interview-container.dedicated-page .ai-chat-container {
    border-radius: 0;
  }

  .ai-interview-container.dedicated-page .chat-sidebar {
    width: 64px;
  }

  .ai-interview-container.dedicated-page .sidebar-header {
    justify-content: center;
    padding: 0 8px;
  }

  .ai-interview-container.dedicated-page .sidebar-toolbar {
    margin-left: 0;
    flex-direction: row;
    gap: 0;
  }

  .ai-interview-container.dedicated-page .sidebar-primary-action {
    margin: 8px auto 0;
    width: 28px;
    height: 28px;
    justify-content: center;
    padding: 0;
    border: none;
    background: transparent;
  }

  .ai-interview-container.dedicated-page .session-list {
    display: none;
  }

  .ai-interview-container.dedicated-page .chat-home-panel,
  .ai-interview-container.dedicated-page .chat-messages,
  .ai-interview-container.dedicated-page .chat-input {
    padding-left: 20px;
    padding-right: 20px;
  }

  .ai-interview-container.dedicated-page .chat-home-title {
    font-size: 32px;
  }
}

@media (max-width: 480px) {
  .ai-interview-container {
    height: 100vh;
    overflow-y: auto;
  }

  .ai-chat-shell,
  .ai-chat-container {
    min-height: 100vh;
    height: 100vh;
  }
  
  .main-content {
    padding: 10px 10px;
    flex: none;
    min-height: auto;
    overflow-y: auto;
  }
  
  .top-navigation {
    margin-bottom: 15px;
    padding: 0 10px;
  }
  
  .back-button {
    padding: 8px 12px;
    margin-right: 10px;
  }
  
  .holographic-header {
    margin-bottom: 0;
  }
  
  .main-title {
    font-size: 1.2em;
  }

  .subtitle {
    font-size: 0.8em;
  }

  .module-hologram {
    padding: 10px;
    min-height: 160px;
  }
  
  .module-icon {
    font-size: 1.8em;
    margin-bottom: 6px;
  }
  
  .hologram-frame h3 {
    font-size: 1em;
    margin: 0 0 5px;
  }
  
  .module-desc {
    font-size: 0.8em;
    margin-bottom: 8px;
  }
  
  .progress-ring {
    width: 50px;
    height: 50px;
    margin: 8px auto;
  }

  .about-section {
    margin-top: 20px;
    padding: 12px;
  }

  .team-name {
    font-size: 1.1em;
    margin-bottom: 8px;
  }

  .project-info {
    font-size: 0.8em;
    margin-bottom: 12px;
  }
  
  @media (max-width: 480px) {
    .about-section {
      margin-top: 15px;
      padding: 10px;
    }
    
    .tech-highlights {
      gap: 15px;
      margin-top: 12px;
    }
    
    .tech-item {
      padding: 6px 10px;
      width: 100px;
      height: 70px;
    }
    
    .tech-icon {
      font-size: 1.3em;
      margin-bottom: 4px;
    }
    
    .tech-text {
      font-size: 0.7em;
    }
  }

  .tech-highlights {
    gap: 20px;
    margin-top: 15px;
  }

  .tech-item {
    padding: 8px 12px;
  }

  .tech-icon {
    font-size: 1.5em;
    margin-bottom: 6px;
  }

  .tech-text {
    font-size: 0.8em;
  }
  
  .message {
    max-width: 90%;
  }
  
  .send-btn {
    width: 40px;
    height: 40px;
  }
  
  .assistant-wrapper {
    flex-direction: column-reverse;
    align-items: flex-end;
  }
  
  .assistant-text {
    font-size: 11px;
    padding: 4px 8px;
    margin-right: 10px;
  }

  .ai-interview-container.dedicated-page .chat-sidebar {
    width: 64px;
  }

  .ai-interview-container.dedicated-page .chat-home-title {
    font-size: 28px;
  }
  
}

/* 护眼模式适配 */
.eye-care .error-message {
  background: rgba(255, 77, 77, 0.05);
  border-color: rgba(255, 77, 77, 0.2);
  color: #d32f2f;
}

.eye-care .error-message.service-unavailable {
  background: rgba(255, 159, 67, 0.08);
  border-color: rgba(255, 159, 67, 0.22);
  color: #c77700;
}

.eye-care .error-message .retry-btn {
  border-color: #d32f2f;
  color: #d32f2f;
}

.eye-care .error-message .retry-btn:hover {
  background: rgba(255, 77, 77, 0.05);
}

.eye-care .error-message.service-unavailable .retry-btn {
  border-color: #c77700;
  color: #c77700;
}

.eye-care .error-message.service-unavailable .retry-btn:hover {
  background: rgba(255, 159, 67, 0.08);
}

/* 护眼模式最终校准：对齐 ai-hall 的深绿宇宙毛玻璃风格 */
.ai-interview-container.dedicated-page.eye-care {
  background: linear-gradient(135deg, #1c2a1f 0%, #2a3c2e 100%);
}

.ai-interview-container.dedicated-page.eye-care #stars,
.ai-interview-container.dedicated-page.eye-care #stars2,
.ai-interview-container.dedicated-page.eye-care #stars3 {
  opacity: 0.42;
  filter: sepia(0.42) hue-rotate(58deg) saturate(0.76);
}

.ai-interview-container.dedicated-page.eye-care .page-transition-overlay {
  background: radial-gradient(circle at 50% 50%, rgba(127,176,105,0.18), rgba(0,0,0,0.40));
}

.ai-interview-container.dedicated-page.eye-care .ai-chat-container {
  background: transparent;
  border-color: rgba(144, 198, 149, 0.18);
  box-shadow: none;
}

.ai-interview-container.dedicated-page.eye-care .chat-sidebar,
.ai-interview-container.dedicated-page.eye-care .chat-header,
.ai-interview-container.dedicated-page.eye-care .input-wrapper,
.ai-interview-container.dedicated-page.eye-care .recommended-action-card {
  background: rgba(255, 255, 255, 0.10);
  border: 1px solid rgba(144, 198, 149, 0.24);
  backdrop-filter: blur(18px) saturate(145%);
  -webkit-backdrop-filter: blur(18px) saturate(145%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.14);
}

.ai-interview-container.dedicated-page.eye-care .chat-sidebar {
  color: #ffffff;
}

.ai-interview-container.dedicated-page.eye-care .sidebar-brand,
.ai-interview-container.dedicated-page.eye-care .sidebar-primary-action,
.ai-interview-container.dedicated-page.eye-care .session-title,
.ai-interview-container.dedicated-page.eye-care .recommended-action-title,
.ai-interview-container.dedicated-page.eye-care .recommended-action-btn {
  color: rgba(255, 255, 255, 0.92);
}

.ai-interview-container.dedicated-page.eye-care .sidebar-section-label,
.ai-interview-container.dedicated-page.eye-care .chat-session-name,
.ai-interview-container.dedicated-page.eye-care .recommended-action-desc,
.ai-interview-container.dedicated-page.eye-care .message-input::placeholder {
  color: rgba(219, 232, 217, 0.64);
}

.ai-interview-container.dedicated-page.eye-care .sidebar-toggle-btn,
.ai-interview-container.dedicated-page.eye-care .sidebar-icon-btn,
.ai-interview-container.dedicated-page.eye-care .chat-back-btn,
.ai-interview-container.dedicated-page.eye-care .session-delete-btn,
.ai-interview-container.dedicated-page.eye-care .action-btn,
.ai-interview-container.dedicated-page.eye-care .custom-delete-icon {
  color: rgba(219, 232, 217, 0.78);
}

.ai-interview-container.dedicated-page.eye-care .sidebar-toggle-btn:hover,
.ai-interview-container.dedicated-page.eye-care .sidebar-icon-btn:hover,
.ai-interview-container.dedicated-page.eye-care .chat-back-btn:hover {
  color: rgba(219, 232, 217, 0.78);
  background: transparent;
}

.ai-interview-container.dedicated-page.eye-care .sidebar-primary-action,
.ai-interview-container.dedicated-page.eye-care .recommended-action-btn,
.ai-interview-container.dedicated-page.eye-care .quick-btn {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.14), rgba(144, 198, 149, 0.10));
  border: 1px solid rgba(144, 198, 149, 0.24);
  box-shadow: none;
}

.ai-interview-container.dedicated-page.eye-care .sidebar-primary-action:hover,
.ai-interview-container.dedicated-page.eye-care .session-item:hover,
.ai-interview-container.dedicated-page.eye-care .session-item.active,
.ai-interview-container.dedicated-page.eye-care .recommended-action-btn:hover,
.ai-interview-container.dedicated-page.eye-care .quick-btn:hover {
  background: rgba(255, 255, 255, 0.14);
  border-color: rgba(144, 198, 149, 0.36);
  box-shadow: none;
}

.ai-interview-container.dedicated-page.eye-care .session-item {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(144, 198, 149, 0.18);
  box-shadow: none;
}

.ai-interview-container.dedicated-page.eye-care .chat-main,
.ai-interview-container.dedicated-page.eye-care .chat-home-panel,
.ai-interview-container.dedicated-page.eye-care .chat-messages,
.ai-interview-container.dedicated-page.eye-care .chat-input {
  background: transparent;
}

.ai-interview-container.dedicated-page.eye-care .chat-home-title,
.ai-interview-container.dedicated-page.eye-care .welcome-stream-text {
  color: rgba(219, 232, 217, 0.92);
  text-shadow: 0 14px 36px rgba(18, 32, 22, 0.38);
}

.ai-interview-container.dedicated-page.eye-care .message.user .message-bubble {
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(144, 198, 149, 0.28);
  backdrop-filter: blur(16px) saturate(150%);
  -webkit-backdrop-filter: blur(16px) saturate(150%);
  box-shadow: 0 12px 28px rgba(18, 32, 22, 0.18);
}

.ai-interview-container.dedicated-page.eye-care .message.ai .message-bubble {
  background: transparent;
  border: none;
  box-shadow: none;
}

.ai-interview-container.dedicated-page.eye-care .message-content,
.ai-interview-container.dedicated-page.eye-care .message.user .message-content,
.ai-interview-container.dedicated-page.eye-care .message.ai .message-content {
  color: rgba(255, 255, 255, 0.92);
}

.ai-interview-container.dedicated-page.eye-care .message.ai .message-time,
.ai-interview-container.dedicated-page.eye-care .message.user .message-time {
  color: rgba(219, 232, 217, 0.56);
}

.ai-interview-container.dedicated-page.eye-care .message-content a,
.ai-interview-container.dedicated-page.eye-care .message.user .message-content a {
  color: #dbe8d9;
}

.ai-interview-container.dedicated-page.eye-care .message-content :deep(code) {
  background: rgba(144, 198, 149, 0.14);
  color: #edf7ea;
}

.ai-interview-container.dedicated-page.eye-care .message-content :deep(hr) {
  border-top-color: rgba(144, 198, 149, 0.28);
}

.ai-interview-container.dedicated-page.eye-care .message.ai .message-content.streaming::after,
.ai-interview-container.dedicated-page.eye-care .welcome-stream-text::after {
  background: rgba(219, 232, 217, 0.82);
}

.ai-interview-container.dedicated-page.eye-care .input-wrapper:focus-within {
  background: rgba(255, 255, 255, 0.14);
  border-color: rgba(144, 198, 149, 0.44);
  box-shadow: 0 0 0 3px rgba(127, 176, 105, 0.14);
}

.ai-interview-container.dedicated-page.eye-care .message-input,
.ai-interview-container.dedicated-page.eye-care .message-input:focus {
  color: #ffffff;
}

.ai-interview-container.dedicated-page.eye-care .send-btn,
.ai-interview-container.dedicated-page.eye-care .send-btn.active:hover {
  background: linear-gradient(135deg, rgba(127, 176, 105, 0.92), rgba(93, 129, 86, 0.86));
  box-shadow: 0 8px 20px rgba(93, 129, 86, 0.22);
  color: #ffffff !important;
}

.ai-interview-container.dedicated-page.eye-care .send-btn:disabled {
  background: rgba(144, 198, 149, 0.26);
  color: rgba(255, 255, 255, 0.72) !important;
  box-shadow: none;
}

</style>

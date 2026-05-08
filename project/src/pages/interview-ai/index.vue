<template>
  <div class="ai-interview-container" :class="{ 'eye-care': isEyeCareMode }">
    <!-- 科幻背景效果 -->
    <div class="cyber-background">
      <div class="grid-lines"></div>
      <!-- 添加扫描线效果 -->
      <div class="scanning-lines">
        <div class="horizontal-scan"></div>
        <div class="vertical-scan"></div>
        <div class="diagonal-scan"></div>
        <div class="radar-scan"></div>
      </div>
      <div class="floating-particles">
        <div v-for="n in 50" :key="n" class="particle" 
             :style="{ 
               '--delay': `${Math.random() * 5}s`,
               '--x-pos': `${Math.random() * 100}%`,
               '--y-pos': `${Math.random() * 100}%`,
               '--x-end': `${Math.random() * 200 - 100}px`
             }"></div>
      </div>
      <div class="energy-field"></div>
    </div>
    <div v-if="isTransitioning" class="page-transition-overlay" :class="{ 'eye-care': isEyeCareMode }"></div>

    <!-- AI助手对话界面 -->
    <div class="ai-assistant" :class="{ 'active': showAIChat }">
      <div class="assistant-wrapper" v-if="!showAIChat">
        <div class="ai-avatar" @click="toggleAIChat(true)">
          <div class="ai-core"></div>
          <div class="ai-pulse"></div>
        </div>
      </div>
      <div class="ai-chat-overlay" v-if="showAIChat" @click.self="toggleAIChat(false)">
        <div class="ai-chat-container" :class="{ 'sidebar-collapsed': sidebarCollapsed, 'welcome-mode': !hasConversationStarted }">
          <aside class="chat-sidebar">
            <div class="sidebar-header">
              <button class="sidebar-toggle-btn" @click.stop="toggleSidebar" type="button" :aria-label="sidebarCollapsed ? '展开侧边栏' : '收起侧边栏'">
                <svg class="toolbar-icon-svg" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                  <g v-if="sidebarCollapsed">
                    <path d="M4.5 12H19.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                    <path d="M7.5 9.5L4.5 12L7.5 14.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M16.5 9.5L19.5 12L16.5 14.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
                  </g>
                  <g v-else>
                    <path d="M4.5 12H7.8" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                    <path d="M7.8 9.2L11.4 12L7.8 14.8" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M19.5 12H16.2" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                    <path d="M16.2 9.2L12.6 12L16.2 14.8" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
                  </g>
                </svg>
              </button>
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
                  <div class="session-title">{{ session.title || '新会话' }}</div>
                </div>
              </div>
            </div>
          </aside>
          <div class="chat-main">
            <div class="chat-header">
              <div class="header-left">
                <div class="ai-avatar-small"></div>
                <div class="chat-title-group">
                  <span class="ai-name">AI助手</span>
                  <span class="chat-session-name">{{ activeConversationTitle }}</span>
                </div>
              </div>
              <button class="new-chat-btn header-new-chat-btn" @click="createNewSession()" :title="'新建对话'">
                <svg class="toolbar-icon-svg" width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                  <path d="M4 19.5H8.5L18.8 9.2C19.6 8.4 19.6 7.1 18.8 6.3L17.7 5.2C16.9 4.4 15.6 4.4 14.8 5.2L4.5 15.5V19.5H4Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
                  <path d="M13.5 6.5L17.5 10.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                </svg>
              </button>
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
              <div class="error-message" v-if="errorMessage">
                <span>{{ errorMessage }}</span>
                <button class="retry-btn" @click="retry">重试</button>
              </div>
              <div ref="chatMessagesEnd" class="chat-messages-end"></div>
            </div>

            <div class="chat-input">
              <div class="chat-quick-actions" v-if="assistantActions.length > 0">
                <button
                  v-for="action in assistantActions"
                  :key="action.type"
                  class="quick-btn"
                  :class="{ upload: action.type.includes('upload_resume') }"
                  :disabled="isResumeUploading && action.type.includes('upload_resume')"
                  @click="runAssistantAction(action)"
                >
                  {{ isResumeUploading && action.type.includes('upload_resume') ? '分析中...' : action.label }}
                </button>
              </div>
              <div class="input-wrapper">
                <input
                  v-model="userInput"
                  @keydown.enter="handleEnterKey"
                  :placeholder="hasConversationStarted ? '继续输入消息，按 Enter 发送' : '输入你的目标，按 Enter 开启会话'"
                  class="message-input"
                  ref="messageInputRef"
                />
                <div class="input-actions">
                  <span class="clear-btn" @click="confirmClearChat" v-if="hasSessionContextToClear" title="清空当前会话上下文">
                    <svg class="clear-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M9 4h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                      <path d="M5 7h14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                      <rect x="6" y="7" width="12" height="13" rx="2" stroke="currentColor" stroke-width="2"/>
                      <path d="M10 10v6M14 10v6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 顶部导航区域 -->
      <div class="top-navigation">
        <div class="back-button" @click="handleBack">
          <div class="custom-back-arrow"></div>
        </div>
        <!-- 全息投影标题 -->
        <div class="holographic-header">
          <div class="title-container">
            <h1 class="main-title">
              <span class="title-text">智能体面试系统</span>
              <div class="title-glow"></div>
            </h1>
            <p class="subtitle">AI驱动的多维度职业能力评估平台</p>
          </div>
        </div>
      </div>

      <!-- 模块选择区域 -->
      <div class="modules-hub">
        <div class="hub-container">
        <!-- 简历评测模块 -->
          <div class="module-hologram" 
               :class="{ 'completed': comprehensiveState.resume.completed, 'disabled': comprehensiveState.questions.inProgress || comprehensiveState.audio.inProgress }" 
               @click="handleModuleClick('resume', { source: 'manual_click' })">
            <div class="hologram-frame">
          <div class="module-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M7 3h6l4 4v11a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              <path d="M13 3v4h4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M8 9h8M8 12h8M8 15h5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h3>简历评测</h3>
              <p class="module-desc">深度分析简历与职位匹配度</p>
              <div class="progress-ring">
                <svg class="progress-svg" viewBox="0 0 100 100">
                  <circle class="progress-bg" cx="50" cy="50" r="45"></circle>
                  <circle class="progress-fill" cx="50" cy="50" r="45" 
                          :stroke-dasharray="comprehensiveState.resume.completed ? '283' : '0'"></circle>
                </svg>
                <div class="progress-text">
                  <span v-if="comprehensiveState.resume.completed">{{ comprehensiveState.resume.score }}</span>
                  <span v-else>0</span>
          </div>
              </div>
              <div class="status-indicator" :class="resumeStatusText === '已完成' ? 'completed' : 'pending'"></div>
            </div>
            <div class="hologram-glow"></div>
        </div>

        <!-- 试题作答模块 -->
          <div class="module-hologram" 
               :class="{ 'completed': comprehensiveState.questions.completed, 'disabled': !comprehensiveState.resume.completed || comprehensiveState.questions.inProgress || comprehensiveState.audio.inProgress }" 
               @click="handleModuleClick('questions', { source: 'manual_click' })">
            <div class="hologram-frame">
          <div class="module-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="4" y="4" width="16" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
              <path d="M8 8h8M8 12h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M9 17l6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h3>试题作答</h3>
              <p class="module-desc">专业知识与技能综合测试</p>
              <div class="progress-ring">
                <svg class="progress-svg" viewBox="0 0 100 100">
                  <circle class="progress-bg" cx="50" cy="50" r="45"></circle>
                  <circle class="progress-fill" cx="50" cy="50" r="45" 
                          :stroke-dasharray="comprehensiveState.questions.completed ? '283' : '0'"></circle>
                </svg>
                <div class="progress-text">
                  <span v-if="comprehensiveState.questions.completed">{{ comprehensiveState.questions.score }}</span>
                  <span v-else>0</span>
          </div>
              </div>
              <div class="status-indicator" :class="questionsStatusText === '已完成' ? 'completed' : 'pending'"></div>
            </div>
            <div class="hologram-glow"></div>
        </div>

        <!-- 场景评测模块 -->
          <div class="module-hologram" 
               :class="{ 'completed': comprehensiveState.audio.completed, 'disabled': !comprehensiveState.questions.completed || comprehensiveState.audio.inProgress }" 
               @click="handleModuleClick('audio', { source: 'manual_click' })">
            <div class="hologram-frame">
          <div class="module-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 16a4 4 0 0 0 4-4V7a4 4 0 1 0-8 0v5a4 4 0 0 0 4 4Z" stroke="currentColor" stroke-width="2"/>
              <path d="M7 11v1a5 5 0 0 0 10 0v-1" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M12 21v-3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h3>场景评测</h3>
              <p class="module-desc">工作场景应变与沟通能力分析</p>
              <div class="progress-ring">
                <svg class="progress-svg" viewBox="0 0 100 100">
                  <circle class="progress-bg" cx="50" cy="50" r="45"></circle>
                  <circle class="progress-fill" cx="50" cy="50" r="45" 
                          :stroke-dasharray="comprehensiveState.audio.completed ? '283' : '0'"></circle>
                </svg>
                <div class="progress-text">
                  <span v-if="comprehensiveState.audio.completed">{{ comprehensiveState.audio.score }}</span>
                  <span v-else>0</span>
                </div>
              </div>
              <div class="status-indicator" :class="audioStatusText === '已完成' ? 'completed' : 'pending'"></div>
            </div>
            <div class="hologram-glow"></div>
          </div>

        <!-- 综合报告生成区域 -->
          <div class="module-hologram report-module" 
               @click="handleModuleClick('report', { source: 'manual_click' })" 
               :class="{ 'disabled': !canGenerateReport, 'completed': canGenerateReport }">
            <div class="hologram-frame">
              <div class="module-icon">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="5" y="12" width="3" height="7" rx="1" stroke="currentColor" stroke-width="2"/>
                  <rect x="10.5" y="9" width="3" height="10" rx="1" stroke="currentColor" stroke-width="2"/>
                  <rect x="16" y="6" width="3" height="13" rx="1" stroke="currentColor" stroke-width="2"/>
                  <path d="M4 20h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </div>
              <h3>生成综合报告</h3>
              <p class="module-desc">AI深度分析您的综合表现</p>
              <div class="progress-ring">
                <svg class="progress-svg" viewBox="0 0 100 100">
                  <circle class="progress-bg" cx="50" cy="50" r="45"></circle>
                  <circle class="progress-fill" cx="50" cy="50" r="45" 
                          :stroke-dasharray="canGenerateReport ? '283' : '0'"></circle>
                </svg>
                <div class="progress-text">
                  <span v-if="canGenerateReport">✓</span>
                  <span v-else>...</span>
                </div>
              </div>
              <div class="status-indicator" :class="canGenerateReport ? 'completed' : 'pending'"></div>
            </div>
            <div class="hologram-glow"></div>
          </div>
        </div>
      </div>

      <!-- 关于我们部分 -->
      <div class="about-section">
        <div class="about-content">
          <h2 class="team-name">关于我们 · 梦之队</h2>
          <p class="project-info">
            我们是「梦之队」，致力于打造尖端的"多模态模拟面试智能体"。本项目利用先进AI技术，从简历、试题、场景等多维度综合评估您的面试表现，助您在求职路上脱颖而出。
          </p>
          <div class="tech-highlights">
            <div class="tech-item clickable" @click="openFeature('ai')">
              <span class="tech-icon">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="6" y="5" width="12" height="14" rx="4" stroke="currentColor" stroke-width="2"/>
                  <circle cx="10" cy="11" r="1.6" fill="currentColor"/>
                  <circle cx="14" cy="11" r="1.6" fill="currentColor"/>
                  <path d="M9 14h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M12 3v3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <circle cx="12" cy="3" r="1.2" fill="currentColor"/>
                  <path d="M6 10H4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <circle cx="4" cy="10" r="1" fill="currentColor"/>
                  <path d="M18 10h2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <circle cx="20" cy="10" r="1" fill="currentColor"/>
                </svg>
              </span>
              <span class="tech-text">AI智能评估</span>
            </div>
            <div class="tech-item clickable" @click="openFeature('analysis')">
              <span class="tech-icon">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="5" y="12" width="3" height="7" rx="1" stroke="currentColor" stroke-width="2"/>
                  <rect x="10.5" y="9" width="3" height="10" rx="1" stroke="currentColor" stroke-width="2"/>
                  <rect x="16" y="6" width="3" height="13" rx="1" stroke="currentColor" stroke-width="2"/>
                  <path d="M4 20h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </span>
              <span class="tech-text">多维度分析</span>
            </div>
            <div class="tech-item clickable" @click="openFeature('match')">
              <span class="tech-icon">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="12" cy="12" r="7" stroke="currentColor" stroke-width="2"/>
                  <circle cx="12" cy="12" r="4" stroke="currentColor" stroke-width="2"/>
                  <path d="M12 3v3M12 18v3M3 12h3M18 12h3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </span>
              <span class="tech-text">精准匹配</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showFeatureModal" class="feature-overlay" @click.self="closeFeature">
      <div class="feature-modal">
        <div class="feature-header">
          <span class="feature-title">{{ featureContent.title }}</span>
          <span class="feature-close" @click="closeFeature">×</span>
        </div>
        <div class="feature-body">
          <p class="feature-desc">{{ featureContent.desc }}</p>
          <ul class="feature-list">
            <li v-for="(item, idx) in featureContent.points" :key="idx">{{ item }}</li>
          </ul>
        </div>
        <div class="feature-footer">
          <button class="feature-ok" @click="closeFeature">了解了</button>
        </div>
      </div>
    </div>
    <div v-if="confirmDialog.visible" class="feature-overlay" @click.self="closeConfirmDialog">
      <div class="feature-modal confirm-modal" :class="`confirm-${confirmDialog.tone || 'neutral'}`">
        <div class="feature-header" :class="`confirm-header-${confirmDialog.tone || 'neutral'}`">
          <div class="confirm-title-group">
            <span class="feature-title">{{ confirmDialog.title }}</span>
            <span class="confirm-subtitle">{{ confirmDialog.tone === 'danger' ? '危险操作' : '操作确认' }}</span>
          </div>
          <button class="feature-close confirm-close-btn" @click="closeConfirmDialog" type="button" aria-label="关闭">×</button>
        </div>
        <div class="feature-body confirm-body">
          <div class="confirm-icon-wrap" :class="`confirm-icon-${confirmDialog.tone || 'neutral'}`">
            <span>{{ confirmDialog.tone === 'danger' ? '!' : '?' }}</span>
          </div>
          <p class="feature-desc confirm-message">{{ confirmDialog.message }}</p>
        </div>
        <div class="feature-footer confirm-actions">
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
  getResumeAnalysisByXunfei,
  analyzeComprehensiveResume,
  getJobInfo,
  getJobList,
  fetchAgentConversationList,
  upsertAgentConversation,
  deleteAgentConversationById
} from '@/utils/api';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);
const isTransitioning = ref(false);
const ASSISTANT_CONVERSATION_AGENT_KEY = 'interview-assistant';
const DEFAULT_SESSION_TITLE = '新会话';
const EMPTY_SESSION_PREVIEW = '点击开始对话';
const HOME_WELCOME_MESSAGE = '有什么我能帮你的吗？';
const ACTIVE_ASSISTANT_SESSION_STORAGE_KEY = 'activeAiConversationSessionId';
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
  sessionId = '',
  completedType = '',
  mode = '',
  score = '',
  timestamp = ''
} = {}) => {
  const query = [];
  const safeSessionId = String(sessionId || '').trim();
  const safeCompletedType = String(completedType || '').trim();
  const safeMode = String(mode || '').trim();
  const safeScore = String(score || '').trim();
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
  if (safeScore) {
    query.push(`score=${encodeURIComponent(safeScore)}`);
  }
  if (safeTimestamp) {
    query.push(`timestamp=${encodeURIComponent(safeTimestamp)}`);
  }
  return `/pages/interview-ai/index${query.length ? `?${query.join('&')}` : ''}`;
};

const getStoredAssistantConversationSessionId = () => {
  try {
    return String(sessionStorage.getItem(ACTIVE_ASSISTANT_SESSION_STORAGE_KEY) || '').trim();
  } catch (_) {
    return '';
  }
};

const setStoredAssistantConversationSessionId = (sessionId = '') => {
  const safeSessionId = String(sessionId || '').trim();
  try {
    if (safeSessionId) {
      sessionStorage.setItem(ACTIVE_ASSISTANT_SESSION_STORAGE_KEY, safeSessionId);
    } else {
      sessionStorage.removeItem(ACTIVE_ASSISTANT_SESSION_STORAGE_KEY);
    }
  } catch (_) {}
};

// AI对话相关状态
const showAIChat = ref(false);
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
const activeConversationId = ref('');
const sidebarCollapsed = ref(false);
const homeWelcomeText = ref('');
const AVAILABLE_ASSISTANT_ACTION_TYPES = [
  'choose_special',
  'choose_comprehensive',
  'upload_resume_special',
  'upload_resume_comprehensive',
  'go_resume',
  'go_questions',
  'go_audio',
  'go_report'
];
const ASSISTANT_ACTION_LABELS = {
  choose_special: '专项测评',
  choose_comprehensive: '综合测评',
  upload_resume_special: '上传简历做专项评测',
  upload_resume_comprehensive: '上传简历做综合评测',
  go_resume: '去做简历评测',
  go_questions: '去做试题作答',
  go_audio: '去做场景评测',
  go_report: '查看综合报告'
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
let persistSessionTimer = null;
const hasRestoredConversationState = ref(false);
const isEnteringPageFlow = ref(false);
let lastHandledPendingActionKey = '';
const DRAFT_SESSION_ID_PREFIX = 'draft:';

const isPersistentConversationId = (sessionId = '') => {
  const safeSessionId = String(sessionId || '').trim();
  return safeSessionId.startsWith('session:');
};

const buildSessionScopeId = (sessionId = '') => {
  const safeSessionId = String(sessionId || '').trim();
  return safeSessionId || `session:${Date.now()}:${Math.random().toString(36).slice(2, 8)}`;
};

const buildDraftConversationId = (draftId = '') => {
  const safeDraftId = String(draftId || '').trim();
  return safeDraftId || `${DRAFT_SESSION_ID_PREFIX}${Date.now()}:${Math.random().toString(36).slice(2, 8)}`;
};

const getActiveConversationUiId = () => {
  return String(activeConversationRecord.value?.id || activeConversationId.value || '').trim();
};

const getActivePersistentSessionId = (fallback = '') => {
  const activeRecordSessionId = String(activeConversationRecord.value?.sessionId || '').trim();
  if (isPersistentConversationId(activeRecordSessionId)) {
    return activeRecordSessionId;
  }
  const uiId = getActiveConversationUiId();
  if (isPersistentConversationId(uiId)) {
    return uiId;
  }
  const storedSessionId = getStoredAssistantConversationSessionId();
  if (isPersistentConversationId(storedSessionId)) {
    return storedSessionId;
  }
  const safeFallback = String(fallback || '').trim();
  return isPersistentConversationId(safeFallback) ? safeFallback : '';
};

const syncRouteWithPersistentSession = (fallback = '') => {
  const targetSessionId = getActivePersistentSessionId(fallback);
  const currentRouteSessionId = String(route.query.sessionId || '').trim();
  if (!targetSessionId || currentRouteSessionId === targetSessionId) {
    return;
  }
  router.replace(buildInterviewAiUrl({ sessionId: targetSessionId }));
};

const shouldAutoOpenAiChatOnEntry = () => {
  const completedType = String(route.query.completedType || '').trim();
  if (completedType) {
    return true;
  }
  const autoOpen = String(route.query.autoOpen || '').trim().toLowerCase();
  if (autoOpen === '1' || autoOpen === 'true') {
    return true;
  }
  return false;
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

const normalizeTargetModuleValue = (value) => {
  const safeValue = String(value || '').trim().toLowerCase();
  return ['resume', 'questions', 'audio', 'report'].includes(safeValue) ? safeValue : '';
};

const mapModuleFromActionType = (actionType) => {
  switch (String(actionType || '').trim()) {
    case 'upload_resume_special':
    case 'upload_resume_comprehensive':
    case 'go_resume':
      return 'resume';
    case 'go_questions':
      return 'questions';
    case 'go_audio':
      return 'audio';
    case 'go_report':
      return 'report';
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

  resetComprehensiveState();
  pendingSuggestedJob.value = null;
  const current = activeConversationRecord.value;
  if (current) {
    const updatedSession = createSessionState({
      ...current,
      jobId: nextJobId
    });
    conversationRecords.value = sortSessions([
      updatedSession,
      ...conversationRecords.value.filter((item) => item.id !== updatedSession.id)
    ]);
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
      sessionId: String(item.sessionId || '').trim(),
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
    sessionId: getActivePersistentSessionId(),
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
  return Array.isArray(messages) && messages.some((item) => item?.type === 'user' && String(item?.content || '').trim());
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

const createDefaultComprehensiveState = () => ({
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
});

const normalizeComprehensiveState = (state = {}) => {
  const defaults = createDefaultComprehensiveState();
  return {
    ...defaults,
    ...(state && typeof state === 'object' ? state : {}),
    resume: {
      ...defaults.resume,
      ...((state && typeof state === 'object' && state.resume) ? state.resume : {})
    },
    questions: {
      ...defaults.questions,
      ...((state && typeof state === 'object' && state.questions) ? state.questions : {})
    },
    audio: {
      ...defaults.audio,
      ...((state && typeof state === 'object' && state.audio) ? state.audio : {})
    },
    overall: {
      ...defaults.overall,
      ...((state && typeof state === 'object' && state.overall) ? state.overall : {})
    }
  };
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
  const candidateId = String(overrides.id || overrides.sessionId || '').trim();
  const sessionId = isPersistentConversationId(candidateId)
    ? candidateId
    : buildDraftConversationId(candidateId);
  const safeMessages = Array.isArray(overrides.aiMessages) && overrides.aiMessages.length > 0
    ? sanitizeConversationMessages(overrides.aiMessages)
    : [];
  return {
    id: sessionId,
    jobId: String(overrides.jobId || currentJobId.value || '').trim(),
    title: String(overrides.title || createSessionTitleFromMessages(safeMessages) || buildUntitledSessionTitle()),
    preview: String(overrides.preview || EMPTY_SESSION_PREVIEW),
    sessionId,
    hasInteractedWithAssistant: overrides.hasInteractedWithAssistant ?? hasStartedConversation(safeMessages),
    hasShownWelcomeMessage: overrides.hasShownWelcomeMessage ?? false,
    selectedEvaluationMode: String(overrides.selectedEvaluationMode || ''),
    assistantActions: normalizeStoredAssistantActions(overrides.assistantActions || []),
    eventLogs: normalizeStoredEventLogs(overrides.eventLogs || []),
    aiMessages: safeMessages,
    comprehensiveState: normalizeComprehensiveState(overrides.comprehensiveState),
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

const ensureAssistantSessionId = () => {
  const scopedId = getActiveConversationUiId();
  if (isPersistentConversationId(scopedId)) {
    assistantConversationScopeId.value = scopedId;
    return assistantConversationScopeId.value;
  }
  if (!hasStartedConversation(aiMessages.value)) {
    assistantConversationScopeId.value = '';
    return '';
  }
  assistantConversationScopeId.value = buildSessionScopeId();
  return assistantConversationScopeId.value;
};

const applySessionState = (session) => {
  const safeSession = createSessionState(session || {});
  activeConversationId.value = safeSession.id;
  setStoredAssistantConversationSessionId(isPersistentConversationId(safeSession.id) ? safeSession.id : '');
  currentJobId.value = String(safeSession.jobId || '').trim();
  assistantConversationScopeId.value = isPersistentConversationId(safeSession.sessionId || safeSession.id)
    ? (safeSession.sessionId || safeSession.id)
    : '';
  aiMessages.value = safeSession.aiMessages;
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
  comprehensiveState.value = normalizeComprehensiveState(safeSession.comprehensiveState);
  if (hasStartedConversation(safeSession.aiMessages)) {
    stopHomeWelcomeTyping();
  } else {
    startHomeWelcomeTyping(true);
  }
};

const activeConversationRecord = computed(() => {
  return conversationRecords.value.find((item) => item.id === activeConversationId.value) || null;
});

const orderedConversationRecords = computed(() => {
  return sortSessions(conversationRecords.value.filter((item) => hasSessionFirstQuestion(item)));
});

const hasConversationStarted = computed(() => {
  return hasStartedConversation(aiMessages.value);
});

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

const activeConversationTitle = computed(() => {
  return activeConversationRecord.value?.title || DEFAULT_SESSION_TITLE;
});

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
  return conversationRecords.value.find((item) => !hasSessionFirstQuestion(item)) || null;
};

const createNewSession = (options = {}) => {
  const { activate = true, persist = true } = options;
  const reusableSession = findReusableDraftSession();
  if (reusableSession) {
    if (activate) {
      applySessionState(reusableSession);
    }
    if (persist) {
      persistConversationState();
    }
    return reusableSession;
  }
  const session = createSessionState({ title: buildUntitledSessionTitle() });
  conversationRecords.value = sortSessions([session, ...conversationRecords.value.filter((item) => item.id !== session.id)]);
  if (activate) {
    applySessionState(session);
  }
  if (persist) {
    persistConversationState();
  }
  return session;
};

const buildCurrentSessionSnapshot = () => {
  const existing = activeConversationRecord.value;
  const hasRealConversationStarted = hasStartedConversation(aiMessages.value);
  const persistentSessionId = isPersistentConversationId(existing?.id)
    ? existing.id
    : getActivePersistentSessionId();
  const resolvedSessionId = hasRealConversationStarted
    ? (persistentSessionId || ensureAssistantSessionId())
    : (String(existing?.id || getActiveConversationUiId() || '').trim() || buildDraftConversationId());
  const firstQuestionAt = existing?.firstQuestionAt || (hasStartedConversation(aiMessages.value) ? formatDateTimeUtc8() : '');
  const title = hasRealConversationStarted
    ? createSessionTitleFromMessages(aiMessages.value)
    : (existing?.title || buildUntitledSessionTitle());
  return createSessionState({
    id: resolvedSessionId,
    jobId: String(existing?.jobId || currentJobId.value || '').trim(),
    title,
    preview: EMPTY_SESSION_PREVIEW,
    sessionId: resolvedSessionId,
    hasInteractedWithAssistant: hasInteractedWithAssistant.value,
    hasShownWelcomeMessage: hasShownWelcomeMessage.value,
    selectedEvaluationMode: selectedEvaluationMode.value,
    assistantActions: assistantActions.value,
    eventLogs: eventLogs.value,
    aiMessages: aiMessages.value,
    comprehensiveState: comprehensiveState.value,
    firstQuestionAt,
    createdAt: existing?.createdAt || formatDateTimeUtc8(),
    updatedAt: formatDateTimeUtc8()
  });
};

const syncCurrentSessionState = () => {
  const snapshot = buildCurrentSessionSnapshot();
  activeConversationId.value = snapshot.id;
  conversationRecords.value = sortSessions([
    snapshot,
    ...conversationRecords.value.filter((item) => item.id !== snapshot.id)
  ]);
  return snapshot;
};

// 将当前前端会话快照转换为后端会话持久化请求。
const buildConversationUpsertRequest = (snapshot, overrideJobId = null) => ({
  sessionId: snapshot?.id || '',
  agentKey: ASSISTANT_CONVERSATION_AGENT_KEY,
  jobId: Number(String((overrideJobId ?? snapshot?.jobId ?? currentJobId.value) || '').trim()) || null,
  title: snapshot?.title || '',
  selectedEvaluationMode: snapshot?.selectedEvaluationMode || '',
  hasInteractedWithAssistant: Boolean(snapshot?.hasInteractedWithAssistant),
  hasShownWelcomeMessage: Boolean(snapshot?.hasShownWelcomeMessage),
  firstQuestionAt: snapshot?.firstQuestionAt || '',
  assistantActions: Array.isArray(snapshot?.assistantActions) ? snapshot.assistantActions : [],
  eventLogs: Array.isArray(snapshot?.eventLogs) ? snapshot.eventLogs : [],
  aiMessages: sanitizeConversationMessages(snapshot?.aiMessages),
  comprehensiveState: snapshot?.comprehensiveState && typeof snapshot.comprehensiveState === 'object'
    ? snapshot.comprehensiveState
    : createDefaultComprehensiveState()
});

// 只有已经开始交互或已存在远端记录的会话，才会触发后端持久化。
const shouldPersistConversationToServer = (snapshot) => {
  if (!snapshot?.id) {
    return false;
  }
  if (!isPersistentConversationId(snapshot.id)) {
    return false;
  }
  if (deletedSessionIds.has(snapshot.id)) {
    return false;
  }
  return hasSessionFirstQuestion(snapshot) || persistedSessionIds.has(snapshot.id);
};

// 整包上报一个会话快照，后端会拆分写入 session/message/event/memory 四表。
const persistConversationToServer = async (snapshot, overrideJobId = null) => {
  if (!snapshot?.id) {
    return;
  }
  if (deletedSessionIds.has(snapshot.id)) {
    return;
  }
  const payload = buildConversationUpsertRequest(snapshot, overrideJobId);
  try {
    await upsertAgentConversation(payload);
    persistedSessionIds.add(snapshot.id);
  } catch (error) {
    console.warn('保存 AI 会话到后端失败:', error);
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
    await persistConversationToServer(snapshot, overrideJobId);
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

// 页面进入时恢复服务端会话列表。
// 若 URL 指定了 sessionId，则打开对应历史会话；否则默认进入新的草稿会话。
const restoreConversationState = async () => {
  assistantConversationScopeId.value = '';
  aiMessages.value = [];
  assistantActions.value = [];
  eventLogs.value = [];
  pendingSuggestedJob.value = null;
  hasInteractedWithAssistant.value = false;
  hasShownWelcomeMessage.value = false;
  selectedEvaluationMode.value = '';
  conversationRecords.value = [];
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
      const routeSessionId = String(route.query.sessionId || '').trim()
        || (String(route.query.completedType || '').trim() ? getStoredAssistantConversationSessionId() : '');
      const historySession = routeSessionId
        ? conversationRecords.value.find((item) => item.id === routeSessionId)
        : null;
      if (historySession) {
        applySessionState(historySession);
      } else if (routeSessionId) {
        const placeholderSession = createSessionState({
          id: routeSessionId,
          sessionId: routeSessionId,
          title: buildUntitledSessionTitle()
        });
        conversationRecords.value = sortSessions([
          placeholderSession,
          ...conversationRecords.value.filter((item) => item.id !== routeSessionId)
        ]);
        applySessionState(placeholderSession);
      } else {
        createNewSession({ activate: true, persist: false });
      }
      sidebarCollapsed.value = false;
      return;
    }

    const session = createSessionState();
    conversationRecords.value = [session];
    applySessionState(session);
  } catch (error) {
    console.warn('恢复 AI 会话状态失败:', error);
    const session = createSessionState();
    conversationRecords.value = [session];
    applySessionState(session);
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
  conversationRecords.value = sortSessions([
    resetSession,
    ...conversationRecords.value.filter((item) => item.id !== resetSession.id)
  ]);
  persistConversationState();
};

const switchConversation = (sessionId) => {
  if (!sessionId || sessionId === getActiveConversationUiId()) {
    return;
  }
  syncCurrentSessionState();
  const target = conversationRecords.value.find((item) => item.id === sessionId);
  if (!target) {
    return;
  }
  applySessionState(target);
  if (isPersistentConversationId(target.id || target.sessionId)) {
    syncRouteWithPersistentSession(String(target.id || target.sessionId || '').trim());
  }
  persistConversationState();
  nextTick(() => {
    scrollToBottom('auto', true);
  });
};

const deleteConversationRecord = async (sessionId) => {
  if (persistSessionTimer) {
    clearTimeout(persistSessionTimer);
    persistSessionTimer = null;
  }
  const remaining = conversationRecords.value.filter((item) => item.id !== sessionId);
  if (isPersistentConversationId(sessionId)) {
    try {
      await deleteAgentConversationById(sessionId);
    } catch (error) {
      console.warn('删除后端 AI 会话失败:', error);
      uni.showToast({
        title: '删除失败，请稍后重试',
        icon: 'none'
      });
      return;
    }
  }
  deletedSessionIds.add(sessionId);
  persistedSessionIds.delete(sessionId);
  if (remaining.length === 0) {
    const session = createSessionState();
    conversationRecords.value = [session];
    applySessionState(session);
  } else {
    conversationRecords.value = sortSessions(remaining);
    if (getActiveConversationUiId() === sessionId) {
      applySessionState(conversationRecords.value[0]);
    }
  }
  if (String(getActiveConversationUiId() || '').trim() === String(sessionId || '').trim()) {
    setStoredAssistantConversationSessionId(getActivePersistentSessionId());
  }
  if (String(route.query.sessionId || '').trim() === String(sessionId || '').trim()) {
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

const confirmDeleteConversationRecord = (sessionId) => {
  const target = conversationRecords.value.find((item) => item.id === sessionId);
  confirmDialog.value = {
    visible: true,
    type: 'delete-session',
    tone: 'danger',
    targetSessionId: sessionId,
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
    audio: 'audio_completed'
  };
  const pendingType = pendingTypeMap[completedType];
  if (!pendingType) {
    return null;
  }
  const sourceSessionId = String(route.query.sessionId || '').trim() || getStoredAssistantConversationSessionId();
  const sourceMode = String(route.query.mode || 'COMPREHENSIVE').trim() || 'COMPREHENSIVE';
  const sourceScore = Number(route.query.score || 0) || 0;
  const sourceTimestamp = String(route.query.timestamp || '').trim();
  const pendingActionKey = [pendingType, sourceSessionId, sourceMode, sourceScore, sourceTimestamp].join('|');
  if (pendingActionKey && pendingActionKey === lastHandledPendingActionKey) {
    return null;
  }
  return {
    type: pendingType,
    mode: sourceMode,
    sessionId: sourceSessionId,
    score: sourceScore,
    pendingActionKey
  };
};

const clearRoutePendingAssistantAction = () => {
  if (!String(route.query.completedType || '').trim() && !String(route.query.mode || '').trim() && !String(route.query.timestamp || '').trim()) {
    return;
  }
  const fallbackSessionId = String(route.query.sessionId || '').trim() || getActivePersistentSessionId();
  router.replace(buildInterviewAiUrl({
    sessionId: fallbackSessionId
  }));
};

const buildCompletionFollowupQuestion = ({ type = '', mode = '', score = 0 } = {}) => {
  const safeType = String(type || '').trim();
  const safeMode = String(mode || '').trim().toUpperCase();
  const safeScore = Math.max(0, Number(score) || 0);
  const scoreText = safeScore > 0 ? `，得分 ${safeScore} 分` : '';
  const evaluationLabel = safeMode === 'SPECIAL' ? '专项测评' : '综合测评';
  if (safeType === 'resume_completed') {
    return `我已经完成${evaluationLabel}的简历评测${scoreText}。请基于当前会话确认我已完成该环节，结合当前进度告诉我下一步，并给出可执行按钮。`;
  }
  if (safeType === 'questions_completed') {
    return `我已经完成${evaluationLabel}的试题作答${scoreText}。请基于当前会话确认我已完成该环节，结合当前进度告诉我下一步，并给出可执行按钮。`;
  }
  if (safeType === 'audio_completed') {
    return `我已经完成${evaluationLabel}的场景评测${scoreText}。请基于当前会话确认我已完成该环节，结合当前进度告诉我下一步，并给出可执行按钮。`;
  }
  return '';
};

const getModuleDisplayName = (moduleType) => {
  const moduleNameMap = {
    resume: '简历评测',
    questions: '试题作答',
    audio: '场景评测',
    report: '综合报告'
  };
  return moduleNameMap[String(moduleType || '').trim()] || '';
};

const buildCompletionFallbackAssistantReply = (pending = {}, payload = {}) => {
  const safeType = String(pending?.type || '').trim();
  const safeMode = String(pending?.mode || '').trim().toUpperCase();
  const safeScore = Math.max(0, Number(pending?.score || 0) || 0);
  const finishedModuleMap = {
    resume_completed: '简历评测',
    questions_completed: '试题作答',
    audio_completed: '场景评测'
  };
  const evaluationLabel = safeMode === 'SPECIAL' ? '专项测评' : '综合测评';
  const finishedModuleLabel = finishedModuleMap[safeType] || '当前评测';
  const scoreText = safeScore > 0 ? `，本次得分 ${safeScore} 分` : '';
  const nextModule = normalizeTargetModuleValue(payload?.targetModule)
    || mapModuleFromActionType(payload?.actions?.[0]?.type);
  const nextModuleLabel = getModuleDisplayName(nextModule);
  if (nextModuleLabel && nextModuleLabel !== finishedModuleLabel) {
    return `已收到，你刚完成了${evaluationLabel}中的${finishedModuleLabel}${scoreText}。我已结合当前进度为你准备好下一步，建议继续进入${nextModuleLabel}，你可以直接点击下方按钮继续。`;
  }
  return `已收到，你刚完成了${evaluationLabel}中的${finishedModuleLabel}${scoreText}。我已经根据当前会话为你更新了后续建议，你可以直接查看下方推荐操作继续。`;
};

const handlePendingAssistantAction = async () => {
  const routePending = consumeRoutePendingAssistantAction();
  const pending = routePending;
  if (!pending) {
    return;
  }
  showAIChat.value = shouldAutoOpenAiChatOnEntry();
  if (pending.sessionId) {
    const targetSession = conversationRecords.value.find((item) => item.id === pending.sessionId);
    if (targetSession) {
      applySessionState(targetSession);
    } else if (isPersistentConversationId(pending.sessionId)) {
      const placeholderSession = createSessionState({
        id: pending.sessionId,
        sessionId: pending.sessionId,
        title: buildUntitledSessionTitle()
      });
      conversationRecords.value = sortSessions([
        placeholderSession,
        ...conversationRecords.value.filter((item) => item.id !== pending.sessionId)
      ]);
      applySessionState(placeholderSession);
    }
  }
  if (routePending) {
    lastHandledPendingActionKey = String(routePending.pendingActionKey || '');
    applyComprehensiveCompletion(routePending);
    clearRoutePendingAssistantAction();
  }
  hasInteractedWithAssistant.value = true;
  if (pending.mode) {
    selectedEvaluationMode.value = pending.mode;
  }
  await nextTick();
  scrollToBottom('auto', true);
  const completionQuestion = buildCompletionFollowupQuestion(pending);
  if (completionQuestion) {
    await submitAssistantQuestion(completionQuestion, {
      appendUserMessage: true,
      fallbackReplyBuilder: (payload) => buildCompletionFallbackAssistantReply(pending, payload),
      minThinkingDuration: 1200
    });
  }
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

// AI对话功能
const toggleAIChat = (nextState) => {
  const willOpen = typeof nextState === 'boolean' ? nextState : !showAIChat.value;
  if (willOpen && !hasConversationStarted.value) {
    stopHomeWelcomeTyping();
    homeWelcomeText.value = '';
  }
  showAIChat.value = typeof nextState === 'boolean' ? nextState : !showAIChat.value;

  if (showAIChat.value) {
    nextTick(() => {
      scrollToBottom('smooth', true);
      if (messageInputRef.value && typeof messageInputRef.value.focus === 'function') {
        messageInputRef.value.focus();
      }
    });
    if (!hasConversationStarted.value) {
      startHomeWelcomeTyping(true);
    }
  }
  persistConversationState();
};

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value;
};

const handleEnterKey = (event) => {
  event.preventDefault();
  console.log('Enter键被按下，输入内容:', userInput.value);
  if (userInput.value.trim()) {
    console.log('准备发送消息');
    sendMessage();
  } else {
    console.log('输入为空，不发送');
  }
};

const inferEvaluationMode = (text) => {
  const content = String(text || '');
  if (content.includes('综合测评') || content.includes('综合报告')) {
    return 'COMPREHENSIVE';
  }
  if (content.includes('专项测评') || content.includes('专项简历') || content.includes('专项试题') || content.includes('专项场景')) {
    return 'SPECIAL';
  }
  return '';
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

const getAIResponse = async (userQuestion) => {
  try {
    const response = await getInterviewAssistantReply({
      question: userQuestion,
      sessionId: ensureAssistantSessionId(),
      evaluationMode: selectedEvaluationMode.value,
      currentJobId: currentJobId.value,
      currentState: comprehensiveState.value,
      conversationHistory: buildConversationHistory(),
      availableClientActions: AVAILABLE_ASSISTANT_ACTION_TYPES
    });
    if (response.code !== 200) {
      throw new Error(response.message || '获取答案失败');
    }
    return parseAssistantPayload(response.data);
  } catch (error) {
    console.error('API调用失败:', error);
    throw new Error('ai出错啦请刷新界面重试');
  }
};

const submitAssistantQuestion = async (question, options = {}) => {
  const userQuestion = String(question || '').trim();
  if (!userQuestion) {
    return;
  }

  const {
    appendUserMessage = true,
    fallbackReplyBuilder = null,
    minThinkingDuration = 420
  } = options;
  errorMessage.value = '';
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
    conversationRecords.value = sortSessions([
      snapshot,
      ...conversationRecords.value.filter((item) => item.id !== snapshot.id)
    ]);
  }

  assistantActions.value = [];
  hasInteractedWithAssistant.value = true;
  showAIChat.value = true;
  const thinkingMessage = createAssistantThinkingMessage();
  isTyping.value = true;
  persistConversationState();

  await nextTick();
  scrollToBottom('smooth', true);
  if (messageInputRef.value && typeof messageInputRef.value.focus === 'function') {
    messageInputRef.value.focus();
  }

  const requestStartedAt = Date.now();
  try {
    const payload = await getAIResponse(userQuestion);
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
    if (payload.targetMode === 'SPECIAL' || payload.targetMode === 'COMPREHENSIVE') {
      selectedEvaluationMode.value = payload.targetMode;
    }
    if (payload.targetJobHint) {
      pendingSuggestedJob.value = await resolveJobByHint(payload.targetJobHint);
    } else if (payload.intent === 'switch_job' || !payload.targetJobConfirmed) {
      pendingSuggestedJob.value = await findMentionedJobFromText(payload.reply);
    } else {
      pendingSuggestedJob.value = null;
    }
    appendSessionEvent('assistant_reply', {
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
      await appendAiMessageWithTyping(assistantReply, thinkingMessage);
    } else {
      aiMessages.value = aiMessages.value.filter((item) => item !== thinkingMessage);
      isTyping.value = false;
    }
    assistantActions.value = normalizeAssistantActions(payload.actions);
    await nextTick();
    await ensureStreamingScrollToBottom(true);
    persistConversationState();
  } catch (error) {
    console.error('获取AI回复失败:', error);
    aiMessages.value = aiMessages.value.filter((item) => item !== thinkingMessage);
    isTyping.value = false;
    errorMessage.value = '出错啦～请刷新界面重试OnO';
    persistConversationState();
    await nextTick();
    scrollToBottom();
  }
};

const sendMessage = async () => {
  console.log('sendMessage函数被调用，输入内容:', userInput.value);
  if (!userInput.value.trim()) return;
  const text = userInput.value;
  userInput.value = '';
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
    audio: '场景评测',
    report: '综合报告'
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

  if (['upload_resume_special', 'upload_resume_comprehensive', 'go_resume', 'go_questions', 'go_audio', 'go_report'].includes(action.type)) {
    const targetJob = await resolveSuggestedJobForAction();
    if (targetJob?.id) {
      explicitTargetJobId = String(targetJob.id).trim();
      await switchCurrentJobContext(targetJob, {
        source: 'assistant_action',
        actionType: String(action.type || '').trim()
      });
    }
  }

  switch (action.type) {
    case 'choose_special':
      selectedEvaluationMode.value = 'SPECIAL';
      await submitAssistantQuestion('我想做专项测评', { appendUserMessage: true });
      return;
    case 'choose_comprehensive':
      selectedEvaluationMode.value = 'COMPREHENSIVE';
      await submitAssistantQuestion('我想做综合测评', { appendUserMessage: true });
      return;
    case 'upload_resume_special':
      selectedEvaluationMode.value = 'SPECIAL';
      await goToModule(resolvedTargetModule || 'resume', {
        jobId: explicitTargetJobId,
        source: 'assistant_action',
        actionType: action.type,
        label: action.label
      });
      return;
    case 'upload_resume_comprehensive':
      selectedEvaluationMode.value = 'COMPREHENSIVE';
      await goToModule(resolvedTargetModule || 'resume', {
        jobId: explicitTargetJobId,
        source: 'assistant_action',
        actionType: action.type,
        label: action.label
      });
      return;
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
    case 'go_audio':
      await goToModule(resolvedTargetModule || 'audio', {
        jobId: explicitTargetJobId,
        source: 'assistant_action',
        actionType: action.type,
        label: action.label
      });
      return;
    case 'go_report':
      await goToModule(resolvedTargetModule || 'report', {
        jobId: explicitTargetJobId,
        source: 'assistant_action',
        actionType: action.type,
        label: action.label
      });
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
  comprehensiveState.value.resume = {
    completed: true,
    inProgress: false,
    score: safeScore,
    analysisId: `chat-${Date.now()}`,
    startTime: comprehensiveState.value.resume.startTime || formatDateTimeUtc8(),
    endTime: formatDateTimeUtc8(),
    attempts: (comprehensiveState.value.resume.attempts || 0) + 1
  };
  comprehensiveState.value.overall = {
    ...comprehensiveState.value.overall,
    startTime: comprehensiveState.value.overall.startTime || formatDateTimeUtc8(),
    status: 'in_progress'
  };
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
  try {
    const effectiveMode = selectedEvaluationMode.value === 'COMPREHENSIVE' ? 'COMPREHENSIVE' : 'SPECIAL';
    selectedEvaluationMode.value = effectiveMode;
    await addLocalAiMessage(
      effectiveMode === 'COMPREHENSIVE'
        ? '收到简历，我会按综合测评里的简历环节先帮你分析，完成后你可以继续做试题作答和场景评测。'
        : '收到简历，我会按专项简历测评帮你分析。'
    );

    const extractRes = await extractResumeContent(file);
    if (extractRes.code !== 200 || !extractRes.data) {
      throw new Error(extractRes.message || '简历内容提取失败');
    }

    const resumeContent = extractRes.data;
    const jobDetails = await resolveJobDetails();

    let analysisResponse;
    if (effectiveMode === 'COMPREHENSIVE') {
      analysisResponse = await analyzeComprehensiveResume(resumeContent, jobDetails || {});
    } else {
      const jobDesc = jobDetails
        ? `岗位名称：${jobDetails.name}\n岗位描述：${jobDetails.description || ''}`
        : '';
      analysisResponse = await getResumeAnalysisByXunfei(jobDesc, resumeContent);
    }

    if (analysisResponse.code !== 200 || !analysisResponse.data) {
      throw new Error(analysisResponse.message || '简历分析失败');
    }

    const summary = buildResumeSummary(analysisResponse.data);
    const resumeScore = extractResumeScoreFromAnalysis(analysisResponse.data);

    if (effectiveMode === 'COMPREHENSIVE') {
      markResumeCompletedInComprehensive(resumeScore);
    }

    await addLocalAiMessage(summary);
    await submitAssistantQuestion(
      effectiveMode === 'COMPREHENSIVE'
        ? '我刚完成了综合测评中的简历评测，请结合当前进度告诉我下一步，并给出可执行按钮。'
        : '我刚完成了专项简历评测，请结合当前情况告诉我下一步，并在有必要时给出可执行按钮。',
      { appendUserMessage: false }
    );
  } catch (error) {
    console.error('聊天内简历分析失败:', error);
    errorMessage.value = error.message || '聊天内简历分析失败，请重试';
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
      errorMessage.value = '选择简历失败，请重试';
    }
  });
};

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));
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

let homeWelcomeTypingSeq = 0;

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

function stopHomeWelcomeTyping() {
  homeWelcomeTypingSeq += 1;
  homeWelcomeText.value = '';
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

// 计算属性
const resumeStatusText = computed(() => {
  if (comprehensiveState.value.resume.inProgress) return '进行中';
  return comprehensiveState.value.resume.completed ? '已完成' : '未开始';
});

const questionsStatusText = computed(() => {
  if (comprehensiveState.value.questions.inProgress) return '进行中';
  return comprehensiveState.value.questions.completed ? '已完成' : '未开始';
});

const audioStatusText = computed(() => {
  if (comprehensiveState.value.audio.inProgress) return '进行中';
  return comprehensiveState.value.audio.completed ? '已完成' : '未开始';
});

const canGenerateReport = computed(() => {
  return comprehensiveState.value.resume.completed &&
         comprehensiveState.value.questions.completed &&
         comprehensiveState.value.audio.completed;
});

const resetComprehensiveState = () => {
  comprehensiveState.value = createDefaultComprehensiveState();
};

const applyComprehensiveCompletion = ({ type = '', mode = '', score = 0 } = {}) => {
  const safeType = String(type || '').trim();
  const safeMode = String(mode || '').trim().toUpperCase();
  if (!safeType) {
    return;
  }
  const moduleMap = {
    resume_completed: 'resume',
    questions_completed: 'questions',
    audio_completed: 'audio'
  };
  const safeScore = Math.max(0, Number(score) || 0);
  const now = formatDateTimeUtc8();
  const nextState = normalizeComprehensiveState(comprehensiveState.value);
  if (safeMode === 'COMPREHENSIVE' || !safeMode) {
    nextState.overall = {
      ...nextState.overall,
      startTime: nextState.overall.startTime || now,
      endTime: safeType === 'audio_completed' ? now : nextState.overall.endTime,
      status: safeType === 'audio_completed' ? 'completed' : 'in_progress'
    };
  }
  if (safeType === 'resume_completed') {
    nextState.resume = {
      ...nextState.resume,
      completed: true,
      inProgress: false,
      score: safeScore,
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
      interviewId: nextState.questions.interviewId || `questions-${Date.now()}`,
      startTime: nextState.questions.startTime || now,
      endTime: now,
      attempts: Math.max(1, Number(nextState.questions.attempts || 0) + 1)
    };
  } else if (safeType === 'audio_completed') {
    nextState.audio = {
      ...nextState.audio,
      completed: true,
      inProgress: false,
      score: safeScore,
      assessmentId: nextState.audio.assessmentId || `audio-${Date.now()}`,
      startTime: nextState.audio.startTime || now,
      endTime: now,
      attempts: Math.max(1, Number(nextState.audio.attempts || 0) + 1)
    };
    nextState.overall.totalScore = Math.round((
      Number(nextState.resume.score || 0)
      + Number(nextState.questions.score || 0)
      + Number(nextState.audio.score || 0)
    ) / 3);
  }
  comprehensiveState.value = nextState;
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
  const effectiveJobId = String(options?.jobId || currentJobId.value || activeConversationRecord.value?.jobId || '').trim();
  const currentSnapshot = syncCurrentSessionState();
  const effectiveSessionId = getActivePersistentSessionId(String(currentSnapshot?.id || '').trim());
  const eventPayload = {
    source: String(options?.source || 'manual_click').trim(),
    module: moduleType,
    jobId: effectiveJobId,
    mode: selectedEvaluationMode.value,
    actionType: String(options?.actionType || '').trim(),
    label: String(options?.label || '').trim()
  };
  console.log('[AI Interview] 模块点击事件触发:', moduleType);
  console.log('[AI Interview] canGenerateReport状态:', canGenerateReport.value);
  console.log('[AI Interview] 当前jobId:', currentJobId.value);
  console.log('[AI Interview] 本次跳转effectiveJobId:', effectiveJobId);
  console.log('[AI Interview] 综合状态:', {
    resume: comprehensiveState.value.resume.completed,
    questions: comprehensiveState.value.questions.completed,
    audio: comprehensiveState.value.audio.completed
  });

  const isComprehensiveMode = selectedEvaluationMode.value === 'COMPREHENSIVE';
  const routes = isComprehensiveMode
    ? {
        resume: '/pages/comprehensive-resume/index',
        questions: '/pages/comprehensive-questions/index',
        audio: '/pages/comprehensive-scenario/index'
      }
    : {
        resume: '/pages/interview-resume/index',
        questions: '/pages/interview-questions/index',
        audio: '/pages/interview-scenario/index'
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
  if (isComprehensiveMode && moduleType === 'audio' && comprehensiveState.value.audio.completed) {
    appendSessionEvent('module_navigation_blocked', {
      ...eventPayload,
      reason: 'audio_already_completed'
    });
    uni.showToast({
      title: '场景评测作答已结束',
      icon: 'none',
      duration: 2000
    });
    return;
  }
  if (moduleType === 'report') {
    if (!isComprehensiveMode) {
      appendSessionEvent('module_navigation_blocked', {
        ...eventPayload,
        reason: 'report_unavailable_in_special_mode'
      });
      uni.showToast({
        title: '专项测评暂无综合报告',
        icon: 'none',
        duration: 2000
      });
      return;
    }
    console.log('[AI Interview] 综合报告就绪，准备跳转');
    // 确保传递jobId参数到综合报告页面
    if (effectiveJobId) {
      await flushConversationPersistence(currentSnapshot, effectiveJobId);
      const reportUrl = `/pages/comprehensive-report/index?jobId=${effectiveJobId}&type=overall&from=ai-interview${effectiveSessionId ? `&sessionId=${encodeURIComponent(effectiveSessionId)}` : ''}`;
      appendSessionEvent('module_navigation_started', {
        ...eventPayload,
        meta: {
          route: reportUrl
        }
      });
      console.log('[AI Interview] 跳转到综合报告页面，URL:', reportUrl);
      smoothPush(reportUrl);
    } else {
      await flushConversationPersistence(currentSnapshot);
      appendSessionEvent('module_navigation_started', {
        ...eventPayload,
        meta: {
          route: '/pages/comprehensive-report/index?type=overall'
        }
      });
      console.warn('[AI Interview] 跳转到综合报告页面，但没有jobId可传递');
      smoothPush('/pages/comprehensive-report/index?type=overall');
    }
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

  if (isComprehensiveMode && moduleType === 'audio' && !comprehensiveState.value.questions.completed) {
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

  if (routes[moduleType]) {
    // 确保传递jobId参数
    if (effectiveJobId) {
      // 记录来源为AI面试页面
      try {
        sessionStorage.setItem('fromAiInterview', 'true');
      } catch (err) {
        console.error('无法保存来源页面信息:', err);
      }
      const query = [
        `jobId=${encodeURIComponent(effectiveJobId)}`,
        'from=ai-interview',
        `mode=${encodeURIComponent(selectedEvaluationMode.value || '')}`
      ];
      if (effectiveSessionId) {
        query.push(`sessionId=${encodeURIComponent(effectiveSessionId)}`);
      }
      await flushConversationPersistence(currentSnapshot, effectiveJobId);
      const routeUrl = `${routes[moduleType]}?${query.join('&')}`;
      appendSessionEvent('module_navigation_started', {
        ...eventPayload,
        meta: {
          route: routeUrl
        }
      });
      smoothPush(routeUrl);
      console.log(`跳转到${moduleType}页面，携带jobId:`, effectiveJobId);
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
  console.log('[Comprehensive Test] Component mounted');
  await enterPageFlow();

  uni.$on('resumeCompleted', (data) => {
    const emittedSessionId = String(data?.sessionId || '').trim();
    if (!emittedSessionId || emittedSessionId === String(getActiveConversationUiId() || '').trim()) {
      applyComprehensiveCompletion({
        type: 'resume_completed',
        score: Number(data?.score || 0) || 0
      });
    }
  });

});

onBeforeUnmount(() => {
  persistConversationState();
  try {
    uni.$off('resumeCompleted');
    console.log('[Comprehensive Test] 已清理事件监听器');
  } catch (e) {
    console.warn('[Comprehensive Test] 清理事件监听器失败:', e);
  }
});

// uni-app 生命周期
onLoad(() => {
  console.log('[Comprehensive Test] Page loaded');
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
    if (messageInputRef.value && typeof messageInputRef.value.focus === 'function') {
      messageInputRef.value.focus();
    }
  });
};

// 正在输入状态
const isTyping = ref(false);

// 消息输入框引用
const messageInputRef = ref(null);

// 错误处理相关状态
const errorMessage = ref('');

// 重试上一次的请求
const retry = async () => {
  // 获取最后一条用户消息
  const lastUserMessage = [...aiMessages.value].reverse().find(msg => msg.type === 'user');
  
  if (lastUserMessage) {
    errorMessage.value = '';
    await submitAssistantQuestion(lastUserMessage.content, { appendUserMessage: false });
  }
};

const showFeatureModal = ref(false);
const featureKey = ref('');
const featureMap = {
  ai: {
    title: 'AI智能评估',
    desc: '以大模型为核心，从多源数据理解你的能力表现，生成专业评估与建议。',
    points: ['实时语义理解与问答', '能力维度自动抽取', '个性化改进建议']
  },
  analysis: {
    title: '多维度分析',
    desc: '结合简历、试题与场景表现，进行结构化的多维度综合分析。',
    points: ['简历与岗位匹配度', '知识与技能掌握度', '场景即兴应对能力']
  },
  match: {
    title: '精准匹配',
    desc: '将你的能力画像与岗位要求进行精准比对，识别优势与短板。',
    points: ['关键能力权重匹配', '差距定位与补齐建议', '岗位适配度评分']
  }
};
const featureContent = computed(() => featureMap[featureKey.value] || { title: '', desc: '', points: [] });
const openFeature = (key) => { featureKey.value = key; showFeatureModal.value = true; };
const closeFeature = () => { showFeatureModal.value = false; };

// 返回按钮逻辑，始终跳转到/pages/interview-interface/index并带jobId
const handleBack = () => {
  // 获取jobId，优先从currentJobId、route、localStorage
  let jobId = null;
  try {
    jobId = currentJobId?.value || route?.query?.jobId || localStorage.getItem('currentJobId') || uni.getStorageSync('currentJobId');
  } catch (e) {}
  if (!jobId) jobId = 1; // fallback
  smoothReLaunch(`/pages/interview-interface/index?jobId=${jobId}`);
};
</script>

<style scoped>
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

.cyber-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

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
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}

.scanning-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.horizontal-scan {
  position: absolute;
  top: 50%;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, 
    rgba(0, 255, 255, 0) 0%, 
    rgba(0, 255, 255, 0.5) 20%, 
    rgba(0, 255, 255, 0.8) 50%,
    rgba(0, 255, 255, 0.5) 80%,
    rgba(0, 255, 255, 0) 100%);
  box-shadow: 0 0 10px rgba(0, 255, 255, 0.8);
  animation: horizontalScan 8s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}

@keyframes horizontalScan {
  0% { transform: translateY(-50vh); }
  50% { transform: translateY(50vh); }
  100% { transform: translateY(-50vh); }
}

.vertical-scan {
  position: absolute;
  top: 0;
  left: 50%;
  width: 2px;
  height: 100%;
  background: linear-gradient(0deg, 
    rgba(0, 255, 255, 0) 0%, 
    rgba(0, 255, 255, 0.5) 20%, 
    rgba(0, 255, 255, 0.8) 50%,
    rgba(0, 255, 255, 0.5) 80%,
    rgba(0, 255, 255, 0) 100%);
  box-shadow: 0 0 10px rgba(0, 255, 255, 0.8);
  animation: verticalScan 12s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}

@keyframes verticalScan {
  0% { transform: translateX(-50vw); }
  50% { transform: translateX(50vw); }
  100% { transform: translateX(-50vw); }
}

.diagonal-scan {
  position: absolute;
  top: 0;
  left: 0;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, 
    rgba(0, 255, 255, 0) 0%, 
    rgba(0, 255, 255, 0.1) 10%, 
    rgba(0, 255, 255, 0.3) 20%,
    rgba(0, 255, 255, 0) 30%);
  transform-origin: 0 0;
  animation: diagonalScan 15s linear infinite;
}

@keyframes diagonalScan {
  0% { transform: translate(-100%, -100%) rotate(0deg); }
  100% { transform: translate(0%, 0%) rotate(360deg); }
}

.radar-scan {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 0;
  height: 0;
  border-radius: 50%;
  box-shadow: 0 0 0 1px rgba(0, 255, 255, 0.3);
  animation: radarPulse 4s ease-out infinite;
}

.radar-scan::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 2px;
  height: 50%;
  background: linear-gradient(to top, rgba(0, 255, 255, 0.8), transparent);
  transform-origin: bottom center;
  animation: radarSweep 3s linear infinite;
}

@keyframes radarPulse {
  0% {
    width: 0;
    height: 0;
    opacity: 0.8;
  }
  100% {
    width: 200vw;
    height: 200vw;
    opacity: 0;
  }
}

@keyframes radarSweep {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.floating-particles {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.particle {
  position: absolute;
  width: 2px;
  height: 2px;
  background: #00ffff;
  border-radius: 50%;
  opacity: 0;
  box-shadow: 0 0 2px #00ffff;
  animation: floatParticle 10s ease-out infinite;
}

@keyframes floatParticle {
  0% {
    transform: translate(0, 0);
    opacity: 0;
  }
  10% {
    opacity: 0.8;
  }
  90% {
    opacity: 0.6;
  }
  100% {
    opacity: 0;
  }
}

.energy-field {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(0, 255, 255, 0.1) 0%, transparent 70%);
  animation: pulse 4s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.3; }
  50% { transform: translate(-50%, -50%) scale(1.2); opacity: 0.6; }
}

/* AI助手界面 */
.ai-assistant {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 1000;
}

.notification-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 22px;
  height: 22px;
  background: #ff3860;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  box-shadow: 0 0 5px rgba(255, 56, 96, 0.5);
  animation: pulse 2s infinite;
}

.ai-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(45deg, #00ffff, #0080ff);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.5);
  transition: all 0.3s ease;
  animation: attention 3s ease-in-out infinite;
}

@keyframes attention {
  0%, 100% { transform: scale(1); box-shadow: 0 0 20px rgba(0, 255, 255, 0.5); }
  50% { transform: scale(1.1); box-shadow: 0 0 30px rgba(0, 255, 255, 0.8); }
}

.ai-avatar:hover {
  transform: scale(1.15);
  box-shadow: 0 0 40px rgba(0, 255, 255, 1);
}

.ai-core {
  width: 20px;
  height: 20px;
  background: #ffffff;
  border-radius: 50%;
  animation: corePulse 2s ease-in-out infinite;
}

@keyframes corePulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.8; }
}

.ai-pulse {
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  border: 2px solid #00ffff;
  border-radius: 50%;
  animation: pulseRing 2s ease-in-out infinite;
}

@keyframes pulseRing {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

.ai-chat-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 28px;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(8px);
}

.ai-chat-container {
  width: min(1180px, 92vw);
  height: min(760px, 88vh);
  background-color: rgba(0, 0, 0, 0.88);
  border: 1px solid #00ffff;
  border-radius: 22px;
  box-shadow: 0 0 30px rgba(0, 255, 255, 0.28);
  display: flex;
  overflow: hidden;
  z-index: 1001;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
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
  overflow: hidden;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 64px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(0, 255, 255, 0.12);
  pointer-events: none;
}

.ai-chat-container.sidebar-collapsed .sidebar-header {
  width: 84px;
  padding: 0;
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
  flex: 0 0 28px;
  pointer-events: auto;
  color: rgba(255, 255, 255, 0.9);
  width: 28px;
  height: 28px;
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
  color: #ffffff;
  opacity: 0.9;
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;
}

.toolbar-icon-svg {
  display: block;
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
  padding: 0 22px;
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

.ai-avatar-small {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(45deg, #00ffff, #0080ff);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-avatar-small::after {
  content: '';
  position: absolute;
  width: 12px;
  height: 12px;
  background: #ffffff;
  border-radius: 50%;
  animation: corePulse 2s ease-in-out infinite;
}

.ai-name {
  color: #00ffff;
  font-weight: bold;
  font-size: 16px;
  letter-spacing: 0.5px;
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
  background: linear-gradient(135deg, #0080ff, #00ffff);
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 10px rgba(0, 255, 255, 0.2);
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
  color: rgba(255, 255, 255, 0.82);
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
  border: 1px solid rgba(0, 229, 255, 0.28);
  background: linear-gradient(135deg, rgba(124, 77, 255, 0.12), rgba(0, 229, 255, 0.08));
}

.recommended-action-text {
  min-width: 0;
}

.recommended-action-title {
  font-size: 13px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 4px;
}

.recommended-action-desc {
  font-size: 12px;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.72);
}

.recommended-action-btn {
  flex-shrink: 0;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #7c4dff, #00e5ff);
  color: #ffffff;
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
  background: linear-gradient(135deg, rgba(124, 77, 255, 0.45), rgba(0, 229, 255, 0.35));
  border-color: rgba(0, 229, 255, 0.7);
  color: #ffffff;
}

.quick-btn.highlighted {
  background: linear-gradient(135deg, rgba(0, 229, 255, 0.28), rgba(124, 77, 255, 0.26));
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

/* 主内容区域 */
.main-content {
  position: relative;
  z-index: 1;
  padding: 20px 20px;
  max-width: 1200px;
  margin: 0 auto;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
  overflow-y: hidden;
}

/* 全息投影标题 */
.holographic-header {
  text-align: center;
  margin-bottom: 10px;
  flex-shrink: 1;
  flex-grow: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.title-container {
  position: relative;
  display: inline-block;
}

.main-title {
  font-size: 2em;
  font-weight: bold;
  background: linear-gradient(45deg, #00ffff, #ffffff, #0080ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  margin: 0;
}

.title-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, #00ffff, #ffffff, #0080ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: blur(10px);
  opacity: 0.5;
  animation: titleGlow 3s ease-in-out infinite;
}

@keyframes titleGlow {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.7; }
}

.subtitle {
  font-size: 1em;
  color: rgba(255, 255, 255, 0.8);
  margin: 6px 0 12px;
}

/* 模块选择区域 */
  .modules-hub {
    margin-bottom: 20px;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
  
  @media (max-width: 768px) {
    .modules-hub {
      flex: none;
      margin-bottom: 15px;
    }
  }

.hub-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  margin-top: 20px;
  width: 100%;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
}

.module-hologram {
  position: relative;
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid #00ffff;
  border-radius: 16px;
  padding: 12px 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  overflow: hidden;
  min-height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.module-hologram:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0, 255, 255, 0.3);
}

.module-hologram.completed {
  border-color: #00ff00;
  background: rgba(0, 255, 0, 0.1);
}

.module-hologram.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

.hologram-frame {
  position: relative;
  z-index: 2;
  text-align: center;
}

.module-icon {
  font-size: 2em;
  margin-bottom: 8px;
  filter: drop-shadow(0 0 10px rgba(0, 255, 255, 0.5));
}

.hologram-frame h3 {
  font-size: 1.1em;
  margin: 0 0 6px;
  color: #ffffff;
}

.module-desc {
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8px;
  line-height: 1.2;
  font-size: 0.75em;
}

.progress-ring {
  position: relative;
  width: 50px;
  height: 50px;
  margin: 8px auto;
}

.progress-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.progress-bg {
  fill: none;
  stroke: rgba(0, 255, 255, 0.3);
  stroke-width: 8;
}

.progress-fill {
  fill: none;
  stroke: #00ffff;
  stroke-width: 8;
  stroke-linecap: round;
  stroke-dasharray: 283;
  stroke-dashoffset: 283;
  transition: stroke-dashoffset 1s ease;
}

.progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 1.2em;
  font-weight: bold;
  color: #00ffff;
}

.status-indicator {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.status-indicator.completed {
  background: #00ff00;
  box-shadow: 0 0 10px rgba(0, 255, 0, 0.5);
}

.status-indicator.pending {
  background: #ffaa00;
  box-shadow: 0 0 10px rgba(255, 170, 0, 0.5);
}

.hologram-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at center, rgba(0, 255, 255, 0.1) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.module-hologram:hover .hologram-glow {
  opacity: 1;
}

/* 综合报告模块特殊样式 */
.report-module {
  border-color: #00ffff;
  background: rgba(0, 0, 0, 0.6);
}

.report-module:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0, 255, 255, 0.3);
}

.report-module.completed {
  border-color: #00ff00;
  background: rgba(0, 255, 0, 0.15);
}

.report-module.completed:hover {
  box-shadow: 0 20px 40px rgba(0, 255, 0, 0.3);
}

/* 护眼模式适配 */
.eye-care {
  background: linear-gradient(135deg, #f0f2e6 0%, #e8ecd9 50%, #dfe3d0 100%) !important;
}

.eye-care .grid-lines {
  background-image: 
    linear-gradient(rgba(62, 142, 65, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(62, 142, 65, 0.1) 1px, transparent 1px);
}

.eye-care .horizontal-scan,
.eye-care .vertical-scan {
  background: linear-gradient(90deg, 
    rgba(62, 142, 65, 0) 0%, 
    rgba(62, 142, 65, 0.3) 20%, 
    rgba(62, 142, 65, 0.5) 50%,
    rgba(62, 142, 65, 0.3) 80%,
    rgba(62, 142, 65, 0) 100%);
  box-shadow: 0 0 10px rgba(62, 142, 65, 0.3);
}

.eye-care .diagonal-scan {
  background: linear-gradient(45deg, 
    rgba(62, 142, 65, 0) 0%, 
    rgba(62, 142, 65, 0.1) 10%, 
    rgba(62, 142, 65, 0.2) 20%,
    rgba(62, 142, 65, 0) 30%);
}

.eye-care .radar-scan {
  box-shadow: 0 0 0 1px rgba(62, 142, 65, 0.2);
}

.eye-care .radar-scan::before {
  background: linear-gradient(to top, rgba(62, 142, 65, 0.5), transparent);
}

.eye-care .particle {
  background: rgba(62, 142, 65, 0.5);
}

.eye-care .energy-field {
  background: radial-gradient(circle, rgba(62, 142, 65, 0.1) 0%, transparent 70%);
}

.eye-care .main-title {
  background: linear-gradient(45deg, #3e8e41, #2d5a2d, #357a38);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.eye-care .title-glow {
  background: linear-gradient(45deg, #3e8e41, #2d5a2d, #357a38);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.eye-care .module-hologram {
  background: rgba(255, 255, 255, 0.9);
  border-color: #3e8e41;
  color: #333;
}

.eye-care .module-hologram h3 {
  color: #333;
}

.eye-care .module-desc {
  color: #666;
}

.eye-care .report-module {
  background: rgba(255, 255, 255, 0.9);
  border-color: #3e8e41;
  color: #333;
}

.eye-care .report-module.completed {
  border-color: #2d5a2d;
  background: rgba(62, 142, 65, 0.1);
}

.eye-care .report-module h3 {
  color: #333;
}

.eye-care .report-module .module-desc {
  color: #666;
}

/* 护眼模式下的关于我们部分 */
.eye-care .about-section {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(62, 142, 65, 0.3);
}

.eye-care .team-name {
  background: linear-gradient(45deg, #3e8e41, #2d5a2d);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 10px rgba(62, 142, 65, 0.3);
}

.eye-care .project-info {
  color: rgba(0, 0, 0, 0.7);
}

.eye-care .tech-item {
  background: rgba(62, 142, 65, 0.1);
  border-color: rgba(62, 142, 65, 0.3);
}

.eye-care .tech-item:hover {
  background: rgba(62, 142, 65, 0.15);
  box-shadow: 0 5px 15px rgba(62, 142, 65, 0.2);
}

.eye-care .tech-icon {
  filter: drop-shadow(0 0 5px rgba(62, 142, 65, 0.5));
}

.eye-care .tech-text {
  color: rgba(0, 0, 0, 0.8);
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

.eye-care .new-chat-btn:hover,
.eye-care .sidebar-toggle-btn:hover {
  color: #2f4b2e;
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

.eye-care .ai-avatar-small {
  background: linear-gradient(45deg, #3e8e41, #357a38);
}

.eye-care .ai-avatar-small::after {
  background: #f0f2e6;
}

.eye-care .ai-name {
  color: #3e8e41;
}

.eye-care .chat-session-name {
  color: rgba(74, 95, 69, 0.72);
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
  background: linear-gradient(135deg, #6d9363, #5d8156);
  box-shadow: 0 6px 16px rgba(93, 129, 86, 0.2);
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

.eye-care .message.user .message-content {
  color: #fff;
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
  color: rgba(83, 101, 76, 0.78);
  opacity: 1;
}

.eye-care .inline-thinking,
.eye-care .inline-thinking-text {
  color: #4b5f45;
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
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(103, 136, 93, 0.28);
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

.assistant-wrapper {
  display: flex;
  align-items: center;
  flex-direction: row-reverse;
  gap: 10px;
}

.session-list::-webkit-scrollbar {
  width: 6px;
}

.session-list::-webkit-scrollbar-thumb {
  background: rgba(0, 255, 255, 0.22);
  border-radius: 999px;
}

.assistant-text {
  background: rgba(0, 0, 0, 0.7);
  color: #00ffff;
  padding: 8px 12px;
  border-radius: 20px;
  font-size: 14px;
  white-space: nowrap;
  border: 1px solid #00ffff;
  box-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
  animation: fadeInOut 3s ease-in-out infinite;
  transition: all 0.3s ease;
  pointer-events: none;
}

.assistant-text:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 15px rgba(0, 255, 255, 0.8);
}

/* 护眼模式适配 */
.eye-care .assistant-text {
  background: rgba(255, 255, 255, 0.9);
  color: #3e8e41;
  border-color: #3e8e41;
  box-shadow: 0 0 10px rgba(62, 142, 65, 0.5);
}

.clickable { cursor: pointer; }
.feature-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.feature-modal {
  width: 520px;
  max-width: 92vw;
  border-radius: 16px;
  background: rgba(0, 0, 0, 0.85);
  border: 1px solid rgba(0, 255, 255, 0.35);
  box-shadow: 0 10px 30px rgba(0, 255, 255, 0.25);
  overflow: hidden;
  position: relative;
  animation: fadeIn 0.25s ease;
}
.feature-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  background: linear-gradient(90deg, rgba(0,128,255,0.35), rgba(0,255,255,0.25));
}
.feature-title {
  color: #00ffff;
  font-weight: 700;
  font-size: 18px;
}
.feature-close {
  position: absolute;
  top: 10px;
  right: 12px;
  background: transparent;
  border: none;
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  outline: none;
  box-shadow: none;
  -webkit-appearance: none;
  appearance: none;
  padding: 0;
  line-height: 1;
}
.feature-body {
  padding: 18px;
}
.feature-desc {
  color: rgba(255,255,255,0.9);
  line-height: 1.7;
}
.feature-list {
  margin-top: 10px;
  padding-left: 18px;
}
.feature-list li {
  color: rgba(255,255,255,0.85);
  margin-bottom: 6px;
}
.feature-footer {
  padding: 0 18px 18px;
  display: flex;
  justify-content: flex-end;
}

.confirm-modal {
  width: min(640px, calc(100vw - 48px));
  max-width: 640px;
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(7, 20, 34, 0.98), rgba(3, 10, 20, 0.98));
  border: 1px solid rgba(0, 255, 255, 0.18);
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.42), 0 0 22px rgba(0, 255, 255, 0.08);
}

.confirm-actions {
  gap: 14px;
  justify-content: flex-end;
}

.confirm-cancel-btn,
.confirm-primary-btn {
  min-width: 120px;
  height: 46px;
  border-radius: 12px;
  padding: 0 18px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 15px;
  font-weight: 600;
}

.confirm-cancel-btn {
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.confirm-primary-btn {
  color: #ffffff;
}

.confirm-primary-danger {
  background: linear-gradient(90deg, #00d5ff, #0a8bff);
  box-shadow: 0 10px 24px rgba(10, 139, 255, 0.24);
}

.confirm-primary-neutral {
  background: linear-gradient(90deg, #00d5ff, #0a8bff);
  box-shadow: 0 10px 24px rgba(10, 139, 255, 0.24);
}

.confirm-header-danger {
  background: linear-gradient(90deg, rgba(0, 213, 255, 0.12), rgba(10, 139, 255, 0.08));
}

.confirm-header-neutral {
  background: linear-gradient(90deg, rgba(0, 213, 255, 0.12), rgba(10, 139, 255, 0.08));
}

.confirm-danger {
  border-color: rgba(0, 255, 255, 0.2);
}

.confirm-neutral {
  border-color: rgba(0, 255, 255, 0.2);
}

.confirm-cancel-btn:hover,
.confirm-primary-btn:hover {
  transform: translateY(-1px);
}

.confirm-title-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.confirm-subtitle {
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
  line-height: 1;
}

.confirm-close-btn {
  top: 18px;
  right: 18px;
  font-size: 18px;
}

.confirm-body {
  padding: 30px 28px 18px;
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
  background: rgba(0, 213, 255, 0.12);
  color: #59dcff;
  border: 1px solid rgba(0, 213, 255, 0.18);
}

.confirm-icon-neutral {
  background: rgba(0, 213, 255, 0.12);
  color: #67d8ff;
  border: 1px solid rgba(0, 213, 255, 0.18);
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
.eye-care .feature-overlay { background: rgba(0,0,0,0.35); backdrop-filter: blur(4px); }
.eye-care .feature-modal { background: rgba(255,255,255,0.95); border-color: rgba(62,142,65,0.35); box-shadow: 0 10px 30px rgba(62,142,65,0.25); }
.eye-care .feature-header { background: linear-gradient(90deg, rgba(127,176,105,0.25), rgba(144,198,149,0.2)); }
.eye-care .feature-title { color: #3e8e41; }
.eye-care .feature-desc, .eye-care .feature-list li { color: #333; }
.eye-care .feature-ok { background: linear-gradient(90deg, #7fb069, #90c695); }
.eye-care .feature-close { color: #3e8e41; background: transparent; box-shadow: none; outline: none; }
.eye-care .confirm-cancel-btn { background: rgba(62, 142, 65, 0.08); color: #2d5a2d; border-color: rgba(62, 142, 65, 0.2); }
.eye-care .confirm-primary-danger { background: linear-gradient(90deg, #d96c6c, #e5936b); }
.eye-care .confirm-primary-neutral { background: linear-gradient(90deg, #7fb069, #90c695); }
.eye-care .confirm-header-danger { background: linear-gradient(90deg, rgba(217,108,108,0.22), rgba(229,147,107,0.18)); }
.eye-care .confirm-header-neutral { background: linear-gradient(90deg, rgba(127,176,105,0.25), rgba(144,198,149,0.2)); }
.eye-care .confirm-danger { border-color: rgba(217,108,108,0.28); box-shadow: 0 10px 30px rgba(217,108,108,0.18); }
.eye-care .confirm-subtitle { color: rgba(45, 90, 45, 0.62); }
.eye-care .confirm-icon-danger { background: rgba(217, 108, 108, 0.12); color: #b55555; border-color: rgba(217, 108, 108, 0.2); }
.eye-care .confirm-icon-neutral { background: rgba(127, 176, 105, 0.12); color: #4f7f4a; border-color: rgba(127, 176, 105, 0.2); }

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
  
  .ai-chat-container {
    width: 320px;
    height: 400px;
    bottom: 70px;
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
}

@media (max-width: 480px) {
  .ai-interview-container {
    height: 100vh;
    overflow-y: auto;
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
  
  .ai-chat-container {
    width: 90vw;
    height: 70vh;
    right: 5vw;
    left: 5vw;
    bottom: 80px;
    max-height: 400px;
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
  
}

/* 护眼模式适配 */
.eye-care .error-message {
  background: rgba(255, 77, 77, 0.05);
  border-color: rgba(255, 77, 77, 0.2);
  color: #d32f2f;
}

.eye-care .error-message .retry-btn {
  border-color: #d32f2f;
  color: #d32f2f;
}

.eye-care .error-message .retry-btn:hover {
  background: rgba(255, 77, 77, 0.05);
}

.top-navigation {
  display: flex;
  align-items: center;
  width: 100%;
  position: relative;
  margin-bottom: 20px;
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
  z-index: 100;
  margin-right: 15px;
  box-shadow: 0 0 10px rgba(0, 255, 255, 0.2);
  flex-shrink: 0;
}

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

/* 关于我们部分样式 */
.about-section {
  margin-top: 20px;
  text-align: center;
  padding: 15px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 16px;
  border: 1px solid rgba(0, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  flex-shrink: 0;
  width: 100%;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
}

.about-content {
  max-width: 600px;
  margin: 0 auto;
}

.team-name {
  font-size: 1.2em;
  font-weight: bold;
  background: linear-gradient(45deg, #00ffff, #0080ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 10px;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.3);
}

.project-info {
  font-size: 0.9em;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 15px;
  line-height: 1.5;
}

.tech-highlights {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 15px;
  flex-wrap: wrap;
}

.tech-item {
  text-align: center;
  padding: 8px 12px;
  background: rgba(0, 255, 255, 0.05);
  border-radius: 10px;
  border: 1px solid rgba(0, 255, 255, 0.1);
  transition: all 0.3s ease;
  width: 120px;
  height: 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.tech-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 255, 255, 0.2);
  background: rgba(0, 255, 255, 0.15);
}

.tech-icon {
  display: block;
  font-size: 1.5em;
  margin-bottom: 6px;
  filter: drop-shadow(0 0 5px rgba(0, 255, 255, 0.5));
}

.tech-text {
  font-size: 0.8em;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.about-section {
  margin-top: 40px;
  text-align: center;
  width: 100%;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
}

.about-content {
  max-width: 600px;
  margin: 0 auto;
}

.team-name {
  font-size: 2em;
  font-weight: bold;
  color: #00ffff;
  margin-bottom: 10px;
}

.project-info {
  font-size: 1.1em;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 20px;
}

.tech-highlights {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
}

.tech-item {
  text-align: center;
}

.tech-icon {
  font-size: 2em;
  color: #00ffff;
  margin-bottom: 5px;
}

.tech-text {
  font-size: 1.1em;
  color: rgba(255, 255, 255, 0.8);
}
</style>

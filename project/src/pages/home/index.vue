<template>
  <div class="home-cosmic-container" :class="{ 'eye-care-mode': isEyeCareMode }">
    <div id="stars"></div>
    <div id="stars2"></div>
    <div id="stars3"></div>
    <canvas ref="fortuneCanvas" class="fortune-canvas"></canvas>
    <div class="bubbles">
      <div v-for="n in 20" :key="n" class="bubble"></div>
    </div>
    <div v-if="isTransitioning" class="page-transition-overlay" :class="{ 'eye-care-mode': isEyeCareMode }"></div>

    <div class="cosmic-content-container">
      <div class="home-header">
        <div class="home-avatar" @click="goToPersonalCenter">
          <img :src="avatarSrc" alt="头像" @error="onAvatarError" />
        </div>
        <div class="home-title">
          <div class="title-row">
            <div class="title-left">
              <div class="title-main"><h1 class="title-text">欢迎回来</h1></div>
              <div class="title-name">
                <h1 class="username-highlight">{{ displayName }}</h1>
                <button class="fortune-icon" aria-label="抽签" @click.stop="openFortune">☄</button>
                <span v-if="cooldownLeftSec>0" class="fortune-cd">{{ cooldownLeftSec }}s后可抽</span>
              </div>
            </div>
            <div class="home-switch inline">
              <span class="eye-label">护眼模式</span>
              <el-switch v-model="localEyeCare" @change="toggleEyeCare" />
            </div>
          </div>
          <p class="subtitle">开启你的智能面试之旅</p>

        </div>
      </div>

      <div class="quick-actions">
        <div class="action-card action-feedback" @click="openFeedbackStatus">
          <svg class="card-svg" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="3" y="6" width="14" height="10" rx="3" stroke="currentColor" stroke-width="2"/>
            <path d="M6.5 9h7" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M6.5 12h5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <div class="card-text">
            <div class="card-title">反馈进程查询</div>
            <div class="card-sub">查看反馈处理状态与回复</div>
          </div>
          <span class="card-badge badge-feedback">反馈</span>
        </div>
        <div class="action-card action-career" @click="openInterviewJobs">
          <svg class="card-svg" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M4 9h16v8a3 3 0 0 1-3 3H7a3 3 0 0 1-3-3V9Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            <path d="M9 9V6h6v3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M12 13v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <div class="card-text">
            <div class="card-title">推荐求职网站</div>
            <div class="card-sub">Boss直聘</div>
          </div>
          <span class="card-badge badge-career">求职</span>
        </div>
        <div class="action-card action-history" @click="goToHistory">
          <svg class="card-svg" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M5 5h14v14H5z" stroke="currentColor" stroke-width="2"/>
            <path d="M8 14l3-3 3 2 2-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <div class="card-text">
            <div class="card-title">历史记录</div>
            <div class="card-sub">回看表现，持续优化提升</div>
          </div>
          <span class="card-badge badge-history">记录</span>
        </div>
        <div class="action-card action-profile" @click="goToPersonalCenter">
          <svg class="card-svg" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/>
            <path d="M4 20c1.8-3.2 5-5 8-5s6.2 1.8 8 5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <div class="card-text">
            <div class="card-title">个人中心</div>
            <div class="card-sub">查看资料与偏好设置</div>
          </div>
          <span class="card-badge badge-profile">账户</span>
        </div>
        <div class="action-card action-chat" @click="goToChatHall">
          <svg class="card-svg" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="3" y="6" width="14" height="10" rx="3" stroke="currentColor" stroke-width="2"/>
            <path d="M7 16 L5 18 L5 16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M6 9h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M6 12h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M6 14h4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <div class="card-text">
            <div class="card-title">聊天大厅</div>
            <div class="card-sub">全站交流与话题讨论</div>
          </div>
          <span class="card-badge badge-chat">聊天</span>
        </div>
        <div class="action-card action-rank" @click="goToLeaderboard">
          <svg class="card-svg" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="4" y="12" width="3" height="8" rx="1" stroke="currentColor" stroke-width="2"/>
            <rect x="10" y="9" width="3" height="11" rx="1" stroke="currentColor" stroke-width="2"/>
            <rect x="16" y="6" width="3" height="14" rx="1" stroke="currentColor" stroke-width="2"/>
            <circle cx="19" cy="4.5" r="2" stroke="currentColor" stroke-width="2"/>
            <path d="M18.2 6.6l.8 1.3.8-1.3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <div class="card-text">
            <div class="card-title">排行榜</div>
            <div class="card-sub">周榜月榜与总榜</div>
          </div>
          <span class="card-badge badge-rank">榜单</span>
        </div>
      </div>

      <div class="home-cta">
        <button class="cosmic-start-btn" @click="goToJobSelection">
          <span>开始你的面试之旅</span>
          <span class="btn-star">✦</span>
        </button>
      </div>

      <div class="moon-toggle" @click="toggleMoon" :class="{ 'eye-care': isEyeCareMode }" aria-label="Eye Mode Toggle">
        <svg v-if="!isEyeCareMode" class="moon-svg" viewBox="0 0 64 64">
          <defs>
            <radialGradient id="moonGold" cx="50%" cy="50%" r="50%">
              <stop offset="0%" stop-color="#fff6cc"/>
              <stop offset="100%" stop-color="#ffcc80"/>
            </radialGradient>
            <mask id="crescentCut">
              <rect width="64" height="64" fill="white"/>
              <circle cx="40" cy="28" r="24" fill="black"/>
            </mask>
          </defs>
          <circle cx="32" cy="32" r="26" fill="url(#moonGold)" mask="url(#crescentCut)"/>
        </svg>
        <svg v-else class="moon-svg" viewBox="0 0 64 64">
          <defs>
            <radialGradient id="moonSoft" cx="50%" cy="50%" r="50%">
              <stop offset="0%" stop-color="#fffbea"/>
              <stop offset="100%" stop-color="#ffd54f"/>
            </radialGradient>
          </defs>
          <circle cx="32" cy="32" r="26" fill="url(#moonSoft)"/>
        </svg>
      </div>
      <div v-if="blessLeft && fireActive" class="bless-caption bless-left"><span class="bless-text">{{ blessLeft }}</span></div>

  <div class="couplet-admin-fab" :class="{ disabled: !isAdmin }" @click="onCoupletClick" aria-label="Blessing Admin">
    <svg viewBox="0 0 64 64" class="couplet-svg">
          <defs>
            <linearGradient id="coupletRed" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#ff2d2d"/>
              <stop offset="100%" stop-color="#b60000"/>
            </linearGradient>
            <linearGradient id="coupletGold" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" stop-color="#ffd54f"/>
              <stop offset="100%" stop-color="#ff9100"/>
            </linearGradient>
          </defs>
          <rect x="18" y="6" width="28" height="9" rx="2" fill="url(#coupletRed)" stroke="url(#coupletGold)" stroke-width="2"/>
          <circle cx="32" cy="10.5" r="1.6" fill="#ffd54f"/>
          <path d="M12 13 H24 V50 L18 56 L12 50 Z" fill="url(#coupletRed)" stroke="url(#coupletGold)" stroke-width="2"/>
          <path d="M40 13 H52 V50 L46 56 L40 50 Z" fill="url(#coupletRed)" stroke="url(#coupletGold)" stroke-width="2"/>
        </svg>
      </div>
      <div v-if="blessRight && fireActive" class="bless-caption bless-right"><span class="bless-text">{{ blessRight }}</span></div>

      <div class="knot-fab" @click="triggerFireworks" aria-label="Chinese Knot">
        <svg class="knot-svg" viewBox="0 0 64 64">
          <defs>
            <linearGradient id="knotRed" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" stop-color="#ff4d4f"/>
              <stop offset="100%" stop-color="#d6001f"/>
            </linearGradient>
            <linearGradient id="knotGold" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" stop-color="#ffd54f"/>
              <stop offset="100%" stop-color="#ff9100"/>
            </linearGradient>
          </defs>
          <rect x="20" y="20" width="24" height="24" rx="4" fill="url(#knotRed)" stroke="url(#knotGold)" stroke-width="2" transform="rotate(45 32 32)"/>
          <circle cx="12" cy="32" r="4" fill="none" stroke="url(#knotGold)" stroke-width="2"/>
          <circle cx="52" cy="32" r="4" fill="none" stroke="url(#knotGold)" stroke-width="2"/>
          <circle cx="32" cy="12" r="4" fill="none" stroke="url(#knotGold)" stroke-width="2"/>
          <circle cx="32" cy="52" r="4" fill="none" stroke="url(#knotGold)" stroke-width="2"/>
          <path d="M20 32 L32 20 L44 32 L32 44 Z" fill="none" stroke="url(#knotGold)" stroke-width="2"/>
          <path d="M24 32 L32 24 L40 32 L32 40 Z" fill="none" stroke="url(#knotGold)" stroke-width="2"/>
          <circle cx="32" cy="32" r="3.2" fill="url(#knotGold)"/>
          <text x="32" y="36" text-anchor="middle" font-size="16" font-weight="700" fill="#ffd54f" font-family="" >福</text>
          <path d="M32 8 L32 16 M32 56 L32 48" stroke="url(#knotGold)" stroke-width="3" stroke-linecap="round"/>
          <path d="M28 56 L32 60 L36 56" stroke="url(#knotGold)" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span v-if="!showCloseHint" class="fab-tooltip">点击有惊喜</span>
        <span v-if="showCloseHint" class="fab-sticky-hint">再次点击关闭</span>
      </div>

      <div v-if="showNicknameModal" class="nickname-overlay">
        <div class="nickname-modal" :class="{ 'eye-care-mode': isEyeCareMode }">
          <div class="nickname-title">系统检测您未设置昵称</div>
          <div class="nickname-sub">请设置一个昵称以便在排行榜与聊天中展示</div>
          <div class="nickname-input-row">
            <input class="nickname-field" v-model="nicknameInput" placeholder="请输入昵称（只支持字母或汉字）" />
          </div>
          <div class="nickname-error" v-if="nicknameError">{{ nicknameError }}</div>
          <button class="nickname-confirm" @click="confirmNickname" :disabled="nicknameSubmitting">{{ nicknameSubmitting ? '设置中…' : '确定' }}</button>
        </div>
      </div>

      <div v-if="isAdmin" class="admin-feedback-fab" @click="openAdminFeedback">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M3 6h18v10a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3V6Z" stroke="currentColor" stroke-width="2"/>
          <path d="M7 10h10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <path d="M7 13h7" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span class="fab-tooltip">反馈处理</span>
      </div>

      <el-dialog v-model="feedbackStatusVisible" title="我的反馈进程" width="520px" align-center>
        <div class="feedback-status-list">
          <div v-if="loadingStatus" class="status-loading">加载中...</div>
          <div v-else>
            <div v-if="myStatuses.length===0" class="status-empty">暂无反馈处理进程</div>
            <div v-else class="status-items">
              <div class="status-item" v-for="it in myStatuses" :key="it.id" @click="openMyFeedbackDetail(it)">
                <div class="status-title">标题：{{ it.title }}</div>
                <div class="status-hint">点击查看处理详情</div>
                <span class="status-tag" :class="it.status.toLowerCase()">{{ statusText(it.status) }}</span>
                <div class="status-reply" v-if="it.reply">回复：{{ it.reply }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-dialog>

      <el-dialog v-model="adminFeedbackVisible" title="反馈处理" width="640px" align-center>
        <div class="admin-feedback-list">
          <div v-if="loadingAdmin" class="status-loading">加载中...</div>
          <div v-else>
            <div v-if="adminFeedback.length===0" class="status-empty">暂无用户反馈</div>
            <div v-else class="admin-items">
              <div class="admin-item" v-for="it in adminFeedback" :key="it.id" @click="selectFeedback(it)">
                <div class="admin-line">反馈人：{{ it.nickname || ('用户ID ' + (it.userId || '')) }}</div>
                <div class="admin-line">反馈类型：{{ it.type }}，反馈标题：{{ it.title }}</div>
                <div class="admin-line">反馈内容：{{ it.content }}</div>
                <span class="status-tag" :class="it.status.toLowerCase()">{{ statusText(it.status) }}</span>
              </div>
            </div>
            <div v-if="selectedFeedback" class="admin-detail">
              <div class="detail-user">{{ selectedFeedback.nickname || ('用户ID ' + (selectedFeedback.userId || '')) }}</div>
              <div class="detail-title">{{ selectedFeedback.title }}</div>
              <div class="detail-content">{{ selectedFeedback.content }}</div>
              <div class="detail-tags">
                <span class="tag type">{{ selectedFeedback.type }}</span>
                <span class="tag status" :class="(selectedFeedback.status||'').toLowerCase()">{{ statusText(selectedFeedback.status) }}</span>
                <span class="tag time">{{ formatTime(selectedFeedback.updatedAt || selectedFeedback.replyTime || selectedFeedback.createdAt) }}</span>
              </div>
              <div class="reply-row">
                <input class="reply-input" v-model="replyText" placeholder="输入回复内容" />
                <el-select v-model="replyStatus" class="reply-select" placeholder="状态">
                  <el-option label="已解决" value="RESOLVED" />
                  <el-option label="处理中" value="PENDING" />
                </el-select>
                <el-button class="reply-btn" type="primary" :loading="replying" @click="submitReply">回复</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-dialog>

      <el-dialog v-model="blessingAdminVisible" title="祝福语设置" width="640px" align-center>
        <div class="blessing-admin-wrap">
          <div class="blessing-create">
            <input class="bless-input" v-model="blessContent" placeholder="输入祝福语" />
            <el-select v-model="blessType" class="bless-type" placeholder="类型">
              <el-option label="通用" value="general" />
              <el-option label="节日" value="festival" />
            </el-select>
            <el-button type="primary" :loading="blessCreating" @click="createBlessing">新增</el-button>
          </div>
          <div class="blessing-list">
            <div v-for="b in blessings" :key="b.id" class="blessing-item">
              <div class="b-content">{{ b.content }}</div>
              <div class="b-meta">{{ b.type || '—' }} · {{ b.status }}</div>
              <div class="b-actions">
                <el-select v-model="b.status" @change="updateBlessingStatus(b)">
                  <el-option label="ACTIVE" value="ACTIVE" />
                  <el-option label="INACTIVE" value="INACTIVE" />
                </el-select>
                <el-button type="danger" size="small" @click="deleteBlessing(b)">删除</el-button>
              </div>
            </div>
            <div v-if="!blessings.length" class="status-empty">暂无祝福语</div>
          </div>
        </div>
      </el-dialog>

      <el-dialog v-model="myFeedbackDetailVisible" title="反馈详情" width="520px" align-center>
        <div v-if="myDetail" class="my-detail-simple">
          <div class="line">反馈内容：{{ myDetail.content || '—' }}</div>
          <div class="line">回复人：{{ myDetail.adminName || '—' }}</div>
          <div class="line">回复内容：{{ myDetail.reply || '暂无回复' }}</div>
        </div>
        <div v-else class="status-loading">加载中...</div>
      </el-dialog>

      <div class="home-carousel" :class="{ hiddenSurprise: fireActive || surpriseMode === 1 }">
        <el-carousel height="160px" indicator-position="outside" :interval="7000">
          <el-carousel-item>
            <div class="carousel-card">
              <div class="carousel-text">
                <div class="carousel-title">技巧提示</div>
                <ul class="tips-list">
                  <li class="tips-item">准备3-5个亮点项目，使用 STAR（情境-任务-行动-结果）结构，量化指标与个人贡献</li>
                  <li class="tips-item">依据岗位 JD 提炼必备技能，匹配一条最能体现该技能的项目案例与复盘</li>
                  <li class="tips-item">自我介绍控制在 60–90 秒，覆盖背景、核心能力、代表项目与优势亮点</li>
                </ul>
              </div>
            </div>
          </el-carousel-item>
          <el-carousel-item>
            <div class="carousel-card">
              <div class="carousel-text">
                <div class="carousel-title">效率提升</div>
                <ul class="tips-list">
                  <li class="tips-item">用首页入口直达历史记录与个人中心，快速定位上次薄弱题与场景</li>
                  <li class="tips-item">每次面试后写 3 点改进事项，并在下一次练习前逐项检视与复盘</li>
                  <li class="tips-item">针对薄弱点制定 1–2 个刻意练习目标，限定时间完成并记录成果</li>
                </ul>
              </div>
            </div>
          </el-carousel-item>
          <el-carousel-item>
            <div class="carousel-card">
              <div class="carousel-text">
                <div class="carousel-title">护眼模式</div>
                <ul class="tips-list">
                  <li class="tips-item">开启护眼模式与暗色背景，减少高亮刺激与长时间阅读疲劳</li>
                  <li class="tips-item">页面色彩与阴影已同步适配，确保对比度与可读性在不同光线下稳定</li>
                  <li class="tips-item">长时刷题或复盘建议搭配护眼模式，并每 45 分钟休息 5–10 分钟</li>
                </ul>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>


      <el-dialog v-model="fortuneVisible" title="今日运势" width="360px" align-center>
        <div class="fortune-result">
          <div class="fortune-title">{{ fortuneResult.label }}</div>
          <div class="fortune-desc">{{ fortuneResult.desc }}</div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import SmartIcon from '@/components/SmartIcon.vue'
import { API, getBlessings, createBlessing as apiCreateBlessing, updateBlessing as apiUpdateBlessing, deleteBlessing as apiDeleteBlessing } from '@/utils/api'
import { getUserSession, setUserSessionPatch, refreshUserSessionFromServer, subscribeUserSession, getSessionToken } from '@/utils/user-session'
import { applyAvatarFallback, resolveUserAvatar } from '@/utils/avatar'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

const { isEyeCareMode } = storeToRefs(userStore)
const localEyeCare = ref(isEyeCareMode.value)
const surpriseMode = ref(0)
const greetingText = ref('')
const blessLeft = ref('')
const blessRight = ref('')
const refreshBlessCaptions = async () => {
  try {
    const listResp = await getBlessings({ status: 'ACTIVE' })
    const dataList = Array.isArray(listResp?.data) ? listResp.data : (Array.isArray(listResp?.data?.data) ? listResp.data.data : [])
    if (Array.isArray(dataList) && dataList.length >= 2) {
      const i1 = Math.floor(Math.random() * dataList.length)
      let i2 = Math.floor(Math.random() * dataList.length)
      if (i2 === i1) i2 = (i1 + 1) % dataList.length
      blessLeft.value = String(dataList[i1]?.content || '').trim()
      blessRight.value = String(dataList[i2]?.content || '').trim()
    } else if (Array.isArray(dataList) && dataList.length === 1) {
      const c = String(dataList[0]?.content || '').trim()
      blessLeft.value = c
      blessRight.value = c
    } else {
      blessLeft.value = ''
      blessRight.value = ''
    }
  } catch {}
}
const pickGreeting = async () => {
  try {
    const resp = await getBlessings({ status: 'ACTIVE' })
    const list = Array.isArray(resp?.data) ? resp.data : (Array.isArray(resp?.data?.data) ? resp.data.data : [])
    if (list.length) {
      const i = Math.floor(Math.random()*list.length)
      const c = String(list[i]?.content || '').trim()
      if (c) { greetingText.value = c; return }
    }
  } catch {}
  greetingText.value = ''
}
const palettesFest = [
  ['#ff4d4f','#ff9100','#ffd54f','#ff85c0','#4a6fff','#7c4dff'],
  ['#ff9966','#ff5e62','#ffc371','#ffd54f','#ff9100','#ff6ec7'],
  ['#00e5ff','#4a6fff','#7c4dff','#ff6ec7','#ffd54f','#ff9100'],
  ['#ff6ec7','#ffd54f','#ff9100','#7c4dff','#4a6fff','#00e5ff']
]
const palettesEco = [
  ['#66bb6a','#8bc34a','#cddc39','#ffd54f','#ffb74d','#90c695'],
  ['#7fb069','#90c695','#cde6a5','#ffd54f','#ffb74d','#a8dba8'],
  ['#6fbf73','#8fd694','#cbe896','#ffe082','#ffca80','#9dd39d']
]
let currentPalette = []
const avatarSrc = ref(resolveUserAvatar(userStore.userInfo || userStore.getUserInfo))
let unsubscribeUserSession = null
const showNicknameModal = ref(false)
const nicknameInput = ref('')
const nicknameError = ref('')
const nicknameSubmitting = ref(false)

const displayName = computed(() => {
  const info = userStore.userInfo || userStore.getUserInfo
  const nick = info && info.nickname ? String(info.nickname).trim() : ''
  if (nick) return nick
  return '未命名用户'
})

const playKey = computed(() => {
  const info = userStore.userInfo || userStore.getUserInfo
  const id = (info && (info.nickname && String(info.nickname).trim())) ? String(info.nickname).trim() : 'guest'
  return 'funPlayAt:' + id
})
const COOLDOWN_MS = 10 * 1000
const getCooldownLeft = () => {
  const last = Number(localStorage.getItem(playKey.value) || '0')
  const now = Date.now()
  const left = COOLDOWN_MS - (now - last)
  return Math.max(0, left)
}
const cooldownLeftSec = ref(0)
let cooldownTimer = 0
const updateCooldown = () => {
  const left = getCooldownLeft()
  cooldownLeftSec.value = Math.ceil(left / 1000)
  if (cooldownLeftSec.value > 0 && !cooldownTimer) {
    cooldownTimer = setInterval(() => {
      const l = getCooldownLeft()
      cooldownLeftSec.value = Math.ceil(l / 1000)
      if (cooldownLeftSec.value <= 0) { clearInterval(cooldownTimer); cooldownTimer = 0 }
    }, 500)
  }
  if (cooldownLeftSec.value <= 0 && cooldownTimer) { clearInterval(cooldownTimer); cooldownTimer = 0 }
}
const ensureCanPlay = () => {
  const left = getCooldownLeft()
  if (left > 0) { updateCooldown(); return false }
  localStorage.setItem(playKey.value, String(Date.now()))
  updateCooldown()
  return true
}

const toggleEyeCare = async (val) => {
  try {
    isEyeCareMode.value = val
    localEyeCare.value = val
    await userStore.setEyeCareMode(val)
    if (fireActive.value) applyRandomPalette()
  } catch (e) {
    localEyeCare.value = isEyeCareMode.value
    uni.showToast({ title: '请先登录后再设置', icon: 'none' })
  }
}

const onAvatarError = (e) => {
  applyAvatarFallback(e, userStore.userInfo || userStore.getUserInfo || getUserSession())
}

const resolveCurrentAvatar = () => {
  const info = userStore.userInfo || userStore.getUserInfo || getUserSession()
  avatarSrc.value = resolveUserAvatar(info)
}

onMounted(async () => {
  resolveCurrentAvatar()
  const info = userStore.getUserInfo
  const nick = info && info.nickname ? String(info.nickname).trim() : ''
  if (!nick) { showNicknameModal.value = true; nicknameInput.value = '' }
  surpriseMode.value = Number(info?.surpriseMode ?? 0)
  try {
    const latest = await refreshUserSessionFromServer()
    avatarSrc.value = resolveUserAvatar(latest)
    if (latest && typeof latest.surpriseMode !== 'undefined') {
      surpriseMode.value = Number(latest.surpriseMode ?? 0)
    }
  } catch {}
  unsubscribeUserSession = subscribeUserSession((session) => {
    avatarSrc.value = resolveUserAvatar(session)
  })
  try {
    const unwatch = userStore.$subscribe(() => {
      resolveCurrentAvatar()
    })
    if (typeof unwatch === 'function') {
      const prev = unsubscribeUserSession
      unsubscribeUserSession = () => {
        if (prev) prev()
        unwatch()
      }
    }
  } catch {}
  await refreshBlessCaptions()
  nextTick(async () => {
    initFortuneCanvas()
    updateCooldown()
    if (surpriseMode.value === 1 && !fireActive.value) {
      applyRandomPalette()
      await refreshBlessCaptions()

      initFireCanvas()


      startFireworks()
      window.addEventListener('resize', onFireResize)
      showCloseHint.value = true
    }
  })
})

onUnmounted(() => {
  if (unsubscribeUserSession) unsubscribeUserSession()
  stopFortuneAnimation()
  window.removeEventListener('resize', handleResize)
  stopFireworks()
  window.removeEventListener('resize', onFireResize)
})

const fortuneCanvas = ref(null)
let fCtx = null
let fAnimId = 0
let fW = 0
let fH = 0
let t = 0

const initFortuneCanvas = () => {
  const el = fortuneCanvas.value
  if (!el || typeof el.getContext !== 'function') return
  fCtx = el.getContext('2d')
  handleResize()
  window.addEventListener('resize', handleResize)
  startFortuneAnimation()
}

const handleResize = () => {
  if (!fortuneCanvas.value || !fCtx) return
  const dpr = Math.max(1, window.devicePixelRatio || 1)
  fW = window.innerWidth
  fH = window.innerHeight
  fortuneCanvas.value.width = Math.floor(fW * dpr)
  fortuneCanvas.value.height = Math.floor(fH * dpr)
  fortuneCanvas.value.style.width = fW + 'px'
  fortuneCanvas.value.style.height = fH + 'px'
  fCtx.setTransform(dpr, 0, 0, dpr, 0, 0)
}

const startFortuneAnimation = () => {
  cancelAnimationFrame(fAnimId)
  if (!fCtx) return
  const tick = () => {
    fAnimId = requestAnimationFrame(tick)
    t += 0.016
    fCtx.clearRect(0, 0, fW, fH)
    drawAura()
  }
  fAnimId = requestAnimationFrame(tick)
}

const stopFortuneAnimation = () => {
  if (fAnimId) cancelAnimationFrame(fAnimId)
}

const drawAura = () => {
  const cx = fW * 0.5
  const cy = fH * 0.22
  for (let i = 0; i < 3; i++) {
    const r = 90 + i * 38 + Math.sin(t * (0.5 + i * 0.2)) * 12
    const g = fCtx.createRadialGradient(cx, cy, r * 0.2, cx, cy, r)
    g.addColorStop(0, 'rgba(124,77,255,0.20)')
    g.addColorStop(1, 'rgba(124,77,255,0.0)')
    fCtx.fillStyle = g
    fCtx.beginPath()
    fCtx.arc(cx, cy, r, 0, Math.PI * 2)
    fCtx.fill()
  }
}

const fireCanvas = ref(null)
let fwCtx = null
let fwId = 0
let fwW = 0
let fwH = 0
let fireActive = ref(false)
let rockets = []
let particles = []
let converge = false
let targets = []
let pColors = ['#ff4d4f','#ffd54f','#ff85c0','#ff9100','#ff6ec7']
const applyRandomPalette = () => { const pool = isEyeCareMode?.value ? palettesEco : palettesFest; const idx = Math.floor(Math.random()*pool.length); currentPalette = pool[idx] || []; pColors = (currentPalette && currentPalette.length ? currentPalette.slice() : pColors) }
const initFireCanvas = () => {
  let el = fireCanvas.value
  if (!el) {
    if (typeof document !== 'undefined') {
      el = document.createElement('canvas')
      el.className = 'firework-canvas'
      document.body.appendChild(el)
      fireCanvas.value = el
      el.style.position = 'fixed'
      el.style.left = '0'
      el.style.top = '0'
      el.style.right = '0'
      el.style.bottom = '0'
      el.style.pointerEvents = 'none'
    } else {
      return
    }
  }
  const dpr = Math.max(1, window.devicePixelRatio || 1)
  const wrap = el.parentElement
  if (wrap) {
    const rect = wrap.getBoundingClientRect()
    fwW = Math.max(320, rect.width)
    fwH = Math.max(240, rect.height)
  } else {
    fwW = window.innerWidth
    fwH = window.innerHeight
  }
  el.width = Math.floor(fwW * dpr)
  el.height = Math.floor(fwH * dpr)
  fwCtx = el.getContext('2d')
  fwCtx.setTransform(dpr,0,0,dpr,0,0)
}
const onFireResize = () => { if (fireActive.value) initFireCanvas() }
const startFireworks = () => {
  cancelAnimationFrame(fwId)
  fireActive.value = true
  rockets = []
  particles = []
  converge = false
  targets = []
  const spawn = () => {
    if (converge) return
    const x = Math.random()*fwW
    rockets.push({x, y: fwH+10, vx: (Math.random()-0.5)*1.2, vy: - (6+Math.random()*2), color: pColors[Math.floor(Math.random()*pColors.length)], life: 60+Math.random()*30})
  }
  let last = performance.now()
  const tick = (now) => {
    fwId = requestAnimationFrame(tick)
    const dt = Math.min(0.033, (now-last)/1000)
    last = now
    fwCtx.clearRect(0,0,fwW,fwH)
    fwCtx.globalCompositeOperation = 'lighter'
    for (let i=rockets.length-1;i>=0;i--) {
      const r = rockets[i]
      r.life -= 1
      r.x += r.vx
      r.y += r.vy
      r.vy += 0.06
      fwCtx.fillStyle = r.color
      fwCtx.beginPath(); fwCtx.arc(r.x, r.y, 2.2, 0, Math.PI*2); fwCtx.fill()
      if (r.vy > -1 || r.life<=0 || r.y < fwH*0.25) {
        explode(r)
        rockets.splice(i,1)
      }
    }
    for (let i=particles.length-1;i>=0;i--) {
      const p = particles[i]
      if (converge && p.tx!=null) {
        const dx = p.tx - p.x
        const dy = p.ty - p.y
        p.vx += dx*0.02
        p.vy += dy*0.02
        p.vx *= 0.92
        p.vy *= 0.92
      } else {
        p.vy += 0.08
        p.vx *= 0.99
        p.vy *= 0.99
      }
      p.x += p.vx
      p.y += p.vy
      p.life -= dt
      fwCtx.fillStyle = p.color
      fwCtx.beginPath(); fwCtx.arc(p.x, p.y, p.size, 0, Math.PI*2); fwCtx.fill()
      if (p.life<=0 && !converge) particles.splice(i,1)
    }
    if (!converge && particles.length<480) spawn()
    drawRibbon()
  }
  const startAt = performance.now()
  tick(startAt)
}
const explode = (r) => {
  const n = 36+Math.floor(Math.random()*24)
  for (let i=0;i<n;i++) {
    const a = (i/n)*Math.PI*2
    const sp = 2.4+Math.random()*2.6
    particles.push({x:r.x, y:r.y, vx: Math.cos(a)*sp+(Math.random()-0.5)*0.6, vy: Math.sin(a)*sp+(Math.random()-0.5)*0.6, size: 1.8+Math.random()*1.2, color: pColors[Math.floor(Math.random()*pColors.length)], life: 2.8+Math.random()*1.2})
  }
}
const computeTextTargets = () => {
  const off = document.createElement('canvas')
  off.width = Math.floor(fwW)
  off.height = Math.floor(fwH)
  const oc = off.getContext('2d')
  oc.clearRect(0,0,fwW,fwH)
  oc.fillStyle = '#fff'
  const fontFamily = '"HanziPen SC", "STKaiti", "KaiTi", "PingFang SC", "Microsoft YaHei", sans-serif'
  let fontSizePx = Math.round(Math.max(12, Math.min(42, fwW * 0.030)))
  oc.font = `600 ${fontSizePx}px ${fontFamily}`
  const text = String(greetingText.value||'').trim()
  if (text) {
    const len = text.length
    const baseScale = len > 4 ? Math.max(0.25, Math.pow(4/len, 0.90)) : 1
    fontSizePx = Math.round(fontSizePx * baseScale)
    oc.font = `600 ${fontSizePx}px ${fontFamily}`
    let maxW = fwW * Math.max(0.50, 0.70 - Math.min(0.18, len*0.014))
    while (oc.measureText(text).width > maxW && fontSizePx > 28) {
      fontSizePx -= 2
      oc.font = `600 ${fontSizePx}px ${fontFamily}`
    }
    oc.textAlign = 'center'
    oc.textBaseline = 'middle'
    const ratio = fwW / fwH
    let tyRel = ratio > 1.6 ? 0.40 : 0.37
    tyRel = Math.max(0.30, Math.min(0.50, tyRel))
    const txRel = ratio > 1.6 ? 0.33 : 0.31
    const tx = fwW * txRel
    const ty = fwH * tyRel
    oc.fillText(text, tx, ty)
  }
  const step = 8
  const img = oc.getImageData(0,0,fwW,fwH).data
  const pts = []
  for (let y=0;y<fwH;y+=step){
    for(let x=0;x<fwW;x+=step){
      const a = img[(y*fwW+x)*4+3]
      if(a>128) pts.push({x,y})
    }
  }
  return pts
}
const beginConverge = () => {
  targets = computeTextTargets()
  if (!targets || !targets.length) { converge = false; return }
  for (let i=0;i<particles.length;i++){
    const t = targets[i%targets.length]
    particles[i].tx = t.x
    particles[i].ty = t.y
    particles[i].life = 10
  }
  converge = true
}
let ribbonPhase = 0
let writeProgress = 0
const writeSpeed = 0.015
const drawRibbon = () => {}
const stopFireworks = () => {
  cancelAnimationFrame(fwId)
  fireActive.value = false
  if (fireCanvas.value) {
    const el = fireCanvas.value
    const ctx = fwCtx
    if (ctx) ctx.clearRect(0,0,fwW,fwH)
    if (el && el.parentElement) {
      el.parentElement.removeChild(el)
      fireCanvas.value = null
      fwCtx = null
    }
  }
  window.removeEventListener('resize', onFireResize)
  showCloseHint.value = false
  try {
    const token = getSessionToken()
    if (token) {
      request({ url: API.USER.SURPRISE_MODE, method: 'PUT', data: { surpriseMode: 0 } }).then(resp => {
        if (resp.code===200) {
          surpriseMode.value = Number(resp.data?.surpriseMode ?? 0)
        }
      }).catch(()=>{})
    }
  } catch {}
}

const stopFireworksLocal = () => {
  cancelAnimationFrame(fwId)
  fireActive.value = false
  if (fwCtx) fwCtx.clearRect(0,0,fwW,fwH)
  showCloseHint.value = false
}
const triggerFireworks = async () => {
  if (fireActive.value) { fireDialogVisible.value = false; stopFireworks(); return }
  const token = getSessionToken()
  if (!token) { uni.showToast({ title: '请先登录后开启惊喜', icon: 'none' }); return }
  try {
    const resp = await request({ url: API.USER.SURPRISE_MODE, method: 'PUT', data: { surpriseMode: 1 } })
    if (resp.code===200) {
      surpriseMode.value = Number(resp.data?.surpriseMode ?? 1)
    } else {
      uni.showToast({ title: resp.message || '开启失败', icon: 'none' })
      return
    }
  } catch (e) {
    uni.showToast({ title: '网络异常', icon: 'none' })
    return
  }
  fireDialogVisible.value = true
  showCloseHint.value = true
  nextTick(async () => { applyRandomPalette(); await refreshBlessCaptions(); initFireCanvas(); startFireworks(); window.addEventListener('resize', onFireResize) })
}

const fortuneVisible = ref(false)
const fortuneResult = ref({ label: '', desc: '' })
const fireDialogVisible = ref(false)
const showCloseHint = ref(false)
const fortunePool = [
  { label: '上上签', weight: 15, desc: '福星高照，诸事顺遂。事业上遇贵人提携，财运正偏财皆旺，感情和睦顺遂，家宅平安喜乐，是难得的全吉之兆' },
  { label: '上签', weight: 25, desc: '顺风顺水，稳步推进。事业按计划突破瓶颈，财运稳步增长，感情稳定升温，虽无惊喜但处处顺心，宜乘势而上' },
  { label: '中上签', weight: 22, desc: '有望突破，坚持不懈。事业出现转机但需付出努力，财运有机会但需主动把握，感情需多沟通化解小矛盾，坚持则能收获成果' },
  { label: '小吉', weight: 10, desc: '心态积极，稳中有升。事业无大波澜但小有进步，财运小吉小利，感情平淡温馨，保持积极心态则运势渐佳' },
  { label: '中签', weight: 8, desc: '平稳发展，按部就班。事业维持现状，财运平稳无波动，感情无大变化，宜守成不宜冒进，静待时机' },
  { label: '末吉', weight: 6, desc: '谨慎为宜，耐心等待。事业易遇小阻碍，财运需防破财，感情易有小误会，宜谨言慎行，不可急躁行事' },
  { label: '下签', weight: 3, desc: '避险蓄力，择时而动。事业易遇挫折，财运低迷，感情易生矛盾，宜收敛锋芒，低调行事，静待运势回转' },
  { label: '大吉签', weight: 5, desc: '鸿运当头，势不可挡。事业迎来重大机遇，财运暴涨可期，感情良缘天成，家宅兴旺，是名利双收的吉祥之兆' },
  { label: '中平签', weight: 3, desc: '吉凶参半，顺其自然。事业有得有失，财运时好时坏，感情平淡无波，宜随遇而安，不必强求' },
  { label: '中下签', weight: 2, desc: '波折暗藏，谨守本心。事业易遇小人作祟，财运易有损耗，感情易起隔阂，宜明辨是非，坚守原则' },
  { label: '下下签', weight: 1, desc: '时运不济，静待转机。事业阻碍重重，财运破败难免，感情易生裂痕，宜闭门反思，积累实力待时来运转' }
]

const openFortune = () => {
  if (!ensureCanPlay()) return
  const total = fortunePool.reduce((s, i) => s + i.weight, 0)
  let r = Math.random() * total
  let pick = fortunePool[0]
  for (const item of fortunePool) {
    if (r < item.weight) { pick = item; break } else r -= item.weight
  }
  fortuneResult.value = { label: pick.label, desc: pick.desc }
  fortuneVisible.value = true
}

const triggerFortune = () => {
  openFortune()
}

// 气韵预测模块已移除

const isTransitioning = ref(false)
const ensureSurpriseClosed = async () => {
  try {
    const token = getSessionToken()
    if (!token) return
    if (surpriseMode.value === 1 || fireActive.value) {
      stopFireworks()
      await request({ url: API.USER.SURPRISE_MODE, method: 'PUT', data: { surpriseMode: 0 } })
      fireActive.value = false
      surpriseMode.value = 0
    }
  } catch {}
}
const smoothPush = async (url) => {
  isTransitioning.value = true
  await ensureSurpriseClosed()
  setTimeout(() => {
    router.push(url)
  }, 180)
}
const goToJobSelection = () => smoothPush('/pages/job-selection/index')
const goToHistory = () => smoothPush('/pages/history/index')
const goToPersonalCenter = () => smoothPush('/pages/personal-center/index')
const openAlgorithmPractice = () => {
  if (typeof window !== 'undefined') window.open('https://codetop.cc/login', '_blank')
}
const openInterviewJobs = () => {
  if (typeof window !== 'undefined') window.open('https://www.bosszhipin.com/', '_blank')
}
const goToChatHall = () => smoothPush('/pages/chat-hall/index')
const goToLeaderboard = () => smoothPush('/pages/leaderboard/index')
const toggleMoon = () => { toggleEyeCare(!isEyeCareMode.value) }

const isAdmin = computed(() => {
  const info = userStore.userInfo || userStore.getUserInfo
  const ut = info && (info.userType ?? info.user_type ?? info.type)
  return ut === 3 || ut === '3'
})

const feedbackStatusVisible = ref(false)
const myStatuses = ref([])
const loadingStatus = ref(false)
const formatTime = (s) => {
  if (!s) return ''
  try { const d = new Date(s); return d.toLocaleString('zh-CN', { hour12: false }) } catch { return s }
}
const statusText = (s) => {
  const k = String(s || '').toUpperCase()
  if (k === 'PENDING' || k === 'PROCESSING') return '处理中'
  if (k === 'RESOLVED') return '已解决'
  return s
}
const parseList = (resp) => {
  try {
    const d = resp?.data
    if (Array.isArray(d)) return d
    if (Array.isArray(d?.data)) return d.data
    return []
  } catch { return [] }
}
const parseObject = (resp) => {
  try {
    const d = resp?.data
    if (d?.data && !Array.isArray(d.data) && typeof d.data === 'object') return d.data
    if (d && !Array.isArray(d) && typeof d === 'object') return d
    return null
  } catch { return null }
}
const openFeedbackStatus = async () => {
  feedbackStatusVisible.value = true
  loadingStatus.value = true
  try {
    const token = uni.getStorageSync('token')
    if (!token) { uni.showToast({ title: '请先登录后查看', icon: 'none' }); return }
    const resp = await request({ url: API.FEEDBACK.MY_STATUS, method: 'GET' })
    const arr = parseList(resp)
    myStatuses.value = arr.slice().sort((a, b) => {
      const ta = Date.parse(a.updatedAt || a.replyTime || a.createdAt || 0)
      const tb = Date.parse(b.updatedAt || b.replyTime || b.createdAt || 0)
      return tb - ta
    })
  } finally { loadingStatus.value = false }
}

const adminFeedbackVisible = ref(false)
const adminFeedback = ref([])
const selectedFeedback = ref(null)
const loadingAdmin = ref(false)
const replyText = ref('')
const replyStatus = ref('RESOLVED')
const replying = ref(false)
const blessingAdminVisible = ref(false)
const blessings = ref([])
const blessContent = ref('')
const blessType = ref('general')
const blessCreating = ref(false)
const openBlessingAdmin = async () => {
  blessingAdminVisible.value = true
  await loadBlessings()
}
const onCoupletClick = () => {
  if (!isAdmin.value) { return }
  openBlessingAdmin()
}
const loadBlessings = async () => {
  try {
    const resp = await getBlessings({ status: 'ACTIVE' })
    const arr = Array.isArray(resp?.data) ? resp.data : (Array.isArray(resp?.data?.data) ? resp.data.data : [])
    blessings.value = arr
  } catch {}
}
const createBlessing = async () => {
  if (!String(blessContent.value||'').trim()) { uni.showToast({ title: '请输入祝福语', icon: 'none' }); return }
  try {
    blessCreating.value = true
    const resp = await apiCreateBlessing({ content: String(blessContent.value).trim(), type: blessType.value })
    if (resp?.code===200) { blessContent.value=''; await loadBlessings(); uni.showToast({ title: '创建成功', icon: 'success' }) }
  } finally { blessCreating.value = false }
}
const updateBlessingStatus = async (b) => { try { await apiUpdateBlessing(b.id, { status: b.status }) } catch {} }
const deleteBlessing = async (b) => { try { const resp = await apiDeleteBlessing(b.id); if (resp?.code===200) { blessings.value = blessings.value.filter(it => it.id!==b.id); uni.showToast({ title: '删除成功', icon: 'success' }) } } catch {} }
const openAdminFeedback = async () => {
  adminFeedbackVisible.value = true
  loadingAdmin.value = true
  try {
    const token = uni.getStorageSync('token')
    if (!token) { uni.showToast({ title: '请先登录后查看', icon: 'none' }); return }
    if (!isAdmin.value) { uni.showToast({ title: '仅管理员可用', icon: 'none' }); return }
    const resp = await request({ url: API.FEEDBACK.LIST, method: 'GET' })
    const arr = parseList(resp)
    adminFeedback.value = arr.slice().sort((a, b) => {
      const ta = Date.parse(a.updatedAt || a.createdAt || 0)
      const tb = Date.parse(b.updatedAt || b.createdAt || 0)
      return tb - ta
    })
  } finally { loadingAdmin.value = false }
}
const selectFeedback = async (it) => {
  selectedFeedback.value = it
  try {
    const token = uni.getStorageSync('token')
    if (!token) return
    const resp = await request({ url: API.FEEDBACK.DETAIL(it.id), method: 'GET' })
    const obj = parseObject(resp)
    if (obj) selectedFeedback.value = obj
  } catch {}
}
const submitReply = async () => {
  if (!selectedFeedback.value) return
  try {
    replying.value = true
    const token = uni.getStorageSync('token')
    if (!token) return
    const resp = await request({ url: API.FEEDBACK.REPLY(selectedFeedback.value.id), method: 'POST', data: { id: selectedFeedback.value.id, reply: replyText.value, status: replyStatus.value } })
    const updated = parseObject(resp)
    if (updated) {
      uni.showToast({ title: '回复成功', icon: 'success' })
      replyText.value = ''
      selectedFeedback.value = updated
      adminFeedback.value = (adminFeedback.value || []).map(it => it.id === updated.id ? { ...it, status: updated.status, reply: updated.reply, updatedAt: updated.updatedAt } : it)
    } else {
      const msg = resp?.message || '回复失败'
      uni.showToast({ title: msg, icon: 'none' })
    }
  } finally { replying.value = false }
}

const myFeedbackDetailVisible = ref(false)
const myDetail = ref(null)
const openMyFeedbackDetail = async (it) => {
  myFeedbackDetailVisible.value = true
  myDetail.value = null
  try {
    const token = uni.getStorageSync('token')
    if (!token) return
    const resp = await request({ url: API.FEEDBACK.DETAIL(it.id), method: 'GET' })
    const obj = parseObject(resp)
    if (obj) myDetail.value = obj
  } catch {}
}

const validateNickname = (val) => {
  const v = String(val || '').trim()
  if (v.length < 1 || v.length > 6) { nicknameError.value = '昵称长度需为1-6字符'; return false }
  if (!/^[\u4e00-\u9fa5a-zA-Z]+$/.test(v)) { nicknameError.value = '仅允许字母或汉字'; return false }
  nicknameError.value = ''
  return true
}

const confirmNickname = async () => {
  const ok = validateNickname(nicknameInput.value)
  if (!ok) return
  try {
    nicknameSubmitting.value = true
    const token = uni.getStorageSync('token')
    if (!token) { uni.showToast({ title: '请先登录', icon: 'none' }); return }
    const resp = await request({ url: API.USER.NICKNAME, method: 'PUT', data: { nickname: String(nicknameInput.value).trim() } })
    if (resp.code === 200) {
      const updated = setUserSessionPatch({ nickname: String(nicknameInput.value).trim() })
      try { userStore.setUserInfo(updated) } catch {}
      showNicknameModal.value = false
      uni.showToast({ title: '昵称设置成功', icon: 'success', duration: 1200 })
    } else {
      const msg = resp.message || '设置失败'
      nicknameError.value = msg
    }
  } finally {
    nicknameSubmitting.value = false
  }
}
</script>

<style scoped lang="scss">
@use 'sass:math';
@use 'sass:string';
.home-cosmic-container {
  min-height: 100vh;
  width: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  box-sizing: border-box;
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 24px 20px 32px;
  background: linear-gradient(135deg, #0a1929 0%, #1a1f35 100%);
  transition: background 0.4s;
}
.home-cosmic-container.eye-care-mode {
  background: linear-gradient(135deg, #1c2a1f 0%, #2a3c2e 100%);
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
  top: 0; left: 0; right: 0; bottom: 0;
  display: block;
  z-index: 0;
  background: transparent;
  pointer-events: none;
}
#stars { width: 1px; height: 1px; box-shadow: $shadows-small; animation: animStar 150s linear infinite; }
#stars2 { width: 2px; height: 2px; box-shadow: $shadows-medium; animation: animStar 100s linear infinite; }
#stars3 { width: 3px; height: 3px; box-shadow: $shadows-large; animation: animStar 50s linear infinite; }
@keyframes animStar { from { transform: translateY(0px); } to { transform: translateY(-2560px); } }
.fortune-canvas { position: fixed; inset: 0; z-index: 1; background: transparent; pointer-events: none; }
.firework-canvas { position: fixed; inset: 0; z-index: 5; background: transparent; pointer-events: none; }
.fireworks-dialog :deep(.el-dialog__header) { background: linear-gradient(90deg,#d6001f 0%, #ff9100 100%); color: #fff; border-radius: 12px 12px 0 0; }
.fireworks-dialog :deep(.el-dialog__body) { background: radial-gradient(circle at 30% 20%, rgba(255,77,79,0.18), rgba(255,145,0,0.12)); }
.fireworks-wrap { padding: 6px; border-radius: 12px; border: 1px solid rgba(255,145,0,0.35); box-shadow: 0 8px 26px rgba(255,145,0,0.28); }
.fireworks-wrap .firework-canvas { position: relative; width: 100%; height: 420px; display:block; }
.fire-desc { margin-top: 8px; text-align: center; color: #ffd54f; font-weight: 600; }
.bubbles { position: absolute; inset: 0; z-index: 2; pointer-events: none; }
.page-transition-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  pointer-events: none;
  background: radial-gradient(circle at 50% 50%, rgba(124,77,255,0.18), rgba(0,0,0,0.55));
  animation: fadeOutSoft 220ms ease forwards;
}
@keyframes fadeOutSoft {
  from { opacity: 0; }
  to { opacity: 1; }
}
.home-cosmic-container.eye-care-mode .page-transition-overlay {
  background: radial-gradient(circle at 50% 50%, rgba(127,176,105,0.18), rgba(0,0,0,0.40));
}

.cosmic-content-container {
  position: relative;
  z-index: 3;
  width: min(100%, 960px);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.home-header {
  display: grid;
  grid-template-columns: 72px 1fr;
  align-items: center;
  gap: 16px;
}
.home-switch { display: flex; align-items: center; gap: 8px; margin-top: 12px; }
.home-switch.inline { margin-top: 0; }
.eye-label { color: #4A6FFF; font-weight: 600; }
.home-avatar img {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: 2px solid #7c4dff;
  box-shadow: 0 0 12px 2px #4a6fff55, 0 0 0 4px #fff0;
  object-fit: cover;
  background: #222;
}
.title-row { display: flex; align-items: center; justify-content: space-between; gap: 10px; height: 2.6rem; }
.title-left { display: flex; align-items: center; gap: 10px; }
.title-main, .title-name { display: flex; align-items: center; height: 100%; }
.title-text, .username-highlight { font-size: 2rem; margin: 0; display: flex; align-items: center; height: 100%; line-height: 1; }
.home-title .title-text {
  background: linear-gradient(90deg, #7c4dff 10%, #00e5ff 90%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 18px #7c4dff44, 0 0 8px #00e5ff33;
}
.home-title .subtitle {
  margin-top: 4px;
  color: #a8c6ff;
}
.username-highlight { font-family: 'STKaiti','KaiTi','cursive','PingFang SC',sans-serif; color: #eaf2ff; }

.quick-actions { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }

.action-card {
  background: rgba(20, 24, 40, 0.92);
  border-radius: 24px;
  box-shadow: 0 8px 40px 0 #7c4dff22, 0 1.5px 6px #00e5ff11;
  border: 2px solid rgba(124,77,255,0.18);
  backdrop-filter: blur(16px);
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: box-shadow 0.3s, transform 0.2s, border 0.3s;
  position: relative;
}
.action-card:hover { box-shadow: 0 16px 60px 0 #7c4dff44, 0 2px 12px #00e5ff22; transform: translateY(-2px); border-color: #7c4dff88; }
.action-card .card-text { display: flex; flex-direction: column; }
.action-card .card-title { color: #eaf2ff; font-weight: 600; }
.action-card .card-sub { color: #a8c6ff; font-size: 0.9rem; }
.card-svg { color: #eaf2ff; filter: drop-shadow(0 0 6px #7c4dff55); }

.card-badge { position: absolute; right: 14px; top: 14px; padding: 2px 10px; border-radius: 999px; font-size: 12px; color: #fff; backdrop-filter: blur(4px); }
.badge-alg { background: linear-gradient(90deg,#00f5ff 0%,#4a6fff 100%); }
.badge-feedback { background: linear-gradient(90deg,#4A6FFF 0%,#7c4dff 100%); }
.badge-career { background: linear-gradient(90deg,#00e5ff 0%,#14c5b4 100%); }
.badge-history { background: linear-gradient(90deg,#ff6ec7 0%,#a74cff 100%); }
.badge-profile { background: linear-gradient(90deg,#7fb069 0%,#90c695 100%); }
.badge-chat { background: linear-gradient(90deg,#4a6fff 0%,#7c4dff 100%); }
.badge-rank { background: linear-gradient(90deg,#ffd54f 0%,#ff9100 100%); }

.action-feedback { border-color: rgba(70,90,255,0.50); box-shadow: 0 8px 40px 0 rgba(70,90,255,0.28), 0 1.5px 6px rgba(124,77,255,0.20); }
.action-feedback:hover { box-shadow: 0 18px 60px 0 rgba(70,90,255,0.42), 0 4px 18px rgba(124,77,255,0.30); }

.admin-feedback-fab { position: fixed; right: 22px; bottom: 22px; z-index: 10; width: 56px; height: 56px; border-radius: 999px; background: rgba(20,24,40,0.92); border: 2px solid rgba(124,77,255,0.35); display:flex; align-items:center; justify-content:center; box-shadow: 0 8px 26px rgba(124,77,255,0.28); cursor: pointer; }
.admin-feedback-fab svg { color: #ffd54f; filter: drop-shadow(0 0 8px #ff910055); }
.admin-feedback-fab:hover { transform: translateY(-2px); box-shadow: 0 12px 32px rgba(124,77,255,0.35); }
.admin-feedback-fab .fab-tooltip { position: absolute; bottom: 64px; right: 0; transform: translateY(6px); background: rgba(20,24,40,0.92); color: #eaf2ff; border: 1px solid rgba(124,77,255,0.35); border-radius: 8px; padding: 6px 10px; font-size: 12px; white-space: nowrap; opacity: 0; pointer-events: none; transition: opacity .2s, transform .2s; }
.admin-feedback-fab:hover .fab-tooltip { opacity: 1; transform: translateY(0); }

.moon-toggle { position: fixed; left: 20px; top: 20px; z-index: 10; width: 44px; height: 44px; display:flex; align-items:center; justify-content:center; background: transparent; border: none; box-shadow: none; backdrop-filter: none; cursor: pointer; }
.moon-toggle.eye-care { background: transparent; border: none; box-shadow: none; }
.moon-svg { width: 100%; height: 100%; filter: none; }

.couplet-admin-fab { position: fixed; right: 22px; top: 22px; z-index: 10; width: 52px; height: 52px; display:flex; align-items:center; justify-content:center; background: transparent; border: none; box-shadow: none; backdrop-filter: none; cursor: pointer; }
.couplet-admin-fab:hover { transform: translateY(-1px); }
.couplet-svg { width: 86%; height: 86%; }
.couplet-admin-fab.disabled { opacity: 0.9; cursor: default; pointer-events: none; }
.couplet-admin-fab.disabled:hover { transform: none; }

.bless-caption { position: fixed; z-index: 9; padding: 6px 10px; border-radius: 12px; font-weight: 700; font-size: 14px; color: #ffd54f; background: rgba(20,24,40,0.62); border: 1px solid rgba(255,213,79,0.45); box-shadow: 0 6px 16px rgba(255,213,79,0.25); backdrop-filter: blur(6px); max-width: 42vw; text-overflow: ellipsis; overflow: hidden; white-space: nowrap; }
.bless-left { left: 20px; top: 72px; }
.bless-right { right: 22px; top: 76px; }
.home-cosmic-container.eye-care-mode .bless-caption { color: #fffbea; background: rgba(127,176,105,0.28); border-color: rgba(127,176,105,0.45); box-shadow: 0 6px 16px rgba(127,176,105,0.25); }
.bless-text { display: inline-block; vertical-align: middle; }

.blessing-admin-wrap { display:flex; flex-direction: column; gap: 12px; }
.blessing-create { display:flex; gap:8px; align-items:center; }
.bless-input { flex:1; height:36px; border-radius:10px; border:1px solid #ffb74d; background: #ffffff; color:#1a1f35; padding:0 12px; caret-color:#ff9100; }
.bless-input::placeholder { color: #6b7890; opacity: 1; }
.bless-input:focus { outline: none; box-shadow: 0 0 0 3px rgba(255,190,0,0.35); border-color: #ff9100; background: #ffffff; }
.bless-type :deep(.el-input__wrapper) { background: #ffffff; box-shadow: none; border: 1px solid #ffb74d; }
.bless-type :deep(.el-input__inner) { color: #1a1f35; }
.bless-type { width:160px; }
.blessing-list { display:flex; flex-direction:column; gap:8px; max-height: 360px; overflow:auto; }
.blessing-item { display:grid; grid-template-columns: 1fr auto auto; gap:10px; align-items:center; background: rgba(255,190,0,0.08); border:1px solid rgba(255,190,0,0.35); border-radius:12px; padding:8px 10px; }
.b-content { color:#1a1f35; font-weight:700; }
.b-meta { color:#8b6b20; font-size:12px; }
.status-empty { color:#6b7890; font-weight:600; text-align:center; }
.b-actions { display:flex; gap:8px; align-items:center; }
.home-cosmic-container.eye-care-mode .couplet-admin-fab { background: transparent; border-color: transparent; box-shadow: none; }
.home-cosmic-container.eye-care-mode .blessing-item { background: rgba(127,176,105,0.12); border-color: rgba(127,176,105,0.35); }
.home-cosmic-container.eye-care-mode .bless-input { border-color: rgba(127,176,105,0.65); background: rgba(255,255,255,0.18); color:#153023; caret-color:#7fb069; }
.home-cosmic-container.eye-care-mode .bless-input::placeholder { color:#396247; opacity: 0.95; }
.home-cosmic-container.eye-care-mode .b-content { color:#153023; }
.home-cosmic-container.eye-care-mode .b-meta { color:#396247; }

.knot-fab { position: fixed; left: 22px; bottom: 22px; z-index: 6; width: 56px; height: 56px; border-radius: 999px; backdrop-filter: blur(6px); background: rgba(20,24,40,0.72); border: 2px solid rgba(255,145,0,0.45); display: flex; align-items: center; justify-content: center; box-shadow: 0 8px 26px rgba(255,145,0,0.33); cursor: pointer; }
.knot-fab:hover { transform: translateY(-2px); box-shadow: 0 12px 32px rgba(255,145,0,0.35); }
.knot-svg { width: 76%; height: 76%; }
.knot-fab .fab-tooltip { position: absolute; bottom: 64px; left: 0; transform: translateY(6px); background: rgba(20,24,40,0.92); color: #ffd54f; border: 1px solid rgba(255,145,0,0.35); border-radius: 8px; padding: 6px 10px; font-size: 12px; white-space: nowrap; opacity: 0; pointer-events: none; transition: opacity .2s, transform .2s; }
.knot-fab:hover .fab-tooltip { opacity: 1; transform: translateY(0); }
.knot-fab .fab-sticky-hint { position: absolute; bottom: 64px; left: 0; background: rgba(20,24,40,0.92); color: #ffd54f; border: 1px solid rgba(255,145,0,0.45); border-radius: 8px; padding: 6px 10px; font-size: 12px; white-space: nowrap; box-shadow: 0 8px 18px rgba(255,145,0,0.28); }
.home-cosmic-container.eye-care-mode .knot-fab { background: rgba(144,198,149,0.18); border-color: rgba(127,176,105,0.45); box-shadow: 0 8px 26px rgba(127,176,105,0.28); }
.home-cosmic-container.eye-care-mode .knot-fab .fab-tooltip { background: rgba(127,176,105,0.35); color: #fffbea; border-color: rgba(127,176,105,0.55); text-shadow: 0 0 2px rgba(0,0,0,0.25); font-weight: 700; }
.home-cosmic-container.eye-care-mode .knot-fab .fab-sticky-hint { background: rgba(127,176,105,0.35); color: #fffbea; border-color: rgba(127,176,105,0.55); text-shadow: 0 0 2px rgba(0,0,0,0.25); font-weight: 700; }
.home-cosmic-container.eye-care-mode .knot-svg text { fill: #7fb069; }

.feedback-status-list { min-height: 180px; }
.status-loading, .status-empty { color: #a8c6ff; text-align: center; padding: 12px; }
.status-items { display: flex; flex-direction: column; gap: 10px; }
.status-item { position: relative; background: linear-gradient(180deg, rgba(20,24,40,0.86), rgba(20,24,40,0.72)); border: 1px solid rgba(124,77,255,0.25); border-radius: 12px; padding: 12px 14px; box-shadow: 0 6px 18px rgba(0,0,0,0.22); cursor: pointer; transition: transform .18s ease, box-shadow .24s ease, border-color .24s ease; }
.status-item:hover { transform: translateY(-2px); box-shadow: 0 12px 26px rgba(124,77,255,0.28); border-color: rgba(124,77,255,0.40); }
.status-title { color: #eaf2ff; font-weight: 600; }
.status-hint { color: #a8c6ff; font-size: 12px; margin-top: 4px; }
.status-tag { position: absolute; right: 12px; top: 10px; padding: 2px 10px; border-radius: 999px; background: rgba(124,77,255,0.18); color:#eaf2ff; font-size: 12px; }
.status-tag.resolved { background: rgba(127,176,105,0.25); }
.status-tag.pending, .status-tag.processing { background: rgba(255,145,0,0.25); }
.status-reply { color: #eaf2ff; margin-top: 6px; }

.admin-items { display:flex; flex-direction: column; gap:8px; }
.admin-item { position: relative; background: linear-gradient(180deg, rgba(20,24,40,0.86), rgba(20,24,40,0.72)); border: 1px solid rgba(124,77,255,0.25); border-radius: 12px; padding: 12px 14px; box-shadow: 0 6px 18px rgba(0,0,0,0.22); cursor: pointer; transition: transform .18s ease, box-shadow .24s ease, border-color .24s ease; }
.admin-item:hover { transform: translateY(-2px); box-shadow: 0 12px 26px rgba(124,77,255,0.28); border-color: rgba(124,77,255,0.40); }
.admin-line { color: #eaf2ff; font-size: 13px; }
.admin-detail { margin-top: 12px; background: linear-gradient(180deg, rgba(20,24,40,0.86), rgba(20,24,40,0.72)); border: 1px solid rgba(124,77,255,0.25); border-radius: 16px; padding: 14px; box-shadow: 0 6px 18px rgba(0,0,0,0.22); }
.detail-user { color:#ffd54f; font-weight:700; margin-bottom:6px; }
.detail-title { color:#eaf2ff; font-weight:700; }
.detail-content { color:#a8c6ff; margin-top:6px; }
.detail-tags { display:flex; gap:8px; margin-top:8px; align-items:center; }
.detail-tags .tag { padding: 2px 8px; border-radius: 999px; font-size: 12px; color:#eaf2ff; border: 1px solid rgba(124,77,255,0.25); background: rgba(124,77,255,0.12); }
.detail-tags .tag.status.resolved { background: rgba(127,176,105,0.22); border-color: rgba(127,176,105,0.35); }
.detail-tags .tag.status.pending, .detail-tags .tag.status.processing { background: rgba(255,145,0,0.20); border-color: rgba(255,145,0,0.35); }
.reply-row { display:flex; gap:8px; margin-top:10px; align-items:center; }
.reply-input { flex:1; height:42px; border-radius:10px; border:1px solid rgba(124,77,255,0.35); background: rgba(255,255,255,0.06); color:#eaf2ff; padding:0 10px; box-sizing:border-box; line-height:42px; }
.reply-select { width: 160px; --el-input-height: 42px; }
.reply-select :deep(.el-input__wrapper) { min-height: 42px; height: 42px; }
.reply-select :deep(.el-input__inner) { height: 42px; }
.reply-btn { height: 42px; padding: 0 16px; }
.reply-btn { height: 42px; }
.my-detail-simple { display:flex; flex-direction:column; gap:8px; }
.my-detail-simple .line { color:#1a1f35; }
.home-cosmic-container.eye-care-mode .my-detail-simple .line { color:#153023; }
.action-career { border-color: rgba(0,229,255,0.35); box-shadow: 0 8px 40px 0 #00e5ff22, 0 1.5px 6px #14c5b422; }
.action-career:hover { box-shadow: 0 18px 60px 0 #00e5ff44, 0 4px 18px #14c5b433; }
.action-history { border-color: rgba(255,110,199,0.50); box-shadow: 0 8px 40px 0 rgba(255,110,199,0.28), 0 1.5px 6px rgba(255,145,0,0.20); }
.action-history:hover { box-shadow: 0 18px 60px 0 rgba(255,110,199,0.42), 0 4px 18px rgba(255,145,0,0.30); }
.action-profile { border-color: rgba(127,176,105,0.35); box-shadow: 0 8px 40px 0 #7fb06933, 0 1.5px 6px #90c69522; }
.action-profile:hover { box-shadow: 0 18px 60px 0 #7fb06955, 0 4px 18px #90c69544; }
.action-chat { border-color: rgba(0,229,255,0.50); box-shadow: 0 8px 40px 0 rgba(0,229,255,0.28), 0 1.5px 6px rgba(20,197,180,0.20); }
.action-chat:hover { box-shadow: 0 18px 60px 0 rgba(0,229,255,0.42), 0 4px 18px rgba(20,197,180,0.30); }
.action-rank { border-color: rgba(255,145,0,0.35); box-shadow: 0 8px 40px 0 #ffd54f33, 0 1.5px 6px #ff910033; }
.action-rank:hover { box-shadow: 0 18px 60px 0 #ffd54f55, 0 4px 18px #ff910055; }

.home-cta { display: flex; justify-content: center; }
.cosmic-start-btn {
  padding: 12px 48px;
  font-size: 1.05rem;
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
.cosmic-start-btn::before { content: ''; position: absolute; left: -60%; top: 0; width: 60%; height: 100%; background: linear-gradient(120deg, rgba(255,255,255,0.18) 0%, rgba(255,255,255,0.04) 100%); filter: blur(2px); opacity: 0; transition: opacity 0.3s, left 0.5s; pointer-events: none; }
.cosmic-start-btn:hover { box-shadow: 0 0 24px 4px #7c4dff88, 0 12px 32px #00e5ff33; transform: translateY(-2px); }
.cosmic-start-btn:hover::before { left: 110%; opacity: 1; transition: opacity 0.3s, left 0.5s; }
.btn-star { color: #00e5ff; font-size: 1.2em; text-shadow: 0 0 8px #00e5ff99; }

.home-carousel { margin-top: 0; }
.home-carousel.hiddenSurprise { visibility: hidden; pointer-events: none; }
.carousel-card { display: flex; align-items: center; justify-content: center; gap: 12px; height: 100%; padding: 12px; }
.carousel-text .carousel-title { color: #eaf2ff; font-weight: 600; }
.carousel-text { text-align: center; max-width: 720px; margin: 0 auto; }
.tips-list { margin: 6px 0 0 0; padding: 0; list-style: none; }
.tips-item { color: #a8c6ff; font-size: 0.9rem; line-height: 1.5; }

.fortune-icon { background: transparent; border: none; outline: none; appearance: none; box-shadow: none; color: #eaf2ff; opacity: 0.9; font-size: 22px; font-weight: 700; line-height: 1; margin-left: 8px; padding: 0; cursor: pointer; mix-blend-mode: screen; }
.fortune-icon:hover { color: #ffffff; opacity: 1; }
.fortune-cd { margin-left: 8px; color: #a8c6ff; font-size: 0.85rem; }
.fortune-result { display: flex; flex-direction: column; align-items: center; padding: 12px 6px; gap: 6px; }
.fortune-title { font-size: 1.4rem; font-weight: 700; color: #7c4dff; }
.fortune-desc { color: #666; }
/* 气韵预测样式已移除 */

.nickname-overlay { position: fixed; inset: 0; z-index: 10000; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(6px); background: radial-gradient(circle at 50% 50%, rgba(0,0,0,0.55), rgba(0,0,0,0.35)); }
.nickname-modal { width: 420px; max-width: 92vw; border-radius: 20px; padding: 20px 18px; background: linear-gradient(135deg, rgba(20,24,40,0.96) 0%, rgba(28,32,58,0.96) 100%); border: 1px solid rgba(124,77,255,0.35); box-shadow: 0 18px 60px rgba(124,77,255,0.25), 0 8px 24px rgba(0,229,255,0.15); display: flex; flex-direction: column; gap: 12px; }
.nickname-title { font-size: 1.3rem; font-weight: 700; color: #ffd54f; text-shadow: 0 0 10px #ff910055; text-align: center; }
.nickname-sub { color: #a8c6ff; font-size: 0.95rem; text-align: center; }
.nickname-input-row { display: flex; align-items: center; justify-content: center; }
.nickname-field { flex: 1; height: 36px; border-radius: 10px; border: 1px solid rgba(124,77,255,0.45); background: rgba(255,255,255,0.08); color: #eaf2ff; padding: 6px 10px; }
.nickname-field:focus { outline: none; box-shadow: 0 0 0 3px rgba(124,77,255,0.25); border-color: rgba(124,77,255,0.65); }
.nickname-error { color: #ff9e80; font-size: 12px; }
.nickname-confirm { align-self: center; margin-top: 14px; padding: 12px 24px; width: 68%; max-width: 340px; height: 46px; border-radius: 14px; border: none; cursor: pointer; color: #1a1f35; background: linear-gradient(90deg,#ffd54f 0%, #ff9100 100%); background-size: 200% 100%; box-shadow: 0 10px 28px rgba(255,190,0,0.32); font-weight: 700; display: flex; align-items: center; justify-content: center; position: relative; transition: background-position .8s ease, transform .18s ease, box-shadow .28s ease; letter-spacing: 1px; }
.nickname-confirm:hover { background-position: right center; transform: translateY(-1px); box-shadow: 0 14px 36px rgba(255,190,0,0.42); }
.nickname-confirm:active { transform: translateY(1px) scale(0.98); }
.nickname-confirm::before { content: ''; position: absolute; left: -40%; top: 0; width: 40%; height: 100%; background: linear-gradient(120deg, rgba(255,255,255,0.22) 0%, rgba(255,255,255,0.0) 100%); filter: blur(2px); opacity: 0; transition: left .6s ease, opacity .3s ease; pointer-events: none; }
.nickname-confirm:hover::before { left: 110%; opacity: 1; }
.nickname-confirm[disabled] { opacity: 0.6; cursor: not-allowed; box-shadow: none; }
.home-cosmic-container.eye-care-mode .nickname-modal { background: linear-gradient(135deg, rgba(144,198,149,0.96) 0%, rgba(127,176,105,0.96) 100%); border-color: rgba(127,176,105,0.45); box-shadow: 0 18px 60px rgba(127,176,105,0.25), 0 8px 24px rgba(127,176,105,0.15); }

@media (max-width: 900px) {
  .home-cosmic-container { padding: 20px 16px 28px; }
  .quick-actions { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 560px) {
  .home-cosmic-container {
    padding: 92px 14px 96px;
  }
  .cosmic-content-container {
    gap: 14px;
  }
  .quick-actions { grid-template-columns: 1fr; }
  .home-header { grid-template-columns: 52px 1fr; }
}

.home-cosmic-container.eye-care-mode .action-card { background: rgba(144, 198, 149, 0.08); border-color: rgba(127, 176, 105, 0.25); box-shadow: 0 8px 40px 0 rgba(127, 176, 105, 0.15), 0 1.5px 6px rgba(144, 198, 149, 0.1); }
.home-cosmic-container.eye-care-mode .action-card:hover { box-shadow: 0 16px 60px 0 rgba(127, 176, 105, 0.25), 0 2px 12px rgba(144, 198, 149, 0.15); border-color: rgba(127, 176, 105, 0.4); }
.home-cosmic-container.eye-care-mode .action-feedback { border-color: rgba(74,111,255,0.30); box-shadow: 0 8px 40px 0 rgba(74,111,255,0.22), 0 1.5px 6px rgba(124,77,255,0.18); }
.home-cosmic-container.eye-care-mode .action-feedback:hover { box-shadow: 0 18px 60px 0 rgba(74,111,255,0.34), 0 4px 18px rgba(124,77,255,0.26); }
.home-cosmic-container.eye-care-mode .action-career { border-color: rgba(0,229,255,0.30); box-shadow: 0 8px 40px 0 rgba(0,229,255,0.20), 0 1.5px 6px rgba(20,197,180,0.18); }
.home-cosmic-container.eye-care-mode .action-career:hover { box-shadow: 0 18px 60px 0 rgba(0,229,255,0.32), 0 4px 18px rgba(20,197,180,0.26); }
.home-cosmic-container.eye-care-mode .action-history { border-color: rgba(124,77,255,0.30); box-shadow: 0 8px 40px 0 rgba(124,77,255,0.20), 0 1.5px 6px rgba(0,229,255,0.16); }
.home-cosmic-container.eye-care-mode .action-history:hover { box-shadow: 0 18px 60px 0 rgba(124,77,255,0.32), 0 4px 18px rgba(0,229,255,0.24); }
.home-cosmic-container.eye-care-mode .action-profile { border-color: rgba(127,176,105,0.35); box-shadow: 0 8px 40px 0 rgba(127,176,105,0.20), 0 1.5px 6px rgba(144,198,149,0.16); }
.home-cosmic-container.eye-care-mode .action-profile:hover { box-shadow: 0 18px 60px 0 rgba(127,176,105,0.32), 0 4px 18px rgba(144,198,149,0.24); }
.home-cosmic-container.eye-care-mode .action-chat { border-color: rgba(74,111,255,0.30); box-shadow: 0 8px 40px 0 rgba(74,111,255,0.20), 0 1.5px 6px rgba(124,77,255,0.18); }
.home-cosmic-container.eye-care-mode .action-chat:hover { box-shadow: 0 18px 60px 0 rgba(74,111,255,0.32), 0 4px 18px rgba(124,77,255,0.24); }
.home-cosmic-container.eye-care-mode .action-rank { border-color: rgba(255,145,0,0.35); box-shadow: 0 8px 40px 0 rgba(255,145,0,0.22), 0 1.5px 6px rgba(255,213,79,0.18); }
.home-cosmic-container.eye-care-mode .action-rank:hover { box-shadow: 0 18px 60px 0 rgba(255,145,0,0.34), 0 4px 18px rgba(255,213,79,0.26); }
.home-cosmic-container.eye-care-mode .badge-alg { background: linear-gradient(90deg,#7fb069 0%,#90c695 100%); }
.home-cosmic-container.eye-care-mode .badge-career { background: linear-gradient(90deg,#7fb069 0%,#90c695 100%); }
.home-cosmic-container.eye-care-mode .badge-history { background: linear-gradient(90deg,#b3c27d 0%,#7fb069 100%); }
.home-cosmic-container.eye-care-mode .badge-profile { background: linear-gradient(90deg,#7fb069 0%,#90c695 100%); }
.home-cosmic-container.eye-care-mode .title-text { background: linear-gradient(90deg, #7fb069 10%, #90c695 90%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; text-shadow: 0 0 18px rgba(127, 176, 105, 0.3), 0 0 8px rgba(144, 198, 149, 0.2); }
.home-cosmic-container.eye-care-mode .subtitle { color: #a8c69f; }
.home-cosmic-container.eye-care-mode .username-highlight { color: #dbe8d9; }
.home-cosmic-container.eye-care-mode .cosmic-start-btn { background: linear-gradient(90deg, #7fb069 0%, #90c695 100%); box-shadow: 0 6px 24px rgba(127, 176, 105, 0.2); }
.home-cosmic-container.eye-care-mode .btn-star { color: #fff; text-shadow: 0 0 8px rgba(255, 255, 255, 0.8), 0 0 12px rgba(144, 198, 149, 0.6); opacity: 0.9; }
.home-cosmic-container.eye-care-mode .status-item { background: rgba(144, 198, 149, 0.10); border-color: rgba(127,176,105,0.35); }
.home-cosmic-container.eye-care-mode .status-item:hover { box-shadow: 0 12px 26px rgba(127,176,105,0.28); border-color: rgba(127,176,105,0.45); }
.home-cosmic-container.eye-care-mode .status-title { color: #153023; }
.home-cosmic-container.eye-care-mode .status-hint { color: #396247; }
.home-cosmic-container.eye-care-mode .status-tag { background: rgba(127,176,105,0.18); border: 1px solid rgba(127,176,105,0.35); color: #153023; }
.home-cosmic-container.eye-care-mode .admin-item { background: rgba(144, 198, 149, 0.10); border-color: rgba(127,176,105,0.35); }
.home-cosmic-container.eye-care-mode .admin-item:hover { box-shadow: 0 12px 26px rgba(127,176,105,0.28); border-color: rgba(127,176,105,0.45); }
.home-cosmic-container.eye-care-mode .admin-detail { background: linear-gradient(180deg, rgba(127,176,105,0.16), rgba(127,176,105,0.10)); border-color: rgba(127,176,105,0.35); box-shadow: 0 6px 18px rgba(0,0,0,0.18); }
.home-cosmic-container.eye-care-mode .detail-user { color: #ffeb99; }
.home-cosmic-container.eye-care-mode .detail-tags .tag { background: rgba(127,176,105,0.12); border-color: rgba(127,176,105,0.28); color: #153023; }
.home-cosmic-container.eye-care-mode .detail-title, .home-cosmic-container.eye-care-mode .detail-content { color: #153023; }
.home-cosmic-container.eye-care-mode .status-loading, .home-cosmic-container.eye-care-mode .status-empty { color: #2f5138; }
.home-cosmic-container.eye-care-mode .admin-line { color: #153023; }
.home-cosmic-container.eye-care-mode .reply-input { color: #153023; background: rgba(255,255,255,0.12); }
.home-cosmic-container.eye-care-mode .reply-input::placeholder { color: #3e6a4b; }
.home-cosmic-container.eye-care-mode .reply-btn { background: linear-gradient(90deg, #7fb069 0%, #90c695 100%); border: none; color: #1a1f35; box-shadow: 0 6px 18px rgba(127,176,105,0.24); }
.home-cosmic-container.eye-care-mode .reply-input { border-color: rgba(127,176,105,0.45); background: rgba(255,255,255,0.08); color: #dbe8d9; }
.home-cosmic-container.eye-care-mode .admin-feedback-fab { background: rgba(144,198,149,0.18); border-color: rgba(127,176,105,0.45); box-shadow: 0 8px 26px rgba(127,176,105,0.28); }
.home-cosmic-container.eye-care-mode .admin-feedback-fab svg { color: #dbe8d9; filter: drop-shadow(0 0 8px rgba(127,176,105,0.45)); }
.home-cosmic-container.eye-care-mode .admin-feedback-fab .fab-tooltip { background: rgba(127,176,105,0.35); color: #fffbea; border-color: rgba(127,176,105,0.55); text-shadow: 0 0 2px rgba(0,0,0,0.25); font-weight: 700; }
</style>

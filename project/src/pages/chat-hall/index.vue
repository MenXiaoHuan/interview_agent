<template>
  <div class="chat-cosmic-container" :class="{ 'eye-care-mode': isEyeCareMode }">
    <div id="stars"></div>
    <div id="stars2"></div>
    <div id="stars3"></div>
    <div class="bubbles"><div v-for="n in 20" :key="n" class="bubble"></div></div>
    <div class="cosmic-content-container">
      <div class="cosmic-header-row">
        <div class="back-button" @click="handleBack" aria-label="返回" role="button">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
      </div>
      <div class="chat-container">
    <div class="chat-header">
      <h1 class="title">聊天大厅</h1>
      <div class="room">全站 · {{ room }}</div>
      
    </div>
    <div class="chat-body" v-loading="loading" ref="bodyRef">
      <div v-for="m in messages" :key="m.id || m.ts" class="msg" :class="{ me: m.isMe }">
        <div class="avatar">{{ (m.user || 'U')[0] }}</div>
        <div class="content">
          <div class="meta">
            <span class="name">{{ m.user || '匿名' }}</span>
            <span class="time">{{ formatTime(m.ts) }}</span>
          </div>
          <div class="text">{{ m.content }}</div>
        </div>
      </div>
      <div v-if="!messages.length && !loading" class="empty">暂时没有消息</div>
    </div>
    <div class="chat-input">
      <el-input v-model="input" placeholder="说点什么..." />
      <el-button type="primary" @click="handleSend" :disabled="sendDisabled">发送</el-button>
    </div>
      </div>
    </div>
    
  </div>
</template>
<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { API } from '@/utils/api'

const room = ref('global')
const messages = ref([])
const loading = ref(false)
const input = ref('')
let pollTimer = null
let isFetching = false
let prevLength = 0
const sendDisabled = ref(false)
let lastSendTs = 0
const bodyRef = ref(null)
const currentUserId = ref(null)
const currentNickname = ref('')
const getSessionUserInfo = () => {
  try {
    const u = typeof window !== 'undefined' ? window.sessionStorage.getItem('userInfo') : ''
    if (u) return JSON.parse(u)
  } catch {}
  const uid = typeof uni !== 'undefined' ? uni.getStorageSync('activeUserId') : ''
  let raw = ''
  if (uid) {
    raw = typeof uni !== 'undefined' ? (uni.getStorageSync(`userInfo:${uid}`) || uni.getStorageSync('userInfo')) : ''
  } else {
    raw = typeof uni !== 'undefined' ? uni.getStorageSync('userInfo') : ''
  }
  try { return raw ? JSON.parse(raw) : {} } catch { return {} }
}

const getCurrentUserId = () => {
  try {
    if (currentUserId.value) return currentUserId.value
    const uidStore = (userStore && (userStore.userId || (userStore.userInfo && userStore.userInfo.userId))) || null
    if (uidStore) return uidStore
  } catch {}
  const sess = getSessionUserInfo() || {}
  return sess.userId || null
}

const ensureCurrentUserFromServer = async () => {
  try {
    const token = getSessionToken()
    if (!token) return
    const res = await fetch(API.USER.PROFILE, { headers: { 'Authorization': `Bearer ${token}` } })
    if (!res.ok) return
    const data = await res.json()
    if (data && data.code === 200 && data.data) {
      currentUserId.value = data.data.userId || null
      currentNickname.value = data.data.nickname || data.data.username || ''
    }
  } catch {}
}

const userStore = useUserStore()
const { isEyeCareMode } = storeToRefs(userStore)

const formatTime = (ts) => {
  if (!ts) return ''
  const d = new Date(ts)
  const p = (n) => String(n).padStart(2, '0')
  return `${p(d.getHours())}:${p(d.getMinutes())}`
}

const getSessionToken = () => {
  try {
    const t = typeof window !== 'undefined' ? window.sessionStorage.getItem('token') : ''
    if (t) return t
  } catch {}
  const uid = typeof uni !== 'undefined' ? uni.getStorageSync('activeUserId') : ''
  if (uid) {
    const ns = typeof uni !== 'undefined' ? (uni.getStorageSync(`token:${uid}`) || '') : ''
    if (ns) return ns
  }
  const legacy = typeof uni !== 'undefined' ? (uni.getStorageSync('token') || '') : ''
  return legacy || ''
}

const loadMessages = async () => {
  try {
    const token = getSessionToken()
    if (!token) return
    const res = await fetch(API.CHAT.MESSAGES(1), {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (!res.ok) return
    const resp = await res.json()
    if (resp && (resp.code === 20000 || resp.code === 200) && Array.isArray(resp.data)) {
      const uid = getCurrentUserId()
      const normalized = resp.data.map(item => ({
        id: item.id,
        content: item.content,
        ts: Date.parse(item.createdAt),
        user: item.nickname || '匿名',
        avatarUrl: item.avatarUrl || '',
        isMe: uid ? Number(item.userId) === Number(uid) : false
      })).sort((a, b) => a.ts - b.ts)
      const newLen = normalized.length
      if (newLen !== prevLength) {
        messages.value = normalized
        prevLength = newLen
        await nextTick()
        if (bodyRef.value) {
          bodyRef.value.scrollTop = bodyRef.value.scrollHeight
        }
      }
    }
  } catch {}
}

const sendCore = async () => {
  const text = input.value.trim()
  if (!text) return
  const token = getSessionToken()
  const res = await fetch(API.CHAT.MESSAGE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ content: text })
  })
  if (res.status === 401) {
    if (typeof uni !== 'undefined') {
      uni.showToast({ title: '请先登录', icon: 'none', duration: 2000 })
      setTimeout(() => { uni.navigateTo({ url: '/pages/login/index' }) }, 1500)
    }
    return
  }
  try {
    const resp = await res.json()
    if (!(resp && (resp.code === 20000 || resp.code === 200))) {
      if (typeof uni !== 'undefined') {
        uni.showToast({ title: resp?.message || '发送失败', icon: 'none', duration: 2000 })
      }
      return
    }
    if (typeof uni !== 'undefined') {
      uni.showToast({ title: '发送成功', icon: 'success', duration: 1000 })
    }
    input.value = ''
    // 若后端返回的userId与当前用户不一致，则强制同步当前用户
    try {
      const sentUid = resp?.data?.userId
      const uid = getCurrentUserId()
      if (sentUid !== undefined && uid !== null && Number(sentUid) !== Number(uid)) {
        await ensureCurrentUserFromServer()
      }
    } catch {}
    await loadMessages()
    await nextTick()
    if (bodyRef.value) {
      bodyRef.value.scrollTop = bodyRef.value.scrollHeight
    }
  } catch {}
}

const canSendNow = () => {
  const now = Date.now()
  if (now - lastSendTs < 1000) return false
  lastSendTs = now
  return true
}

const handleSend = async () => {
  if (!canSendNow()) return
  sendDisabled.value = true
  try {
    await sendCore()
  } finally {
    setTimeout(() => { sendDisabled.value = false }, 1000)
  }
}

onMounted(() => {
  loadMessages()
  ensureCurrentUserFromServer()
  pollTimer = setInterval(async () => {
    if (isFetching) return
    isFetching = true
    try { await loadMessages() } finally { isFetching = false }
  }, 1000)
})

onUnmounted(() => { if (pollTimer) { clearInterval(pollTimer); pollTimer = null } })

const handleBack = () => { uni.reLaunch({ url: '/pages/home/index' }) }


</script>

<style scoped lang="scss">
@use 'sass:math';
@use 'sass:string';
.chat-cosmic-container { min-height: 100vh; width: 100vw; overflow: hidden; position: relative; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg,#0a1929 0%, #1a1f35 100%); }
.chat-cosmic-container.eye-care-mode { background: linear-gradient(135deg, #1c2a1f 0%, #2a3c2e 100%); }
@function multiple-box-shadow($n) { $value: '#{math.random(2560)}px #{math.random(2560)}px #FFF'; @for $i from 2 through $n { $value: '#{$value}, #{math.random(2560)}px #{math.random(2560)}px #FFF'; } @return string.unquote($value); }
$shadows-small: multiple-box-shadow(1000);
$shadows-medium: multiple-box-shadow(300);
$shadows-large: multiple-box-shadow(150);
#stars, #stars2, #stars3 { position: absolute; inset: 0; display: block; z-index: 0; background: transparent; pointer-events: none; }
#stars { width: 1px; height: 1px; box-shadow: $shadows-small; animation: animStar 150s linear infinite; }
#stars2 { width: 2px; height: 2px; box-shadow: $shadows-medium; animation: animStar 100s linear infinite; }
#stars3 { width: 3px; height: 3px; box-shadow: $shadows-large; animation: animStar 50s linear infinite; }
@keyframes animStar { from { transform: translateY(0px); } to { transform: translateY(-2560px); } }
.bubbles { position: absolute; inset: 0; z-index: 2; pointer-events: none; }
.cosmic-content-container { position: relative; z-index: 3; width: 100%; min-height: 80vh; display: flex; align-items: center; justify-content: center; }
.cosmic-header-row { position: fixed; left: 20px; top: 20px; z-index: 4; }
.back-button { cursor: pointer; width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; border-radius: 12px; background: rgba(124, 77, 255, 0.12); border: 1px solid rgba(124, 77, 255, 0.25); backdrop-filter: blur(10px); transition: all 0.3s ease; box-shadow: 0 2px 8px rgba(0,0,0,0.2); }
.back-button:hover { transform: scale(1.05); background: rgba(124, 77, 255, 0.2); border-color: rgba(124, 77, 255, 0.35); box-shadow: 0 0 20px rgba(124,77,255,0.2), 0 0 40px rgba(0,229,255,0.12); }
.back-button svg { color: #7c4dff; filter: drop-shadow(0 0 5px rgba(124, 77, 255, 0.5)); }
/* 护眼模式返回按钮适配 */
.chat-cosmic-container.eye-care-mode .back-button { background: rgba(127, 176, 105, 0.12); border-color: rgba(127, 176, 105, 0.25); }
.chat-cosmic-container.eye-care-mode .back-button:hover { background: rgba(127, 176, 105, 0.18); border-color: rgba(127, 176, 105, 0.35); box-shadow: 0 0 20px rgba(127, 176, 105, 0.25), 0 0 40px rgba(144, 198, 149, 0.12); }
.chat-cosmic-container.eye-care-mode .back-button svg { color: #7fb069; filter: drop-shadow(0 0 5px rgba(127, 176, 105, 0.5)); }
.knot-fab { position: fixed; left: 22px; bottom: 22px; z-index: 6; width: 56px; height: 56px; border-radius: 999px; backdrop-filter: blur(6px); background: rgba(20,24,40,0.72); border: 2px solid rgba(255,145,0,0.45); display: flex; align-items: center; justify-content: center; box-shadow: 0 8px 26px rgba(255,145,0,0.33); cursor: pointer; }
.knot-fab:hover { transform: translateY(-2px); box-shadow: 0 12px 32px rgba(255,145,0,0.35); }
.knot-svg { width: 76%; height: 76%; }
.fab-tooltip { position: absolute; bottom: 64px; left: 0; transform: translateY(6px); background: rgba(20,24,40,0.92); color: #ffd54f; border: 1px solid rgba(255,145,0,0.35); border-radius: 8px; padding: 6px 10px; font-size: 12px; white-space: nowrap; opacity: 0; pointer-events: none; transition: opacity .2s, transform .2s; }
.knot-fab:hover .fab-tooltip { opacity: 1; transform: translateY(0); }
.fab-sticky-hint { position: absolute; bottom: 64px; left: 0; background: rgba(20,24,40,0.92); color: #ffd54f; border: 1px solid rgba(255,145,0,0.45); border-radius: 8px; padding: 6px 10px; font-size: 12px; white-space: nowrap; box-shadow: 0 8px 18px rgba(255,145,0,0.28); }
.chat-cosmic-container.eye-care-mode .knot-fab { background: rgba(144,198,149,0.18); border-color: rgba(127,176,105,0.45); box-shadow: 0 8px 26px rgba(127,176,105,0.28); }
.chat-cosmic-container.eye-care-mode .fab-tooltip, .chat-cosmic-container.eye-care-mode .fab-sticky-hint { background: rgba(127,176,105,0.35); color: #fffbea; border-color: rgba(127,176,105,0.55); text-shadow: 0 0 2px rgba(0,0,0,0.25); font-weight: 700; }
.fireworks-dialog :deep(.el-dialog__header) { background: linear-gradient(90deg,#d6001f 0%, #ff9100 100%); color: #fff; border-radius: 12px 12px 0 0; }
.fireworks-dialog :deep(.el-dialog__body) { background: radial-gradient(circle at 30% 20%, rgba(255,77,79,0.18), rgba(255,145,0,0.12)); }
.fireworks-wrap { padding: 6px; border-radius: 12px; border: 1px solid rgba(255,145,0,0.35); box-shadow: 0 8px 26px rgba(255,145,0,0.28); }
.firework-static-bg { height: 300px; border-radius: 12px; background: radial-gradient(circle at 20% 30%, rgba(255,77,79,0.25), transparent 40%), radial-gradient(circle at 70% 50%, rgba(255,213,79,0.25), transparent 40%), radial-gradient(circle at 40% 70%, rgba(148,77,255,0.25), transparent 40%); }
.blessing-text { margin-top: 10px; color: #fff; text-shadow: 0 1px 2px rgba(0,0,0,0.25); }
.chat-cosmic-container.eye-care-mode .blessing-text { color: #153023; }
.chat-container { width: 720px; height: 500px; margin: 0 auto; padding: 16px; border-radius: 24px; background: rgba(20,24,40,0.86); border: 2px solid rgba(124,77,255,0.18); box-shadow: 0 8px 40px 0 #7c4dff22; display: flex; flex-direction: column; }
.chat-cosmic-container.eye-care-mode .chat-container { background: rgba(127, 176, 105, 0.12); border: 2px solid rgba(127,176,105,0.28); box-shadow: 0 8px 40px 0 rgba(127,176,105,0.18); }
.chat-header { display: flex; align-items: baseline; gap: 10px; margin-bottom: 12px; }
.title { margin: 0; font-size: 1.4rem; background: linear-gradient(90deg,#7c4dff 0%,#00e5ff 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.room { color: #a8c6ff; }
 
.chat-body { flex: 1; overflow: auto; display: flex; flex-direction: column; gap: 12px; padding: 8px 2px; }
.chat-body { scrollbar-width: none; }
.chat-body::-webkit-scrollbar { width: 0; height: 0; }
.msg { display: flex; gap: 10px; align-items: flex-start; }
.msg.me { justify-content: flex-end; }
.msg.me .avatar { order: 2; }
.msg.me .content { text-align: right; }
.msg.me .meta { flex-direction: row-reverse; }
.msg .meta { margin-bottom: 4px; }
.msg .text { display: inline-block; max-width: 85%; padding: 10px 12px; border-radius: 14px; background: rgba(255,255,255,0.06); border: 1px solid rgba(124,77,255,0.18); color: #eaf2ff; box-shadow: 0 2px 12px rgba(0,0,0,0.15); }
.msg.me .text { background: linear-gradient(135deg, #4A6FFF 0%, #6B8CFF 100%); color: #ffffff; border: none; box-shadow: 0 6px 20px rgba(107,140,255,0.28); }
.msg .time { opacity: 0; visibility: hidden; transition: opacity .2s ease; }
.msg:hover .time { opacity: 1; visibility: visible; }
.msg.me .avatar { background: #4A6FFF; }
.chat-cosmic-container.eye-care-mode .msg.me .text { background: linear-gradient(135deg, #7fb069 0%, #90c695 100%); color: #ffffff; }
.avatar { width: 32px; height: 32px; border-radius: 50%; background: #7c4dff; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; }
.content { flex: 1; }
.meta { display: flex; gap: 8px; align-items: baseline; }
.name { color: #eaf2ff; font-weight: 600; }
.time { color: #8aa6dd; font-size: 12px; }
.text { color: #eaf2ff; }
.empty { color: #a8c6ff; text-align: center; padding: 18px 0; }
.chat-input { display: flex; gap: 8px; margin-top: 12px; }
@media (max-width: 780px) { .chat-container { width: 92vw; height: 70vh; } }
</style>

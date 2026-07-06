<template>
  <div class="rank-cosmic-container" :class="{ 'eye-care-mode': isEyeCareMode }">
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
      <div class="rank-container">
    <div class="rank-header">
      <h1 class="title">排行榜</h1>
      <div class="rank-tabs">
        <button
          v-for="stage in rankStages"
          :key="stage.type"
          class="tab-btn"
          :class="{ active: rankType === stage.type }"
          @click="switchType(stage.type)"
        >
          {{ stage.label }}
        </button>
      </div>
      
    </div>
    <transition name="rank-switch" mode="out-in">
      <div class="rank-list" v-loading="loading" :key="rankType">
        <div v-for="(u,i) in pagedList" :key="u.userId || i" class="rank-item" :class="{ me: isMe(u) }">
          <div class="rank-no" :class="{'top': i < 3}">{{ i + 1 }}</div>
          <div class="rank-name">
            <img v-if="u.avatarUrl" class="avatar" :src="u.avatarUrl" alt="avatar" />
            <span>{{ u.nickname || '匿名' }}</span>
          </div>
          <div class="rank-score"><span class="score-label">最高分：</span><span class="score-val">{{ formatScoreInt(u.score) }}</span><span>分</span></div>
        </div>
        <div v-if="!list.length && !loading" class="empty">暂无数据</div>
      </div>
  </transition>
      </div>
    </div>
    
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { API } from '@/utils/api/pages/leaderboard'

const list = ref([])
const loading = ref(false)
const topN = computed(() => list.value.slice(0, 10))
const pagedList = computed(() => topN.value)

const userStore = useUserStore()
const { isEyeCareMode } = storeToRefs(userStore)
const rankType = ref('resume')
const rankStages = [
  { type: 'resume', label: '简历榜', endpoint: API.RANK.RESUME },
  { type: 'question', label: '试题榜', endpoint: API.RANK.QUESTION },
  { type: 'round_1', label: '一面榜', endpoint: API.RANK.ROUND_1 },
  { type: 'round_2', label: '二面榜', endpoint: API.RANK.ROUND_2 },
  { type: 'round_3', label: '三面榜', endpoint: API.RANK.ROUND_3 }
]
 

const formatScoreInt = (s) => {
  const n = Number(s)
  const int = Math.round(Number.isNaN(n) ? 0 : n)
  const clamped = Math.max(0, Math.min(100, int))
  const str = String(clamped)
  return str.padStart(3, '\u00A0')
}

const getSessionToken = () => {
  try {
    const t = typeof window !== 'undefined' ? window.sessionStorage.getItem('token') : ''
    if (t) return t
  } catch {}
  const legacy = typeof uni !== 'undefined' ? (uni.getStorageSync('token') || '') : ''
  return legacy || ''
}

const getSessionUserId = () => {
  try {
    const u = typeof window !== 'undefined' ? window.sessionStorage.getItem('userInfo') : ''
    if (u) return JSON.parse(u)?.userId || null
  } catch {}
  const raw = typeof uni !== 'undefined' ? (uni.getStorageSync('userInfo') || '') : ''
  try { return raw ? JSON.parse(raw)?.userId || null : null } catch { return null }
}

const isMe = (item) => {
  const uid = getSessionUserId()
  return uid ? Number(item.userId) === Number(uid) : false
}

const loadRank = async () => {
  loading.value = true
  try {
    const token = getSessionToken()
    const currentStage = rankStages.find((item) => item.type === rankType.value) || rankStages[0]
    const qp = ['size=10']
    const url = `${currentStage.endpoint}${qp.length ? ('?' + qp.join('&')) : ''}`
    const res = await fetch(url, { headers: token ? { 'Authorization': `Bearer ${token}` } : {} })
    if (!res.ok) { list.value = []; return }
    const resp = await res.json()
    if (resp && (resp.code === 200 || resp.code === 20000) && Array.isArray(resp.data)) {
      list.value = resp.data
    } else {
      list.value = []
    }
  } finally {
    loading.value = false
  }
}

const switchType = (t) => { if (rankType.value !== t) { rankType.value = t; loadRank() } }

onMounted(() => { loadRank() })

const handleBack = () => { uni.reLaunch({ url: '/pages/home/index' }) }
</script>

<style scoped lang="scss">
@use 'sass:math';
@use 'sass:string';
.rank-cosmic-container { min-height: 100vh; width: 100vw; overflow: hidden; position: relative; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg,#0a1929 0%, #1a1f35 100%); }
.rank-cosmic-container.eye-care-mode { background: linear-gradient(135deg, #1c2a1f 0%, #2a3c2e 100%); }
@function multiple-box-shadow($n) { $value: '#{math.random(2560)}px #{math.random(2560)}px #FFF'; @for $i from 2 through $n { $value: '#{$value}, #{math.random(2560)}px #{math.random(2560)}px #FFF'; } @return string.unquote($value); }
$shadows-small: multiple-box-shadow(1000); $shadows-medium: multiple-box-shadow(300); $shadows-large: multiple-box-shadow(150);
#stars, #stars2, #stars3 { position: absolute; inset: 0; display: block; z-index: 0; background: transparent; pointer-events: none; }
#stars { width: 1px; height: 1px; box-shadow: $shadows-small; animation: animStar 150s linear infinite; }
#stars2 { width: 2px; height: 2px; box-shadow: $shadows-medium; animation: animStar 100s linear infinite; }
#stars3 { width: 3px; height: 3px; box-shadow: $shadows-large; animation: animStar 50s linear infinite; }
@keyframes animStar { from { transform: translateY(0px); } to { transform: translateY(-2560px); } }
.bubbles { position: absolute; inset: 0; z-index: 2; pointer-events: none; }
.cosmic-content-container { position: relative; z-index: 3; width: 100%; max-width: 960px; margin: 24px auto; }
.cosmic-header-row { position: fixed; left: 20px; top: 20px; z-index: 4; }
.back-button { cursor: pointer; width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; border-radius: 12px; background: rgba(124, 77, 255, 0.12); border: 1px solid rgba(124, 77, 255, 0.25); backdrop-filter: blur(10px); transition: all 0.3s ease; box-shadow: 0 2px 8px rgba(0,0,0,0.2); }
.back-button:hover { transform: scale(1.05); background: rgba(124, 77, 255, 0.2); border-color: rgba(124, 77, 255, 0.35); box-shadow: 0 0 20px rgba(124,77,255,0.2), 0 0 40px rgba(0,229,255,0.12); }
.back-button svg { color: #7c4dff; filter: drop-shadow(0 0 5px rgba(124, 77, 255, 0.5)); }
.rank-container { max-width: 960px; margin: 0 auto; padding: 16px; border-radius: 24px; background: rgba(20,24,40,0.86); border: 2px solid rgba(124,77,255,0.18); box-shadow: 0 8px 40px 0 #7c4dff22; }
.rank-container { width: 720px; height: 500px; display: flex; flex-direction: column; }
.rank-header { display: grid; grid-template-columns: 1fr auto; align-items: center; gap: 12px; margin-bottom: 12px; }
.title { margin: 0; font-size: 1.6rem; background: linear-gradient(90deg,#ffd54f 0%, #ff9100 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; letter-spacing: 0.5px; }
.rank-tabs { display: flex; flex-wrap: wrap; gap: 8px; margin-left: 8px; padding: 4px; border-radius: 12px; background: rgba(255,190,0,0.08); border: 1px solid rgba(255,190,0,0.25); box-shadow: 0 4px 20px rgba(255,190,0,0.12); }
.tab-btn { padding: 6px 12px; border-radius: 10px; border: 1px solid rgba(255,190,0,0.25); background: rgba(255,190,0,0.06); color: #ffcc80; cursor: pointer; transition: all .2s ease; }
.tab-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 16px rgba(255,190,0,0.18); }
.tab-btn.active { background: linear-gradient(90deg,#ffd54f 0%, #ff9100 100%); color: #1a1f35; font-weight: 700; border: none; box-shadow: 0 8px 24px rgba(255,190,0,0.28); }
 
 
.rank-list { display: flex; flex-direction: column; gap: 10px; flex: 1; overflow: auto; }
.rank-item { display: grid; grid-template-columns: 46px 1fr 140px; align-items: center; gap: 12px; padding: 12px 14px; border-radius: 16px; background: linear-gradient(180deg, rgba(10,14,28,0.78), rgba(10,14,28,0.66)); border: 1px solid rgba(124,77,255,0.18); box-shadow: 0 6px 18px rgba(0,0,0,0.25); transition: all .18s ease; }
.rank-item:hover { transform: translateY(-2px); box-shadow: 0 10px 26px rgba(124,77,255,0.28); }
.rank-no { width: 32px; height: 32px; border-radius: 50%; background: #7c4dff; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; }
.rank-no.top { background: linear-gradient(90deg,#ffd54f 0%, #ff9100 100%); box-shadow: 0 0 10px #ffb74d88, 0 0 24px #ffd54f44; }
.rank-item:nth-child(1) .rank-no { background: linear-gradient(90deg,#ffd54f 0%, #ff9100 100%); }
.rank-item:nth-child(2) .rank-no { background: linear-gradient(90deg,#cfd8dc 0%, #90a4ae 100%); }
.rank-item:nth-child(3) .rank-no { background: linear-gradient(90deg,#ffcc80 0%, #ffb74d 100%); }
.rank-name { color: #eaf2ff; display: flex; align-items: center; gap: 10px; }
.rank-item.me { background: rgba(255,180,0,0.14); border: 1px solid rgba(255,190,0,0.35); }
.avatar { width: 28px; height: 28px; border-radius: 50%; object-fit: cover; }
.rank-score { color: #ffcc80; text-align: right; font-weight: 700; width: 140px; white-space: nowrap; font-variant-numeric: tabular-nums; font-feature-settings: "tnum"; display: flex; justify-content: flex-end; }
.empty { color: #a8c6ff; text-align: center; padding: 18px 0; }
/* 切换转场 */
.rank-switch-enter-active, .rank-switch-leave-active { transition: opacity .25s ease, transform .25s ease; }
.rank-switch-enter-from { opacity: 0; transform: translateY(6px) scale(0.98); }
.rank-switch-leave-to { opacity: 0; transform: translateY(-6px) scale(0.98); }
/* 护眼模式返回按钮适配 */
.rank-cosmic-container.eye-care-mode .back-button { background: rgba(127, 176, 105, 0.12); border-color: rgba(127, 176, 105, 0.25); }
.rank-cosmic-container.eye-care-mode .back-button:hover { background: rgba(127, 176, 105, 0.18); border-color: rgba(127, 176, 105, 0.35); box-shadow: 0 0 20px rgba(127, 176, 105, 0.25), 0 0 40px rgba(144, 198, 149, 0.12); }
.rank-cosmic-container.eye-care-mode .back-button svg { color: #7fb069; filter: drop-shadow(0 0 5px rgba(127, 176, 105, 0.5)); }

/* 护眼模式排行榜盒子适配 */
.rank-cosmic-container.eye-care-mode .rank-container { background: rgba(144,198,149,0.10); border-color: rgba(127,176,105,0.30); box-shadow: 0 8px 40px 0 rgba(127,176,105,0.18); }
.rank-cosmic-container.eye-care-mode .rank-item { background: linear-gradient(180deg, rgba(127,176,105,0.18), rgba(127,176,105,0.12)); border-color: rgba(127,176,105,0.28); }
.rank-cosmic-container.eye-care-mode .rank-name { color: #e8f2e6; }
.rank-cosmic-container.eye-care-mode .rank-score { color: #e8f2e6; }
.rank-cosmic-container.eye-care-mode .rank-tabs { background: rgba(127,176,105,0.08); border-color: rgba(127,176,105,0.25); box-shadow: 0 4px 20px rgba(127,176,105,0.12); }
.rank-cosmic-container.eye-care-mode .tab-btn { border-color: rgba(127,176,105,0.25); background: rgba(127,176,105,0.06); color: #dbe8d9; }
.rank-cosmic-container.eye-care-mode .tab-btn.active { background: linear-gradient(90deg,#7fb069 0%, #90c695 100%); color: #1a1f35; box-shadow: 0 8px 24px rgba(127,176,105,0.28); }

.firework-canvas { position: fixed; inset: 0; z-index: 5; background: transparent; pointer-events: none; }

.knot-fab { position: fixed; left: 22px; bottom: 22px; z-index: 6; width: 56px; height: 56px; border-radius: 999px; backdrop-filter: blur(6px); background: rgba(20,24,40,0.72); border: 2px solid rgba(255,145,0,0.45); display: flex; align-items: center; justify-content: center; box-shadow: 0 8px 26px rgba(255,145,0,0.33); cursor: pointer; }
.knot-fab:hover { transform: translateY(-2px); box-shadow: 0 12px 32px rgba(255,145,0,0.35); }
.knot-svg { width: 76%; height: 76%; }
.fab-tooltip { position: absolute; bottom: 64px; left: 0; transform: translateY(6px); background: rgba(20,24,40,0.92); color: #ffd54f; border: 1px solid rgba(255,145,0,0.35); border-radius: 8px; padding: 6px 10px; font-size: 12px; white-space: nowrap; opacity: 0; pointer-events: none; transition: opacity .2s, transform .2s; }
.knot-fab:hover .fab-tooltip { opacity: 1; transform: translateY(0); }
.fab-sticky-hint { position: absolute; bottom: 64px; left: 0; background: rgba(20,24,40,0.92); color: #ffd54f; border: 1px solid rgba(255,145,0,0.45); border-radius: 8px; padding: 6px 10px; font-size: 12px; white-space: nowrap; box-shadow: 0 8px 18px rgba(255,145,0,0.28); }
.rank-cosmic-container.eye-care-mode .knot-fab { background: rgba(144,198,149,0.18); border-color: rgba(127,176,105,0.45); box-shadow: 0 8px 26px rgba(127,176,105,0.28); }
.rank-cosmic-container.eye-care-mode .fab-tooltip, .rank-cosmic-container.eye-care-mode .fab-sticky-hint { background: rgba(127,176,105,0.35); color: #fffbea; border-color: rgba(127,176,105,0.55); text-shadow: 0 0 2px rgba(0,0,0,0.25); font-weight: 700; }
.fireworks-dialog :deep(.el-dialog__header) { background: linear-gradient(90deg,#d6001f 0%, #ff9100 100%); color: #fff; border-radius: 12px 12px 0 0; }
.fireworks-dialog :deep(.el-dialog__body) { background: radial-gradient(circle at 30% 20%, rgba(255,77,79,0.18), rgba(255,145,0,0.12)); }
.fireworks-wrap { padding: 6px; border-radius: 12px; border: 1px solid rgba(255,145,0,0.35); box-shadow: 0 8px 26px rgba(255,145,0,0.28); }
.firework-static-bg { height: 300px; border-radius: 12px; background: radial-gradient(circle at 20% 30%, rgba(255,77,79,0.25), transparent 40%), radial-gradient(circle at 70% 50%, rgba(255,213,79,0.25), transparent 40%), radial-gradient(circle at 40% 70%, rgba(148,77,255,0.25), transparent 40%); }
.blessing-text { margin-top: 10px; color: #fff; text-shadow: 0 1px 2px rgba(0,0,0,0.25); }
.rank-cosmic-container.eye-care-mode .blessing-text { color: #153023; }
</style>

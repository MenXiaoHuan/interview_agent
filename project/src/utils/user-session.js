import { API } from '@/utils/api'

const KEY = 'userInfo'
const TOKEN_KEY = 'token'
const SID_KEY = '__tabSessionId__'
let bus = null
let subs = []

function getSessionId() {
  try {
    let id = sessionStorage.getItem(SID_KEY)
    if (!id) {
      id = String(Date.now()) + '-' + Math.random().toString(36).slice(2)
      sessionStorage.setItem(SID_KEY, id)
    }
    return id
  } catch {
    return 'sid'
  }
}

function ensureBus() {
  if (bus) return bus
  const sid = getSessionId()
  try { bus = new BroadcastChannel('user-session-bus-' + sid) } catch { bus = null }
  if (bus) {
    bus.onmessage = (ev) => {
      const d = ev && ev.data
      if (!d || d.key !== KEY) return
      for (let i = 0; i < subs.length; i++) {
        try { subs[i](d.value) } catch {}
      }
    }
  }
  return bus
}

function getRaw() {
  try {
    const s = typeof sessionStorage !== 'undefined' ? sessionStorage.getItem(KEY) : null
    if (s) return JSON.parse(s)
  } catch {}
  try {
    const s = uni.getStorageSync(KEY)
    if (s) return JSON.parse(s)
  } catch {}
  return null
}

function setRaw(obj) {
  const s = JSON.stringify(obj || {})
  try { if (typeof sessionStorage !== 'undefined') sessionStorage.setItem(KEY, s) } catch {}
  try {
    const bc = ensureBus()
    if (bc) bc.postMessage({ key: KEY, value: obj })
  } catch {}
}

export function getUserSession() {
  return getRaw() || {}
}

export function setUserSessionPatch(patch) {
  const cur = getRaw() || {}
  const next = { ...cur, ...(patch || {}) }
  setRaw(next)
  return next
}

export function subscribeUserSession(handler) {
  ensureBus()
  subs.push(handler)
  return () => {
    const idx = subs.indexOf(handler)
    if (idx >= 0) subs.splice(idx, 1)
  }
}

export async function refreshUserSessionFromServer() {
  try {
    const token = getSessionToken()
    if (!token) return getUserSession()
    const resp = await uni.request({ url: API.USER.PROFILE, method: 'GET', header: { 'Authorization': `Bearer ${token}` } })
    const data = resp?.data?.data || {}
    if (resp.statusCode === 200 && resp.data?.code === 200) {
      return setUserSessionPatch(data)
    }
    return getUserSession()
  } catch {
    return getUserSession()
  }
}

export function getSessionToken() {
  try { const t = sessionStorage.getItem(TOKEN_KEY); if (t) return t } catch {}
  try { const t = uni.getStorageSync(TOKEN_KEY); if (t) return t } catch {}
  return ''
}

export function setSessionToken(token) {
  try { sessionStorage.setItem(TOKEN_KEY, token || '') } catch {}
}

export function clearSessionToken() {
  try { sessionStorage.removeItem(TOKEN_KEY) } catch {}
}

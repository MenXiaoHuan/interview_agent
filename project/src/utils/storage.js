// 本地存储工具，兼容uni和浏览器
const SESSION_KEYS = new Set(['token', 'userInfo', 'activeUserId', 'currentJobId', 'activeAiConversationChatId'])

export function getStorage(key) {
  try {
    if (SESSION_KEYS.has(key) && typeof window !== 'undefined' && window.sessionStorage) {
      const value = window.sessionStorage.getItem(key)
      if (value) return value
    }
  } catch {}
  if (typeof uni !== 'undefined') {
    return uni.getStorageSync(key)
  }
  return typeof localStorage !== 'undefined' ? localStorage.getItem(key) : null
}

export function setStorage(key, value) {
  try {
    if (SESSION_KEYS.has(key) && typeof window !== 'undefined' && window.sessionStorage) {
      window.sessionStorage.setItem(key, value)
    }
  } catch {}
  if (typeof uni !== 'undefined') {
    return uni.setStorageSync(key, value)
  }
  if (typeof localStorage !== 'undefined') {
    return localStorage.setItem(key, value)
  }
}

export function removeStorage(key) {
  try {
    if (SESSION_KEYS.has(key) && typeof window !== 'undefined' && window.sessionStorage) {
      window.sessionStorage.removeItem(key)
    }
  } catch {}
  if (typeof uni !== 'undefined') {
    return uni.removeStorageSync(key)
  }
  if (typeof localStorage !== 'undefined') {
    return localStorage.removeItem(key)
  }
}

export const getToken = () => getStorage('token')
export const setToken = (token) => setStorage('token', token)
export const removeToken = () => removeStorage('token')
export const getUserInfo = () => getStorage('userInfo')
export const setUserInfo = (userInfo) =>
  setStorage('userInfo', typeof userInfo === 'string' ? userInfo : JSON.stringify(userInfo))
export const removeUserInfo = () => removeStorage('userInfo')

import { executeRestLikeRequest } from '@/utils/graphql-bridge'

const DEFAULT_TIMEOUT = 10000
const DEFAULT_HEADERS = {
  'Content-Type': 'application/json'
}

function getToken() {
  try {
    const token = typeof window !== 'undefined' ? window.sessionStorage.getItem('token') : null
    if (token) {
      return token
    }
  } catch {}
  if (typeof uni !== 'undefined') {
    return uni.getStorageSync('token') || null
  }
  return null
}

function handleErrorPayload(status, data) {
  if (typeof uni === 'undefined') {
    return
  }

  uni.hideLoading()

  switch (status) {
    case 401:
      try {
        if (typeof window !== 'undefined') {
          window.sessionStorage.removeItem('token')
          window.sessionStorage.removeItem('userInfo')
          window.sessionStorage.removeItem('activeUserId')
        }
      } catch {}
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      uni.showToast({
        title: data?.message || '请重新登录',
        icon: 'none',
        duration: 2000
      })
      setTimeout(() => {
        uni.navigateTo({ url: '/pages/login/index' })
      }, 2000)
      break
    case 403:
      uni.showToast({ title: data?.message || '权限不足', icon: 'none', duration: 2000 })
      break
    case 404:
      uni.showToast({ title: '请求的资源不存在', icon: 'none', duration: 2000 })
      break
    case 429:
      uni.showToast({ title: '请求过于频繁，请稍后再试', icon: 'none', duration: 2000 })
      break
    case 500:
      uni.showToast({ title: '服务器错误: ' + (data?.message || '未知错误'), icon: 'none', duration: 2000 })
      break
    default:
      uni.showToast({ title: data?.message || '请求失败', icon: 'none', duration: 2000 })
  }
}

async function coreRequest(config = {}) {
  const headers = {
    ...DEFAULT_HEADERS,
    ...(config.headers || {})
  }

  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const payload = await executeRestLikeRequest({
    url: config.url,
    method: config.method || 'get',
    params: config.params,
    data: config.data,
    headers,
    timeout: config.timeout || DEFAULT_TIMEOUT
  })

  if (payload.code === 200) {
    return payload
  }

  handleErrorPayload(payload.code, payload)
  const error = new Error(payload.message || '请求失败')
  error.response = {
    status: payload.code,
    data: payload
  }
  throw error
}

function request(config) {
  return coreRequest(config)
}

request.get = (url, config = {}) => coreRequest({
  ...config,
  url,
  method: 'get'
})

request.post = (url, data, config = {}) => coreRequest({
  ...config,
  url,
  method: 'post',
  data
})

request.put = (url, data, config = {}) => coreRequest({
  ...config,
  url,
  method: 'put',
  data
})

request.delete = (url, config = {}) => coreRequest({
  ...config,
  url,
  method: 'delete'
})

export default request

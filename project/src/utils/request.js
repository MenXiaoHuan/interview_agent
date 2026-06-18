import axios from 'axios'
import { BASE_URL } from '@/utils/api-config'
import { getToken, removeStorage } from '@/utils/storage'

const SUCCESS_CODE = 200
const DEFAULT_TIMEOUT = 10000
const DEFAULT_HEADERS = {
  'Content-Type': 'application/json'
}
const ENCRYPTED_PASSWORD_PREFIX = 'RSA:'
const RSA_PUBLIC_KEY_STORAGE_KEY = 'rsaPublicKeyPem'
const SENSITIVE_PASSWORD_RULES = [
  { method: 'POST', path: '/api/auth/login', fields: ['password'] },
  { method: 'POST', path: '/api/auth/register', fields: ['password', 'confirmPassword'] },
  { method: 'POST', path: '/api/auth/forgot/reset', fields: ['newPassword'] },
  { method: 'PUT', path: '/api/user/password', fields: ['oldPassword', 'newPassword'] }
]

const client = axios.create({
  timeout: DEFAULT_TIMEOUT
})

let cachedRsaPublicKeyPem = null
let cachedRsaPublicKeyPromise = null

function handleErrorPayload(status, data) {
  if (typeof uni === 'undefined') {
    return
  }

  uni.hideLoading()

  switch (status) {
    case 401:
      removeStorage('token')
      removeStorage('userInfo')
      removeStorage('activeUserId')
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

function getHeaderValue(headers, key) {
  if (!headers) {
    return undefined
  }
  const targetKey = key.toLowerCase()
  const entries = Object.entries(headers)
  const matched = entries.find(([name]) => String(name).toLowerCase() === targetKey)
  return matched ? matched[1] : undefined
}

function normalizeMethod(method) {
  return String(method || 'GET').toUpperCase()
}

function normalizeUrl(url) {
  return new URL(String(url), BASE_URL)
}

function isApiStylePayload(value) {
  return Boolean(
    value &&
    typeof value === 'object' &&
    !Array.isArray(value) &&
    Object.prototype.hasOwnProperty.call(value, 'code') &&
    Object.prototype.hasOwnProperty.call(value, 'data')
  )
}

function toSuccessPayload(rawResult, message = '请求成功') {
  if (isApiStylePayload(rawResult)) {
    return rawResult
  }
  return {
    code: SUCCESS_CODE,
    message,
    data: rawResult
  }
}

function toErrorPayload(status, rawResult) {
  if (isApiStylePayload(rawResult)) {
    return rawResult
  }
  return {
    code: Number(status || 500),
    message: typeof rawResult === 'string' ? rawResult : (rawResult?.message || '请求失败'),
    data: null
  }
}

function getRsaPublicKeyFromStorage() {
  try {
    if (typeof window !== 'undefined' && window.sessionStorage) {
      return window.sessionStorage.getItem(RSA_PUBLIC_KEY_STORAGE_KEY)
    }
  } catch {}
  return null
}

function setRsaPublicKeyToStorage(pem) {
  try {
    if (typeof window !== 'undefined' && window.sessionStorage) {
      window.sessionStorage.setItem(RSA_PUBLIC_KEY_STORAGE_KEY, pem)
    }
  } catch {}
}

function clearRsaPublicKeyCache() {
  cachedRsaPublicKeyPem = null
  cachedRsaPublicKeyPromise = null
  try {
    if (typeof window !== 'undefined' && window.sessionStorage) {
      window.sessionStorage.removeItem(RSA_PUBLIC_KEY_STORAGE_KEY)
    }
  } catch {}
}

async function fetchRsaPublicKey() {
  if (cachedRsaPublicKeyPem) {
    return cachedRsaPublicKeyPem
  }

  const storedPublicKey = getRsaPublicKeyFromStorage()
  if (storedPublicKey) {
    cachedRsaPublicKeyPem = storedPublicKey
    return storedPublicKey
  }

  if (cachedRsaPublicKeyPromise) {
    return cachedRsaPublicKeyPromise
  }

  cachedRsaPublicKeyPromise = (async () => {
    const response = await client.get(`${BASE_URL}/api/auth/rsa-public-key`, {
      headers: {
        Accept: 'application/json'
      },
      validateStatus: () => true
    })

    const payload = response.status >= 200 && response.status < 300
      ? toSuccessPayload(response.data)
      : toErrorPayload(response.status, response.data)

    const publicKey = payload?.data
    if (payload.code !== SUCCESS_CODE || !publicKey) {
      throw new Error(payload?.message || '获取 RSA 公钥失败')
    }

    cachedRsaPublicKeyPem = publicKey
    setRsaPublicKeyToStorage(publicKey)
    return publicKey
  })()

  try {
    return await cachedRsaPublicKeyPromise
  } finally {
    cachedRsaPublicKeyPromise = null
  }
}

function pemToArrayBuffer(pem) {
  const cleaned = String(pem || '')
    .replace(/-----BEGIN PUBLIC KEY-----/g, '')
    .replace(/-----END PUBLIC KEY-----/g, '')
    .replace(/\s+/g, '')
  const binary = atob(cleaned)
  const bytes = new Uint8Array(binary.length)
  for (let index = 0; index < binary.length; index += 1) {
    bytes[index] = binary.charCodeAt(index)
  }
  return bytes.buffer
}

function arrayBufferToBase64(buffer) {
  const bytes = new Uint8Array(buffer)
  let binary = ''
  for (let index = 0; index < bytes.length; index += 1) {
    binary += String.fromCharCode(bytes[index])
  }
  return btoa(binary)
}

async function encryptPasswordValue(value) {
  if (typeof value !== 'string' || !value || value.startsWith(ENCRYPTED_PASSWORD_PREFIX)) {
    return value
  }
  if (!globalThis.crypto?.subtle) {
    throw new Error('当前环境不支持 RSA 密码加密')
  }

  const publicKey = await fetchRsaPublicKey()
  const cryptoKey = await globalThis.crypto.subtle.importKey(
    'spki',
    pemToArrayBuffer(publicKey),
    {
      name: 'RSA-OAEP',
      hash: 'SHA-256'
    },
    false,
    ['encrypt']
  )
  const encoded = new TextEncoder().encode(value)
  const encrypted = await globalThis.crypto.subtle.encrypt({ name: 'RSA-OAEP' }, cryptoKey, encoded)
  return `${ENCRYPTED_PASSWORD_PREFIX}${arrayBufferToBase64(encrypted)}`
}

function getSensitivePasswordFields(url, method) {
  const pathname = normalizeUrl(url).pathname
  const matchedRule = SENSITIVE_PASSWORD_RULES.find((rule) => rule.method === method && rule.path === pathname)
  return matchedRule ? matchedRule.fields : []
}

async function secureSensitiveRequest(config) {
  const fields = getSensitivePasswordFields(config.url, config.method)
  if (fields.length === 0 || !config.data || config.data instanceof FormData || typeof config.data === 'string') {
    return config
  }

  const securedData = { ...config.data }
  for (const field of fields) {
    if (field in securedData) {
      securedData[field] = await encryptPasswordValue(securedData[field])
    }
  }

  return {
    ...config,
    data: securedData
  }
}

function isRsaDecryptFailure(payload) {
  const message = String(payload?.message || '')
  return payload?.code === 400 && (message.includes('解密失败') || message.includes('密码解密失败'))
}

async function coreRequest(config = {}) {
  const requestConfig = {
    ...config,
    method: normalizeMethod(config.method),
    timeout: config.timeout || DEFAULT_TIMEOUT
  }

  const headers = {
    ...DEFAULT_HEADERS,
    ...(requestConfig.headers || {})
  }

  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const securedRequest = await secureSensitiveRequest({
    ...requestConfig,
    headers
  })

  const requestHeaders = { ...(securedRequest.headers || {}) }
  if (securedRequest.data instanceof FormData) {
    delete requestHeaders['Content-Type']
    delete requestHeaders['content-type']
  } else {
    const contentType = String(getHeaderValue(requestHeaders, 'Content-Type') || '')
    if (!contentType) {
      requestHeaders['Content-Type'] = 'application/json'
    }
  }

  const execute = async (currentRequest) => {
    const response = await client.request({
      url: currentRequest.url,
      method: currentRequest.method,
      params: currentRequest.params,
      data: currentRequest.data,
      headers: requestHeaders,
      timeout: currentRequest.timeout,
      validateStatus: () => true
    })

    return response.status >= 200 && response.status < 300
      ? toSuccessPayload(response.data)
      : toErrorPayload(response.status, response.data)
  }

  let payload = await execute(securedRequest)
  if (isRsaDecryptFailure(payload)) {
    clearRsaPublicKeyCache()
    const retriedRequest = await secureSensitiveRequest({
      ...requestConfig,
      headers
    })
    payload = await execute(retriedRequest)
  }

  if (payload.code === SUCCESS_CODE) {
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

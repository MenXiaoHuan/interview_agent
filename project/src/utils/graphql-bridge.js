import { BASE_URL, GRAPHQL_URL } from '@/utils/api-config'

const SUCCESS_CODE = 200
const JSON_HEADERS = {
  'Content-Type': 'application/json',
  'Accept': 'application/json'
}

const USER_FIELDS = `
  id
  userId: id
  username
  nickname
  avatarUrl
  avatar: avatarUrl
  userType
  email
  phone
  gender
  status
  eyeCareMode
  surpriseMode
  lastLoginAt
  createdAt
  updatedAt
`

const JOB_CATEGORY_FIELDS = `
  id
  name
  level
  parentId
  sortOrder
  status
  createdAt
  updatedAt
  children {
    id
    name
    level
    parentId
    sortOrder
    status
    createdAt
    updatedAt
  }
`

const JOB_FIELDS = `
  id
  categoryId
  name
  description
  requirements
  status
  createdAt
  updatedAt
  category {
    id
    name
    level
    parentId
    sortOrder
    status
    createdAt
    updatedAt
  }
`

const CHOICE_QUESTION_FIELDS = `
  id
  jobId
  question
  options
  correctAnswer
  explanation
  difficulty
  score
  createdAt
  updatedAt
`

const SCENARIO_QUESTION_FIELDS = `
  id
  jobId
  question
  createdAt
  updatedAt
`

const INTERVIEW_RESULT_FIELDS = `
  totalScore
  correctCount
  correctRate
  questions {
    id
    question
    options
    correctAnswer
    userAnswer
    explanation
  }
`

const INTERVIEW_RECORD_FIELDS = `
  id
  userId
  jobId
  totalScore
  correctCount
  correctRate
  duration
  status
  evaluationType
  createdAt
  updatedAt
`

const INTERVIEW_SUBMIT_FIELDS = `
  interviewId
  totalScore
  correctCount
  correctRate
  questions {
    questionId
    question
    userAnswer
    correctAnswer
    isCorrect
    score
  }
`

const RESUME_HISTORY_FIELDS = `
  id
  userId
  jobId
  resumeContent
  score
  advantages
  disadvantages
  improvementSuggestions
  professionalSkills
  projectExecution
  innovation
  communication
  adaptability
  createdAt
  updatedAt
  evaluationType
`

const SCENARIO_ANALYSIS_FIELDS = `
  id
  userId
  totalScore
  score1
  suggestion1
  score2
  suggestion2
  score3
  suggestion3
  score4
  suggestion4
  score5
  suggestion5
  evaluationType
  createTime
  updateTime
`

const COMPREHENSIVE_RESUME_FIELDS = `
  id
  userId
  jobId
  fileName
  fileSize
  fileType
  uploadTime
  overallScore
  skillMatch
  experienceMatch
  educationMatch
  communicationSkill
  leadershipSkill
  aiAnalysis
  aiSuggestions
`

const COMPREHENSIVE_QUESTION_FIELDS = `
  id
  userId
  jobId
  startTime
  endTime
  totalTime
  overallScore
  aiSuggestions
`

const COMPREHENSIVE_SCENARIO_FIELDS = `
  id
  userId
  jobId
  startTime
  endTime
  totalTime
  overallScore
  fluencyScore
  emotionScore
  relevanceScore
  adaptabilityScore
  aiAssessment
`

const COMPREHENSIVE_REPORT_FIELDS = `
  id
  userId
  jobId
  jobTitle
  overallScore
  resumeScore
  questionScore
  scenarioScore
  strengthAnalysis
  improvementAnalysis
  learningRoute
  createTime
  job {
    id
    categoryId
    name
    description
    requirements
    status
    createdAt
    updatedAt
  }
`

const COMPREHENSIVE_REPORT_HISTORY_FIELDS = `
  id
  userId
  jobId
  overallScore
  resumeScore
  questionScore
  scenarioScore
  strengthAnalysis
  improvementAnalysis
  learningRoute
  createTime
  job {
    id
    categoryId
    name
    description
    requirements
    status
    createdAt
    updatedAt
  }
`

const FEEDBACK_ITEM_FIELDS = `
  id
  userId
  nickname
  type
  title
  content
  status
  createdAt
  updatedAt
`

const FEEDBACK_FIELDS = `
  id
  userId
  type
  title
  content
  status
  reply
  replyTime
  adminId
  adminName
  createdAt
  updatedAt
`

const FEEDBACK_STATUS_FIELDS = `
  id
  title
  status
  reply
  replyTime
  updatedAt
`

const BLESSING_FIELDS = `
  id
  userId
  content
  type
  status
  createdAt
  updatedAt
`

const BLESSING_SUMMARY_FIELDS = `
  id
  content
  type
  status
  createdAt
  updatedAt
`

const CHAT_MESSAGE_FIELDS = `
  id
  userId
  content
  createdAt
`

const CHAT_MESSAGE_VIEW_FIELDS = `
  id
  userId
  nickname
  avatarUrl
  content
  createdAt
`

const AGENT_CONVERSATION_MESSAGE_RESPONSE_FIELDS = `
  type
  content
  time
`

const AGENT_CONVERSATION_RESPONSE_FIELDS = `
  id
  jobId
  agentKey
  title
  preview
  hasInteractedWithAssistant
  hasShownWelcomeMessage
  selectedEvaluationMode
  assistantActions
  eventLogs
  aiMessages {
    ${AGENT_CONVERSATION_MESSAGE_RESPONSE_FIELDS}
  }
  comprehensiveState
  firstQuestionAt
  createdAt
  updatedAt
`

const RANKING_FIELDS = `
  userId
  nickname
  avatarUrl
  score
  rank
`

const RESET_CODE_FIELDS = `
  contact
  channel
  code
  createdAt
  expiresAt
`

const bridgeRuntime = typeof globalThis !== 'undefined' ? globalThis : null
const ORIGINAL_FETCH_KEY = '__INTERVIEW_AGENT_ORIGINAL_FETCH__'
const ORIGINAL_UNI_REQUEST_KEY = '__INTERVIEW_AGENT_ORIGINAL_UNI_REQUEST__'

const originalFetch = (() => {
  if (!bridgeRuntime) {
    return null
  }
  if (typeof bridgeRuntime[ORIGINAL_FETCH_KEY] === 'function') {
    return bridgeRuntime[ORIGINAL_FETCH_KEY]
  }
  const nativeFetch = typeof bridgeRuntime.fetch === 'function'
    ? bridgeRuntime.fetch.bind(bridgeRuntime)
    : null
  if (nativeFetch) {
    bridgeRuntime[ORIGINAL_FETCH_KEY] = nativeFetch
  }
  return nativeFetch
})()

let originalUniRequest = bridgeRuntime?.[ORIGINAL_UNI_REQUEST_KEY] || null
const ENCRYPTED_PASSWORD_PREFIX = 'RSA:'
const RSA_PUBLIC_KEY_STORAGE_KEY = 'rsaPublicKeyPem'
const RSA_PUBLIC_KEY_QUERY = `
  query RsaPublicKey {
    rsaPublicKey
  }
`
const SENSITIVE_PASSWORD_RULES = [
  { method: 'POST', path: '/api/auth/login', fields: ['password'] },
  { method: 'POST', path: '/api/auth/register', fields: ['password', 'confirmPassword'] },
  { method: 'POST', path: '/api/auth/forgot/reset', fields: ['newPassword'] },
  { method: 'PUT', path: '/api/user/password', fields: ['oldPassword', 'newPassword'] }
]

let cachedRsaPublicKeyPem = null
let cachedRsaPublicKeyPromise = null

function getHeaderValue(headers, key) {
  if (!headers) {
    return undefined
  }
  const targetKey = key.toLowerCase()
  const entries = headers instanceof Headers
    ? Array.from(headers.entries())
    : Object.entries(headers)
  const matched = entries.find(([name]) => String(name).toLowerCase() === targetKey)
  return matched ? matched[1] : undefined
}

function normalizeHeaders(headers) {
  if (!headers) {
    return {}
  }
  if (headers instanceof Headers) {
    return Object.fromEntries(headers.entries())
  }
  return { ...headers }
}

function normalizeMethod(method) {
  return String(method || 'GET').toUpperCase()
}

function normalizeUrl(url) {
  if (url instanceof URL) {
    return url
  }
  return new URL(String(url), BASE_URL)
}

function shouldBridge(url) {
  const { pathname } = normalizeUrl(url)
  if (pathname === '/graphql') {
    return false
  }
  if (pathname.startsWith('/api/transcription/')) {
    return false
  }
  if (pathname === '/api/resume/extract') {
    return false
  }
  return pathname.startsWith('/api/') || pathname.startsWith('/scenario/')
}

function normalizeBody(body, headers) {
  if (body == null) {
    return undefined
  }
  if (body instanceof FormData) {
    return body
  }
  if (typeof body !== 'string') {
    return body
  }
  const contentType = String(getHeaderValue(headers, 'Content-Type') || '')
  if (contentType.includes('application/json')) {
    try {
      return JSON.parse(body)
    } catch {
      return body
    }
  }
  return body
}

function normalizeRequestConfig(config) {
  return {
    url: config.url,
    method: normalizeMethod(config.method),
    params: config.params || {},
    data: config.data,
    headers: normalizeHeaders(config.headers || config.header),
    timeout: config.timeout
  }
}

function getSensitivePasswordFields(pathname, method) {
  const matchedRule = SENSITIVE_PASSWORD_RULES.find((rule) => rule.method === method && rule.path === pathname)
  return matchedRule ? matchedRule.fields : []
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
  if (!originalFetch) {
    throw new Error('当前环境不支持获取 RSA 公钥')
  }

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
    const response = await originalFetch(GRAPHQL_URL, {
      method: 'POST',
      headers: {
        ...JSON_HEADERS
      },
      body: JSON.stringify({
        query: RSA_PUBLIC_KEY_QUERY,
        operationName: 'RsaPublicKey'
      })
    })

    const json = await response.json()
    const publicKey = json?.data?.rsaPublicKey
    if (!response.ok || !publicKey) {
      throw new Error(json?.errors?.[0]?.message || '获取 RSA 公钥失败')
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

async function secureSensitiveRequest(config) {
  const pathname = normalizeUrl(config.url).pathname
  const fields = getSensitivePasswordFields(pathname, config.method)
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

function tryParseJsonString(value) {
  if (typeof value !== 'string') {
    return value
  }

  const trimmed = value.trim()
  if (!trimmed || (!trimmed.startsWith('{') && !trimmed.startsWith('['))) {
    return value
  }

  try {
    return JSON.parse(trimmed)
  } catch (_) {
    return value
  }
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

function toGraphQlPayload(rawResult, message = '请求成功') {
  const normalizedResult = tryParseJsonString(rawResult)
  if (isApiStylePayload(normalizedResult)) {
    return normalizedResult
  }

  return {
    code: SUCCESS_CODE,
    message,
    data: normalizedResult
  }
}

function toErrorPayload(code, message) {
  return {
    code,
    message: message || '请求失败',
    data: null
  }
}

function buildUrlSearchParams(urlObject, extraParams = {}) {
  const params = Object.fromEntries(urlObject.searchParams.entries())
  return { ...params, ...(extraParams || {}) }
}

function buildGraphQlOperation(config) {
  const urlObject = normalizeUrl(config.url)
  const path = urlObject.pathname
  const method = normalizeMethod(config.method)
  const params = buildUrlSearchParams(urlObject, config.params)
  const data = config.data

  if (method === 'POST' && path === '/api/auth/login') {
    return {
      operationName: 'login',
      query: `
        mutation Login($input: LoginInput!) {
          login(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'POST' && path === '/api/auth/register') {
    return {
      operationName: 'register',
      query: `
        mutation Register($input: RegisterInput!) {
          register(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'POST' && path === '/api/auth/forgot/send-code') {
    return {
      operationName: 'sendResetCode',
      query: `
        mutation SendResetCode($input: ForgotSendCodeInput!) {
          sendResetCode(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'POST' && path === '/api/auth/forgot/reset') {
    return {
      operationName: 'resetPassword',
      query: `
        mutation ResetPassword($input: ForgotResetInput!) {
          resetPassword(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'GET' && path === '/api/auth/forgot/code') {
    return {
      operationName: 'latestResetCode',
      query: `
        query LatestResetCode($contact: String!) {
          latestResetCode(contact: $contact) {
            ${RESET_CODE_FIELDS}
          }
        }
      `,
      variables: { contact: params.contact }
    }
  }

  if (method === 'GET' && path === '/api/auth/forgot/email-code') {
    return {
      operationName: 'latestResetCode',
      query: `
        query LatestResetCode($contact: String!) {
          latestResetCode(contact: $contact) {
            ${RESET_CODE_FIELDS}
          }
        }
      `,
      variables: { contact: params.email }
    }
  }

  if (method === 'GET' && path === '/api/auth/forgot/phone-code') {
    return {
      operationName: 'latestResetCode',
      query: `
        query LatestResetCode($contact: String!) {
          latestResetCode(contact: $contact) {
            ${RESET_CODE_FIELDS}
          }
        }
      `,
      variables: { contact: params.phone }
    }
  }

  if (method === 'GET' && path === '/api/user/profile') {
    return {
      operationName: 'me',
      query: `
        query CurrentUser {
          me {
            ${USER_FIELDS}
          }
        }
      `,
      variables: {}
    }
  }

  if (method === 'PUT' && path === '/api/user/eyemode') {
    return {
      operationName: 'updateEyeMode',
      query: `
        mutation UpdateEyeMode($input: UpdateEyeModeInput!) {
          updateEyeMode(input: $input) {
            eyeCareMode
          }
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'PUT' && path === '/api/user/surprisemode') {
    return {
      operationName: 'updateSurpriseMode',
      query: `
        mutation UpdateSurpriseMode($input: UpdateSurpriseModeInput!) {
          updateSurpriseMode(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'PUT' && path === '/api/user/nickname') {
    return {
      operationName: 'updateNickname',
      query: `
        mutation UpdateNickname($input: UpdateNicknameInput!) {
          updateNickname(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  if ((method === 'POST' && path === '/api/user/avatar/upload') || (method === 'POST' && path === '/api/user/avatar')) {
    return {
      operationName: 'updateAvatar',
      query: `
        mutation UpdateAvatar($input: UpdateAvatarInput!) {
          updateAvatar(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'PUT' && path === '/api/user/password') {
    return {
      operationName: 'updatePassword',
      query: `
        mutation UpdatePassword($input: UpdatePasswordInput!) {
          updatePassword(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'PUT' && path === '/api/user/email') {
    return {
      operationName: 'bindEmail',
      query: `
        mutation BindEmail($input: BindEmailInput!) {
          bindEmail(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'PUT' && path === '/api/user/phone') {
    return {
      operationName: 'bindPhone',
      query: `
        mutation BindPhone($input: BindPhoneInput!) {
          bindPhone(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'PUT' && path === '/api/user/status') {
    return {
      operationName: 'updateStatus',
      query: `
        mutation UpdateStatus($input: UpdateStatusInput!) {
          updateStatus(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'PUT' && path === '/api/user/userType') {
    return {
      operationName: 'updateUserType',
      query: `
        mutation UpdateUserType($input: UpdateUserTypeInput!) {
          updateUserType(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'PUT' && path === '/api/user/gender') {
    return {
      operationName: 'updateGender',
      query: `
        mutation UpdateGender($input: UpdateGenderInput!) {
          updateGender(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'GET' && path === '/api/user/list') {
    return {
      operationName: 'users',
      query: `
        query Users($page: Int!, $size: Int!) {
          users(page: $page, size: $size) {
            ${USER_FIELDS}
          }
        }
      `,
      variables: {
        page: Number(params.page || 1),
        size: Number(params.size || 10)
      }
    }
  }

  const deleteUserMatch = path.match(/^\/api\/user\/delete\/([^/]+)$/)
  if (method === 'DELETE' && deleteUserMatch) {
    return {
      operationName: 'deleteUser',
      query: `
        mutation DeleteUser($username: String!) {
          deleteUser(username: $username)
        }
      `,
      variables: { username: decodeURIComponent(deleteUserMatch[1]) }
    }
  }

  if (method === 'PUT' && path === '/api/user/update/nickname') {
    return {
      operationName: 'adminUpdateUserNickname',
      query: `
        mutation AdminUpdateUserNickname($username: String!, $nickname: String!) {
          adminUpdateUserNickname(username: $username, nickname: $nickname)
        }
      `,
      variables: {
        username: data.username,
        nickname: data.nickname
      }
    }
  }

  if (method === 'PUT' && path === '/api/user/update/email') {
    return {
      operationName: 'adminUpdateUserEmail',
      query: `
        mutation AdminUpdateUserEmail($username: String!, $email: String!) {
          adminUpdateUserEmail(username: $username, email: $email)
        }
      `,
      variables: {
        username: data.username,
        email: data.email
      }
    }
  }

  if (method === 'PUT' && path === '/api/user/update/phone') {
    return {
      operationName: 'adminUpdateUserPhone',
      query: `
        mutation AdminUpdateUserPhone($username: String!, $phone: String!) {
          adminUpdateUserPhone(username: $username, phone: $phone)
        }
      `,
      variables: {
        username: data.username,
        phone: data.phone
      }
    }
  }

  if (method === 'PUT' && path === '/api/user/update/gender') {
    return {
      operationName: 'adminUpdateUserGender',
      query: `
        mutation AdminUpdateUserGender($username: String!, $gender: Int!) {
          adminUpdateUserGender(username: $username, gender: $gender)
        }
      `,
      variables: {
        username: data.username,
        gender: Number(data.gender)
      }
    }
  }

  if (method === 'PUT' && path === '/api/user/update/userType') {
    return {
      operationName: 'adminUpdateUserType',
      query: `
        mutation AdminUpdateUserType($username: String!, $userType: Int!) {
          adminUpdateUserType(username: $username, userType: $userType)
        }
      `,
      variables: {
        username: data.username,
        userType: Number(data.userType)
      }
    }
  }

  if (method === 'PUT' && path === '/api/user/update/status') {
    return {
      operationName: 'adminUpdateUserStatus',
      query: `
        mutation AdminUpdateUserStatus($username: String!, $status: Int!) {
          adminUpdateUserStatus(username: $username, status: $status)
        }
      `,
      variables: {
        username: data.username,
        status: Number(data.status)
      }
    }
  }

  if (method === 'GET' && path === '/api/job-categories/tree') {
    return {
      operationName: 'jobCategoryTree',
      query: `
        query JobCategoryTree {
          jobCategoryTree {
            ${JOB_CATEGORY_FIELDS}
          }
        }
      `,
      variables: {}
    }
  }

  const categoryNameMatch = path.match(/^\/api\/job-categories\/([^/]+)\/name$/)
  if (method === 'GET' && categoryNameMatch) {
    return {
      operationName: 'jobCategoryName',
      query: `
        query JobCategoryName($categoryId: ID!) {
          jobCategoryName(categoryId: $categoryId)
        }
      `,
      variables: { categoryId: categoryNameMatch[1] }
    }
  }

  if (method === 'POST' && path === '/api/job-categories/first-level') {
    return {
      operationName: 'createFirstLevelCategory',
      query: `
        mutation CreateFirstLevelCategory($input: JobCategoryCreateInput!) {
          createFirstLevelCategory(input: $input) {
            ${JOB_CATEGORY_FIELDS}
          }
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'POST' && path === '/api/job-categories/second-level') {
    return {
      operationName: 'createSecondLevelCategory',
      query: `
        mutation CreateSecondLevelCategory($input: JobCategoryCreateInput!) {
          createSecondLevelCategory(input: $input) {
            ${JOB_CATEGORY_FIELDS}
          }
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'PUT' && path === '/api/job-categories') {
    return {
      operationName: 'updateCategory',
      query: `
        mutation UpdateCategory($input: JobCategoryUpdateInput!) {
          updateCategory(input: $input) {
            ${JOB_CATEGORY_FIELDS}
          }
        }
      `,
      variables: { input: data }
    }
  }

  const deleteFirstCategoryMatch = path.match(/^\/api\/job-categories\/first-level\/([^/]+)$/)
  if (method === 'DELETE' && deleteFirstCategoryMatch) {
    return {
      operationName: 'deleteFirstLevelCategory',
      query: `
        mutation DeleteFirstLevelCategory($categoryId: ID!) {
          deleteFirstLevelCategory(categoryId: $categoryId)
        }
      `,
      variables: { categoryId: deleteFirstCategoryMatch[1] }
    }
  }

  const deleteSecondCategoryMatch = path.match(/^\/api\/job-categories\/second-level\/([^/]+)$/)
  if (method === 'DELETE' && deleteSecondCategoryMatch) {
    return {
      operationName: 'deleteSecondLevelCategory',
      query: `
        mutation DeleteSecondLevelCategory($categoryId: ID!) {
          deleteSecondLevelCategory(categoryId: $categoryId)
        }
      `,
      variables: { categoryId: deleteSecondCategoryMatch[1] }
    }
  }

  if (method === 'GET' && path === '/api/job') {
    return {
      operationName: 'jobs',
      query: `
        query Jobs($categoryId: ID!) {
          jobs(categoryId: $categoryId) {
            ${JOB_FIELDS}
          }
        }
      `,
      variables: { categoryId: params.categoryId }
    }
  }

  const jobDetailMatch = path.match(/^\/api\/job\/([^/]+)$/)
  if (method === 'GET' && jobDetailMatch) {
    return {
      operationName: 'job',
      query: `
        query Job($id: ID!) {
          job(id: $id) {
            ${JOB_FIELDS}
          }
        }
      `,
      variables: { id: jobDetailMatch[1] }
    }
  }

  if (method === 'POST' && path === '/api/jobs') {
    return {
      operationName: 'createJob',
      query: `
        mutation CreateJob($input: JobCreateInput!) {
          createJob(input: $input) {
            ${JOB_FIELDS}
          }
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'PUT' && path === '/api/jobs') {
    return {
      operationName: 'updateJob',
      query: `
        mutation UpdateJob($input: JobUpdateInput!) {
          updateJob(input: $input) {
            ${JOB_FIELDS}
          }
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'GET' && path === '/api/jobs') {
    return {
      operationName: 'allJobs',
      query: `
        query AllJobs {
          allJobs {
            ${JOB_FIELDS}
          }
        }
      `,
      variables: {}
    }
  }

  const deleteJobMatch = path.match(/^\/api\/jobs\/([^/]+)$/)
  if (method === 'DELETE' && deleteJobMatch) {
    return {
      operationName: 'deleteJob',
      query: `
        mutation DeleteJob($jobId: ID!) {
          deleteJob(jobId: $jobId)
        }
      `,
      variables: { jobId: deleteJobMatch[1] }
    }
  }

  if (method === 'GET' && path === '/api/interview/question/getAll') {
    return {
      operationName: 'allChoiceQuestions',
      query: `
        query AllChoiceQuestions {
          allChoiceQuestions {
            ${CHOICE_QUESTION_FIELDS}
          }
        }
      `,
      variables: {}
    }
  }

  const choiceQuestionMatch = path.match(/^\/api\/interview\/question\/([^/]+)$/)
  if (method === 'GET' && choiceQuestionMatch) {
    return {
      operationName: 'choiceQuestions',
      query: `
        query ChoiceQuestions($jobId: ID!) {
          choiceQuestions(jobId: $jobId) {
            ${CHOICE_QUESTION_FIELDS}
          }
        }
      `,
      variables: { jobId: choiceQuestionMatch[1] }
    }
  }

  if (method === 'POST' && path === '/api/interview/question/add') {
    return {
      operationName: 'addChoiceQuestion',
      query: `
        mutation AddChoiceQuestion($input: ChoiceQuestionInput!) {
          addChoiceQuestion(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'PUT' && path === '/api/interview/question/update') {
    return {
      operationName: 'updateChoiceQuestion',
      query: `
        mutation UpdateChoiceQuestion($input: ChoiceQuestionInput!) {
          updateChoiceQuestion(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  const deleteChoiceQuestionMatch = path.match(/^\/api\/interview\/question\/delete\/([^/]+)$/)
  if (method === 'POST' && deleteChoiceQuestionMatch) {
    return {
      operationName: 'deleteChoiceQuestion',
      query: `
        mutation DeleteChoiceQuestion($id: ID!) {
          deleteChoiceQuestion(id: $id)
        }
      `,
      variables: { id: deleteChoiceQuestionMatch[1] }
    }
  }

  if (method === 'POST' && path === '/api/interview/submit') {
    return {
      operationName: 'submitInterviewAnswers',
      query: `
        mutation SubmitInterviewAnswers($input: InterviewSubmitInput!) {
          submitInterviewAnswers(input: $input) {
            ${INTERVIEW_SUBMIT_FIELDS}
          }
        }
      `,
      variables: { input: data }
    }
  }

  const interviewResultMatch = path.match(/^\/api\/interview\/result\/([^/]+)$/)
  if (method === 'GET' && interviewResultMatch) {
    return {
      operationName: 'interviewResult',
      query: `
        query InterviewResult($interviewQuestionRecordId: ID!) {
          interviewResult(interviewQuestionRecordId: $interviewQuestionRecordId) {
            ${INTERVIEW_RESULT_FIELDS}
          }
        }
      `,
      variables: { interviewQuestionRecordId: interviewResultMatch[1] }
    }
  }

  const interviewHistoryByJobMatch = path.match(/^\/api\/interview\/question\/history\/([^/]+)\/job\/([^/]+)$/)
  if (method === 'GET' && interviewHistoryByJobMatch) {
    return {
      operationName: 'interviewRecordsByJob',
      query: `
        query InterviewRecordsByJob($userId: ID!, $jobId: ID!, $evaluationType: String) {
          interviewRecordsByJob(userId: $userId, jobId: $jobId, evaluationType: $evaluationType) {
            ${INTERVIEW_RECORD_FIELDS}
          }
        }
      `,
      variables: {
        userId: interviewHistoryByJobMatch[1],
        jobId: interviewHistoryByJobMatch[2],
        evaluationType: params.evaluationType || 'SPECIAL'
      }
    }
  }

  const interviewHistoryMatch = path.match(/^\/api\/interview\/question\/history\/([^/]+)$/)
  if (method === 'GET' && interviewHistoryMatch) {
    return {
      operationName: 'interviewRecords',
      query: `
        query InterviewRecords($userId: ID!, $evaluationType: String) {
          interviewRecords(userId: $userId, evaluationType: $evaluationType) {
            ${INTERVIEW_RECORD_FIELDS}
          }
        }
      `,
      variables: {
        userId: interviewHistoryMatch[1],
        evaluationType: params.evaluationType || 'SPECIAL'
      }
    }
  }

  if (method === 'GET' && path === '/api/scenario-question/getAll') {
    return {
      operationName: 'allScenarioQuestions',
      query: `
        query AllScenarioQuestions {
          allScenarioQuestions {
            ${SCENARIO_QUESTION_FIELDS}
          }
        }
      `,
      variables: {}
    }
  }

  const scenarioQuestionMatch = path.match(/^\/api\/scenario-question\/get\/([^/]+)$/)
  if (method === 'GET' && scenarioQuestionMatch) {
    return {
      operationName: 'scenarioQuestions',
      query: `
        query ScenarioQuestions($jobId: ID!) {
          scenarioQuestions(jobId: $jobId) {
            ${SCENARIO_QUESTION_FIELDS}
          }
        }
      `,
      variables: { jobId: scenarioQuestionMatch[1] }
    }
  }

  if (method === 'POST' && path === '/api/scenario-question/create') {
    return {
      operationName: 'createScenarioQuestion',
      query: `
        mutation CreateScenarioQuestion($input: ScenarioQuestionInput!) {
          createScenarioQuestion(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'POST' && path === '/api/scenario-question/update') {
    return {
      operationName: 'updateScenarioQuestion',
      query: `
        mutation UpdateScenarioQuestion($input: ScenarioQuestionInput!) {
          updateScenarioQuestion(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  const deleteScenarioQuestionMatch = path.match(/^\/api\/scenario-question\/delete\/([^/]+)$/)
  if (method === 'POST' && deleteScenarioQuestionMatch) {
    return {
      operationName: 'deleteScenarioQuestion',
      query: `
        mutation DeleteScenarioQuestion($id: ID!) {
          deleteScenarioQuestion(id: $id)
        }
      `,
      variables: { id: deleteScenarioQuestionMatch[1] }
    }
  }

  if (method === 'POST' && path === '/api/resume/extract') {
    const file = data instanceof FormData ? data.get('file') : undefined
    return {
      operationName: 'extractResumeContent',
      query: `
        mutation ExtractResumeContent($file: Upload!) {
          extractResumeContent(file: $file)
        }
      `,
      variables: { file },
      multipart: true,
      fileVariableName: 'file'
    }
  }

  if (method === 'POST' && path === '/api/resume/save/history') {
    return {
      operationName: 'saveResumeAnalysisHistory',
      query: `
        mutation SaveResumeAnalysisHistory($input: SaveResumeHistoryInput!, $evaluationType: String) {
          saveResumeAnalysisHistory(input: $input, evaluationType: $evaluationType)
        }
      `,
      variables: {
        input: data,
        evaluationType: params.evaluationType || 'SPECIAL'
      }
    }
  }

  const resumeHistoryByUserMatch = path.match(/^\/api\/resume\/history\/([^/]+)$/)
  if (method === 'GET' && resumeHistoryByUserMatch) {
    return {
      operationName: 'resumeAnalysisHistories',
      query: `
        query ResumeAnalysisHistories($userId: ID!, $evaluationType: String) {
          resumeAnalysisHistories(userId: $userId, evaluationType: $evaluationType) {
            ${RESUME_HISTORY_FIELDS}
          }
        }
      `,
      variables: {
        userId: resumeHistoryByUserMatch[1],
        evaluationType: params.evaluationType || 'SPECIAL'
      }
    }
  }

  const resumeHistoryDetailMatch = path.match(/^\/api\/resume\/history\/id\/([^/]+)$/)
  if (method === 'GET' && resumeHistoryDetailMatch) {
    return {
      operationName: 'resumeAnalysisHistory',
      query: `
        query ResumeAnalysisHistory($resumeHistoryId: ID!) {
          resumeAnalysisHistory(resumeHistoryId: $resumeHistoryId) {
            ${RESUME_HISTORY_FIELDS}
          }
        }
      `,
      variables: { resumeHistoryId: resumeHistoryDetailMatch[1] }
    }
  }

  const restoreResumeMatch = path.match(/^\/api\/resume\/restore\/([^/]+)$/)
  if (method === 'POST' && restoreResumeMatch) {
    return {
      operationName: 'restoreResume',
      query: `
        mutation RestoreResume($resumeHistoryId: ID!) {
          restoreResume(resumeHistoryId: $resumeHistoryId) {
            resumeId
            content
          }
        }
      `,
      variables: { resumeHistoryId: restoreResumeMatch[1] }
    }
  }

  if (method === 'POST' && path === '/scenario/save/history') {
    return {
      operationName: 'saveScenarioAnalysisHistory',
      query: `
        mutation SaveScenarioAnalysisHistory($input: SaveScenarioAnalysisHistoryInput!, $evaluationType: String) {
          saveScenarioAnalysisHistory(input: $input, evaluationType: $evaluationType)
        }
      `,
      variables: {
        input: data,
        evaluationType: params.evaluationType || 'SPECIAL'
      }
    }
  }

  const scenarioHistoryMatch = path.match(/^\/scenario\/history\/([^/]+)$/)
  if (method === 'GET' && scenarioHistoryMatch) {
    return {
      operationName: 'scenarioAnalysisHistories',
      query: `
        query ScenarioAnalysisHistories($userId: String!, $evaluationType: String) {
          scenarioAnalysisHistories(userId: $userId, evaluationType: $evaluationType) {
            ${SCENARIO_ANALYSIS_FIELDS}
          }
        }
      `,
      variables: {
        userId: scenarioHistoryMatch[1],
        evaluationType: params.evaluationType || 'SPECIAL'
      }
    }
  }

  if (method === 'POST' && path === '/api/comprehensive-history/resume') {
    return {
      operationName: 'saveComprehensiveResumeHistory',
      query: `
        mutation SaveComprehensiveResumeHistory($input: ComprehensiveResumeHistoryInput!) {
          saveComprehensiveResumeHistory(input: $input) {
            ${COMPREHENSIVE_RESUME_FIELDS}
          }
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'POST' && path === '/api/comprehensive-history/question') {
    return {
      operationName: 'saveComprehensiveQuestionHistory',
      query: `
        mutation SaveComprehensiveQuestionHistory($input: ComprehensiveQuestionHistoryInput!) {
          saveComprehensiveQuestionHistory(input: $input) {
            ${COMPREHENSIVE_QUESTION_FIELDS}
          }
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'POST' && path === '/api/comprehensive-history/scenario') {
    return {
      operationName: 'saveComprehensiveScenarioHistory',
      query: `
        mutation SaveComprehensiveScenarioHistory($input: ComprehensiveScenarioHistoryInput!) {
          saveComprehensiveScenarioHistory(input: $input) {
            ${COMPREHENSIVE_SCENARIO_FIELDS}
          }
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'POST' && path === '/api/comprehensive-history/report') {
    return {
      operationName: 'saveComprehensiveReport',
      query: `
        mutation SaveComprehensiveReport($input: ComprehensiveReportInput!) {
          saveComprehensiveReport(input: $input) {
            ${COMPREHENSIVE_REPORT_HISTORY_FIELDS}
          }
        }
      `,
      variables: { input: data }
    }
  }

  const comprehensiveReportMatch = path.match(/^\/api\/comprehensive-history\/report\/([^/]+)$/)
  if (method === 'GET' && comprehensiveReportMatch) {
    return {
      operationName: 'comprehensiveReport',
      query: `
        query ComprehensiveReport($id: ID!) {
          comprehensiveReport(id: $id) {
            ${COMPREHENSIVE_REPORT_FIELDS}
          }
        }
      `,
      variables: { id: comprehensiveReportMatch[1] }
    }
  }

  const comprehensiveHistoryByUserMatch = path.match(/^\/api\/comprehensive-history\/reports\/user\/([^/]+)$/)
  if (method === 'GET' && comprehensiveHistoryByUserMatch) {
    return {
      operationName: 'userComprehensiveReports',
      query: `
        query UserComprehensiveReports($userId: ID!) {
          userComprehensiveReports(userId: $userId) {
            ${COMPREHENSIVE_REPORT_FIELDS}
          }
        }
      `,
      variables: { userId: comprehensiveHistoryByUserMatch[1] }
    }
  }

  if (method === 'POST' && path === '/api/feedback/submit') {
    return {
      operationName: 'submitFeedback',
      query: `
        mutation SubmitFeedback($input: FeedbackSubmitInput!) {
          submitFeedback(input: $input) {
            ${FEEDBACK_FIELDS}
          }
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'GET' && path === '/api/feedback') {
    return {
      operationName: 'feedbackList',
      query: `
        query FeedbackList {
          feedbackList {
            ${FEEDBACK_ITEM_FIELDS}
          }
        }
      `,
      variables: {}
    }
  }

  const feedbackDetailMatch = path.match(/^\/api\/feedback\/([^/]+)$/)
  if (method === 'GET' && feedbackDetailMatch) {
    return {
      operationName: 'feedback',
      query: `
        query Feedback($id: ID!) {
          feedback(id: $id) {
            ${FEEDBACK_FIELDS}
          }
        }
      `,
      variables: { id: feedbackDetailMatch[1] }
    }
  }

  const feedbackReplyMatch = path.match(/^\/api\/feedback\/([^/]+)\/reply$/)
  if (method === 'POST' && feedbackReplyMatch) {
    return {
      operationName: 'replyFeedback',
      query: `
        mutation ReplyFeedback($input: FeedbackReplyInput!) {
          replyFeedback(input: $input) {
            ${FEEDBACK_FIELDS}
          }
        }
      `,
      variables: {
        input: {
          ...(data || {}),
          id: Number(feedbackReplyMatch[1])
        }
      }
    }
  }

  if (method === 'GET' && path === '/api/feedback/my/status') {
    return {
      operationName: 'myFeedbackStatus',
      query: `
        query MyFeedbackStatus {
          myFeedbackStatus {
            ${FEEDBACK_STATUS_FIELDS}
          }
        }
      `,
      variables: {}
    }
  }

  if (method === 'GET' && path === '/api/blessings') {
    return {
      operationName: 'blessings',
      query: `
        query Blessings($status: String, $type: String) {
          blessings(status: $status, type: $type) {
            ${BLESSING_SUMMARY_FIELDS}
          }
        }
      `,
      variables: {
        status: params.status || null,
        type: params.type || null
      }
    }
  }

  if (method === 'POST' && path === '/api/blessings') {
    return {
      operationName: 'createBlessing',
      query: `
        mutation CreateBlessing($input: BlessingCreateInput!) {
          createBlessing(input: $input) {
            ${BLESSING_FIELDS}
          }
        }
      `,
      variables: { input: data }
    }
  }

  const blessingDetailMatch = path.match(/^\/api\/blessings\/([^/]+)$/)
  if (method === 'GET' && blessingDetailMatch) {
    return {
      operationName: 'blessing',
      query: `
        query Blessing($id: ID!) {
          blessing(id: $id) {
            ${BLESSING_FIELDS}
          }
        }
      `,
      variables: { id: blessingDetailMatch[1] }
    }
  }

  if (method === 'PUT' && blessingDetailMatch) {
    return {
      operationName: 'updateBlessing',
      query: `
        mutation UpdateBlessing($id: ID!, $input: BlessingUpdateInput!) {
          updateBlessing(id: $id, input: $input) {
            ${BLESSING_FIELDS}
          }
        }
      `,
      variables: {
        id: blessingDetailMatch[1],
        input: data
      }
    }
  }

  if (method === 'DELETE' && blessingDetailMatch) {
    return {
      operationName: 'deleteBlessing',
      query: `
        mutation DeleteBlessing($id: ID!) {
          deleteBlessing(id: $id)
        }
      `,
      variables: { id: blessingDetailMatch[1] }
    }
  }

  if (method === 'POST' && path === '/api/chat/message') {
    return {
      operationName: 'saveChatMessage',
      query: `
        mutation SaveChatMessage($input: ChatMessageSaveInput!) {
          saveChatMessage(input: $input) {
            ${CHAT_MESSAGE_FIELDS}
          }
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'GET' && (path === '/api/chat/messages' || path === '/api/chat/messages/1d')) {
    return {
      operationName: 'chatMessages',
      query: `
        query ChatMessages($days: Int) {
          chatMessages(days: $days) {
            ${CHAT_MESSAGE_VIEW_FIELDS}
          }
        }
      `,
      variables: { days: Number(params.days || 1) }
    }
  }

  if (method === 'GET' && path === '/api/agent-conversations') {
    return {
      operationName: 'agentConversations',
      query: `
        query AgentConversations($agentKey: String) {
          agentConversations(agentKey: $agentKey) {
            ${AGENT_CONVERSATION_RESPONSE_FIELDS}
          }
        }
      `,
      variables: {
        agentKey: params.agentKey || null
      }
    }
  }

  if (method === 'POST' && path === '/api/agent-conversations/session') {
    return {
      operationName: 'upsertAgentConversation',
      query: `
        mutation UpsertAgentConversation($input: AgentConversationUpsertRequest!) {
          upsertAgentConversation(input: $input) {
            ${AGENT_CONVERSATION_RESPONSE_FIELDS}
          }
        }
      `,
      variables: { input: data }
    }
  }

  const agentConversationDeleteMatch = path.match(/^\/api\/agent-conversations\/([^/]+)$/)
  if (method === 'DELETE' && agentConversationDeleteMatch) {
    let decodedSessionId = agentConversationDeleteMatch[1]
    try {
      decodedSessionId = decodeURIComponent(decodedSessionId)
    } catch (_) {}
    return {
      operationName: 'deleteAgentConversation',
      query: `
        mutation DeleteAgentConversation($sessionId: ID!) {
          deleteAgentConversation(sessionId: $sessionId)
        }
      `,
      variables: { sessionId: decodedSessionId }
    }
  }

  if (method === 'GET' && path === '/api/rank/comprehensive/resume') {
    return {
      operationName: 'comprehensiveResumeRankings',
      query: `
        query ComprehensiveResumeRankings($jobId: ID, $size: Int) {
          comprehensiveResumeRankings(jobId: $jobId, size: $size) {
            ${RANKING_FIELDS}
          }
        }
      `,
      variables: {
        jobId: params.jobId || null,
        size: Number(params.size || 50)
      }
    }
  }

  if (method === 'GET' && path === '/api/rank/comprehensive/question') {
    return {
      operationName: 'comprehensiveQuestionRankings',
      query: `
        query ComprehensiveQuestionRankings($jobId: ID, $size: Int) {
          comprehensiveQuestionRankings(jobId: $jobId, size: $size) {
            ${RANKING_FIELDS}
          }
        }
      `,
      variables: {
        jobId: params.jobId || null,
        size: Number(params.size || 50)
      }
    }
  }

  if (method === 'GET' && path === '/api/rank/comprehensive/scenario') {
    return {
      operationName: 'comprehensiveScenarioRankings',
      query: `
        query ComprehensiveScenarioRankings($jobId: ID, $size: Int) {
          comprehensiveScenarioRankings(jobId: $jobId, size: $size) {
            ${RANKING_FIELDS}
          }
        }
      `,
      variables: {
        jobId: params.jobId || null,
        size: Number(params.size || 50)
      }
    }
  }

  if (method === 'GET' && path === '/api/rank/specialized/resume') {
    return {
      operationName: 'specializedResumeRankings',
      query: `
        query SpecializedResumeRankings($jobId: ID, $evaluationType: String, $size: Int) {
          specializedResumeRankings(jobId: $jobId, evaluationType: $evaluationType, size: $size) {
            ${RANKING_FIELDS}
          }
        }
      `,
      variables: {
        jobId: params.jobId || null,
        evaluationType: params.evaluationType || null,
        size: Number(params.size || 50)
      }
    }
  }

  if (method === 'GET' && path === '/api/rank/specialized/question') {
    return {
      operationName: 'specializedQuestionRankings',
      query: `
        query SpecializedQuestionRankings($jobId: ID, $evaluationType: String, $size: Int) {
          specializedQuestionRankings(jobId: $jobId, evaluationType: $evaluationType, size: $size) {
            ${RANKING_FIELDS}
          }
        }
      `,
      variables: {
        jobId: params.jobId || null,
        evaluationType: params.evaluationType || null,
        size: Number(params.size || 50)
      }
    }
  }

  if (method === 'GET' && path === '/api/rank/specialized/scenario') {
    return {
      operationName: 'specializedScenarioRankings',
      query: `
        query SpecializedScenarioRankings($evaluationType: String, $size: Int) {
          specializedScenarioRankings(evaluationType: $evaluationType, size: $size) {
            ${RANKING_FIELDS}
          }
        }
      `,
      variables: {
        evaluationType: params.evaluationType || null,
        size: Number(params.size || 50)
      }
    }
  }

  if (method === 'POST' && path === '/api/speech/synthesize') {
    return {
      operationName: 'synthesizeSpeech',
      query: `
        mutation SynthesizeSpeech($input: SpeechSynthesisInput!) {
          synthesizeSpeech(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  if (method === 'POST' && path === '/api/transcription/upload') {
    const file = data instanceof FormData ? data.get('file') : undefined
    return {
      operationName: 'uploadAudio',
      query: `
        mutation UploadAudio($file: Upload!) {
          uploadAudio(file: $file)
        }
      `,
      variables: { file },
      multipart: true,
      fileVariableName: 'file'
    }
  }

  if (method === 'POST' && path === '/api/transcription/create-task') {
    const audioUrl = data instanceof FormData ? data.get('audioUrl') : data.audioUrl
    return {
      operationName: 'createTranscriptionTask',
      query: `
        mutation CreateTranscriptionTask($audioUrl: String!) {
          createTranscriptionTask(audioUrl: $audioUrl)
        }
      `,
      variables: { audioUrl }
    }
  }

  const queryTaskMatch = path.match(/^\/api\/transcription\/query-task\/([^/]+)$/)
  if (method === 'GET' && queryTaskMatch) {
    return {
      operationName: 'queryTranscriptionTask',
      query: `
        query QueryTranscriptionTask($taskId: String!) {
          queryTranscriptionTask(taskId: $taskId)
        }
      `,
      variables: { taskId: queryTaskMatch[1] }
    }
  }

  if (method === 'POST' && path === '/api/face/getAnalysis') {
    const file = data instanceof FormData ? data.get('file') : undefined
    return {
      operationName: 'analyzeFace',
      query: `
        mutation AnalyzeFace($file: Upload!) {
          analyzeFace(file: $file)
        }
      `,
      variables: { file },
      multipart: true,
      fileVariableName: 'file'
    }
  }

  if (method === 'POST' && path === '/api/xunfei/getAgentAnswer') {
    return {
      operationName: 'getAgentAnswer',
      query: `
        mutation GetAgentAnswer($input: AgentInput!) {
          getAgentAnswer(input: $input)
        }
      `,
      variables: { input: data }
    }
  }

  return null
}

async function fetchGraphQl(operation, headers = {}) {
  if (!originalFetch) {
    throw new Error('当前环境不支持 fetch')
  }

  const mergedHeaders = {
    ...JSON_HEADERS,
    ...headers
  }

  delete mergedHeaders['Content-Type']
  delete mergedHeaders['content-type']

  const response = await originalFetch(GRAPHQL_URL, {
    method: 'POST',
    headers: {
      ...mergedHeaders,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      query: operation.query,
      variables: operation.variables
    })
  })

  return response.json()
}

async function fetchGraphQlMultipart(operation, headers = {}) {
  if (!originalFetch) {
    throw new Error('当前环境不支持 fetch')
  }

  const formData = new FormData()
  const operationsPayload = {
    query: operation.query,
    variables: {
      ...(operation.variables || {}),
      [operation.fileVariableName]: null
    }
  }

  formData.append('operations', JSON.stringify(operationsPayload))
  formData.append('map', JSON.stringify({
    '0': [`variables.${operation.fileVariableName}`]
  }))
  formData.append('0', operation.variables[operation.fileVariableName])

  const mergedHeaders = {
    ...headers
  }
  delete mergedHeaders['Content-Type']
  delete mergedHeaders['content-type']

  const response = await originalFetch(GRAPHQL_URL, {
    method: 'POST',
    headers: mergedHeaders,
    body: formData
  })

  return response.json()
}

function readGraphQlResult(json, operation) {
  if (json && Array.isArray(json.errors) && json.errors.length > 0) {
    const firstError = json.errors[0] || {}
    const code = Number(firstError?.extensions?.code || 500)
    return toErrorPayload(code, firstError.message || '请求失败')
  }

  const rawResult = json?.data?.[operation.operationName]
  return toGraphQlPayload(rawResult)
}

async function executeGraphQlOperation(operation, headers) {
  const json = operation.multipart
    ? await fetchGraphQlMultipart(operation, headers)
    : await fetchGraphQl(operation, headers)
  return readGraphQlResult(json, operation)
}

async function fallbackRestRequest(config) {
  if (!originalFetch) {
    throw new Error('当前环境不支持网络请求')
  }

  const urlObject = normalizeUrl(config.url)
  Object.entries(config.params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      urlObject.searchParams.set(key, value)
    }
  })

  const headers = { ...(config.headers || {}) }
  let body = config.data
  if (body instanceof FormData) {
    // Let the browser generate the multipart boundary automatically.
    delete headers['Content-Type']
    delete headers['content-type']
  } else if (body && typeof body !== 'string') {
    const contentType = String(getHeaderValue(headers, 'Content-Type') || '')
    if (contentType.includes('application/json')) {
      body = JSON.stringify(body)
    }
  }

  const response = await originalFetch(urlObject.toString(), {
    method: config.method,
    headers,
    body: ['GET', 'HEAD'].includes(config.method) ? undefined : body
  })
  return response.json()
}

export async function executeRestLikeRequest(rawConfig) {
  const normalizedConfig = normalizeRequestConfig(rawConfig)
  const pathname = normalizeUrl(normalizedConfig.url).pathname
  const shouldRetryWithFreshKey = getSensitivePasswordFields(pathname, normalizedConfig.method).length > 0

  const config = await secureSensitiveRequest(normalizedConfig)
  const allowGraphQlBridge = shouldBridge(config.url)
  const operation = allowGraphQlBridge ? buildGraphQlOperation(config) : null
  if (operation) {
    const firstPayload = await executeGraphQlOperation(operation, config.headers)
    if (!shouldRetryWithFreshKey || !isRsaDecryptFailure(firstPayload)) {
      return firstPayload
    }

    clearRsaPublicKeyCache()
    const retryConfig = await secureSensitiveRequest(normalizedConfig)
    const retryOperation = shouldBridge(retryConfig.url) ? buildGraphQlOperation(retryConfig) : null
    if (retryOperation) {
      return executeGraphQlOperation(retryOperation, retryConfig.headers)
    }
    return firstPayload
  }

  const firstPayload = await fallbackRestRequest(config)
  if (!shouldRetryWithFreshKey || !isRsaDecryptFailure(firstPayload)) {
    return firstPayload
  }

  clearRsaPublicKeyCache()
  const retryConfig = await secureSensitiveRequest(normalizedConfig)
  return fallbackRestRequest(retryConfig)
}

export function createFetchResponse(payload) {
  return new Response(JSON.stringify(payload), {
    status: payload.code === SUCCESS_CODE ? 200 : payload.code,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

export function patchGlobalRequestApis() {
  if (typeof uni !== 'undefined' && typeof uni.request === 'function') {
    if (!originalUniRequest) {
      originalUniRequest = uni.request.bind(uni)
      if (bridgeRuntime) {
        bridgeRuntime[ORIGINAL_UNI_REQUEST_KEY] = originalUniRequest
      }
    }
    uni.request = function patchedUniRequest(options = {}) {
      if (!shouldBridge(options.url)) {
        return originalUniRequest(options)
      }

      const config = {
        url: options.url,
        method: options.method || 'GET',
        params: options.params,
        data: options.data,
        headers: options.header || options.headers || {}
      }

      const promise = executeRestLikeRequest(config)
        .then((payload) => {
          const response = {
            statusCode: payload.code === SUCCESS_CODE ? 200 : payload.code,
            data: payload,
            header: {
              'content-type': 'application/json'
            }
          }
          if (typeof options.success === 'function') {
            options.success(response)
          }
          if (typeof options.complete === 'function') {
            options.complete(response)
          }
          return response
        })
        .catch((error) => {
          const response = {
            statusCode: 500,
            data: toErrorPayload(500, error.message || '请求失败'),
            header: {
              'content-type': 'application/json'
            }
          }
          if (typeof options.fail === 'function') {
            options.fail(response)
          }
          if (typeof options.complete === 'function') {
            options.complete(response)
          }
          return response
        })

      return promise
    }
  }

  if (originalFetch) {
    globalThis.fetch = async function patchedFetch(input, init = {}) {
      const url = typeof input === 'string' || input instanceof URL
        ? String(input)
        : input?.url

      if (!url || !shouldBridge(url)) {
        return originalFetch(input, init)
      }

      const headers = normalizeHeaders(init.headers || input?.headers)
      const payload = await executeRestLikeRequest({
        url,
        method: init.method || input?.method || 'GET',
        headers,
        data: normalizeBody(init.body, headers)
      })

      return createFetchResponse(payload)
    }
  }
}

// Re-apply the runtime patch whenever this module is evaluated so HMR updates
// to bridge rules take effect immediately without relying on api.js to reload.
patchGlobalRequestApis()

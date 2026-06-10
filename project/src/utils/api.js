import request from '@/utils/request';
import { patchGlobalRequestApis } from '@/utils/graphql-bridge';
import { BASE_URL } from '@/utils/api-config';

patchGlobalRequestApis();
export { BASE_URL };

// API 端点
export const API = {
  AUTH: {
    LOGIN: `${BASE_URL}/api/auth/login`,
    REGISTER: `${BASE_URL}/api/auth/register`,
    FORGOT: {
      SEND_CODE: `${BASE_URL}/api/auth/forgot/send-code`,
      CODE: (contact) => `${BASE_URL}/api/auth/forgot/code?contact=${encodeURIComponent(contact)}`,
      EMAIL_CODE: (email) => `${BASE_URL}/api/auth/forgot/email-code?email=${encodeURIComponent(email)}`,
      PHONE_CODE: (phone) => `${BASE_URL}/api/auth/forgot/phone-code?phone=${encodeURIComponent(phone)}`,
      RESET: `${BASE_URL}/api/auth/forgot/reset`,
    },
  },
  USER: {
    AVATAR: `${BASE_URL}/api/user/avatar`,
    NICKNAME: `${BASE_URL}/api/user/nickname`,
    PROFILE: `${BASE_URL}/api/user/profile`,
    CHANGE_PASSWORD: `${BASE_URL}/api/user/password`,
    BIND_EMAIL: `${BASE_URL}/api/user/email`,
    BIND_PHONE: `${BASE_URL}/api/user/phone`,
    EYEMODE: `${BASE_URL}/api/user/eyemode`,
    SURPRISE_MODE: `${BASE_URL}/api/user/surprisemode`,
    LIST: `${BASE_URL}/api/user/list`,
    // 用户管理相关API
    DELETE: (username) => `${BASE_URL}/api/user/delete/${username}`,
    UPDATE_NICKNAME: `${BASE_URL}/api/user/update/nickname`,
    UPDATE_EMAIL: `${BASE_URL}/api/user/update/email`,
    UPDATE_PHONE: `${BASE_URL}/api/user/update/phone`,
    UPDATE_GENDER: `${BASE_URL}/api/user/update/gender`,
    // 用户权限管理相关API
    UPDATE_USER_TYPE: `${BASE_URL}/api/user/update/userType`,
  },
  INTERVIEW: {
    QUESTIONS: `${BASE_URL}/api/interview/question`,
    SUBMIT: `${BASE_URL}/api/interview/submit`,
    RESULT: `${BASE_URL}/api/interview/result`,
    QUESTION_HISTORY: (userId) => `${BASE_URL}/api/interview/question/history/${userId}`,
    // 选择试题管理相关API
    GET_ALL_QUESTIONS: `${BASE_URL}/api/interview/question/getAll`,
    GET_QUESTIONS_BY_JOB: (jobId) => `${BASE_URL}/api/interview/question/${jobId}`,
    ADD_QUESTION: `${BASE_URL}/api/interview/question/add`,
    UPDATE_QUESTION: `${BASE_URL}/api/interview/question/update`,
    DELETE_QUESTION: (id) => `${BASE_URL}/api/interview/question/delete/${id}`,
  },
  JOB: {
    CATEGORIES: `${BASE_URL}/api/job-categories/tree`,
    LIST: `${BASE_URL}/api/jobs`,
    DETAIL: (id) => `${BASE_URL}/api/job/${id}`,
    // 岗位分类管理相关API
    CREATE_FIRST_LEVEL: `${BASE_URL}/api/job-categories/first-level`,
    CREATE_SECOND_LEVEL: `${BASE_URL}/api/job-categories/second-level`,
    UPDATE_CATEGORY: `${BASE_URL}/api/job-categories`,
    DELETE_FIRST_LEVEL: (categoryId) => `${BASE_URL}/api/job-categories/first-level/${categoryId}`,
    DELETE_SECOND_LEVEL: (categoryId) => `${BASE_URL}/api/job-categories/second-level/${categoryId}`,
    // 新增：根据分类获取岗位列表
    JOBS_BY_CATEGORY: `${BASE_URL}/api/job`,
  },
  FEEDBACK: {
    SUBMIT: `${BASE_URL}/api/feedback/submit`,
    LIST: `${BASE_URL}/api/feedback`,
    DETAIL: (id) => `${BASE_URL}/api/feedback/${id}`,
    REPLY: (id) => `${BASE_URL}/api/feedback/${id}/reply`,
    MY_STATUS: `${BASE_URL}/api/feedback/my/status`,
  },
  RESUME: {
    UPLOAD: `${BASE_URL}/api/resume/upload`,
    HISTORY: (userId) => `${BASE_URL}/api/resume/history/${userId}`,
    ORIGINAL: (resumeId) => `${BASE_URL}/api/resume/original/${resumeId}`,
    UPDATE: (resumeId) => `${BASE_URL}/api/resume/update/${resumeId}`,
    RESTORE: (resumeId) => `${BASE_URL}/api/resume/restore/${resumeId}`,
    EXTRACT: `${BASE_URL}/api/resume/extract`,
  },
  COMPREHENSIVE: {
    HISTORY: (userId) => `${BASE_URL}/api/comprehensive-history/reports/user/${userId}`,
    RESUME: `${BASE_URL}/api/comprehensive-history/resume`,
    QUESTION: `${BASE_URL}/api/comprehensive-history/question`,
    SCENARIO: `${BASE_URL}/api/comprehensive-history/scenario`,
    REPORT: `${BASE_URL}/api/comprehensive-history/report`,
  },
  AI: {
    XUNFEI_AGENT: `${BASE_URL}/api/xunfei/getAgentAnswer`,
  },
  AGENT_CONVERSATIONS: {
    LIST: `${BASE_URL}/api/agent-conversations`,
    UPSERT: `${BASE_URL}/api/agent-conversations/session`,
    DELETE_BY_ID: (chatId) => `${BASE_URL}/api/agent-conversations/${encodeURIComponent(chatId)}`
  },
  RANK: {
    COMPREHENSIVE: {
      RESUME: `${BASE_URL}/api/rank/comprehensive/resume`,
      QUESTION: `${BASE_URL}/api/rank/comprehensive/question`,
      SCENARIO: `${BASE_URL}/api/rank/comprehensive/scenario`,
    },
    SPECIALIZED: {
      RESUME: `${BASE_URL}/api/rank/specialized/resume`,
      QUESTION: `${BASE_URL}/api/rank/specialized/question`,
      SCENARIO: `${BASE_URL}/api/rank/specialized/scenario`,
    },
  },
  CHAT: {
    MESSAGES: (days = 1) => `${BASE_URL}/api/chat/messages?days=${days}`,
    MESSAGE: `${BASE_URL}/api/chat/message`,
  },
  BLESSING: {
    LIST: `${BASE_URL}/api/blessings`,
    DETAIL: (id) => `${BASE_URL}/api/blessings/${id}`,
    CREATE: `${BASE_URL}/api/blessings`,
    UPDATE: (id) => `${BASE_URL}/api/blessings/${id}`,
    DELETE: (id) => `${BASE_URL}/api/blessings/${id}`,
  },
  ADMIN: {
    USERS: `${BASE_URL}/api/admin/users`,
    USER_DETAIL: (userId) => `${BASE_URL}/api/admin/users/${userId}`,
    UPDATE_USER: (userId) => `${BASE_URL}/api/admin/users/${userId}`,
    DELETE_USER: (userId) => `${BASE_URL}/api/admin/users/${userId}`,
    QUESTION_RECORDS: `${BASE_URL}/api/admin/question-records`,
    SCENARIO_RECORDS: `${BASE_URL}/api/admin/scenario-records`,
    DELETE_QUESTION_RECORD: (recordId) => `${BASE_URL}/api/admin/question-records/${recordId}`,
    DELETE_SCENARIO_RECORD: (recordId) => `${BASE_URL}/api/admin/scenario-records/${recordId}`,
    // 岗位管理相关API
    POSITIONS: `${BASE_URL}/api/admin/positions`,
    POSITION_DETAIL: (positionId) => `${BASE_URL}/api/admin/positions/${positionId}`,
    CREATE_POSITION: `${BASE_URL}/api/admin/positions`,
    UPDATE_POSITION: (positionId) => `${BASE_URL}/api/admin/positions/${positionId}`,
    DELETE_POSITION: (positionId) => `${BASE_URL}/api/admin/positions/${positionId}`,
    POSITION_CATEGORIES: `${BASE_URL}/api/admin/position-categories`,
    CREATE_CATEGORY: `${BASE_URL}/api/admin/position-categories`,
    UPDATE_CATEGORY: (categoryId) => `${BASE_URL}/api/admin/position-categories/${categoryId}`,
    DELETE_CATEGORY: (categoryId) => `${BASE_URL}/api/admin/position-categories/${categoryId}`,
  },
  SCENARIO: {
    SAVE_HISTORY: `${BASE_URL}/scenario/save/history`,
    HISTORY: (userId) => `${BASE_URL}/scenario/history/${userId}`,
  },
  SCENARIO_QUESTION: {
    GET_ALL: `${BASE_URL}/api/scenario-question/getAll`,
    GET: (jobId) => `${BASE_URL}/api/scenario-question/get/${jobId}`,
    CREATE: `${BASE_URL}/api/scenario-question/create`,
    UPDATE: `${BASE_URL}/api/scenario-question/update`,
    DELETE: (id) => `${BASE_URL}/api/scenario-question/delete/${id}`,
  },
  SPEECH: {
    SYNTHESIZE: `${BASE_URL}/api/speech/synthesize`,
  },
  TRANSCRIPTION: {
    UPLOAD: `${BASE_URL}/api/transcription/upload`,
    CREATE_TASK: `${BASE_URL}/api/transcription/create-task`,
    QUERY_TASK: (taskId) => `${BASE_URL}/api/transcription/query-task/${taskId}`,
  },
};

// ==================== 面试相关API ====================

// 获取面试题目
export function getInterviewQuestions(jobId) {
  return request({
    url: `${API.INTERVIEW.QUESTIONS}/${jobId}`,
    method: 'get'
  });
}

// ==================== 选择试题管理相关API ====================

// 获取所有选择试题
export function getAllChoiceQuestions() {
  return request({
    url: API.INTERVIEW.GET_ALL_QUESTIONS,
    method: 'get'
  });
}

// 根据岗位ID获取选择试题
export function getChoiceQuestionsByJob(jobId) {
  return request({
    url: API.INTERVIEW.GET_QUESTIONS_BY_JOB(jobId),
    method: 'get'
  });
}

// 添加选择试题
export function addChoiceQuestion(data) {
  return request({
    url: API.INTERVIEW.ADD_QUESTION,
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/json'
    }
  });
}

// 更新选择试题
export function updateChoiceQuestion(data) {
  return request({
    url: API.INTERVIEW.UPDATE_QUESTION,
    method: 'put',
    data,
    headers: {
      'Content-Type': 'application/json'
    }
  });
}

// 删除选择试题
export function deleteChoiceQuestion(id) {
  return request({
    url: API.INTERVIEW.DELETE_QUESTION(id),
    method: 'post'
  });
}

// 提交面试答案
export function submitInterviewAnswers(data) {
  return request({
    url: API.INTERVIEW.SUBMIT,
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/json'
    }
  });
}

// 获取面试结果
export function getInterviewResult(interviewId) {
  return request({
    url: `${API.INTERVIEW.RESULT}/${interviewId}`,
    method: 'get'
  });
}

// 获取岗位信息
export function getJobInfo(jobId) {
  return request({
    url: API.JOB.DETAIL(jobId),
    method: 'get'
  });
}

// 查询当前 agentKey 下的会话列表，供左侧历史栏恢复使用。
export function fetchAgentConversationList(agentKey) {
  return request({
    url: API.AGENT_CONVERSATIONS.LIST,
    method: 'get',
    params: {
      agentKey
    }
  });
}

// 整包上报当前会话快照，由后端拆分落到 session/message/event/memory 四表。
export function upsertAgentConversation(data) {
  return request({
    url: API.AGENT_CONVERSATIONS.UPSERT,
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/json'
    }
  });
}

// 删除一个会话，后端会级联删除该 session 关联的 message/event/memory 记录。
export function deleteAgentConversationById(chatId) {
  return request({
    url: API.AGENT_CONVERSATIONS.DELETE_BY_ID(chatId),
    method: 'delete'
  });
}

// 获取岗位分类树
export function getJobCategoriesTree() {
  return request({
    url: API.JOB.CATEGORIES,
    method: 'get'
  });
}

// 创建一级岗位分类
export function createFirstLevelCategory(data) {
  return request({
    url: API.JOB.CREATE_FIRST_LEVEL,
    method: 'post',
    data
  });
}

// 创建二级岗位分类
export function createSecondLevelCategory(data) {
  return request({
    url: API.JOB.CREATE_SECOND_LEVEL,
    method: 'post',
    data
  });
}

// 更新岗位分类
export function updateJobCategory(data) {
  return request({
    url: API.JOB.UPDATE_CATEGORY,
    method: 'put',
    data
  });
}

// 删除一级岗位分类
export function deleteFirstLevelCategory(categoryId) {
  return request({
    url: API.JOB.DELETE_FIRST_LEVEL(categoryId),
    method: 'delete'
  });
}

// 删除二级岗位分类
export function deleteSecondLevelCategory(categoryId) {
  return request({
    url: API.JOB.DELETE_SECOND_LEVEL(categoryId),
    method: 'delete'
  });
}

// 获取分类下的岗位列表
export function getJobsByCategory(categoryId) {
  return request({
    url: API.JOB.LIST,
    method: 'get',
    params: { categoryId }
  });
}

// 新增：根据分类ID获取岗位列表（使用正确的接口）
export function getJobsByCategoryId(categoryId) {
  return request({
    url: API.JOB.JOBS_BY_CATEGORY,
    method: 'get',
    params: { categoryId }
  });
}

// 岗位管理相关API函数
export function getJobList() {
  return request({
    url: `${BASE_URL}/api/jobs`,
    method: 'GET'
  });
}

export function getJobDetail(id) {
  return request({
    url: API.JOB.DETAIL(id),
    method: 'GET'
  });
}

export function getBlessings(params = {}) {
  return request({
    url: API.BLESSING.LIST,
    method: 'get',
    params
  });
}

export function createBlessing(data) {
  return request({
    url: API.BLESSING.CREATE,
    method: 'post',
    data,
    headers: { 'Content-Type': 'application/json' }
  });
}

export function updateBlessing(id, data) {
  return request({
    url: API.BLESSING.UPDATE(id),
    method: 'put',
    data,
    headers: { 'Content-Type': 'application/json' }
  });
}

export function deleteBlessing(id) {
  return request({
    url: API.BLESSING.DELETE(id),
    method: 'delete'
  });
}

export function createJob(data) {
  return request({
    url: `${BASE_URL}/api/jobs`,
    method: 'POST',
    data
  });
}

export function updateJob(data) {
  return request({
    url: `${BASE_URL}/api/jobs`,
    method: 'PUT',
    data
  });
}

export function deleteJob(jobId) {
  return request({
    url: `${BASE_URL}/api/jobs/${jobId}`,
    method: 'DELETE'
  });
}

// 调用后端统一封装的 AI Agent。
export function callAiAgent(agentKey, params = {}, options = {}) {
  return request({
    url: API.AI.XUNFEI_AGENT,
    method: 'post',
    data: {
      agentKey,
      params,
      chatId: options.chatId || ''
    },
    headers: {
      'Content-Type': 'application/json'
    },
    timeout: options.timeout || 120000
  });
}

function normalizeQuestionAnalysisPayload(payload = {}) {
  return {
    question: payload.question || '',
    options: Array.isArray(payload.options) ? payload.options : [],
    answer: payload.answer ?? payload.correctAnswer ?? ''
  }
}

function normalizeScenarioQuestionItems(questions = []) {
  if (!Array.isArray(questions)) {
    return []
  }
  // Keep payload minimal and aligned with SKILL.md contracts:
  // - scenario-question-scoring: question + userAnswer
  // - scenario-audio-evaluation: question + userAnswer (+ transcript/recognized text if available)
  return questions.map((item) => {
    const question = item?.question || ''
    const userAnswer = item?.userAnswer ?? item?.answer ?? ''
    const transcript = item?.transcript ?? item?.recognizedText ?? userAnswer ?? ''
    return { question, userAnswer, transcript }
  })
}

// 调用大模型进行场景分析
export function analyzeScenarioWithXunfei(agentKey, params = {}, options = {}) {
  return callAiAgent(agentKey, params, options);
}

export function analyzeScenarioInterview(jobInfo, questionsAndAnswers) {
  return callAiAgent('scenario-evaluation', {
    jobName: jobInfo?.name || '',
    jobDescription: jobInfo?.description || '',
    questionsAndAnswers
  }, { timeout: 120000 }).then((response) => normalizeAgentObjectResponse(response));
}

export function getComprehensiveReportAnalysis(payload) {
  return callAiAgent('comprehensive-report', payload, { timeout: 120000 })
    .then((response) => normalizeAgentObjectResponse(response));
}

export function generateScenarioQuestionByAi({ jobName, jobRequirements, angle }) {
  return callAiAgent('scenario-question-gen', {
    jobName,
    jobRequirements,
    angle
  }, { timeout: 120000 }).then((response) => normalizeAgentTextResponse(response, 'question'));
}

export function analyzeComprehensiveScenarioAudio(questions) {
  return callAiAgent('scenario-audio-evaluation', {
    questions: normalizeScenarioQuestionItems(questions)
  }, { timeout: 120000 }).then((response) => normalizeAgentObjectResponse(response));
}

export function analyzeComprehensiveResume(resumeContent, jobDetails = {}) {
  return callAiAgent('comprehensive-resume-analysis', {
    resumeContent,
    jobName: jobDetails?.name || '',
    jobRequirements: jobDetails?.requirements || ''
  }, { timeout: 120000 }).then((response) => normalizeAgentObjectResponse(response));
}

export function generateComprehensiveQuestions(jobName, jobDescription) {
  return callAiAgent('comprehensive-question-generation', {
    jobName,
    jobDescription
  }, { timeout: 120000 }).then((response) => normalizeAgentObjectResponse(response));
}

export function scoreScenarioQuestions(questions) {
  return callAiAgent('scenario-question-scoring', {
    questions: normalizeScenarioQuestionItems(questions)
  }, { timeout: 120000 }).then((response) => normalizeAgentObjectResponse(response));
}

export function getQuestionAnalysisByAi(payload) {
  return callAiAgent('question-analysis', normalizeQuestionAnalysisPayload(payload), { timeout: 120000 })
    .then((response) => normalizeAgentTextResponse(response, 'analysis'));
}

export function getInterviewAssistantReply(payload) {
  if (typeof payload === 'string') {
    return callAiAgent('interview-assistant', {
      question: payload
    }, { timeout: 120000 }).then((response) => normalizeAgentObjectResponse(response));
  }
  const safePayload = payload && typeof payload === 'object' ? { ...payload } : {}
  const chatId = safePayload.chatId || ''
  delete safePayload.chatId
  return callAiAgent('interview-assistant', safePayload, {
    timeout: 120000,
    chatId
  }).then((response) => normalizeAgentObjectResponse(response));
}

function normalizeAiJsonString(raw) {
  if (raw == null) {
    return ''
  }
  if (typeof raw !== 'string') {
    return JSON.stringify(raw)
  }

  const text = raw.trim()
  if (!text) {
    return ''
  }

  const codeBlockMatch = text.match(/```(?:json)?\s*([\s\S]*?)```/i)
  if (codeBlockMatch?.[1]) {
    const candidate = codeBlockMatch[1].trim()
    try {
      return JSON.stringify(JSON.parse(candidate))
    } catch {}
  }

  try {
    return JSON.stringify(JSON.parse(text))
  } catch {}

  const objectMatch = text.match(/\{[\s\S]*\}/)
  if (objectMatch?.[0]) {
    const candidate = objectMatch[0].trim()
    try {
      return JSON.stringify(JSON.parse(candidate))
    } catch {}
  }

  return text
}

function normalizeStructuredTextField(raw, fieldName) {
  if (!fieldName) {
    return raw
  }
  if (raw && typeof raw === 'object') {
    return raw[fieldName] ?? raw
  }
  const normalized = normalizeAiJsonString(raw)
  if (typeof normalized !== 'string') {
    return normalized
  }
  try {
    const parsed = JSON.parse(normalized)
    if (parsed && typeof parsed === 'object' && fieldName in parsed) {
      return parsed[fieldName]
    }
  } catch {}
  return normalized
}

function normalizeAgentTextResponse(response, fieldName) {
  if (!response || response.code !== 200) {
    return response
  }
  return {
    ...response,
    data: normalizeStructuredTextField(response.data, fieldName)
  }
}

function normalizeAgentObjectResponse(response) {
  if (!response || response.code !== 200) {
    return response
  }
  if (response.data && typeof response.data === 'object') {
    return response
  }
  const normalized = normalizeAiJsonString(response.data)
  try {
    return {
      ...response,
      data: JSON.parse(normalized)
    }
  } catch {
    return {
      ...response,
      data: normalized
    }
  }
}

export function getResumeAnalysisByXunfei(jobDesc, resumeText) {
  return callAiAgent('resume-analysis', {
    jobDesc,
    resumeText
  }, { timeout: 120000 }).then(response => {
    const normalized = normalizeAgentObjectResponse(response)
    if (response.code === 200 && response.data) {
      return {
        code: 200,
        data: normalized.data,
        message: response.message,
        success: response.success
      };
    } else {
      throw new Error(response.message || '获取分析结果失败');
    }
  });
}

// 根据岗位ID获取场景问题列表
export function getScenarioQuestions(jobId) {
  return request({
    url: API.SCENARIO_QUESTION.GET(jobId),
    method: 'get'
  });
}

// 保存场景评测历史
export function saveScenarioHistory(data, evaluationType = 'SPECIAL') {
  return request({
    url: `${API.SCENARIO.SAVE_HISTORY}?evaluationType=${evaluationType}`,
    method: 'post',
    data,
    withCredentials: false,
    headers: {
      'Content-Type': 'application/json'
    }
  });
}

// ==================== 语音相关API ====================

// 文本转语音（TTS）接口
export function synthesizeSpeech({ text, vcn = 'x4_yezi', pitch = 50, speed = 50, tte = 'UTF8' }) {
  return request({
    url: API.SPEECH.SYNTHESIZE,
    method: 'post',
    data: { text, vcn, pitch, speed, tte },
    headers: {
      'Content-Type': 'application/json'
    },
    responseType: 'arraybuffer'  // 直接请求二进制数据
  }).then(response => {
    // 如果返回的是JSON格式（包含code、message、data字段）
    if (response && response.code !== undefined) {
      if (response.code === 200) {
        return response.data; // 返回data字段中的音频数据
      } else {
        throw new Error(response.message || '语音合成失败');
      }
    }
    // 如果直接返回的是二进制数据
    return response;
  });
}

// 步骤1：上传音频文件，返回音频URL
export function uploadAudioFile(file) {
  const formData = new FormData();
  formData.append('file', file);
  return request({
    url: API.TRANSCRIPTION.UPLOAD,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 300000
  });
}

// 步骤2：创建转写任务，返回任务ID
export function createTranscriptionTask(audioUrl) {
  const formData = new FormData();
  formData.append('audioUrl', audioUrl);
  return request({
    url: API.TRANSCRIPTION.CREATE_TASK,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}

// 步骤3：查询转写任务结果
export function queryTranscriptionTask(taskId) {
  return request({
    url: API.TRANSCRIPTION.QUERY_TASK(taskId),
    method: 'get'
  });
}

// ==================== 简历相关API ====================

// 上传简历
export function uploadResume(file, jobId) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('jobId', jobId);

  return request({
    url: API.RESUME.UPLOAD,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}


// 获取原始简历内容
export function getResumeOriginal(resumeId) {
  return request({
    url: API.RESUME.ORIGINAL(resumeId),
    method: 'get',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  });
}

// 更新简历内容
export function updateResumeContent(resumeId, userId, jobId, content) {
  return request({
    url: API.RESUME.UPDATE(resumeId),
    method: 'put',
    data: { userId, jobId, content },
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    },
    timeout: 120000
  });
}

// 恢复简历内容
export function restoreResumeContent(resumeId) {
  return request({
    url: API.RESUME.RESTORE(resumeId),
    method: 'post',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  });
}

// 提取简历内容
export function extractResumeContent(file) {
  const formData = new FormData();
  formData.append('file', file);

  return request({
    url: API.RESUME.EXTRACT,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
  }).then(response => {
    console.log('[Resume Extract] Content extraction response:', response);
    return response;
  }).catch(error => {
    console.error('[Resume Extract] Error extracting content:', error);
    throw error;
  });
}

// 保存简历分析历史
export function saveResumeHistory({ id, userId, jobId, resumeContent, score, feedback, competenciesScore, evaluationType }) {
  const payload = {
    id,
    jobId,
    resumeContent,
    score,
    feedback,
    competenciesScore,
    ...(userId ? { userId } : {})
  }
  return request({
    url: '/api/resume/save/history' + (evaluationType ? `?evaluationType=${evaluationType}` : ''),
    method: 'post',
    data: payload,
    withCredentials: false,
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  }).then(res => {
    if (res.code === 200) {
      return res.data; // 历史记录ID
    } else {
      throw new Error(res.message || '保存历史失败');
    }
  });
}

// 查询用户简历分析历史
export function getResumeHistoryList(userId, evaluationType = 'SPECIAL') {
  return request({
    url: `/api/resume/history/${userId}` + (evaluationType ? `?evaluationType=${evaluationType}` : ''),
    method: 'get',
    headers: {
      'Accept': 'application/json'
    }
  });
}

// 获取用户列表（分页）
export function getUserList(page, size) {
  return request({
    url: API.USER.LIST,
    method: 'get',
    params: { page, size }
  });
}

// ==================== 用户管理相关API ====================

// 删除用户
export function deleteUser(username) {
  return request({
    url: API.USER.DELETE(username),
    method: 'delete'
  });
}

// 更新用户昵称
export function updateUserNickname(username, nickname) {
  return request({
    url: API.USER.UPDATE_NICKNAME,
    method: 'put',
    data: { username, nickname }
  });
}

// 更新用户邮箱
export function updateUserEmail(username, email) {
  return request({
    url: API.USER.UPDATE_EMAIL,
    method: 'put',
    data: { username, email }
  });
}

// 更新用户手机号
export function updateUserPhone(username, phone) {
  return request({
    url: API.USER.UPDATE_PHONE,
    method: 'put',
    data: { username, phone }
  });
}

// 更新用户性别
export function updateUserGender(username, gender) {
  return request({
    url: API.USER.UPDATE_GENDER,
    method: 'put',
    data: { username, gender }
  });
}

// 更新用户类型（权限）
export function updateUserType(username, userType) {
  return request({
    url: API.USER.UPDATE_USER_TYPE,
    method: 'put',
    data: { username, userType }
  });
}

// 根据历史记录ID恢复简历内容
export function restoreResumeContentByHistoryId(resumeHistoryId) {
  return request({
    url: `/api/resume/restore/${resumeHistoryId}`,
    method: 'post',
    headers: {
      'Accept': 'application/json'
    }
  });
}

// 查询单条简历分析历史详情
export function getResumeHistoryDetailById(resumeHistoryId) {
  return request({
    url: `/api/resume/history/id/${resumeHistoryId}`,
    method: 'get',
    headers: {
      'Accept': 'application/json'
    }
  });
}

// ==================== 综合评估相关API ====================

// 保存简历综合评估历史
export function saveComprehensiveResumeHistory(data) {
  return request({
    url: API.COMPREHENSIVE.RESUME,
    method: 'post',
    data: {
      userId: data.userId,
      jobId: data.jobId,
      fileName: data.fileName,
      fileSize: data.fileSize,
      fileType: data.fileType,
      overallScore: data.overallScore,
      skillMatch: data.skillMatch,
      experienceMatch: data.experienceMatch,
      educationMatch: data.educationMatch,
      communicationSkill: data.communicationSkill,
      leadershipSkill: data.leadershipSkill,
      aiAnalysis: data.aiAnalysis,
      aiSuggestions: data.aiSuggestions
    },
    withCredentials: false,
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  }).then(res => {
    if (res.code === 200) {
      return res.data;
    } else {
      throw new Error(res.message || '保存综合评估历史失败');
    }
  });
}

// 保存问题综合评估历史
export function saveComprehensiveQuestionHistory(data) {
  return request({
    url: API.COMPREHENSIVE.QUESTION,
    method: 'post',
    data: {
      userId: data.userId,
      jobId: data.jobId,
      startTime: data.startTime,
      endTime: data.endTime,
      totalTime: data.totalTime,
      overallScore: data.overallScore,
      aiSuggestions: data.aiSuggestions
    },
    withCredentials: false,
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  }).then(res => {
    if (res.code === 200) {
      return res.data;
    } else {
      throw new Error(res.message || '保存问题综合评估历史失败');
    }
  });
}

// 保存场景综合评估历史
export function saveComprehensiveScenarioHistory(data) {
  return request({
    url: API.COMPREHENSIVE.SCENARIO,
    method: 'post',
    data: {
      userId: data.userId,
      jobId: data.jobId,
      startTime: data.startTime,
      endTime: data.endTime,
      totalTime: data.totalTime,
      overallScore: data.overallScore,
      fluencyScore: data.fluencyScore,
      emotionScore: data.emotionScore,
      relevanceScore: data.relevanceScore,
      adaptabilityScore: data.adaptabilityScore,
      aiAssessment: data.aiAssessment
    },
    withCredentials: false,
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  }).then(res => {
    if (res.code === 200) {
      return res.data;
    } else {
      throw new Error(res.message || '保存场景综合评估历史失败');
    }
  });
}

// 保存综合评估报告
export function saveComprehensiveReportHistory(data) {
  return request({
    url: API.COMPREHENSIVE.REPORT,
    method: 'post',
    data: {
      userId: data.userId,
      jobId: data.jobId,
      overallScore: data.overallScore,
      resumeScore: data.resumeScore,
      questionScore: data.questionScore,
      scenarioScore: data.scenarioScore,
      strengthAnalysis: data.strengthAnalysis,
      improvementAnalysis: data.improvementAnalysis,
      learningRoute: data.learningRoute
    },
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  }).then(res => {
    if (res.code === 200) {
      return res.data;
    } else {
      throw new Error(res.message || '保存综合评估报告失败');
    }
  });
} 

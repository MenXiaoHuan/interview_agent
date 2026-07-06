import request from '@/utils/api/request';
import { BASE_URL } from '@/utils/api/api-config';

const AGENT_ENDPOINT = `${BASE_URL}/api/xunfei/getAgentAnswer`;
const JOB_DETAIL = (id) => `${BASE_URL}/api/job/${id}`;
const JOB_LIST = `${BASE_URL}/api/jobs`;
const RESUME_EXTRACT = `${BASE_URL}/api/resume/extract`;
const AGENT_CONVERSATIONS = {
  LIST: `${BASE_URL}/api/agent-conversations`,
  UPSERT: `${BASE_URL}/api/agent-conversations/session`,
  DELETE_BY_ID: (chatId) => `${BASE_URL}/api/agent-conversations/${encodeURIComponent(chatId)}`,
};

function callAiAgent(agentKey, params = {}, options = {}) {
  return request({
    url: AGENT_ENDPOINT,
    method: 'post',
    data: {
      agentKey,
      params,
      chatId: options.chatId || '',
    },
    headers: {
      'Content-Type': 'application/json',
    },
    timeout: options.timeout || 120000,
  });
}

function normalizeAiJsonString(raw) {
  if (raw == null) return '';
  if (typeof raw !== 'string') return JSON.stringify(raw);
  const text = raw.trim();
  if (!text) return '';

  const codeBlockMatch = text.match(/```(?:json)?\s*([\s\S]*?)```/i);
  if (codeBlockMatch?.[1]) {
    const candidate = codeBlockMatch[1].trim();
    try {
      return JSON.stringify(JSON.parse(candidate));
    } catch {}
  }

  try {
    return JSON.stringify(JSON.parse(text));
  } catch {}

  const objectMatch = text.match(/\{[\s\S]*\}/);
  if (objectMatch?.[0]) {
    const candidate = objectMatch[0].trim();
    try {
      return JSON.stringify(JSON.parse(candidate));
    } catch {}
  }

  return text;
}

function normalizeAgentObjectResponse(response) {
  if (!response || response.code !== 200) return response;
  if (response.data && typeof response.data === 'object') return response;
  const normalized = normalizeAiJsonString(response.data);
  try {
    return {
      ...response,
      data: JSON.parse(normalized),
    };
  } catch {
    return {
      ...response,
      data: normalized,
    };
  }
}

export function getJobInfo(jobId) {
  return request({
    url: JOB_DETAIL(jobId),
    method: 'get',
  });
}

export function getJobList() {
  return request({
    url: JOB_LIST,
    method: 'GET',
  });
}

export function fetchAgentConversationList(agentKey) {
  return request({
    url: AGENT_CONVERSATIONS.LIST,
    method: 'get',
    params: {
      agentKey,
    },
  });
}

export function upsertAgentConversation(data) {
  return request({
    url: AGENT_CONVERSATIONS.UPSERT,
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/json',
    },
  });
}

export function deleteAgentConversationById(chatId) {
  return request({
    url: AGENT_CONVERSATIONS.DELETE_BY_ID(chatId),
    method: 'delete',
  });
}

export function extractResumeContent(file) {
  const formData = new FormData();
  formData.append('file', file);

  return request({
    url: RESUME_EXTRACT,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}

export function analyzeComprehensiveResume(resumeContent, jobDetails = {}) {
  return callAiAgent('ai-resume-analysis', {
    resumeContent,
    jobName: jobDetails?.name || '',
    jobRequirements: jobDetails?.requirements || '',
  }, { timeout: 120000 }).then((response) => normalizeAgentObjectResponse(response));
}

export function getInterviewAssistantReply(payload) {
  if (typeof payload === 'string') {
    return callAiAgent('ai-interview-assistant', {
      question: payload,
    }, { timeout: 120000 }).then((response) => normalizeAgentObjectResponse(response));
  }
  const safePayload = payload && typeof payload === 'object' ? { ...payload } : {};
  const chatId = safePayload.chatId || '';
  delete safePayload.chatId;
  return callAiAgent('ai-interview-assistant', safePayload, {
    timeout: 120000,
    chatId,
  }).then((response) => normalizeAgentObjectResponse(response));
}

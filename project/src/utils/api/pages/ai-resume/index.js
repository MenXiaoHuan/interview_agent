import request from '@/utils/api/request';
import { BASE_URL } from '@/utils/api/api-config';

const AGENT_ENDPOINT = `${BASE_URL}/api/xunfei/getAgentAnswer`;
const JOB_DETAIL = (id) => `${BASE_URL}/api/job/${id}`;
const RESUME_EXTRACT = `${BASE_URL}/api/resume/extract`;
const AI_RESUME_HISTORY = `${BASE_URL}/api/ai-assessment/resume`;

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
    try {
      return JSON.stringify(JSON.parse(codeBlockMatch[1].trim()));
    } catch {}
  }
  try {
    return JSON.stringify(JSON.parse(text));
  } catch {}
  const objectMatch = text.match(/\{[\s\S]*\}/);
  if (objectMatch?.[0]) {
    try {
      return JSON.stringify(JSON.parse(objectMatch[0].trim()));
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

export function saveAiResumeHistory(data) {
  return request({
    url: AI_RESUME_HISTORY,
    method: 'post',
    data: {
      assessmentSessionId: data.assessmentSessionId,
      userId: data.userId,
      jobId: data.jobId,
      fileName: data.fileName,
      fileSize: data.fileSize,
      fileType: data.fileType,
      overallScore: data.overallScore,
      aiAnalysis: data.aiAnalysis,
      aiSuggestions: data.aiSuggestions,
    },
    withCredentials: false,
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
  }).then((res) => {
    if (res.code === 200) {
      return res.data;
    }
    throw new Error(res.message || '保存AIview简历归档失败');
  });
}

import request from '@/utils/api/request';
import { BASE_URL } from '@/utils/api/api-config';

const AGENT_ENDPOINT = `${BASE_URL}/api/xunfei/getAgentAnswer`;
const JOB_DETAIL = (id) => `${BASE_URL}/api/job/${id}`;
const AI_QUESTION_HISTORY = `${BASE_URL}/api/ai-assessment/question`;

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

function normalizeQuestionAnalysisPayload(payload = {}) {
  return {
    question: payload.question || '',
    options: Array.isArray(payload.options) ? payload.options : [],
    answer: payload.answer ?? payload.correctAnswer ?? '',
  };
}

function normalizeScenarioQuestionItems(questions = []) {
  if (!Array.isArray(questions)) return [];
  return questions.map((item) => {
    const question = item?.question || '';
    const userAnswer = item?.userAnswer ?? item?.answer ?? '';
    const transcript = item?.transcript ?? item?.recognizedText ?? userAnswer ?? '';
    return { question, userAnswer, transcript };
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

function normalizeStructuredTextField(raw, fieldName) {
  if (!fieldName) return raw;
  if (raw && typeof raw === 'object') return raw[fieldName] ?? raw;
  const normalized = normalizeAiJsonString(raw);
  try {
    const parsed = JSON.parse(normalized);
    if (parsed && typeof parsed === 'object' && fieldName in parsed) {
      return parsed[fieldName];
    }
  } catch {}
  return normalized;
}

function normalizeAgentTextResponse(response, fieldName) {
  if (!response || response.code !== 200) return response;
  return {
    ...response,
    data: normalizeStructuredTextField(response.data, fieldName),
  };
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

export function generateComprehensiveQuestions(jobName, jobDescription) {
  return callAiAgent('ai-question-generation', {
    jobName,
    jobDescription,
  }, { timeout: 120000 }).then((response) => normalizeAgentObjectResponse(response));
}

export function scoreScenarioQuestions(questions) {
  return callAiAgent('ai-question-scoring', {
    questions: normalizeScenarioQuestionItems(questions),
  }, { timeout: 120000 }).then((response) => normalizeAgentObjectResponse(response));
}

export function getQuestionAnalysisByAi(payload) {
  return callAiAgent('ai-question-analysis', normalizeQuestionAnalysisPayload(payload), { timeout: 120000 })
    .then((response) => normalizeAgentTextResponse(response, 'analysis'));
}

export function saveAiQuestionHistory(data) {
  return request({
    url: AI_QUESTION_HISTORY,
    method: 'post',
    data: {
      assessmentSessionId: data.assessmentSessionId,
      userId: data.userId,
      jobId: data.jobId,
      startTime: data.startTime,
      endTime: data.endTime,
      totalTime: data.totalTime,
      overallScore: data.overallScore,
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
    throw new Error(res.message || '保存AIview试题归档失败');
  });
}

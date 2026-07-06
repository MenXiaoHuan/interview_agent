import request from '@/utils/api/request';
import { BASE_URL } from '@/utils/api/api-config';

export { BASE_URL };

const AGENT_ENDPOINT = `${BASE_URL}/api/xunfei/getAgentAnswer`;
const AI_SCENARIO_HISTORY = `${BASE_URL}/api/ai-assessment/interview-round`;
const SPEECH_SYNTHESIZE = `${BASE_URL}/api/speech/synthesize`;
const TRANSCRIPTION = {
  UPLOAD: `${BASE_URL}/api/transcription/upload`,
  CREATE_TASK: `${BASE_URL}/api/transcription/create-task`,
  QUERY_TASK: (taskId) => `${BASE_URL}/api/transcription/query-task/${taskId}`,
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

export function generateScenarioQuestionByAi({ jobName, jobRequirements, angle }) {
  return callAiAgent('ai-round-question-generation', {
    jobName,
    jobRequirements,
    angle,
  }, { timeout: 120000 }).then((response) => normalizeAgentTextResponse(response, 'question'));
}

export function analyzeComprehensiveScenarioAudio(questions) {
  return callAiAgent('ai-round-evaluation', {
    questions: normalizeScenarioQuestionItems(questions),
  }, { timeout: 120000 }).then((response) => normalizeAgentObjectResponse(response));
}

export function saveAiInterviewRoundHistory(data) {
  return request({
    url: AI_SCENARIO_HISTORY,
    method: 'post',
    data: {
      assessmentSessionId: data.assessmentSessionId,
      userId: data.userId,
      jobId: data.jobId,
      roundType: data.roundType,
      startTime: data.startTime,
      endTime: data.endTime,
      totalTime: data.totalTime,
      overallScore: data.overallScore,
      aiAssessment: data.aiAssessment,
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
    throw new Error(res.message || '保存AIview面试归档失败');
  });
}

export function synthesizeSpeech({ text, vcn = 'x4_yezi', pitch = 50, speed = 50, tte = 'UTF8' }) {
  return request({
    url: SPEECH_SYNTHESIZE,
    method: 'post',
    data: { text, vcn, pitch, speed, tte },
    headers: {
      'Content-Type': 'application/json',
    },
    responseType: 'arraybuffer',
  }).then((response) => {
    if (response && response.code !== undefined) {
      if (response.code === 200) {
        return response.data;
      }
      throw new Error(response.message || '语音合成失败');
    }
    return response;
  });
}

export function uploadAudioFile(file) {
  const formData = new FormData();
  formData.append('file', file);
  return request({
    url: TRANSCRIPTION.UPLOAD,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 300000,
  });
}

export function createTranscriptionTask(audioUrl) {
  const formData = new FormData();
  formData.append('audioUrl', audioUrl);
  return request({
    url: TRANSCRIPTION.CREATE_TASK,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

export function queryTranscriptionTask(taskId) {
  return request({
    url: TRANSCRIPTION.QUERY_TASK(taskId),
    method: 'get',
  });
}

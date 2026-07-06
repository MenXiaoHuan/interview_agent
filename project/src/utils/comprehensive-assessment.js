const ACTIVE_ASSESSMENT_PREFIX = 'activeComprehensiveAssessmentId:';
const ASSESSMENT_SESSION_PREFIX = 'comprehensiveAssessmentSession:';
const ASSESSMENT_COMPLETION_INBOX_KEY = 'pendingAiAssessmentCompletions';
const ASSESSMENT_THRESHOLDS_KEY = 'aiAssessmentThresholds';
const LEGACY_COMPREHENSIVE_STATE_KEY = 'comprehensiveTestState';
const DEFAULT_PASS_SCORE = 80;
const STAGE_SEQUENCE = ['resume', 'questions', 'round_1', 'round_2', 'round_3'];
const STAGE_ALIASES = {
  audio: 'round_1',
  round1: 'round_1',
  round2: 'round_2',
  round3: 'round_3'
};

const nowIsoString = () => new Date().toISOString();

const readStorage = (key) => {
  try {
    if (typeof uni !== 'undefined' && typeof uni.getStorageSync === 'function') {
      const value = uni.getStorageSync(key);
      if (value !== '' && value !== undefined) {
        return value;
      }
    }
  } catch (_) {}
  try {
    if (typeof localStorage !== 'undefined') {
      return localStorage.getItem(key);
    }
  } catch (_) {}
  return null;
};

const writeStorage = (key, value) => {
  try {
    if (typeof uni !== 'undefined' && typeof uni.setStorageSync === 'function') {
      uni.setStorageSync(key, value);
    }
  } catch (_) {}
  try {
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(key, value);
    }
  } catch (_) {}
};

const removeStorage = (key) => {
  try {
    if (typeof uni !== 'undefined' && typeof uni.removeStorageSync === 'function') {
      uni.removeStorageSync(key);
    }
  } catch (_) {}
  try {
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(key);
    }
  } catch (_) {}
};

const parseStoredJson = (value) => {
  if (!value) {
    return null;
  }
  if (typeof value === 'object') {
    return value;
  }
  try {
    return JSON.parse(String(value));
  } catch (_) {
    return null;
  }
};

const safeJobId = (jobId) => String(jobId || '').trim();
const safeText = (value) => String(value || '').trim();
const normalizeScoreValue = (value, fallback = DEFAULT_PASS_SCORE) => {
  const score = Math.round(Number(value));
  if (!Number.isFinite(score)) {
    return fallback;
  }
  return Math.min(100, Math.max(0, score));
};

const buildSessionStorageKey = (jobId, assessmentId) => (
  `${ASSESSMENT_SESSION_PREFIX}${safeJobId(jobId)}:${String(assessmentId || '').trim()}`
);

const buildActiveAssessmentStorageKey = (jobId) => (
  `${ACTIVE_ASSESSMENT_PREFIX}${safeJobId(jobId)}`
);

const readCompletionInbox = () => {
  const parsed = parseStoredJson(readStorage(ASSESSMENT_COMPLETION_INBOX_KEY));
  return Array.isArray(parsed) ? parsed : [];
};

const writeCompletionInbox = (events = []) => {
  const safeEvents = Array.isArray(events) ? events : [];
  if (safeEvents.length === 0) {
    removeStorage(ASSESSMENT_COMPLETION_INBOX_KEY);
    return [];
  }
  writeStorage(ASSESSMENT_COMPLETION_INBOX_KEY, JSON.stringify(safeEvents.slice(-20)));
  return safeEvents;
};

const completionMatches = (event = {}, match = {}) => {
  const expectedType = safeText(match.type);
  const expectedAssessmentId = safeText(match.assessmentId);
  const expectedJobId = safeJobId(match.jobId);
  const expectedChatId = safeText(match.chatId);
  return (!expectedType || safeText(event.type) === expectedType)
    && (!expectedAssessmentId || safeText(event.assessmentId) === expectedAssessmentId)
    && (!expectedJobId || safeJobId(event.jobId) === expectedJobId)
    && (!expectedChatId || safeText(event.chatId) === expectedChatId);
};

export const enqueueAiAssessmentCompletion = (event = {}) => {
  const safeEvent = {
    type: safeText(event.type),
    chatId: safeText(event.chatId),
    jobId: safeJobId(event.jobId),
    assessmentId: safeText(event.assessmentId),
    mode: safeText(event.mode) || 'COMPREHENSIVE',
    score: Math.max(0, Number(event.score || 0) || 0),
    hasScore: event.score !== undefined && safeText(event.score) !== '',
    suggestions: Array.isArray(event.suggestions)
      ? event.suggestions.map((item) => safeText(item)).filter(Boolean)
      : [],
    timestamp: safeText(event.timestamp) || String(Date.now())
  };
  if (!safeEvent.type || !safeEvent.assessmentId) {
    return null;
  }
  const nextInbox = [
    ...readCompletionInbox().filter((item) => !(
      safeText(item.type) === safeEvent.type
      && safeText(item.assessmentId) === safeEvent.assessmentId
      && safeText(item.timestamp) === safeEvent.timestamp
    )),
    safeEvent
  ];
  writeCompletionInbox(nextInbox);
  return safeEvent;
};

export const consumeAiAssessmentCompletion = (match = {}) => {
  const inbox = readCompletionInbox();
  const targetIndex = inbox.findLastIndex((event) => completionMatches(event, match));
  if (targetIndex < 0) {
    return null;
  }
  const [targetEvent] = inbox.splice(targetIndex, 1);
  writeCompletionInbox(inbox);
  return targetEvent || null;
};

const createDefaultStageState = (overrides = {}) => ({
  completed: false,
  inProgress: false,
  score: 0,
  passed: false,
  rejected: false,
  summary: '',
  startTime: null,
  endTime: null,
  attempts: 0,
  ...overrides
});

export const getStageSequence = () => [...STAGE_SEQUENCE];

export const normalizeAssessmentThresholds = (thresholds = {}) => {
  const source = thresholds && typeof thresholds === 'object' ? thresholds : {};
  const rawStages = source.stages && typeof source.stages === 'object' ? source.stages : {};
  const defaultPassScore = normalizeScoreValue(source.defaultPassScore, DEFAULT_PASS_SCORE);
  return {
    defaultPassScore,
    stages: STAGE_SEQUENCE.reduce((acc, stage) => {
      acc[stage] = normalizeScoreValue(rawStages[stage], defaultPassScore);
      return acc;
    }, {})
  };
};

export const getAssessmentThresholds = () => {
  const stored = parseStoredJson(readStorage(ASSESSMENT_THRESHOLDS_KEY));
  return normalizeAssessmentThresholds(stored || { defaultPassScore: DEFAULT_PASS_SCORE });
};

export const saveAssessmentThresholds = (thresholds = {}) => {
  const normalized = normalizeAssessmentThresholds(thresholds);
  writeStorage(ASSESSMENT_THRESHOLDS_KEY, JSON.stringify(normalized));
  return normalized;
};

export const resetAssessmentThresholds = () => {
  removeStorage(ASSESSMENT_THRESHOLDS_KEY);
  return getAssessmentThresholds();
};

export const getStagePassScore = (stage = '', thresholds = getAssessmentThresholds()) => {
  const safeStage = normalizeComprehensiveStage(stage);
  const normalized = normalizeAssessmentThresholds(thresholds);
  return safeStage ? normalized.stages[safeStage] : normalized.defaultPassScore;
};

export const normalizeComprehensiveStage = (stage) => {
  const safeStage = String(stage || '').trim().toLowerCase();
  if (!safeStage) {
    return '';
  }
  if (STAGE_SEQUENCE.includes(safeStage)) {
    return safeStage;
  }
  return STAGE_ALIASES[safeStage] || '';
};

export const hasStagePassed = (stage = '', stageState = {}, thresholds = getAssessmentThresholds()) => (
  Number(stageState?.score || 0) >= getStagePassScore(stage, thresholds)
);

export const canAdvanceFromResume = (resumeStage = {}, thresholds = getAssessmentThresholds()) => (
  hasStagePassed('resume', resumeStage, thresholds)
);

export const createDefaultComprehensiveState = () => ({
  resume: createDefaultStageState({ analysisId: null, suggestions: [] }),
  questions: createDefaultStageState({ interviewId: null }),
  round_1: createDefaultStageState({ interviewId: null }),
  round_2: createDefaultStageState({ interviewId: null }),
  round_3: createDefaultStageState({ interviewId: null }),
  overall: {
    startTime: null,
    endTime: null,
    totalScore: 0,
    status: 'not_started',
    currentStage: 'resume',
    eliminationReason: '',
    finalSummary: '',
    finalSuggestion: ''
  }
});

export const normalizeComprehensiveState = (state = {}) => {
  const defaults = createDefaultComprehensiveState();
  const safeState = state && typeof state === 'object' ? state : {};
  const normalizedCurrentStage = normalizeComprehensiveStage(
    safeState?.overall?.currentStage || safeState?.currentStage || defaults.overall.currentStage
  ) || defaults.overall.currentStage;
  const legacyAudioStage = safeState.audio && typeof safeState.audio === 'object'
    ? safeState.audio
    : {};
  return {
    ...defaults,
    ...safeState,
    resume: {
      ...defaults.resume,
      ...(safeState.resume && typeof safeState.resume === 'object' ? safeState.resume : {}),
      passed: canAdvanceFromResume(safeState.resume || {})
    },
    questions: {
      ...defaults.questions,
      ...(safeState.questions && typeof safeState.questions === 'object' ? safeState.questions : {}),
      passed: hasStagePassed('questions', safeState.questions || {})
    },
    round_1: {
      ...defaults.round_1,
      ...legacyAudioStage,
      ...(safeState.round_1 && typeof safeState.round_1 === 'object' ? safeState.round_1 : {}),
      passed: hasStagePassed('round_1', safeState.round_1 || legacyAudioStage || {})
    },
    round_2: {
      ...defaults.round_2,
      ...(safeState.round_2 && typeof safeState.round_2 === 'object' ? safeState.round_2 : {}),
      passed: hasStagePassed('round_2', safeState.round_2 || {})
    },
    round_3: {
      ...defaults.round_3,
      ...(safeState.round_3 && typeof safeState.round_3 === 'object' ? safeState.round_3 : {}),
      passed: hasStagePassed('round_3', safeState.round_3 || {})
    },
    overall: {
      ...defaults.overall,
      ...(safeState.overall && typeof safeState.overall === 'object' ? safeState.overall : {}),
      currentStage: normalizedCurrentStage
    }
  };
};

export const buildComprehensiveAssessmentId = () => (
  `cmp_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
);

export const createAiAssessmentSession = ({
  jobId = '',
  assessmentId = '',
  state = {}
} = {}) => ({
  assessmentId: String(assessmentId || buildComprehensiveAssessmentId()).trim(),
  jobId: safeJobId(jobId),
  state: normalizeComprehensiveState(state),
  createdAt: nowIsoString(),
  lastUpdated: nowIsoString()
});

export const getActiveComprehensiveAssessmentId = (jobId) => {
  const safeId = safeJobId(jobId);
  if (!safeId) {
    return '';
  }
  return String(readStorage(buildActiveAssessmentStorageKey(safeId)) || '').trim();
};

export const setActiveComprehensiveAssessmentId = (jobId, assessmentId) => {
  const safeId = safeJobId(jobId);
  const safeAssessmentId = String(assessmentId || '').trim();
  if (!safeId) {
    return '';
  }
  if (!safeAssessmentId) {
    removeStorage(buildActiveAssessmentStorageKey(safeId));
    return '';
  }
  writeStorage(buildActiveAssessmentStorageKey(safeId), safeAssessmentId);
  return safeAssessmentId;
};

export const saveAiAssessmentSession = (session) => {
  const safeSession = {
    ...session,
    jobId: safeJobId(session?.jobId),
    assessmentId: String(session?.assessmentId || '').trim(),
    state: normalizeComprehensiveState(session?.state),
    createdAt: String(session?.createdAt || nowIsoString()),
    lastUpdated: nowIsoString()
  };
  if (!safeSession.jobId || !safeSession.assessmentId) {
    return null;
  }
  writeStorage(
    buildSessionStorageKey(safeSession.jobId, safeSession.assessmentId),
    JSON.stringify(safeSession)
  );
  setActiveComprehensiveAssessmentId(safeSession.jobId, safeSession.assessmentId);
  const legacyState = parseStoredJson(readStorage(LEGACY_COMPREHENSIVE_STATE_KEY));
  if (legacyState && safeJobId(legacyState.jobId) === safeSession.jobId) {
    removeStorage(LEGACY_COMPREHENSIVE_STATE_KEY);
  }
  return safeSession;
};

export const loadAiAssessmentSession = (jobId, assessmentId = '') => {
  const safeId = safeJobId(jobId);
  const safeAssessmentId = String(assessmentId || '').trim();
  if (!safeId || !safeAssessmentId) {
    return null;
  }
  const parsed = parseStoredJson(readStorage(buildSessionStorageKey(safeId, safeAssessmentId)));
  if (!parsed || typeof parsed !== 'object') {
    return null;
  }
  return {
    ...parsed,
    assessmentId: safeAssessmentId,
    jobId: safeId,
    state: normalizeComprehensiveState(parsed.state)
  };
};

const migrateLegacyComprehensiveState = (jobId) => {
  const safeId = safeJobId(jobId);
  if (!safeId) {
    return null;
  }
  const legacyState = parseStoredJson(readStorage(LEGACY_COMPREHENSIVE_STATE_KEY));
  if (!legacyState || safeJobId(legacyState.jobId) !== safeId) {
    return null;
  }
  const migratedSession = createAiAssessmentSession({
    jobId: safeId,
    state: legacyState
  });
  return saveAiAssessmentSession(migratedSession);
};

export const ensureActiveAiAssessmentSession = (
  jobId,
  { assessmentId = '', createIfMissing = true, forceNew = false } = {}
) => {
  const safeId = safeJobId(jobId);
  if (!safeId) {
    return null;
  }
  if (forceNew) {
    return saveAiAssessmentSession(createAiAssessmentSession({ jobId: safeId }));
  }

  const preferredAssessmentId = String(assessmentId || getActiveComprehensiveAssessmentId(safeId) || '').trim();
  let existing = preferredAssessmentId
    ? loadAiAssessmentSession(safeId, preferredAssessmentId)
    : null;

  if (!existing) {
    existing = migrateLegacyComprehensiveState(safeId);
  }

  if (!existing && createIfMissing) {
    existing = saveAiAssessmentSession(createAiAssessmentSession({ jobId: safeId }));
  }

  if (existing) {
    setActiveComprehensiveAssessmentId(safeId, existing.assessmentId);
  }
  return existing;
};

export const updateComprehensiveAssessmentState = (
  jobId,
  assessmentId,
  updater,
  { createIfMissing = true } = {}
) => {
  const session = ensureActiveAiAssessmentSession(jobId, {
    assessmentId,
    createIfMissing
  });
  if (!session) {
    return null;
  }
  const currentState = normalizeComprehensiveState(session.state);
  const nextState = typeof updater === 'function'
    ? updater(currentState, session)
    : updater;
  return saveAiAssessmentSession({
    ...session,
    state: normalizeComprehensiveState(nextState)
  });
};

export const createNewAiAssessmentSession = (jobId) => (
  ensureActiveAiAssessmentSession(jobId, { forceNew: true })
);

export const clearActiveComprehensiveAssessment = (jobId) => {
  const safeId = safeJobId(jobId);
  if (!safeId) {
    return;
  }
  removeStorage(buildActiveAssessmentStorageKey(safeId));
};

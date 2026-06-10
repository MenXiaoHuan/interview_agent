const ACTIVE_ASSESSMENT_PREFIX = 'activeComprehensiveAssessmentId:';
const ASSESSMENT_SESSION_PREFIX = 'comprehensiveAssessmentSession:';
const LEGACY_COMPREHENSIVE_STATE_KEY = 'comprehensiveTestState';

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

const buildSessionStorageKey = (jobId, assessmentId) => (
  `${ASSESSMENT_SESSION_PREFIX}${safeJobId(jobId)}:${String(assessmentId || '').trim()}`
);

const buildActiveAssessmentStorageKey = (jobId) => (
  `${ACTIVE_ASSESSMENT_PREFIX}${safeJobId(jobId)}`
);

export const createDefaultComprehensiveState = () => ({
  resume: {
    completed: false,
    inProgress: false,
    score: 0,
    analysisId: null,
    startTime: null,
    endTime: null,
    attempts: 0
  },
  questions: {
    completed: false,
    inProgress: false,
    score: 0,
    interviewId: null,
    startTime: null,
    endTime: null,
    attempts: 0
  },
  audio: {
    completed: false,
    inProgress: false,
    score: 0,
    assessmentId: null,
    startTime: null,
    endTime: null,
    attempts: 0
  },
  overall: {
    startTime: null,
    endTime: null,
    totalScore: 0,
    status: 'not_started'
  }
});

export const normalizeComprehensiveState = (state = {}) => {
  const defaults = createDefaultComprehensiveState();
  return {
    ...defaults,
    ...(state && typeof state === 'object' ? state : {}),
    resume: {
      ...defaults.resume,
      ...((state && typeof state === 'object' && state.resume) ? state.resume : {})
    },
    questions: {
      ...defaults.questions,
      ...((state && typeof state === 'object' && state.questions) ? state.questions : {})
    },
    audio: {
      ...defaults.audio,
      ...((state && typeof state === 'object' && state.audio) ? state.audio : {})
    },
    overall: {
      ...defaults.overall,
      ...((state && typeof state === 'object' && state.overall) ? state.overall : {})
    }
  };
};

export const buildComprehensiveAssessmentId = () => (
  `cmp_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
);

export const createComprehensiveAssessmentSession = ({
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

export const saveComprehensiveAssessmentSession = (session) => {
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

export const loadComprehensiveAssessmentSession = (jobId, assessmentId = '') => {
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
  const migratedSession = createComprehensiveAssessmentSession({
    jobId: safeId,
    state: legacyState
  });
  return saveComprehensiveAssessmentSession(migratedSession);
};

export const ensureActiveComprehensiveAssessmentSession = (
  jobId,
  { assessmentId = '', createIfMissing = true, forceNew = false } = {}
) => {
  const safeId = safeJobId(jobId);
  if (!safeId) {
    return null;
  }
  if (forceNew) {
    return saveComprehensiveAssessmentSession(createComprehensiveAssessmentSession({ jobId: safeId }));
  }

  const preferredAssessmentId = String(assessmentId || getActiveComprehensiveAssessmentId(safeId) || '').trim();
  let existing = preferredAssessmentId
    ? loadComprehensiveAssessmentSession(safeId, preferredAssessmentId)
    : null;

  if (!existing) {
    existing = migrateLegacyComprehensiveState(safeId);
  }

  if (!existing && createIfMissing) {
    existing = saveComprehensiveAssessmentSession(createComprehensiveAssessmentSession({ jobId: safeId }));
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
  const session = ensureActiveComprehensiveAssessmentSession(jobId, {
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
  return saveComprehensiveAssessmentSession({
    ...session,
    state: normalizeComprehensiveState(nextState)
  });
};

export const createNewComprehensiveAssessmentSession = (jobId) => (
  ensureActiveComprehensiveAssessmentSession(jobId, { forceNew: true })
);

export const clearActiveComprehensiveAssessment = (jobId) => {
  const safeId = safeJobId(jobId);
  if (!safeId) {
    return;
  }
  removeStorage(buildActiveAssessmentStorageKey(safeId));
};

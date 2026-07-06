import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const pageName = path.basename(currentDir);
const source = readFileSync(
  path.join(currentDir, "..", "..", "..", "pages", pageName, "index.vue"),
  "utf8",
);

describe("comprehensive ai flow", () => {
  it("removes special-assessment and report actions from ai-chat", () => {
    expect(source).not.toContain("choose_special");
    expect(source).not.toContain("upload_resume_special");
    expect(source).not.toContain("go_audio");
    expect(source).not.toContain("go_report");
    expect(source).not.toContain("view_resume_history");
    expect(source).not.toContain("view_questions_history");
    expect(source).not.toContain("view_audio_history");
    expect(source).not.toContain("view_comprehensive_history");
    expect(source).not.toContain("专项测评");
    expect(source).not.toContain("综合报告");
  });

  it("defines the canonical ai interview stage order and action labels", () => {
    expect(source).toContain("round_1");
    expect(source).toContain("round_2");
    expect(source).toContain("round_3");
    expect(source).toContain("AI 面试");
    expect(source).toContain("简历评测");
    expect(source).toContain("试题作答");
    expect(source).toContain("进入技术一面");
    expect(source).toContain("进入技术二面");
    expect(source).toContain("进入 HR 三面");
    expect(source).toContain("重新开始 AI 面试");
  });

  it("routes interview stages to the ai-prefixed execution pages", () => {
    expect(source).toContain("resume: '/pages/ai-resume/index'");
    expect(source).toContain("questions: '/pages/ai-questions/index'");
    expect(source).toContain("round_1: '/pages/ai-scenario/index'");
    expect(source).toContain("/pages/ai-hall/index");
    expect(source).not.toContain("/pages/comprehensive-resume/index");
    expect(source).not.toContain("/pages/comprehensive-questions/index");
    expect(source).not.toContain("/pages/comprehensive-scenario/index");
    expect(source).not.toContain("/pages/interview-interface/index");
    expect(source).not.toContain("/pages/history/index");
  });

  it("stores AIview conversations under the ai-prefixed assistant agent key", () => {
    expect(source).toContain("const ASSISTANT_CONVERSATION_AGENT_KEY = 'ai-interview-assistant'");
    expect(source).not.toContain("const ASSISTANT_CONVERSATION_AGENT_KEY = 'interview-assistant'");
  });

  it("renders recommended action buttons in the chat template", () => {
    expect(source).toContain('v-if="assistantActions.length > 0"');
    expect(source).toContain('v-for="action in assistantActions"');
    expect(source).toContain('@click="runAssistantAction(action)"');
    expect(source).toContain("recommended-action-btn");
  });

  it("uses deterministic local stage summaries for ai interview progression", () => {
    expect(source).toContain("const buildDeterministicAssistantActions =");
    expect(source).toContain("const buildLocalCompletionSummary =");
    expect(source).toContain("const submitCompletionFollowupToAssistant = async");
    expect(source).toContain("const buildResumeSuggestionLines =");
    expect(source).toContain("AI 改进建议");
    expect(source).toContain("简历评测未通过");
    expect(source).toContain("技术一面");
    expect(source).not.toContain("场景评测");
    expect(source).toContain("createNewAiAssessmentSession");
    expect(source).toContain("clearActiveComprehensiveAssessment");
    expect(source).toContain("getStagePassScore('resume', assessmentThresholds.value)");
    expect(source).toContain("低于 ${resumePassScore} 分门槛");
    expect(source).not.toContain("低于 70 分门槛");
    expect(source).not.toContain("safeScore >= 70");
  });

  it("prepares a fresh comprehensive session immediately after failure", () => {
    expect(source).toContain("const prepareNextComprehensiveSessionAfterFailure = () =>");
    expect(source).toContain("assessment_auto_reinitialized_after_failure");
    expect(source).toContain("const completedStageMap =");
    expect(source).toContain("!stagePassed(completedStage, comprehensiveState.value[completedStage])");
    expect(source).toContain("prepareNextComprehensiveSessionAfterFailure()");
    expect(source).toContain("currentComprehensiveAssessmentId.value = String(nextSession?.assessmentId || '').trim()");
    expect(source).toContain("comprehensiveState.value = normalizeComprehensiveState(nextSession?.state)");
  });

  it("restarts AI interview locally without asking for repeated confirmation", () => {
    expect(source).toContain("const handleRestartComprehensiveAssessment = async");
    expect(source).toContain("const restartedSession = resetComprehensiveState()");
    expect(source).toContain("assistantActions.value = buildDeterministicAssistantActions(comprehensiveState.value)");
    expect(source).toContain("await appendAiMessageWithTyping('好的，已为你重置本轮 AI 面试全部环节。");
    expect(source).not.toContain("await submitAssistantQuestion(buildAssistantActionQuestion(action, targetJob), { appendUserMessage: true })");
  });

  it("prefers the freshly reset assessment id over stale route query ids", () => {
    expect(source).toContain("assessmentId || currentComprehensiveAssessmentId.value || route.query.assessmentId || ''");
    expect(source).toContain("currentComprehensiveAssessmentId.value || route.query.assessmentId");
    expect(source).not.toContain("route.query.assessmentId || currentComprehensiveAssessmentId.value");
  });

  it("treats completion return assistant replies as a started conversation", () => {
    expect(source).toContain("item?.type === 'ai' && String(item?.content || '').trim()");
  });

  it("reacts to completion route query changes even when the page instance is reused", () => {
    expect(source).toContain("() => [route.query.completedType, route.query.timestamp, route.query.chatId]");
    expect(source).toContain("await handlePendingAssistantAction()");
  });

  it("consumes durable completion events when route query handling is missed", () => {
    expect(source).toContain("consumeAiAssessmentCompletion");
    expect(source).toContain("const consumeStoredPendingAssistantAction = () =>");
    expect(source).toContain("const inboxPending = consumeStoredPendingAssistantAction()");
    expect(source).toContain("const pending = externalPending || routePending || inboxPending");
  });

  it("handles resume completion events through the same assistant summary pipeline", () => {
    expect(source).toContain("const buildCompletionEventPendingAction =");
    expect(source).toContain("const handleCompletionEvent = async");
    expect(source).toContain("await handlePendingAssistantAction(eventPending)");
    expect(source).toContain("uni.$on('resumeCompleted', handleCompletionEvent)");
    expect(source).not.toContain("uni.$on('resumeCompleted', (data) =>");
  });

  it("keeps route job context when consuming completion returns", () => {
    expect(source).toContain("const resolveRouteJobId = () =>");
    expect(source).toContain("const routeJobId = resolveRouteJobId()");
    expect(source).toContain("currentJobId.value = routeJobId || String(safeSession.jobId || '').trim()");
    expect(source).toContain("currentJobId.value = String(route.query.jobId || currentJobId.value || '').trim()");
  });

  it("deduplicates completion return placeholder sessions by id and chatId", () => {
    expect(source).toContain("const sessionMatchesAnyKey = (sessionLike = {}, keys = []) =>");
    expect(source).toContain("Array.from(keys || [])");
    expect(source).toContain("collectConversationSessionKeys(sessionLike).some((item) => safeKeys.has(item))");
    expect(source).toContain("const findConversationRecordByKey = (key = '') =>");
    expect(source).toContain("conversationRecords.value.find((item) => sessionMatchesAnyKey(item, [key]))");
    expect(source).toContain("const activateConversationByKey = (key = '') =>");
    expect(source).toContain("const targetSession = activateConversationByKey(pending.chatId)");
    expect(source).toContain("...collectConversationSessionKeys(safeSession)");
    expect(source).toContain("!sessionMatchesAnyKey(item, idsToReplace)");
    expect(source).toContain("upsertConversationRecord(placeholderSession, [pending.chatId])");
    expect(source).not.toContain("conversationRecords.value = sortSessions([\\n        placeholderSession");
  });

  it("flushes completion return summaries into the active persistent session", () => {
    expect(source).toContain("const persistConversationStateNow = async () =>");
    expect(source).toContain("return flushConversationPersistence(snapshot)");
    expect(source).toContain("await persistConversationStateNow()");
    expect(source).toContain("const sourceSessionId = String(route.query.chatId || '').trim() || getActivePersistentSessionId() || getStoredAssistantConversationChatId()");
  });

  it("uses AI follow-up for completion returns before falling back to deterministic summaries", () => {
    expect(source).toContain("await submitCompletionFollowupToAssistant(pending, localReply)");
    expect(source).toContain("await submitAssistantQuestion(followupQuestion, {");
    expect(source).toContain("appendUserMessage: false");
    expect(source).toContain("fallbackReplyBuilder: () => fallbackReply");
    expect(source).toContain("await appendAiMessageWithTyping(fallbackReply)");
    expect(source).not.toContain("await appendAiMessageWithTyping(localReply);\n  if (pending.type === 'resume_completed'");
  });

  it("guards textarea auto-resize through a resolved native element", () => {
    expect(source).toContain("const getMessageInputElement = () =>");
    expect(source).toContain("const inputElement = getMessageInputElement()");
    expect(source).not.toContain("messageInputRef.value.style.height = 'auto'");
  });
});

import { describe, expect, it } from "vitest";

import {
  canAdvanceFromResume,
  getAssessmentThresholds,
  getStagePassScore,
  consumeAiAssessmentCompletion,
  createDefaultComprehensiveState,
  enqueueAiAssessmentCompletion,
  getStageSequence,
  hasStagePassed,
  normalizeComprehensiveState,
  normalizeComprehensiveStage,
  saveAssessmentThresholds,
} from "../../utils/comprehensive-assessment";

describe("comprehensive assessment state model", () => {
  it("uses the five-stage comprehensive pipeline", () => {
    const state = createDefaultComprehensiveState();

    expect(state).toHaveProperty("resume");
    expect(state).toHaveProperty("questions");
    expect(state).toHaveProperty("round_1");
    expect(state).toHaveProperty("round_2");
    expect(state).toHaveProperty("round_3");
    expect(state).not.toHaveProperty("audio");
  });

  it("exports the canonical stage sequence", () => {
    expect(getStageSequence()).toEqual([
      "resume",
      "questions",
      "round_1",
      "round_2",
      "round_3",
    ]);
  });

  it("normalizes legacy stage aliases into the new flow", () => {
    expect(normalizeComprehensiveStage("audio")).toBe("round_1");
    expect(normalizeComprehensiveStage("round_2")).toBe("round_2");
    expect(normalizeComprehensiveStage("report")).toBe("");
  });

  it("uses 80 as the default pass score for all AIview stages", () => {
    localStorage.clear();

    expect(getAssessmentThresholds().defaultPassScore).toBe(80);
    expect(getStagePassScore("resume")).toBe(80);
    expect(getStagePassScore("questions")).toBe(80);
    expect(getStagePassScore("round_1")).toBe(80);
    expect(canAdvanceFromResume({ score: 79 })).toBe(false);
    expect(canAdvanceFromResume({ score: 80 })).toBe(true);
    expect(hasStagePassed("questions", { score: 79 })).toBe(false);
    expect(hasStagePassed("questions", { score: 80 })).toBe(true);
  });

  it("supports custom stage pass scores with a persisted fallback default", () => {
    localStorage.clear();

    saveAssessmentThresholds({
      defaultPassScore: 85,
      stages: {
        resume: 75,
        questions: 90,
      },
    });

    expect(getStagePassScore("resume")).toBe(75);
    expect(getStagePassScore("questions")).toBe(90);
    expect(getStagePassScore("round_1")).toBe(85);
    expect(canAdvanceFromResume({ score: 74 })).toBe(false);
    expect(canAdvanceFromResume({ score: 75 })).toBe(true);
  });

  it("keeps resume suggestions in the assessment state", () => {
    const state = createDefaultComprehensiveState();
    expect(state.resume.suggestions).toEqual([]);

    const normalized = normalizeComprehensiveState({
      resume: {
        score: 76,
        suggestions: ["补充 Python 项目经验"],
      },
    });
    expect(normalized.resume.suggestions).toEqual(["补充 Python 项目经验"]);
  });

  it("queues and consumes completion events by assessment context", () => {
    localStorage.clear();

    enqueueAiAssessmentCompletion({
      type: "resume_completed",
      chatId: "chat:abc",
      jobId: "1",
      assessmentId: "cmp_001",
      score: 76,
      suggestions: ["补充 Python 项目经验"],
      timestamp: "100",
    });

    const event = consumeAiAssessmentCompletion({
      chatId: "chat:abc",
      jobId: "1",
      assessmentId: "cmp_001",
    });

    expect(event).toMatchObject({
      type: "resume_completed",
      chatId: "chat:abc",
      jobId: "1",
      assessmentId: "cmp_001",
      score: 76,
      suggestions: ["补充 Python 项目经验"],
    });
    expect(consumeAiAssessmentCompletion({ chatId: "chat:abc" })).toBeNull();
  });
});

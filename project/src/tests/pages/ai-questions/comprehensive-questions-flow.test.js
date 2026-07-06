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

describe("comprehensive questions flow", () => {
  it("does not keep a standalone post-exam analysis page", () => {
    expect(source).not.toContain("题目回顾区域");
    expect(source).not.toContain("AI详细解析");
    expect(source).not.toContain("获取AI详细解析");
  });

  it("routes the completed questions stage back into ai-chat orchestration", () => {
    expect(source).toContain("/pages/ai-chat/index");
    expect(source).toContain("completedType=questions");
    expect(source).not.toContain("/pages/comprehensive-report/index");
    expect(source).toContain("scheduleReturnToAiAfterSuccess");
    expect(source).toContain("const scheduleReturnToAiAfterSuccess = () => {\n  goToAudioAssessment();\n};");
    expect(source).toContain("返回AIview中...");
    expect(source).not.toContain("立即返回 AI");
    expect(source).not.toContain("正在返回 AI 对话框");
    expect(source).not.toContain("返回中...");
    expect(source).not.toContain("returnToAiTimer = setTimeout");
  });

  it("centers scenario answer input and limits it to 1000 characters", () => {
    expect(source).toContain('maxlength="1000"');
    expect(source).toContain("answer-length-hint");
    expect(source).toContain("{{ String(currentQuestion.userAnswer || '').length }}/1000");
    expect(source).toContain(".scenario-question {\n  display: flex;");
    expect(source).toContain("align-items: center;");
    expect(source).toContain(".scenario-textarea {\n  width: min(100%, 920px);");
    expect(source).toContain("margin: 0 auto;");
  });

  it("normalizes raw question scores to a 100-point stage score", () => {
    expect(source).toContain("const normalizeQuestionStageScore = (earnedScore = 0, maxScore = 100) =>");
    expect(source).toContain("score.value = normalizeQuestionStageScore(totalScore, totalPossibleScore)");
    expect(source).toContain("totalPossibleScore += Number(q.score || 0)");
    expect(source).not.toContain("score.value = Math.max(0, Math.min(100, Math.round(totalScore)))");
  });
});

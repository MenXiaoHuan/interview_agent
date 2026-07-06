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

describe("comprehensive resume flow", () => {
  it("does not render a standalone analysis result page", () => {
    expect(source).not.toContain("评测结果");
    expect(source).not.toContain("AI改进建议");
    expect(source).not.toContain("查看分析报告");
  });

  it("returns to ai-chat after completion", () => {
    expect(source).toContain("/pages/ai-chat/index");
    expect(source).toContain("completedType=resume");
    expect(source).not.toContain("/pages/comprehensive-report/index");
    expect(source).toContain("scheduleReturnToAiAfterSuccess");
    expect(source).toContain("const scheduleReturnToAiAfterSuccess = () => {\n  viewReport();\n};");
    expect(source).toContain("返回AIview中...");
    expect(source).not.toContain("立即返回 AI");
    expect(source).not.toContain("正在返回 AI 对话框");
    expect(source).not.toContain("返回中...");
    expect(source).not.toContain("returnToAiTimer = setTimeout");
  });

  it("queues a durable completion event before returning to ai-chat", () => {
    expect(source).toContain("enqueueAiAssessmentCompletion");
    expect(source).toContain("type: 'resume_completed'");
    expect(source).toContain("suggestions: suggestions.value");
    expect(source).toContain("assessmentId: assessmentId.value");
  });

  it("uses three resume analysis steps without a standalone report generation step", () => {
    expect(source).toContain("const analysisSteps = [");
    expect(source).toContain("'解析文档'");
    expect(source).toContain("'提取信息'");
    expect(source).toContain("'匹配分析'");
    expect(source).not.toContain("'生成报告'");
    expect(source).not.toContain("currentStep.value = 4");
  });

  it("keeps completion fallback on the ai-chat route with completion query", () => {
    expect(source).toContain("const targetUrl = buildNextComprehensiveStepUrl()");
    expect(source).toContain("router.replace(targetUrl)");
    expect(source).toContain("window.location.href = `/#${targetUrl}`");
    expect(source).not.toContain("uni.$emit('resumeCompleted'");
  });

  it("normalizes backend resume scores from 0-100 overall score fields", () => {
    expect(source).toContain("const normalizeResumeScore = (analysisResult = {}) =>");
    expect(source).toContain("analysisResult.overallScore");
    expect(source).toContain("analysisResult.overall_score");
    expect(source).toContain("const normalizeResumeScoreValue = (value = 0) =>");
    expect(source).toContain("safeScore > 0 && safeScore <= 10 ? safeScore * 10 : safeScore");
    expect(source).toContain("resumeScore.value = normalizeResumeScore(analysisResult)");
    expect(source).not.toContain("resumeScore.value = analysisResult.score || 0");
  });

  it("keeps resume analysis payload focused on total score and suggestions", () => {
    expect(source).toContain("suggestions.value = normalizeResumeSuggestions(analysisResult)");
    expect(source).toContain("overallScore: resumeScore.value");
    expect(source).toContain("aiAnalysis: buildResumeAnalysisSummary()");
    expect(source).not.toContain("skillMatch.value = analysisResult.skillMatch || 0");
    expect(source).not.toContain("experienceMatch.value = analysisResult.experienceMatch || 0");
    expect(source).not.toContain("educationMatch.value = analysisResult.educationMatch || 0");
    expect(source).not.toContain("communicationSkill.value = analysisResult.communicationSkill || 0");
    expect(source).not.toContain("leadershipSkill.value = analysisResult.leadershipSkill || 0");
    expect(source).not.toContain("技能匹配度：${skillMatch.value}");
  });
});

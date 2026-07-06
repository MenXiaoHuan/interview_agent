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
const apiSource = readFileSync(
  path.join(currentDir, "..", "..", "..", "utils", "api", "pages", pageName, "index.js"),
  "utf8",
);

describe("comprehensive scenario rounds", () => {
  it("supports the three interview rounds", () => {
    expect(source).toContain("round_1");
    expect(source).toContain("round_2");
    expect(source).toContain("round_3");
    expect(source).toContain("技术一面");
    expect(source).toContain("技术二面");
    expect(source).toContain("HR 三面");
  });

  it("returns to ai-chat instead of the standalone report page", () => {
    expect(source).not.toContain("/pages/comprehensive-report/index");
    expect(source).not.toContain("buildComprehensiveReportUrl");
    expect(source).toContain("completedType: roundType.value");
    expect(source).toContain("scheduleReturnToAiAfterSuccess");
    expect(source).toContain("const scheduleReturnToAiAfterSuccess = () => {\n  goToMainComprehensivePage();\n};");
    expect(source).toContain("返回AIview中...");
    expect(source).not.toContain("立即返回 AI");
    expect(source).not.toContain("正在返回 AI 对话框");
    expect(source).not.toContain("返回中...");
    expect(source).not.toContain("returnToAiTimer = setTimeout");
  });

  it("archives interview rounds with total score and AI assessment JSON only", () => {
    expect(source).toContain("overallScore: parseInt(overallScore.value)");
    expect(source).toContain("aiAssessment: aiAssessmentResult.value || JSON.stringify(aiAssessment.value)");
    expect(source).not.toContain("fluencyScore: parseInt(fluencyScore.value)");
    expect(source).not.toContain("emotionScore: parseInt(emotionScore.value)");
    expect(source).not.toContain("relevanceScore: parseInt(relevanceScore.value)");
    expect(source).not.toContain("adaptabilityScore: parseInt(adaptabilityScore.value)");
    expect(apiSource).toContain("aiAssessment: data.aiAssessment");
    expect(apiSource).not.toContain("communicationScore");
    expect(apiSource).not.toContain("logicScore");
    expect(apiSource).not.toContain("professionalismScore");
  });
});

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
const pagesJson = readFileSync(
  path.join(currentDir, "..", "..", "..", "pages.json"),
  "utf8",
);

describe("interview interface routing", () => {
  it("routes the smart interview module to the dedicated ai-chat page", () => {
    expect(source).toContain("goToInterviewAI");
    expect(source).toContain("/pages/ai-chat/index");
    expect(source).not.toContain("/pages/interview-ai/index?jobId=");
  });

  it("uses the interview hall copy and AIview-centric module description", () => {
    expect(source).toContain("<h1>面试大厅</h1>");
    expect(source).toContain("AI 模拟面试");
    expect(source).toContain("AIview");
    expect(source).toContain("简历投递");
    expect(source).toContain("试题作答");
    expect(source).toContain("一面");
    expect(source).toContain("二面");
    expect(source).toContain("三面");
    expect(source).toContain("统一承接简历投递、试题作答与三轮面试");
    expect(source).toContain("用一个对话入口串起岗位理解、过程承接、阶段反馈和最终建议");
    expect(source).not.toContain("专项测评");
    expect(source).not.toContain("goToInterviewResume");
    expect(source).not.toContain("goToQuestionsAnswering");
    expect(source).not.toContain("goToScenarioAssessment");
  });

  it("makes the AIview entry visibly clickable without an extra CTA button", () => {
    expect(source).toContain("ai-view-entry-card");
    expect(source).toContain("glass-ai-card");
    expect(source).toContain("ai-view-dynamic-island");
    expect(source).toContain("island-orb");
    expect(source).toContain("island-text");
    expect(source).toContain("island-arrow");
    expect(source).toContain("width: 0");
    expect(source).toContain("opacity: 0");
    expect(source).toContain("max-width: 92px");
    expect(source).toContain("width: 34px");
    expect(source).toContain("height: 34px");
    expect(source).toContain("justify-content: center");
    expect(source).toContain("role=\"button\"");
    expect(source).toContain("从这里开始完整 AI 模拟面试");
    expect(source).toContain("开始 AI 面试");
    expect(source).toContain("ai-view-tooltip");
    expect(source).toContain("bottom: 16px");
    expect(source).toContain("right: 16px");
    expect(source).toContain("cursor: pointer");
    expect(source).not.toContain("点击开始面试之旅");
    expect(source).not.toContain("ai-view-cta");
    expect(source).not.toContain("cta-arrow");
    expect(source).not.toContain("threshold-settings-btn");
    expect(source).not.toContain("通过标准设置");
    expect(source).not.toContain("测评标准");
  });

  it("renders AIview job insights for the selected job without manual refresh controls", () => {
    expect(source).toContain("compact-hall-layout");
    expect(source).toContain("insights-module-section");
    expect(source).toContain('<h2 class="section-title">历史洞察</h2>');
    expect(source).toContain("历史洞察");
    expect(source).not.toContain("AIview 岗位洞察");
    expect(source).toContain("fetchAiviewJobInsights");
    expect(source).toContain("insightStages");
    expect(source).toContain("简历投递");
    expect(source).toContain("试题作答");
    expect(source).toContain("一面");
    expect(source).toContain("二面");
    expect(source).toContain("三面");
    expect(source).toContain("近 7 天 {{ formatCount(stage.attemptCount) }} 次");
    expect(source).toContain("规则建议：{{ stage.advice }}");
    expect(source).toContain("<span>平均分</span>");
    expect(source).not.toContain("平均最高分");
    expect(source).not.toContain("advice-label");
    expect(source).toContain("glass-insight-card");
    expect(source).toContain("line-clamp");
    expect(source).toContain("color: #ffffff");
    expect(source).not.toContain("样本 {{ stage.participantCount }} 人");
    expect(source).not.toContain("洞察当前岗位在五个环节的表现");
    expect(source).not.toContain("刷新洞察");
    expect(source).not.toContain("insights-refresh-btn");
    expect(source).not.toContain("insights-header");
    expect(source).not.toContain("insights-summary");
  });

  it("opens a centered detail modal when an insight card is clicked", () => {
    expect(source).toContain("@click=\"openInsightDetail(stage)\"");
    expect(source).toContain("@keydown.enter=\"openInsightDetail(stage)\"");
    expect(source).toContain("selectedInsightStage");
    expect(source).toContain("insight-detail-overlay");
    expect(source).toContain("insight-detail-modal");
    expect(source).toContain("insight-detail-close");
    expect(source).toContain("background: transparent");
    expect(source).toContain("border-radius: 0");
    expect(source).toContain("insight-detail-pop");
    expect(source).toContain("@click.self=\"closeInsightDetail\"");
    expect(source).toContain("closeInsightDetail");
    expect(source).toContain("transform: scale");
  });

  it("registers the ai-chat page and removes the legacy interview-ai page", () => {
    expect(pagesJson).toContain('"path": "pages/ai-hall/index"');
    expect(pagesJson).toContain('"path": "pages/ai-chat/index"');
    expect(pagesJson).toContain('"path": "pages/ai-resume/index"');
    expect(pagesJson).toContain('"path": "pages/ai-questions/index"');
    expect(pagesJson).toContain('"path": "pages/ai-scenario/index"');
    expect(pagesJson).not.toContain('"path": "pages/interview-ai/index"');
    expect(pagesJson).not.toContain('"path": "pages/interview-resume/index"');
    expect(pagesJson).not.toContain('"path": "pages/interview-questions/index"');
    expect(pagesJson).not.toContain('"path": "pages/interview-scenario/index"');
    expect(pagesJson).not.toContain('"path": "pages/interview-interface/index"');
    expect(pagesJson).not.toContain('"path": "pages/comprehensive-resume/index"');
    expect(pagesJson).not.toContain('"path": "pages/comprehensive-questions/index"');
    expect(pagesJson).not.toContain('"path": "pages/comprehensive-scenario/index"');
    expect(pagesJson).not.toContain('"path": "pages/comprehensive-report/index"');
    expect(pagesJson).not.toContain('"path": "pages/history/index"');
    expect(pagesJson).not.toContain('"path": "pages/admin/question-management/index"');
    expect(pagesJson).not.toContain('"path": "pages/admin/scenario-management/index"');
  });
});

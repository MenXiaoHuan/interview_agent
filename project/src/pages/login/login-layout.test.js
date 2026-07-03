import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const source = readFileSync(path.join(currentDir, "index.vue"), "utf8");

describe("login page layout", () => {
  it("uses a two-column brand and form layout while preserving auth entry points", () => {
    expect(source).toContain("login-layout");
    expect(source).toContain("login-showcase");
    expect(source).toContain("login-panel");
    expect(source).not.toContain("login-panel-card");
    expect(source).not.toContain("panel-form");
    expect(source).not.toContain("showcase-brand-mark");
    expect(source).not.toContain("showcase-badge");
    expect(source).not.toContain("showcase-stat-list");
    expect(source).toContain("一站式智能面试体验");
    expect(source).toContain("AI 面试、专项题作答、简历评测、场景评测与综合报告");
    expect(source).toContain("覆盖岗位选择到报告复盘的完整训练闭环");
    expect(source).toContain("多模态评测结合 AI 能力生成针对性反馈");
    expect(source).toContain("历史记录与综合报告沉淀每一次训练结果");
    expect(source).toContain("openForgotModal");
    expect(source).not.toContain("fp-overlay");
    expect(source).not.toContain("fp-close");
    expect(source).toContain("fp-subtitle");
    expect(source).toContain("fp-section-label");
    expect(source).not.toContain("fp-inline-hint");
    expect(source).toContain("forgot-panel-header");
    expect(source).toContain(".fp-code-field .fp-input");
    expect(source).toContain("box-sizing: border-box");
    expect(source).toContain('router-link to="/pages/register/index"');
    expect(source).toContain("handleLogin");
  });
});

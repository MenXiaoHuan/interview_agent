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

describe("ai chat page layout", () => {
  it("uses a full-page workspace instead of an overlay card", () => {
    const dedicatedContainerBlock = source.match(
      /\.ai-interview-container\.dedicated-page \.ai-chat-container \{[\s\S]*?\n\}/,
    )?.[0] || "";

    expect(source).toContain('class="ai-chat-shell"');
    expect(source).not.toContain('class="ai-chat-overlay"');
    expect(source).toContain("width: 100%");
    expect(source).toContain("min-height: 100vh");
    expect(dedicatedContainerBlock).toContain("border-radius: 0");
    expect(dedicatedContainerBlock).toContain("box-shadow: none");
  });

  it("uses the refreshed sidebar header and new conversation entry", () => {
    expect(source).toContain("sidebar-primary-action");
    expect(source).toContain("新建对话");
    expect(source).toContain("最近对话");
    expect(source).toContain("AIview");
    expect(source).toContain('v-if="!sidebarCollapsed" class="sidebar-brand"');
    expect(source).toContain("收起侧边栏");
    expect(source).toContain("新建对话");
    expect(source).toContain("返回上一界面");
    expect(source).not.toContain("sidebar-shortcut-item");
    expect(source).not.toContain('aria-label="搜索会话"');
    expect(source).toContain("width: 64px");
    expect(source).not.toContain('<span class="ai-name">AI助手</span>');
    expect(source).toContain("<textarea");
    expect(source).toContain("border-radius: 18px");
    expect(source).toContain("background: linear-gradient(135deg, rgba(255, 255, 255, 0.16), rgba(125, 211, 252, 0.10))");
  });

  it("uses a subtle default composer state with a distinct focus state", () => {
    expect(source).toContain("border: 1px solid rgba(186, 230, 253, 0.24)");
    expect(source).toContain("background: rgba(255, 255, 255, 0.10)");
    expect(source).toContain("backdrop-filter: blur(18px) saturate(145%)");
    expect(source).toContain("border-color: rgba(186, 230, 253, 0.52)");
    expect(source).toContain("box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.16)");
    expect(source).not.toContain("composer-footer");
  });

  it("aligns AI chat visual style with the interview hall starry glass UI", () => {
    expect(source).toContain('<div id="stars"></div>');
    expect(source).toContain('<div id="stars2"></div>');
    expect(source).toContain('<div id="stars3"></div>');
    expect(source).toContain("ai-chat-glass-panel");
    expect(source).toContain("background: linear-gradient(135deg, #0a1929 0%, #1a1f35 100%)");
    expect(source).toContain('@use "sass:math";');
    expect(source).toContain('@use "sass:string";');
    expect(source).toContain("@function multiple-box-shadow($n)");
    expect(source).toContain("$shadows-small: multiple-box-shadow(1000)");
    expect(source).toContain("$shadows-medium: multiple-box-shadow(300)");
    expect(source).toContain("$shadows-large: multiple-box-shadow(150)");
    expect(source).toContain("#stars, #stars2, #stars3");
    expect(source).toContain("box-shadow: $shadows-small");
    expect(source).toContain("box-shadow: $shadows-medium");
    expect(source).toContain("box-shadow: $shadows-large");
    expect(source).toContain("backdrop-filter: blur(18px) saturate(145%)");
    expect(source).toContain("rgba(255, 255, 255, 0.12)");
    expect(source).toContain("animation: animChatStar");
    expect(source).toContain("transform: translateY(-2560px)");
    expect(source).not.toContain("chat-stars-random-field");
    expect(source).not.toContain("chat-nebula-layer");
    expect(source).not.toContain("shootingStar");
    expect(source).not.toContain("chat-stars-small::after");
    expect(source).not.toContain("chat-stars-small::before");
    expect(source).not.toContain("rgba(124, 77, 255");
    expect(source).not.toContain("rgba(168, 85, 247");
    expect(source).not.toContain("rgba(244, 114, 182");
    expect(source).not.toContain("transform: translateY(-420px)");
  });

  it("uses compact non-overlapping tooltips below sidebar actions", () => {
    expect(source).toContain("tooltip-bubble");
    expect(source).toContain("top: calc(100% + 10px)");
    expect(source).toContain("left: 50%");
    expect(source).toContain("transform: translateX(-50%) translateY(-4px)");
    expect(source).toContain("background: rgba(15, 23, 42, 0.72)");
    expect(source).toContain('<span class="tooltip-bubble tooltip-right">新建对话</span>');
    expect(source).not.toContain("background: rgba(33, 33, 33, 0.92)");
  });

  it("keeps thinking text white and action icon hover colors stable", () => {
    expect(source).toContain(".ai-interview-container.dedicated-page .inline-thinking,\n.ai-interview-container.dedicated-page .inline-thinking-text {\n  color: #ffffff;");
    expect(source).toContain(".ai-interview-container.dedicated-page .sidebar-toggle-btn:hover,\n.ai-interview-container.dedicated-page .sidebar-icon-btn:hover,\n.ai-interview-container.dedicated-page .chat-back-btn:hover {\n  color: #6b7280;");
    expect(source).toContain(".ai-interview-container.dedicated-page .sidebar-toggle-btn,\n.ai-interview-container.dedicated-page .sidebar-icon-btn,\n.ai-interview-container.dedicated-page .chat-back-btn {\n  color: #6b7280;");
    expect(source).toContain(".eye-care .sidebar-toggle-btn:hover {\n  color: #4d6848;");
    expect(source).toContain(".eye-care .chat-back-btn:hover {\n  color: #486244;");
  });

  it("aligns eye-care mode with the ai-hall dark green glass theme", () => {
    expect(source).toContain(".ai-interview-container.dedicated-page.eye-care {\n  background: linear-gradient(135deg, #1c2a1f 0%, #2a3c2e 100%);");
    expect(source).toContain(".ai-interview-container.dedicated-page.eye-care #stars,\n.ai-interview-container.dedicated-page.eye-care #stars2,\n.ai-interview-container.dedicated-page.eye-care #stars3 {");
    expect(source).toContain("opacity: 0.42;");
    expect(source).toContain(".ai-interview-container.dedicated-page.eye-care .chat-sidebar,\n.ai-interview-container.dedicated-page.eye-care .chat-header,\n.ai-interview-container.dedicated-page.eye-care .input-wrapper,\n.ai-interview-container.dedicated-page.eye-care .recommended-action-card {");
    expect(source).toContain("background: rgba(255, 255, 255, 0.10);");
    expect(source).toContain("border: 1px solid rgba(144, 198, 149, 0.24);");
    expect(source).toContain("backdrop-filter: blur(18px) saturate(145%);");
    expect(source).toContain(".ai-interview-container.dedicated-page.eye-care .message.user .message-bubble {\n  background: rgba(255, 255, 255, 0.14);");
    expect(source).toContain(".ai-interview-container.dedicated-page.eye-care .message-content,\n.ai-interview-container.dedicated-page.eye-care .message.user .message-content,\n.ai-interview-container.dedicated-page.eye-care .message.ai .message-content {\n  color: rgba(255, 255, 255, 0.92);");
  });

  it("removes legacy non-dedicated branches and feature modal remnants", () => {
    expect(source).not.toContain('v-if="!isDedicatedAiChatPage" class="cyber-background"');
    expect(source).not.toContain('v-if="!isDedicatedAiChatPage" class="main-content"');
    expect(source).not.toContain("showFeatureModal");
    expect(source).not.toContain("featureMap =");
    expect(source).not.toContain("feature-overlay");
    expect(source).not.toContain("handleComposerPrimaryAction");
  });

  it("uses a compact confirmation modal instead of a stretched close bar", () => {
    expect(source).toContain("confirm-close-btn");
    expect(source).toContain("position: absolute");
    expect(source).toContain("width: 32px");
    expect(source).toContain("height: 32px");
    expect(source).toContain("display: flex");
    expect(source).toContain("justify-content: flex-end");
  });

  it("keeps the confirmation modal palette aligned with the page theme", () => {
    expect(source).toContain("background: rgba(2, 8, 23, 0.72)");
    expect(source).toContain("backdrop-filter: blur(18px) saturate(145%)");
    expect(source).toContain("background: linear-gradient(135deg, rgba(15, 23, 42, 0.86), rgba(8, 47, 73, 0.74))");
    expect(source).toContain("border: 1px solid rgba(186, 230, 253, 0.24)");
    expect(source).toContain("box-shadow: 0 28px 80px rgba(2, 8, 23, 0.48), inset 0 1px 0 rgba(255, 255, 255, 0.12)");
    expect(source).toContain("color: rgba(255, 255, 255, 0.92)");
    expect(source).not.toContain("background: linear-gradient(180deg, #ffffff, #f7fbff)");
    expect(source).not.toContain("background: #f8fbff");
  });

  it("exposes AIview pass score settings from ai-chat header only", () => {
    expect(source).toContain('<teleport to="body">');
    expect(source).toContain("threshold-settings-btn");
    expect(source).toContain("threshold-dynamic-island");
    expect(source).toContain("threshold-island-orb");
    expect(source).toContain("threshold-island-title");
    expect(source).toContain("showThresholdSettings");
    expect(source).toContain("通过标准");
    expect(source).not.toContain("默认 80 分");
    expect(source).toContain("threshold-settings-panel");
    expect(source).toContain(":class=\"{ 'is-open': showThresholdSettings }\"");
    expect(source).toContain(".threshold-settings-btn:hover .threshold-island-copy");
    expect(source).toContain("position: fixed");
    expect(source).toContain("z-index: 6000");
    expect(source).toContain("width: min(360px, calc(100vw - 32px))");
    expect(source).toContain("max-height: min(520px, calc(100vh - 112px))");
    expect(source).toContain("overflow-y: auto");
    expect(source).toContain(".threshold-settings-panel::before");
    expect(source).toContain("right: 42px");
    expect(source).toContain("transform: rotate(45deg)");
    expect(source).toContain("grid-template-columns: 1fr");
    expect(source).toContain("flex-direction: row");
    expect(source).toContain("justify-content: space-between");
    expect(source).toContain("width: 92px");
    expect(source).toContain("height: 36px");
    expect(source).toContain("saveAssessmentThresholds");
    expect(source).toContain("getAssessmentThresholds");
  });

  it("keeps pass score settings readable in eye-care mode", () => {
    expect(source).toContain(".eye-care .threshold-island-title { color: #ffffff; background: none; -webkit-text-fill-color: #ffffff;");
    expect(source).toContain(".eye-care .threshold-settings-panel { background: linear-gradient(135deg, rgba(28, 42, 31, 0.92), rgba(42, 60, 46, 0.82));");
    expect(source).toContain(".eye-care .threshold-settings-header strong { color: #ffffff; }");
    expect(source).toContain(".eye-care .threshold-field { color: rgba(255, 255, 255, 0.74); }");
    expect(source).toContain(".eye-care .threshold-field input { background: rgba(255, 255, 255, 0.10); color: #ffffff;");
  });

  it("uses more readable user bubbles and recommended action copy colors", () => {
    expect(source).toContain("background: rgba(255, 255, 255, 0.14)");
    expect(source).toContain("box-shadow: 0 12px 28px rgba(15, 23, 42, 0.18)");
    expect(source).toContain(".ai-interview-container.dedicated-page .message.user .message-bubble");
    expect(source).toContain("border: 1px solid rgba(186, 230, 253, 0.28)");
    expect(source).toContain("color: #ffffff");
    expect(source).toContain("color: rgba(255, 255, 255, 0.68)");
    expect(source).toContain("background: rgba(255, 255, 255, 0.12)");
    expect(source).toContain("color: rgba(255, 255, 255, 0.92)");
  });
});

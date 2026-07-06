import { describe, expect, it } from "vitest";
import { existsSync, readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const srcDir = path.join(currentDir, "..", "..");
const apiPagesDir = path.join(srcDir, "utils", "api", "pages");

const readSource = (relativePath) => readFileSync(path.join(srcDir, relativePath), "utf8");

describe("page scoped api modules", () => {
  it("keeps api foundation files inside utils/api", () => {
    ["api-config.js", "request.js"].forEach((fileName) => {
      expect(existsSync(path.join(srcDir, "utils", "api", fileName))).toBe(true);
      expect(existsSync(path.join(srcDir, "utils", fileName))).toBe(false);
    });
    expect(existsSync(path.join(srcDir, "utils", "api", "api.js"))).toBe(false);
    expect(existsSync(path.join(srcDir, "utils", "api.js"))).toBe(false);
  });

  it("keeps page api modules under utils/api/pages by page name", () => {
    [
      "admin",
      "ai-chat",
      "ai-hall",
      "ai-resume",
      "ai-questions",
      "ai-scenario",
      "chat-hall",
      "home",
      "landing",
      "leaderboard",
      "login",
      "personal-center",
      "job-selection",
      "register",
    ].forEach((pageName) => {
      expect(existsSync(path.join(apiPagesDir, pageName, "index.js"))).toBe(true);
    });
  });

  it("uses page scoped api modules from the core AI pages", () => {
    expect(readSource("pages/ai-chat/index.vue")).toContain("@/utils/api/pages/ai-chat");
    expect(readSource("pages/ai-resume/index.vue")).toContain("@/utils/api/pages/ai-resume");
    expect(readSource("pages/ai-questions/index.vue")).toContain("@/utils/api/pages/ai-questions");
    expect(readSource("pages/ai-scenario/index.vue")).toContain("@/utils/api/pages/ai-scenario");
    expect(readSource("pages/home/index.vue")).toContain("@/utils/api/pages/home");
    expect(readSource("pages/login/index.vue")).toContain("@/utils/api/pages/login");
    expect(readSource("pages/personal-center/index.vue")).toContain("@/utils/api/pages/personal-center");
  });

  it("does not re-export the deleted aggregate api module from page api modules", () => {
    const pageModules = [
      "admin",
      "ai-chat",
      "ai-hall",
      "ai-resume",
      "ai-questions",
      "ai-scenario",
      "chat-hall",
      "home",
      "landing",
      "leaderboard",
      "login",
      "personal-center",
      "job-selection",
      "register",
    ];

    pageModules.forEach((pageName) => {
      const source = readFileSync(path.join(apiPagesDir, pageName, "index.js"), "utf8");
      expect(source).not.toContain("../../api.js");
      expect(source).not.toContain("../../../api.js");
    });
  });

  it("keeps AIview page api modules on the ai-prefixed agent keys", () => {
    const aiApiSource = [
      readFileSync(path.join(apiPagesDir, "ai-chat", "index.js"), "utf8"),
      readFileSync(path.join(apiPagesDir, "ai-resume", "index.js"), "utf8"),
      readFileSync(path.join(apiPagesDir, "ai-questions", "index.js"), "utf8"),
      readFileSync(path.join(apiPagesDir, "ai-scenario", "index.js"), "utf8"),
    ].join("\n");

    [
      "ai-interview-assistant",
      "ai-resume-analysis",
      "ai-question-generation",
      "ai-question-analysis",
      "ai-question-scoring",
      "ai-round-question-generation",
      "ai-round-evaluation",
    ].forEach((agentKey) => {
      expect(aiApiSource).toContain(`'${agentKey}'`);
    });

    [
      "interview-assistant",
      "resume-analysis",
      "question-analysis",
      "scenario-question-gen",
      "scenario-audio-evaluation",
      "scenario-question-scoring",
      "comprehensive-question-generation",
      "comprehensive-resume-analysis",
    ].forEach((legacyAgentKey) => {
      expect(aiApiSource).not.toContain(`'${legacyAgentKey}'`);
    });
  });

  it("keeps AIview job insights under the ai-hall page api module", () => {
    const aiHallApiSource = readFileSync(path.join(apiPagesDir, "ai-hall", "index.js"), "utf8");

    expect(aiHallApiSource).toContain("/api/aiview-insights/job/");
    expect(aiHallApiSource).toContain("/last-7-days");
    expect(aiHallApiSource).toContain("fetchAiviewJobInsights");
    expect(aiHallApiSource).not.toContain("../../api.js");
    expect(aiHallApiSource).not.toContain("../../../api.js");
  });
});

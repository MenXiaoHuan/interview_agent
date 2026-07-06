import { describe, expect, it } from "vitest";
import { existsSync, readdirSync, readFileSync } from "node:fs";
import path from "node:path";

const repoRoot = path.resolve(__dirname, "../../../..");
const backendRoot = path.join(repoRoot, "backend");
const skillsDir = path.join(backendRoot, "src/main/resources/skills");

describe("AIview backend agent skills", () => {
  it("keeps only AIview-named agent skills", () => {
    expect(readdirSync(skillsDir).sort()).toEqual([
      "ai-interview-assistant",
      "ai-question-analysis",
      "ai-question-generation",
      "ai-question-scoring",
      "ai-resume-analysis",
      "ai-round-evaluation",
      "ai-round-question-generation",
    ]);
  });

  it("removes legacy comprehensive and scenario agent keys from backend routing", () => {
    const files = [
      "src/main/java/com/multimodal/interview/reactagent/ReactAgentConfig.java",
      "src/main/java/com/multimodal/interview/reactagent/ReactAgentRouter.java",
    ].map((relativePath) => readFileSync(path.join(backendRoot, relativePath), "utf8"));

    files.forEach((source) => {
      [
        "resume-analysis",
        "scenario-evaluation",
        "scenario-question-gen",
        "scenario-audio-evaluation",
        "comprehensive-resume-analysis",
        "comprehensive-question-generation",
        "scenario-question-scoring",
        "question-analysis",
        "interview-assistant",
      ].forEach((legacyKey) => {
        expect(source).not.toContain(`"${legacyKey}"`);
      });
    });
  });

  it("does not expose legacy resume history tools to AIview agents", () => {
    const config = readFileSync(
      path.join(backendRoot, "src/main/java/com/multimodal/interview/reactagent/ReactAgentConfig.java"),
      "utf8",
    );

    expect(config).not.toContain("ResumeController");
    expect(config).not.toContain("resumeController");
    expect(config).not.toContain("resume_get_history");
    expect(config).not.toContain("resume_restore_content");
  });

  it("keeps matching SKILL names for every AIview agent directory", () => {
    readdirSync(skillsDir).forEach((skillName) => {
      const skillFile = path.join(skillsDir, skillName, "SKILL.md");
      expect(existsSync(skillFile)).toBe(true);
      expect(readFileSync(skillFile, "utf8")).toContain(`name: ${skillName}`);
    });
  });

  it("keeps resume analysis output focused on 100-point total score and suggestions", () => {
    const skill = readFileSync(path.join(skillsDir, "ai-resume-analysis", "SKILL.md"), "utf8");
    const structuredOutput = readFileSync(
      path.join(backendRoot, "src/main/java/com/multimodal/interview/reactagent/output/AgentStructuredOutputs.java"),
      "utf8",
    );

    expect(skill).toContain("score` 必须是 0-100");
    expect(skill).toContain("只返回 `score` 和 `suggestions`");
    ["skillMatch", "experienceMatch", "educationMatch", "communicationSkill", "leadershipSkill"].forEach((field) => {
      expect(skill).not.toContain(field);
      expect(structuredOutput).not.toContain(`private Integer ${field}`);
    });
  });
});

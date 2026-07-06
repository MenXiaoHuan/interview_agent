import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const readApiModule = (pageName) => readFileSync(
  path.join(currentDir, "..", "..", "utils", "api", "pages", pageName, "index.js"),
  "utf8",
);

describe("AIview assessment api payloads", () => {
  it("forwards assessmentSessionId through AIview stage archive requests", () => {
    const source = [
      readApiModule("ai-resume"),
      readApiModule("ai-questions"),
      readApiModule("ai-scenario"),
    ].join("\n");
    expect(source).toContain("/api/ai-assessment/resume");
    expect(source).toContain("/api/ai-assessment/question");
    expect(source).toContain("/api/ai-assessment/interview-round");
    expect(source).not.toContain("/api/comprehensive-history");
    expect(source).toContain("assessmentSessionId: data.assessmentSessionId");
    expect(source.match(/assessmentSessionId: data\.assessmentSessionId/g)?.length).toBe(3);
  });
});

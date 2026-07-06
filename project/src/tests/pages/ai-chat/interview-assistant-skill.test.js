import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const source = readFileSync(
  path.join(currentDir, "../../../../../backend/src/main/resources/skills/ai-interview-assistant/SKILL.md"),
  "utf8",
);

describe("interview assistant skill copy", () => {
  it("uses AIview测评 as the orchestration concept without legacy assessment modes", () => {
    expect(source).toContain("AIview测评");
    expect(source).not.toContain("专项测评");
    expect(source).not.toContain("综合测评");
    expect(source).not.toContain("view_comprehensive_history");
  });
});

import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const source = readFileSync(
  path.join(currentDir, "..", "..", "..", "pages", "admin", "index.vue"),
  "utf8",
);

describe("admin center layout", () => {
  it("removes the question bank management section", () => {
    expect(source).not.toContain("题库管理");
    expect(source).not.toContain("试题评测管理");
    expect(source).not.toContain("场景评测管理");
    expect(source).not.toContain("goToQuestionManagement");
    expect(source).not.toContain("goToScenarioManagement");
    expect(source).not.toContain("/pages/admin/question-management/index");
    expect(source).not.toContain("/pages/admin/scenario-management/index");
  });
});

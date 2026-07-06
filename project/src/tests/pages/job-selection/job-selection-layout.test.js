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

describe("job selection page layout", () => {
  it("uses a split showcase and selection panel layout", () => {
    expect(source).toContain("job-selection-layout");
    expect(source).toContain("job-selection-showcase");
    expect(source).toContain("job-selection-panel");
    expect(source).toContain("job-selection-panel-body");
    expect(source).not.toContain("cosmic-selection-card");
    expect(source).not.toContain('v-if="isLoading" class="loading-inline"');
    expect(source).toContain(':class="{ visible: isLoading }"');
    expect(source).toContain("岗位选择");
    expect(source).toContain("handleStartInterview");
  });

  it("highlights the ai interview while preserving the training loop narrative", () => {
    expect(source).toContain("AIview 面试主入口");
    expect(source).toContain("简历投递、试题作答与三轮面试");
    expect(source).not.toContain(
      "从岗位分类筛选到进入专属面试流程，先锁定目标岗位，再开启后续 AI 面试、专项题、简历评测与综合报告沉淀。",
    );
  });
});

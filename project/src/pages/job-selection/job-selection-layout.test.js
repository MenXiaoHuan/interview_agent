import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const source = readFileSync(path.join(currentDir, "index.vue"), "utf8");

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
});

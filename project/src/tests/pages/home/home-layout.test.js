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

describe("home page layout", () => {
  it("removes the career website and standalone history shortcuts", () => {
    expect(source).not.toContain("action-career");
    expect(source).not.toContain("推荐求职网站");
    expect(source).not.toContain("Boss直聘");
    expect(source).not.toContain("openInterviewJobs");
    expect(source).not.toContain("action-history");
    expect(source).not.toContain("goToHistory");
    expect(source).not.toContain("AIview 对话");
  });
});

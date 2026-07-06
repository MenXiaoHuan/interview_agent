import { describe, expect, it } from "vitest";
import { existsSync, readFileSync } from "node:fs";
import path from "node:path";

const projectRoot = path.resolve(__dirname, "../../..");
const srcDir = path.join(projectRoot, "src");

describe("legacy icon fallback removal", () => {
  it("removes the Font Awesome fallback utility and SmartIcon wrapper", () => {
    expect(existsSync(path.join(srcDir, "utils", "icons.js"))).toBe(false);
    expect(existsSync(path.join(srcDir, "components", "SmartIcon.vue"))).toBe(false);
  });

  it("does not import the removed icon fallback utility", () => {
    const filesToCheck = [
      "main.js",
      "pages/ai-resume/index.vue",
      "pages/ai-questions/index.vue",
      "pages/ai-scenario/index.vue",
      "pages/home/index.vue",
    ];

    filesToCheck.forEach((relativePath) => {
      const source = readFileSync(path.join(srcDir, relativePath), "utf8");
      expect(source).not.toContain("@/utils/icons");
      expect(source).not.toContain("@/components/SmartIcon.vue");
      expect(source).not.toContain("<SmartIcon");
    });
  });
});

import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import path from "node:path";

const projectRoot = path.resolve(__dirname, "../../..");
const deletedAssetNames = [
  "homepage_male.jpg",
  "interview_ai.jpg",
  "job_selection_male.jpg",
  "homepage_female.jpg",
];

const filesToCheck = [
  path.join(projectRoot, "src/utils/static-showcase-images.js"),
];

describe("deleted static asset references", () => {
  it("loads showcase images from the folder instead of hardcoded file names", () => {
    for (const filePath of filesToCheck) {
      const source = readFileSync(filePath, "utf8");
      expect(source).toContain("import.meta.glob");
      expect(source).toContain("/src/static/funtion_show/*");
      for (const assetName of deletedAssetNames) {
        expect(source).not.toContain(assetName);
      }
    }
  });
});

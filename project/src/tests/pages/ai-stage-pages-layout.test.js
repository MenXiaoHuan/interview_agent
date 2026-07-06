import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const srcDir = path.join(currentDir, "..", "..");

const readPage = (pageName) => readFileSync(
  path.join(srcDir, "pages", pageName, "index.vue"),
  "utf8",
);

const pages = [
  {
    name: "ai-resume",
    legacyBackground: "cyber-background",
    glassTarget: "content-card",
  },
  {
    name: "ai-questions",
    legacyBackground: "animated-background",
    glassTarget: "content-wrapper",
  },
  {
    name: "ai-scenario",
    legacyBackground: "sci-fi-background",
    glassTarget: "scene-assessment-card",
  },
];

describe("AI stage pages starry glass layout", () => {
  it.each(pages)("uses the interview hall starfield on $name", ({ name, legacyBackground }) => {
    const source = readPage(name);

    expect(source).toContain('<div id="stars"></div>');
    expect(source).toContain('<div id="stars2"></div>');
    expect(source).toContain('<div id="stars3"></div>');
    expect(source).toContain('lang="scss" scoped');
    expect(source).toContain('@use "sass:math";');
    expect(source).toContain('@use "sass:string";');
    expect(source).toContain("@function multiple-box-shadow($n)");
    expect(source).toContain("$shadows-small: multiple-box-shadow(1000)");
    expect(source).toContain("$shadows-medium: multiple-box-shadow(300)");
    expect(source).toContain("$shadows-large: multiple-box-shadow(150)");
    expect(source).toContain("#stars, #stars2, #stars3");
    expect(source).toContain("transform: translateY(-2560px)");
    expect(source).toContain(`.${legacyBackground} {`);
    expect(source).toContain("display: none;");
  });

  it.each(pages)("uses frosted glass surfaces on $name", ({ name, glassTarget }) => {
    const source = readPage(name);

    expect(source).toContain(`.${glassTarget}`);
    expect(source).toContain("background: rgba(255, 255, 255, 0.10)");
    expect(source).toContain("border: 1px solid rgba(186, 230, 253, 0.24)");
    expect(source).toContain("backdrop-filter: blur(18px) saturate(145%)");
    expect(source).toContain("-webkit-backdrop-filter: blur(18px) saturate(145%)");
  });
});

import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));
const source = readFileSync(
  path.join(currentDir, "..", "..", "..", "pages", "leaderboard", "index.vue"),
  "utf8",
);
const apiSource = readFileSync(
  path.join(currentDir, "..", "..", "..", "utils", "api", "pages", "leaderboard", "index.js"),
  "utf8",
);

describe("leaderboard layout", () => {
  it("uses AIview stage tabs instead of assessment mode tabs", () => {
    ["简历榜", "试题榜", "一面榜", "二面榜", "三面榜"].forEach((label) => {
      expect(source).toContain(label);
    });

    expect(source).not.toContain("综合评测");
    expect(source).not.toContain("专项评测");
    expect(source).not.toContain("场景榜");
    expect(source).not.toContain("rank-mode-tabs");
    expect(source).not.toContain("switchMode");
  });

  it("loads rankings from AIview stage endpoints only", () => {
    expect(apiSource).toContain("/api/rank/aiview/resume");
    expect(apiSource).toContain("/api/rank/aiview/question");
    expect(apiSource).toContain("/api/rank/aiview/round/round_1");
    expect(apiSource).toContain("/api/rank/aiview/round/round_2");
    expect(apiSource).toContain("/api/rank/aiview/round/round_3");
    expect(apiSource).not.toContain("/api/rank/comprehensive");
    expect(apiSource).not.toContain("/api/rank/specialized");
  });
});

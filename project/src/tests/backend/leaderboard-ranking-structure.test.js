import { describe, expect, it } from "vitest";
import { existsSync, readFileSync } from "node:fs";
import path from "node:path";

const repoRoot = path.resolve(__dirname, "../../../..");
const backendRoot = path.join(repoRoot, "backend");

const readBackendSource = (relativePath) => readFileSync(path.join(backendRoot, relativePath), "utf8");

describe("AIview leaderboard ranking", () => {
  it("exposes only AIview stage leaderboard endpoints", () => {
    const controller = readBackendSource("src/main/java/com/multimodal/interview/controller/RankingController.java");

    expect(controller).toContain('@RequestMapping("/api/rank/aiview")');
    expect(controller).toContain('@GetMapping("/resume")');
    expect(controller).toContain('@GetMapping("/question")');
    expect(controller).toContain('@GetMapping("/round/{roundType}")');
    expect(controller).not.toContain("comprehensive");
    expect(controller).not.toContain("specialized");
  });

  it("deduplicates by user with max score and filters interview rounds by roundType", () => {
    const mapper = readBackendSource("src/main/java/com/multimodal/interview/mapper/RankingMapper.java");

    expect(mapper).toContain("FROM ai_resume_history");
    expect(mapper).toContain("FROM ai_question_history");
    expect(mapper).toContain("FROM ai_interview_round_history");
    expect(mapper).toContain("MAX(overall_score) AS max_score");
    expect(mapper.match(/GROUP BY user_id/g)?.length).toBe(3);
    expect(mapper).toContain("round_type = #{roundType}");
  });

  it("removes legacy specialized leaderboard backend modules", () => {
    [
      "src/main/java/com/multimodal/interview/controller/RankingSpecializedController.java",
      "src/main/java/com/multimodal/interview/service/RankingSpecializedService.java",
      "src/main/java/com/multimodal/interview/service/impl/RankingSpecializedServiceImpl.java",
      "src/main/java/com/multimodal/interview/mapper/RankingSpecializedMapper.java",
    ].forEach((relativePath) => {
      expect(existsSync(path.join(backendRoot, relativePath))).toBe(false);
    });
  });
});

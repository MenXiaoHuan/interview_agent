import { describe, expect, it } from "vitest";
import { existsSync, readFileSync } from "node:fs";
import path from "node:path";

const repoRoot = path.resolve(__dirname, "../../../..");
const backendRoot = path.join(repoRoot, "backend");

const readBackendSource = (relativePath) => readFileSync(path.join(backendRoot, relativePath), "utf8");

describe("AIview job insights backend", () => {
  it("exposes a last-7-days insight endpoint by job id", () => {
    const controllerPath = "src/main/java/com/multimodal/interview/controller/AiviewInsightsController.java";

    expect(existsSync(path.join(backendRoot, controllerPath))).toBe(true);

    const controller = readBackendSource(controllerPath);
    expect(controller).toContain('@RequestMapping("/api/aiview-insights")');
    expect(controller).toContain('@GetMapping("/job/{jobId}/last-7-days")');
    expect(controller).toContain("getLast7DaysInsights");
  });

  it("aggregates all five AIview stages by job, recent time window, raw average score, max score, and raw attempt count", () => {
    const mapper = readBackendSource("src/main/java/com/multimodal/interview/mapper/AiviewInsightsMapper.java");
    const aggregate = readBackendSource("src/main/java/com/multimodal/interview/dto/AiviewInsightAggregate.java");
    const stageResponse = readBackendSource("src/main/java/com/multimodal/interview/dto/AiviewInsightStageResponse.java");

    expect(mapper).toContain("FROM ai_resume_history");
    expect(mapper).toContain("FROM ai_question_history");
    expect(mapper).toContain("FROM ai_interview_round_history");
    expect(mapper).toContain("job_id = #{jobId}");
    expect(mapper).toContain("DATE_SUB(NOW(), INTERVAL 7 DAY)");
    expect(mapper).toContain("COUNT(*) AS attemptCount");
    expect(mapper).toContain("AVG(overall_score) AS averageScore");
    expect(mapper).toContain("MAX(overall_score) AS max_score");
    expect(mapper).not.toContain("AVG(t.max_score) AS averageScore");
    expect(mapper).toContain("COUNT(DISTINCT user_id) AS participantCount");
    expect(mapper).not.toContain("GROUP BY user_id");
    expect(mapper).toContain("round_type = #{roundType}");
    expect(aggregate).toContain("private Integer attemptCount;");
    expect(stageResponse).toContain("private Integer attemptCount;");
  });

  it("uses rule-based advice instead of adding another AI agent for insights", () => {
    const service = readBackendSource("src/main/java/com/multimodal/interview/service/impl/AiviewInsightsServiceImpl.java");
    const agentConfig = readBackendSource("src/main/java/com/multimodal/interview/reactagent/ReactAgentConfig.java");

    expect(service).toContain("buildAdvice");
    expect(service).toContain("insufficient-data");
    expect(service).toContain("at-risk");
    expect(service).toContain("DEFAULT_PASS_SCORE = 80");
    expect(service).toContain("averageScore < DEFAULT_PASS_SCORE");
    expect(service).toContain("DEFAULT_PASS_SCORE + 10");
    expect(service).not.toContain("平均分低于 70");
    expect(agentConfig).not.toContain("aiview-insights");
  });
});

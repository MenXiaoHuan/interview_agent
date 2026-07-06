import { describe, expect, it } from "vitest";
import { existsSync, readFileSync } from "node:fs";
import path from "node:path";

const repoRoot = path.resolve(__dirname, "../../../..");
const backendRoot = path.join(repoRoot, "backend");

const readBackendSource = (relativePath) => readFileSync(path.join(backendRoot, relativePath), "utf8");

describe("AIview assessment persistence naming", () => {
  it("uses ai-prefixed database tables and removes the standalone report table", () => {
    const sql = readBackendSource("src/main/resources/sql/interview_agent.sql");
    [
      "CREATE TABLE `ai_assessment_session`",
      "CREATE TABLE `ai_resume_history`",
      "CREATE TABLE `ai_question_history`",
      "CREATE TABLE `ai_interview_round_history`",
    ].forEach((snippet) => {
      expect(sql).toContain(snippet);
    });

    [
      "comprehensive_assessment_session",
      "comprehensive_resume_history",
      "comprehensive_question_history",
      "comprehensive_scenario_history",
      "comprehensive_report_history",
    ].forEach((legacyName) => {
      expect(sql).not.toContain(legacyName);
    });
  });

  it("renames backend persistence classes away from comprehensive naming", () => {
    [
      "src/main/java/com/multimodal/interview/controller/AiAssessmentController.java",
      "src/main/java/com/multimodal/interview/service/AiAssessmentService.java",
      "src/main/java/com/multimodal/interview/service/impl/AiAssessmentServiceImpl.java",
      "src/main/java/com/multimodal/interview/entity/AiAssessmentSession.java",
      "src/main/java/com/multimodal/interview/entity/AiResumeHistory.java",
      "src/main/java/com/multimodal/interview/entity/AiQuestionHistory.java",
      "src/main/java/com/multimodal/interview/entity/AiInterviewRoundHistory.java",
      "src/main/java/com/multimodal/interview/mapper/AiAssessmentSessionMapper.java",
      "src/main/java/com/multimodal/interview/mapper/AiResumeHistoryMapper.java",
      "src/main/java/com/multimodal/interview/mapper/AiQuestionHistoryMapper.java",
      "src/main/java/com/multimodal/interview/mapper/AiInterviewRoundHistoryMapper.java",
    ].forEach((relativePath) => {
      expect(existsSync(path.join(backendRoot, relativePath))).toBe(true);
    });

    [
      "src/main/java/com/multimodal/interview/entity/ComprehensiveReportHistory.java",
      "src/main/java/com/multimodal/interview/mapper/ComprehensiveReportHistoryMapper.java",
      "src/main/java/com/multimodal/interview/dto/ComprehensiveReportRequest.java",
      "src/main/java/com/multimodal/interview/dto/ComprehensiveReportResponse.java",
      "src/main/java/com/multimodal/interview/dto/ComprehensiveReportSummaryResponse.java",
    ].forEach((relativePath) => {
      expect(existsSync(path.join(backendRoot, relativePath))).toBe(false);
    });
  });

  it("exposes AIview assessment endpoints instead of comprehensive-history endpoints", () => {
    const controller = readBackendSource("src/main/java/com/multimodal/interview/controller/AiAssessmentController.java");
    expect(controller).toContain('@RequestMapping("/api/ai-assessment")');
    expect(controller).toContain('@PostMapping("/resume")');
    expect(controller).toContain('@PostMapping("/question")');
    expect(controller).toContain('@PostMapping("/interview-round")');
    expect(controller).not.toContain("/report");
    expect(controller).not.toContain("comprehensive-history");
  });

  it("stores resume assessment with only total score and suggestions", () => {
    const sql = readBackendSource("src/main/resources/sql/interview_agent.sql");
    const mapper = readBackendSource("src/main/java/com/multimodal/interview/mapper/AiResumeHistoryMapper.java");
    const entity = readBackendSource("src/main/java/com/multimodal/interview/entity/AiResumeHistory.java");
    const dto = readBackendSource("src/main/java/com/multimodal/interview/dto/AiResumeHistoryRequest.java");

    expect(sql).toContain("`overall_score` tinyint unsigned DEFAULT NULL COMMENT '总体评分(0-100)'");
    expect(sql).toContain("`ai_suggestions` json DEFAULT NULL COMMENT 'AI改进建议'");
    ["skill_match", "experience_match", "education_match", "communication_skill", "leadership_skill"].forEach((column) => {
      expect(sql).not.toContain(`\`${column}\``);
      expect(mapper).not.toContain(column);
    });
    ["skillMatch", "experienceMatch", "educationMatch", "communicationSkill", "leadershipSkill"].forEach((field) => {
      expect(entity).not.toContain(field);
      expect(dto).not.toContain(field);
    });
  });

  it("stores interview round assessment with only total score and AI assessment JSON", () => {
    const sql = readBackendSource("src/main/resources/sql/interview_agent.sql");
    const mapper = readBackendSource("src/main/java/com/multimodal/interview/mapper/AiInterviewRoundHistoryMapper.java");
    const entity = readBackendSource("src/main/java/com/multimodal/interview/entity/AiInterviewRoundHistory.java");
    const dto = readBackendSource("src/main/java/com/multimodal/interview/dto/AiInterviewRoundHistoryRequest.java");

    expect(sql).toContain("`overall_score` tinyint unsigned DEFAULT NULL COMMENT '总体评分(0-100)'");
    expect(sql).toContain("`ai_assessment` json DEFAULT NULL COMMENT 'AI评估详情'");
    ["fluency_score", "emotion_score", "relevance_score", "adaptability_score"].forEach((column) => {
      expect(sql).not.toContain(`\`${column}\``);
      expect(mapper).not.toContain(column);
    });
    ["fluencyScore", "emotionScore", "relevanceScore", "adaptabilityScore"].forEach((field) => {
      expect(entity).not.toContain(field);
      expect(dto).not.toContain(field);
    });
  });
});

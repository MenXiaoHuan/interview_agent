import { describe, expect, it } from "vitest";
import { existsSync, readFileSync } from "node:fs";
import path from "node:path";

const repoRoot = path.resolve(__dirname, "../../../..");
const backendRoot = path.join(repoRoot, "backend");

describe("backend question bank module removal", () => {
  it("removes obsolete question bank controllers, services, mappers, and entities", () => {
    [
      "src/main/java/com/multimodal/interview/controller/ChoiceQuestionController.java",
      "src/main/java/com/multimodal/interview/controller/ScenarioQuestionController.java",
      "src/main/java/com/multimodal/interview/service/ChoiceQuestionService.java",
      "src/main/java/com/multimodal/interview/service/ScenarioQuestionService.java",
      "src/main/java/com/multimodal/interview/service/impl/ChoiceQuestionServiceImpl.java",
      "src/main/java/com/multimodal/interview/service/impl/ScenarioQuestionServiceImpl.java",
      "src/main/java/com/multimodal/interview/mapper/ChoiceQuestionMapper.java",
      "src/main/java/com/multimodal/interview/mapper/ScenarioQuestionMapper.java",
      "src/main/java/com/multimodal/interview/mapper/ChoiceQuestionRecordMapper.java",
      "src/main/java/com/multimodal/interview/mapper/ChoiceAnswerMapper.java",
      "src/main/java/com/multimodal/interview/entity/choiceQuestion.java",
      "src/main/java/com/multimodal/interview/entity/ScenarioQuestion.java",
      "src/main/java/com/multimodal/interview/entity/choiceQuestionRecord.java",
      "src/main/java/com/multimodal/interview/entity/choiceAnswer.java",
    ].forEach((relativePath) => {
      expect(existsSync(path.join(backendRoot, relativePath))).toBe(false);
    });
  });

  it("removes obsolete special assessment history controllers, services, mappers, and entities", () => {
    [
      "src/main/java/com/multimodal/interview/controller/ScenarioAnalysisController.java",
      "src/main/java/com/multimodal/interview/service/ScenarioService.java",
      "src/main/java/com/multimodal/interview/service/impl/ScenarioServiceImpl.java",
      "src/main/java/com/multimodal/interview/mapper/ScenarioAnalysisMapper.java",
      "src/main/java/com/multimodal/interview/mapper/ResumeAnalysisHistoryMapper.java",
      "src/main/java/com/multimodal/interview/mapper/ResumeHistoricalCopyMapper.java",
      "src/main/java/com/multimodal/interview/entity/ScenarioAnalysis.java",
      "src/main/java/com/multimodal/interview/entity/ResumeAnalysisHistory.java",
      "src/main/java/com/multimodal/interview/entity/ResumeHistoricalCopy.java",
      "src/main/java/com/multimodal/interview/dto/SaveResumeHistoryRequest.java",
      "src/main/java/com/multimodal/interview/dto/SaveScenarioAnalysisHistoryRequest.java",
    ].forEach((relativePath) => {
      expect(existsSync(path.join(backendRoot, relativePath))).toBe(false);
    });
  });

  it("removes question bank and legacy choice record tables from the SQL bootstrap script", () => {
    const sql = readFileSync(path.join(backendRoot, "src/main/resources/sql/interview_agent.sql"), "utf8");
    expect(sql).not.toContain("CREATE TABLE `choice_question`");
    expect(sql).not.toContain("CREATE TABLE `scenario_question`");
    expect(sql).not.toContain("CREATE TABLE `choice_question_record`");
    expect(sql).not.toContain("choice_question_record");
    expect(sql).not.toContain("choice_answer");
    expect(sql).not.toContain("CREATE TABLE `resume_analysis_history`");
    expect(sql).not.toContain("CREATE TABLE `resume_historical_copy`");
    expect(sql).not.toContain("CREATE TABLE `scenario_analysis`");
    expect(sql).not.toContain("resume_analysis_history");
    expect(sql).not.toContain("resume_historical_copy");
    expect(sql).not.toContain("scenario_analysis");
  });

  it("does not inject the obsolete choice question controller into ReactAgent tools", () => {
    const source = readFileSync(
      path.join(backendRoot, "src/main/java/com/multimodal/interview/reactagent/ReactAgentConfig.java"),
      "utf8",
    );
    expect(source).not.toContain("ChoiceQuestionController");
    expect(source).not.toContain("choiceQuestionController");
  });
});

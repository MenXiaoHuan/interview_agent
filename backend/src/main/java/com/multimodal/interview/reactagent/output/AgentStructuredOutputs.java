package com.multimodal.interview.reactagent.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * AI Agent 结构化输出定义。
 *
 * <p>职责：</p>
 * <p>
 * - 为每个 Agent 定义稳定的 Java 输出模型
 * </p>
 * <p>
 * - 通过 {@code BeanOutputConverter} 自动生成 JSON Schema，交给 ReactAgent 的 {@code outputSchema(...)}
 * </p>
 * <p>
 * - 让后端在路由层可以按 agentKey 将模型输出反序列化为对象，而不是让前端到处手写 JSON.parse
 * </p>
 */
public final class AgentStructuredOutputs {

    private AgentStructuredOutputs() {
    }

    @Data
    public static class ResumeAnalysisResult {
        private Integer score;
        private ResumeFeedback feedback;
        @JsonProperty("competencies_score")
        private ResumeCompetenciesScore competenciesScore;
    }

    @Data
    public static class ResumeFeedback {
        private String advantages;
        private String disadvantages;
        @JsonProperty("improvement_suggestions")
        private String improvementSuggestions;
    }

    @Data
    public static class ResumeCompetenciesScore {
        @JsonProperty("professional_skills")
        private Integer professionalSkills;
        @JsonProperty("project_execution")
        private Integer projectExecution;
        private Integer innovation;
        private Integer communication;
        private Integer adaptability;
    }

    @Data
    public static class ScenarioEvaluationResult {
        private Integer totalScore;
        private List<AbilityScore> abilities;
        private List<String> questionSuggestions;
        private String pronunciationAdvice;
        private String expressionAdvice;
    }

    @Data
    public static class AbilityScore {
        private String name;
        private Integer score;
        private String desc;
    }

    @Data
    public static class ComprehensiveReportResult {
        private String feedbackTitle;
        private String feedbackText;
        private StrengthAnalysis strengthAnalysis;
        private ImprovementAnalysis improvementAnalysis;
        private String learningPathRecommendation;
    }

    @Data
    public static class StrengthAnalysis {
        private List<String> strengths;
    }

    @Data
    public static class ImprovementAnalysis {
        private List<String> improvements;
    }

    @Data
    public static class ScenarioQuestionGenerationResult {
        private String question;
    }

    @Data
    public static class ScenarioAudioEvaluationResult {
        private Integer totalScore;
        private AudioDimensions dimensions;
        private PronunciationResult pronunciation;
        private FacialExpressionResult facialExpression;
        private SpeechRateResult speechRate;
        private String overallComment;
        private List<AudioQuestionResult> questions;
        private List<String> keyImprovements;
    }

    @Data
    public static class AudioDimensions {
        private Integer fluency;
        private Integer relevance;
        private Integer adaptability;
        private Integer professionalism;
        private Integer completeness;
    }

    @Data
    public static class PronunciationResult {
        private Integer score;
        private String comment;
    }

    @Data
    public static class FacialExpressionResult {
        private Integer score;
        private List<String> suggestions;
    }

    @Data
    public static class SpeechRateResult {
        private String currentRate;
        private List<String> suggestions;
    }

    @Data
    public static class AudioQuestionResult {
        private String question;
        private String userAnswer;
        private Integer score;
        private String comment;
        private List<String> strengths;
        private List<String> weaknesses;
        private String improvement;
    }

    @Data
    public static class ComprehensiveResumeAnalysisResult {
        private Integer score;
        private Integer skillMatch;
        private Integer experienceMatch;
        private Integer educationMatch;
        private Integer communicationSkill;
        private Integer leadershipSkill;
        private List<String> suggestions;
    }

    @Data
    public static class ComprehensiveQuestionGenerationResult {
        @JsonProperty("选择题")
        private List<ChoiceQuestionItem> choiceQuestions;
        @JsonProperty("填空题")
        private List<FillBlankQuestionItem> fillBlankQuestions;
        @JsonProperty("场景题")
        private List<ScenarioQuestionItem> scenarioQuestions;
    }

    @Data
    public static class ChoiceQuestionItem {
        private String question;
        private List<String> options;
        private String answer;
        private Integer score;
    }

    @Data
    public static class FillBlankQuestionItem {
        private String question;
        private String answer;
        private Integer score;
    }

    @Data
    public static class ScenarioQuestionItem {
        private String question;
        private String answer;
        private Integer score;
    }

    @Data
    public static class ScenarioQuestionScoringResult {
        private List<ScenarioQuestionScoreItem> examData;
    }

    @Data
    public static class ScenarioQuestionScoreItem {
        private Integer score;
        private String analysis;
    }

    @Data
    public static class QuestionAnalysisResult {
        private String analysis;
    }

    @Data
    public static class InterviewAssistantResult {
        private String reply;
        private List<AssistantAction> actions;
        private String intent;
        private String targetModule;
        private String targetMode;
        private String targetJobHint;
        private Boolean targetJobConfirmed;
        private Boolean completionAcknowledged;
    }

    @Data
    public static class AssistantAction {
        private String type;
        private String label;
    }
}

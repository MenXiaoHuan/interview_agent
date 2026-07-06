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
    public static class ScenarioQuestionGenerationResult {
        private String question = "请结合一次真实项目经历，说明你如何分析问题、制定方案并推动落地？";
    }

    @Data
    public static class ScenarioAudioEvaluationResult {
        private Integer totalScore = 0;
        private AudioDimensions dimensions = new AudioDimensions();
        private PronunciationResult pronunciation = new PronunciationResult();
        private FacialExpressionResult facialExpression = new FacialExpressionResult();
        private SpeechRateResult speechRate = new SpeechRateResult();
        private String overallComment = "AI 评测结果暂时不完整，请以当前作答记录为准重新评测或继续下一步。";
        private List<AudioQuestionResult> questions = List.of();
        private List<String> keyImprovements = List.of("补充更完整的作答内容", "按题目要求给出结构化回答");
    }

    @Data
    public static class AudioDimensions {
        private Integer fluency = 0;
        private Integer relevance = 0;
        private Integer adaptability = 0;
        private Integer professionalism = 0;
        private Integer completeness = 0;
    }

    @Data
    public static class PronunciationResult {
        private Integer score = 0;
        private String comment = "暂无稳定的表达层评测结果。";
    }

    @Data
    public static class FacialExpressionResult {
        private Integer score = 0;
        private List<String> suggestions = List.of("保持自然稳定的面试状态");
    }

    @Data
    public static class SpeechRateResult {
        private String currentRate = "unknown";
        private List<String> suggestions = List.of("控制语速，先结论后展开");
    }

    @Data
    public static class AudioQuestionResult {
        private String question = "";
        private String userAnswer = "";
        private Integer score = 0;
        private String comment = "本题评测结果暂时不完整，建议重新作答或补充关键细节。";
        private List<String> strengths = List.of();
        private List<String> weaknesses = List.of("未获得稳定的结构化评测结果");
        private String improvement = "补充更具体的背景、动作和结果。";
    }

    @Data
    public static class AiResumeAnalysisResult {
        private Integer score = 0;
        private List<String> suggestions = List.of("补充与岗位要求直接相关的项目证据和量化结果。");
    }

    @Data
    public static class AiQuestionGenerationResult {
        @JsonProperty("选择题")
        private List<ChoiceQuestionItem> choiceQuestions = List.of();
        @JsonProperty("填空题")
        private List<FillBlankQuestionItem> fillBlankQuestions = List.of();
        @JsonProperty("场景题")
        private List<ScenarioQuestionItem> scenarioQuestions = List.of();
    }

    @Data
    public static class ChoiceQuestionItem {
        private String question = "";
        private List<String> options = List.of();
        private String answer = "";
        private Integer score = 0;
    }

    @Data
    public static class FillBlankQuestionItem {
        private String question = "";
        private String answer = "";
        private Integer score = 0;
    }

    @Data
    public static class ScenarioQuestionItem {
        private String question = "";
        private String answer = "";
        private Integer score = 0;
    }

    @Data
    public static class ScenarioQuestionScoringResult {
        private List<ScenarioQuestionScoreItem> examData = List.of();
    }

    @Data
    public static class ScenarioQuestionScoreItem {
        private Integer score = 0;
        private String analysis = "本题评分暂时不完整，请重新提交或补充作答内容。";
    }

    @Data
    public static class QuestionAnalysisResult {
        private String analysis = "本题解析暂时不可用，请结合题干、正确答案和关键知识点进行复盘。";
    }

    @Data
    public static class InterviewAssistantResult {
        private String reply = "我来帮你安排下一步。";
        private List<AssistantAction> actions = List.of();
        private String intent = "general_consultation";
        private String targetModule = "NONE";
        private String targetMode = "UNKNOWN";
        private String targetJobHint = "";
        private Boolean targetJobConfirmed = false;
        private Boolean completionAcknowledged = false;
    }

    @Data
    public static class AssistantAction {
        private String type = "";
        private String label = "";
    }
}

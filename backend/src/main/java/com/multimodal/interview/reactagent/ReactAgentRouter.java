package com.multimodal.interview.reactagent;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.reactagent.output.AgentStructuredOutputs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ReactAgent 路由器。
 *
 * <p>职责：</p>
 * <p>
 * - 根据 controller 请求中的 agentKey 选择对应的 ReactAgent Bean
 * </p>
 * <p>
 * - 将前端传入的 params 转成更适合模型理解的 UserMessage 文本（避免模型“只看到一坨 JSON”）
 * </p>
 * <p>
 * - 将底层 GraphRunnerException 转成统一的业务异常，交给全局异常处理器返回给前端
 * </p>
 */
@Component
@Slf4j
public class ReactAgentRouter {

    private static final Gson GSON = new Gson();
    private static final Pattern JSON_CODE_BLOCK_PATTERN = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JSON_OBJECT_PATTERN = Pattern.compile("\\{[\\s\\S]*\\}");
    private static final List<String> AI_UNAVAILABLE_KEYWORDS = List.of(
            "insufficient",
            "balance",
            "quota",
            "billing",
            "payment required",
            "余额不足",
            "欠费",
            "充值",
            "用量超限",
            "rate limit",
            "api key",
            "model is overloaded",
            "service unavailable",
            "temporarily unavailable"
    );
    private static final Map<String, Class<?>> STRUCTURED_OUTPUT_TYPES = Map.of(
            "ai-interview-assistant", AgentStructuredOutputs.InterviewAssistantResult.class,
            "ai-round-question-generation", AgentStructuredOutputs.ScenarioQuestionGenerationResult.class,
            "ai-round-evaluation", AgentStructuredOutputs.ScenarioAudioEvaluationResult.class,
            "ai-resume-analysis", AgentStructuredOutputs.AiResumeAnalysisResult.class,
            "ai-question-generation", AgentStructuredOutputs.AiQuestionGenerationResult.class,
            "ai-question-scoring", AgentStructuredOutputs.ScenarioQuestionScoringResult.class,
            "ai-question-analysis", AgentStructuredOutputs.QuestionAnalysisResult.class
    );

    private final Map<String, ReactAgent> agentByKey;
    private final ObjectMapper objectMapper;
    private final InterviewAssistantMemoryService interviewAssistantMemoryService;

    public ReactAgentRouter(
            Map<String, ReactAgent> agentByKey,
            ObjectMapper objectMapper,
            InterviewAssistantMemoryService interviewAssistantMemoryService
    ) {
        this.agentByKey = Map.copyOf(agentByKey);
        this.objectMapper = objectMapper;
        this.interviewAssistantMemoryService = interviewAssistantMemoryService;
    }

    /**
     * 执行指定 agentKey 对应的 ReactAgent。
     *
     * @param agentKey agent 唯一标识（与 Spring Bean 名称一致）
     * @param params 业务参数（来自前端）
     * @return 模型最终输出。结构化 agent 返回对象；对话类 agent 返回字符串。
     */
    public Object run(String agentKey, Map<String, Object> params, String chatId) {
        if (agentKey == null || agentKey.isBlank()) {
            throw BusinessException.badRequest("agentKey不能为空");
        }
        ReactAgent agent = agentByKey.get(agentKey);
        if (agent == null) {
            throw BusinessException.badRequest("未知agentKey: " + agentKey);
        }
        try {
            Map<String, Object> safeParams = params == null ? Map.of() : params;
            if ("ai-interview-assistant".equals(agentKey)) {
                safeParams = interviewAssistantMemoryService.enrichParams(chatId, safeParams);
            }
            String userContent = buildUserContent(agentKey, safeParams);
            AssistantMessage message = agent.call(new UserMessage(userContent));
            String text = message == null ? "" : String.valueOf(message.getText());
            Object output = parseAgentOutput(agentKey, text, safeParams);
            if ("ai-interview-assistant".equals(agentKey) && output instanceof AgentStructuredOutputs.InterviewAssistantResult result) {
                interviewAssistantMemoryService.rememberAssistantReply(chatId, safeParams, result.getReply());
            }
            return output;
        } catch (BusinessException exception) {
            throw exception;
        } catch (GraphRunnerException exception) {
            log.error("Agent运行时调用失败, agentKey={}, message={}", agentKey, exception.getMessage(), exception);
            throw BusinessException.serviceUnavailable("AI 服务当前不可用");
        } catch (RuntimeException exception) {
            if (looksLikeAiServiceFailure(exception.getMessage())) {
                log.error("Agent疑似模型服务异常, agentKey={}, message={}", agentKey, exception.getMessage(), exception);
                throw BusinessException.serviceUnavailable("AI 服务当前不可用");
            }
            throw exception;
        }
    }

    private Object parseAgentOutput(String agentKey, String rawText, Map<String, Object> params) {
        Class<?> outputType = STRUCTURED_OUTPUT_TYPES.get(agentKey);
        if (outputType == null) {
            return rawText == null ? "" : rawText;
        }
        String candidate = extractJsonCandidate(rawText);
        if (looksLikeAiServiceFailure(candidate) || looksLikeAiServiceFailure(rawText)) {
            log.error("Agent返回模型服务错误载荷, agentKey={}, rawText={}, candidate={}",
                    agentKey,
                    abbreviate(rawText),
                    abbreviate(candidate));
            throw BusinessException.serviceUnavailable("AI 服务当前不可用");
        }
        try {
            Object parsed = objectMapper.readValue(candidate, outputType);
            if ("ai-interview-assistant".equals(agentKey)
                    && parsed instanceof AgentStructuredOutputs.InterviewAssistantResult result) {
                return postProcessInterviewAssistant(result, params);
            }
            return postProcessStructuredOutput(agentKey, parsed, params);
        } catch (JsonProcessingException exception) {
            log.warn("Agent结构化输出解析失败，将尝试降级处理, agentKey={}, rawText={}, candidate={}, reason={}",
                    agentKey,
                    abbreviate(rawText),
                    abbreviate(candidate),
                    exception.getOriginalMessage());
            if (looksLikeAiServiceFailure(candidate) || looksLikeAiServiceFailure(rawText)) {
                throw BusinessException.serviceUnavailable("AI 服务当前不可用");
            }
            return buildFallbackStructuredOutput(agentKey, rawText, candidate, params);
        }
    }

    private Object postProcessStructuredOutput(String agentKey, Object parsed, Map<String, Object> params) {
        if (parsed instanceof AgentStructuredOutputs.AiResumeAnalysisResult result) {
            result.setScore(clampScore(result.getScore()));
            result.setSuggestions(normalizeStringList(
                    result.getSuggestions(),
                    List.of("补充与岗位要求直接相关的项目证据和量化结果。")
            ));
            return result;
        }
        if (parsed instanceof AgentStructuredOutputs.QuestionAnalysisResult result) {
            result.setAnalysis(firstNonBlank(
                    result.getAnalysis(),
                    "本题解析暂时不可用，请结合题干、正确答案和关键知识点进行复盘。"
            ));
            return result;
        }
        if (parsed instanceof AgentStructuredOutputs.ScenarioQuestionGenerationResult result) {
            result.setQuestion(firstNonBlank(
                    result.getQuestion(),
                    "请结合一次真实项目经历，说明你如何分析问题、制定方案并推动落地？"
            ));
            return result;
        }
        if (parsed instanceof AgentStructuredOutputs.ScenarioQuestionScoringResult result) {
            result.setExamData(normalizeQuestionScoringItems(result.getExamData(), params));
            return result;
        }
        if (parsed instanceof AgentStructuredOutputs.ScenarioAudioEvaluationResult result) {
            return normalizeRoundEvaluationResult(result);
        }
        if (parsed instanceof AgentStructuredOutputs.AiQuestionGenerationResult result) {
            result.setChoiceQuestions(result.getChoiceQuestions() == null ? List.of() : result.getChoiceQuestions());
            result.setFillBlankQuestions(result.getFillBlankQuestions() == null ? List.of() : result.getFillBlankQuestions());
            result.setScenarioQuestions(result.getScenarioQuestions() == null ? List.of() : result.getScenarioQuestions());
            return result;
        }
        return parsed;
    }

    private Object buildFallbackStructuredOutput(
            String agentKey,
            String rawText,
            String candidate,
            Map<String, Object> params
    ) {
        log.warn("Agent结构化输出已降级处理, agentKey={}, rawText={}, candidate={}",
                agentKey,
                abbreviate(rawText),
                abbreviate(candidate));
        JsonNode root = parseJsonNode(candidate);
        String readableText = firstNonBlank(textFromJson(root), rawText, candidate);
        return switch (agentKey) {
            case "ai-resume-analysis" -> fallbackResumeAnalysis(root, readableText);
            case "ai-question-analysis" -> fallbackQuestionAnalysis(root, readableText);
            case "ai-round-question-generation" -> fallbackRoundQuestionGeneration(root, readableText);
            case "ai-question-scoring" -> fallbackQuestionScoring(root, params, readableText);
            case "ai-round-evaluation" -> fallbackRoundEvaluation(root, params, readableText);
            case "ai-question-generation" -> new AgentStructuredOutputs.AiQuestionGenerationResult();
            case "ai-interview-assistant" -> {
                AgentStructuredOutputs.InterviewAssistantResult result = new AgentStructuredOutputs.InterviewAssistantResult();
                result.setReply(firstNonBlank(readableText, "我来帮你安排下一步。"));
                yield postProcessInterviewAssistant(result, params);
            }
            default -> readableText;
        };
    }

    private AgentStructuredOutputs.AiResumeAnalysisResult fallbackResumeAnalysis(JsonNode root, String readableText) {
        AgentStructuredOutputs.AiResumeAnalysisResult result = new AgentStructuredOutputs.AiResumeAnalysisResult();
        result.setScore(clampScore(intValue(root, "score", "overallScore", "overall_score")));
        List<String> suggestions = stringsFromJson(root, "suggestions", "improvementSuggestions", "aiSuggestions", "advice");
        if (suggestions.isEmpty() && !readableText.isBlank()) {
            suggestions = List.of(readableText);
        }
        result.setSuggestions(normalizeStringList(
                suggestions,
                List.of("补充与岗位要求直接相关的项目证据和量化结果。")
        ));
        return result;
    }

    private AgentStructuredOutputs.QuestionAnalysisResult fallbackQuestionAnalysis(JsonNode root, String readableText) {
        AgentStructuredOutputs.QuestionAnalysisResult result = new AgentStructuredOutputs.QuestionAnalysisResult();
        result.setAnalysis(firstNonBlank(
                textValue(root, "analysis"),
                textValue(root, "content"),
                textValue(root, "reply"),
                readableText,
                "本题解析暂时不可用，请结合题干、正确答案和关键知识点进行复盘。"
        ));
        return result;
    }

    private AgentStructuredOutputs.ScenarioQuestionGenerationResult fallbackRoundQuestionGeneration(JsonNode root, String readableText) {
        AgentStructuredOutputs.ScenarioQuestionGenerationResult result = new AgentStructuredOutputs.ScenarioQuestionGenerationResult();
        result.setQuestion(firstNonBlank(
                textValue(root, "question"),
                readableText,
                "请结合一次真实项目经历，说明你如何分析问题、制定方案并推动落地？"
        ));
        return result;
    }

    private AgentStructuredOutputs.ScenarioQuestionScoringResult fallbackQuestionScoring(
            JsonNode root,
            Map<String, Object> params,
            String readableText
    ) {
        AgentStructuredOutputs.ScenarioQuestionScoringResult result = new AgentStructuredOutputs.ScenarioQuestionScoringResult();
        result.setExamData(normalizeQuestionScoringItems(List.of(), params));
        if (result.getExamData().isEmpty()) {
            AgentStructuredOutputs.ScenarioQuestionScoreItem item = new AgentStructuredOutputs.ScenarioQuestionScoreItem();
            item.setAnalysis(firstNonBlank(readableText, textFromJson(root), item.getAnalysis()));
            result.setExamData(List.of(item));
        }
        return result;
    }

    private AgentStructuredOutputs.ScenarioAudioEvaluationResult fallbackRoundEvaluation(
            JsonNode root,
            Map<String, Object> params,
            String readableText
    ) {
        AgentStructuredOutputs.ScenarioAudioEvaluationResult result = new AgentStructuredOutputs.ScenarioAudioEvaluationResult();
        result.setTotalScore(clampScore(intValue(root, "totalScore", "overallScore", "score")));
        result.setOverallComment(firstNonBlank(
                textValue(root, "overallComment"),
                textValue(root, "comment"),
                readableText,
                result.getOverallComment()
        ));
        result.setQuestions(List.of());
        result.setKeyImprovements(normalizeStringList(
                stringsFromJson(root, "keyImprovements", "suggestions", "advice"),
                result.getKeyImprovements()
        ));
        return normalizeRoundEvaluationResult(result);
    }

    private AgentStructuredOutputs.ScenarioAudioEvaluationResult normalizeRoundEvaluationResult(
            AgentStructuredOutputs.ScenarioAudioEvaluationResult result
    ) {
        AgentStructuredOutputs.ScenarioAudioEvaluationResult safe =
                result == null ? new AgentStructuredOutputs.ScenarioAudioEvaluationResult() : result;
        safe.setTotalScore(clampScore(safe.getTotalScore()));
        safe.setDimensions(safe.getDimensions() == null ? new AgentStructuredOutputs.AudioDimensions() : safe.getDimensions());
        safe.setPronunciation(safe.getPronunciation() == null ? new AgentStructuredOutputs.PronunciationResult() : safe.getPronunciation());
        safe.setFacialExpression(safe.getFacialExpression() == null ? new AgentStructuredOutputs.FacialExpressionResult() : safe.getFacialExpression());
        safe.setSpeechRate(safe.getSpeechRate() == null ? new AgentStructuredOutputs.SpeechRateResult() : safe.getSpeechRate());
        safe.setOverallComment(firstNonBlank(
                safe.getOverallComment(),
                "AI 评测结果暂时不完整，请以当前作答记录为准重新评测或继续下一步。"
        ));
        safe.setQuestions(safe.getQuestions() == null ? List.of() : safe.getQuestions());
        safe.setKeyImprovements(normalizeStringList(
                safe.getKeyImprovements(),
                List.of("补充更完整的作答内容", "按题目要求给出结构化回答")
        ));
        return safe;
    }

    private List<AgentStructuredOutputs.ScenarioQuestionScoreItem> normalizeQuestionScoringItems(
            List<AgentStructuredOutputs.ScenarioQuestionScoreItem> items,
            Map<String, Object> params
    ) {
        if (items != null && !items.isEmpty()) {
            List<AgentStructuredOutputs.ScenarioQuestionScoreItem> normalized = new ArrayList<>();
            for (AgentStructuredOutputs.ScenarioQuestionScoreItem item : items) {
                AgentStructuredOutputs.ScenarioQuestionScoreItem safeItem =
                        item == null ? new AgentStructuredOutputs.ScenarioQuestionScoreItem() : item;
                safeItem.setScore(Math.max(0, Math.min(30, safeItem.getScore() == null ? 0 : safeItem.getScore())));
                safeItem.setAnalysis(firstNonBlank(
                        safeItem.getAnalysis(),
                        "本题评分暂时不完整，请重新提交或补充作答内容。"
                ));
                normalized.add(safeItem);
            }
            return normalized;
        }
        List<?> questions = params == null ? List.of() : params.get("questions") instanceof List<?> list ? list : List.of();
        if (questions.isEmpty()) {
            return List.of();
        }
        List<AgentStructuredOutputs.ScenarioQuestionScoreItem> fallback = new ArrayList<>();
        for (int index = 0; index < questions.size(); index++) {
            AgentStructuredOutputs.ScenarioQuestionScoreItem item = new AgentStructuredOutputs.ScenarioQuestionScoreItem();
            item.setScore(0);
            item.setAnalysis("第 " + (index + 1) + " 题评分暂时不完整，请重新提交或补充作答内容。");
            fallback.add(item);
        }
        return fallback;
    }

    private JsonNode parseJsonNode(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(text);
        } catch (Exception ignore) {
            return null;
        }
    }

    private int clampScore(Integer score) {
        if (score == null) {
            return 0;
        }
        return Math.max(0, Math.min(100, score));
    }

    private Integer intValue(JsonNode root, String... fieldNames) {
        if (root == null || fieldNames == null) {
            return 0;
        }
        for (String fieldName : fieldNames) {
            JsonNode value = root.path(fieldName);
            if (value.isMissingNode() || value.isNull()) {
                continue;
            }
            if (value.isNumber()) {
                return value.asInt(0);
            }
            if (value.isTextual()) {
                try {
                    return Integer.parseInt(value.asText("").trim());
                } catch (NumberFormatException ignored) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private List<String> stringsFromJson(JsonNode root, String... fieldNames) {
        if (root == null || fieldNames == null) {
            return List.of();
        }
        for (String fieldName : fieldNames) {
            JsonNode value = root.path(fieldName);
            if (value.isMissingNode() || value.isNull()) {
                continue;
            }
            if (value.isArray()) {
                List<String> items = new ArrayList<>();
                value.forEach(item -> {
                    String text = item.isValueNode() ? item.asText("") : item.toString();
                    if (text != null && !text.isBlank()) {
                        items.add(text.trim());
                    }
                });
                if (!items.isEmpty()) {
                    return items;
                }
            }
            if (value.isValueNode()) {
                String text = value.asText("");
                if (!text.isBlank()) {
                    return List.of(text.trim());
                }
            }
        }
        return List.of();
    }

    private List<String> normalizeStringList(List<String> values, List<String> fallback) {
        List<String> normalized = values == null ? List.of() : values.stream()
                .map(item -> item == null ? "" : item.trim())
                .filter(item -> !item.isBlank())
                .distinct()
                .limit(5)
                .toList();
        if (!normalized.isEmpty()) {
            return normalized;
        }
        return fallback == null || fallback.isEmpty() ? List.of() : fallback;
    }

    private String textFromJson(JsonNode root) {
        if (root == null || root.isNull()) {
            return "";
        }
        return firstNonBlank(
                textValue(root, "message"),
                textValue(root, "reply"),
                textValue(root, "analysis"),
                textValue(root, "content"),
                textValue(root, "text"),
                root.isValueNode() ? root.asText("") : ""
        );
    }

    private boolean looksLikeAiServiceFailure(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String normalized = text.toLowerCase();
        for (String keyword : AI_UNAVAILABLE_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return true;
            }
        }

        try {
            JsonNode root = objectMapper.readTree(text);
            String topLevelMessage = firstNonBlank(
                    textValue(root, "message"),
                    textValue(root, "msg"),
                    textValue(root, "error_msg")
            );
            String nestedErrorMessage = "";
            JsonNode errorNode = root.path("error");
            if (!errorNode.isMissingNode() && !errorNode.isNull()) {
                nestedErrorMessage = firstNonBlank(
                        textValue(errorNode, "message"),
                        textValue(errorNode, "msg"),
                        errorNode.isTextual() ? errorNode.asText("") : ""
                );
            }
            String mergedMessage = firstNonBlank(topLevelMessage, nestedErrorMessage);
            if (!mergedMessage.isBlank()) {
                String mergedLower = mergedMessage.toLowerCase();
                for (String keyword : AI_UNAVAILABLE_KEYWORDS) {
                    if (mergedLower.contains(keyword)) {
                        return true;
                    }
                }
            }

            String codeText = firstNonBlank(
                    textValue(root, "code"),
                    textValue(root, "status"),
                    textValue(root, "error_code")
            );
            if ("402".equals(codeText) || "429".equals(codeText) || "503".equals(codeText)) {
                return true;
            }
            return root.hasNonNull("error")
                    && !mergedMessage.isBlank();
        } catch (Exception ignore) {
            return false;
        }
    }

    private String textValue(JsonNode node, String fieldName) {
        if (node == null || fieldName == null || fieldName.isBlank()) {
            return "";
        }
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return "";
        }
        return value.isValueNode() ? value.asText("") : value.toString();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String abbreviate(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.replace("\r", "\\r").replace("\n", "\\n");
        return normalized.length() <= 600 ? normalized : normalized.substring(0, 600) + "...";
    }

    /**
     * 对面试助手结果做后处理。
     *
     * <p>原因：</p>
     * <p>
     * - 对话类 Agent 相比评分/分析类 Agent，更容易输出模型自己的“思考过程”或状态转述
     * </p>
     * <p>
     * - 即便结构化输出已经生效，模型偶尔仍可能漏掉动作按钮
     * </p>
     * <p>
     * 因此这里补两层兜底：
     * </p>
     * <p>
     * - 清洗 reply 中不应展示给用户的元话术
     * </p>
     * <p>
     * - 当 actions 为空时，基于问题、模式、进度和前端能力自动推断可执行按钮
     * </p>
     */
    private AgentStructuredOutputs.InterviewAssistantResult postProcessInterviewAssistant(
            AgentStructuredOutputs.InterviewAssistantResult result,
            Map<String, Object> params
    ) {
        AgentStructuredOutputs.InterviewAssistantResult safeResult =
                result == null ? new AgentStructuredOutputs.InterviewAssistantResult() : result;
        safeResult.setReply(stabilizeAssistantReply(params, sanitizeAssistantReply(safeResult.getReply())));

        List<String> availableActions = extractStringList(params.get("availableClientActions"));
        List<AgentStructuredOutputs.AssistantAction> actions =
                normalizeAssistantActions(safeResult.getActions(), availableActions);

        if (actions.isEmpty()) {
            actions = inferAssistantActions(params, safeResult.getReply(), availableActions);
        }
        safeResult.setActions(actions);
        safeResult.setIntent(normalizeAssistantIntent(
                safeResult.getIntent(),
                inferAssistantIntent(params, safeResult.getReply(), actions)
        ));
        safeResult.setTargetModule(normalizeTargetModule(
                safeResult.getTargetModule(),
                inferTargetModule(params, safeResult.getReply(), actions)
        ));
        safeResult.setTargetMode(normalizeTargetMode(
                safeResult.getTargetMode(),
                inferTargetMode(params, safeResult.getReply(), actions)
        ));
        safeResult.setTargetJobHint(normalizeTargetJobHint(
                safeResult.getTargetJobHint(),
                inferTargetJobHint(params, safeResult.getReply(), safeResult.getIntent())
        ));
        safeResult.setTargetJobConfirmed(resolveTargetJobConfirmed(
                safeResult.getTargetJobConfirmed(),
                params
        ));
        safeResult.setCompletionAcknowledged(resolveCompletionAcknowledged(
                safeResult.getCompletionAcknowledged(),
                params,
                safeResult.getReply()
        ));
        return safeResult;
    }

    private String stabilizeAssistantReply(Map<String, Object> params, String reply) {
        String question = value(params, "question");
        String completionType = detectCompletionType(question);
        if (completionType.isBlank()) {
            return reply;
        }
        String normalizedReply = reply == null ? "" : reply.trim();
        if (containsAny(normalizedReply, "已完成", "刚完成", "完成了", "下一步", "接下来")) {
            return normalizedReply;
        }
        String prefix = switch (completionType) {
            case "resume_completed" -> "已确认你已完成 AIview 测评中的简历投递。";
            case "questions_completed" -> "已确认你已完成 AIview 测评中的试题作答。";
            case "round_1_completed" -> "已确认你已完成 AIview 测评中的一面。";
            case "round_2_completed" -> "已确认你已完成 AIview 测评中的二面。";
            case "round_3_completed" -> "已确认你已完成 AIview 测评中的三面。";
            default -> "";
        };
        if (prefix.isBlank()) {
            return normalizedReply;
        }
        if (normalizedReply.isBlank()) {
            return prefix;
        }
        return prefix + "\n\n" + normalizedReply;
    }

    /**
     * 清洗面试助手给用户展示的 reply 文本。
     *
     * <p>只保留真正面向用户的结论、建议和下一步说明；
     * 去掉模型把内部判断过程直接说出来的元话术。</p>
     */
    private String sanitizeAssistantReply(String reply) {
        String text = reply == null ? "" : reply.replace("\r\n", "\n").trim();
        if (text.isEmpty()) {
            return "我来帮你安排下一步。";
        }

        String[] rawLines = text.split("\n");
        List<String> keptLines = new ArrayList<>();
        for (String rawLine : rawLines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                if (!keptLines.isEmpty() && !keptLines.get(keptLines.size() - 1).isBlank()) {
                    keptLines.add("");
                }
                continue;
            }
            if (isMetaReasoningLine(line)) {
                continue;
            }
            keptLines.add(line);
        }

        while (!keptLines.isEmpty() && (keptLines.get(0).isBlank() || isDividerLine(keptLines.get(0)))) {
            keptLines.remove(0);
        }
        while (!keptLines.isEmpty() && (keptLines.get(keptLines.size() - 1).isBlank()
                || isDividerLine(keptLines.get(keptLines.size() - 1)))) {
            keptLines.remove(keptLines.size() - 1);
        }

        String cleaned = String.join("\n", keptLines)
                .replaceAll("\n{3,}", "\n\n")
                .trim();
        return cleaned.isEmpty() ? "我来帮你安排下一步。" : cleaned;
    }

    /**
     * 判断一行文本是否属于不应该展示给用户的“内部推理/状态转述”。
     */
    private boolean isMetaReasoningLine(String line) {
        return line.startsWith("现在我需要")
                || line.startsWith("现在我来")
                || line.startsWith("好的，现在我来")
                || line.startsWith("根据用户")
                || line.startsWith("根据当前")
                || line.startsWith("我看到您当前")
                || line.startsWith("我看到当前")
                || line.startsWith("当前所有状态")
                || line.startsWith("根据可用的客户端动作")
                || line.startsWith("说明下一步")
                || line.startsWith("我将")
                || line.contains("availableClientActions")
                || line.contains("客户端动作")
                || line.contains("思考过程");
    }

    private boolean isDividerLine(String line) {
        return "---".equals(line) || "———".equals(line) || "***".equals(line);
    }

    private List<AgentStructuredOutputs.AssistantAction> normalizeAssistantActions(
            List<AgentStructuredOutputs.AssistantAction> actions,
            List<String> availableActions
    ) {
        if (actions == null || actions.isEmpty()) {
            return List.of();
        }
        List<AgentStructuredOutputs.AssistantAction> normalized = new ArrayList<>();
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (AgentStructuredOutputs.AssistantAction action : actions) {
            if (action == null || action.getType() == null || action.getType().isBlank()) {
                continue;
            }
            String type = action.getType().trim();
            if (!availableActions.contains(type) || !seen.add(type)) {
                continue;
            }
            AgentStructuredOutputs.AssistantAction item = new AgentStructuredOutputs.AssistantAction();
            item.setType(type);
            item.setLabel(action.getLabel() == null || action.getLabel().isBlank()
                    ? defaultActionLabel(type)
                    : action.getLabel().trim());
            normalized.add(item);
            if (normalized.size() >= 3) {
                break;
            }
        }
        return normalized;
    }

    private List<AgentStructuredOutputs.AssistantAction> inferAssistantActions(
            Map<String, Object> params,
            String reply,
            List<String> availableActions
    ) {
        if (availableActions.isEmpty()) {
            return List.of();
        }

        String question = value(params, "question");
        String evaluationMode = value(params, "evaluationMode");
        String combined = (question + "\n" + (reply == null ? "" : reply)).trim();
        Map<String, Object> state = asMap(params.get("currentState"));
        String completionType = detectCompletionType(question);

        List<AgentStructuredOutputs.AssistantAction> inferred = new ArrayList<>();
        if (!completionType.isBlank()) {
            if ("resume_completed".equals(completionType)) {
                if ("COMPREHENSIVE".equalsIgnoreCase(evaluationMode) && !isCompleted(state, "questions")) {
                    addActionIfAvailable(inferred, availableActions, "go_questions");
                }
            } else if ("questions_completed".equals(completionType)) {
                if ("COMPREHENSIVE".equalsIgnoreCase(evaluationMode) && !isCompleted(state, "round_1")) {
                    addActionIfAvailable(inferred, availableActions, "go_round_1");
                }
            } else if ("round_1_completed".equals(completionType)) {
                if ("COMPREHENSIVE".equalsIgnoreCase(evaluationMode) && !isCompleted(state, "round_2")) {
                    addActionIfAvailable(inferred, availableActions, "go_round_2");
                }
            } else if ("round_2_completed".equals(completionType)) {
                if ("COMPREHENSIVE".equalsIgnoreCase(evaluationMode) && !isCompleted(state, "round_3")) {
                    addActionIfAvailable(inferred, availableActions, "go_round_3");
                }
            } else if ("round_3_completed".equals(completionType)) {
                if ("COMPREHENSIVE".equalsIgnoreCase(evaluationMode)) {
                    addActionIfAvailable(inferred, availableActions, "restart_comprehensive");
                }
            }
            if (!inferred.isEmpty()) {
                return inferred;
            }
        }

        if (containsAny(combined, "简历", "上传简历")) {
            addActionIfAvailable(inferred, availableActions, "go_resume");
            if (!inferred.isEmpty()) {
                return inferred;
            }
        }

        if (containsAny(combined, "试题", "笔试")) {
            if ("COMPREHENSIVE".equalsIgnoreCase(evaluationMode) && !isCompleted(state, "resume")) {
                addActionIfAvailable(inferred, availableActions, "go_resume");
            } else {
                addActionIfAvailable(inferred, availableActions, "go_questions");
            }
            if (!inferred.isEmpty()) {
                return inferred;
            }
        }

        if (containsAny(combined, "一面", "技术一面", "二面", "技术二面", "三面", "HR")) {
            if ("COMPREHENSIVE".equalsIgnoreCase(evaluationMode) && !isCompleted(state, "questions")) {
                addActionIfAvailable(inferred, availableActions, "go_questions");
            } else if (containsAny(combined, "二面", "技术二面") && !isCompleted(state, "round_1")) {
                addActionIfAvailable(inferred, availableActions, "go_round_1");
            } else if (containsAny(combined, "三面", "HR") && !isCompleted(state, "round_2")) {
                addActionIfAvailable(inferred, availableActions, "go_round_2");
            } else if (containsAny(combined, "三面", "HR")) {
                addActionIfAvailable(inferred, availableActions, "go_round_3");
            } else if (containsAny(combined, "二面", "技术二面")) {
                addActionIfAvailable(inferred, availableActions, "go_round_2");
            } else {
                addActionIfAvailable(inferred, availableActions, "go_round_1");
            }
            if (!inferred.isEmpty()) {
                return inferred;
            }
        }

        if (containsAny(combined, "下一步", "接下来", "下一阶段", "继续")) {
            if (!isCompleted(state, "resume")) {
                addActionIfAvailable(inferred, availableActions, "go_resume");
            } else if (!isCompleted(state, "questions")) {
                addActionIfAvailable(inferred, availableActions, "go_questions");
            } else if (!isCompleted(state, "round_1")) {
                addActionIfAvailable(inferred, availableActions, "go_round_1");
            } else if (!isCompleted(state, "round_2")) {
                addActionIfAvailable(inferred, availableActions, "go_round_2");
            } else if (!isCompleted(state, "round_3")) {
                addActionIfAvailable(inferred, availableActions, "go_round_3");
            } else {
                addActionIfAvailable(inferred, availableActions, "restart_comprehensive");
            }
        }
        return inferred;
    }

    /**
     * 从当前综合测评状态中读取某个阶段是否已完成。
     */
    private boolean isCompleted(Map<String, Object> state, String key) {
        Map<String, Object> step = asMap(state.get(key));
        Object completed = step.get("completed");
        if (completed instanceof Boolean bool) {
            return bool;
        }
        return completed != null && Boolean.parseBoolean(String.valueOf(completed));
    }

    private void addActionIfAvailable(
            List<AgentStructuredOutputs.AssistantAction> actions,
            List<String> availableActions,
            String type
    ) {
        if (!availableActions.contains(type) || actions.stream().anyMatch(item -> type.equals(item.getType()))) {
            return;
        }
        AgentStructuredOutputs.AssistantAction action = new AgentStructuredOutputs.AssistantAction();
        action.setType(type);
        action.setLabel(defaultActionLabel(type));
        actions.add(action);
    }

    /**
     * 为常见动作类型提供稳定的默认按钮文案。
     *
     * <p>用途：当模型漏掉 label 时，前端仍能拿到可点击、可展示的动作按钮。</p>
     */
    private String defaultActionLabel(String type) {
        return switch (type) {
            case "go_resume" -> "去做简历投递";
            case "go_questions" -> "去做试题作答";
            case "go_round_1" -> "进入一面";
            case "go_round_2" -> "进入二面";
            case "go_round_3" -> "进入三面";
            case "restart_comprehensive" -> "重新开始 AIview 测评";
            default -> type;
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private List<String> extractStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .map(item -> item == null ? "" : String.valueOf(item).trim())
                .filter(item -> !item.isBlank())
                .toList();
    }

    private boolean containsAny(String text, String... keywords) {
        String content = text == null ? "" : text;
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String detectCompletionType(String text) {
        String content = text == null ? "" : text;
        if (content.isBlank()) {
            return "";
        }
        if ((content.contains("刚完成") || content.contains("已完成") || content.contains("完成了"))
                && content.contains("简历")) {
            return "resume_completed";
        }
        if ((content.contains("刚完成") || content.contains("已完成") || content.contains("完成了"))
                && (content.contains("试题") || content.contains("笔试"))) {
            return "questions_completed";
        }
        if ((content.contains("刚完成") || content.contains("已完成") || content.contains("完成了"))
                && (content.contains("一面") || content.contains("技术一面"))) {
            return "round_1_completed";
        }
        if ((content.contains("刚完成") || content.contains("已完成") || content.contains("完成了"))
                && (content.contains("二面") || content.contains("技术二面"))) {
            return "round_2_completed";
        }
        if ((content.contains("刚完成") || content.contains("已完成") || content.contains("完成了"))
                && (content.contains("三面") || content.contains("HR"))) {
            return "round_3_completed";
        }
        return "";
    }

    private String inferAssistantIntent(
            Map<String, Object> params,
            String reply,
            List<AgentStructuredOutputs.AssistantAction> actions
    ) {
        String question = value(params, "question");
        String completionType = detectCompletionType(question);
        if (!completionType.isBlank()) {
            return "module_completed";
        }
        if (containsJobSwitchIntent(question)) {
            return "switch_job";
        }
        String targetModule = inferTargetModule(params, reply, actions);
        if (!"NONE".equals(targetModule)
                && containsAny(question, "开始", "进入", "去做", "我要做", "我想做", "下一步", "接下来", "继续")) {
            return "navigate_module";
        }
        if (containsAny(question, "下一步", "接下来", "继续")) {
            return "next_step";
        }
        return "general_consultation";
    }

    private String inferTargetModule(
            Map<String, Object> params,
            String reply,
            List<AgentStructuredOutputs.AssistantAction> actions
    ) {
        String question = value(params, "question");
        String completionType = detectCompletionType(question);
        String evaluationMode = inferTargetMode(params, reply, actions);
        Map<String, Object> state = asMap(params.get("currentState"));
        if ("resume_completed".equals(completionType)
                && "COMPREHENSIVE".equalsIgnoreCase(evaluationMode)
                && !isCompleted(state, "questions")) {
            return "questions";
        }
        if ("questions_completed".equals(completionType)
                && "COMPREHENSIVE".equalsIgnoreCase(evaluationMode)
                && !isCompleted(state, "round_1")) {
            return "round_1";
        }
        if ("round_1_completed".equals(completionType)
                && "COMPREHENSIVE".equalsIgnoreCase(evaluationMode)
                && !isCompleted(state, "round_2")) {
            return "round_2";
        }
        if ("round_2_completed".equals(completionType)
                && "COMPREHENSIVE".equalsIgnoreCase(evaluationMode)
                && !isCompleted(state, "round_3")) {
            return "round_3";
        }
        for (AgentStructuredOutputs.AssistantAction action : actions == null ? List.<AgentStructuredOutputs.AssistantAction>of() : actions) {
            String module = targetModuleForAction(action == null ? "" : action.getType());
            if (!"NONE".equals(module)) {
                return module;
            }
        }
        String combined = (question + "\n" + (reply == null ? "" : reply)).trim();
        if (containsAny(combined, "简历", "上传简历")) {
            return "resume";
        }
        if (containsAny(combined, "试题", "笔试")) {
            return "questions";
        }
        if (containsAny(combined, "一面", "技术一面")) {
            return "round_1";
        }
        if (containsAny(combined, "二面", "技术二面")) {
            return "round_2";
        }
        if (containsAny(combined, "三面", "HR")) {
            return "round_3";
        }
        return "NONE";
    }

    private String inferTargetMode(
            Map<String, Object> params,
            String reply,
            List<AgentStructuredOutputs.AssistantAction> actions
    ) {
        String evaluationMode = value(params, "evaluationMode").trim();
        if (!evaluationMode.isBlank()) {
            return normalizeTargetMode(evaluationMode, "UNKNOWN");
        }
        for (AgentStructuredOutputs.AssistantAction action : actions == null ? List.<AgentStructuredOutputs.AssistantAction>of() : actions) {
            if (action == null || action.getType() == null) {
                continue;
            }
            if ("go_resume".equals(action.getType())
                    || "go_questions".equals(action.getType())
                    || "go_round_1".equals(action.getType())
                    || "go_round_2".equals(action.getType())
                    || "go_round_3".equals(action.getType())) {
                return "COMPREHENSIVE";
            }
        }
        String combined = (value(params, "question") + "\n" + (reply == null ? "" : reply)).trim();
        if (containsAny(combined, "AIview", "AI 模拟面试", "面试")) {
            return "COMPREHENSIVE";
        }
        return "UNKNOWN";
    }

    private String normalizeAssistantIntent(String rawIntent, String fallback) {
        String intent = rawIntent == null ? "" : rawIntent.trim();
        return switch (intent) {
            case "general_consultation", "choose_mode", "switch_job", "navigate_module", "module_completed", "next_step" -> intent;
            default -> fallback == null || fallback.isBlank() ? "general_consultation" : fallback;
        };
    }

    private String normalizeTargetModule(String rawTargetModule, String fallback) {
        String targetModule = rawTargetModule == null ? "" : rawTargetModule.trim().toLowerCase();
        return switch (targetModule) {
            case "resume", "questions", "round_1", "round_2", "round_3", "none" -> targetModule.toUpperCase().equals("NONE") ? "NONE" : targetModule;
            default -> fallback == null || fallback.isBlank() ? "NONE" : fallback;
        };
    }

    private String normalizeTargetMode(String rawTargetMode, String fallback) {
        String targetMode = rawTargetMode == null ? "" : rawTargetMode.trim().toUpperCase();
        return switch (targetMode) {
            case "SPECIAL", "COMPREHENSIVE", "UNKNOWN" -> targetMode;
            default -> fallback == null || fallback.isBlank() ? "UNKNOWN" : fallback;
        };
    }

    private Boolean resolveTargetJobConfirmed(Boolean rawValue, Map<String, Object> params) {
        if (rawValue != null) {
            return rawValue;
        }
        String question = value(params, "question");
        if (containsJobSwitchIntent(question)) {
            return false;
        }
        return !value(params, "currentJobId").isBlank();
    }

    private String normalizeTargetJobHint(String rawValue, String fallback) {
        String targetJobHint = rawValue == null ? "" : rawValue.trim();
        if (!targetJobHint.isBlank()) {
            return targetJobHint;
        }
        return fallback == null ? "" : fallback.trim();
    }

    private Boolean resolveCompletionAcknowledged(
            Boolean rawValue,
            Map<String, Object> params,
            String reply
    ) {
        if (rawValue != null) {
            return rawValue;
        }
        if (!detectCompletionType(value(params, "question")).isBlank()) {
            return true;
        }
        return containsAny(reply, "已确认你已完成", "你已完成", "刚完成", "已完成");
    }

    private boolean containsJobSwitchIntent(String text) {
        return containsAny(text, "切换", "换成", "换到", "改成", "改为", "转为", "转成", "转到");
    }

    private String inferTargetJobHint(Map<String, Object> params, String reply, String intent) {
        String question = value(params, "question");
        if ("switch_job".equals(intent) || containsJobSwitchIntent(question)) {
            String extracted = extractTargetJobHint(question);
            if (!extracted.isBlank()) {
                return extracted;
            }
        }
        if (Boolean.TRUE.equals(resolveTargetJobConfirmed(null, params))) {
            return "";
        }
        String replyExtracted = extractTargetJobHint(reply);
        return replyExtracted;
    }

    private String extractTargetJobHint(String text) {
        String content = text == null ? "" : text.trim();
        if (content.isBlank()) {
            return "";
        }
        String normalized = content
                .replaceAll("(请|帮我|一下|一下子|现在|当前|我想|我要|我准备|我希望)", " ")
                .replaceAll("(切换|换成|换到|改成|改为|转为|转成|转到)", " ")
                .replaceAll("(岗位|职位|方向|行业|评测|测评|面试|进行|开始|继续|下一步|模块)", " ")
                .replaceAll("[，。！？、,.!?()（）:：]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (normalized.isBlank()) {
            return "";
        }
        String[] parts = normalized.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            String token = part.trim();
            if (token.length() < 2) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(token);
            if (builder.length() >= 18) {
                break;
            }
        }
        return builder.toString().trim();
    }

    private String targetModuleForAction(String actionType) {
        return switch (actionType == null ? "" : actionType.trim()) {
            case "go_resume" -> "resume";
            case "go_questions" -> "questions";
            case "go_round_1" -> "round_1";
            case "go_round_2" -> "round_2";
            case "go_round_3" -> "round_3";
            default -> "NONE";
        };
    }

    private String extractJsonCandidate(String rawText) {
        String text = rawText == null ? "" : rawText.trim();
        if (text.isEmpty()) {
            return "{}";
        }

        Matcher codeBlockMatcher = JSON_CODE_BLOCK_PATTERN.matcher(text);
        if (codeBlockMatcher.find() && codeBlockMatcher.group(1) != null) {
            return codeBlockMatcher.group(1).trim();
        }

        try {
            objectMapper.readTree(text);
            return text;
        } catch (JsonProcessingException ignore) {
            Matcher objectMatcher = JSON_OBJECT_PATTERN.matcher(text);
            if (objectMatcher.find()) {
                return objectMatcher.group().trim();
            }
            return text;
        }
    }

    /**
     * 将 params 转成用户消息文本。
     *
     * <p>说明：这里的拼接逻辑是“最小必要”的提示增强，避免破坏原业务契约：
     * 前端仍然只传 params；系统约束与输出格式由 skill 决定。</p>
     */
    private String buildUserContent(String agentKey, Map<String, Object> params) {
        return switch (agentKey) {
            case "ai-resume-analysis" -> "岗位名称：" + value(params, "jobName")
                    + "\n岗位要求：" + value(params, "jobRequirements")
                    + "\n简历内容：" + value(params, "resumeContent");
            case "ai-interview-assistant" -> {
                StringBuilder builder = new StringBuilder();
                builder.append("当前用户问题：").append(value(params, "question")).append("\n");
                builder.append("当前岗位ID：").append(value(params, "currentJobId")).append("\n");
                builder.append("用户已选择的测评模式：").append(value(params, "evaluationMode")).append("\n");
                builder.append("当前 AIview 通过标准(JSON)：").append(GSON.toJson(params.get("assessmentThresholds"))).append("\n");
                builder.append("当前测评进度摘要：").append(summarizeCurrentState(asMap(params.get("currentState")))).append("\n");
                builder.append("当前测评进度(JSON)：").append(GSON.toJson(params.get("currentState"))).append("\n");
                builder.append("会话历史摘要：").append(value(params, "conversationSummary")).append("\n");
                builder.append("最近对话历史(JSON)：").append(GSON.toJson(params.get("conversationHistory"))).append("\n");
                builder.append("前端当前支持的动作按钮类型(JSON)：").append(GSON.toJson(params.get("availableClientActions")));
                yield builder.toString();
            }
            default -> "参数(JSON)：\n" + GSON.toJson(params);
        };
    }

    private String value(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private String summarizeCurrentState(Map<String, Object> state) {
        if (state == null || state.isEmpty()) {
            return "暂无测评进度";
        }
        return "简历="
                + stepSummary(state, "resume")
                + "；试题="
                + stepSummary(state, "questions")
                + "；一面="
                + stepSummary(state, "round_1")
                + "；二面="
                + stepSummary(state, "round_2")
                + "；三面="
                + stepSummary(state, "round_3")
                + "；总体="
                + overallSummary(asMap(state.get("overall")));
    }

    private String stepSummary(Map<String, Object> state, String key) {
        Map<String, Object> step = asMap(state.get(key));
        if (step.isEmpty()) {
            return "未开始";
        }
        boolean completed = isCompleted(state, key);
        boolean inProgress = Boolean.parseBoolean(String.valueOf(step.getOrDefault("inProgress", false)));
        int score = (int) Math.round(parseDouble(step.get("score")));
        if (completed) {
            return score > 0 ? "已完成(" + score + "分)" : "已完成";
        }
        if (inProgress) {
            return "进行中";
        }
        return "未开始";
    }

    private String overallSummary(Map<String, Object> overall) {
        if (overall.isEmpty()) {
            return "未开始";
        }
        String status = String.valueOf(overall.getOrDefault("status", "not_started"));
        int totalScore = (int) Math.round(parseDouble(overall.get("totalScore")));
        if ("completed".equalsIgnoreCase(status)) {
            return totalScore > 0 ? "已完成(" + totalScore + "分)" : "已完成";
        }
        if ("in_progress".equalsIgnoreCase(status)) {
            return "进行中";
        }
        return "未开始";
    }

    private double parseDouble(Object value) {
        if (value == null) {
            return 0D;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return 0D;
        }
    }
}

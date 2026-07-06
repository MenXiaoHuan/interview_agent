package com.multimodal.interview.service.impl;

import com.multimodal.interview.dto.*;
import com.multimodal.interview.entity.*;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.mapper.*;
import com.multimodal.interview.service.AiAssessmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 综合评估历史服务实现。
 */
@Slf4j
@Service
public class AiAssessmentServiceImpl implements AiAssessmentService {
    private static final int DEFAULT_PASS_SCORE = 80;
    private static final String ROUND_1 = "round_1";
    private static final String ROUND_2 = "round_2";
    private static final String ROUND_3 = "round_3";
    private static final Map<String, String> ROUND_LABELS = Map.of(
            ROUND_1, "技术一面",
            ROUND_2, "技术二面",
            ROUND_3, "HR 三面"
    );

    @Autowired
    private AiResumeHistoryMapper resumeHistoryMapper;

    @Autowired
    private AiQuestionHistoryMapper questionHistoryMapper;

    @Autowired
    private AiInterviewRoundHistoryMapper scenarioHistoryMapper;

    @Autowired
    private AiAssessmentSessionMapper assessmentSessionMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AiResumeHistory saveResumeHistory(AiResumeHistoryRequest request) {
        request.setAssessmentSessionId(resolveAssessmentSessionId(
                request.getAssessmentSessionId(),
                request.getUserId(),
                request.getJobId()
        ));
        AiAssessmentSession session = getOrCreateAssessmentSession(
                request.getAssessmentSessionId(),
                request.getUserId(),
                request.getJobId()
        );
        ensureSessionWritable(session, "简历评测");
        AiResumeHistory resume = new AiResumeHistory();
        BeanUtils.copyProperties(request, resume);

        int result = resumeHistoryMapper.insert(resume);
        if (result <= 0) {
            throw BusinessException.internalError("保存简历综合评估历史失败");
        }

        int passScore = resolveStagePassScore(request.getPassScores(), "resume", request.getPassScore());
        session.setCurrentStage(safeScore(resume.getOverallScore()) >= passScore ? "questions" : "resume");
        session.setStatus(safeScore(resume.getOverallScore()) >= passScore ? "IN_PROGRESS" : "FAILED");
        session.setEliminationReason(safeScore(resume.getOverallScore()) >= passScore ? null : "resume_score_below_threshold");
        session.setEndTime(safeScore(resume.getOverallScore()) >= passScore ? null : resume.getUploadTime());
        assessmentSessionMapper.updateByAssessmentSessionId(session);

        return resume;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AiQuestionHistory saveQuestionHistory(AiQuestionHistoryRequest request) {
        request.setAssessmentSessionId(resolveAssessmentSessionId(
                request.getAssessmentSessionId(),
                request.getUserId(),
                request.getJobId()
        ));
        AiAssessmentSession session = getOrCreateAssessmentSession(
                request.getAssessmentSessionId(),
                request.getUserId(),
                request.getJobId()
        );
        ensureSessionWritable(session, "试题作答");
        AiResumeHistory resumeHistory =
                resumeHistoryMapper.findLatestByAssessmentSessionId(request.getAssessmentSessionId());
        if (resumeHistory == null) {
            throw BusinessException.badRequest("请先完成简历评测后再进入试题作答");
        }
        int resumePassScore = resolveStagePassScore(request.getPassScores(), "resume", null);
        int questionPassScore = resolveStagePassScore(request.getPassScores(), "questions", request.getPassScore());
        if (safeScore(resumeHistory.getOverallScore()) < resumePassScore) {
            throw BusinessException.badRequest("简历评测未达到" + resumePassScore + "分，不能进入试题作答");
        }
        AiQuestionHistory question = new AiQuestionHistory();
        BeanUtils.copyProperties(request, question);

        int result = questionHistoryMapper.insert(question);
        if (result <= 0) {
            throw BusinessException.internalError("保存问题综合评估历史失败");
        }

        boolean questionPassed = safeScore(question.getOverallScore()) >= questionPassScore;
        session.setStatus(questionPassed ? "IN_PROGRESS" : "FAILED");
        session.setCurrentStage(questionPassed ? ROUND_1 : "questions");
        session.setEliminationReason(questionPassed ? null : "questions_score_below_threshold");
        session.setEndTime(questionPassed ? null : question.getEndTime());
        assessmentSessionMapper.updateByAssessmentSessionId(session);

        return question;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AiInterviewRoundHistory saveScenarioHistory(AiInterviewRoundHistoryRequest request) {
        request.setAssessmentSessionId(resolveAssessmentSessionId(
                request.getAssessmentSessionId(),
                request.getUserId(),
                request.getJobId()
        ));
        AiAssessmentSession session = getOrCreateAssessmentSession(
                request.getAssessmentSessionId(),
                request.getUserId(),
                request.getJobId()
        );
        ensureSessionWritable(session, ROUND_LABELS.getOrDefault(request.getRoundType(), "面试轮次"));
        String roundType = normalizeRoundType(request.getRoundType());
        request.setRoundType(roundType);
        AiResumeHistory resumeHistory =
                resumeHistoryMapper.findLatestByAssessmentSessionId(request.getAssessmentSessionId());
        if (resumeHistory == null || safeScore(resumeHistory.getOverallScore()) < resolveStagePassScore(request.getPassScores(), "resume", null)) {
            throw BusinessException.badRequest("简历评测未通过，不能进入面试轮次");
        }
        AiQuestionHistory questionHistory =
                questionHistoryMapper.findLatestByAssessmentSessionId(request.getAssessmentSessionId());
        if (questionHistory == null) {
            throw BusinessException.badRequest("请先完成试题作答后再进入面试轮次");
        }
        if (safeScore(questionHistory.getOverallScore()) < resolveStagePassScore(request.getPassScores(), "questions", null)) {
            throw BusinessException.badRequest("试题作答未通过，不能进入面试轮次");
        }
        ensureScenarioRoundOrder(request.getAssessmentSessionId(), roundType, request.getPassScores());
        AiInterviewRoundHistory scenario = new AiInterviewRoundHistory();
        BeanUtils.copyProperties(request, scenario);

        // 验证和清理 aiAssessment 数据
        if (scenario.getAiAssessment() != null) {
            String aiAssessment = scenario.getAiAssessment().trim();

            // 处理包含 Markdown 代码块的 JSON 格式
            if (aiAssessment.startsWith("```json\n") && aiAssessment.endsWith("\n```")) {
                // 提取 JSON 内容，去掉 Markdown 代码块标记
                String jsonContent = aiAssessment.substring(8, aiAssessment.length() - 4).trim();
                scenario.setAiAssessment(jsonContent);
            } else if (aiAssessment.startsWith("```") && aiAssessment.endsWith("```")) {
                // 处理其他类型的代码块
                String jsonContent = aiAssessment.substring(3, aiAssessment.length() - 3).trim();
                scenario.setAiAssessment(jsonContent);
            } else if (aiAssessment.equals("Invalid value.") || aiAssessment.isEmpty() ||
                    aiAssessment.equals("null") || aiAssessment.equals("undefined")) {
                scenario.setAiAssessment(null);
            } else {
                // 验证是否为有效的 JSON 格式
                try {
                    // 检查是否以 { 或 [ 开头，并且以 } 或 ] 结尾
                    if ((aiAssessment.startsWith("{") && aiAssessment.endsWith("}")) ||
                            (aiAssessment.startsWith("[") && aiAssessment.endsWith("]"))) {
                        // 使用 Jackson 验证 JSON 格式
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        mapper.readTree(aiAssessment); // 如果JSON无效，这里会抛出异常
                    } else {
                        // 不是有效的JSON格式，设置为null
                        scenario.setAiAssessment(null);
                    }
                } catch (Exception e) {
                    // 如果JSON格式无效，设置为null
                    log.warn("AI Assessment JSON格式无效，设置为null: {}", aiAssessment);
                    scenario.setAiAssessment(null);
                }
            }
        }

        int result = scenarioHistoryMapper.insert(scenario);
        if (result <= 0) {
            throw BusinessException.internalError("保存场景综合评估历史失败");
        }

        boolean roundPassed = safeScore(scenario.getOverallScore()) >= resolveStagePassScore(request.getPassScores(), roundType, request.getPassScore());
        session.setStatus(roundPassed ? (ROUND_3.equals(roundType) ? "PASSED" : "IN_PROGRESS") : "FAILED");
        session.setCurrentStage(roundPassed ? resolveNextStage(roundType) : roundType);
        session.setEliminationReason(roundPassed ? null : roundType + "_score_below_threshold");
        session.setEndTime((ROUND_3.equals(roundType) || !roundPassed) ? scenario.getEndTime() : null);
        assessmentSessionMapper.updateByAssessmentSessionId(session);

        return scenario;
    }

    private String resolveAssessmentSessionId(String providedSessionId, Long userId, Long jobId) {
        String safeSessionId = providedSessionId == null ? "" : providedSessionId.trim();
        if (!safeSessionId.isEmpty()) {
            return safeSessionId;
        }
        return "cmp_" + String.valueOf(userId) + "_" + String.valueOf(jobId) + "_" + System.currentTimeMillis();
    }

    private int safeScore(Integer score) {
        return score == null ? 0 : score;
    }

    private int resolvePassScore(Integer passScore) {
        return passScore == null ? DEFAULT_PASS_SCORE : Math.max(0, Math.min(100, passScore));
    }

    private int resolveStagePassScore(Map<String, Integer> passScores, String stage, Integer fallbackPassScore) {
        if (passScores != null && stage != null && passScores.get(stage) != null) {
            return resolvePassScore(passScores.get(stage));
        }
        return resolvePassScore(fallbackPassScore);
    }

    private String normalizeRoundType(String roundType) {
        String safeRoundType = roundType == null ? "" : roundType.trim().toLowerCase();
        return switch (safeRoundType) {
            case ROUND_1, ROUND_2, ROUND_3 -> safeRoundType;
            case "", "audio" -> ROUND_1;
            default -> throw BusinessException.badRequest("无效的面试轮次: " + roundType);
        };
    }

    private void ensureScenarioRoundOrder(String assessmentSessionId, String roundType, Map<String, Integer> passScores) {
        if (ROUND_2.equals(roundType)) {
            requireScenarioRound(assessmentSessionId, ROUND_1, passScores);
            return;
        }
        if (ROUND_3.equals(roundType)) {
            requireScenarioRound(assessmentSessionId, ROUND_2, passScores);
        }
    }

    private void requireScenarioRound(String assessmentSessionId, String roundType, Map<String, Integer> passScores) {
        AiInterviewRoundHistory scenarioHistory =
                scenarioHistoryMapper.findLatestByAssessmentSessionIdAndRoundType(assessmentSessionId, roundType);
        if (scenarioHistory == null) {
            throw BusinessException.badRequest("缺少" + ROUND_LABELS.getOrDefault(roundType, roundType) + "记录，不能继续当前流程");
        }
        if (safeScore(scenarioHistory.getOverallScore()) < resolveStagePassScore(passScores, roundType, null)) {
            throw BusinessException.badRequest(ROUND_LABELS.getOrDefault(roundType, roundType) + "未通过，不能继续当前流程");
        }
    }

    private AiAssessmentSession getOrCreateAssessmentSession(String assessmentSessionId, Long userId, Long jobId) {
        AiAssessmentSession existing = assessmentSessionMapper.findByAssessmentSessionId(assessmentSessionId);
        if (existing != null) {
            return existing;
        }
        AiAssessmentSession created = new AiAssessmentSession();
        created.setAssessmentSessionId(assessmentSessionId);
        created.setUserId(userId);
        created.setJobId(jobId);
        created.setStatus("IN_PROGRESS");
        created.setCurrentStage("resume");
        created.setStartTime(java.time.LocalDateTime.now());
        assessmentSessionMapper.insert(created);
        return created;
    }

    private void ensureSessionWritable(AiAssessmentSession session, String stageLabel) {
        if (session == null) {
            return;
        }
        if ("FAILED".equalsIgnoreCase(session.getStatus()) || "PASSED".equalsIgnoreCase(session.getStatus())) {
            throw BusinessException.badRequest("当前综合测评流程已结束，不能继续写入" + stageLabel);
        }
    }

    private String resolveNextStage(String roundType) {
        if (ROUND_1.equals(roundType)) {
            return ROUND_2;
        }
        if (ROUND_2.equals(roundType)) {
            return ROUND_3;
        }
        return "completed";
    }
} 

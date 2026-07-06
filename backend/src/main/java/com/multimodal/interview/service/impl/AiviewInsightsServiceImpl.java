package com.multimodal.interview.service.impl;

import com.multimodal.interview.dto.AiviewInsightAggregate;
import com.multimodal.interview.dto.AiviewInsightStageResponse;
import com.multimodal.interview.dto.AiviewJobInsightsResponse;
import com.multimodal.interview.mapper.AiviewInsightsMapper;
import com.multimodal.interview.service.AiviewInsightsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * AIview 岗位洞察服务实现。
 */
@Service
public class AiviewInsightsServiceImpl implements AiviewInsightsService {
    private static final int DEFAULT_PASS_SCORE = 80;
    private static final int WINDOW_DAYS = 7;

    private final AiviewInsightsMapper aiviewInsightsMapper;

    public AiviewInsightsServiceImpl(AiviewInsightsMapper aiviewInsightsMapper) {
        this.aiviewInsightsMapper = aiviewInsightsMapper;
    }

    @Override
    public AiviewJobInsightsResponse getLast7DaysInsights(Long jobId) {
        List<AiviewInsightStageResponse> stages = new ArrayList<>();
        stages.add(toStage("resume", "简历投递", aiviewInsightsMapper.aggregateResume(jobId)));
        stages.add(toStage("question", "试题作答", aiviewInsightsMapper.aggregateQuestion(jobId)));
        stages.add(toStage("round_1", "一面", aiviewInsightsMapper.aggregateRound(jobId, "round_1")));
        stages.add(toStage("round_2", "二面", aiviewInsightsMapper.aggregateRound(jobId, "round_2")));
        stages.add(toStage("round_3", "三面", aiviewInsightsMapper.aggregateRound(jobId, "round_3")));

        AiviewJobInsightsResponse response = new AiviewJobInsightsResponse();
        response.setJobId(jobId);
        response.setDays(WINDOW_DAYS);
        response.setStages(stages);
        response.setTotalParticipantCount(stages.stream()
                .mapToInt(AiviewInsightStageResponse::getParticipantCount)
                .sum());
        response.setSummary(buildSummary(stages));
        return response;
    }

    private AiviewInsightStageResponse toStage(String stageKey, String stageName, AiviewInsightAggregate aggregate) {
        int attemptCount = aggregate == null || aggregate.getAttemptCount() == null
                ? 0
                : aggregate.getAttemptCount();
        int participantCount = aggregate == null || aggregate.getParticipantCount() == null
                ? 0
                : aggregate.getParticipantCount();
        Double averageScore = aggregate == null ? null : round(aggregate.getAverageScore());
        Double maxScore = aggregate == null ? null : round(aggregate.getMaxScore());

        AiviewInsightStageResponse stage = new AiviewInsightStageResponse();
        stage.setStageKey(stageKey);
        stage.setStageName(stageName);
        stage.setAttemptCount(attemptCount);
        stage.setParticipantCount(participantCount);
        stage.setAverageScore(averageScore);
        stage.setMaxScore(maxScore);
        stage.setStatus(buildStatus(participantCount, averageScore));
        stage.setAdvice(buildAdvice(stageName, participantCount, averageScore));
        return stage;
    }

    private String buildStatus(int participantCount, Double averageScore) {
        if (participantCount == 0 || averageScore == null) {
            return "insufficient-data";
        }
        if (averageScore < DEFAULT_PASS_SCORE) {
            return "at-risk";
        }
        if (averageScore < DEFAULT_PASS_SCORE + 10) {
            return "needs-attention";
        }
        return "stable";
    }

    private String buildAdvice(String stageName, int participantCount, Double averageScore) {
        if (participantCount == 0 || averageScore == null) {
            return stageName + "近 7 天暂无有效样本，建议先引导候选人完成该环节以形成趋势判断。";
        }
        if (averageScore < DEFAULT_PASS_SCORE) {
            return stageName + "平均分低于 " + DEFAULT_PASS_SCORE + "，建议优先复盘岗位要求与候选人准备材料，收紧通过标准并补充针对性练习。";
        }
        if (averageScore < DEFAULT_PASS_SCORE + 10) {
            return stageName + "表现处于可提升区间，建议关注低分能力项，并在 AIview 对话中强化阶段反馈。";
        }
        return stageName + "整体表现稳定，可继续保持当前筛选与训练节奏，并沉淀高分样本经验。";
    }

    private String buildSummary(List<AiviewInsightStageResponse> stages) {
        long emptyStageCount = stages.stream()
                .filter(stage -> "insufficient-data".equals(stage.getStatus()))
                .count();
        long riskStageCount = stages.stream()
                .filter(stage -> "at-risk".equals(stage.getStatus()))
                .count();

        if (emptyStageCount == stages.size()) {
            return "当前岗位最近 7 天暂无 AIview 有效测评数据，建议先积累候选人样本。";
        }
        if (riskStageCount > 0) {
            return "当前岗位最近 7 天存在薄弱环节，建议优先处理低于 " + DEFAULT_PASS_SCORE + " 分的阶段。";
        }
        if (emptyStageCount > 0) {
            return "当前岗位最近 7 天已有部分 AIview 数据，仍需补齐未覆盖阶段。";
        }
        return "当前岗位最近 7 天五个环节均有有效数据，可结合阶段均分持续优化面试策略。";
    }

    private Double round(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}

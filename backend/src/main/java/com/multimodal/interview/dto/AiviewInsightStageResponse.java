package com.multimodal.interview.dto;

import lombok.Data;

/**
 * AIview 岗位洞察单阶段统计。
 */
@Data
public class AiviewInsightStageResponse {
    private String stageKey;
    private String stageName;
    private Integer attemptCount;
    private Integer participantCount;
    private Double averageScore;
    private Double maxScore;
    private String status;
    private String advice;
}

package com.multimodal.interview.dto;

import lombok.Data;

/**
 * AIview 岗位洞察聚合查询结果。
 */
@Data
public class AiviewInsightAggregate {
    private Integer attemptCount;
    private Integer participantCount;
    private Double averageScore;
    private Double maxScore;
}

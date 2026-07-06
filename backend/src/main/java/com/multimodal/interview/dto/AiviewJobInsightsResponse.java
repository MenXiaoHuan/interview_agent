package com.multimodal.interview.dto;

import lombok.Data;

import java.util.List;

/**
 * AIview 岗位近 7 天洞察响应。
 */
@Data
public class AiviewJobInsightsResponse {
    private Long jobId;
    private Integer days;
    private Integer totalParticipantCount;
    private String summary;
    private List<AiviewInsightStageResponse> stages;
}

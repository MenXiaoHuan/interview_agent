package com.multimodal.interview.service;

import com.multimodal.interview.dto.AiviewJobInsightsResponse;

/**
 * AIview 岗位洞察服务。
 */
public interface AiviewInsightsService {
    AiviewJobInsightsResponse getLast7DaysInsights(Long jobId);
}

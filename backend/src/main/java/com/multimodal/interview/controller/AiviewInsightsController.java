package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.dto.AiviewJobInsightsResponse;
import com.multimodal.interview.service.AiviewInsightsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AIview 岗位洞察接口控制器。
 */
@Tag(name = "AIview岗位洞察", description = "按岗位分析最近 7 天 AIview 五阶段表现")
@RestController
@RequestMapping("/api/aiview-insights")
public class AiviewInsightsController {
    private final AiviewInsightsService aiviewInsightsService;

    public AiviewInsightsController(AiviewInsightsService aiviewInsightsService) {
        this.aiviewInsightsService = aiviewInsightsService;
    }

    /**
     * 查询岗位最近 7 天 AIview 五阶段洞察。
     *
     * @param jobId 岗位 ID
     * @return 统一响应体
     */
    @Operation(summary = "查询岗位最近 7 天 AIview 五阶段洞察")
    @GetMapping("/job/{jobId}/last-7-days")
    public ApiResponse<AiviewJobInsightsResponse> getLast7DaysInsights(@PathVariable Long jobId) {
        return ApiResponse.success(aiviewInsightsService.getLast7DaysInsights(jobId));
    }
}

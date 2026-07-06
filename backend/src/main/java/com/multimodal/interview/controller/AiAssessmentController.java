package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.dto.*;
import com.multimodal.interview.entity.AiQuestionHistory;
import com.multimodal.interview.entity.AiResumeHistory;
import com.multimodal.interview.entity.AiInterviewRoundHistory;
import com.multimodal.interview.service.AiAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * AIview 测评流程归档接口控制器。
 */
@Tag(name = "AIview测评归档", description = "AIview测评流程归档相关接口")
@RestController
@RequestMapping("/api/ai-assessment")
public class AiAssessmentController {

    private final AiAssessmentService aiAssessmentService;

    public AiAssessmentController(AiAssessmentService aiAssessmentService) {
        this.aiAssessmentService = aiAssessmentService;
    }
    /**
     * 保存简历综合评估历史。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "保存AIview简历投递归档")
    @PostMapping("/resume")
    public ApiResponse<AiResumeHistory> saveResumeHistory(@Valid @RequestBody AiResumeHistoryRequest request){
        AiResumeHistory history = aiAssessmentService.saveResumeHistory(request);
        return ApiResponse.success("保存AIview简历投递归档成功", history);
    }
    /**
     * 保存问题综合评估历史。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "保存AIview试题作答归档")
    @PostMapping("/question")
    public ApiResponse<AiQuestionHistory> saveQuestionHistory(@Valid @RequestBody AiQuestionHistoryRequest request){
        AiQuestionHistory history = aiAssessmentService.saveQuestionHistory(request);
        return ApiResponse.success("保存AIview试题作答归档成功", history);
    }
    /**
     * 保存场景综合评估历史。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "保存AIview面试轮次归档")
    @PostMapping("/interview-round")
    public ApiResponse<AiInterviewRoundHistory> saveScenarioHistory(@Valid @RequestBody AiInterviewRoundHistoryRequest request){
        AiInterviewRoundHistory history = aiAssessmentService.saveScenarioHistory(request);
        return ApiResponse.success("保存AIview面试轮次归档成功", history);
    }
}

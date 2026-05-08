package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.dto.*;
import com.multimodal.interview.entity.ComprehensiveQuestionHistory;
import com.multimodal.interview.entity.ComprehensiveReportHistory;
import com.multimodal.interview.entity.ComprehensiveResumeHistory;
import com.multimodal.interview.entity.ComprehensiveScenarioHistory;
import com.multimodal.interview.service.ComprehensiveHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 综合评估历史接口控制器。
 */
@Tag(name = "综合评估历史", description = "综合评估历史相关接口")
@RestController
@RequestMapping("/api/comprehensive-history")
public class ComprehensiveHistoryController {

    private final ComprehensiveHistoryService comprehensiveHistoryService;

    public ComprehensiveHistoryController(ComprehensiveHistoryService comprehensiveHistoryService) {
        this.comprehensiveHistoryService = comprehensiveHistoryService;
    }
    /**
     * 保存简历综合评估历史。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "保存简历综合评估历史")
    @PostMapping("/resume")
    public ApiResponse<ComprehensiveResumeHistory> saveResumeHistory(@Valid @RequestBody ComprehensiveResumeHistoryRequest request){
        ComprehensiveResumeHistory history = comprehensiveHistoryService.saveResumeHistory(request);
        return ApiResponse.success("保存简历综合评估历史成功", history);
    }
    /**
     * 保存问题综合评估历史。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "保存问题综合评估历史")
    @PostMapping("/question")
    public ApiResponse<ComprehensiveQuestionHistory> saveQuestionHistory(@Valid @RequestBody ComprehensiveQuestionHistoryRequest request){
        ComprehensiveQuestionHistory history = comprehensiveHistoryService.saveQuestionHistory(request);
        return ApiResponse.success("保存问题综合评估历史成功", history);
    }
    /**
     * 保存场景综合评估历史。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "保存场景综合评估历史")
    @PostMapping("/scenario")
    public ApiResponse<ComprehensiveScenarioHistory> saveScenarioHistory(@Valid @RequestBody ComprehensiveScenarioHistoryRequest request){
        ComprehensiveScenarioHistory history = comprehensiveHistoryService.saveScenarioHistory(request);
        return ApiResponse.success("保存场景综合评估历史成功", history);
    }
    /**
     * 保存综合评估报告。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "保存综合评估报告")
    @PostMapping("/report")
    public ApiResponse<ComprehensiveReportHistory> saveComprehensiveReport(@Valid @RequestBody ComprehensiveReportRequest request){
        ComprehensiveReportHistory report = comprehensiveHistoryService.saveComprehensiveReport(request);
        return ApiResponse.success("保存综合评估报告成功", report);
    }
    /**
     * 获取综合评估报告详情。
     *
     * @param id 主键 ID
     * @return 统一响应体
     */
    @Operation(summary = "获取综合评估报告详情")
    @GetMapping("/report/{id}")
    public ApiResponse<ComprehensiveReportResponse> getComprehensiveReport(@PathVariable("id") Long id){
        ComprehensiveReportResponse report = comprehensiveHistoryService.getComprehensiveReportById(id);
        return ApiResponse.success("获取综合评估报告成功", report);
    }
    /**
     * 获取用户的所有综合评估报告。
     *
     * @param userId 用户 ID
     * @return 统一响应体
     */
    @Operation(summary = "获取用户的所有综合评估报告")
    @GetMapping("/reports/user/{userId}")
    public ApiResponse<List<ComprehensiveReportResponse>> getUserComprehensiveReports(@PathVariable("userId") Long userId){
        List<ComprehensiveReportResponse> reports = comprehensiveHistoryService.getComprehensiveReportsByUserId(userId);
        return ApiResponse.success("获取用户综合评估报告成功", reports);
    }
}

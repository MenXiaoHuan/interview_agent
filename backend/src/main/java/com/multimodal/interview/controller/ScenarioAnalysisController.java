package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.common.result.ResultCode;
import com.multimodal.interview.dto.SaveScenarioAnalysisHistoryRequest;
import com.multimodal.interview.entity.ScenarioAnalysis;
import com.multimodal.interview.service.ScenarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 场景分析接口控制器。
 */
@Slf4j
@Tag(name = "场景分析", description = "场景分析相关接口")
@RestController
@RequestMapping("/scenario")
@RequiredArgsConstructor
public class ScenarioAnalysisController {

    private final ScenarioService scenarioService;
    /**
     * 保存场景分析历史。
     *
     * @param scenarioHistory 场景分析历史请求
     * @param evaluationType 评估类型
     * @return 统一响应体
     */
    @Operation(summary = "保存场景分析历史")
    @PostMapping("/save/history")
    public ApiResponse<String> saveScenarioAnalysisHistory(
            @RequestBody SaveScenarioAnalysisHistoryRequest scenarioHistory,
            @RequestParam(required = false, defaultValue = "SPECIAL") String evaluationType){
        try {
            scenarioService.saveScenarioAnalysisHistory(scenarioHistory, evaluationType);
            return ApiResponse.success("保存成功");
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.INTERNAL_ERROR,"保存失败: " + e.getMessage());
        }
    }
    /**
     * 获取用户场景分析历史。
     *
     * @param userId 用户 ID
     * @param evaluationType 评估类型
     * @return 统一响应体
     */
    @Operation(summary = "获取用户场景分析历史",
            description = "获取指定用户的指定的场景分析记录 ")
    @GetMapping("/history/{userId}")
    public ApiResponse<List<ScenarioAnalysis>> getScenarioAnalysisHistory(
            @PathVariable String userId,
            @RequestParam(required = false, defaultValue = "SPECIAL") String evaluationType){
        try {
            List<ScenarioAnalysis> history = scenarioService.getScenarioAnalysisHistory(userId, evaluationType);
            return ApiResponse.success(history);
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.INTERNAL_ERROR,"获取失败: " + e.getMessage());
        }
    }
} 
package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.common.result.ResultCode;
import com.multimodal.interview.dto.ResumeContentResponse;
import com.multimodal.interview.dto.SaveResumeHistoryRequest;
import com.multimodal.interview.entity.ResumeAnalysisHistory;
import com.multimodal.interview.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 简历分析接口控制器。
 */
@Slf4j
@Tag(name = "简历分析", description = "简历分析相关接口")
@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;
    /**
     * 简历内容提取。
     *
     * @param file 上传文件
     * @return 统一响应体
     */
    @Operation(summary = "简历内容提取",
            description = "将简历文件转换为文本内容")
    @PostMapping("/extract")
    public ApiResponse<String> extractResumeContent(
            @RequestParam("file") MultipartFile file
    ){
        try {
            String resumeContent = resumeService.extractResumeContent(file);
            return ApiResponse.success("简历内容提取成功", resumeContent);
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.SYSTEM_ERROR.getCode(), "Error extracting resume content: " + e.getMessage());
        }
    }
    /**
     * 保存简历分析历史。
     *
     * @param resume 简历分析历史请求
     * @param evaluationType 评估类型
     * @return 统一响应体
     */
    @Operation(summary = "保存简历分析历史",
            description = "保存简历分析历史")
    @PostMapping("/save/history")
    public ApiResponse<String> saveResumeAnalysisHistory(
            @RequestBody SaveResumeHistoryRequest resume,
            @RequestParam(required = false, defaultValue = "SPECIAL") String evaluationType
    ){
        try {
            String resumeHistoryId = resumeService.saveResumeHistory(resume, evaluationType);
            return ApiResponse.success(resumeHistoryId);
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.INTERNAL_ERROR, e.getMessage());
        }
    }
    /**
     * 获取简历分析历史。
     *
     * @param userId 用户 ID
     * @param evaluationType 评估类型
     * @return 统一响应体
     */
    @Operation(summary = "获取简历分析历史",
            description = "通过用户id和历史类型获取简历分析历史")
    @GetMapping("/history/{userId}")
    public ApiResponse<List<ResumeAnalysisHistory>> getResumeAnalysisHistoryByUserIdAndEvaluationType(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "SPECIAL") String evaluationType){
        List<ResumeAnalysisHistory> records = resumeService
                .getResumeAnalysisHistoryByUserIdAndEvaluationType(userId, evaluationType);
        return ApiResponse.success(records);
    }
    /**
     * 获取简历分析历史。
     *
     * @param resumeHistoryId 简历历史 ID
     * @return 统一响应体
     */
    @Operation(summary = "获取简历分析历史",
            description = "通过简历历史id获取简历分析历史")
    @GetMapping("/history/id/{resumeHistoryId}")
    public ApiResponse<ResumeAnalysisHistory> getResumeAnalysisHistoryByResumeHistoryId(@PathVariable String resumeHistoryId){
        ResumeAnalysisHistory record = resumeService
                .getResumeAnalysisHistoryByResumeHistoryId(resumeHistoryId);
        return ApiResponse.success(record);
    }
    /**
     * 恢复简历内容。
     *
     * @param resumeHistoryId 简历历史 ID
     * @return 统一响应体
     */
    @Operation(summary = "恢复简历内容",
            description = "通过简历历史id恢复最近一次简历内容")
    @PostMapping("/restore/{resumeHistoryId}")
    public ApiResponse<ResumeContentResponse> restoreResume(@PathVariable String resumeHistoryId){
        try {
            String content = resumeService.rollBackResumeContent(resumeHistoryId);
            ResumeContentResponse response = ResumeContentResponse.builder()
                    .resumeId(resumeHistoryId)
                    .content(content)
                    .build();
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(ResultCode.SYSTEM_ERROR.getCode(), "恢复简历内容失败: " + e.getMessage());
        }
    }

    // ==================== Agent Tools（供 ReactAgent 调用） ====================
    @Tool(
            name = "resume_get_history",
            description = "按用户ID与评估类型获取简历评测历史记录列表，用于生成综合报告或追溯历史结果"
    )
    public List<ResumeAnalysisHistory> toolGetResumeAnalysisHistory(
            @ToolParam(description = "用户ID") Long userId,
            @ToolParam(description = "评估类型（SPECIAL/COMPREHENSIVE 等，默认 SPECIAL）") String evaluationType
    ) {
        String type = (evaluationType == null || evaluationType.isBlank()) ? "SPECIAL" : evaluationType;
        return resumeService.getResumeAnalysisHistoryByUserIdAndEvaluationType(userId, type);
    }

    @Tool(
            name = "resume_get_history_by_id",
            description = "根据简历历史ID获取单条简历评测历史记录"
    )
    public ResumeAnalysisHistory toolGetResumeAnalysisHistoryById(
            @ToolParam(description = "简历历史ID") String resumeHistoryId
    ) {
        return resumeService.getResumeAnalysisHistoryByResumeHistoryId(resumeHistoryId);
    }

    @Tool(
            name = "resume_restore_content",
            description = "根据简历历史ID恢复简历文本内容（用于后续出题或复盘评测）"
    )
    public String toolRestoreResumeContent(
            @ToolParam(description = "简历历史ID") String resumeHistoryId
    ) {
        return resumeService.rollBackResumeContent(resumeHistoryId);
    }
} 

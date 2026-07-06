package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.common.result.ResultCode;
import com.multimodal.interview.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
} 

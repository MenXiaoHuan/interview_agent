package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.service.FacialAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
/**
 * 面部表情分析接口控制器。
 */
@Tag(name = "面部表情分析", description = "面部表情分析相关接口")
@RestController
@RequestMapping("/api/face")
public class FacialAnalysisController {
    @Autowired
    FacialAnalysisService facialAnalysisService;
    /**
     * 获取面部表情分析。
     *
     * @param file 上传文件
     * @return 统一响应体
     * @throws IOException 接口处理异常
     */
    @Operation(summary = "获取面部表情分析", description = "获取面部表情分析相关业务（调用讯飞的api实现的）")
    @PostMapping("/getAnalysis")
    public ApiResponse<String> getFacialAnalysis(@RequestParam("file") MultipartFile file) throws IOException{

        String facialAnalysis = facialAnalysisService.getFacialAnalysis(file);

        if (facialAnalysis == null) return ApiResponse.badRequest("获取失败");

        return ApiResponse.success("获取成功", facialAnalysis);
    }
}

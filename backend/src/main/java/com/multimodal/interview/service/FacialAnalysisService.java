package com.multimodal.interview.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 面部表情分析服务接口。
 */
public interface FacialAnalysisService {
    /**
     * 获取人脸分析结果
     */
    String getFacialAnalysis(MultipartFile file) throws IOException;
}

package com.multimodal.interview.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 简历分析服务接口。
 */
public interface ResumeService {
    /**
     * 提取简历内容
     */
    String extractResumeContent(MultipartFile file) throws IOException;
}

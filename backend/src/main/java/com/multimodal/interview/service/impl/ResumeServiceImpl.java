package com.multimodal.interview.service.impl;

import com.multimodal.interview.service.ResumeService;
import com.multimodal.interview.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 简历分析服务实现。
 */
@Slf4j
@Service
public class ResumeServiceImpl implements ResumeService {
    /**
     * {@inheritDoc}
     */
    @Override
    public String extractResumeContent(MultipartFile file) throws IOException {
        return FileUtil.extractText(file);
    }
} 

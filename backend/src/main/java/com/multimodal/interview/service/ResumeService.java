package com.multimodal.interview.service;

import com.multimodal.interview.dto.SaveResumeHistoryRequest;
import com.multimodal.interview.entity.ResumeAnalysisHistory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 简历分析服务接口。
 */
public interface ResumeService {
    /**
     * 提取简历内容
     */
    String extractResumeContent(MultipartFile file) throws IOException;

    /**
     * 保存简历分析历史
     */
    String saveResumeHistory(SaveResumeHistoryRequest resumeHistory, String evaluationType);

    /**
     * 获取简历分析历史
     */
    List<ResumeAnalysisHistory> getResumeAnalysisHistoryByUserIdAndEvaluationType(Long userId, String evaluationType);

    /**
     * 获取简历分析历史详情
     */
    ResumeAnalysisHistory getResumeAnalysisHistoryByResumeHistoryId(String resumeHistoryId);

    /**
     * 恢复简历内容
     */
    String rollBackResumeContent(String resumeHistoryId);
}

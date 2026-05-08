package com.multimodal.interview.service;

import com.multimodal.interview.dto.*;
import com.multimodal.interview.entity.ComprehensiveQuestionHistory;
import com.multimodal.interview.entity.ComprehensiveReportHistory;
import com.multimodal.interview.entity.ComprehensiveResumeHistory;
import com.multimodal.interview.entity.ComprehensiveScenarioHistory;

import java.util.List;

/**
 * 综合评估历史服务接口。
 */
public interface ComprehensiveHistoryService {

    /**
     * 保存简历综合评估历史。
     */
    ComprehensiveResumeHistory saveResumeHistory(ComprehensiveResumeHistoryRequest request);

    /**
     * 保存问题综合评估历史。
     */
    ComprehensiveQuestionHistory saveQuestionHistory(ComprehensiveQuestionHistoryRequest request);

    /**
     * 保存场景综合评估历史。
     */
    ComprehensiveScenarioHistory saveScenarioHistory(ComprehensiveScenarioHistoryRequest request);

    /**
     * 保存综合评估报告。
     */
    ComprehensiveReportHistory saveComprehensiveReport(ComprehensiveReportRequest request);

    /**
     * 根据用户 ID 获取所有综合评估报告。
     */
    List<ComprehensiveReportResponse> getComprehensiveReportsByUserId(Long userId);

    /**
     * 根据 ID 获取综合评估报告详情。
     */
    ComprehensiveReportResponse getComprehensiveReportById(Long id);
}

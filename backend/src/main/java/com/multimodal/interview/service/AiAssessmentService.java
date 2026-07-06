package com.multimodal.interview.service;

import com.multimodal.interview.dto.*;
import com.multimodal.interview.entity.AiQuestionHistory;
import com.multimodal.interview.entity.AiResumeHistory;
import com.multimodal.interview.entity.AiInterviewRoundHistory;

/**
 * AIview 测评流程归档服务接口。
 */
public interface AiAssessmentService {

    /**
     * 保存简历综合评估历史。
     */
    AiResumeHistory saveResumeHistory(AiResumeHistoryRequest request);

    /**
     * 保存问题综合评估历史。
     */
    AiQuestionHistory saveQuestionHistory(AiQuestionHistoryRequest request);

    /**
     * 保存场景综合评估历史。
     */
    AiInterviewRoundHistory saveScenarioHistory(AiInterviewRoundHistoryRequest request);

}

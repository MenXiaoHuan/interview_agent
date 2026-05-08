package com.multimodal.interview.service;

import com.multimodal.interview.dto.InterviewResultResponse;
import com.multimodal.interview.dto.InterviewSubmitRequest;
import com.multimodal.interview.dto.InterviewSubmitResponse;
import com.multimodal.interview.entity.choiceQuestion;
import com.multimodal.interview.entity.choiceQuestionRecord;

import java.util.List;

/**
 * 试题作答服务接口。
 */
public interface ChoiceQuestionService {
    /**
     * 获取所有选择试题
     */
    List<choiceQuestion> getAllQuestions();
    /**
     * 根据岗位ID获取选择试题
     */
    List<choiceQuestion> getQuestions(Long jobId);

    /**
     * 提交用户作答试题
     */
    InterviewSubmitResponse submitAnswers(Long userId, InterviewSubmitRequest request);

    /**
     * 获取选择试题作答结果
     */
    InterviewResultResponse getResult(Long interviewId);

    /**
     * 通过userId获取选择试题记录
     */
    List<choiceQuestionRecord> getRecordByUserId(Long userId, String evaluationType);

    /**
     * 通过userId和jobId获取选择试题记录
     */
    List<choiceQuestionRecord> getRecordByUserIdAndJob(Long userId, Long jobId, String evaluationType);

    /**
     * 增加选择题目
     */
    void addQuestion(choiceQuestion question);

    /**
     * 修改选择题目
     */
    void updateQuestion(choiceQuestion question);

    /**
     * 删除选择题目
     */
    void deleteQuestion(Long id);
}

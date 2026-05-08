package com.multimodal.interview.service;

import com.multimodal.interview.dto.FeedbackReplyRequest;
import com.multimodal.interview.dto.FeedbackSubmitRequest;
import com.multimodal.interview.entity.Feedback;
import com.multimodal.interview.dto.FeedbackItemResponse;
import com.multimodal.interview.dto.FeedbackStatusResponse;

import java.util.List;

/**
 * 用户反馈服务接口。
 */
public interface FeedbackService {
    /**
     * 提交反馈
     */
    Feedback submitFeedback(Long userId, FeedbackSubmitRequest request);

    /**
     * 获取反馈详情
     */
    Feedback getFeedbackDetail(Long id);

    /**
     * 获取反馈列表
     */
    List<Feedback> getFeedbackList(Long userId, int page, int size);

    /**
     * 根据条件获取反馈列表
     */
    List<Feedback> getFeedbackListByCondition(String type, String status, int page, int size);

    /**
     * 回复反馈
     */
    Feedback replyFeedback(Long adminId, String adminName, FeedbackReplyRequest request);

    /**
     * 统计用户反馈数量
     */
    int countByUserId(Long userId);

    /**
     * 统计反馈数量
     */
    int countByCondition(String type, String status);

    List<FeedbackItemResponse> getAdminFeedbackExcludingSelf(Long adminId, int page, int size);

    List<FeedbackItemResponse> getAdminFeedbackExcludingSelfAll(Long adminId);

    List<FeedbackItemResponse> getUserFeedbackWithNickname(Long userId, int page, int size);

    List<FeedbackStatusResponse> getMyFeedbackStatus(Long userId);

    List<FeedbackItemResponse> getUserFeedbackWithNicknameAll(Long userId);
}

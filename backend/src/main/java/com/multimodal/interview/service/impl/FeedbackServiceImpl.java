package com.multimodal.interview.service.impl;

import com.multimodal.interview.dto.FeedbackReplyRequest;
import com.multimodal.interview.dto.FeedbackSubmitRequest;
import com.multimodal.interview.dto.FeedbackItemResponse;
import com.multimodal.interview.dto.FeedbackStatusResponse;
import com.multimodal.interview.entity.Feedback;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.mapper.FeedbackMapper;
import com.multimodal.interview.service.FeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户反馈服务实现。
 */
@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    public FeedbackServiceImpl(FeedbackMapper feedbackMapper) {
        this.feedbackMapper = feedbackMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Feedback submitFeedback(Long userId, FeedbackSubmitRequest request) {
        // 检查24小时内提交数量
        int count = feedbackMapper.countByUserId(userId);
        if (count >= 5) {
            throw BusinessException.badRequest("24小时内最多提交5条反馈");
        }

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setType(request.getType());
        feedback.setTitle(request.getTitle());
        feedback.setContent(request.getContent());
        feedback.setStatus("PENDING");

        int rows = feedbackMapper.insert(feedback);
        if (rows != 1) {
            throw BusinessException.internalError("提交反馈失败");
        }

        return feedback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Feedback getFeedbackDetail(Long id) {
        Feedback feedback = feedbackMapper.findById(id);
        if (feedback == null) {
            throw BusinessException.notFound("反馈不存在");
        }
        return feedback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Feedback> getFeedbackList(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return feedbackMapper.findByUserId(userId, offset, size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Feedback> getFeedbackListByCondition(String type, String status, int page, int size) {
        int offset = (page - 1) * size;
        return feedbackMapper.findByCondition(type, status, offset, size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Feedback replyFeedback(Long adminId, String adminName, FeedbackReplyRequest request) {
        Feedback feedback = feedbackMapper.findById(request.getId());
        if (feedback == null) {
            throw BusinessException.notFound("反馈不存在");
        }

        feedback.setReply(request.getReply());
        feedback.setStatus(request.getStatus());
        feedback.setAdminId(adminId);
        feedback.setAdminName(adminName);

        int rows = feedbackMapper.updateReply(feedback);
        if (rows != 1) {
            throw BusinessException.internalError("回复反馈失败");
        }

        return feedback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByUserId(Long userId) {
        return feedbackMapper.countByUserId(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByCondition(String type, String status) {
        return feedbackMapper.countByCondition(type, status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FeedbackItemResponse> getAdminFeedbackExcludingSelf(Long adminId, int page, int size) {
        int offset = (page - 1) * size;
        return feedbackMapper.findAllExcludingUser(adminId, offset, size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FeedbackItemResponse> getAdminFeedbackExcludingSelfAll(Long adminId) {
        return feedbackMapper.findAllExcludingUserAll(adminId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FeedbackItemResponse> getUserFeedbackWithNickname(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return feedbackMapper.findWithUserByUserId(userId, offset, size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FeedbackStatusResponse> getMyFeedbackStatus(Long userId) {
        return feedbackMapper.findStatusByUserId(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FeedbackItemResponse> getUserFeedbackWithNicknameAll(Long userId) {
        return feedbackMapper.findWithUserByUserIdAll(userId);
    }
} 

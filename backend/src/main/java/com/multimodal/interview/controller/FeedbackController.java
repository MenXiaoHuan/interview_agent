package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.dto.FeedbackReplyRequest;
import com.multimodal.interview.dto.FeedbackSubmitRequest;
import com.multimodal.interview.dto.FeedbackItemResponse;
import com.multimodal.interview.dto.FeedbackStatusResponse;
import com.multimodal.interview.entity.Feedback;
import com.multimodal.interview.entity.User;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.mapper.UserMapper;
import com.multimodal.interview.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 用户反馈接口控制器。
 */
@Tag(name = "用户反馈", description = "用户反馈相关接口")
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserMapper userMapper;

    public FeedbackController(FeedbackService feedbackService, UserMapper userMapper) {
        this.feedbackService = feedbackService;
        this.userMapper = userMapper;
    }
    /**
     * 提交反馈。
     *
     * @param request 请求体参数
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "提交反馈")
    @PostMapping("/submit")
    public ApiResponse<Feedback> submitFeedback(@Valid @RequestBody FeedbackSubmitRequest request, Authentication authentication){
        User user = userMapper.findByUsername(authentication.getName());
        Feedback feedback = feedbackService.submitFeedback(user.getId(), request);
        return ApiResponse.success("反馈提交成功", feedback);
    }
    /**
     * 获取反馈列表（管理员查看除自己以外的所有用户；普通用户查看自己的）。
     *
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "获取反馈列表（管理员查看除自己以外的所有用户；普通用户查看自己的）")
    @GetMapping
    public ApiResponse<List<FeedbackItemResponse>> getFeedbackList(Authentication authentication){

        User user = userMapper.findByUsername(authentication.getName());
        if (user.getUserType() != null && user.getUserType() == 3) {
            List<FeedbackItemResponse> list = feedbackService.getAdminFeedbackExcludingSelfAll(user.getId());
            return ApiResponse.success("获取反馈列表成功", list);
        } else {
            List<FeedbackItemResponse> list = feedbackService.getUserFeedbackWithNicknameAll(user.getId());
            return ApiResponse.success("获取反馈列表成功", list);
        }
    }
    /**
     * 获取反馈详情。
     *
     * @param id 主键 ID
     * @return 统一响应体
     */
    @Operation(summary = "获取反馈详情")
    @GetMapping("/{id}")
    public ApiResponse<Feedback> getFeedbackDetail(@PathVariable Long id){
        Feedback feedback = feedbackService.getFeedbackDetail(id);
        return ApiResponse.success("获取反馈详情成功", feedback);
    }
    /**
     * 回复反馈。
     *
     * @param id 主键 ID
     * @param request 请求体参数
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "回复反馈")
    @PostMapping("/{id}/reply")
    public ApiResponse<Feedback> replyFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackReplyRequest request,
            Authentication authentication){
        User admin = userMapper.findByUsername(authentication.getName());
        if (admin.getUserType() != 3) {
            throw BusinessException.forbidden("无权限操作");
        }

        request.setId(id);
        Feedback feedback = feedbackService.replyFeedback(admin.getId(), admin.getUsername(), request);
        return ApiResponse.success("回复成功", feedback);
    }
    /**
     * 查看本人反馈处理状态。
     *
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "查看本人反馈处理状态")
    @GetMapping("/my/status")
    public ApiResponse<List<FeedbackStatusResponse>> getMyFeedbackStatus(Authentication authentication){
        User user = userMapper.findByUsername(authentication.getName());
        List<FeedbackStatusResponse> list = feedbackService.getMyFeedbackStatus(user.getId());
        return ApiResponse.success("获取处理状态成功", list);
    }
}

package com.multimodal.interview.service;

import com.multimodal.interview.dto.ForgotResetRequest;
import com.multimodal.interview.dto.ForgotSendCodeRequest;

/**
 * 忘记密码服务接口。
 */
public interface ForgotPasswordService {
    void sendCode(ForgotSendCodeRequest request);
    void reset(ForgotResetRequest request);
    com.multimodal.interview.entity.PasswordResetToken getLatestValidByContact(String contact);
}

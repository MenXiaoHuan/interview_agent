package com.multimodal.interview.service;

import com.multimodal.interview.dto.LoginRequest;
import com.multimodal.interview.dto.RegisterRequest;
import com.multimodal.interview.entity.User;

/**
 * 用户认证服务接口。
 */
public interface AuthService {
    /**
     * 用户注册
     */
    User register(RegisterRequest request);

    /**
     * 用户登录
     */
    User login(LoginRequest request);

    /**
     * 生成令牌
     */
    String generateToken(User user);
}

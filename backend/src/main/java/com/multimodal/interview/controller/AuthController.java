package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.dto.LoginRequest;
import com.multimodal.interview.dto.RegisterRequest;
import com.multimodal.interview.entity.User;
import com.multimodal.interview.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
/**
 * 用户认证接口控制器。
 */
@Tag(name = "用户认证", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    /**
     * 用户注册。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "用户注册",
            description = "用户注册相关业务")
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request){
        User user = authService.register(request);
        String token = authService.generateToken(user);

        Map<String, Object> userInfo = createUserInfoMap(user);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", userInfo);

        return ApiResponse.success("注册成功", data);
    }
    /**
     * 用户登录。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "用户登录",
            description = "用户登录相关业务")
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request){
        User user = authService.login(request);
        String token = authService.generateToken(user);

        Map<String, Object> userInfo = createUserInfoMap(user);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", userInfo);

        return ApiResponse.success("登录成功", data);
    }
    /**
     * 组装用户信息返回体。
     *
     * @param user 用户实体
     * @return 用户信息映射
     */
    private Map<String, Object> createUserInfoMap(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatarUrl());
        userInfo.put("userType", user.getUserType());
        userInfo.put("eyeCareMode", user.getEyeCareMode());
        userInfo.put("surpriseMode", user.getSurpriseMode());
        return userInfo;
    }
} 

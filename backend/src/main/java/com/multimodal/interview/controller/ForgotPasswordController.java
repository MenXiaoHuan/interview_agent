package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.dto.ForgotResetRequest;
import com.multimodal.interview.dto.ForgotSendCodeRequest;
import com.multimodal.interview.dto.ResetCodeResponse;
import com.multimodal.interview.entity.PasswordResetToken;
import com.multimodal.interview.service.ForgotPasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
/**
 * 忘记密码接口控制器。
 */
@Tag(name = "忘记密码", description = "通过邮箱或手机号找回密码")
@RestController
@RequestMapping("/api/auth/forgot")
public class ForgotPasswordController {
    private final ForgotPasswordService forgotPasswordService;

    public ForgotPasswordController(ForgotPasswordService forgotPasswordService) {
        this.forgotPasswordService = forgotPasswordService;
    }
    /**
     * 发送重置验证码。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "发送重置验证码")
    @PostMapping("/send-code")
    public ApiResponse<Void> sendCode(@Valid @RequestBody ForgotSendCodeRequest request){
        forgotPasswordService.sendCode(request);
        return ApiResponse.success("验证码已发送", null);
    }
    /**
     * 校验验证码并重置密码。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "校验验证码并重置密码")
    @PostMapping("/reset")
    public ApiResponse<Void> reset(@Valid @RequestBody ForgotResetRequest request){
        forgotPasswordService.reset(request);
        return ApiResponse.success("密码重置成功", null);
    }
    /**
     * 查询最新有效验证码（通用contact）。
     *
     * @param contact 联系方式
     * @return 统一响应体
     */
    @Operation(summary = "查询最新有效验证码（通用contact）")
    @GetMapping("/code")
    public ApiResponse<ResetCodeResponse> getCodeByContact(@RequestParam String contact){
        PasswordResetToken token = forgotPasswordService.getLatestValidByContact(contact);
        if (token == null) {
            return ApiResponse.success("无有效验证码", null);
        }
        ResetCodeResponse resp = new ResetCodeResponse();
        resp.setContact(token.getContact());
        resp.setChannel(token.getChannel());
        resp.setCode(token.getCode());
        resp.setCreatedAt(token.getCreatedAt());
        resp.setExpiresAt(token.getExpiresAt());
        return ApiResponse.success(resp);
    }
    /**
     * 查询最新有效验证码（邮箱）。
     *
     * @param email 邮箱地址
     * @return 统一响应体
     */
    @Operation(summary = "查询最新有效验证码（邮箱）")
    @GetMapping("/email-code")
    public ApiResponse<ResetCodeResponse> getCodeByEmail(@RequestParam String email){
        return getCodeByContact(email);
    }
    /**
     * 查询最新有效验证码（手机号）。
     *
     * @param phone 手机号
     * @return 统一响应体
     */
    @Operation(summary = "查询最新有效验证码（手机号）")
    @GetMapping("/phone-code")
    public ApiResponse<ResetCodeResponse> getCodeByPhone(@RequestParam String phone){
        return getCodeByContact(phone);
    }
}

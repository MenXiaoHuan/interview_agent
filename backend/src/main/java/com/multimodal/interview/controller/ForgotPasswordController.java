package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.common.security.RsaPasswordCryptoService;
import com.multimodal.interview.dto.ForgotResetRequest;
import com.multimodal.interview.dto.ForgotSendCodeRequest;
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
    private final RsaPasswordCryptoService rsaPasswordCryptoService;

    public ForgotPasswordController(ForgotPasswordService forgotPasswordService,
                                    RsaPasswordCryptoService rsaPasswordCryptoService) {
        this.forgotPasswordService = forgotPasswordService;
        this.rsaPasswordCryptoService = rsaPasswordCryptoService;
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
        rsaPasswordCryptoService.decrypt(request);
        forgotPasswordService.reset(request);
        return ApiResponse.success("密码重置成功", null);
    }
}

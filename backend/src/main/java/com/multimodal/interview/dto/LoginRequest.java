package com.multimodal.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * LoginRequest 数据传输对象。
 */
@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}

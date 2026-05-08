package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 密码重置验证码实体。
 */
@Data
public class PasswordResetToken {
    private Long id;
    private Long userId;
    private String channel;
    private String contact;
    private String code;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
}

package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体。
 */
@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatarUrl;
    private Integer userType;
    private String email;
    private String phone;
    private Integer gender;
    private Integer status;
    private Integer eyeCareMode;
    private Integer surpriseMode;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

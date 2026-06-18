package com.multimodal.interview.dto;

import com.multimodal.interview.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String nickname,
        String avatarUrl,
        Integer userType,
        String email,
        String phone,
        Integer gender,
        Integer status,
        Integer eyeCareMode,
        Integer surpriseMode,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getUserType(),
                user.getEmail(),
                user.getPhone(),
                user.getGender(),
                user.getStatus(),
                user.getEyeCareMode(),
                user.getSurpriseMode(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

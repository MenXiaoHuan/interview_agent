package com.multimodal.interview.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * ChatMessageResponse 数据传输对象。
 */
@Data
public class ChatMessageResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private Long userId;
    private String nickname;
    private String avatarUrl;
}

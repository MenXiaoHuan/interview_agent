package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息实体。
 */
@Data
public class ChatMessage {
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}

package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 会话消息实体。
 */
@Data
public class AgentConversationMessageEntity {
    private Long id;
    private String sessionId;
    private Integer turnNo;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}

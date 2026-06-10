package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 会话主表实体。
 */
@Data
public class AgentConversationSessionEntity {
    private Long id;
    private Long userId;
    private String agentKey;
    private String chatId;
    private String title;
    private String preview;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

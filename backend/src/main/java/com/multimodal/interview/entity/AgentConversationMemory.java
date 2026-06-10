package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 会话记忆实体。
 */
@Data
public class AgentConversationMemory {
    private Long id;
    private String chatId;
    private String summaryHash;
    private String summaryContent;
    private String recentTurnsJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

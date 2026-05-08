package com.multimodal.interview.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 会话记忆响应。
 */
@Data
public class AgentConversationMemoryResponse {
    private String sessionId;
    private String summaryHash;
    private String summaryContent;
    private Object recentTurns;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

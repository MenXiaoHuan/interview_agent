package com.multimodal.interview.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 会话事件实体。
 */
@Data
public class AgentConversationEventEntity {
    private Long id;
    private String chatId;
    private Integer turnNo;
    private String eventType;
    private String payloadJson;
    private LocalDateTime createdAt;
}

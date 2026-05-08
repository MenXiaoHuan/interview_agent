package com.multimodal.interview.dto;

import lombok.Data;

/**
 * Agent 会话消息响应。
 */
@Data
public class AgentConversationMessageResponse {
    private String type;
    private String content;
    private String time;
}

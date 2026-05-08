package com.multimodal.interview.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Agent 会话响应。
 */
@Data
public class AgentConversationResponse {
    private String id;
    private Long jobId;
    private String agentKey;
    private String title;
    private String preview;
    private Boolean hasInteractedWithAssistant;
    private Boolean hasShownWelcomeMessage;
    private String selectedEvaluationMode;
    private List<Map<String, Object>> assistantActions;
    private List<Map<String, Object>> eventLogs;
    private List<AgentConversationMessageResponse> aiMessages;
    private Map<String, Object> comprehensiveState;
    private LocalDateTime firstQuestionAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

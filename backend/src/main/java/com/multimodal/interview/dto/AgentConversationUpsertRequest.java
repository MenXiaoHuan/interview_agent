package com.multimodal.interview.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Agent 会话保存请求。
 */
@Data
public class AgentConversationUpsertRequest {
    private String sessionId;
    private String agentKey;
    private Long jobId;
    private String title;
    private String selectedEvaluationMode;
    private Boolean hasInteractedWithAssistant;
    private Boolean hasShownWelcomeMessage;
    private String firstQuestionAt;
    private List<Map<String, Object>> assistantActions;
    private List<Map<String, Object>> eventLogs;
    private List<AgentConversationMessageResponse> aiMessages;
    private Map<String, Object> comprehensiveState;
}

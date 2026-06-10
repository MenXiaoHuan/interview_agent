package com.multimodal.interview.service;

import com.multimodal.interview.dto.AgentConversationMemoryResponse;
import com.multimodal.interview.dto.AgentConversationMessageResponse;
import com.multimodal.interview.dto.AgentConversationResponse;
import com.multimodal.interview.dto.AgentConversationUpsertRequest;

import java.util.List;
import java.util.Map;

/**
 * Agent 会话聚合服务。
 */
public interface AgentConversationService {
    List<AgentConversationResponse> listUserConversations(Long userId, String agentKey);

    AgentConversationResponse upsertUserConversation(Long userId, AgentConversationUpsertRequest request);

    void deleteUserConversation(Long userId, String chatId);

    List<AgentConversationMessageResponse> listUserConversationMessages(Long userId, String chatId);

    List<Map<String, Object>> listUserConversationEvents(Long userId, String chatId);

    AgentConversationMemoryResponse getUserConversationMemory(Long userId, String chatId);
}

package com.multimodal.interview.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.dto.AgentConversationMemoryResponse;
import com.multimodal.interview.dto.AgentConversationMessageResponse;
import com.multimodal.interview.dto.AgentConversationResponse;
import com.multimodal.interview.dto.AgentConversationUpsertRequest;
import com.multimodal.interview.entity.AgentConversationEventEntity;
import com.multimodal.interview.entity.AgentConversationMemory;
import com.multimodal.interview.entity.AgentConversationMessageEntity;
import com.multimodal.interview.entity.AgentConversationSessionEntity;
import com.multimodal.interview.mapper.AgentConversationEventMapper;
import com.multimodal.interview.mapper.AgentConversationMemoryMapper;
import com.multimodal.interview.mapper.AgentConversationMessageMapper;
import com.multimodal.interview.mapper.AgentConversationSessionMapper;
import com.multimodal.interview.service.AgentConversationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Agent 会话聚合服务实现。
 */
@Service
public class AgentConversationServiceImpl implements AgentConversationService {
    private static final String DEFAULT_AGENT_KEY = "interview-assistant";
    private static final String STATE_SNAPSHOT_EVENT_TYPE = "session_state_snapshot";
    private static final String DEFAULT_PREVIEW = "点击开始对话";
    private final AgentConversationSessionMapper sessionMapper;
    private final AgentConversationMessageMapper messageMapper;
    private final AgentConversationEventMapper eventMapper;
    private final AgentConversationMemoryMapper memoryMapper;
    private final ObjectMapper objectMapper;

    public AgentConversationServiceImpl(
            AgentConversationSessionMapper sessionMapper,
            AgentConversationMessageMapper messageMapper,
            AgentConversationEventMapper eventMapper,
            AgentConversationMemoryMapper memoryMapper,
            ObjectMapper objectMapper
    ) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.eventMapper = eventMapper;
        this.memoryMapper = memoryMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<AgentConversationResponse> listUserConversations(Long userId, String agentKey) {
        String safeAgentKey = normalizeAgentKey(agentKey);
        List<AgentConversationSessionEntity> sessions = sessionMapper.findByUserAndAgentKey(userId, safeAgentKey);
        List<AgentConversationResponse> result = new ArrayList<>();
        for (AgentConversationSessionEntity session : sessions) {
            result.add(toResponse(session));
        }
        return result;
    }

    @Override
    @Transactional
    public AgentConversationResponse upsertUserConversation(Long userId, AgentConversationUpsertRequest request) {
        if (request == null) {
            throw BusinessException.badRequest("会话请求不能为空");
        }
        String conversationId = resolveIncomingConversationId(request);

        String safeAgentKey = normalizeAgentKey(request.getAgentKey());
        List<AgentConversationMessageResponse> messages = normalizeMessages(request.getAiMessages());
        List<Map<String, Object>> eventLogs = normalizeEventLogs(request.getEventLogs());

        AgentConversationSessionEntity existing = conversationId.isBlank()
                ? null
                : sessionMapper.findByUserAndConversationId(userId, conversationId);
        AgentConversationSessionEntity target = existing == null ? new AgentConversationSessionEntity() : existing;
        target.setUserId(userId);
        target.setAgentKey(safeAgentKey);
        if (existing == null) {
            target.setChatId(generateChatId());
        }
        target.setTitle(resolveSessionTitle(request, messages, existing));
        target.setPreview(buildPreview(messages));

        if (existing == null) {
            sessionMapper.insert(target);
        } else {
            sessionMapper.updateById(target);
        }

        syncMessages(target.getChatId(), messages);
        syncVisibleEvents(target.getChatId(), eventLogs);
        syncStateSnapshotEvent(target.getChatId(), request);
        sessionMapper.touchById(target.getId());
        return toResponse(target);
    }

    @Override
    @Transactional
    public void deleteUserConversation(Long userId, String chatId) {
        AgentConversationSessionEntity existing = findOwnedSession(userId, chatId);
        if (existing == null) {
            return;
        }
        messageMapper.deleteByChatId(existing.getChatId());
        eventMapper.deleteByChatId(existing.getChatId());
        memoryMapper.deleteByChatId(existing.getChatId());
        sessionMapper.deleteByUserAndConversationId(userId, existing.getChatId());
    }

    @Override
    public List<AgentConversationMessageResponse> listUserConversationMessages(Long userId, String chatId) {
        AgentConversationSessionEntity session = findOwnedSession(userId, chatId);
        if (session == null) {
            return List.of();
        }
        return toMessageResponses(messageMapper.findByChatId(session.getChatId()));
    }

    @Override
    public List<Map<String, Object>> listUserConversationEvents(Long userId, String chatId) {
        AgentConversationSessionEntity session = findOwnedSession(userId, chatId);
        if (session == null) {
            return List.of();
        }
        return readVisibleEvents(session.getChatId());
    }

    @Override
    public AgentConversationMemoryResponse getUserConversationMemory(Long userId, String chatId) {
        AgentConversationSessionEntity session = findOwnedSession(userId, chatId);
        if (session == null) {
            return null;
        }
        return toMemoryResponse(memoryMapper.findByChatId(session.getChatId()));
    }

    private AgentConversationResponse toResponse(AgentConversationSessionEntity session) {
        List<AgentConversationMessageEntity> messageEntities = messageMapper.findByChatId(session.getChatId());
        List<AgentConversationMessageResponse> messages = toMessageResponses(messageEntities);
        List<Map<String, Object>> visibleEvents = readVisibleEvents(session.getChatId());
        Map<String, Object> snapshot = readStateSnapshot(session.getChatId());

        AgentConversationResponse response = new AgentConversationResponse();
        response.setId(session.getChatId());
        response.setChatId(session.getChatId());
        response.setJobId(asLong(snapshot.get("jobId")));
        response.setAgentKey(session.getAgentKey());
        response.setTitle(safeText(session.getTitle()));
        response.setPreview(safeText(session.getPreview()).isBlank() ? buildPreview(messages) : safeText(session.getPreview()));
        response.setHasInteractedWithAssistant(readBoolean(snapshot.get("hasInteractedWithAssistant"), hasStartedConversation(messages)));
        response.setHasShownWelcomeMessage(readBoolean(snapshot.get("hasShownWelcomeMessage"), false));
        response.setSelectedEvaluationMode(safeText(valueOf(snapshot.get("selectedEvaluationMode"))));
        response.setAssistantActions(readListOfMap(snapshot.get("assistantActions")));
        response.setEventLogs(visibleEvents);
        response.setAiMessages(messages);
        response.setComprehensiveState(readMap(snapshot.get("comprehensiveState")));
        response.setFirstQuestionAt(resolveFirstQuestionAt(snapshot, messageEntities));
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
        return response;
    }

    private AgentConversationMemoryResponse toMemoryResponse(AgentConversationMemory memory) {
        if (memory == null) {
            return null;
        }
        AgentConversationMemoryResponse response = new AgentConversationMemoryResponse();
        response.setChatId(memory.getChatId());
        response.setSummaryHash(safeText(memory.getSummaryHash()));
        response.setSummaryContent(safeText(memory.getSummaryContent()));
        response.setRecentTurns(readJsonObject(memory.getRecentTurnsJson()));
        response.setCreatedAt(memory.getCreatedAt());
        response.setUpdatedAt(memory.getUpdatedAt());
        return response;
    }

    private void syncMessages(String chatId, List<AgentConversationMessageResponse> incomingMessages) {
        List<AgentConversationMessageResponse> normalizedIncoming = normalizeMessages(incomingMessages);
        List<AgentConversationMessageEntity> existingEntities = messageMapper.findByChatId(chatId);
        List<AgentConversationMessageResponse> existingMessages = toMessageResponses(existingEntities);
        List<Integer> turnNumbers = assignTurnNumbers(normalizedIncoming);
        if (shouldReplaceMessages(existingMessages, normalizedIncoming)) {
            messageMapper.deleteByChatId(chatId);
            insertMessages(chatId, normalizedIncoming, turnNumbers, 0);
            return;
        }
        insertMessages(chatId, normalizedIncoming, turnNumbers, existingMessages.size());
    }

    private void insertMessages(
            String chatId,
            List<AgentConversationMessageResponse> messages,
            List<Integer> turnNumbers,
            int startIndex
    ) {
        for (int index = startIndex; index < messages.size(); index += 1) {
            AgentConversationMessageResponse item = messages.get(index);
            AgentConversationMessageEntity entity = new AgentConversationMessageEntity();
            entity.setChatId(chatId);
            entity.setTurnNo(turnNumbers.get(index));
            entity.setRole("user".equalsIgnoreCase(item.getType()) ? "user" : "assistant");
            entity.setContent(safeText(item.getContent()));
            entity.setCreatedAt(LocalDateTime.now());
            messageMapper.insert(entity);
        }
    }

    private boolean shouldReplaceMessages(
            List<AgentConversationMessageResponse> existingMessages,
            List<AgentConversationMessageResponse> incomingMessages
    ) {
        if (existingMessages.size() > incomingMessages.size()) {
            return true;
        }
        for (int index = 0; index < existingMessages.size(); index += 1) {
            AgentConversationMessageResponse existing = existingMessages.get(index);
            AgentConversationMessageResponse incoming = incomingMessages.get(index);
            if (!Objects.equals(normalizeMessageType(existing.getType()), normalizeMessageType(incoming.getType()))
                    || !Objects.equals(safeText(existing.getContent()), safeText(incoming.getContent()))) {
                return true;
            }
        }
        return false;
    }

    private void syncVisibleEvents(String chatId, List<Map<String, Object>> incomingEventLogs) {
        List<Map<String, Object>> normalizedIncoming = normalizeEventLogs(incomingEventLogs);
        List<Map<String, Object>> existingVisible = readVisibleEvents(chatId);
        if (shouldReplaceEvents(existingVisible, normalizedIncoming)) {
            eventMapper.deleteByChatId(chatId);
            insertVisibleEvents(chatId, normalizedIncoming, 0);
            return;
        }
        insertVisibleEvents(chatId, normalizedIncoming, existingVisible.size());
    }

    private void insertVisibleEvents(String chatId, List<Map<String, Object>> events, int startIndex) {
        for (int index = startIndex; index < events.size(); index += 1) {
            Map<String, Object> item = events.get(index);
            AgentConversationEventEntity entity = new AgentConversationEventEntity();
            entity.setChatId(chatId);
            entity.setTurnNo(null);
            entity.setEventType(safeText(valueOf(item.get("type"))));
            entity.setPayloadJson(writeJson(item, "{}"));
            entity.setCreatedAt(parseDateTime(valueOf(item.get("time"))));
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(LocalDateTime.now());
            }
            eventMapper.insert(entity);
        }
    }

    private boolean shouldReplaceEvents(
            List<Map<String, Object>> existingEvents,
            List<Map<String, Object>> incomingEvents
    ) {
        if (existingEvents.size() > incomingEvents.size()) {
            return true;
        }
        for (int index = 0; index < existingEvents.size(); index += 1) {
            if (!Objects.equals(toComparableEvent(existingEvents.get(index)), toComparableEvent(incomingEvents.get(index)))) {
                return true;
            }
        }
        return false;
    }

    private void syncStateSnapshotEvent(String chatId, AgentConversationUpsertRequest request) {
        eventMapper.deleteByChatIdAndType(chatId, STATE_SNAPSHOT_EVENT_TYPE);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("jobId", request.getJobId());
        payload.put("selectedEvaluationMode", safeText(request.getSelectedEvaluationMode()));
        payload.put("hasInteractedWithAssistant", Boolean.TRUE.equals(request.getHasInteractedWithAssistant()));
        payload.put("hasShownWelcomeMessage", Boolean.TRUE.equals(request.getHasShownWelcomeMessage()));
        payload.put("firstQuestionAt", safeText(request.getFirstQuestionAt()));
        payload.put("assistantActions", request.getAssistantActions() == null ? List.of() : request.getAssistantActions());
        payload.put("comprehensiveState", request.getComprehensiveState() == null ? Map.of() : request.getComprehensiveState());

        AgentConversationEventEntity snapshotEvent = new AgentConversationEventEntity();
        snapshotEvent.setChatId(chatId);
        snapshotEvent.setTurnNo(null);
        snapshotEvent.setEventType(STATE_SNAPSHOT_EVENT_TYPE);
        snapshotEvent.setPayloadJson(writeJson(payload, "{}"));
        snapshotEvent.setCreatedAt(LocalDateTime.now());
        eventMapper.insert(snapshotEvent);
    }

    private List<Map<String, Object>> readVisibleEvents(String chatId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (AgentConversationEventEntity item : eventMapper.findByChatId(chatId)) {
            if (STATE_SNAPSHOT_EVENT_TYPE.equals(item.getEventType())) {
                continue;
            }
            Map<String, Object> payload = readJsonMap(item.getPayloadJson());
            if (payload.isEmpty()) {
                payload = new LinkedHashMap<>();
            }
            payload.put("type", safeText(item.getEventType()));
            if (!payload.containsKey("time") || safeText(valueOf(payload.get("time"))).isBlank()) {
                payload.put("time", formatDateTime(item.getCreatedAt()));
            }
            result.add(normalizeEventLog(payload));
        }
        return result;
    }

    private Map<String, Object> readStateSnapshot(String chatId) {
        AgentConversationEventEntity snapshot = eventMapper.findLatestByChatIdAndType(chatId, STATE_SNAPSHOT_EVENT_TYPE);
        return readJsonMap(snapshot == null ? null : snapshot.getPayloadJson());
    }

    private List<AgentConversationMessageResponse> normalizeMessages(List<AgentConversationMessageResponse> messages) {
        if (messages == null) {
            return List.of();
        }
        List<AgentConversationMessageResponse> result = new ArrayList<>();
        for (AgentConversationMessageResponse item : messages) {
            if (item == null) {
                continue;
            }
            String type = normalizeMessageType(item.getType());
            String content = safeText(item.getContent());
            if (content.isBlank()) {
                continue;
            }
            AgentConversationMessageResponse response = new AgentConversationMessageResponse();
            response.setType(type);
            response.setContent(content);
            response.setTime(safeText(item.getTime()));
            result.add(response);
        }
        return result;
    }

    private List<AgentConversationMessageResponse> toMessageResponses(List<AgentConversationMessageEntity> entities) {
        List<AgentConversationMessageResponse> result = new ArrayList<>();
        for (AgentConversationMessageEntity entity : entities) {
            AgentConversationMessageResponse response = new AgentConversationMessageResponse();
            response.setType("user".equalsIgnoreCase(entity.getRole()) ? "user" : "ai");
            response.setContent(safeText(entity.getContent()));
            response.setTime(formatClock(entity.getCreatedAt()));
            result.add(response);
        }
        return result;
    }

    private String resolveSessionTitle(
            AgentConversationUpsertRequest request,
            List<AgentConversationMessageResponse> messages,
            AgentConversationSessionEntity existing
    ) {
        String explicitTitle = safeText(request.getTitle());
        if (!explicitTitle.isBlank()) {
            return explicitTitle;
        }
        if (existing != null && !safeText(existing.getTitle()).isBlank()) {
            return existing.getTitle();
        }
        for (AgentConversationMessageResponse item : messages) {
            if ("user".equalsIgnoreCase(item.getType()) && !safeText(item.getContent()).isBlank()) {
                String content = safeText(item.getContent());
                return content.length() > 24 ? content.substring(0, 24) + "..." : content;
            }
        }
        return "未命名会话";
    }

    private String buildPreview(List<AgentConversationMessageResponse> messages) {
        if (messages == null || messages.isEmpty()) {
            return DEFAULT_PREVIEW;
        }
        for (int index = messages.size() - 1; index >= 0; index -= 1) {
            String content = safeText(messages.get(index).getContent());
            if (!content.isBlank()) {
                return content.length() > 40 ? content.substring(0, 40) + "..." : content;
            }
        }
        return DEFAULT_PREVIEW;
    }

    private List<Integer> assignTurnNumbers(List<AgentConversationMessageResponse> messages) {
        List<Integer> result = new ArrayList<>();
        int currentTurn = 0;
        for (AgentConversationMessageResponse item : messages) {
            if ("user".equalsIgnoreCase(item.getType())) {
                currentTurn += 1;
            }
            if (currentTurn <= 0) {
                currentTurn = 1;
            }
            result.add(currentTurn);
        }
        return result;
    }

    private List<Map<String, Object>> normalizeEventLogs(List<Map<String, Object>> eventLogs) {
        if (eventLogs == null) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : eventLogs) {
            Map<String, Object> normalized = normalizeEventLog(item);
            if (!safeText(valueOf(normalized.get("type"))).isBlank()) {
                result.add(normalized);
            }
        }
        return result;
    }

    private Map<String, Object> normalizeEventLog(Map<String, Object> item) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("id", safeText(valueOf(item == null ? null : item.get("id"))));
        normalized.put("type", safeText(valueOf(item == null ? null : item.get("type"))));
        normalized.put("time", safeText(valueOf(item == null ? null : item.get("time"))));
        normalized.put("source", safeText(valueOf(item == null ? null : item.get("source"))));
        normalized.put("chatId", safeText(valueOf(item == null ? null : item.get("chatId"))));
        normalized.put("jobId", safeText(valueOf(item == null ? null : item.get("jobId"))));
        normalized.put("module", safeText(valueOf(item == null ? null : item.get("module"))));
        normalized.put("mode", safeText(valueOf(item == null ? null : item.get("mode"))));
        normalized.put("actionType", safeText(valueOf(item == null ? null : item.get("actionType"))));
        normalized.put("label", safeText(valueOf(item == null ? null : item.get("label"))));
        normalized.put("completionType", safeText(valueOf(item == null ? null : item.get("completionType"))));
        Object rawScore = item == null ? 0 : item.getOrDefault("score", 0);
        normalized.put("score", toDouble(rawScore));
        normalized.put("reason", safeText(valueOf(item == null ? null : item.get("reason"))));
        normalized.put("fromJobId", safeText(valueOf(item == null ? null : item.get("fromJobId"))));
        normalized.put("toJobId", safeText(valueOf(item == null ? null : item.get("toJobId"))));
        normalized.put("targetJobHint", safeText(valueOf(item == null ? null : item.get("targetJobHint"))));
        normalized.put("intent", safeText(valueOf(item == null ? null : item.get("intent"))));
        normalized.put("reply", safeText(valueOf(item == null ? null : item.get("reply"))));
        normalized.put("meta", item != null && item.get("meta") instanceof Map<?, ?> meta ? new LinkedHashMap<>(meta) : Map.of());
        if (safeText(valueOf(normalized.get("time"))).isBlank()) {
            normalized.put("time", formatDateTime(LocalDateTime.now()));
        }
        return normalized;
    }

    private String toComparableEvent(Map<String, Object> event) {
        return writeJson(normalizeEventLog(event), "{}");
    }

    private LocalDateTime resolveFirstQuestionAt(Map<String, Object> snapshot, List<AgentConversationMessageEntity> messages) {
        LocalDateTime snapshotValue = normalizeDateTime(snapshot.get("firstQuestionAt"));
        if (snapshotValue != null) {
            return snapshotValue;
        }
        for (AgentConversationMessageEntity item : messages) {
            if ("user".equalsIgnoreCase(item.getRole())) {
                return item.getCreatedAt();
            }
        }
        return null;
    }

    private LocalDateTime normalizeDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalDateTime();
        }
        return parseDateTime(String.valueOf(value));
    }

    private boolean hasStartedConversation(List<AgentConversationMessageResponse> messages) {
        for (AgentConversationMessageResponse item : messages) {
            if ("user".equalsIgnoreCase(item.getType()) && !safeText(item.getContent()).isBlank()) {
                return true;
            }
        }
        return false;
    }

    private List<Map<String, Object>> readListOfMap(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                result.add(new LinkedHashMap<>(castMap(map)));
            }
        }
        return result;
    }

    private Map<String, Object> readMap(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        return new LinkedHashMap<>(castMap(map));
    }

    private Map<String, Object> readJsonMap(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(rawJson, new TypeReference<LinkedHashMap<String, Object>>() {
            });
        } catch (Exception exception) {
            return new LinkedHashMap<>();
        }
    }

    private Object readJsonObject(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(rawJson, Object.class);
        } catch (Exception exception) {
            return List.of();
        }
    }

    private AgentConversationSessionEntity findOwnedSession(Long userId, String conversationId) {
        String safeConversationId = safeText(conversationId);
        if (safeConversationId.isBlank()) {
            return null;
        }
        return sessionMapper.findByUserAndConversationId(userId, safeConversationId);
    }

    private Map<String, Object> castMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }

    private Boolean readBoolean(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return "true".equalsIgnoreCase(String.valueOf(value).trim()) || "1".equals(String.valueOf(value).trim());
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (Exception exception) {
            return 0D;
        }
    }

    private String writeJson(Object value, String fallback) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            return fallback;
        }
    }

    private String normalizeAgentKey(String agentKey) {
        String safe = safeText(agentKey);
        return safe.isBlank() ? DEFAULT_AGENT_KEY : safe;
    }

    private String resolveIncomingConversationId(AgentConversationUpsertRequest request) {
        return safeText(request.getChatId());
    }

    private String generateChatId() {
        long timestamp = System.currentTimeMillis();
        int suffix = ThreadLocalRandom.current().nextInt(10000, 100000);
        return Long.toString(timestamp) + suffix;
    }

    private String normalizeMessageType(String type) {
        return "user".equalsIgnoreCase(type) ? "user" : "ai";
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.toString();
    }

    private String formatClock(LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.toLocalTime().withNano(0).toString();
    }

    private LocalDateTime parseDateTime(String value) {
        String safe = safeText(value);
        if (safe.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(safe);
        } catch (DateTimeParseException ignored) {
            try {
                return OffsetDateTime.parse(safe).toLocalDateTime();
            } catch (DateTimeParseException ignoredAgain) {
                return null;
            }
        }
    }
}

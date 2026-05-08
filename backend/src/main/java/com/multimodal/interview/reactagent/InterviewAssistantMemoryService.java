package com.multimodal.interview.reactagent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimodal.interview.entity.AgentConversationMemory;
import com.multimodal.interview.entity.AgentConversationMessageEntity;
import com.multimodal.interview.mapper.AgentConversationMemoryMapper;
import com.multimodal.interview.mapper.AgentConversationMessageMapper;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 面试助手会话记忆服务。
 *
 * <p>基于 message 表中的 turn_no 做轮次压缩，只给模型提供摘要和最近 3 轮原文。</p>
 */
@Service
public class InterviewAssistantMemoryService {

    private static final int MAX_RECENT_TURNS = 3;
    private static final int MAX_TURNS_BEFORE_SUMMARY = 10;

    private final ChatModel chatModel;
    private final AgentConversationMemoryMapper memoryMapper;
    private final AgentConversationMessageMapper messageMapper;
    private final ObjectMapper objectMapper;

    public InterviewAssistantMemoryService(
            ChatModel chatModel,
            AgentConversationMemoryMapper memoryMapper,
            AgentConversationMessageMapper messageMapper,
            ObjectMapper objectMapper
    ) {
        this.chatModel = chatModel;
        this.memoryMapper = memoryMapper;
        this.messageMapper = messageMapper;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> enrichParams(String sessionId, Map<String, Object> params) {
        Map<String, Object> enriched = new LinkedHashMap<>(params == null ? Map.of() : params);
        List<Map<String, String>> incomingHistory = normalizeConversationHistory(enriched.get("conversationHistory"));
        if (sessionId == null || sessionId.isBlank()) {
            enriched.put("conversationSummary", "");
            enriched.put("conversationHistory", incomingHistory);
            return enriched;
        }

        AgentConversationMemory storedMemory = memoryMapper.findBySessionId(sessionId);
        List<TurnView> turns = resolveTurns(sessionId, storedMemory, incomingHistory);
        String storedSummary = storedMemory == null ? "" : valueOf(storedMemory.getSummaryContent());
        String summary = storedSummary;
        List<TurnView> turnsForAi = turns;

        if (turns.size() > MAX_TURNS_BEFORE_SUMMARY) {
            List<TurnView> recentTurns = tailTurns(turns, MAX_RECENT_TURNS);
            List<TurnView> earlierTurns = turns.subList(0, Math.max(0, turns.size() - recentTurns.size()));
            summary = summarizeAndPersist(sessionId, storedMemory, earlierTurns, recentTurns);
            turnsForAi = recentTurns;
        } else {
            persistMemory(sessionId, storedMemory, "", storedSummary, tailTurns(turns, MAX_RECENT_TURNS));
        }

        enriched.put("conversationSummary", summary);
        enriched.put("conversationHistory", flattenTurns(turnsForAi));
        return enriched;
    }

    public void rememberAssistantReply(String sessionId, Map<String, Object> params, String reply) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        AgentConversationMemory storedMemory = memoryMapper.findBySessionId(sessionId);
        List<Map<String, String>> history = normalizeConversationHistory(params == null ? null : params.get("conversationHistory"));
        if (reply != null && !reply.isBlank()) {
            history = new ArrayList<>(history);
            history.add(Map.of("role", "assistant", "content", reply.trim()));
        }
        List<TurnView> turns = buildTurnsFromHistory(history);
        String summary = params == null ? "" : valueOf(params.get("conversationSummary"));
        persistMemory(sessionId, storedMemory, "", summary, tailTurns(turns, MAX_RECENT_TURNS));
    }

    private List<TurnView> resolveTurns(
            String sessionId,
            AgentConversationMemory storedMemory,
            List<Map<String, String>> incomingHistory
    ) {
        List<TurnView> dbTurns = buildTurnsFromMessages(messageMapper.findBySessionId(sessionId));
        List<TurnView> incomingTurns = buildTurnsFromHistory(incomingHistory);
        if (!incomingTurns.isEmpty()) {
            if (incomingTurns.size() > dbTurns.size() || flattenTurns(incomingTurns).size() > flattenTurns(dbTurns).size()) {
                return incomingTurns;
            }
            if (incomingTurns.size() == dbTurns.size() && !flattenTurns(incomingTurns).isEmpty()) {
                return incomingTurns;
            }
        }
        if (!dbTurns.isEmpty()) {
            return dbTurns;
        }
        return readRecentTurns(storedMemory);
    }

    private List<TurnView> buildTurnsFromMessages(List<AgentConversationMessageEntity> messages) {
        List<TurnView> turns = new ArrayList<>();
        TurnView current = null;
        for (AgentConversationMessageEntity item : messages) {
            if (item == null) {
                continue;
            }
            int turnNo = item.getTurnNo() == null || item.getTurnNo() <= 0 ? (turns.size() + 1) : item.getTurnNo();
            if (current == null || current.turnNo != turnNo) {
                current = new TurnView(turnNo);
                turns.add(current);
            }
            current.messages.add(Map.of(
                    "role", normalizeRole(item.getRole()),
                    "content", valueOf(item.getContent())
            ));
        }
        return turns;
    }

    private List<TurnView> buildTurnsFromHistory(List<Map<String, String>> history) {
        List<TurnView> turns = new ArrayList<>();
        TurnView current = null;
        int turnNo = 0;
        for (Map<String, String> item : history) {
            String role = normalizeRole(item.get("role"));
            String content = valueOf(item.get("content"));
            if (content.isBlank()) {
                continue;
            }
            if ("user".equals(role)) {
                turnNo += 1;
                current = new TurnView(turnNo);
                turns.add(current);
            } else if (current == null) {
                turnNo = Math.max(1, turnNo);
                current = new TurnView(turnNo == 0 ? 1 : turnNo);
                turns.add(current);
            }
            current.messages.add(Map.of("role", role, "content", content));
        }
        return turns;
    }

    private List<Map<String, String>> flattenTurns(List<TurnView> turns) {
        List<Map<String, String>> result = new ArrayList<>();
        for (TurnView turn : turns) {
            result.addAll(turn.messages);
        }
        return result;
    }

    private List<TurnView> tailTurns(List<TurnView> turns, int maxSize) {
        if (turns.size() <= maxSize) {
            return new ArrayList<>(turns);
        }
        return new ArrayList<>(turns.subList(turns.size() - maxSize, turns.size()));
    }

    private List<Map<String, String>> normalizeConversationHistory(Object rawHistory) {
        if (!(rawHistory instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, String>> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            String role = getString(map, "role");
            String content = getString(map, "content");
            if (role.isBlank() || content.isBlank()) {
                continue;
            }
            result.add(Map.of("role", normalizeRole(role), "content", content));
        }
        return result;
    }

    private String summarizeAndPersist(
            String sessionId,
            AgentConversationMemory storedMemory,
            List<TurnView> earlierTurns,
            List<TurnView> recentTurns
    ) {
        if (earlierTurns.isEmpty()) {
            String storedSummary = storedMemory == null ? "" : valueOf(storedMemory.getSummaryContent());
            persistMemory(sessionId, storedMemory, "", storedSummary, recentTurns);
            return storedSummary;
        }

        String sourceHash = hashTurns(earlierTurns);
        if (storedMemory != null
                && Objects.equals(sourceHash, valueOf(storedMemory.getSummaryHash()))
                && hasText(storedMemory.getSummaryContent())) {
            persistMemory(sessionId, storedMemory, sourceHash, storedMemory.getSummaryContent(), recentTurns);
            return storedMemory.getSummaryContent().trim();
        }

        String summary = summarizeTurns(earlierTurns);
        persistMemory(sessionId, storedMemory, sourceHash, summary, recentTurns);
        return summary;
    }

    private String summarizeTurns(List<TurnView> turns) {
        StringBuilder transcript = new StringBuilder();
        for (TurnView turn : turns) {
            transcript.append("[turn ").append(turn.turnNo).append("]\n");
            for (Map<String, String> item : turn.messages) {
                transcript.append("[")
                        .append(item.get("role"))
                        .append("] ")
                        .append(item.get("content"))
                        .append("\n");
            }
        }

        String summary = chatModel.call(
                new SystemMessage("""
                        你是会话压缩助手。请把下面的历史对话压缩成简明中文摘要，供面试助手继续使用。
                        只保留与继续对话最相关的信息：
                        1. 用户当前目标和意图
                        2. 用户已选择的测评模式
                        3. 已完成/未完成的测评环节
                        4. 用户特别关注的问题、限制或偏好
                        5. 待继续推进的下一步
                        输出控制在 6 条以内短要点，不要编造内容，不要输出 Markdown 代码块。
                        """),
                new UserMessage(transcript.toString())
        );
        return summary == null ? "" : summary.trim();
    }

    private List<TurnView> readRecentTurns(AgentConversationMemory storedMemory) {
        if (storedMemory == null || !hasText(storedMemory.getRecentTurnsJson())) {
            return List.of();
        }
        try {
            List<Map<String, Object>> raw = objectMapper.readValue(
                    storedMemory.getRecentTurnsJson(),
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );
            List<TurnView> result = new ArrayList<>();
            for (Map<String, Object> item : raw) {
                int turnNo = Integer.parseInt(String.valueOf(item.getOrDefault("turnNo", result.size() + 1)));
                TurnView turn = new TurnView(turnNo);
                Object messages = item.get("messages");
                if (messages instanceof List<?> list) {
                    for (Object message : list) {
                        if (message instanceof Map<?, ?> map) {
                            String role = getString(map, "role");
                            String content = getString(map, "content");
                            if (!role.isBlank() && !content.isBlank()) {
                                turn.messages.add(Map.of("role", normalizeRole(role), "content", content));
                            }
                        }
                    }
                }
                if (!turn.messages.isEmpty()) {
                    result.add(turn);
                }
            }
            return result;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private void persistMemory(
            String sessionId,
            AgentConversationMemory storedMemory,
            String summaryHash,
            String summaryContent,
            List<TurnView> recentTurns
    ) {
        try {
            AgentConversationMemory target = storedMemory == null ? new AgentConversationMemory() : storedMemory;
            target.setSessionId(sessionId);
            target.setSummaryHash(summaryHash);
            target.setSummaryContent(summaryContent == null ? "" : summaryContent.trim());
            target.setRecentTurnsJson(objectMapper.writeValueAsString(serializeTurns(recentTurns)));
            if (target.getId() == null) {
                memoryMapper.insert(target);
            } else {
                memoryMapper.updateById(target);
            }
        } catch (Exception ignored) {
            // 记忆持久化失败不阻断主对话流程
        }
    }

    private List<Map<String, Object>> serializeTurns(List<TurnView> turns) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (TurnView turn : turns) {
            result.add(Map.of(
                    "turnNo", turn.turnNo,
                    "messages", turn.messages
            ));
        }
        return result;
    }

    private String hashTurns(List<TurnView> turns) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(objectMapper.writeValueAsBytes(serializeTurns(turns)));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception exception) {
            return Integer.toHexString(turns.toString().hashCode());
        }
    }

    private String getString(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String normalizeRole(String role) {
        return "user".equalsIgnoreCase(role) ? "user" : "assistant";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String valueOf(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static final class TurnView {
        private final int turnNo;
        private final List<Map<String, String>> messages = new ArrayList<>();

        private TurnView(int turnNo) {
            this.turnNo = turnNo;
        }
    }
}

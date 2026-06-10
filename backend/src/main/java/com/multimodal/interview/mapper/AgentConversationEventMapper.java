package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.AgentConversationEventEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Agent 会话事件 Mapper。
 */
@Mapper
public interface AgentConversationEventMapper {

    @Select("""
            SELECT id,
                   chat_id AS chatId,
                   turn_no AS turnNo,
                   event_type AS eventType,
                   payload_json AS payloadJson,
                   created_at AS createdAt
            FROM agent_conversation_event
            WHERE chat_id = #{chatId}
            ORDER BY id ASC
            """)
    List<AgentConversationEventEntity> findByChatId(@Param("chatId") String chatId);

    @Select("""
            SELECT id,
                   chat_id AS chatId,
                   turn_no AS turnNo,
                   event_type AS eventType,
                   payload_json AS payloadJson,
                   created_at AS createdAt
            FROM agent_conversation_event
            WHERE chat_id = #{chatId}
              AND event_type = #{eventType}
            ORDER BY id DESC
            LIMIT 1
            """)
    AgentConversationEventEntity findLatestByChatIdAndType(
            @Param("chatId") String chatId,
            @Param("eventType") String eventType
    );

    @Insert("""
            INSERT INTO agent_conversation_event (
                chat_id,
                turn_no,
                event_type,
                payload_json,
                created_at
            ) VALUES (
                #{chatId},
                #{turnNo},
                #{eventType},
                #{payloadJson},
                #{createdAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AgentConversationEventEntity entity);

    @Delete("""
            DELETE FROM agent_conversation_event
            WHERE chat_id = #{chatId}
            """)
    int deleteByChatId(@Param("chatId") String chatId);

    @Delete("""
            DELETE FROM agent_conversation_event
            WHERE chat_id = #{chatId}
              AND event_type = #{eventType}
            """)
    int deleteByChatIdAndType(@Param("chatId") String chatId, @Param("eventType") String eventType);
}

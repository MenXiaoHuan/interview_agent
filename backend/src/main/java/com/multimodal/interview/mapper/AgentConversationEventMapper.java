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
                   session_id AS sessionId,
                   turn_no AS turnNo,
                   event_type AS eventType,
                   payload_json AS payloadJson,
                   created_at AS createdAt
            FROM agent_conversation_event
            WHERE session_id = #{sessionId}
            ORDER BY id ASC
            """)
    List<AgentConversationEventEntity> findBySessionId(@Param("sessionId") String sessionId);

    @Select("""
            SELECT id,
                   session_id AS sessionId,
                   turn_no AS turnNo,
                   event_type AS eventType,
                   payload_json AS payloadJson,
                   created_at AS createdAt
            FROM agent_conversation_event
            WHERE session_id = #{sessionId}
              AND event_type = #{eventType}
            ORDER BY id DESC
            LIMIT 1
            """)
    AgentConversationEventEntity findLatestBySessionIdAndType(
            @Param("sessionId") String sessionId,
            @Param("eventType") String eventType
    );

    @Insert("""
            INSERT INTO agent_conversation_event (
                session_id,
                turn_no,
                event_type,
                payload_json,
                created_at
            ) VALUES (
                #{sessionId},
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
            WHERE session_id = #{sessionId}
            """)
    int deleteBySessionId(@Param("sessionId") String sessionId);

    @Delete("""
            DELETE FROM agent_conversation_event
            WHERE session_id = #{sessionId}
              AND event_type = #{eventType}
            """)
    int deleteBySessionIdAndType(@Param("sessionId") String sessionId, @Param("eventType") String eventType);
}

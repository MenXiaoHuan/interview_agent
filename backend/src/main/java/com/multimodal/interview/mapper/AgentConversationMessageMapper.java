package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.AgentConversationMessageEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Agent 会话消息 Mapper。
 */
@Mapper
public interface AgentConversationMessageMapper {

    @Select("""
            SELECT id,
                   session_id AS sessionId,
                   turn_no AS turnNo,
                   role,
                   content,
                   created_at AS createdAt
            FROM agent_conversation_message
            WHERE session_id = #{sessionId}
            ORDER BY id ASC
            """)
    List<AgentConversationMessageEntity> findBySessionId(@Param("sessionId") String sessionId);

    @Insert("""
            INSERT INTO agent_conversation_message (
                session_id,
                turn_no,
                role,
                content,
                created_at
            ) VALUES (
                #{sessionId},
                #{turnNo},
                #{role},
                #{content},
                #{createdAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AgentConversationMessageEntity entity);

    @Delete("""
            DELETE FROM agent_conversation_message
            WHERE session_id = #{sessionId}
            """)
    int deleteBySessionId(@Param("sessionId") String sessionId);
}

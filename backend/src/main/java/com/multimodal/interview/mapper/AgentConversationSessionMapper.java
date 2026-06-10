package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.AgentConversationSessionEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Agent 会话主表 Mapper。
 */
@Mapper
public interface AgentConversationSessionMapper {

    @Select("""
            SELECT id,
                   user_id AS userId,
                   agent_key AS agentKey,
                   chat_id AS chatId,
                   title,
                   preview,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM agent_conversation_session
            WHERE user_id = #{userId}
              AND agent_key = #{agentKey}
            ORDER BY updated_at DESC, created_at DESC
            """)
    List<AgentConversationSessionEntity> findByUserAndAgentKey(
            @Param("userId") Long userId,
            @Param("agentKey") String agentKey
    );

    @Select("""
            SELECT id,
                   user_id AS userId,
                   agent_key AS agentKey,
                   chat_id AS chatId,
                   title,
                   preview,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM agent_conversation_session
            WHERE user_id = #{userId}
              AND chat_id = #{conversationId}
            LIMIT 1
            """)
    AgentConversationSessionEntity findByUserAndConversationId(
            @Param("userId") Long userId,
            @Param("conversationId") String conversationId
    );

    @Insert("""
            INSERT INTO agent_conversation_session (
                user_id,
                agent_key,
                chat_id,
                title,
                preview
            ) VALUES (
                #{userId},
                #{agentKey},
                #{chatId},
                #{title},
                #{preview}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AgentConversationSessionEntity entity);

    @Update("""
            UPDATE agent_conversation_session
            SET title = #{title},
                preview = #{preview},
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int updateById(AgentConversationSessionEntity entity);

    @Update("""
            UPDATE agent_conversation_session
            SET updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int touchById(@Param("id") Long id);

    @org.apache.ibatis.annotations.Delete("""
            DELETE FROM agent_conversation_session
            WHERE user_id = #{userId}
              AND chat_id = #{conversationId}
            """)
    int deleteByUserAndConversationId(@Param("userId") Long userId, @Param("conversationId") String conversationId);
}

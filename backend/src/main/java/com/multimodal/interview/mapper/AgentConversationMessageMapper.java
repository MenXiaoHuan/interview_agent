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
                   chat_id AS chatId,
                   turn_no AS turnNo,
                   role,
                   content,
                   created_at AS createdAt
            FROM agent_conversation_message
            WHERE chat_id = #{chatId}
            ORDER BY id ASC
            """)
    List<AgentConversationMessageEntity> findByChatId(@Param("chatId") String chatId);

    @Insert("""
            INSERT INTO agent_conversation_message (
                chat_id,
                turn_no,
                role,
                content,
                created_at
            ) VALUES (
                #{chatId},
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
            WHERE chat_id = #{chatId}
            """)
    int deleteByChatId(@Param("chatId") String chatId);
}

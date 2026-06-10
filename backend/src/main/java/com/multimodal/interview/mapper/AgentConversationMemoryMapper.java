package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.AgentConversationMemory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Agent 会话记忆数据访问接口。
 */
@Mapper
public interface AgentConversationMemoryMapper {

    @Select("""
            SELECT id,
                   chat_id AS chatId,
                   summary_hash AS summaryHash,
                   summary_content AS summaryContent,
                   recent_turns_json AS recentTurnsJson,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM agent_conversation_memory
            WHERE chat_id = #{chatId}
            LIMIT 1
            """)
    AgentConversationMemory findByChatId(@Param("chatId") String chatId);

    @Insert("""
            INSERT INTO agent_conversation_memory (
                chat_id,
                summary_hash,
                summary_content,
                recent_turns_json
            ) VALUES (
                #{chatId},
                #{summaryHash},
                #{summaryContent},
                #{recentTurnsJson}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AgentConversationMemory memory);

    @Update("""
            UPDATE agent_conversation_memory
            SET summary_hash = #{summaryHash},
                summary_content = #{summaryContent},
                recent_turns_json = #{recentTurnsJson},
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int updateById(AgentConversationMemory memory);

    @Delete("""
            DELETE FROM agent_conversation_memory
            WHERE chat_id = #{chatId}
            """)
    int deleteByChatId(@Param("chatId") String chatId);
}

package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.ChatMessage;
import com.multimodal.interview.dto.ChatMessageResponse;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ChatMessage 数据访问接口。
 */
@Mapper
public interface ChatMessageMapper {
    @Insert("INSERT INTO chat_message (user_id, content, created_at) VALUES (#{userId}, #{content}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatMessage message);

    @Select("SELECT m.id, m.content, m.created_at, m.user_id AS userId, COALESCE(u.nickname, '系统消息') AS nickname, u.avatar_url AS avatarUrl " +
            "FROM chat_message m LEFT JOIN user u ON m.user_id = u.id " +
            "WHERE m.user_id = #{userId} AND m.created_at >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "ORDER BY m.created_at DESC")
    List<ChatMessageResponse> findRecentByUserId(@Param("userId") Long userId, @Param("days") int days);

    @Select("SELECT m.id, m.content, m.created_at, m.user_id AS userId, COALESCE(u.nickname, '系统消息') AS nickname, u.avatar_url AS avatarUrl " +
            "FROM chat_message m LEFT JOIN user u ON m.user_id = u.id " +
            "WHERE m.created_at >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "ORDER BY m.created_at DESC")
    List<ChatMessageResponse> findRecentGlobal(@Param("days") int days);
}

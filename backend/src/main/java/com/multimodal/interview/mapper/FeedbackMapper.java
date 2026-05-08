package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.Feedback;
import com.multimodal.interview.dto.FeedbackItemResponse;
import com.multimodal.interview.dto.FeedbackStatusResponse;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Feedback 数据访问接口。
 */
@Mapper
public interface FeedbackMapper {
    @Insert("INSERT INTO feedback (user_id, type, title, content, status, created_at, updated_at) " +
            "VALUES (#{userId}, #{type}, #{title}, #{content}, 'PENDING', NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Feedback feedback);

    @Select("SELECT * FROM feedback WHERE id = #{id}")
    Feedback findById(Long id);

    @Select("SELECT * FROM feedback WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<Feedback> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM feedback WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    @Update("UPDATE feedback SET reply = #{reply}, status = #{status}, " +
            "reply_time = NOW(), admin_id = #{adminId}, admin_name = #{adminName}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int updateReply(Feedback feedback);

    @Select("<script>" +
            "SELECT * FROM feedback " +
            "WHERE 1=1 " +
            "<if test='type != null'>AND type = #{type}</if> " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "ORDER BY created_at DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<Feedback> findByCondition(@Param("type") String type, @Param("status") String status,
                                   @Param("offset") int offset, @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM feedback " +
            "WHERE 1=1 " +
            "<if test='type != null'>AND type = #{type}</if> " +
            "<if test='status != null'>AND status = #{status}</if>" +
            "</script>")
    int countByCondition(@Param("type") String type, @Param("status") String status);

    @Select("SELECT f.id, f.user_id AS userId, COALESCE(u.nickname,'用户') AS nickname, f.type, f.title, f.content, f.status, f.created_at AS createdAt, f.updated_at AS updatedAt " +
            "FROM feedback f LEFT JOIN user u ON f.user_id = u.id " +
            "WHERE f.user_id != #{excludeUserId} " +
            "ORDER BY f.created_at DESC LIMIT #{offset}, #{size}")
    List<FeedbackItemResponse> findAllExcludingUser(@Param("excludeUserId") Long excludeUserId,
                                                    @Param("offset") int offset,
                                                    @Param("size") int size);

    @Select("SELECT f.id, f.user_id AS userId, COALESCE(u.nickname,'用户') AS nickname, f.type, f.title, f.content, f.status, f.created_at AS createdAt, f.updated_at AS updatedAt " +
            "FROM feedback f LEFT JOIN user u ON f.user_id = u.id " +
            "WHERE f.user_id != #{excludeUserId} " +
            "ORDER BY f.created_at DESC")
    List<FeedbackItemResponse> findAllExcludingUserAll(@Param("excludeUserId") Long excludeUserId);

    @Select("SELECT f.id, f.user_id AS userId, COALESCE(u.nickname,'用户') AS nickname, f.type, f.title, f.content, f.status, f.created_at AS createdAt, f.updated_at AS updatedAt " +
            "FROM feedback f LEFT JOIN user u ON f.user_id = u.id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.created_at DESC")
    List<FeedbackItemResponse> findWithUserByUserIdAll(@Param("userId") Long userId);

    @Select("SELECT f.id, f.user_id AS userId, COALESCE(u.nickname,'用户') AS nickname, f.type, f.title, f.content, f.status, f.created_at AS createdAt, f.updated_at AS updatedAt " +
            "FROM feedback f LEFT JOIN user u ON f.user_id = u.id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.created_at DESC LIMIT #{offset}, #{size}")
    List<FeedbackItemResponse> findWithUserByUserId(@Param("userId") Long userId,
                                                    @Param("offset") int offset,
                                                    @Param("size") int size);

    @Select("SELECT id, title, status, reply, reply_time AS replyTime, updated_at AS updatedAt " +
            "FROM feedback WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<FeedbackStatusResponse> findStatusByUserId(@Param("userId") Long userId);
}

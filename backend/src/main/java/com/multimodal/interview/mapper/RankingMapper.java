package com.multimodal.interview.mapper;

import com.multimodal.interview.dto.RankingResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Ranking 数据访问接口。
 */
@Mapper
public interface RankingMapper {

    @Select({
            "<script>",
            "SELECT t.user_id AS userId, COALESCE(u.nickname,'系统消息') AS nickname, u.avatar_url AS avatarUrl, t.max_score AS score ",
            "FROM (",
            "  SELECT user_id, MAX(overall_score) AS max_score FROM ai_resume_history",
            "  <where>",
            "    AND overall_score > 0",
            "    <if test=\"jobId != null\">AND job_id = #{jobId}</if>",
            "  </where>",
            "  GROUP BY user_id",
            ") t LEFT JOIN user u ON t.user_id = u.id ",
            "ORDER BY t.max_score DESC LIMIT #{size}",
            "</script>"
    })
    List<RankingResponse> topResume(@Param("jobId") Long jobId, @Param("size") int size);

    @Select({
            "<script>",
            "SELECT t.user_id AS userId, COALESCE(u.nickname,'系统消息') AS nickname, u.avatar_url AS avatarUrl, t.max_score AS score ",
            "FROM (",
            "  SELECT user_id, MAX(overall_score) AS max_score FROM ai_question_history",
            "  <where>",
            "    AND overall_score > 0",
            "    <if test=\"jobId != null\">AND job_id = #{jobId}</if>",
            "  </where>",
            "  GROUP BY user_id",
            ") t LEFT JOIN user u ON t.user_id = u.id ",
            "ORDER BY t.max_score DESC LIMIT #{size}",
            "</script>"
    })
    List<RankingResponse> topQuestion(@Param("jobId") Long jobId, @Param("size") int size);

    @Select({
            "<script>",
            "SELECT t.user_id AS userId, COALESCE(u.nickname,'系统消息') AS nickname, u.avatar_url AS avatarUrl, t.max_score AS score ",
            "FROM (",
            "  SELECT user_id, MAX(overall_score) AS max_score FROM ai_interview_round_history",
            "  <where>",
            "    AND round_type = #{roundType}",
            "    AND overall_score > 0",
            "    <if test=\"jobId != null\">AND job_id = #{jobId}</if>",
            "  </where>",
            "  GROUP BY user_id",
            ") t LEFT JOIN user u ON t.user_id = u.id ",
            "ORDER BY t.max_score DESC LIMIT #{size}",
            "</script>"
    })
    List<RankingResponse> topRound(
            @Param("roundType") String roundType,
            @Param("jobId") Long jobId,
            @Param("size") int size
    );
}

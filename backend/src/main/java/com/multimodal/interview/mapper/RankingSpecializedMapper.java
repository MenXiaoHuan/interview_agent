package com.multimodal.interview.mapper;

import com.multimodal.interview.dto.RankingResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * RankingSpecialized 数据访问接口。
 */
@Mapper
public interface RankingSpecializedMapper {

    @Select({
            "<script>",
            "SELECT t.user_id AS userId, COALESCE(u.nickname,'系统消息') AS nickname, u.avatar_url AS avatarUrl, t.max_score AS score ",
            "FROM (",
            "  SELECT user_id, MAX(score) AS max_score FROM resume_analysis_history",
            "  <where>",
            "    <if test=\"jobId != null\">AND job_id = #{jobId}</if>",
            "    <if test=\"evaluationType != null and evaluationType != ''\">AND evaluation_type = #{evaluationType}</if>",
            "  </where>",
            "  GROUP BY user_id",
            ") t LEFT JOIN user u ON t.user_id = u.id ",
            "ORDER BY t.max_score DESC LIMIT #{size}",
            "</script>"
    })
    List<RankingResponse> topResume(@Param("jobId") Long jobId, @Param("evaluationType") String evaluationType, @Param("size") int size);

    @Select({
            "<script>",
            "SELECT t.user_id AS userId, COALESCE(u.nickname,'系统消息') AS nickname, u.avatar_url AS avatarUrl, t.max_score AS score ",
            "FROM (",
            "  SELECT user_id, MAX(total_score) AS max_score FROM choice_question_record",
            "  <where>",
            "    <if test=\"jobId != null\">AND job_id = #{jobId}</if>",
            "    <if test=\"evaluationType != null and evaluationType != ''\">AND evaluation_type = #{evaluationType}</if>",
            "  </where>",
            "  GROUP BY user_id",
            ") t LEFT JOIN user u ON t.user_id = u.id ",
            "ORDER BY t.max_score DESC LIMIT #{size}",
            "</script>"
    })
    List<RankingResponse> topQuestion(@Param("jobId") Long jobId, @Param("evaluationType") String evaluationType, @Param("size") int size);

    @Select({
            "<script>",
            "SELECT t.user_id AS userId, COALESCE(u.nickname,'系统消息') AS nickname, u.avatar_url AS avatarUrl, t.max_score AS score ",
            "FROM (",
            "  SELECT user_id, MAX(total_score) AS max_score FROM scenario_analysis",
            "  <where>",
            "    <if test=\"evaluationType != null and evaluationType != ''\">AND evaluation_type = #{evaluationType}</if>",
            "  </where>",
            "  GROUP BY user_id",
            ") t LEFT JOIN user u ON t.user_id = u.id ",
            "ORDER BY t.max_score DESC LIMIT #{size}",
            "</script>"
    })
    List<RankingResponse> topScenario(@Param("evaluationType") String evaluationType, @Param("size") int size);
}

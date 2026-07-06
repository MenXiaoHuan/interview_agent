package com.multimodal.interview.mapper;

import com.multimodal.interview.dto.AiviewInsightAggregate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * AIview 岗位洞察数据访问接口。
 */
@Mapper
public interface AiviewInsightsMapper {

    @Select({
            "SELECT COUNT(*) AS attemptCount, COUNT(DISTINCT user_id) AS participantCount,",
            "       AVG(overall_score) AS averageScore, MAX(overall_score) AS max_score",
            "FROM ai_resume_history",
            "WHERE job_id = #{jobId}",
            "  AND upload_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)"
    })
    AiviewInsightAggregate aggregateResume(@Param("jobId") Long jobId);

    @Select({
            "SELECT COUNT(*) AS attemptCount, COUNT(DISTINCT user_id) AS participantCount,",
            "       AVG(overall_score) AS averageScore, MAX(overall_score) AS max_score",
            "FROM ai_question_history",
            "WHERE job_id = #{jobId}",
            "  AND end_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)"
    })
    AiviewInsightAggregate aggregateQuestion(@Param("jobId") Long jobId);

    @Select({
            "SELECT COUNT(*) AS attemptCount, COUNT(DISTINCT user_id) AS participantCount,",
            "       AVG(overall_score) AS averageScore, MAX(overall_score) AS max_score",
            "FROM ai_interview_round_history",
            "WHERE job_id = #{jobId}",
            "  AND round_type = #{roundType}",
            "  AND end_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)"
    })
    AiviewInsightAggregate aggregateRound(@Param("jobId") Long jobId, @Param("roundType") String roundType);
}

package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.AiInterviewRoundHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * AiInterviewRoundHistory 数据访问接口。
 */
@Mapper
public interface AiInterviewRoundHistoryMapper {

    @Insert("INSERT INTO ai_interview_round_history(assessment_session_id, round_type, user_id, job_id, start_time, end_time, " +
            "total_time, overall_score, ai_assessment) " +
            "VALUES(#{assessmentSessionId}, #{roundType}, #{userId}, #{jobId}, #{startTime}, #{endTime}, #{totalTime}, #{overallScore}, " +
            "#{aiAssessment})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiInterviewRoundHistory scenario);

    @Select("SELECT * FROM ai_interview_round_history WHERE id = #{id}")
    @Results(id = "scenarioResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "assessmentSessionId", column = "assessment_session_id"),
            @Result(property = "roundType", column = "round_type"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "jobId", column = "job_id"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "totalTime", column = "total_time"),
            @Result(property = "overallScore", column = "overall_score"),
            @Result(property = "aiAssessment", column = "ai_assessment")
    })
    AiInterviewRoundHistory findById(Long id);

    @Select("SELECT * FROM ai_interview_round_history WHERE user_id = #{userId}")
    @ResultMap("scenarioResultMap")
    List<AiInterviewRoundHistory> findByUserId(Long userId);

    @Select("SELECT * FROM ai_interview_round_history WHERE assessment_session_id = #{assessmentSessionId} AND round_type = #{roundType} ORDER BY id DESC LIMIT 1")
    @ResultMap("scenarioResultMap")
    AiInterviewRoundHistory findLatestByAssessmentSessionIdAndRoundType(String assessmentSessionId, String roundType);
}

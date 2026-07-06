package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.AiQuestionHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * AiQuestionHistory 数据访问接口。
 */
@Mapper
public interface AiQuestionHistoryMapper {

    @Insert("INSERT INTO ai_question_history(assessment_session_id, user_id, job_id, start_time, end_time, " +
            "total_time, overall_score, ai_suggestions) " +
            "VALUES(#{assessmentSessionId}, #{userId}, #{jobId}, #{startTime}, #{endTime}, #{totalTime}, " +
            "#{overallScore}, #{aiSuggestions})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiQuestionHistory question);

    @Select("SELECT * FROM ai_question_history WHERE id = #{id}")
    @Results(id = "questionResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "assessmentSessionId", column = "assessment_session_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "jobId", column = "job_id"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "totalTime", column = "total_time"),
            @Result(property = "overallScore", column = "overall_score"),
            @Result(property = "aiSuggestions", column = "ai_suggestions")
    })
    AiQuestionHistory findById(Long id);

    @Select("SELECT * FROM ai_question_history WHERE user_id = #{userId}")
    @ResultMap("questionResultMap")
    List<AiQuestionHistory> findByUserId(Long userId);

    @Select("SELECT * FROM ai_question_history WHERE assessment_session_id = #{assessmentSessionId} ORDER BY id DESC LIMIT 1")
    @ResultMap("questionResultMap")
    AiQuestionHistory findLatestByAssessmentSessionId(String assessmentSessionId);
}

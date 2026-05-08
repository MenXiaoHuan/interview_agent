package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.ComprehensiveScenarioHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ComprehensiveScenarioHistory 数据访问接口。
 */
@Mapper
public interface ComprehensiveScenarioHistoryMapper {

    @Insert("INSERT INTO comprehensive_scenario_history(user_id, job_id, start_time, end_time, " +
            "total_time, overall_score, fluency_score, emotion_score, relevance_score, " +
            "adaptability_score, ai_assessment) " +
            "VALUES(#{userId}, #{jobId}, #{startTime}, #{endTime}, #{totalTime}, #{overallScore}, " +
            "#{fluencyScore}, #{emotionScore}, #{relevanceScore}, #{adaptabilityScore}, " +
            "#{aiAssessment})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ComprehensiveScenarioHistory scenario);

    @Select("SELECT * FROM comprehensive_scenario_history WHERE id = #{id}")
    @Results(id = "scenarioResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "jobId", column = "job_id"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "totalTime", column = "total_time"),
            @Result(property = "overallScore", column = "overall_score"),
            @Result(property = "fluencyScore", column = "fluency_score"),
            @Result(property = "emotionScore", column = "emotion_score"),
            @Result(property = "relevanceScore", column = "relevance_score"),
            @Result(property = "adaptabilityScore", column = "adaptability_score"),
            @Result(property = "aiAssessment", column = "ai_assessment")
    })
    ComprehensiveScenarioHistory findById(Long id);

    @Select("SELECT * FROM comprehensive_scenario_history WHERE user_id = #{userId}")
    @ResultMap("scenarioResultMap")
    List<ComprehensiveScenarioHistory> findByUserId(Long userId);
}

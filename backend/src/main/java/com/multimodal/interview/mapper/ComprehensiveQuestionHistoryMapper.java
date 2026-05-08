package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.ComprehensiveQuestionHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ComprehensiveQuestionHistory 数据访问接口。
 */
@Mapper
public interface ComprehensiveQuestionHistoryMapper {

    @Insert("INSERT INTO comprehensive_question_history(user_id, job_id, start_time, end_time, " +
            "total_time, overall_score, ai_suggestions) " +
            "VALUES(#{userId}, #{jobId}, #{startTime}, #{endTime}, #{totalTime}, " +
            "#{overallScore}, #{aiSuggestions})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ComprehensiveQuestionHistory question);

    @Select("SELECT * FROM comprehensive_question_history WHERE id = #{id}")
    @Results(id = "questionResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "jobId", column = "job_id"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "totalTime", column = "total_time"),
            @Result(property = "overallScore", column = "overall_score"),
            @Result(property = "aiSuggestions", column = "ai_suggestions")
    })
    ComprehensiveQuestionHistory findById(Long id);

    @Select("SELECT * FROM comprehensive_question_history WHERE user_id = #{userId}")
    @ResultMap("questionResultMap")
    List<ComprehensiveQuestionHistory> findByUserId(Long userId);
}

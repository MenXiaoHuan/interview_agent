package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.AiResumeHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * AiResumeHistory 数据访问接口。
 */
@Mapper
public interface AiResumeHistoryMapper {

    @Insert("INSERT INTO ai_resume_history(assessment_session_id, user_id, job_id, file_name, file_size, file_type, " +
            "overall_score, ai_analysis, ai_suggestions) " +
            "VALUES(#{assessmentSessionId}, #{userId}, #{jobId}, #{fileName}, #{fileSize}, #{fileType}, #{overallScore}, " +
            "#{aiAnalysis}, #{aiSuggestions})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiResumeHistory resume);

    @Select("SELECT * FROM ai_resume_history WHERE id = #{id}")
    @Results(id = "resumeResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "assessmentSessionId", column = "assessment_session_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "jobId", column = "job_id"),
            @Result(property = "fileName", column = "file_name"),
            @Result(property = "fileSize", column = "file_size"),
            @Result(property = "fileType", column = "file_type"),
            @Result(property = "uploadTime", column = "upload_time"),
            @Result(property = "overallScore", column = "overall_score"),
            @Result(property = "aiAnalysis", column = "ai_analysis"),
            @Result(property = "aiSuggestions", column = "ai_suggestions")
    })
    AiResumeHistory findById(Long id);

    @Select("SELECT * FROM ai_resume_history WHERE user_id = #{userId}")
    @ResultMap("resumeResultMap")
    List<AiResumeHistory> findByUserId(Long userId);

    @Select("SELECT * FROM ai_resume_history WHERE assessment_session_id = #{assessmentSessionId} ORDER BY id DESC LIMIT 1")
    @ResultMap("resumeResultMap")
    AiResumeHistory findLatestByAssessmentSessionId(String assessmentSessionId);
}

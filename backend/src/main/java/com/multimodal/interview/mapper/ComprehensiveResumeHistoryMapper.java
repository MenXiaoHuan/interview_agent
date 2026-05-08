package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.ComprehensiveResumeHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ComprehensiveResumeHistory 数据访问接口。
 */
@Mapper
public interface ComprehensiveResumeHistoryMapper {

    @Insert("INSERT INTO comprehensive_resume_history(user_id, job_id, file_name, file_size, file_type, " +
            "overall_score, skill_match, experience_match, education_match, communication_skill, " +
            "leadership_skill, ai_analysis, ai_suggestions) " +
            "VALUES(#{userId}, #{jobId}, #{fileName}, #{fileSize}, #{fileType}, #{overallScore}, " +
            "#{skillMatch}, #{experienceMatch}, #{educationMatch}, #{communicationSkill}, " +
            "#{leadershipSkill}, #{aiAnalysis}, #{aiSuggestions})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ComprehensiveResumeHistory resume);

    @Select("SELECT * FROM comprehensive_resume_history WHERE id = #{id}")
    @Results(id = "resumeResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "jobId", column = "job_id"),
            @Result(property = "fileName", column = "file_name"),
            @Result(property = "fileSize", column = "file_size"),
            @Result(property = "fileType", column = "file_type"),
            @Result(property = "uploadTime", column = "upload_time"),
            @Result(property = "overallScore", column = "overall_score"),
            @Result(property = "skillMatch", column = "skill_match"),
            @Result(property = "experienceMatch", column = "experience_match"),
            @Result(property = "educationMatch", column = "education_match"),
            @Result(property = "communicationSkill", column = "communication_skill"),
            @Result(property = "leadershipSkill", column = "leadership_skill"),
            @Result(property = "aiAnalysis", column = "ai_analysis"),
            @Result(property = "aiSuggestions", column = "ai_suggestions")
    })
    ComprehensiveResumeHistory findById(Long id);

    @Select("SELECT * FROM comprehensive_resume_history WHERE user_id = #{userId}")
    @ResultMap("resumeResultMap")
    List<ComprehensiveResumeHistory> findByUserId(Long userId);
}

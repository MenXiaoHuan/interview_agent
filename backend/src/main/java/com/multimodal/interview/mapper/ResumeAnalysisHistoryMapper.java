package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.ResumeAnalysisHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * ResumeAnalysisHistory 数据访问接口。
 */
@Mapper
public interface ResumeAnalysisHistoryMapper {
    @Insert("INSERT INTO resume_analysis_history (id, user_id, job_id, resume_content, score, advantages, disadvantages, improvement_suggestions, professional_skills, project_execution, innovation, communication, adaptability, evaluation_type, created_at, updated_at)" +
            " VALUES (#{id}, #{userId}, #{jobId}, #{resumeContent}, #{score}, #{advantages}, #{disadvantages}, #{improvementSuggestions}, #{professionalSkills}, #{projectExecution}, #{innovation}, #{communication}, #{adaptability}, #{evaluationType}, NOW(), NOW())")
    void insert(ResumeAnalysisHistory resumeAnalysisHistory);

    @Select("SELECT * FROM resume_analysis_history" +
            " WHERE user_id = #{userId}" +
            " AND evaluation_type = #{evaluationType}" +
            " ORDER BY created_at DESC")
    List<ResumeAnalysisHistory> findByUserIdAndevaluationType(Long userId, String evaluationType);

    @Select("SELECT * FROM resume_analysis_history" +
            " WHERE id = #{resumeHistoryId}")
    ResumeAnalysisHistory findByResumeHistoryId(String resumeHistoryId);

    @Update("UPDATE resume_analysis_history " +
            "SET user_id = #{userId}, " +
            "job_id = #{jobId}, " +
            "resume_content = #{resumeContent}, " +
            "score = #{score}, " +
            "advantages = #{advantages}, " +
            "disadvantages = #{disadvantages}, " +
            "improvement_suggestions = #{improvementSuggestions}, " +
            "professional_skills = #{professionalSkills}, " +
            "project_execution = #{projectExecution}, " +
            "innovation = #{innovation}, " +
            "communication = #{communication}, " +
            "adaptability = #{adaptability}, " +
            "created_at = #{createdAt}, " +
            "updated_at = #{updatedAt}, " +
            "evaluation_type = #{evaluationType} " +
            "WHERE id = #{id}")
    void update(ResumeAnalysisHistory resumeAnalysisHistory);
}

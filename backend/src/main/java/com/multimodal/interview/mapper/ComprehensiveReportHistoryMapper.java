package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.ComprehensiveReportHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ComprehensiveReportHistory 数据访问接口。
 */
@Mapper
public interface ComprehensiveReportHistoryMapper {

    @Insert("INSERT INTO comprehensive_report_history(user_id, job_id, overall_score, resume_score, " +
            "question_score, scenario_score, strength_analysis, improvement_analysis, learning_route, create_time) " +
            "VALUES(#{userId}, #{jobId}, #{overallScore}, #{resumeScore}, " +
            "#{questionScore}, #{scenarioScore}, #{strengthAnalysis}, #{improvementAnalysis}, #{learningRoute}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ComprehensiveReportHistory report);

    @Select("SELECT * FROM comprehensive_report_history WHERE id = #{id}")
    @Results(id = "reportResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "jobId", column = "job_id"),
            @Result(property = "overallScore", column = "overall_score"),
            @Result(property = "resumeScore", column = "resume_score"),
            @Result(property = "questionScore", column = "question_score"),
            @Result(property = "scenarioScore", column = "scenario_score"),
            @Result(property = "strengthAnalysis", column = "strength_analysis"),
            @Result(property = "improvementAnalysis", column = "improvement_analysis"),
            @Result(property = "learningRoute", column = "learning_route"),
            @Result(property = "createTime", column = "create_time")
    })
    ComprehensiveReportHistory findById(Long id);

    @Select("SELECT * FROM comprehensive_report_history WHERE user_id = #{userId}")
    @ResultMap("reportResultMap")
    List<ComprehensiveReportHistory> findByUserId(Long userId);

    @Select("SELECT * FROM comprehensive_report_history WHERE user_id = #{userId} AND job_id = #{jobId}")
    @ResultMap("reportResultMap")
    List<ComprehensiveReportHistory> findByUserIdAndJobId(Long userId, Long jobId);

    @Select("SELECT * FROM comprehensive_report_history ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    @ResultMap("reportResultMap")
    List<ComprehensiveReportHistory> findAll(int limit, int offset);

    @Select("SELECT COUNT(*) FROM comprehensive_report_history")
    int count();

    @Delete("DELETE FROM comprehensive_report_history WHERE id = #{id}")
    int deleteById(Long id);

    @Update("UPDATE comprehensive_report_history SET overall_score = #{overallScore}, " +
            "resume_score = #{resumeScore}, question_score = #{questionScore}, " +
            "scenario_score = #{scenarioScore}, strength_analysis = #{strengthAnalysis}, " +
            "improvement_analysis = #{improvementAnalysis}, learning_route = #{learningRoute} " +
            "WHERE id = #{id}")
    int update(ComprehensiveReportHistory report);

    @Select("SELECT crh.id, crh.user_id, crh.job_id, j.name as job_name, " +
            "crh.create_time, crh.strength_analysis, crh.improvement_analysis, crh.learning_route " +
            "FROM comprehensive_report_history crh " +
            "LEFT JOIN jobs j ON crh.job_id = j.id " +
            "WHERE crh.user_id = #{userId} " +
            "ORDER BY crh.create_time DESC")
    @Results(id = "reportSummaryResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "jobId", column = "job_id"),
            @Result(property = "jobName", column = "job_name"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "strengthAnalysis", column = "strength_analysis"),
            @Result(property = "improvementAnalysis", column = "improvement_analysis"),
            @Result(property = "learningRoute", column = "learning_route")
    })
    List<com.multimodal.interview.dto.ComprehensiveReportSummaryResponse> findSummaryByUserId(Long userId);
}

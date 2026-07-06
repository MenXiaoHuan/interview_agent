package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.AiAssessmentSession;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * AIview 测评流程实例 Mapper。
 */
@Mapper
public interface AiAssessmentSessionMapper {

    @Select("""
            SELECT id,
                   assessment_session_id AS assessmentSessionId,
                   user_id AS userId,
                   job_id AS jobId,
                   status,
                   current_stage AS currentStage,
                   elimination_reason AS eliminationReason,
                   final_summary AS finalSummary,
                   final_suggestion AS finalSuggestion,
                   start_time AS startTime,
                   end_time AS endTime,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM ai_assessment_session
            WHERE assessment_session_id = #{assessmentSessionId}
            LIMIT 1
            """)
    @Results(id = "assessmentSessionResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "assessmentSessionId", column = "assessmentSessionId"),
            @Result(property = "userId", column = "userId"),
            @Result(property = "jobId", column = "jobId"),
            @Result(property = "status", column = "status"),
            @Result(property = "currentStage", column = "currentStage"),
            @Result(property = "eliminationReason", column = "eliminationReason"),
            @Result(property = "finalSummary", column = "finalSummary"),
            @Result(property = "finalSuggestion", column = "finalSuggestion"),
            @Result(property = "startTime", column = "startTime"),
            @Result(property = "endTime", column = "endTime"),
            @Result(property = "createdAt", column = "createdAt"),
            @Result(property = "updatedAt", column = "updatedAt")
    })
    AiAssessmentSession findByAssessmentSessionId(String assessmentSessionId);

    @Insert("""
            INSERT INTO ai_assessment_session (
                assessment_session_id,
                user_id,
                job_id,
                status,
                current_stage,
                elimination_reason,
                final_summary,
                final_suggestion,
                start_time,
                end_time
            ) VALUES (
                #{assessmentSessionId},
                #{userId},
                #{jobId},
                #{status},
                #{currentStage},
                #{eliminationReason},
                #{finalSummary},
                #{finalSuggestion},
                #{startTime},
                #{endTime}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiAssessmentSession session);

    @Update("""
            UPDATE ai_assessment_session
            SET status = #{status},
                current_stage = #{currentStage},
                elimination_reason = #{eliminationReason},
                final_summary = #{finalSummary},
                final_suggestion = #{finalSuggestion},
                start_time = #{startTime},
                end_time = #{endTime},
                updated_at = CURRENT_TIMESTAMP
            WHERE assessment_session_id = #{assessmentSessionId}
            """)
    int updateByAssessmentSessionId(AiAssessmentSession session);
}

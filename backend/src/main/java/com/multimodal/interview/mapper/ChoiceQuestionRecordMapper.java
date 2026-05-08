package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.choiceQuestionRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ChoiceQuestionRecord 数据访问接口。
 */
@Mapper
public interface ChoiceQuestionRecordMapper {
    @Insert("INSERT INTO choice_question_record (user_id, job_id, total_score, correct_count, correct_rate, status) " +
            "VALUES (#{userId}, #{jobId}, #{totalScore}, #{correctCount}, #{correctRate}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(choiceQuestionRecord record);

    @Select("SELECT * FROM choice_question_record WHERE id = #{id}")
    choiceQuestionRecord findById(Long id);

    @Insert("INSERT INTO choice_question_record (user_id, job_id, total_score) " +
            "VALUES (#{userId}, #{jobId}, #{totalScore})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(choiceQuestionRecord record);

    @Select("SELECT * FROM choice_question_record WHERE user_id = #{userId} AND evaluation_type = #{evaluationType} ORDER BY created_at DESC")
    List<choiceQuestionRecord> findByUserIdAndEvaluationType(@Param("userId") Long userId, @Param("evaluationType") String evaluationType);

    @Select("SELECT * FROM choice_question_record WHERE user_id = #{userId} AND job_id = #{jobId} AND evaluation_type = #{evaluationType} ORDER BY created_at DESC")
    List<choiceQuestionRecord> findByUserIdAndJobIdAndEvaluationType(@Param("userId") Long userId, @Param("jobId") Long jobId, @Param("evaluationType") String evaluationType);
}

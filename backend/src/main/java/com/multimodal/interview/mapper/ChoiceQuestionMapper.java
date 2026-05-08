package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.choiceQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ChoiceQuestion 数据访问接口。
 */
@Mapper
public interface ChoiceQuestionMapper {
    @Select("SELECT * FROM choice_question")
    List<choiceQuestion> getAll();

    @Select("SELECT * FROM choice_question WHERE job_id = #{jobId} ORDER BY RAND() LIMIT 10")
    List<choiceQuestion> findRandomQuestionsByJobId(Long jobId);

    @Select("SELECT * FROM choice_question WHERE id = #{id}")
    choiceQuestion findById(Long id);

    @Select("SELECT * FROM choice_question WHERE job_id = #{jobId}")
    List<choiceQuestion> findByJobId(Long jobId);

    @Insert("INSERT INTO choice_question(job_id, question, options, correct_answer, explanation, difficulty, score ,created_at)" +
            "VALUES(#{jobId},#{question},#{options},#{correctAnswer},#{explanation},#{difficulty},#{score},NOW())")
    int addQuestion(choiceQuestion question);

    @Update("UPDATE choice_question SET question = #{question}, options = #{options}, correct_answer = #{correctAnswer}, explanation = #{explanation}, difficulty = #{difficulty}, score = #{score}, updated_at = NOW()" +
            "WHERE id = #{id}")
    int updateQuestion(choiceQuestion question);

    @Delete("DELETE FROM choice_question WHERE id = #{id}")
    int deleteQuestion(Long id);
}

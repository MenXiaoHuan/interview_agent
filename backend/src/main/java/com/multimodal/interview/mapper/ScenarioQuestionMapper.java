package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.ScenarioQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ScenarioQuestion 数据访问接口。
 */
@Mapper
public interface ScenarioQuestionMapper {
    @Insert("INSERT INTO scenario_question(job_id,question,created_at) VALUES(#{jobId},#{question},NOW())")
    int addQuestion(ScenarioQuestion question);

    @Select("SELECT id, job_id AS jobId, question, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM scenario_question WHERE job_id = #{jobId}")
    List<ScenarioQuestion> getQuestionsByJobId(Long jobId);

    @Select("SELECT * FROM scenario_question")
    List<ScenarioQuestion> getAll();

    @Update("UPDATE scenario_question SET question = #{question},updated_at = NOW() WHERE id = #{id}")
    int updateQuestion(ScenarioQuestion question);

    @Delete("DELETE FROM scenario_question WHERE id = #{id}")
    int deleteQuestion(Long id);
}

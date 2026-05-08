package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.ScenarioAnalysis;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * ScenarioAnalysis 数据访问接口。
 */
@Mapper
public interface ScenarioAnalysisMapper {

    @Insert("INSERT INTO scenario_analysis (user_id, total_score, score1, suggestion1, score2, suggestion2, score3, suggestion3, score4, suggestion4, score5, suggestion5, create_time, update_time) " +
            "VALUES (#{userId}, #{totalScore}, #{score1}, #{suggestion1}, #{score2}, #{suggestion2}, #{score3}, #{suggestion3}, #{score4}, #{suggestion4}, #{score5}, #{suggestion5}, NOW(), NOW())")
    int insert(ScenarioAnalysis scenarioAnalysis);

    @Select("SELECT * FROM scenario_analysis WHERE id = #{id}")
    ScenarioAnalysis findById(@Param("id") Long id);

    @Select("SELECT * FROM scenario_analysis WHERE user_id = #{userId} AND evaluation_type = #{evaluationType} ORDER BY create_time DESC")
    List<ScenarioAnalysis> findByUserIdAndEvaluationType(@Param("userId") String userId, @Param("evaluationType") String evaluationType);
}

package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.choiceAnswer;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * ChoiceAnswer 数据访问接口。
 */
@Mapper
public interface ChoiceAnswerMapper {
    @Insert("INSERT INTO choice_answer (record_id, question_id, user_answer, is_correct, score, answer_time) " +
            "VALUES (#{recordId}, #{questionId}, #{userAnswer}, #{isCorrect}, #{score}, #{answerTime})")
    void save(choiceAnswer answer);

    @Select("SELECT * FROM choice_answer WHERE record_id = #{recordId}")
    List<choiceAnswer> findByRecordId(Long recordId);
}

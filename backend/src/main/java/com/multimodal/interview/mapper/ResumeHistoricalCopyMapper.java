package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.ResumeHistoricalCopy;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * ResumeHistoricalCopy 数据访问接口。
 */
@Mapper
public interface ResumeHistoricalCopyMapper {
    @Insert("INSERT INTO resume_historical_copy (id, resume_history_id, content, created_at, updated_at) VALUES (#{id}, #{resumeHistoryId}, #{content}, NOW(), NOW())")
    void insert(ResumeHistoricalCopy historyCopy);

    @Select("SELECT * FROM resume_historical_copy WHERE resume_history_id = #{resumeHistoryId} ORDER BY created_at DESC LIMIT 1")
    ResumeHistoricalCopy findLatestByResumeId(String resumeHistoryId);
}

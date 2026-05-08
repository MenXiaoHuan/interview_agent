package com.multimodal.interview.mapper;

import com.multimodal.interview.dto.BlessingResponse;
import com.multimodal.interview.entity.Blessing;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Blessing 数据访问接口。
 */
@Mapper
public interface BlessingMapper {
    @Insert("INSERT INTO blessing (user_id, content, type, status, created_at, updated_at) VALUES (#{userId}, #{content}, #{type}, 'ACTIVE', NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Blessing blessing);

    @Select("SELECT * FROM blessing WHERE id = #{id}")
    Blessing findById(Long id);

    @Update("UPDATE blessing SET content = #{content}, type = #{type}, status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int update(Blessing blessing);

    @Delete("DELETE FROM blessing WHERE id = #{id}")
    int delete(Long id);

    @Select("<script>" +
            "SELECT id, content, type, status, created_at AS createdAt, updated_at AS updatedAt FROM blessing " +
            "<where>" +
            "<if test=\"status != null\">AND status = #{status}</if>" +
            "<if test=\"type != null\">AND type = #{type}</if>" +
            "</where>" +
            "ORDER BY created_at DESC" +
            "</script>")
    List<BlessingResponse> list(@Param("status") String status, @Param("type") String type);
}

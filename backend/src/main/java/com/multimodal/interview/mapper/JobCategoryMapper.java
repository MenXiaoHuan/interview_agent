package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.JobCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * JobCategory 数据访问接口。
 */
@Mapper
public interface JobCategoryMapper {

    /**
     * 获取所有启用的分类，按层级和排序号排序
     */
    @Select("SELECT id, name, level, parent_id as parentId, " +
            "sort_order as sortOrder, status, " +
            "created_at as createdAt, updated_at as updatedAt " +
            "FROM job_category " +
            "WHERE status = 1 " +
            "ORDER BY level ASC, sort_order ASC")
    List<JobCategory> selectAllEnabled();

    /**
     * 根据ID查询分类
     */
    @Select("SELECT id, name, level, parent_id as parentId, " +
            "sort_order as sortOrder, status, " +
            "created_at as createdAt, updated_at as updatedAt " +
            "FROM job_category WHERE id = #{id}")
    JobCategory selectById(Long id);

    /**
     * 查询指定父分类下的所有启用的子分类
     */
    @Select("SELECT id, name, level, parent_id as parentId, " +
            "sort_order as sortOrder, status, " +
            "created_at as createdAt, updated_at as updatedAt " +
            "FROM job_category " +
            "WHERE parent_id = #{parentId} AND status = 1 " +
            "ORDER BY sort_order ASC")
    List<JobCategory> selectByParentId(Long parentId);

    /**
     * 根据名称和层级查询分类
     */
    @Select("SELECT id, name, level, parent_id as parentId, " +
            "sort_order as sortOrder, status, " +
            "created_at as createdAt, updated_at as updatedAt " +
            "FROM job_category " +
            "WHERE name = #{name} AND level = #{level} AND status = 1")
    JobCategory selectByNameAndLevel(String name, Integer level);

    /**
     * 根据名称和父分类ID查询分类
     */
    @Select("SELECT id, name, level, parent_id as parentId, " +
            "sort_order as sortOrder, status, " +
            "created_at as createdAt, updated_at as updatedAt " +
            "FROM job_category " +
            "WHERE name = #{name} AND parent_id = #{parentId} AND status = 1")
    JobCategory selectByNameAndParentId(String name, Long parentId);

    /**
     * 根据名称和层级查询分类（排除指定ID）
     */
    @Select("SELECT id, name, level, parent_id as parentId, " +
            "sort_order as sortOrder, status, " +
            "created_at as createdAt, updated_at as updatedAt " +
            "FROM job_category " +
            "WHERE name = #{name} AND level = #{level} AND id != #{excludeId} AND status = 1")
    JobCategory selectByNameAndLevelExcludeId(String name, Integer level, Long excludeId);

    /**
     * 插入分类
     */
    @org.apache.ibatis.annotations.Insert("INSERT INTO job_category (name, level, parent_id, sort_order, status, created_at, updated_at) " +
            "VALUES (#{name}, #{level}, #{parentId}, #{sortOrder}, #{status}, NOW(), NOW())")
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(JobCategory category);

    /**
     * 更新分类
     */
    @org.apache.ibatis.annotations.Update("UPDATE job_category SET name = #{name}, sort_order = #{sortOrder}, updated_at = NOW() " +
            "WHERE id = #{id}")
    int updateById(JobCategory category);

    /**
     * 删除分类
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM job_category WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 删除指定父分类下的所有子分类
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM job_category WHERE parent_id = #{parentId}")
    int deleteByParentId(Long parentId);
}

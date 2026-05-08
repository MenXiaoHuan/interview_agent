package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.Job;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Job 数据访问接口。
 */
@Mapper
public interface JobMapper {

    @Select("SELECT * FROM job WHERE category_id = #{categoryId} AND status = 1")
    List<Job> selectByCategoryId(Long categoryId);

    @Select("SELECT j.*, " +
            "c.id as 'category.id', " +
            "c.name as 'category.name', " +
            "c.level as 'category.level', " +
            "c.parent_id as 'category.parentId' " +
            "FROM job j " +
            "LEFT JOIN job_category c ON j.category_id = c.id " +
            "WHERE j.id = #{id}")
    Job selectByIdWithCategory(Long id);

    @Select("SELECT id, category_id, name, description, requirements, status, created_at, updated_at FROM job WHERE id = #{id}")
    Job getJobById(Long id);

    /**
     * 根据名称和分类ID查询岗位
     */
    @Select("SELECT id, category_id, name, description, requirements, status, created_at, updated_at " +
            "FROM job WHERE name = #{name} AND category_id = #{categoryId} AND status = 1")
    Job selectByNameAndCategoryId(String name, Long categoryId);

    /**
     * 根据名称和分类ID查询岗位（排除指定ID）
     */
    @Select("SELECT id, category_id, name, description, requirements, status, created_at, updated_at " +
            "FROM job WHERE name = #{name} AND category_id = #{categoryId} AND id != #{excludeId} AND status = 1")
    Job selectByNameAndCategoryIdExcludeId(String name, Long categoryId, Long excludeId);

    /**
     * 插入岗位
     */
    @Insert("INSERT INTO job (category_id, name, description, requirements, status, created_at, updated_at) " +
            "VALUES (#{categoryId}, #{name}, #{description}, #{requirements}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Job job);

    /**
     * 更新岗位
     */
    @Update("UPDATE job SET name = #{name}, description = #{description}, " +
            "requirements = #{requirements}, status = #{status},updated_at = NOW() WHERE id = #{id}")
    int updateById(Job job);

    /**
     * 删除岗位
     */
    @Delete("DELETE FROM job WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 根据分类ID删除岗位
     */
    @Delete("DELETE FROM job WHERE category_id = #{categoryId}")
    int deleteByCategoryId(Long categoryId);

    /**
     * 获取所有启用的岗位列表，包括二级岗位ID
     */
    @Select("SELECT j.id, j.category_id, j.name, j.description, j.requirements, j.status, " +
            "j.created_at, j.updated_at, " +
            "c.id as 'category.id', c.name as 'category.name', c.level as 'category.level', " +
            "c.parent_id as 'category.parentId', c.sort_order as 'category.sortOrder', " +
            "c.status as 'category.status', c.created_at as 'category.createdAt', " +
            "c.updated_at as 'category.updatedAt' " +
            "FROM job j " +
            "LEFT JOIN job_category c ON j.category_id = c.id " +
            "ORDER BY j.category_id ASC, j.id ASC")
    List<Job> selectAllJobsWithCategory();
}

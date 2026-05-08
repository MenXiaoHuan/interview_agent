package com.multimodal.interview.service;

import com.multimodal.interview.dto.JobCategoryCreateRequest;
import com.multimodal.interview.dto.JobCategoryUpdateRequest;
import com.multimodal.interview.dto.JobCreateRequest;
import com.multimodal.interview.dto.JobUpdateRequest;
import com.multimodal.interview.entity.Job;
import com.multimodal.interview.entity.JobCategory;


import java.util.List;
import java.util.Map;

/**
 * 岗位管理服务接口。
 */
public interface JobService {
    /**
     * 获取所有分类的树形结构
     * 包括一级分类和二级分类，按层级和排序号排序
     *
     * @return 分类树形结构列表
     */
    List<JobCategory> getCategoryTree();

    /**
     * 获取指定分类下的所有岗位
     *
     * @param categoryId 分类ID
     * @return 岗位列表
     */
    List<Job> getJobsByCategory(Long categoryId);

    /**
     * 获取岗位详情
     *
     * @param id 岗位ID
     * @return 岗位详情
     */
    Job getJobDetail(Long id);

    /**
     * 获取岗位信息
     *
     * @param jobId 岗位ID
     * @return 岗位信息
     */
    Map<String, String> getJobInfo(Long jobId);

    /**
     * 创建一级岗位分类
     *
     * @param request 创建请求
     * @return 创建的分类
     */
    JobCategory createFirstLevelCategory(JobCategoryCreateRequest request);

    /**
     * 创建二级岗位分类
     *
     * @param request 创建请求
     * @return 创建的分类
     */
    JobCategory createSecondLevelCategory(JobCategoryCreateRequest request);

    /**
     * 更新岗位分类
     *
     * @param request 更新请求
     * @return 更新后的分类
     */
    JobCategory updateCategory(JobCategoryUpdateRequest request);

    /**
     * 删除一级岗位分类（同时删除其下的所有二级分类和具体岗位）
     *
     * @param categoryId 一级分类ID
     */
    void deleteFirstLevelCategory(Long categoryId);

    /**
     * 删除二级岗位分类（同时删除其下的所有具体岗位）
     *
     * @param categoryId 二级分类ID
     */
    void deleteSecondLevelCategory(Long categoryId);

    /**
     * 创建具体岗位
     *
     * @param request 创建请求
     * @return 创建的岗位
     */
    Job createJob(JobCreateRequest request);

    /**
     * 更新具体岗位
     *
     * @param request 更新请求
     * @return 更新后的岗位
     */
    Job updateJob(JobUpdateRequest request);

    /**
     * 删除具体岗位
     *
     * @param jobId 岗位ID
     */
    void deleteJob(Long jobId);

    /**
     * 获取所有岗位列表，包括二级岗位ID
     *
     * @return 岗位列表
     */
    List<Job> getAllJobsWithCategory();

    /**
     * 根据二级岗位ID查询二级岗位名称
     *
     * @param categoryId 二级岗位ID
     * @return 二级岗位名称
     */
    String getCategoryNameById(Long categoryId);
}

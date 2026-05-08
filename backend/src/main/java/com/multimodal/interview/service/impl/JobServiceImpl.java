package com.multimodal.interview.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimodal.interview.dto.JobCategoryCreateRequest;
import com.multimodal.interview.dto.JobCategoryUpdateRequest;
import com.multimodal.interview.dto.JobCreateRequest;
import com.multimodal.interview.dto.JobUpdateRequest;
import com.multimodal.interview.cache.ReadCacheService;
import com.multimodal.interview.entity.Job;
import com.multimodal.interview.entity.JobCategory;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.mapper.JobCategoryMapper;
import com.multimodal.interview.mapper.JobMapper;
import com.multimodal.interview.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 岗位管理服务实现。
 */
@Slf4j
@Service
public class JobServiceImpl implements JobService {

    private static final String CACHE_PREFIX = "read-cache:job:";
    private static final Duration HOT_LIST_TTL = Duration.ofMinutes(10);
    private static final Duration DETAIL_TTL = Duration.ofMinutes(8);
    private static final Duration NULL_TTL = Duration.ofMinutes(2);

    private final JobMapper jobMapper;
    private final JobCategoryMapper jobCategoryMapper;
    private final ReadCacheService readCacheService;
    private final ObjectMapper objectMapper;

    public JobServiceImpl(
            JobMapper jobMapper,
            JobCategoryMapper jobCategoryMapper,
            ReadCacheService readCacheService,
            ObjectMapper objectMapper
    ) {
        this.jobMapper = jobMapper;
        this.jobCategoryMapper = jobCategoryMapper;
        this.readCacheService = readCacheService;
        this.objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<JobCategory> getCategoryTree() {
        List<JobCategory> cached = readCacheService.getOrLoad(
                CACHE_PREFIX + "category-tree",
                objectMapper.getTypeFactory().constructCollectionType(List.class, JobCategory.class),
                HOT_LIST_TTL,
                NULL_TTL,
                this::loadCategoryTreeFromDb
        );
        return cached == null ? new ArrayList<>() : cached;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Job> getJobsByCategory(Long categoryId) {
        List<Job> cached = readCacheService.getOrLoad(
                CACHE_PREFIX + "jobs-by-category:" + categoryId,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Job.class),
                HOT_LIST_TTL,
                NULL_TTL,
                () -> loadJobsByCategoryFromDb(categoryId)
        );
        return cached == null ? List.of() : cached;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Job getJobDetail(Long id) {
        Job job = readCacheService.getOrLoad(
                CACHE_PREFIX + "detail:" + id,
                objectMapper.getTypeFactory().constructType(Job.class),
                DETAIL_TTL,
                NULL_TTL,
                () -> loadJobDetailFromDb(id)
        );
        if (job == null) {
            throw BusinessException.notFound("岗位不存在");
        }
        return job;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getJobInfo(Long jobId) {
        //从数据库获取职位信息
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("id", jobId.toString());
        jobInfo.put("name", "示例职位");
        jobInfo.put("description", "这是一个示例职位描述");
        jobInfo.put("requirements", "1. 要求1\n2. 要求2\n3. 要求3");
        return jobInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public JobCategory createFirstLevelCategory(JobCategoryCreateRequest request) {
        // 验证层级
        if (request.getLevel() != 1) {
            throw BusinessException.badRequest("一级分类的层级必须为1");
        }

        // 验证分类名称是否已存在
        JobCategory existingCategory = jobCategoryMapper.selectByNameAndLevel(request.getName(), 1);
        if (existingCategory != null) {
            throw BusinessException.badRequest("一级分类名称已存在");
        }

        JobCategory category = new JobCategory();
        category.setName(request.getName());
        category.setLevel(1);
        category.setParentId(null);
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setStatus(1);

        int rows = jobCategoryMapper.insert(category);
        if (rows != 1) {
            throw BusinessException.internalError("创建一级分类失败");
        }

        log.info("Created first-level category: {}", category.getName());
        evictJobReadCaches();
        return category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public JobCategory createSecondLevelCategory(JobCategoryCreateRequest request) {
        // 验证层级
        if (request.getLevel() != 2) {
            throw BusinessException.badRequest("二级分类的层级必须为2");
        }

        // 验证父分类是否存在
        if (request.getParentId() == null) {
            throw BusinessException.badRequest("二级分类必须指定父分类ID");
        }

        JobCategory parentCategory = jobCategoryMapper.selectById(request.getParentId());
        if (parentCategory == null || parentCategory.getLevel() != 1) {
            throw BusinessException.badRequest("父分类不存在或不是一级分类");
        }

        // 验证分类名称是否已存在
        JobCategory existingCategory = jobCategoryMapper.selectByNameAndParentId(request.getName(), request.getParentId());
        if (existingCategory != null) {
            throw BusinessException.badRequest("该父分类下已存在同名二级分类");
        }

        JobCategory category = new JobCategory();
        category.setName(request.getName());
        category.setLevel(2);
        category.setParentId(request.getParentId());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setStatus(1);

        int rows = jobCategoryMapper.insert(category);
        if (rows != 1) {
            throw BusinessException.internalError("创建二级分类失败");
        }

        log.info("Created second-level category: {} under parent: {}", category.getName(), parentCategory.getName());
        evictJobReadCaches();
        return category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public JobCategory updateCategory(JobCategoryUpdateRequest request) {
        JobCategory existingCategory = jobCategoryMapper.selectById(request.getId());
        if (existingCategory == null) {
            throw BusinessException.notFound("分类不存在");
        }

        // 验证分类名称是否已存在（排除自身）
        JobCategory duplicateCategory = jobCategoryMapper.selectByNameAndLevelExcludeId(
                request.getName(), existingCategory.getLevel(), request.getId());
        if (duplicateCategory != null) {
            throw BusinessException.badRequest("分类名称已存在");
        }

        existingCategory.setName(request.getName());
        if (request.getSortOrder() != null) {
            existingCategory.setSortOrder(request.getSortOrder());
        }

        int rows = jobCategoryMapper.updateById(existingCategory);
        if (rows != 1) {
            throw BusinessException.internalError("更新分类失败");
        }

        log.info("Updated category: {}", existingCategory.getName());
        evictJobReadCaches();
        return existingCategory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteFirstLevelCategory(Long categoryId) {
        JobCategory category = jobCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }

        if (category.getLevel() != 1) {
            throw BusinessException.badRequest("只能删除一级分类");
        }

        // 获取该一级分类下的所有二级分类
        List<JobCategory> secondLevelCategories = jobCategoryMapper.selectByParentId(categoryId);

        // 删除所有二级分类下的岗位
        for (JobCategory secondLevel : secondLevelCategories) {
            jobMapper.deleteByCategoryId(secondLevel.getId());
        }

        // 删除所有二级分类
        jobCategoryMapper.deleteByParentId(categoryId);

        // 删除一级分类
        int rows = jobCategoryMapper.deleteById(categoryId);
        if (rows != 1) {
            throw BusinessException.internalError("删除一级分类失败");
        }

        log.info("Deleted first-level category: {} with {} second-level categories",
                category.getName(), secondLevelCategories.size());
        evictJobReadCaches();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteSecondLevelCategory(Long categoryId) {
        JobCategory category = jobCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }

        if (category.getLevel() != 2) {
            throw BusinessException.badRequest("只能删除二级分类");
        }

        // 删除该二级分类下的所有岗位
        jobMapper.deleteByCategoryId(categoryId);

        // 删除二级分类
        int rows = jobCategoryMapper.deleteById(categoryId);
        if (rows != 1) {
            throw BusinessException.internalError("删除二级分类失败");
        }

        log.info("Deleted second-level category: {}", category.getName());
        evictJobReadCaches();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Job createJob(JobCreateRequest request) {
        // 验证分类是否存在且为二级分类
        JobCategory category = jobCategoryMapper.selectById(request.getCategoryId());
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }

        if (category.getLevel() != 2) {
            throw BusinessException.badRequest("只能在二级分类下创建岗位");
        }

        // 验证岗位名称是否已存在
        Job existingJob = jobMapper.selectByNameAndCategoryId(request.getName(), request.getCategoryId());
        if (existingJob != null) {
            throw BusinessException.badRequest("该分类下已存在同名岗位");
        }

        Job job = new Job();
        job.setCategoryId(request.getCategoryId());
        job.setName(request.getName());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setStatus(1);

        int rows = jobMapper.insert(job);
        if (rows != 1) {
            throw BusinessException.internalError("创建岗位失败");
        }

        log.info("Created job: {} in category: {}", job.getName(), category.getName());
        evictJobReadCaches();
        return job;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Job updateJob(JobUpdateRequest request) {
        Job existingJob = jobMapper.getJobById(request.getId());
        if (existingJob == null) {
            throw BusinessException.notFound("岗位不存在");
        }

        // 验证岗位名称是否已存在（排除自身）
        Job duplicateJob = jobMapper.selectByNameAndCategoryIdExcludeId(
                request.getName(), existingJob.getCategoryId(), request.getId());
        if (duplicateJob != null) {
            throw BusinessException.badRequest("该分类下已存在同名岗位");
        }

        existingJob.setName(request.getName());
        existingJob.setDescription(request.getDescription());
        existingJob.setRequirements(request.getRequirements());
        existingJob.setStatus(request.getStatus());

        int rows = jobMapper.updateById(existingJob);
        if (rows != 1) {
            throw BusinessException.internalError("更新岗位失败");
        }

        log.info("Updated job: {}", existingJob.getName());
        evictJobReadCaches();
        return existingJob;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteJob(Long jobId) {
        Job job = jobMapper.getJobById(jobId);
        if (job == null) {
            throw BusinessException.notFound("岗位不存在");
        }

        int rows = jobMapper.deleteById(jobId);
        if (rows != 1) {
            throw BusinessException.internalError("删除岗位失败");
        }

        log.info("Deleted job: {}", job.getName());
        evictJobReadCaches();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Job> getAllJobsWithCategory() {
        List<Job> cached = readCacheService.getOrLoad(
                CACHE_PREFIX + "all-jobs",
                objectMapper.getTypeFactory().constructCollectionType(List.class, Job.class),
                HOT_LIST_TTL,
                NULL_TTL,
                this::loadAllJobsWithCategoryFromDb
        );
        return cached == null ? List.of() : cached;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCategoryNameById(Long categoryId) {
        JobCategory category = jobCategoryMapper.selectById(categoryId);
        if (category == null) {
            log.warn("Category not found: {}", categoryId);
            throw BusinessException.notFound("岗位分类不存在");
        }

        // 验证是否是二级分类
        if (category.getLevel() != 2) {
            log.warn("Invalid category level: {}, categoryId: {}", category.getLevel(), categoryId);
            throw BusinessException.badRequest("只能查询二级岗位分类");
        }

        log.debug("Found category name: {} for categoryId: {}", category.getName(), categoryId);
        return category.getName();
    }

    private List<JobCategory> loadCategoryTreeFromDb() {
        List<JobCategory> allCategories = jobCategoryMapper.selectAllEnabled();

        if (CollectionUtils.isEmpty(allCategories)) {
            log.info("No job categories found");
            return new ArrayList<>();
        }

        Map<Integer, List<JobCategory>> levelMap = allCategories.stream()
                .collect(Collectors.groupingBy(JobCategory::getLevel));
        List<JobCategory> firstLevel = levelMap.getOrDefault(1, new ArrayList<>());
        List<JobCategory> secondLevel = levelMap.getOrDefault(2, new ArrayList<>());
        Map<Long, List<JobCategory>> parentChildMap = secondLevel.stream()
                .collect(Collectors.groupingBy(JobCategory::getParentId));

        firstLevel.forEach(category -> {
            List<JobCategory> children = parentChildMap.get(category.getId());
            if (!CollectionUtils.isEmpty(children)) {
                category.setChildren(children);
            }
        });

        log.debug("Built category tree with {} first-level categories", firstLevel.size());
        return firstLevel;
    }

    private List<Job> loadJobsByCategoryFromDb(Long categoryId) {
        JobCategory category = jobCategoryMapper.selectById(categoryId);
        if (category == null) {
            log.warn("Category not found: {}", categoryId);
            throw BusinessException.notFound("分类不存在");
        }
        if (category.getLevel() != 2) {
            log.warn("Invalid category level: {}, categoryId: {}", category.getLevel(), categoryId);
            throw BusinessException.badRequest("只能查询二级分类下的岗位");
        }
        List<Job> jobs = jobMapper.selectByCategoryId(categoryId);
        log.debug("Found {} jobs in category {}", jobs.size(), categoryId);
        return jobs;
    }

    private Job loadJobDetailFromDb(Long id) {
        Job job = jobMapper.selectByIdWithCategory(id);
        if (job == null) {
            log.warn("Job not found: {}", id);
        }
        return job;
    }

    private List<Job> loadAllJobsWithCategoryFromDb() {
        List<Job> jobs = jobMapper.selectAllJobsWithCategory();
        log.debug("Found {} jobs with category information", jobs.size());
        return jobs;
    }

    private void evictJobReadCaches() {
        readCacheService.evict(CACHE_PREFIX + "category-tree");
        readCacheService.evict(CACHE_PREFIX + "all-jobs");
        readCacheService.evictByPrefix(CACHE_PREFIX + "jobs-by-category:");
        readCacheService.evictByPrefix(CACHE_PREFIX + "detail:");
    }
} 

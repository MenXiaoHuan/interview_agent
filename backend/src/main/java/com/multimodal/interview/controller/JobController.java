package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.dto.JobCategoryCreateRequest;
import com.multimodal.interview.dto.JobCategoryUpdateRequest;
import com.multimodal.interview.dto.JobCreateRequest;
import com.multimodal.interview.dto.JobUpdateRequest;
import com.multimodal.interview.entity.Job;
import com.multimodal.interview.entity.JobCategory;
import com.multimodal.interview.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位与岗位分类管理接口控制器。
 */
@Validated
@Tag(name = "岗位管理", description = "岗位分类和岗位信息相关接口")
@RestController
@RequestMapping("/api")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }
    /**
     * 获取岗位分类树形结构。
     * @return 统一响应体
     */
    @Operation(summary = "获取岗位分类树形结构",
            description = "获取所有启用的岗位分类，包括一级分类和二级分类，按层级和排序号排序")
    @GetMapping("/job-categories/tree")
    public ApiResponse<List<JobCategory>> getCategoryTree(){
        return ApiResponse.success(jobService.getCategoryTree());
    }
    /**
     * 获取分类下的岗位列表。
     *
     * @param categoryId 分类 ID
     * @return 统一响应体
     */
    @Operation(summary = "获取分类下的岗位列表",
            description = "获取指定二级分类下的所有启用的岗位列表")
    @GetMapping("/job")
    public ApiResponse<List<Job>> getJobs(
            @Parameter(description = "二级分类ID", required = true)
            @NotNull(message = "分类ID不能为空")
            @RequestParam Long categoryId){
        return ApiResponse.success(jobService.getJobsByCategory(categoryId));
    }
    /**
     * 获取岗位详情。
     *
     * @param id 主键 ID
     * @return 统一响应体
     */
    @Operation(summary = "获取岗位详情",
            description = "根据岗位ID获取岗位的详细信息，包括所属分类信息")
    @GetMapping("/job/{id}")
    public ApiResponse<Job> getJobDetail(
            @Parameter(description = "岗位ID", required = true)
            @NotNull(message = "岗位ID不能为空")
            @PathVariable Long id){
        return ApiResponse.success(jobService.getJobDetail(id));
    }

    // ==================== 岗位分类管理接口 ====================
    /**
     * 创建一级岗位分类。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "创建一级岗位分类",
            description = "创建一级岗位分类，如：人工智能、大数据等")
    @PostMapping("/job-categories/first-level")
    public ApiResponse<JobCategory> createFirstLevelCategory(@Valid @RequestBody JobCategoryCreateRequest request){
        JobCategory category = jobService.createFirstLevelCategory(request);
        return ApiResponse.success("创建一级岗位分类成功", category);
    }
    /**
     * 创建二级岗位分类。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "创建二级岗位分类",
            description = "创建二级岗位分类，需要指定父分类ID")
    @PostMapping("/job-categories/second-level")
    public ApiResponse<JobCategory> createSecondLevelCategory(@Valid @RequestBody JobCategoryCreateRequest request){
        JobCategory category = jobService.createSecondLevelCategory(request);
        return ApiResponse.success("创建二级岗位分类成功", category);
    }
    /**
     * 更新岗位分类。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "更新岗位分类",
            description = "更新岗位分类的名称和排序")
    @PutMapping("/job-categories")
    public ApiResponse<JobCategory> updateCategory(@Valid @RequestBody JobCategoryUpdateRequest request){
        JobCategory category = jobService.updateCategory(request);
        return ApiResponse.success("更新岗位分类成功", category);
    }
    /**
     * 删除一级岗位分类。
     *
     * @param categoryId 分类 ID
     * @return 统一响应体
     */
    @Operation(summary = "删除一级岗位分类",
            description = "删除一级岗位分类，同时删除其下的所有二级分类和具体岗位")
    @DeleteMapping("/job-categories/first-level/{categoryId}")
    public ApiResponse<Void> deleteFirstLevelCategory(
            @Parameter(description = "一级分类ID", required = true)
            @NotNull(message = "分类ID不能为空")
            @PathVariable Long categoryId){
        jobService.deleteFirstLevelCategory(categoryId);
        return ApiResponse.success("删除一级岗位分类成功", null);
    }
    /**
     * 删除二级岗位分类。
     *
     * @param categoryId 分类 ID
     * @return 统一响应体
     */
    @Operation(summary = "删除二级岗位分类",
            description = "删除二级岗位分类，同时删除其下的所有具体岗位")
    @DeleteMapping("/job-categories/second-level/{categoryId}")
    public ApiResponse<Void> deleteSecondLevelCategory(
            @Parameter(description = "二级分类ID", required = true)
            @NotNull(message = "分类ID不能为空")
            @PathVariable Long categoryId){
        jobService.deleteSecondLevelCategory(categoryId);
        return ApiResponse.success("删除二级岗位分类成功", null);
    }
    /**
     * 根据二级岗位ID查询岗位分类名称。
     *
     * @param categoryId 分类 ID
     * @return 统一响应体
     */
    @Operation(summary = "根据二级岗位ID查询岗位分类名称",
            description = "根据二级岗位分类ID查询对应的岗位分类名称")
    @GetMapping("/job-categories/{categoryId}/name")
    public ApiResponse<String> getCategoryNameById(
            @Parameter(description = "二级岗位分类ID", required = true)
            @NotNull(message = "分类ID不能为空")
            @PathVariable Long categoryId){
        return ApiResponse.success(jobService.getCategoryNameById(categoryId));
    }

    // ==================== 具体岗位管理接口 ====================
    /**
     * 创建具体岗位。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "创建具体岗位",
            description = "在指定二级分类下创建具体岗位")
    @PostMapping("/jobs")
    public ApiResponse<Job> createJob(@Valid @RequestBody JobCreateRequest request){
        Job job = jobService.createJob(request);
        return ApiResponse.success("创建岗位成功", job);
    }
    /**
     * 更新具体岗位。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "更新具体岗位",
            description = "更新具体岗位的信息")
    @PutMapping("/jobs")
    public ApiResponse<Job> updateJob(@Valid @RequestBody JobUpdateRequest request){
        Job job = jobService.updateJob(request);
        return ApiResponse.success("更新岗位成功", job);
    }
    /**
     * 获取所有岗位列表。
     * @return 统一响应体
     */
    @Operation(summary = "获取所有岗位列表",
            description = "获取所有启用的岗位列表，包括二级岗位ID和分类信息")
    @GetMapping("/jobs")
    public ApiResponse<List<Job>> getAllJobs(){
        return ApiResponse.success(jobService.getAllJobsWithCategory());
    }
    /**
     * 删除具体岗位。
     *
     * @param jobId 岗位 ID
     * @return 统一响应体
     */
    @Operation(summary = "删除具体岗位",
            description = "删除指定的具体岗位")
    @DeleteMapping("/jobs/{jobId}")
    public ApiResponse<Void> deleteJob(
            @Parameter(description = "岗位ID", required = true)
            @NotNull(message = "岗位ID不能为空")
            @PathVariable Long jobId){
        jobService.deleteJob(jobId);
        return ApiResponse.success("删除岗位成功", null);
    }
} 

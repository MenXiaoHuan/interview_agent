package com.multimodal.interview.reactagent.tool;

import com.multimodal.interview.entity.Job;
import com.multimodal.interview.entity.JobCategory;
import com.multimodal.interview.service.JobService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobAgentTool {

    private final JobService jobService;

    public JobAgentTool(JobService jobService) {
        this.jobService = jobService;
    }

    @Tool(name = "job_get_category_tree", description = "获取岗位分类树（一级/二级分类），按层级与排序返回")
    public List<JobCategory> getCategoryTree() {
        return jobService.getCategoryTree();
    }

    @Tool(name = "job_get_jobs_by_category", description = "根据二级岗位分类ID获取该分类下的启用岗位列表")
    public List<Job> getJobsByCategory(@ToolParam(description = "二级岗位分类ID") Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            return List.of();
        }
        return jobService.getJobsByCategory(categoryId);
    }

    @Tool(name = "job_get_detail", description = "根据岗位ID获取岗位详情")
    public Job getJobDetail(@ToolParam(description = "岗位ID") Long jobId) {
        if (jobId == null || jobId <= 0) {
            return null;
        }
        return jobService.getJobDetail(jobId);
    }

    @Tool(name = "job_get_all_jobs", description = "获取所有启用的岗位列表")
    public List<Job> getAllJobs() {
        return jobService.getAllJobsWithCategory();
    }

    @Tool(name = "job_get_category_name", description = "根据二级岗位分类ID查询岗位分类名称")
    public String getCategoryName(@ToolParam(description = "二级岗位分类ID") Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            return "";
        }
        return jobService.getCategoryNameById(categoryId);
    }
}

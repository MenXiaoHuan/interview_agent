package com.multimodal.interview.dto;

import com.multimodal.interview.entity.Job;
import com.multimodal.interview.entity.JobCategory;

import java.time.LocalDateTime;

public record JobResponse(
        Long id,
        Long categoryId,
        String name,
        String description,
        String requirements,
        Integer status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        JobCategory category
) {
    public static JobResponse from(Job job) {
        if (job == null) {
            return null;
        }
        return new JobResponse(
                job.getId(),
                job.getCategoryId(),
                job.getName(),
                job.getDescription(),
                job.getRequirements(),
                job.getStatus(),
                job.getCreatedAt(),
                job.getUpdatedAt(),
                job.getCategory()
        );
    }
}

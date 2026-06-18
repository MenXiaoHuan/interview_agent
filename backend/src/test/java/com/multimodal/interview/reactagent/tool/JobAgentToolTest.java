package com.multimodal.interview.reactagent.tool;

import com.multimodal.interview.service.JobService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JobAgentToolTest {

    @Test
    void delegatesReadOnlyCategoryTreeToService() {
        JobService jobService = mock(JobService.class);
        JobAgentTool tool = new JobAgentTool(jobService);

        tool.getCategoryTree();

        verify(jobService).getCategoryTree();
    }
}

import request from '@/utils/api/request';
import { BASE_URL } from '@/utils/api/api-config';

const AIVIEW_JOB_INSIGHTS = (jobId) => `${BASE_URL}/api/aiview-insights/job/${jobId}/last-7-days`;

export function fetchAiviewJobInsights(jobId) {
  return request({
    url: AIVIEW_JOB_INSIGHTS(jobId),
    method: 'get',
  });
}

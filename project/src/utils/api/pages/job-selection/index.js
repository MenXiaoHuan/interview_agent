import request from '@/utils/api/request';
import { BASE_URL } from '@/utils/api/api-config';

const JOB_DETAIL = (id) => `${BASE_URL}/api/job/${id}`;

export function getJobInfo(jobId) {
  return request({
    url: JOB_DETAIL(jobId),
    method: 'get',
  });
}

import request from '@/utils/api/request';
import { BASE_URL } from '@/utils/api/api-config';

export const API = {
  USER: {
    SURPRISE_MODE: `${BASE_URL}/api/user/surprisemode`,
    NICKNAME: `${BASE_URL}/api/user/nickname`,
  },
  FEEDBACK: {
    LIST: `${BASE_URL}/api/feedback`,
    DETAIL: (id) => `${BASE_URL}/api/feedback/${id}`,
    REPLY: (id) => `${BASE_URL}/api/feedback/${id}/reply`,
    MY_STATUS: `${BASE_URL}/api/feedback/my/status`,
  },
  BLESSING: {
    LIST: `${BASE_URL}/api/blessings`,
    CREATE: `${BASE_URL}/api/blessings`,
    UPDATE: (id) => `${BASE_URL}/api/blessings/${id}`,
    DELETE: (id) => `${BASE_URL}/api/blessings/${id}`,
  },
};

export function getBlessings(params = {}) {
  return request({
    url: API.BLESSING.LIST,
    method: 'get',
    params,
  });
}

export function createBlessing(data) {
  return request({
    url: API.BLESSING.CREATE,
    method: 'post',
    data,
    headers: { 'Content-Type': 'application/json' },
  });
}

export function updateBlessing(id, data) {
  return request({
    url: API.BLESSING.UPDATE(id),
    method: 'put',
    data,
    headers: { 'Content-Type': 'application/json' },
  });
}

export function deleteBlessing(id) {
  return request({
    url: API.BLESSING.DELETE(id),
    method: 'delete',
  });
}

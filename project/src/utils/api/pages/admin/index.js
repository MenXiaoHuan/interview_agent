import request from '@/utils/api/request';
import { BASE_URL } from '@/utils/api/api-config';

export const API = {
  USER: {
    LIST: `${BASE_URL}/api/user/list`,
    DELETE: (username) => `${BASE_URL}/api/user/delete/${username}`,
    UPDATE_NICKNAME: `${BASE_URL}/api/user/update/nickname`,
    UPDATE_EMAIL: `${BASE_URL}/api/user/update/email`,
    UPDATE_PHONE: `${BASE_URL}/api/user/update/phone`,
    UPDATE_GENDER: `${BASE_URL}/api/user/update/gender`,
    UPDATE_USER_TYPE: `${BASE_URL}/api/user/update/userType`,
  },
  JOB: {
    CATEGORIES: `${BASE_URL}/api/job-categories/tree`,
    LIST: `${BASE_URL}/api/jobs`,
    DETAIL: (id) => `${BASE_URL}/api/job/${id}`,
    CREATE_FIRST_LEVEL: `${BASE_URL}/api/job-categories/first-level`,
    CREATE_SECOND_LEVEL: `${BASE_URL}/api/job-categories/second-level`,
    UPDATE_CATEGORY: `${BASE_URL}/api/job-categories`,
    DELETE_FIRST_LEVEL: (categoryId) => `${BASE_URL}/api/job-categories/first-level/${categoryId}`,
    DELETE_SECOND_LEVEL: (categoryId) => `${BASE_URL}/api/job-categories/second-level/${categoryId}`,
    JOBS_BY_CATEGORY: `${BASE_URL}/api/job`,
  },
};

export function getUserList(page, size) {
  return request({
    url: API.USER.LIST,
    method: 'get',
    params: { page, size },
  });
}

export function deleteUser(username) {
  return request({
    url: API.USER.DELETE(username),
    method: 'delete',
  });
}

export function updateUserNickname(username, nickname) {
  return request({
    url: API.USER.UPDATE_NICKNAME,
    method: 'put',
    data: { username, nickname },
  });
}

export function updateUserEmail(username, email) {
  return request({
    url: API.USER.UPDATE_EMAIL,
    method: 'put',
    data: { username, email },
  });
}

export function updateUserPhone(username, phone) {
  return request({
    url: API.USER.UPDATE_PHONE,
    method: 'put',
    data: { username, phone },
  });
}

export function updateUserGender(username, gender) {
  return request({
    url: API.USER.UPDATE_GENDER,
    method: 'put',
    data: { username, gender },
  });
}

export function updateUserType(username, userType) {
  return request({
    url: API.USER.UPDATE_USER_TYPE,
    method: 'put',
    data: { username, userType },
  });
}

export function getJobCategoriesTree() {
  return request({
    url: API.JOB.CATEGORIES,
    method: 'get',
  });
}

export function createFirstLevelCategory(data) {
  return request({
    url: API.JOB.CREATE_FIRST_LEVEL,
    method: 'post',
    data,
  });
}

export function createSecondLevelCategory(data) {
  return request({
    url: API.JOB.CREATE_SECOND_LEVEL,
    method: 'post',
    data,
  });
}

export function updateJobCategory(data) {
  return request({
    url: API.JOB.UPDATE_CATEGORY,
    method: 'put',
    data,
  });
}

export function deleteFirstLevelCategory(categoryId) {
  return request({
    url: API.JOB.DELETE_FIRST_LEVEL(categoryId),
    method: 'delete',
  });
}

export function deleteSecondLevelCategory(categoryId) {
  return request({
    url: API.JOB.DELETE_SECOND_LEVEL(categoryId),
    method: 'delete',
  });
}

export function getJobsByCategoryId(categoryId) {
  return request({
    url: API.JOB.JOBS_BY_CATEGORY,
    method: 'get',
    params: { categoryId },
  });
}

export function getJobList() {
  return request({
    url: API.JOB.LIST,
    method: 'GET',
  });
}

export function getJobDetail(id) {
  return request({
    url: API.JOB.DETAIL(id),
    method: 'GET',
  });
}

export function createJob(data) {
  return request({
    url: API.JOB.LIST,
    method: 'POST',
    data,
  });
}

export function updateJob(data) {
  return request({
    url: API.JOB.LIST,
    method: 'PUT',
    data,
  });
}

export function deleteJob(jobId) {
  return request({
    url: `${API.JOB.LIST}/${jobId}`,
    method: 'DELETE',
  });
}

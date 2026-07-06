import { defineStore } from 'pinia';
import request from '@/utils/api/request';
import { BASE_URL } from '@/utils/api/api-config';

const JOB_API = {
  CATEGORIES: `${BASE_URL}/api/job-categories/tree`,
  DETAIL: (jobId) => `${BASE_URL}/api/job/${jobId}`,
  JOBS_BY_CATEGORY: `${BASE_URL}/api/job`,
};

function getJobsByCategoryId(categoryId) {
  return request({
    url: JOB_API.JOBS_BY_CATEGORY,
    method: 'get',
    params: { categoryId },
  });
}

export const useJobStore = defineStore('job', {
  state: () => ({
    categoryTree: [], // 分类树形结构数据
    jobs: [], // 当前选中分类下的岗位列表
    currentCategory: null, // 当前选中的分类
    currentJob: null, // 当前选中的岗位详情
    loading: {
      categories: false,
      jobs: false,
      jobDetail: false
    },
    error: {
      categories: null,
      jobs: null,
      jobDetail: null
    }
  }),

  actions: {
    // 获取所有岗位分类（树形结构）
    async fetchCategories() {
      this.loading.categories = true;
      this.error.categories = null;
      try {
        const response = await request.get(JOB_API.CATEGORIES);
        if (response.code === 200) {
          // 保存树形结构数据
          this.categoryTree = Array.isArray(response.data) 
            ? response.data.filter(cat => cat.status === 1)
            : [];
        } else {
          throw new Error(response.message || '获取分类数据失败');
        }
      } catch (error) {
        console.error('获取岗位分类失败:', error);
        this.categoryTree = []; // 出错时重置为空数组
        this.error.categories = error.message || '获取分类数据失败';
        throw error;
      } finally {
        this.loading.categories = false;
      }
    },

    // 根据分类ID获取岗位列表
    async fetchJobsByCategory(categoryId) {
      if (!categoryId) {
        this.jobs = [];
        return;
      }

      this.loading.jobs = true;
      this.error.jobs = null;
      try {
        console.log('正在获取分类ID为', categoryId, '的岗位列表');
        const response = await getJobsByCategoryId(categoryId);
        
        console.log('岗位列表响应:', response);
        
        if (response.code === 200) {
          // 确保数据是数组并且只包含启用的岗位
          this.jobs = Array.isArray(response.data) 
            ? response.data.filter(job => job.status === 1)
            : [];
          console.log('过滤后的岗位列表:', this.jobs);
        } else {
          throw new Error(response.message || '获取岗位列表失败');
        }
      } catch (error) {
        console.error('获取岗位列表失败:', error);
        this.jobs = []; // 出错时重置为空数组
        this.error.jobs = error.message || '获取岗位列表失败';
        throw error;
      } finally {
        this.loading.jobs = false;
      }
    },

    // 获取岗位详情
    async fetchJobDetail(jobId) {
      if (!jobId) {
        this.currentJob = null;
        return;
      }

      this.loading.jobDetail = true;
      this.error.jobDetail = null;
      try {
        const response = await request.get(JOB_API.DETAIL(jobId));
        
        if (response.code === 200) {
          this.currentJob = response.data;
        } else {
          throw new Error(response.message || '获取岗位详情失败');
        }
      } catch (error) {
        console.error('获取岗位详情失败:', error);
        this.currentJob = null; // 出错时重置为null
        this.error.jobDetail = error.message || '获取岗位详情失败';
        throw error;
      } finally {
        this.loading.jobDetail = false;
      }
    },

    // 设置当前选中的分类
    async setCurrentCategory(category) {
      this.currentCategory = category;
      if (category && category.level === 2) { // 只有二级分类才获取岗位列表
        await this.fetchJobsByCategory(category.id);
      } else {
        this.jobs = [];
      }
      this.currentJob = null; // 清空当前选中的岗位
    },

    // 设置当前选中的岗位
    async setCurrentJob(job) {
      if (job) {
        await this.fetchJobDetail(job.id);
      } else {
        this.currentJob = null;
      }
    },

    // 清除错误状态
    clearErrors() {
      this.error = {
        categories: null,
        jobs: null,
        jobDetail: null
      };
    }
  },

  getters: {
    // 获取一级分类列表（已排序）
    topLevelCategories: (state) => {
      return state.categoryTree
        .filter(cat => cat.status === 1)
        .sort((a, b) => a.sortOrder - b.sortOrder);
    },
    
    // 获取指定父分类下的子分类
    getSubCategories: (state) => (parentId) => {
      const parent = state.categoryTree.find(cat => cat.id === parentId);
      if (!parent || !parent.children) return [];
      return parent.children
        .filter(cat => cat.status === 1)
        .sort((a, b) => a.sortOrder - b.sortOrder);
    },

    // 判断是否正在加载数据
    isLoading: (state) => {
      return state.loading.categories || state.loading.jobs || state.loading.jobDetail;
    },

    // 获取当前岗位详情
    getCurrentJob: (state) => state.currentJob,

    // 获取分类是否有效
    isCategoryValid: (state) => (category) => {
      return category && category.status === 1;
    },

    // 获取错误状态
    hasError: (state) => {
      return state.error.categories || state.error.jobs || state.error.jobDetail;
    }
  }
}); 

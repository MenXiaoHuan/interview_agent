<!-- 岗位选择组件 -->
<template>
  <div class="job-selector" :class="{ 'eye-care': isEyeCareMode }">
    <div v-if="isLoading && !topLevelCategories.length" class="loading-overlay">
      <div class="loading-spinner">加载中...</div>
    </div>
    
    <!-- 分类选择 -->
    <div class="categories-section" v-else>
      <!-- 一级分类 -->
      <div class="category-list primary-category">
        <div
          v-for="category in validTopLevelCategories"
          :key="category.id"
          class="category-item"
          :class="{ 
            active: category.id === selectedPrimaryCategory?.id,
            disabled: !jobStore.isCategoryValid(category)
          }"
          @click="selectPrimaryCategory(category)"
        >
          <span class="category-name">{{ category.name }}</span>
          <span class="category-count" v-if="getValidSubCategoriesCount(category.id)">
            ({{ getValidSubCategoriesCount(category.id) }})
          </span>
        </div>
      </div>

      <!-- 二级分类 -->
      <div class="category-list secondary-category" v-if="selectedPrimaryCategory">
        <div
          v-for="category in validSubCategories"
          :key="category.id"
          class="category-item"
          :class="{ 
            active: category.id === currentCategory?.id,
            disabled: !jobStore.isCategoryValid(category)
          }"
          @click="selectSecondaryCategory(category)"
        >
          <span class="category-name">{{ category.name }}</span>
        </div>
      </div>
    </div>

    <!-- 岗位列表和详情 -->
    <div class="content-section" v-if="currentCategory">
      <!-- 岗位列表 -->
      <div class="jobs-section">
        <div v-if="isLoading" class="loading">加载中...</div>
        <template v-else-if="jobs.length">
          <div
            v-for="job in validJobs"
            :key="job.id"
            class="job-item"
            :class="{ 
              active: job.id === selectedJob?.id,
              disabled: job.status === 0
            }"
            @click="selectJob(job)"
          >
            <h3>{{ job.name }}</h3>
            <p class="description">{{ job.description }}</p>
          </div>
        </template>
        <div v-else class="no-data">
          暂无岗位数据
        </div>
      </div>

      <!-- 岗位详情 -->
      <div class="job-detail" v-if="currentJobDetail">
        <h2>{{ currentJobDetail.name }}</h2>
        <div class="detail-section">
          <h3>岗位描述</h3>
          <p>{{ currentJobDetail.description }}</p>
        </div>
        <div class="detail-section">
          <h3>岗位要求</h3>
          <div class="requirements">
            {{ currentJobDetail.requirements }}
          </div>
        </div>
        <div class="detail-section">
          <h3>所属分类</h3>
          <p>{{ getCategoryPath(currentJobDetail) }}</p>
        </div>
        <div class="meta-info">
          <span>创建时间：{{ formatDate(currentJobDetail.created_at) }}</span>
          <span>更新时间：{{ formatDate(currentJobDetail.updated_at) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useJobStore } from '@/stores/job';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';

const jobStore = useJobStore();
const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);

// 本地状态
const selectedPrimaryCategory = ref(null);
const selectedJob = ref(null);

// 计算属性
const topLevelCategories = computed(() => jobStore.topLevelCategories);
const validTopLevelCategories = computed(() => 
  topLevelCategories.value.filter(cat => jobStore.isCategoryValid(cat))
);

const subCategories = computed(() => {
  if (!selectedPrimaryCategory.value) return [];
  return jobStore.getSubCategories(selectedPrimaryCategory.value.id);
});

const validSubCategories = computed(() => 
  subCategories.value.filter(cat => jobStore.isCategoryValid(cat))
);

const currentCategory = computed(() => jobStore.currentCategory);
const jobs = computed(() => jobStore.jobs);
const validJobs = computed(() => jobs.value.filter(job => job.status === 1));
const isLoading = computed(() => jobStore.isLoading);
const currentJobDetail = computed(() => jobStore.getCurrentJob);

// 方法
const getValidSubCategoriesCount = (parentId) => {
  return jobStore.getSubCategories(parentId).filter(cat => jobStore.isCategoryValid(cat)).length;
};

const selectPrimaryCategory = (category) => {
  if (!jobStore.isCategoryValid(category)) return;
  selectedPrimaryCategory.value = category;
  jobStore.setCurrentCategory(null);
  selectedJob.value = null;
};

const selectSecondaryCategory = (category) => {
  if (!jobStore.isCategoryValid(category)) return;
  jobStore.setCurrentCategory(category);
  selectedJob.value = null;
};

const selectJob = async (job) => {
  if (job.status === 0) return;
  selectedJob.value = job;
  await jobStore.setCurrentJob(job);
  emit('select', job);
};

const getCategoryPath = (job) => {
  if (!job || !job.category) return '';
  const primaryCategory = topLevelCategories.value.find(
    cat => cat.id === job.category.parent_id
  );
  return primaryCategory 
    ? `${primaryCategory.name} / ${job.category.name}`
    : job.category.name;
};

import { DEFAULT_AVATAR } from '@/utils/avatar';

// 工具方法
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 事件
const emit = defineEmits(['select']);

// 处理图片加载错误
const handleImageError = (event) => {
  event.target.src = DEFAULT_AVATAR;
};

// 生命周期钩子
onMounted(async () => {
  try {
    await jobStore.fetchCategories();
  } catch (error) {
    console.error('初始化分类数据失败:', error);
  }
});
</script>

<style scoped>
.job-selector {
  display: flex;
  gap: 20px;
  height: 100%;
  min-height: 600px;
  position: relative;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
}

.loading-spinner {
  padding: 20px;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.categories-section {
  display: flex;
  gap: 15px;
  min-width: 400px;
  border-right: 1px solid #eee;
}

.category-list {
  flex: 1;
  padding: 10px;
}

.category-item {
  padding: 10px 15px;
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 8px;
  transition: all 0.3s ease;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.category-count {
  font-size: 12px;
  color: #999;
}

.category-item:not(.disabled):hover {
  background-color: #f5f5f5;
}

.category-item.active:not(.disabled) {
  background-color: #e6f7ff;
  color: #1890ff;
}

.category-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  margin-right: 8px;
  object-fit: cover;
}

.content-section {
  flex: 1;
  display: flex;
  gap: 20px;
}

.jobs-section {
  flex: 1;
  padding: 10px;
  border-right: 1px solid #eee;
  overflow-y: auto;
}

.job-item {
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.job-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.job-item:not(.disabled):hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.job-item.active:not(.disabled) {
  border-color: #1890ff;
  background-color: #e6f7ff;
}

.job-item h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
}

.description {
  color: #666;
  font-size: 14px;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.job-detail {
  flex: 1;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
}

.job-detail h2 {
  margin: 0 0 20px 0;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h3 {
  font-size: 16px;
  margin: 0 0 10px 0;
  color: #333;
}

.requirements {
  white-space: pre-line;
  color: #666;
}

.meta-info {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 20px;
  color: #999;
  font-size: 12px;
}

.loading {
  text-align: center;
  padding: 20px;
  color: #666;
}

.no-data {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 14px;
}

/* 护眼模式样式 */
.eye-care.job-selector {
  background: linear-gradient(135deg, #1c2a1f 0%, #2a3c2e 100%);
}

.eye-care .loading-overlay {
  background: rgba(144, 198, 149, 0.1);
}

.eye-care .loading-spinner {
  background: rgba(144, 198, 149, 0.15);
  color: #7fb069;
  border: 1px solid rgba(127, 176, 105, 0.3);
}

.eye-care .categories-section {
  border-right-color: rgba(127, 176, 105, 0.3);
}

.eye-care .category-item {
  background: rgba(144, 198, 149, 0.05);
  border: 1px solid rgba(127, 176, 105, 0.2);
  color: #dbe8d9;
}

.eye-care .category-item:hover:not(.disabled) {
  background: rgba(127, 176, 105, 0.15);
  border-color: rgba(127, 176, 105, 0.4);
  box-shadow: 0 2px 8px rgba(127, 176, 105, 0.2);
}

.eye-care .category-item.active:not(.disabled) {
  background: rgba(127, 176, 105, 0.2);
  border-color: rgba(127, 176, 105, 0.4);
  color: #fff;
  box-shadow: 0 2px 8px rgba(127, 176, 105, 0.3);
}

.eye-care .category-count {
  color: rgba(127, 176, 105, 0.7);
}

.eye-care .content-section {
  background: rgba(144, 198, 149, 0.03);
}

.eye-care .jobs-section {
  border-right-color: rgba(127, 176, 105, 0.3);
}

.eye-care .job-item {
  background: rgba(144, 198, 149, 0.05);
  border-color: rgba(127, 176, 105, 0.2);
  color: #dbe8d9;
}

.eye-care .job-item:hover:not(.disabled) {
  background: rgba(127, 176, 105, 0.1);
  border-color: rgba(127, 176, 105, 0.4);
  box-shadow: 0 2px 8px rgba(127, 176, 105, 0.2);
}

.eye-care .job-item.active:not(.disabled) {
  background: rgba(127, 176, 105, 0.2);
  border-color: rgba(127, 176, 105, 0.4);
  color: #fff;
  box-shadow: 0 2px 8px rgba(127, 176, 105, 0.3);
}

.eye-care .job-item h3 {
  color: #fff;
}

.eye-care .description {
  color: rgba(255, 255, 255, 0.8);
}

.eye-care .job-detail {
  background: rgba(144, 198, 149, 0.08);
  border: 1px solid rgba(127, 176, 105, 0.25);
  color: #dbe8d9;
}

.eye-care .job-detail h2 {
  color: #fff;
  border-bottom-color: rgba(127, 176, 105, 0.3);
}

.eye-care .detail-section h3 {
  color: #fff;
}

.eye-care .requirements {
  color: rgba(255, 255, 255, 0.8);
}

.eye-care .meta-info {
  border-top-color: rgba(127, 176, 105, 0.3);
  color: rgba(127, 176, 105, 0.7);
}

.eye-care .loading {
  color: #7fb069;
}

.eye-care .no-data {
  color: rgba(127, 176, 105, 0.7);
}
</style> 

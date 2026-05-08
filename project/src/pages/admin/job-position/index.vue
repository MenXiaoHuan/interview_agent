<template>
  <div class="job-positions-container" :class="{ 'eye-care': isEyeCareMode }">
    <div class="header">
      <span class="back-icon" @click="goBack" aria-label="返回" role="button">‹</span>
      <h1 class="page-title">岗位职位管理</h1>
    </div>

    <div class="management-content">


      <!-- 岗位筛选区域 -->
      <div class="filter-panel">
        <div class="filter-row">
          <div class="filter-group">
            <label>一级分类</label>
            <div class="custom-select" :class="{ open: filterPrimaryOpen }" @click.stop>
              <button type="button" class="select-trigger" @click="toggleFilterPrimaryDropdown" aria-haspopup="listbox" :aria-expanded="filterPrimaryOpen">
                <span class="trigger-text">{{ filterPrimaryLabel }}</span>
                <svg class="caret" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </button>
              <ul v-if="filterPrimaryOpen" class="select-options" role="listbox">
                <li class="select-option" :class="{ active: !filters.primaryCategory }" role="option" :aria-selected="!filters.primaryCategory" @click="selectFilterPrimary('')">
                  <svg class="option-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="8" stroke="currentColor" stroke-width="2"/></svg>
                  <span class="option-text">全部</span>
                  <svg v-if="!filters.primaryCategory" class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </li>
                <li v-for="category in primaryCategories" :key="category.id" class="select-option" :class="{ active: filters.primaryCategory === category.id }" role="option" :aria-selected="filters.primaryCategory === category.id" @click="selectFilterPrimary(category.id)">
                  <svg class="option-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="4" y="8" width="16" height="10" rx="2" stroke="currentColor" stroke-width="2"/><path d="M9 8V6a3 3 0 0 1 6 0v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                  <span class="option-text">{{ category.name }}</span>
                  <svg v-if="filters.primaryCategory===category.id" class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </li>
                <li v-if="primaryCategories.length===0" class="select-empty">暂无一级分类</li>
              </ul>
            </div>
          </div>
          <div class="filter-group">
            <label>二级分类</label>
            <div class="custom-select" :class="{ open: filterSecondaryOpen, disabled: !filters.primaryCategory }" @click.stop>
              <button type="button" class="select-trigger" :disabled="!filters.primaryCategory" @click="toggleFilterSecondaryDropdown" aria-haspopup="listbox" :aria-expanded="filterSecondaryOpen">
                <span class="trigger-text">{{ filterSecondaryLabel }}</span>
                <svg class="caret" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </button>
              <ul v-if="filterSecondaryOpen" class="select-options" role="listbox">
                <li class="select-option" :class="{ active: !filters.secondaryCategory }" role="option" :aria-selected="!filters.secondaryCategory" @click="selectFilterSecondary('')">
                  <svg class="option-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="8" stroke="currentColor" stroke-width="2"/></svg>
                  <span class="option-text">全部</span>
                  <svg v-if="!filters.secondaryCategory" class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </li>
                <li v-for="category in filteredSecondaryCategories" :key="category.id" class="select-option" :class="{ active: filters.secondaryCategory === category.id }" role="option" :aria-selected="filters.secondaryCategory === category.id" @click="selectFilterSecondary(category.id)">
                  <svg class="option-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="4" y="8" width="16" height="10" rx="2" stroke="currentColor" stroke-width="2"/><path d="M4 9l8 5 8-5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                  <span class="option-text">{{ category.name }}</span>
                  <svg v-if="filters.secondaryCategory===category.id" class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </li>
                <li v-if="filteredSecondaryCategories.length===0" class="select-empty">暂无二级分类</li>
              </ul>
            </div>
          </div>
          <div class="filter-group">
            <label>状态</label>
            <div class="custom-select" :class="{ open: filterStatusOpen }" @click.stop>
              <button type="button" class="select-trigger" @click="toggleFilterStatusDropdown" aria-haspopup="listbox" :aria-expanded="filterStatusOpen">
                <span class="trigger-text">{{ filterStatusLabel }}</span>
                <svg class="caret" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </button>
              <ul v-if="filterStatusOpen" class="select-options" role="listbox">
                <li class="select-option" :class="{ active: !filters.status }" role="option" :aria-selected="!filters.status" @click="selectFilterStatus('')">
                  <svg class="option-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="8" stroke="currentColor" stroke-width="2"/></svg>
                  <span class="option-text">全部</span>
                  <svg v-if="!filters.status" class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </li>
                <li class="select-option" :class="{ active: filters.status==='active' }" role="option" :aria-selected="filters.status==='active'" @click="selectFilterStatus('active')">
                  <svg class="option-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M9 12l2 2 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/></svg>
                  <span class="option-text">活跃</span>
                  <svg v-if="filters.status==='active'" class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </li>
                <li class="select-option" :class="{ active: filters.status==='inactive' }" role="option" :aria-selected="filters.status==='inactive'" @click="selectFilterStatus('inactive')">
                  <svg class="option-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M9 12l6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><path d="M15 12l-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/></svg>
                  <span class="option-text">未活跃</span>
                  <svg v-if="filters.status==='inactive'" class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </li>
              </ul>
            </div>
          </div>
        </div>
        <div class="filter-row">
          <div class="filter-group search-group">
            <input type="text" v-model="filters.keyword" placeholder="搜索岗位名称...">
            <button class="search-btn" @click="searchJobs">搜索</button>
          </div>
          <div class="filter-group">
            <button class="add-job-btn" @click="showAddJobModal">
              <span class="add-icon">+</span> 添加岗位
            </button>
          </div>
        </div>
      </div>

      <!-- 岗位列表 -->
      <div class="jobs-panel">
        <div v-if="isLoading" class="loading-container">
          <div class="loading-spinner"></div>
          <p class="loading-text">加载中...</p>
        </div>
        <template v-else>
          <div class="jobs-table">
            <div class="table-header">
              <div class="th job-name">岗位名称</div>
              <div class="th job-category">所属分类</div>
              <div class="th job-status">状态</div>
              <div class="th job-actions">操作</div>
            </div>
            <div v-for="job in filteredJobs" :key="job.id" class="table-row">
              <div class="td job-name">{{ job.name }}</div>
              <div class="td job-category">
                {{ getCategoryPath(job) }}
              </div>
              <div class="td job-status">
                <span class="status-badge" :class="job.status === 1 ? 'active' : 'inactive'">
                  {{ job.status === 1 ? '活跃' : '未活跃' }}
                </span>
              </div>
              <div class="td job-actions">
                <button class="edit-btn" @click="editJob(job)" title="编辑">
                  <i class="edit-icon"></i>
                </button>
                <button class="toggle-btn" @click="toggleJobStatus(job)" :title="job.status === 1 ? '停用' : '启用'">
                  <i class="toggle-icon"></i>
                </button>
                <button class="delete-btn" @click="handleDeleteJob(job)" title="删除">
                  <i class="delete-icon"></i>
                </button>
              </div>
            </div>
          </div>
          <div v-if="filteredJobs.length === 0" class="empty-list">
            暂无符合条件的岗位数据
          </div>
        </template>
      </div>

      <!-- 分页控件 -->
      <div class="pagination" v-if="!isLoading && totalPages > 1">
        <button 
          class="page-btn" 
          :class="{ disabled: currentPage === 1 }"
          @click="changePage(currentPage - 1)"
          :disabled="currentPage === 1"
        >
          上一页
        </button>
        <div class="page-info">{{ currentPage }} / {{ totalPages }}</div>
        <button 
          class="page-btn" 
          :class="{ disabled: currentPage === totalPages }"
          @click="changePage(currentPage + 1)"
          :disabled="currentPage === totalPages"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- 添加/编辑岗位弹窗 -->
    <div class="modal" v-if="showJobModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑' : '添加' }}岗位</h3>
          <span class="close-btn" @click="closeJobModal">&times;</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label for="primaryCategory">一级分类</label>
            <div class="custom-select" :class="{ open: primaryDropdownOpen }" @click.stop>
              <button type="button" class="select-trigger" @click="togglePrimaryDropdown" aria-haspopup="listbox" :aria-expanded="primaryDropdownOpen">
                <span class="trigger-text">{{ primarySelectedLabel }}</span>
                <svg class="caret" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </button>
              <ul v-if="primaryDropdownOpen" class="select-options" role="listbox">
                <li class="select-option" :class="{ active: !jobForm.primaryCategoryId }" role="option" :aria-selected="!jobForm.primaryCategoryId" @click="selectPrimaryCategory('')">
                  <svg class="option-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="8" stroke="currentColor" stroke-width="2"/></svg>
                  <span class="option-text">请选择一级分类</span>
                  <svg v-if="!jobForm.primaryCategoryId" class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </li>
                <li v-for="category in primaryCategories" :key="category.id" class="select-option" :class="{ active: jobForm.primaryCategoryId === category.id }" role="option" :aria-selected="jobForm.primaryCategoryId === category.id" @click="selectPrimaryCategory(category.id)">
                  <svg class="option-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="4" y="8" width="16" height="10" rx="2" stroke="currentColor" stroke-width="2"/><path d="M9 8V6a3 3 0 0 1 6 0v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                  <span class="option-text">{{ category.name }}</span>
                  <svg v-if="jobForm.primaryCategoryId===category.id" class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </li>
                <li v-if="primaryCategories.length===0" class="select-empty">暂无一级分类</li>
              </ul>
            </div>
          </div>
          <div class="form-group">
            <label for="secondaryCategory">二级分类</label>
            <div class="custom-select" :class="{ open: secondaryDropdownOpen, disabled: !jobForm.primaryCategoryId }" @click.stop>
              <button type="button" class="select-trigger" :disabled="!jobForm.primaryCategoryId" @click="toggleSecondaryDropdown" aria-haspopup="listbox" :aria-expanded="secondaryDropdownOpen">
                <span class="trigger-text">{{ secondarySelectedLabel }}</span>
                <svg class="caret" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </button>
              <ul v-if="secondaryDropdownOpen" class="select-options" role="listbox">
                <li class="select-option" :class="{ active: !jobForm.secondaryCategoryId }" role="option" :aria-selected="!jobForm.secondaryCategoryId" @click="selectSecondaryCategory('')">
                  <svg class="option-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="8" stroke="currentColor" stroke-width="2"/></svg>
                  <span class="option-text">请选择二级分类</span>
                  <svg v-if="!jobForm.secondaryCategoryId" class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </li>
                <li v-for="category in jobFormSecondaryCategories" :key="category.id" class="select-option" :class="{ active: jobForm.secondaryCategoryId === category.id }" role="option" :aria-selected="jobForm.secondaryCategoryId === category.id" @click="selectSecondaryCategory(category.id)">
                  <svg class="option-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="4" y="8" width="16" height="10" rx="2" stroke="currentColor" stroke-width="2"/><path d="M4 9l8 5 8-5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                  <span class="option-text">{{ category.name }}</span>
                  <svg v-if="jobForm.secondaryCategoryId===category.id" class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </li>
                <li v-if="jobFormSecondaryCategories.length===0" class="select-empty">暂无二级分类</li>
              </ul>
            </div>
          </div>
          <div class="form-group">
            <label for="jobName">岗位名称</label>
            <input type="text" id="jobName" v-model="jobForm.name" placeholder="请输入岗位名称">
          </div>
          <div class="form-group">
            <label for="jobDescription">岗位描述</label>
            <textarea id="jobDescription" v-model="jobForm.description" placeholder="请输入岗位描述"></textarea>
          </div>
          <div class="form-group">
            <label for="jobStatus">岗位状态</label>
            <select id="jobStatus" v-model="jobForm.status">
              <option value="active">活跃</option>
              <option value="inactive">未活跃</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeJobModal">取消</button>
          <button class="save-btn" @click="saveJob">保存</button>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div class="modal" v-if="showDeleteModal">
      <div class="modal-content delete-modal">
        <div class="modal-header">
          <h3>确认删除</h3>
          <span class="close-btn" @click="closeDeleteModal">&times;</span>
        </div>
        <div class="modal-body">
          <p>确定要删除岗位"{{ deleteTarget.name }}"吗？</p>
          <p class="warning-text">
            注意：删除岗位将同时删除与该岗位相关的所有面试数据！
          </p>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeDeleteModal">取消</button>
          <button class="delete-confirm-btn" @click="confirmDelete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
import { API, getJobCategoriesTree, getJobList, getJobDetail, createJob, updateJob, deleteJob } from '@/utils/api';

const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);

// 数据状态
const isLoading = ref(false);
const primaryCategories = ref([]);
const secondaryCategories = ref([]);
const jobs = ref([]);

// 分页
const currentPage = ref(1);
const pageSize = ref(10);
const totalItems = ref(0);
const totalPages = computed(() => Math.ceil(totalItems.value / pageSize.value));



// 筛选条件
const filters = ref({
  primaryCategory: '',
  secondaryCategory: '',
  status: '',
  keyword: ''
});

// 自定义下拉（过滤面板）
const filterPrimaryOpen = ref(false);
const filterSecondaryOpen = ref(false);
const filterStatusOpen = ref(false);
const filterPrimaryLabel = computed(() => {
  const id = filters.value.primaryCategory;
  if (!id) return '全部';
  const found = primaryCategories.value.find(c => c.id === id);
  return found ? found.name : '全部';
});
const filterSecondaryLabel = computed(() => {
  const id = filters.value.secondaryCategory;
  if (!id) return '全部';
  const found = filteredSecondaryCategories.value.find(c => c.id === id);
  return found ? found.name : '全部';
});
const filterStatusLabel = computed(() => {
  const s = filters.value.status;
  if (!s) return '全部';
  return s === 'active' ? '活跃' : '未活跃';
});
const toggleFilterPrimaryDropdown = () => { filterPrimaryOpen.value = !filterPrimaryOpen.value; };
const toggleFilterSecondaryDropdown = () => { if (!filters.value.primaryCategory) return; filterSecondaryOpen.value = !filterSecondaryOpen.value; };
const toggleFilterStatusDropdown = () => { filterStatusOpen.value = !filterStatusOpen.value; };
const selectFilterPrimary = (id) => { filters.value.primaryCategory = id || ''; filters.value.secondaryCategory = ''; filterPrimaryOpen.value = false; handlePrimaryCategoryChange(); };
const selectFilterSecondary = (id) => { filters.value.secondaryCategory = id || ''; filterSecondaryOpen.value = false; };
const selectFilterStatus = (s) => { filters.value.status = s || ''; filterStatusOpen.value = false; };

// 模态框状态
const showJobModal = ref(false);
const showDeleteModal = ref(false);
const isEditing = ref(false);

// 表单数据
const jobForm = ref({
  id: '',
  name: '',
  description: '',
  primaryCategoryId: '',
  secondaryCategoryId: '',
  status: 'active'
});

// 自定义下拉（编辑岗位）
const primaryDropdownOpen = ref(false);
const secondaryDropdownOpen = ref(false);
const primarySelectedLabel = computed(() => {
  const id = jobForm.value.primaryCategoryId;
  if (!id) return '请选择一级分类';
  const found = primaryCategories.value.find(c => c.id === id);
  return found ? found.name : '请选择一级分类';
});
const secondarySelectedLabel = computed(() => {
  const id = jobForm.value.secondaryCategoryId;
  if (!id) return '请选择二级分类';
  const found = jobFormSecondaryCategories.value.find(c => c.id === id);
  return found ? found.name : '请选择二级分类';
});
const togglePrimaryDropdown = () => { primaryDropdownOpen.value = !primaryDropdownOpen.value; };
const toggleSecondaryDropdown = () => { if (!jobForm.value.primaryCategoryId) return; secondaryDropdownOpen.value = !secondaryDropdownOpen.value; };
const selectPrimaryCategory = (id) => { jobForm.value.primaryCategoryId = id || ''; jobForm.value.secondaryCategoryId = ''; primaryDropdownOpen.value = false; handleJobFormPrimaryChange(); };
const selectSecondaryCategory = (id) => { jobForm.value.secondaryCategoryId = id || ''; secondaryDropdownOpen.value = false; };

// 删除目标
const deleteTarget = ref({
  id: '',
  name: ''
});

// 计算属性
const filteredSecondaryCategories = computed(() => {
  if (!filters.value.primaryCategory) {
    return secondaryCategories.value;
  }
  return secondaryCategories.value.filter(category => category.parentId === filters.value.primaryCategory);
});

const jobFormSecondaryCategories = computed(() => {
  if (!jobForm.value.primaryCategoryId) {
    return [];
  }
  return secondaryCategories.value.filter(category => category.parentId === jobForm.value.primaryCategoryId);
});

const filteredJobs = computed(() => {
  let result = [...jobs.value];
  
  // 按一级分类筛选
  if (filters.value.primaryCategory) {
    result = result.filter(job => {
      const secondaryCat = secondaryCategories.value.find(sc => sc.id === job.categoryId);
      return secondaryCat && secondaryCat.parentId === filters.value.primaryCategory;
    });
  }
  
  // 按二级分类筛选
  if (filters.value.secondaryCategory) {
    result = result.filter(job => job.categoryId === filters.value.secondaryCategory);
  }
  
  // 按状态筛选
  if (filters.value.status) {
    result = result.filter(job => {
      const jobStatus = job.status === 1 ? 'active' : 'inactive';
      return jobStatus === filters.value.status;
    });
  }
  
  // 按关键词筛选
  if (filters.value.keyword) {
    const keyword = filters.value.keyword.toLowerCase();
    result = result.filter(job => 
      job.name.toLowerCase().includes(keyword) || 
      (job.description && job.description.toLowerCase().includes(keyword))
    );
  }
  
  return result;
});

// 获取分类数据
const fetchCategories = async () => {
  try {
    const response = await getJobCategoriesTree();
    if (response.code === 200) {
      const data = response.data;
      
      // 处理嵌套结构：一级分类直接使用，二级分类从children中提取
      primaryCategories.value = data;
      
      // 从所有一级分类的children中提取二级分类
      const allSecondaryCategories = [];
      data.forEach(primaryCategory => {
        if (primaryCategory.children && Array.isArray(primaryCategory.children)) {
          primaryCategory.children.forEach(secondaryCategory => {
            allSecondaryCategories.push({
              ...secondaryCategory,
              parentId: primaryCategory.id
            });
          });
        }
      });
      secondaryCategories.value = allSecondaryCategories;
    } else {
      throw new Error(response.message || '获取分类数据失败');
    }
  } catch (error) {
    console.error('获取分类数据失败:', error);
    uni.showToast({
      title: error.message || '获取分类数据失败',
      icon: 'none'
    });
  }
};

// 获取岗位数据
const fetchJobs = async () => {
  isLoading.value = true;
  try {
    const response = await getJobList();
    if (response.code === 200 && response.data) {
      jobs.value = response.data;
      totalItems.value = response.data.length;
    } else {
      throw new Error(response.message || '获取岗位数据失败');
    }
  } catch (error) {
    console.error('获取岗位数据失败:', error);
    uni.showToast({
      title: error.message || '获取岗位数据失败',
      icon: 'none'
    });
  } finally {
    isLoading.value = false;
  }
};

// 获取岗位分类路径
const getCategoryPath = (job) => {
  if (!job.category) return '未分类';
  
  // 从job.category中获取二级分类信息
  const secondaryCat = job.category;
  
  // 查找对应的一级分类
  const primaryCat = primaryCategories.value.find(pc => pc.id === secondaryCat.parentId);
  if (!primaryCat) return secondaryCat.name;
  
  return `${primaryCat.name} > ${secondaryCat.name}`;
};

// 处理一级分类变化
const handlePrimaryCategoryChange = () => {
  filters.value.secondaryCategory = '';
};

// 处理表单中一级分类变化
const handleJobFormPrimaryChange = () => {
  jobForm.value.secondaryCategoryId = '';
};

// 搜索岗位
const searchJobs = () => {
  currentPage.value = 1;
  // 不需要重新fetchJobs，因为filteredJobs是computed属性
};

// 切换页码
const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  // 不需要重新fetchJobs，因为filteredJobs是computed属性
};

// 添加岗位
const showAddJobModal = () => {
  isEditing.value = false;
  jobForm.value = {
    id: '',
    name: '',
    description: '',
    primaryCategoryId: '',
    secondaryCategoryId: '',
    status: 'active'
  };
  showJobModal.value = true;
};

// 编辑岗位
const editJob = (job) => {
  isEditing.value = true;
  
  // 从job.category中获取分类信息
  const secondaryCat = job.category;
  const primaryCatId = secondaryCat ? secondaryCat.parentId : '';
  
  jobForm.value = {
    id: job.id,
    name: job.name,
    description: job.description || '',
    primaryCategoryId: primaryCatId,
    secondaryCategoryId: job.categoryId,
    status: job.status === 1 ? 'active' : 'inactive'
  };
  
  showJobModal.value = true;
};

// 保存岗位
const saveJob = async () => {
  // 表单验证
  if (!jobForm.value.name) {
    uni.showToast({
      title: '请输入岗位名称',
      icon: 'none'
    });
    return;
  }

  if (!jobForm.value.primaryCategoryId) {
    uni.showToast({
      title: '请选择一级分类',
      icon: 'none'
    });
    return;
  }

  if (!jobForm.value.secondaryCategoryId) {
    uni.showToast({
      title: '请选择二级分类',
      icon: 'none'
    });
    return;
  }

  try {
    let response;
    if (isEditing.value) {
      response = await updateJob({
        id: jobForm.value.id,
        name: jobForm.value.name,
        description: jobForm.value.description,
        requirements: jobForm.value.description, // 使用description作为requirements
        status: jobForm.value.status === 'active' ? 1 : 0
      });
    } else {
      response = await createJob({
        categoryId: jobForm.value.secondaryCategoryId,
        name: jobForm.value.name,
        description: jobForm.value.description,
        requirements: jobForm.value.description, // 使用description作为requirements
        status: jobForm.value.status === 'active' ? 1 : 0
      });
    }

    if (response.code === 200) {
      uni.showToast({
        title: isEditing.value ? '更新成功' : '添加成功',
        icon: 'success'
      });
      
      // 刷新岗位数据
      await fetchJobs();
      closeJobModal();
    } else {
      throw new Error(response.message || (isEditing.value ? '更新失败' : '添加失败'));
    }
  } catch (error) {
    console.error(isEditing.value ? '更新岗位失败:' : '添加岗位失败:', error);
    uni.showToast({
      title: error.message || (isEditing.value ? '更新失败' : '添加失败'),
      icon: 'none'
    });
  }
};

// 关闭岗位弹窗
const closeJobModal = () => {
  showJobModal.value = false;
};

// 切换岗位状态
const toggleJobStatus = async (job) => {
  try {
    const newStatus = job.status === 1 ? 0 : 1;

    // 更新岗位状态
    const response = await updateJob({
      id: job.id,
      name: job.name,
      description: job.description,
      requirements: job.description,
      status: newStatus
    });

    if (response.code === 200) {
      uni.showToast({
        title: newStatus === 1 ? '岗位已启用' : '岗位已停用',
        icon: 'success'
      });
      
      // 更新本地数据
      const index = jobs.value.findIndex(item => item.id === job.id);
      if (index !== -1) {
        jobs.value[index].status = newStatus;
      }
    } else {
      throw new Error(response.message || '操作失败');
    }
  } catch (error) {
    console.error('切换岗位状态失败:', error);
    uni.showToast({
      title: error.message || '操作失败',
      icon: 'none'
    });
  }
};

// 删除岗位
const handleDeleteJob = (job) => {
  deleteTarget.value = {
    id: job.id,
    name: job.name
  };
  showDeleteModal.value = true;
};

// 确认删除
const confirmDelete = async () => {
  try {
    const response = await deleteJob(deleteTarget.value.id);

    if (response.code === 200) {
      uni.showToast({
        title: '删除成功',
        icon: 'success'
      });
      
      // 刷新岗位数据
      await fetchJobs();
      closeDeleteModal();
    } else {
      throw new Error(response.message || '删除失败');
    }
  } catch (error) {
    console.error('删除岗位失败:', error);
    uni.showToast({
      title: error.message || '删除失败',
      icon: 'none'
    });
  }
};

// 关闭删除确认弹窗
const closeDeleteModal = () => {
  showDeleteModal.value = false;
};

// 返回上一页
const goBack = () => {
  uni.navigateTo({
    url: '/pages/admin/index'
  });
};

// 监听筛选条件变化
watch([filters.primaryCategory, filters.secondaryCategory, filters.status], () => {
  currentPage.value = 1;
  // 不需要重新fetchJobs，因为filteredJobs是computed属性
}, { deep: true });

// 生命周期钩子
onMounted(async () => {
  await fetchCategories();
  await fetchJobs();
});
</script>

<style scoped>
.job-positions-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  transition: background-color 0.3s ease;
}

.job-positions-container.eye-care {
  background: #f0f2e6 !important;
  background-image: none !important;
}

.header {
  position: relative;
  text-align: center;
  margin-bottom: 30px;
}

.back-icon {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  font-size: 2em;
  color: #666;
  cursor: pointer;
  transition: color 0.3s ease;
  padding: 0 15px;
}

.back-icon:hover {
  color: #333;
}

.page-title {
  margin: 0;
  font-size: 1.8em;
  color: #333;
  text-align: center;
}

.management-content {
  max-width: 1200px;
  margin: 0 auto;
}



/* 筛选面板 */
.filter-panel {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 15px;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-group {
  flex: 1;
  min-width: 200px;
}

.filter-group label {
  display: block;
  margin-bottom: 5px;
  color: #333;
  font-weight: 500;
  font-size: 0.9em;
}

.filter-group select,
.filter-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9em;
}

.search-group {
  display: flex;
  gap: 10px;
  align-items: center;
  height: 40px;
}

.search-group input {
  flex: 1;
  height: 40px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9em;
}

.search-btn {
  background: #4A6FFF;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 10px 15px;
  height: 40px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9em;
}

.search-btn:hover {
  background: #3A5FEF;
}

.add-job-btn {
  background: #4A6FFF;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 10px 15px;
  height: 40px;
  font-size: 0.9em;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: background-color 0.3s ease;
  white-space: nowrap;
  width: 100%;
  justify-content: center;
}

.add-job-btn:hover {
  background: #3A5FEF;
}

.add-icon {
  font-size: 1.2em;
  font-weight: bold;
}

/* 岗位列表 */
.jobs-panel {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.jobs-table {
  width: 100%;
  border-collapse: collapse;
}

.table-header {
  display: grid;
  grid-template-columns: 2fr 2fr 1fr 1.5fr;
  background: #f8f9fa;
  padding: 12px 15px;
  font-weight: bold;
  border-bottom: 1px solid #e9ecef;
  border-radius: 8px 8px 0 0;
}

.table-row {
  display: grid;
  grid-template-columns: 2fr 2fr 1fr 1.5fr;
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s ease;
}

.table-row:hover {
  background: #f8f9fa;
}

.table-row:last-child {
  border-bottom: none;
  border-radius: 0 0 8px 8px;
}

.th, .td {
  padding: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.job-name {
  font-weight: 500;
}

.job-category {
  color: #666;
}

.job-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.85em;
  font-weight: 500;
}

.status-badge.active {
  background: #d4edda;
  color: #155724;
}

.status-badge.inactive {
  background: #f8d7da;
  color: #721c24;
}

.edit-btn, .toggle-btn, .delete-btn {
  width: 45px;
  height: 45px;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.edit-btn, .toggle-btn, .delete-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.edit-btn {
  background: #4A6FFF;
}

.toggle-btn {
  background: #ffc107;
}

.delete-btn {
  background: #F44336;
}

.edit-icon, .toggle-icon, .delete-icon {
  display: inline-block;
  width: 28px;
  height: 28px;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}

.edit-icon {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7'%3E%3C/path%3E%3Cpath d='M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z'%3E%3C/path%3E%3C/svg%3E");
}

.toggle-icon {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M9 12l2 2 4-4'%3E%3C/path%3E%3Ccircle cx='12' cy='12' r='10'%3E%3C/circle%3E%3C/svg%3E");
}

.delete-icon {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='3 6 5 6 21 6'%3E%3C/polyline%3E%3Cpath d='M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2'%3E%3C/path%3E%3Cline x1='10' y1='11' x2='10' y2='17'%3E%3C/line%3E%3Cline x1='14' y1='11' x2='14' y2='17'%3E%3C/line%3E%3C/svg%3E");
}

.empty-list {
  text-align: center;
  padding: 30px;
  color: #666;
  font-style: italic;
}

/* 分页控件 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
}

.page-btn {
  padding: 8px 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(.disabled) {
  background: #f8f9fa;
  border-color: #4A6FFF;
  color: #4A6FFF;
}

.page-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 0.9em;
}

/* 加载动画 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(74, 111, 255, 0.1);
  border-top: 4px solid #4A6FFF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

.loading-text {
  color: #666;
  font-size: 1em;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 模态框 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

.delete-modal {
  max-width: 400px;
}

.modal-header {
  padding: 15px 20px;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  font-size: 1.5em;
  color: #adb5bd;
  cursor: pointer;
  transition: color 0.3s ease;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #333;
  font-weight: 500;
}

.form-group select,
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1em;
}

/* 自定义下拉样式（编辑岗位弹窗） */
.custom-select { position: relative; width: 100%; }
.select-trigger { width: 100%; display: flex; justify-content: space-between; align-items: center; padding: 10px 12px; border: 1px solid #ddd; border-radius: 6px; background: #fff; color: #333; cursor: pointer; transition: all 0.3s ease; height: 40px; box-sizing: border-box; }
.select-trigger[disabled] { cursor: not-allowed; opacity: 0.6; }
.select-trigger:hover { border-color: #4A6FFF; box-shadow: 0 0 0 3px rgba(74,111,255,0.12); }
.custom-select.open .select-trigger { border-color: #4A6FFF; box-shadow: 0 0 0 3px rgba(74,111,255,0.15); }
.caret { color: #4A6FFF; transition: transform 0.2s ease; }
.custom-select.open .caret { transform: rotate(180deg); }
.select-options { position: absolute; left: 0; right: 0; top: calc(100% + 8px); background: #fff; border: 1px solid #ddd; border-radius: 8px; box-shadow: 0 10px 24px rgba(0,0,0,0.12); max-height: 220px; overflow: auto; z-index: 10; padding: 8px; }
.select-option { display: flex; align-items: center; gap: 10px; padding: 10px 12px; border-radius: 6px; color: #333; cursor: pointer; transition: background 0.2s ease; }
.select-option:hover { background: #f5f7ff; }
.select-option.active { background: #e9f0ff; }
.option-icon, .check-icon { color: #4A6FFF; }
.select-empty { padding: 12px; text-align: center; color: #999; }

/* 岗位名称和岗位描述输入框的左右边距 */
.form-group input#jobName{
  margin: 0 5px;
  width: calc(100% - 30px);
}
.form-group textarea#jobDescription {
  margin: 0 5px;
  width: calc(100% - 30px);
}

.form-group textarea {
  min-height: 100px;
  resize: vertical;
}

.cancel-btn,
.save-btn,
.delete-confirm-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 0.9em;
  cursor: pointer;
  border: none;
  transition: background-color 0.3s ease;
}

.cancel-btn {
  background: #e9ecef;
  color: #333;
}

.cancel-btn:hover {
  background: #dee2e6;
}

.save-btn {
  background: #4A6FFF;
  color: white;
}

.save-btn:hover {
  background: #3A5FEF;
}

.delete-confirm-btn {
  background: #dc3545;
  color: white;
}

.delete-confirm-btn:hover {
  background: #c82333;
}

.warning-text {
  color: #dc3545;
  font-weight: 500;
}

/* 护眼模式适配 */
.job-positions-container.eye-care .stat-card,
.job-positions-container.eye-care .filter-panel,
.job-positions-container.eye-care .jobs-panel {
  background-color: #f8f9e8;
}

.job-positions-container.eye-care .table-header {
  background-color: #f0f2e6;
}

.job-positions-container.eye-care .table-row:hover {
  background-color: #f0f2e6;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .filter-group {
    min-width: 100%;
  }
  
  .table-header,
  .table-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .th {
    display: none;
  }
  
  .td {
    padding: 5px 0;
    white-space: normal;
  }
  
  .td:before {
    content: attr(data-label);
    font-weight: bold;
    display: inline-block;
    width: 100px;
  }
  
  .job-actions {
    justify-content: flex-start;
  }
}
</style>

<template>
  <div class="scenario-management-container" :class="{ 'eye-care': isEyeCareMode }">
    <div class="header">
      <span class="back-icon" @click="goBack" aria-label="返回" role="button">‹</span>
      <h1 class="page-title">场景评测管理</h1>
    </div>

    <div class="management-content">
      <!-- 岗位选择区域 -->
      <div class="job-selection-panel">
        <h2 class="section-title">选择岗位</h2>
        <div class="filter-row">
          <div class="filter-group">
            <label>岗位</label>
            <div class="custom-select" :class="{ open: jobDropdownOpen }" @click.stop>
              <button type="button" class="select-trigger" @click="toggleJobDropdown" aria-haspopup="listbox" :aria-expanded="jobDropdownOpen">
                <span class="trigger-text">{{ jobSelectedLabel }}</span>
                <svg class="caret" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </button>
              <ul v-if="jobDropdownOpen" class="select-options" role="listbox">
                <li class="select-option" :class="{ active: !filters.jobPosition }" role="option" :aria-selected="!filters.jobPosition" @click="selectJobFilter('')">
                  <svg class="option-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="8" stroke="currentColor" stroke-width="2"/></svg>
                  <span class="option-text">全部岗位</span>
                  <svg v-if="!filters.jobPosition" class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </li>
                <li v-for="job in jobs" :key="job.id" class="select-option" :class="{ active: filters.jobPosition === job.id }" role="option" :aria-selected="filters.jobPosition === job.id" @click="selectJobFilter(job.id)">
                  <svg class="option-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="4" y="8" width="16" height="10" rx="2" stroke="currentColor" stroke-width="2"/><path d="M9 8V6a3 3 0 0 1 6 0v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                  <span class="option-text">{{ job.name }}</span>
                  <svg v-if="filters.jobPosition === job.id" class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </li>
                <li v-if="jobs.length===0" class="select-empty">暂无岗位</li>
              </ul>
            </div>
          </div>
        </div>
        <div class="filter-row">
          <div class="filter-group search-group">
            <input type="text" v-model="filters.keyword" placeholder="搜索场景关键词...">
            <button class="search-btn" @click="searchScenarios">搜索</button>
          </div>
          <div class="filter-group">
            <button class="add-btn" @click="showAddScenarioModal">
              <span class="add-icon">+</span> 添加场景
            </button>
          </div>
        </div>
      </div>

      <!-- 场景列表 -->
      <div class="scenarios-panel">
        <h2 class="section-title">场景列表</h2>
        <div v-if="isLoading" class="loading-container">
          <div class="loading-spinner"></div>
          <p class="loading-text">加载中...</p>
        </div>
        <template v-else>
          <div class="scenarios-table">
            <div class="table-header">
              <div class="th scenario-question">场景问题</div>
              <div class="th scenario-job">关联岗位</div>
              <div class="th scenario-created">创建时间</div>
              <div class="th scenario-actions">操作</div>
            </div>
            <div v-for="scenario in scenarios" :key="scenario.id" class="table-row">
              <div class="td scenario-question">{{ scenario.question }}</div>
              <div class="td scenario-job">{{ getJobName(scenario.jobId) }}</div>
              <div class="td scenario-created">{{ formatDate(scenario.createdAt) }}</div>
              <div class="td scenario-actions">
                <button class="edit-btn" @click="editScenario(scenario)" title="编辑">
                  <i class="edit-icon"></i>
                </button>
                <button class="delete-btn" @click="deleteScenario(scenario)" title="删除">
                  <i class="delete-icon"></i>
                </button>
              </div>
            </div>
          </div>
          <div v-if="scenarios.length === 0" class="empty-list">
            暂无场景数据，请先添加场景
          </div>
        </template>

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
    </div>

    <!-- 添加/编辑场景弹窗 -->
    <div class="modal" v-if="showScenarioModal">
      <div class="modal-content scenario-modal">
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑' : '添加' }}场景</h3>
          <span class="close-btn" @click="closeScenarioModal">&times;</span>
        </div>
        <div class="modal-body">
          <div class="form-group" v-if="!isEditing">
            <label for="scenarioJob">关联岗位</label>
            <select id="scenarioJob" v-model="scenarioForm.jobId" required>
              <option value="">请选择岗位</option>
              <option v-for="job in jobs" :key="job.id" :value="job.id">
                {{ job.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="scenarioQuestion">场景问题</label>
            <div class="input-wrapper">
              <textarea id="scenarioQuestion" v-model="scenarioForm.question" placeholder="请输入场景问题内容" rows="6" maxlength="400"></textarea>
              <div class="input-footer">
                <span class="char-count" :class="{ warning: (scenarioForm.question || '').length >= 360 }">{{ (scenarioForm.question || '').length }}/400</span>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeScenarioModal">取消</button>
          <button class="save-btn" @click="saveScenario">保存</button>
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
          <p>确定要删除场景"{{ deleteTarget.question }}"吗？</p>
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
import { API } from '@/utils/api';

const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);

// 数据状态
const isLoading = ref(false);
const jobs = ref([]);
const scenarios = ref([]);
const allScenarios = ref([]);

// 分页
const currentPage = ref(1);
const pageSize = ref(10);
const totalItems = ref(0);
const totalPages = computed(() => Math.ceil(totalItems.value / pageSize.value));

// 筛选条件
const filters = ref({
  jobPosition: '',
  keyword: ''
});

// 自定义下拉状态
const jobDropdownOpen = ref(false);
const jobSelectedLabel = computed(() => {
  if (!filters.value.jobPosition) return '全部岗位';
  const found = jobs.value.find(j => j.id === filters.value.jobPosition);
  return found ? found.name : '全部岗位';
});
const toggleJobDropdown = () => { jobDropdownOpen.value = !jobDropdownOpen.value; };
const selectJobFilter = (id) => { filters.value.jobPosition = id || ''; jobDropdownOpen.value = false; handleJobPositionChange(); };

// 模态框状态
const showScenarioModal = ref(false);
const showDeleteModal = ref(false);
const isEditing = ref(false);

// 表单数据
const scenarioForm = ref({
  id: '',
  jobId: '',
  question: ''
});

// 删除目标
const deleteTarget = ref({
  id: '',
  question: ''
});

// 获取岗位数据

// 获取岗位数据
const fetchJobs = async () => {
  try {
    const token = uni.getStorageSync('token');
    if (!token) {
      throw new Error('请先登录');
    }

    const response = await uni.request({
      url: API.JOB.LIST,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (response.statusCode === 200 && response.data.code === 200) {
      jobs.value = response.data.data || [];
    } else {
      throw new Error(response.data.message || '获取岗位数据失败');
    }
  } catch (error) {
    console.error('获取岗位数据失败:', error);
    jobs.value = [];
  }
};

// 获取场景数据
const fetchScenarios = async () => {
  isLoading.value = true;
  try {
    const token = uni.getStorageSync('token');
    if (!token) {
      throw new Error('请先登录');
    }

    let url;
    if (filters.value.jobPosition) {
      // 如果选择了岗位，获取该岗位的场景问题
      url = API.SCENARIO_QUESTION.GET(filters.value.jobPosition);
    } else {
      // 如果没有选择岗位，获取所有场景问题
      url = API.SCENARIO_QUESTION.GET_ALL;
    }

    const response = await uni.request({
      url: url,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (response.statusCode === 200 && response.data.code === 200) {
      const raw = response.data.data || [];
      const keyword = (filters.value.keyword || '').trim().toLowerCase();
      const filtered = keyword ? raw.filter(s => (s.question || '').toLowerCase().includes(keyword)) : raw;
      allScenarios.value = filtered;
      totalItems.value = allScenarios.value.length;
      updatePagedScenarios();
    } else {
      throw new Error(response.data.message || '获取场景数据失败');
    }
  } catch (error) {
    console.error('获取场景数据失败:', error);
    scenarios.value = [];
    totalItems.value = 0;
  } finally {
    isLoading.value = false;
  }
};

// 更新分页后的场景数据
const updatePagedScenarios = () => {
  if (totalPages.value > 0 && currentPage.value > totalPages.value) {
    currentPage.value = totalPages.value;
  }
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  scenarios.value = allScenarios.value.slice(start, end);
};

// 处理岗位选择变化
const handleJobPositionChange = () => {
  currentPage.value = 1;
  fetchScenarios();
};

// 搜索场景
const searchScenarios = () => {
  currentPage.value = 1;
  fetchScenarios();
};

// 切换页码
const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  updatePagedScenarios();
};

// 获取岗位名称
const getJobName = (jobId) => {
  const job = jobs.value.find(job => job.id == jobId);
  return job ? job.name : '未知岗位';
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN');
};

// 添加场景
const showAddScenarioModal = () => {
  isEditing.value = false;
  scenarioForm.value = {
    id: '',
    jobId: filters.value.jobPosition || '',
    question: ''
  };
  showScenarioModal.value = true;
};

// 编辑场景
const editScenario = (scenario) => {
  isEditing.value = true;
  scenarioForm.value = {
    id: scenario.id,
    jobId: scenario.jobId,
    question: scenario.question
  };
  showScenarioModal.value = true;
};

// 保存场景
const saveScenario = async () => {
  // 表单验证
  if (!scenarioForm.value.question) {
    uni.showToast({
      title: '请输入场景问题',
      icon: 'none'
    });
    return;
  }
  
  if (!isEditing.value && !scenarioForm.value.jobId) {
    uni.showToast({
      title: '请选择关联岗位',
      icon: 'none'
    });
    return;
  }

  try {
    const token = uni.getStorageSync('token');
    if (!token) {
      throw new Error('请先登录');
    }

    let response;
    if (isEditing.value) {
      // 更新场景：POST /api/scenario-question/update
      response = await uni.request({
        url: API.SCENARIO_QUESTION.UPDATE,
        method: 'POST',
        header: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        data: scenarioForm.value
      });
    } else {
      // 创建场景：POST /api/scenario-question/create
      response = await uni.request({
        url: API.SCENARIO_QUESTION.CREATE,
        method: 'POST',
        header: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        data: scenarioForm.value
      });
    }

    if (response.statusCode === 200 && response.data.code === 200) {
      uni.showToast({
        title: isEditing.value ? '更新成功' : '添加成功',
        icon: 'success'
      });
      
      // 刷新场景数据
      await fetchScenarios();
      closeScenarioModal();
    } else {
      throw new Error(response.data.message || (isEditing.value ? '更新失败' : '添加失败'));
    }
  } catch (error) {
    console.error(isEditing.value ? '更新场景失败:' : '添加场景失败:', error);
    uni.showToast({
      title: error.message || (isEditing.value ? '更新失败' : '添加失败'),
      icon: 'none'
    });
    
    // 即使API调用失败，也尝试刷新数据以确保显示最新状态
    await fetchScenarios();
    closeScenarioModal();
  }
};

// 关闭场景弹窗
const closeScenarioModal = () => {
  showScenarioModal.value = false;
};

// 删除场景
const deleteScenario = (scenario) => {
  deleteTarget.value = {
    id: scenario.id,
    question: scenario.question
  };
  showDeleteModal.value = true;
};

// 确认删除
const confirmDelete = async () => {
  try {
    const token = uni.getStorageSync('token');
    if (!token) {
      throw new Error('请先登录');
    }

    // 删除场景：POST /api/scenario-question/delete/{id}
    const response = await uni.request({
      url: `${API.SCENARIO_QUESTION.DELETE(deleteTarget.value.id)}`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (response.statusCode === 200 && response.data.code === 200) {
      uni.showToast({
        title: '删除成功',
        icon: 'success'
      });
      
      // 刷新场景数据
      await fetchScenarios();
      closeDeleteModal();
    } else {
      throw new Error(response.data.message || '删除失败');
    }
  } catch (error) {
    console.error('删除场景失败:', error);
    uni.showToast({
      title: error.message || '删除失败',
      icon: 'none'
    });
    
    // 模拟成功，更新本地数据
    scenarios.value = scenarios.value.filter(item => item.id !== deleteTarget.value.id);
    
    closeDeleteModal();
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

// 监听岗位选择变化
watch(() => filters.value.jobPosition, (newVal) => {
  if (newVal) {
    currentPage.value = 1;
    fetchScenarios();
  } else {
    scenarios.value = [];
  }
});

// 生命周期钩子
onMounted(async () => {
  await fetchJobs();
  await fetchScenarios();
});
</script>

<style scoped>
.scenario-management-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  transition: background-color 0.3s ease;
}

.scenario-management-container.eye-care {
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

/* 岗位选择面板 */
.job-selection-panel {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.section-title {
  margin: 0 0 15px 0;
  font-size: 1.4em;
  color: #333;
  position: relative;
  padding-left: 15px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 5px;
  background: linear-gradient(135deg, #4A6FFF, #6B8CFF);
  border-radius: 3px;
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
  height: 40px;
  box-sizing: border-box;
}

/* 自定义下拉样式 */
.custom-select { position: relative; width: 100%; }
.select-trigger { width: 100%; display: flex; justify-content: space-between; align-items: center; padding: 10px 12px; border: 1px solid #ddd; border-radius: 6px; background: #fff; color: #333; cursor: pointer; transition: all 0.3s ease; height: 40px; box-sizing: border-box; }
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

.search-group {
  display: flex;
  gap: 10px;
  align-items: center;
  height: 40px;
}

.search-group input {
  flex: 1;
  height: 40px;
}

.search-btn {
  background: #4A6FFF;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 0 20px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-btn:hover {
  background: #3A5FEF;
}

.add-btn {
  background: #4A6FFF;
  color: white;
  border: none;
  border-radius: 6px;
  height: 40px;
  padding: 0 15px;
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

.add-btn:hover {
  background: #3A5FEF;
}

.add-btn:disabled {
  background: #a0aec0;
  cursor: not-allowed;
}

.add-icon {
  font-size: 1.2em;
  font-weight: bold;
}

/* 场景列表 */
.scenarios-panel {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.scenarios-table {
  width: 100%;
  border-collapse: collapse;
}

.table-header {
  display: grid;
  grid-template-columns: 3fr 1fr 1fr 1fr;
  background: #f8f9fa;
  padding: 12px 15px;
  font-weight: bold;
  border-bottom: 1px solid #e9ecef;
  border-radius: 8px 8px 0 0;
}

.table-row {
  display: grid;
  grid-template-columns: 3fr 1fr 1fr 1fr;
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

.scenario-question {
  font-weight: 500;
}

.scenario-job {
  color: #666;
}

.scenario-created {
  color: #666;
  text-align: center;
}

.scenario-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: center;
}

.edit-btn, .delete-btn {
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

.edit-btn, .delete-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.edit-btn {
  background: #4A6FFF;
}

.delete-btn {
  background: #F44336;
}

.edit-icon, .delete-icon {
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

.delete-icon {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='3 6 5 6 21 6'%3E%3C/polyline%3E%3Cpath d='M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2'%3E%3C/path%3E%3Cline x1='10' y1='11' x2='10' y2='17'%3E%3C/line%3E%3Cline x1='14' y1='11' x2='14' y2='17'%3E%3C/line%3E%3C/svg%3E");
}

.empty-list {
  text-align: center;
  padding: 30px;
  color: #666;
  font-style: italic;
}

.no-selection-hint {
  text-align: center;
  padding: 50px 0;
  color: #666;
}

.hint-icon {
  font-size: 3em;
  margin-bottom: 15px;
  color: #4A6FFF;
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
  max-width: 600px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  margin: 0 auto;
}

.scenario-modal {
  max-width: 600px;
}

.delete-modal {
  max-width: 400px;
}

.modal-header {
  padding: 18px 22px;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #4A6FFF, #6B8CFF);
}

.modal-header h3 {
  margin: 0;
  color: #fff;
  font-weight: 600;
}

.close-btn {
  font-size: 20px;
  color: #fff;
  cursor: pointer;
  transition: opacity 0.3s ease;
  opacity: 0.85;
}

.close-btn:hover {
  opacity: 1;
}

.modal-body {
  padding: 22px;
  text-align: left;
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
  width: 100%;
  text-align: left;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #1f2942;
  font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1em;
  box-sizing: border-box;
  display: block;
}

.input-wrapper { position: relative; background: #f8f9fa; border-radius: 10px; padding: 10px; border: 1px solid #e9ecef; }
.input-wrapper:focus-within { background: #fff; box-shadow: 0 0 0 3px rgba(74,111,255,0.15); border-color: #4A6FFF; }
.form-group textarea { width: 100%; min-height: 140px; resize: vertical; border: none; background: transparent; outline: none; font-size: 14px; color: #333; }
.input-footer { display: flex; justify-content: flex-end; margin-top: 6px; }
.char-count { color: #999; font-size: 12px; }
.char-count.warning { color: #dc3545; }

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
  box-shadow: 0 4px 10px rgba(74,111,255,0.25);
}

.save-btn:hover {
  background: #3A5FEF;
  transform: translateY(-1px);
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
.scenario-management-container.eye-care .job-selection-panel,
.scenario-management-container.eye-care .scenarios-panel,
.scenario-management-container.eye-care .modal-content {
  background-color: #f8f9e8;
}
.scenario-management-container.eye-care .modal-header { background: linear-gradient(135deg, #3A5FEF, #4A6FFF); }
.scenario-management-container.eye-care .input-wrapper { background: #f0f2e6; border-color: #d1d5db; }
.scenario-management-container.eye-care .input-wrapper:focus-within { box-shadow: 0 0 0 3px rgba(58,95,239,0.15); }

.scenario-management-container.eye-care .select-trigger { background: #f0f2e6; border-color: #d1d5db; }
.scenario-management-container.eye-care .select-options { background: #f8f9e8; border-color: #d1d5db; }
.scenario-management-container.eye-care .select-option:hover { background: #eef1e1; }
.scenario-management-container.eye-care .select-option.active { background: #e6ead6; }

.scenario-management-container.eye-care .table-header {
  background-color: #f0f2e6;
}

.scenario-management-container.eye-care .table-row:hover {
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
  
  .scenario-actions {
    justify-content: flex-start;
  }
}
</style>

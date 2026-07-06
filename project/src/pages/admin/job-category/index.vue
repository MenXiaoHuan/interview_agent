<template>
  <div class="job-management-container" :class="{ 'eye-care': isEyeCareMode }">
    <div class="header">
      <span class="back-icon" @click="goBack" aria-label="返回" role="button">‹</span>
      <h1 class="page-title">岗位分类管理</h1>
    </div>

    <div class="management-content">

      <!-- 分类管理区域 -->
      <div class="category-management">
        <div class="section-header">
          <h2>一级分类管理</h2>
          <div class="section-actions">
            <button class="add-btn" @click="showAddPrimaryModal">
              <span class="add-icon">+</span> 添加一级分类
            </button>
          </div>
        </div>

        <!-- 一级分类列表 -->
        <div class="category-list" v-if="!isLoading">
          <div v-for="category in primaryCategories" :key="category.id" class="category-item">
                         <div class="category-info">
               <div class="category-name">{{ category.name }}</div>
             </div>
            <div class="category-actions">
              <button class="edit-btn" @click="editPrimaryCategory(category)" title="编辑">
                <i class="edit-icon"></i>
              </button>
              <button class="delete-btn" @click="deletePrimaryCategory(category)" title="删除">
                <i class="delete-icon"></i>
              </button>
            </div>
          </div>
          <div v-if="primaryCategories.length === 0" class="empty-list">
            暂无一级分类数据
          </div>
        </div>
        <div v-else class="loading-container">
          <div class="loading-spinner"></div>
          <p class="loading-text">加载中...</p>
        </div>

        <div class="section-header secondary-header">
          <h2>二级分类管理</h2>
          <div class="section-actions">
            <button class="add-btn" @click="showAddSecondaryModal" :disabled="primaryCategories.length === 0">
              <span class="add-icon">+</span> 添加二级分类
            </button>
          </div>
        </div>

        <!-- 二级分类筛选 -->
        <div class="filter-section">
          <div class="filter-label">按一级分类筛选：</div>
          <div class="custom-select" :class="{ open: filterDropdownOpen }" @click.stop>
            <button type="button" class="select-trigger" @click="toggleFilterDropdown" aria-haspopup="listbox" :aria-expanded="filterDropdownOpen">
              <span class="trigger-text">{{ filterSelectedLabel }}</span>
              <svg class="caret" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </button>
            <ul v-if="filterDropdownOpen" class="select-options" role="listbox">
              <li 
                class="select-option" 
                :class="{ active: selectedPrimaryFilter === '' }" 
                role="option" 
                :aria-selected="selectedPrimaryFilter === ''"
                @click="selectFilterCategory('')"
              >
                <svg class="option-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="8" stroke="currentColor" stroke-width="2"/></svg>
                <span class="option-text">全部</span>
                <svg v-if="selectedPrimaryFilter===''
                " class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </li>
              <li 
                v-for="category in primaryCategories" 
                :key="category.id" 
                class="select-option" 
                :class="{ active: selectedPrimaryFilter === category.id }" 
                role="option"
                :aria-selected="selectedPrimaryFilter === category.id"
                @click="selectFilterCategory(category.id)"
              >
                <svg class="option-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M4 7h16v10H4z" stroke="currentColor" stroke-width="2"/><path d="M4 9l8 5 8-5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                <span class="option-text">{{ category.name }}</span>
                <svg v-if="selectedPrimaryFilter===category.id" class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </li>
              <li v-if="primaryCategories.length===0" class="select-empty">暂无一级分类</li>
            </ul>
          </div>
        </div>

        <!-- 二级分类列表 -->
        <div class="category-list" v-if="!isLoading">
          <div v-for="category in filteredSecondaryCategories" :key="category.id" class="category-item">
                         <div class="category-info">
               <div class="category-name">{{ category.name }}</div>
               <div class="category-parent">所属一级分类：{{ getCategoryParentName(category) }}</div>
             </div>
            <div class="category-actions">
              <button class="edit-btn" @click="editSecondaryCategory(category)" title="编辑">
                <i class="edit-icon"></i>
              </button>
              <button class="delete-btn" @click="deleteSecondaryCategory(category)" title="删除">
                <i class="delete-icon"></i>
              </button>
            </div>
          </div>
          <div v-if="filteredSecondaryCategories.length === 0" class="empty-list">
            暂无二级分类数据
          </div>
        </div>
        <div v-else class="loading-container">
          <div class="loading-spinner"></div>
          <p class="loading-text">加载中...</p>
        </div>
      </div>
    </div>

    <!-- 添加/编辑一级分类弹窗 -->
    <div class="modal" v-if="showPrimaryModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑' : '添加' }}一级分类</h3>
          <span class="close-btn" @click="closePrimaryModal">&times;</span>
        </div>
        <div class="modal-body">
          <div class="form-container">
            <div class="form-group">
              <label for="primaryName">分类名称</label>
              <div class="input-wrapper">
                <input type="text" id="primaryName" v-model="primaryForm.name" placeholder="请输入分类名称">
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closePrimaryModal">取消</button>
          <button class="save-btn" @click="savePrimaryCategory">保存</button>
        </div>
      </div>
    </div>

    <!-- 添加/编辑二级分类弹窗 -->
    <div class="modal" v-if="showSecondaryModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑' : '添加' }}二级分类</h3>
          <span class="close-btn" @click="closeSecondaryModal">&times;</span>
        </div>
        <div class="modal-body">
          <div class="form-container">
            <div class="form-group">
              <label for="parentCategory">所属一级分类</label>
              <div class="input-wrapper">
                <div class="custom-select" :class="{ open: parentDropdownOpen }" @click.stop>
                  <button type="button" class="select-trigger" @click="toggleParentDropdown" aria-haspopup="listbox" :aria-expanded="parentDropdownOpen">
                    <span class="trigger-text">{{ parentSelectedLabel }}</span>
                    <svg class="caret" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                  </button>
                  <ul v-if="parentDropdownOpen" class="select-options" role="listbox">
                    <li 
                      v-for="category in primaryCategories" 
                      :key="category.id" 
                      class="select-option" 
                      :class="{ active: secondaryForm.parentId === category.id }" 
                      role="option"
                      :aria-selected="secondaryForm.parentId === category.id"
                      @click="selectParentCategory(category)"
                    >
                      <svg class="option-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M4 7h16v10H4z" stroke="currentColor" stroke-width="2"/><path d="M4 9l8 5 8-5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                      <span class="option-text">{{ category.name }}</span>
                      <svg v-if="secondaryForm.parentId===category.id" class="check-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                    </li>
                    <li v-if="primaryCategories.length===0" class="select-empty">暂无一级分类</li>
                  </ul>
                </div>
              </div>
            </div>
            <div class="form-group">
              <label for="secondaryName">分类名称</label>
              <div class="input-wrapper">
                <input type="text" id="secondaryName" v-model="secondaryForm.name" placeholder="请输入分类名称">
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeSecondaryModal">取消</button>
          <button class="save-btn" @click="saveSecondaryCategory">保存</button>
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
          <p>确定要删除"{{ deleteTarget.name }}"吗？</p>
          <p class="warning-text" v-if="deleteTarget.level === 'primary'">
            注意：删除一级分类将同时删除其下所有二级分类和岗位！
          </p>
          <p class="warning-text" v-else>
            注意：删除二级分类将同时删除其下所有岗位！
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
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
import { API, getJobCategoriesTree, createFirstLevelCategory, createSecondLevelCategory, updateJobCategory, deleteFirstLevelCategory, deleteSecondLevelCategory } from '@/utils/api/pages/admin';

const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);

// 数据状态
const isLoading = ref(false);
const primaryCategories = ref([]);
const secondaryCategories = ref([]);
const selectedPrimaryFilter = ref('');
const filterDropdownOpen = ref(false);
const filterSelectedLabel = computed(() => {
  const id = selectedPrimaryFilter.value;
  if (!id) return '全部';
  const found = primaryCategories.value.find(c => c.id === id);
  return found ? found.name : '全部';
});
const toggleFilterDropdown = () => { filterDropdownOpen.value = !filterDropdownOpen.value; };
const selectFilterCategory = (id) => { selectedPrimaryFilter.value = id || ''; filterDropdownOpen.value = false; };

// 模态框状态
const showPrimaryModal = ref(false);
const showSecondaryModal = ref(false);
const showDeleteModal = ref(false);
const isEditing = ref(false);

// 表单数据
const primaryForm = ref({
  id: '',
  name: '',
  sortOrder: 0
});

const secondaryForm = ref({
  id: '',
  name: '',
  parentId: '',
  sortOrder: 0
});

// 自定义下拉（所属一级分类）
const parentDropdownOpen = ref(false);
const parentSelectedLabel = computed(() => {
  const id = secondaryForm.value.parentId;
  if (!id) return '请选择一级分类';
  const found = primaryCategories.value.find(c => c.id === id);
  return found ? found.name : '请选择一级分类';
});
const toggleParentDropdown = () => { parentDropdownOpen.value = !parentDropdownOpen.value; };
const selectParentCategory = (category) => {
  secondaryForm.value.parentId = category.id;
  parentDropdownOpen.value = false;
};

// 删除目标
const deleteTarget = ref({
  id: '',
  name: '',
  level: '' // 'primary' 或 'secondary'
});

// 计算属性
const filteredSecondaryCategories = computed(() => {
  if (!selectedPrimaryFilter.value) {
    return secondaryCategories.value;
  }
  return secondaryCategories.value.filter(category => category.parentId === selectedPrimaryFilter.value);
});

// 获取分类数据
const fetchCategories = async () => {
  isLoading.value = true;
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
  } finally {
    isLoading.value = false;
  }
};



// 获取二级分类的父分类名称
const getCategoryParentName = (category) => {
  const parent = primaryCategories.value.find(p => p.id === category.parentId);
  return parent ? parent.name : '未知';
};

// 添加一级分类
const showAddPrimaryModal = () => {
  isEditing.value = false;
  primaryForm.value = {
    id: '',
    name: '',
    sortOrder: 0
  };
  showPrimaryModal.value = true;
};

// 编辑一级分类
const editPrimaryCategory = (category) => {
  isEditing.value = true;
  primaryForm.value = {
    id: category.id,
    name: category.name,
    sortOrder: category.sortOrder || 0
  };
  showPrimaryModal.value = true;
};

// 保存一级分类
const savePrimaryCategory = async () => {
  // 表单验证
  if (!primaryForm.value.name) {
    uni.showToast({
      title: '请输入分类名称',
      icon: 'none'
    });
    return;
  }

  try {
    let response;
    if (isEditing.value) {
      response = await updateJobCategory({
        id: primaryForm.value.id,
        name: primaryForm.value.name,
        sortOrder: primaryForm.value.sortOrder || 0
      });
    } else {
      response = await createFirstLevelCategory({
        name: primaryForm.value.name,
        level: 1,
        sortOrder: primaryForm.value.sortOrder || 0
      });
    }

    if (response.code === 200) {
      uni.showToast({
        title: isEditing.value ? '更新成功' : '添加成功',
        icon: 'success'
      });
      
      // 刷新分类数据
      await fetchCategories();
      closePrimaryModal();
    } else {
      throw new Error(response.message || (isEditing.value ? '更新失败' : '添加失败'));
    }
  } catch (error) {
    console.error(isEditing.value ? '更新一级分类失败:' : '添加一级分类失败:', error);
    uni.showToast({
      title: error.message || (isEditing.value ? '更新失败' : '添加失败'),
      icon: 'none'
    });
  }
};

// 关闭一级分类弹窗
const closePrimaryModal = () => {
  showPrimaryModal.value = false;
};

// 添加二级分类
const showAddSecondaryModal = () => {
  isEditing.value = false;
  secondaryForm.value = {
    id: '',
    name: '',
    parentId: '',
    sortOrder: 0
  };
  showSecondaryModal.value = true;
};

// 编辑二级分类
const editSecondaryCategory = (category) => {
  isEditing.value = true;
  secondaryForm.value = {
    id: category.id,
    name: category.name,
    parentId: category.parentId,
    sortOrder: category.sortOrder || 0
  };
  showSecondaryModal.value = true;
};

// 保存二级分类
const saveSecondaryCategory = async () => {
  // 表单验证
  if (!secondaryForm.value.name) {
    uni.showToast({
      title: '请输入分类名称',
      icon: 'none'
    });
    return;
  }

  if (!secondaryForm.value.parentId) {
    uni.showToast({
      title: '请选择所属一级分类',
      icon: 'none'
    });
    return;
  }

  try {
    let response;
    if (isEditing.value) {
      response = await updateJobCategory({
        id: secondaryForm.value.id,
        name: secondaryForm.value.name,
        sortOrder: secondaryForm.value.sortOrder || 0
      });
    } else {
      response = await createSecondLevelCategory({
        name: secondaryForm.value.name,
        level: 2,
        parentId: secondaryForm.value.parentId,
        sortOrder: secondaryForm.value.sortOrder || 0
      });
    }

    if (response.code === 200) {
      uni.showToast({
        title: isEditing.value ? '更新成功' : '添加成功',
        icon: 'success'
      });
      
      // 刷新分类数据
      await fetchCategories();
      closeSecondaryModal();
    } else {
      throw new Error(response.message || (isEditing.value ? '更新失败' : '添加失败'));
    }
  } catch (error) {
    console.error(isEditing.value ? '更新二级分类失败:' : '添加二级分类失败:', error);
    uni.showToast({
      title: error.message || (isEditing.value ? '更新失败' : '添加失败'),
      icon: 'none'
    });
  }
};

// 关闭二级分类弹窗
const closeSecondaryModal = () => {
  showSecondaryModal.value = false;
  parentDropdownOpen.value = false;
};

// 删除一级分类
const deletePrimaryCategory = (category) => {
  deleteTarget.value = {
    id: category.id,
    name: category.name,
    level: 'primary'
  };
  showDeleteModal.value = true;
};

// 删除二级分类
const deleteSecondaryCategory = (category) => {
  deleteTarget.value = {
    id: category.id,
    name: category.name,
    level: 'secondary'
  };
  showDeleteModal.value = true;
};

// 确认删除
const confirmDelete = async () => {
  try {
    let response;
    if (deleteTarget.value.level === 'primary') {
      response = await deleteFirstLevelCategory(deleteTarget.value.id);
    } else {
      response = await deleteSecondLevelCategory(deleteTarget.value.id);
    }

    if (response.code === 200) {
      uni.showToast({
        title: '删除成功',
        icon: 'success'
      });
      
      // 刷新分类数据
      await fetchCategories();
      closeDeleteModal();
    } else {
      throw new Error(response.message || '删除失败');
    }
  } catch (error) {
    console.error('删除分类失败:', error);
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

// 生命周期钩子
onMounted(async () => {
  await fetchCategories();
});
</script>

<style scoped>
.job-management-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  transition: background-color 0.3s ease;
}

.job-management-container.eye-care {
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



/* 分类管理区域 */
.category-management {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 2px solid #f0f2f5;
  padding-bottom: 15px;
}

.section-actions {
  display: flex;
  justify-content: flex-end;
}

.section-header h2 {
  margin: 0;
  font-size: 1.5em;
  color: #333;
  position: relative;
  padding-left: 15px;
}

.section-header h2::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 5px;
  background: linear-gradient(135deg, #4A6FFF, #6B8CFF);
  border-radius: 3px;
}

.secondary-header {
  margin-top: 40px;
}

.add-btn {
  background: linear-gradient(135deg, #4A6FFF, #6B8CFF);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 1em;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(74, 111, 255, 0.2);
}

.add-btn:hover {
  background: linear-gradient(135deg, #3A5FEF, #5A7CEF);
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(74, 111, 255, 0.3);
}

.add-btn:disabled {
  background: linear-gradient(135deg, #a0aec0, #cbd5e0);
  cursor: not-allowed;
  box-shadow: none;
}

.add-icon {
  font-size: 1.2em;
  font-weight: bold;
}

/* 筛选区域 */
.filter-section {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 15px;
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
}

.filter-label {
  font-size: 1em;
  color: #555;
  font-weight: 500;
}

/* 统一使用自定义下拉，无需原生select样式 */

/* 分类列表 */
.category-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.category-item {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.category-item:hover {
  background: #e9ecef;
  border-left-color: #4A6FFF;
  transform: translateX(5px);
}

.category-info {
  flex: 1;
}

.category-name {
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
  font-size: 1.1em;
}

.category-parent {
  font-size: 0.9em;
  color: #666;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.category-parent::before {
  content: '📂';
  font-size: 1em;
}



.category-actions {
  display: flex;
  gap: 10px;
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
  padding: 40px;
  color: #666;
  font-style: italic;
  background: #f8f9fa;
  border-radius: 10px;
  margin: 20px 0;
}

/* 加载动画 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 5px solid rgba(74, 111, 255, 0.1);
  border-top: 5px solid #4A6FFF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

.loading-text {
  color: #666;
  font-size: 1.1em;
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
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background: white;
  border-radius: 20px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  animation: slideIn 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

@keyframes slideIn {
  from { transform: translateY(-30px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.delete-modal {
  max-width: 400px;
}

.modal-header {
  padding: 25px 30px;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
}

.modal-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.4em;
  font-weight: 600;
}

.close-btn {
  font-size: 1.8em;
  color: #adb5bd;
  cursor: pointer;
  transition: color 0.3s ease;
  line-height: 1;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 25px;
  display: flex;
  justify-content: center;
}

.form-container {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.modal-footer {
  padding: 25px 30px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
}

.form-group {
  margin-bottom: 25px;
  text-align: center;
}

.form-group label {
  display: block;
  margin-bottom: 12px;
  color: #333;
  font-weight: 600;
  font-size: 1.1em;
  text-align: center;
}

.input-wrapper {
  display: flex;
  justify-content: center;
  width: 100%;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  max-width: 300px;
  padding: 15px 20px;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  font-size: 1em;
  transition: all 0.3s ease;
  background-color: #ffffff;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 下拉选项文字左对齐 */
.form-group select option {
  text-align: left;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #4A6FFF;
  box-shadow: 0 0 0 4px rgba(74, 111, 255, 0.15), 0 4px 12px rgba(74, 111, 255, 0.1);
  outline: none;
  background-color: #ffffff;
  text-align: center;
  transform: translateY(-1px);
}

/* 自定义下拉样式 */
.custom-select { position: relative; width: 100%; max-width: 300px; }
.select-trigger { width: 100%; display: flex; justify-content: space-between; align-items: center; padding: 14px 16px; border: 2px solid #e9ecef; border-radius: 12px; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.05); color: #333; cursor: pointer; transition: all 0.3s ease; }
.select-trigger:hover { border-color: #4A6FFF; box-shadow: 0 0 0 4px rgba(74,111,255,0.12); }
.custom-select.open .select-trigger { border-color: #4A6FFF; box-shadow: 0 0 0 4px rgba(74,111,255,0.15), 0 4px 12px rgba(74,111,255,0.1); }
.caret { color: #4A6FFF; transition: transform 0.2s ease; }
.custom-select.open .caret { transform: rotate(180deg); }
.select-options { position: absolute; left: 0; right: 0; top: calc(100% + 8px); background: #fff; border: 2px solid #e9ecef; border-radius: 12px; box-shadow: 0 10px 24px rgba(0,0,0,0.12); max-height: 220px; overflow: auto; z-index: 10; padding: 8px; }
.select-option { display: flex; align-items: center; gap: 10px; padding: 10px 12px; border-radius: 8px; color: #333; cursor: pointer; transition: background 0.2s ease; }
.select-option:hover { background: #f5f7ff; }
.select-option.active { background: #e9f0ff; }
.option-icon, .check-icon { color: #4A6FFF; }
.select-empty { padding: 12px; text-align: center; color: #999; }

/* 护眼模式适配 */
.job-management-container.eye-care .select-trigger { background: #f0f2e6; border-color: #d1d5db; }
.job-management-container.eye-care .select-trigger:hover { box-shadow: 0 0 0 4px rgba(58,95,239,0.12); }
.job-management-container.eye-care .select-options { background: #f8f9e8; border-color: #d1d5db; }
.job-management-container.eye-care .select-option:hover { background: #eef1e1; }
.job-management-container.eye-care .select-option.active { background: #e6ead6; }



.cancel-btn,
.save-btn,
.delete-confirm-btn {
  padding: 12px 24px;
  border-radius: 12px;
  font-size: 1em;
  cursor: pointer;
  border: none;
  transition: all 0.3s ease;
  font-weight: 600;
  min-width: 100px;
}

.cancel-btn {
  background: #f8f9fa;
  color: #666;
  border: 2px solid #e9ecef;
}

.cancel-btn:hover {
  background: #e9ecef;
  color: #333;
  border-color: #dee2e6;
  transform: translateY(-1px);
}

.save-btn {
  background: linear-gradient(135deg, #4A6FFF, #6B8CFF);
  color: white;
  box-shadow: 0 4px 12px rgba(74, 111, 255, 0.25);
}

.save-btn:hover {
  background: linear-gradient(135deg, #3A5FEF, #5A7CEF);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(74, 111, 255, 0.35);
}

.delete-confirm-btn {
  background: linear-gradient(135deg, #dc3545, #e74c3c);
  color: white;
  box-shadow: 0 4px 10px rgba(220, 53, 69, 0.2);
}

.delete-confirm-btn:hover {
  background: linear-gradient(135deg, #c82333, #d32f2f);
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(220, 53, 69, 0.3);
}

.warning-text {
  color: #dc3545;
  font-weight: 500;
  background: rgba(220, 53, 69, 0.1);
  padding: 10px 15px;
  border-radius: 8px;
  margin-top: 10px;
  border-left: 4px solid #dc3545;
}

/* 护眼模式适配 */
.job-management-container.eye-care .stat-card,
.job-management-container.eye-care .category-management {
  background-color: #f8f9e8;
}

.job-management-container.eye-care .category-item {
  background-color: #f0f2e6;
}

.job-management-container.eye-care .category-item:hover {
  background-color: #e6e8dc;
}

.job-management-container.eye-care .filter-section,
.job-management-container.eye-care .empty-list,
.job-management-container.eye-care .modal-header,
.job-management-container.eye-care .modal-footer,
.job-management-container.eye-care .form-group input,
.job-management-container.eye-care .form-group select {
  background-color: #f0f2e6;
  text-align: center;
}

.job-management-container.eye-care .form-group input:focus,
.job-management-container.eye-care .form-group select:focus {
  background-color: #f8f9e8;
  text-align: center;
}

/* 护眼模式下下拉选项文字左对齐 */
.job-management-container.eye-care .form-group select option {
  text-align: left;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .category-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .category-actions {
    margin-top: 15px;
    width: 100%;
    justify-content: flex-end;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .add-btn {
    width: 100%;
    justify-content: center;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .filter-select {
    width: 100%;
  }
  
  .page-title {
    font-size: 1.5em;
  }
  
  .section-header h2 {
    font-size: 1.3em;
  }
  
  /* 移动端模态框优化 */
  .modal-content {
    width: 95%;
    max-width: 400px;
  }
  
  .form-container {
    max-width: 100%;
  }
  
  .form-group input,
  .form-group select,
  .form-group textarea {
    max-width: 100%;
  }
  
  .modal-header,
  .modal-footer {
    padding: 20px;
  }
  
  .modal-body {
    padding: 20px;
  }
}
</style>

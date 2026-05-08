<template>
  <div class="user-permissions-container" :class="{ 'eye-care': isEyeCareMode }">
    <div class="header">
      <span class="back-icon" @click="goBack" aria-label="返回" role="button">‹</span>
      <h1 class="page-title">用户权限管理</h1>
    </div>



    <div class="batch-actions">
      <div class="action-section">
        <h3>批量操作</h3>
        <div class="batch-controls">
          <div class="left-side">
            <select v-model="selectedUserType" class="type-select">
              <option value="1">普通用户</option>
              <option value="2">会员用户</option>
              <option value="3">管理员</option>
            </select>
          </div>
          <div class="right-side">
            <button class="batch-btn" @click="applyBatchChange">
              应用到选中用户
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="users-table">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p class="loading-text">加载中...</p>
      </div>
      <template v-else>
        <div class="table-header">
          <div class="header-cell checkbox-cell">
            <span class="select-all-label" @click="toggleSelectAll">全选</span>
            <span class="user-label">用户</span>
            <input 
              type="checkbox" 
              v-model="allSelected" 
              @change="toggleSelectAll"
              class="select-all-checkbox"
              id="select-all"
              style="display: none;"
            >
          </div>
          <div class="header-cell">当前权限</div>
          <div class="header-cell">注册时间</div>
          <div class="header-cell">操作</div>
        </div>
        
        <div 
          v-for="(user, index) in users" 
          :key="user.id" 
          class="table-row"
          :class="{ 'selected-row': selectedUsers.includes(user.id) }"
          @click="toggleUserSelection(user.id)"
        >
          <div class="table-cell checkbox-cell" @click.stop>
            <span class="row-number">{{ index + 1 }}</span>
            <input 
              type="checkbox" 
              v-model="selectedUsers" 
              :value="user.id"
              class="user-checkbox"
              :id="`user-${user.id}`"
              style="display: none;"
            >
            <div class="user-info">
              <img 
                :src="resolveUserAvatar(user)" 
                :alt="user.username"
                class="user-avatar"
                @error="handleAvatarError($event, user)"
              >
              <div class="user-details">
                <div class="username">{{ user.username }}</div>
                <div class="nickname" v-if="user.nickname">{{ user.nickname }}</div>
                <div class="email" v-if="user.email">{{ user.email }}</div>
              </div>
            </div>
          </div>
          
          <div class="table-cell">
            <span class="permission-badge" :class="getPermissionClass(user.userType)">
              {{ getPermissionText(user.userType) }}
            </span>
          </div>
          
          <div class="table-cell">
            {{ formatDate(user.createdAt) }}
          </div>
          
          <div class="table-cell actions-cell" @click.stop>
            <select 
              v-model="user.userType" 
              class="permission-select"
              @change="updateUserPermission(user)"
            >
              <option value="1">普通用户</option>
              <option value="2">会员用户</option>
              <option value="3">管理员</option>
            </select>
          </div>
        </div>
      </template>
    </div>

    <div class="selected-info-container" v-if="selectedUsers.length > 0">
      <div class="selected-info">
        <div class="left-side">
          <p>已选择 {{ selectedUsers.length }} 个用户</p>
        </div>
        <div class="right-side">
          <button class="clear-selection-btn" @click="clearSelection">
            清除选择
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
import { API, getUserList, updateUserType } from '@/utils/api';
import { applyAvatarFallback, resolveUserAvatar } from '@/utils/avatar';

const userStore = useUserStore();
const { isEyeCareMode, userInfo } = storeToRefs(userStore);

const users = ref([]);
const selectedUsers = ref([]);
const selectedUserType = ref('1');
const loading = ref(false);

// 计算属性和响应式变量
const allSelected = ref(false);

// 监听选中用户变化，更新全选状态
const updateAllSelected = () => {
  allSelected.value = users.value.length > 0 && selectedUsers.value.length === users.value.length;
};



// 获取用户列表
const fetchUsers = async () => {
  loading.value = true;
  try {
    const response = await getUserList(1, 100); // 获取前100个用户
    if (response.code === 200) {
      // 过滤掉当前登录用户
      users.value = response.data.filter(user => user.username !== userInfo.value?.username);
    } else {
      throw new Error(response.message || '获取用户列表失败');
    }
  } catch (error) {
    console.error('获取用户列表失败:', error);
    uni.showToast({
      title: error.message || '获取用户列表失败',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
};

// 切换全选
const toggleSelectAll = () => {
  // 切换全选状态
  allSelected.value = !allSelected.value;
  
  if (allSelected.value) {
    // 全选所有用户
    selectedUsers.value = users.value.map(user => user.id);
  } else {
    // 取消全选
    selectedUsers.value = [];
  }
  
  // 显示提示
  uni.showToast({
    title: allSelected.value ? '已全选' : '已取消全选',
    icon: 'none',
    duration: 1500
  });
};

// 清除选择
const clearSelection = () => {
  selectedUsers.value = [];
  // 强制更新视图，确保序号不会消失
  users.value = [...users.value];
};

// 切换单个用户选择状态
const toggleUserSelection = (userId) => {
  const index = selectedUsers.value.indexOf(userId);
  if (index === -1) {
    selectedUsers.value.push(userId);
  } else {
    selectedUsers.value.splice(index, 1);
  }
  updateAllSelected();
};

// 批量修改权限
const applyBatchChange = async () => {
  if (selectedUsers.value.length === 0) {
    uni.showToast({
      title: '请先选择用户',
      icon: 'none'
    });
    return;
  }

  try {
    uni.showLoading({ title: '更新中...' });

    // 获取选中的用户信息
    const selectedUserList = users.value.filter(user => selectedUsers.value.includes(user.id));
    
    // 批量更新用户权限
    const updatePromises = selectedUserList.map(user => 
      updateUserType(user.username, parseInt(selectedUserType.value))
    );

    await Promise.all(updatePromises);

    // 更新本地数据
    users.value.forEach(user => {
      if (selectedUsers.value.includes(user.id)) {
        user.userType = parseInt(selectedUserType.value);
      }
    });

    selectedUsers.value = [];

    uni.showToast({
      title: '批量更新成功',
      icon: 'success'
    });
  } catch (error) {
    console.error('批量更新失败:', error);
    uni.showToast({
      title: error.message || '批量更新失败',
      icon: 'none'
    });
  } finally {
    uni.hideLoading();
  }
};

// 更新单个用户权限
const updateUserPermission = async (user) => {
  try {
    const response = await updateUserType(user.username, user.userType);
    
    if (response.code === 200) {
      // 强制更新用户对象的userType，确保权限标签正确显示
      user.userType = parseInt(user.userType);
      uni.showToast({
        title: '权限更新成功',
        icon: 'success'
      });
    } else {
      throw new Error(response.message || '更新失败');
    }
  } catch (error) {
    console.error('更新用户权限失败:', error);
    uni.showToast({
      title: error.message || '更新失败',
      icon: 'none'
    });
  }
};

// 获取权限文本
const getPermissionText = (userType) => {
  switch (userType) {
    case 1: return '普通用户';
    case 2: return '会员用户';
    case 3: return '管理员';
    default: return '未知';
  }
};

// 获取权限样式类
const getPermissionClass = (userType) => {
  switch (userType) {
    case 1: return 'permission-normal';
    case 2: return 'permission-vip';
    case 3: return 'permission-admin';
    default: return 'permission-unknown';
  }
};

// 格式化日期
const formatDate = (dateTime) => {
  if (!dateTime) return '-';
  const date = new Date(dateTime);
  return date.toLocaleDateString('zh-CN');
};

// 检查是否为当前用户
const isCurrentUser = (user) => {
  return user.username === userInfo.value?.username;
};

// 处理头像加载错误
const handleAvatarError = (event, user) => {
  applyAvatarFallback(event, user);
};

// 返回
const goBack = () => {
  uni.navigateTo({
    url: '/pages/admin/index'
  });
};

onMounted(() => {
  fetchUsers();
});

// 监听selectedUsers变化，更新全选状态
watch(selectedUsers, () => {
  updateAllSelected();
});
</script>

<style scoped>
.user-permissions-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  transition: background-color 0.3s ease;
}

.user-permissions-container.eye-care {
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



.batch-actions {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-top: 20px;
}

.action-section h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 1.2em;
}

.batch-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.left-side {
  flex: 1;
}

.right-side {
  text-align: right;
}

.batch-btn {
  margin-left: auto;
}

.type-select {
  padding: 10px 15px;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 16px;
  background: white;
  min-width: 150px;
}

.batch-btn {
  padding: 10px 20px;
  background: #4A6FFF;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.batch-btn:hover {
  background: #3a5fef;
}

.users-table {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.table-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  background: #f8f9fa;
  padding: 15px;
  font-weight: bold;
  color: #333;
  border-bottom: 1px solid #e9ecef;
}

.table-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  cursor: pointer;
  min-height: 80px;
  align-items: center;
}

.table-row:hover {
  background-color: #f8f9fa;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.selected-row {
  background-color: #e8f0fe;
  border-left: 4px solid #4A6FFF;
}

.selected-row:hover {
  background-color: #d8e8fd;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.table-cell {
  display: flex;
  align-items: center;
  padding: 0 10px;
  height: 100%;
}

.checkbox-cell {
  display: flex;
  align-items: center;
  gap: 5px;
  justify-content: flex-start;
}


.table-header .checkbox-cell {
  justify-content: flex-start;
}

.select-all-checkbox,
.user-checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.select-all-label {
  cursor: pointer;
  font-size: 0.9em;
  color: #666;
  display: inline-block;
  margin-right: 5px;
}

.select-icon {
  display: inline-block;
  transform: rotate(90deg);
  margin: 0 2px;
  color: #999;
}

.user-label {
  font-size: 0.9em;
  color: #666;
  margin-left: 5px;
}

.table-header .checkbox-cell {
  display: flex;
  align-items: center;
}

.row-number {
  display: inline-block;
  min-width: 20px;
  text-align: center;
  margin-right: 10px;
  color: #666;
  font-size: 0.9em;
}

.checkbox-cell {
  position: relative;
}

.checkbox-cell .user-checkbox {
  position: absolute;
  opacity: 0;
  cursor: pointer;
  height: 0;
  width: 0;
}

.user-cell {
  justify-content: flex-start;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 100%;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.user-details {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
}

.username {
  font-weight: bold;
  color: #333;
}

.nickname {
  font-size: 0.9em;
  color: #666;
}

.email {
  font-size: 0.8em;
  color: #999;
}

.permission-badge {
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 0.9em;
  font-weight: bold;
  display: inline-block;
  min-width: 80px;
  text-align: center;
}

.permission-normal {
  background: #d4edda;
  color: #155724;
}

.permission-vip {
  background: #fff3cd;
  color: #856404;
}

.permission-admin {
  background: #f8d7da;
  color: #721c24;
}

.permission-unknown {
  background: #e2e3e5;
  color: #383d41;
}

.permission-select {
  padding: 8px 12px;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  background: white;
  font-size: 14px;
  min-width: 120px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.permission-select:hover {
  border-color: #4A6FFF;
  box-shadow: 0 2px 8px rgba(74, 111, 255, 0.2);
}

.permission-select:focus {
  outline: none;
  border-color: #4A6FFF;
  box-shadow: 0 0 0 3px rgba(74, 111, 255, 0.1);
}

.selected-info-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px 20px;
  z-index: 100;
}

.selected-info {
  display: flex;
  align-items: center;
  background: white;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  max-width: 1200px;
  margin: 0 auto;
}

.selected-info p {
  margin: 0;
  color: #333;
  font-weight: bold;
}

.clear-selection-btn {
  padding: 8px 16px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  margin-left: auto;
  float: right;
}

.clear-selection-btn:hover {
  background: #c82333;
}

/* 护眼模式适配 */
.eye-care .batch-actions,
.eye-care .users-table,
.eye-care .selected-info {
  background-color: #f8f9e8;
}

.eye-care .type-select,
.eye-care .permission-select {
  background-color: #f0f2e6;
  border-color: #d1d5db;
}

.eye-care .table-row:hover {
  background-color: #f0f2e6;
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

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .user-permissions-container {
    padding: 15px;
  }

  .batch-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .table-header,
  .table-row {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .header-cell,
  .table-cell {
    padding: 5px 0;
    border-bottom: 1px solid #f0f0f0;
  }

  .table-header {
    display: none;
  }

  .table-row {
    background: white;
    margin-bottom: 10px;
    border-radius: 8px;
    padding: 15px;
  }

  .selected-info {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }
}
</style>

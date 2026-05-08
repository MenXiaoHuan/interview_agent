<template>
  <div class="user-management-container" :class="{ 'eye-care': isEyeCareMode }">
    <div class="header">
      <span class="back-icon" @click="goBack" aria-label="返回" role="button">‹</span>
      <h1 class="page-title">用户列表管理</h1>
    </div>

    <div class="search-section">
      <div class="search-box">
        <input 
          v-model="searchQuery" 
          type="text" 
          placeholder="搜索用户名、昵称或邮箱..."
          class="search-input"
          @input="handleSearch"
        >
        <span class="search-icon">🔍</span>
      </div>
      <div class="filter-section">
        <select v-model="userTypeFilter" class="filter-select" @change="handleFilter">
          <option value="">所有用户类型</option>
          <option value="1">普通用户</option>
          <option value="2">会员用户</option>
          <option value="3">管理员</option>
        </select>
      </div>
    </div>

    <div class="user-list">
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-else-if="filteredUsers.length === 0" class="empty-state">
        <div class="empty-icon">👥</div>
        <p>暂无用户数据</p>
      </div>
      
      <div v-else class="users-grid">
        <div 
          v-for="user in paginatedUsers" 
          :key="user.id" 
          class="user-card"
          :class="{ 'current-user': isCurrentUser(user) }"
          @click="viewUserDetail(user)"
        >
          <div class="user-avatar">
            <img 
              :src="resolveUserAvatar(user)" 
              :alt="user.nickname || user.username"
              @error="handleAvatarError($event, user)"
            >
            <div class="user-type-badge" :class="getUserTypeClass(user.userType)">
              {{ getUserTypeText(user.userType) }}
            </div>
            <!-- 当前用户标记 -->
            <div v-if="isCurrentUser(user)" class="current-user-badge">
              <span>我</span>
            </div>
          </div>
          
          <div class="user-info">
            <h3 class="user-name">{{ user.nickname || user.username }}</h3>
            <p class="user-username">@{{ user.username }}</p>
            <p class="user-email" v-if="user.email">{{ user.email }}</p>
            <p class="user-phone" v-if="user.phone">{{ user.phone }}</p>
          </div>
          
          <div class="user-actions">
            <button 
              class="action-btn edit-btn" 
              @click.stop="editUser(user)"
              title="编辑用户"
            >
              <i class="edit-icon"></i>
            </button>
            <button 
              v-if="!isCurrentUser(user)"
              class="action-btn delete-btn" 
              @click.stop="confirmDeleteUser(user)"
              title="删除用户"
            >
              <i class="delete-icon"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="pagination">
      <button 
        class="page-btn" 
        :class="{ disabled: currentPage === 1 }"
        :disabled="currentPage === 1"
        @click="changePage(currentPage - 1)"
      >
        上一页
      </button>
      <div class="page-info">{{ currentPage }} / {{ totalPages }}</div>
      <button 
        class="page-btn" 
        :class="{ disabled: currentPage === totalPages }"
        :disabled="currentPage === totalPages"
        @click="changePage(currentPage + 1)"
      >
        下一页
      </button>
    </div>

    <!-- 用户详情弹窗 -->
    <div v-if="showUserDetail" class="modal-overlay" @click="closeUserDetail">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>用户详情</h3>
          <span class="close-btn" @click="closeUserDetail">×</span>
        </div>
        <div class="modal-body" v-if="selectedUser">
          <div class="user-profile-header">
            <div class="profile-avatar">
              <img 
                :src="resolveUserAvatar(selectedUser)" 
                :alt="selectedUser.nickname || selectedUser.username"
                @error="handleAvatarError($event, selectedUser)"
              >
            </div>
            <div class="profile-name">
              <h2>{{ selectedUser.nickname || selectedUser.username }}</h2>
              <span class="profile-badge" :class="getUserTypeClass(selectedUser.userType)">
                {{ getUserTypeText(selectedUser.userType) }}
              </span>
            </div>
          </div>
          <div class="detail-section">
            <div class="detail-item">
              <label>用户ID:</label>
              <span>{{ selectedUser.id }}</span>
            </div>
            <div class="detail-item">
              <label>用户名:</label>
              <span>{{ selectedUser.username }}</span>
            </div>
            <div class="detail-item">
              <label>昵称:</label>
              <span>{{ selectedUser.nickname || '未设置' }}</span>
            </div>
            <div class="detail-item">
              <label>邮箱:</label>
              <span>{{ selectedUser.email || '未设置' }}</span>
            </div>
            <div class="detail-item">
              <label>手机号:</label>
              <span>{{ selectedUser.phone || '未设置' }}</span>
            </div>
            <div class="detail-item">
              <label>用户类型:</label>
              <span :class="getUserTypeClass(selectedUser.userType)">
                {{ getUserTypeText(selectedUser.userType) }}
              </span>
            </div>
            <div class="detail-item">
              <label>性别:</label>
              <span>{{ selectedUser.gender === 1 ? '男' : selectedUser.gender === 2 ? '女' : '未设置' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑用户弹窗 -->
    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>编辑用户</h3>
          <span class="close-btn" @click="closeEditModal">×</span>
        </div>
        <div class="modal-body">
          <form class="edit-form">
            <div class="form-group">
              <label>用户名:</label>
              <input v-model="editingUser.username" type="text" disabled class="input-readonly">
            </div>
            <div class="form-group">
              <label>昵称:</label>
              <input v-model="editingUser.nickname" type="text" placeholder="请输入昵称">
            </div>
            <div class="form-group">
              <label>邮箱:</label>
              <input v-model="editingUser.email" type="email" placeholder="请输入邮箱">
            </div>
            <div class="form-group">
              <label>手机号:</label>
              <input v-model="editingUser.phone" type="tel" placeholder="请输入手机号">
            </div>
            <div class="form-group">
              <label>性别:</label>
              <select v-model="editingUser.gender" class="select-gender">
                <option value="0">未设置</option>
                <option value="1">男</option>
                <option value="2">女</option>
              </select>
            </div>
            <div class="form-actions">
              <button type="button" class="btn-cancel" @click="closeEditModal">取消</button>
              <button type="submit" class="btn-save" @click="saveUserEdit">保存</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
import { 
  getUserList, 
  deleteUser as deleteUserAPI, 
  updateUserNickname, 
  updateUserEmail, 
  updateUserPhone, 
  updateUserGender 
} from '@/utils/api';
import { applyAvatarFallback, resolveUserAvatar } from '@/utils/avatar';

const userStore = useUserStore();
const { isEyeCareMode, userInfo } = storeToRefs(userStore);

const users = ref([]);
const filteredUsers = ref([]);
const loading = ref(false);
const searchQuery = ref('');
const userTypeFilter = ref('');
const currentPage = ref(1);
const pageSize = ref(12);
const showUserDetail = ref(false);
const showEditModal = ref(false);
const selectedUser = ref(null);
const editingUser = ref({});

// 计算属性
const totalPages = computed(() => Math.ceil(filteredUsers.value.length / pageSize.value));
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredUsers.value.slice(start, end);
});

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true;
  try {
    const response = await getUserList(1, 100); // 获取前100个用户
    
    if (response.code === 200) {
      users.value = response.data;
      filteredUsers.value = [...users.value];
    } else {
      throw new Error(response.message || '获取用户列表失败');
    }
  } catch (error) {
    console.error('获取用户列表失败:', error);
    uni.showToast({
      title: error.message || '获取用户列表失败',
      icon: 'none'
    });
    // 清空数据，不显示模拟数据
    users.value = [];
    filteredUsers.value = [];
  } finally {
    loading.value = false;
  }
};

// 搜索处理
const handleSearch = () => {
  filterUsers();
};

// 筛选处理
const handleFilter = () => {
  filterUsers();
};

// 筛选用户
const filterUsers = () => {
  let filtered = [...users.value];
  
  // 搜索筛选
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(user => 
      user.username.toLowerCase().includes(query) ||
      (user.nickname && user.nickname.toLowerCase().includes(query)) ||
      (user.email && user.email.toLowerCase().includes(query))
    );
  }
  
  // 用户类型筛选
  if (userTypeFilter.value) {
    filtered = filtered.filter(user => user.userType === parseInt(userTypeFilter.value));
  }
  
  filteredUsers.value = filtered;
  currentPage.value = 1; // 重置到第一页
};

// 分页处理
const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

// 查看用户详情
const viewUserDetail = (user) => {
  selectedUser.value = user;
  showUserDetail.value = true;
};

// 关闭用户详情
const closeUserDetail = () => {
  showUserDetail.value = false;
  selectedUser.value = null;
};

// 编辑用户
const editUser = (user) => {
  // 创建一个副本，确保不会直接修改原始数据
  editingUser.value = { ...user };
  showEditModal.value = true;
};

// 关闭编辑弹窗
const closeEditModal = () => {
  showEditModal.value = false;
  editingUser.value = {};
};

// 保存用户编辑
const saveUserEdit = async () => {
  console.log('保存按钮被点击');
  try {
    uni.showLoading({ title: '保存中...' });
    
    // 获取原始用户数据
    const originalUser = users.value.find(u => u.id === editingUser.value.id);
    if (!originalUser) {
      throw new Error('用户不存在');
    }

    console.log('开始保存用户编辑:', {
      original: originalUser,
      editing: editingUser.value
    });

    // 分别调用不同的接口更新字段
    const updatePromises = [];
    const username = editingUser.value.username;

    // 更新昵称
    if (editingUser.value.nickname !== originalUser.nickname) {
      console.log('更新昵称:', editingUser.value.nickname);
      updatePromises.push(updateUserNickname(username, editingUser.value.nickname));
    }

    // 更新邮箱
    if (editingUser.value.email !== originalUser.email) {
      console.log('更新邮箱:', editingUser.value.email);
      updatePromises.push(updateUserEmail(username, editingUser.value.email));
    }

    // 更新手机号
    if (editingUser.value.phone !== originalUser.phone) {
      console.log('更新手机号:', editingUser.value.phone);
      updatePromises.push(updateUserPhone(username, editingUser.value.phone));
    }

    // 更新性别
    if (editingUser.value.gender !== originalUser.gender) {
      console.log('更新性别:', editingUser.value.gender);
      updatePromises.push(updateUserGender(username, editingUser.value.gender));
    }

    console.log('需要更新的字段数量:', updatePromises.length);

    // 执行所有更新请求
    if (updatePromises.length > 0) {
      const responses = await Promise.all(updatePromises);
      
      console.log('API响应:', responses);
      
      // 检查所有响应是否成功
      const allSuccess = responses.every(response => response.code === 200);

      if (allSuccess) {
        // 重新获取用户列表以刷新数据
        await fetchUsers();
        
        uni.showToast({
          title: '保存成功',
          icon: 'success'
        });
        closeEditModal();
      } else {
        throw new Error('部分字段更新失败');
      }
    } else {
      uni.showToast({
        title: '没有需要更新的字段',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('保存用户信息失败:', error);
    uni.showToast({
      title: error.message || '保存失败',
      icon: 'none'
    });
  } finally {
    uni.hideLoading();
  }
};

// 确认删除用户
const confirmDeleteUser = (user) => {
  // 防止删除当前用户
  if (isCurrentUser(user)) {
    uni.showToast({
      title: '不能删除自己的账户',
      icon: 'none'
    });
    return;
  }

  uni.showModal({
    title: '确认删除',
    content: `确定要删除用户 "${user.nickname || user.username}" 吗？此操作不可恢复。`,
    confirmText: '删除',
    confirmColor: '#ff4757',
    success: (res) => {
      if (res.confirm) {
        deleteUser(user.id);
      }
    }
  });
};

// 删除用户
const deleteUser = async (userId) => {
  try {
    // 先获取用户名
    const user = users.value.find(u => u.id === userId);
    if (!user) {
      throw new Error('用户不存在');
    }

    uni.showLoading({ title: '删除中...' });

    const response = await deleteUserAPI(user.username);

    if (response.code === 200) {
      // 重新获取用户列表以刷新数据
      await fetchUsers();
      
      uni.showToast({
        title: '删除成功',
        icon: 'success'
      });
    } else {
      throw new Error(response.message || '删除失败');
    }
  } catch (error) {
    console.error('删除用户失败:', error);
    uni.showToast({
      title: error.message || '删除失败',
      icon: 'none'
    });
  } finally {
    uni.hideLoading();
  }
};

// 获取用户类型文本
const getUserTypeText = (userType) => {
  switch (userType) {
    case 1: return '普通用户';
    case 2: return '会员用户';
    case 3: return '管理员';
    default: return '未知';
  }
};

// 获取用户类型样式类
const getUserTypeClass = (userType) => {
  switch (userType) {
    case 1: return 'type-normal';
    case 2: return 'type-vip';
    case 3: return 'type-admin';
    default: return 'type-unknown';
  }
};

// 判断是否为当前用户
const isCurrentUser = (user) => {
  return userInfo.value && userInfo.value.username === user.username;
};

// 处理头像加载错误
const handleAvatarError = (event, user) => {
  applyAvatarFallback(event, user);
};

// 返回
const goBack = () => {
  // 返回到管理员中心
  uni.redirectTo({
    url: '/pages/admin/index'
  });
};

onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
.user-management-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  transition: background-color 0.3s ease;
}

.user-management-container.eye-care {
  background: #f0f2e6 !important;
  background-image: none !important;
}

.header {
  position: relative;
  text-align: center;
  margin-bottom: 25px;
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
  font-weight: 600;
}

.search-section {
  display: flex;
  margin-bottom: 60px;
  align-items: center;
  position: relative;
  z-index: 10;
  justify-content: center;
  gap: 60px;
  max-width: 1000px;
  margin: 0 auto;
}

.search-box {
  position: relative;
  width: 35%;
  max-width: 350px;
}

.search-input {
  width: 100%;
  padding: 14px 40px 14px 16px;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  background-color: white;
  height: 50px;
  box-sizing: border-box;
}

.search-input:focus {
  outline: none;
  box-shadow: 0 4px 15px rgba(74, 111, 255, 0.2);
}

.search-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
}

.filter-section {
  width: 35%;
  max-width: 350px;
  position: relative;
  z-index: 10;
}

.filter-select {
  padding: 14px 16px;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  background: white;
  width: 100%;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='%23999' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 16px;
  padding-right: 35px;
  height: 50px;
  box-sizing: border-box;
}

.filter-select:focus {
  outline: none;
  box-shadow: 0 4px 15px rgba(74, 111, 255, 0.2);
}

.user-list {
  margin-bottom: 60px;
  margin-top: 30px;
}

.loading {
  text-align: center;
  padding: 60px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(74, 111, 255, 0.1);
  border-top: 4px solid #4A6FFF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 15px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
}

.empty-icon {
  font-size: 4em;
  margin-bottom: 20px;
  color: #4A6FFF;
}

.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  justify-content: center;
  max-width: 1200px;
  margin: 0 auto;
}

.user-card {
  background: white;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  border: 1px solid rgba(0, 0, 0, 0.03);
  display: flex;
  align-items: center;
  z-index: 1;
  height: 120px; /* 统一高度 */
  min-height: 120px;
}

.user-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(74, 111, 255, 0.15);
  border-color: rgba(74, 111, 255, 0.2);
}

/* 当前用户卡片样式 */
.user-card.current-user {
  background: linear-gradient(135deg, #4A6FFF 0%, #3a5fef 100%);
  color: white;
  border-color: #4A6FFF;
}

.user-card.current-user .user-info h3,
.user-card.current-user .user-username,
.user-card.current-user .user-email,
.user-card.current-user .user-phone {
  color: white;
}

.user-card.current-user .user-username {
  color: rgba(255, 255, 255, 0.8);
}

.user-card.current-user .user-email,
.user-card.current-user .user-phone {
  color: rgba(255, 255, 255, 0.7);
}

.user-avatar {
  position: relative;
  margin-right: 15px;
  flex-shrink: 0;
}

.user-avatar img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
  border: 3px solid white;
}

.user-type-badge {
  position: absolute;
  bottom: -5px;
  right: -5px;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  color: white;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

/* 当前用户标记样式 */
.current-user-badge {
  position: absolute;
  top: -5px;
  left: -5px;
  width: 24px;
  height: 24px;
  background: #FFD700;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  z-index: 2;
}

.current-user-badge span {
  font-size: 12px;
  font-weight: bold;
  color: #333;
}

.type-normal {
  background: #4CAF50;
}

.type-vip {
  background: #FFC107;
  color: #333;
}

.type-admin {
  background: #F44336;
}

.type-unknown {
  background: #9E9E9E;
}

.user-info {
  flex: 1;
  overflow: hidden;
}

.user-info h3 {
  margin: 0 0 5px 0;
  font-size: 1.1em;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-username {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 0.9em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-email,
.user-phone {
  margin: 0 0 5px 0;
  color: #999;
  font-size: 0.85em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-actions {
  position: absolute;
  top: 15px;
  right: 15px;
  display: flex;
  gap: 8px;
}

.action-btn {
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

.action-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.edit-btn {
  background: #4A6FFF;
}

.delete-btn {
  background: #F44336;
}

.delete-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  opacity: 0.6;
}

.delete-btn:disabled:hover {
  transform: none;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
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

.eye-care .page-btn { background-color: #f8f9e8; }
.eye-care .page-btn:hover:not(.disabled) { background-color: #f0f2e6; color: #3A5FEF; border-color: #3A5FEF; }

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(5px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background: white;
  border-radius: 15px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { transform: translateY(30px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f9fa;
}

.modal-header h3 {
  margin: 0;
  color: #333;
  font-weight: 600;
}

.close-btn {
  font-size: 24px;
  color: #999;
  cursor: pointer;
  transition: color 0.3s ease;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
  max-height: 60vh;
  overflow-y: auto;
}

.user-profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.profile-avatar {
  margin-right: 15px;
}

.profile-avatar img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid white;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
}

.profile-name {
  flex: 1;
}

.profile-name h2 {
  margin: 0 0 5px 0;
  font-size: 1.5em;
  color: #333;
}

.profile-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: bold;
  color: white;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item label {
  font-weight: bold;
  color: #333;
  min-width: 80px;
}

.detail-item span {
  color: #666;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: bold;
  color: #333;
}

.form-group input,
.form-group select {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 10px;
  font-size: 16px;
  background-color: white;
  transition: all 0.3s ease;
}

.form-group .input-readonly,
.form-group input:disabled {
  background-color: #f1f3f5;
  color: #8a8a8a;
  cursor: not-allowed;
  border-color: #e0e0e0;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #4A6FFF;
  box-shadow: 0 0 0 3px rgba(74, 111, 255, 0.1);
}

.form-actions { display: flex; gap: 16px; justify-content: center; margin-top: 24px; }

.btn-cancel, .btn-save { padding: 12px 28px; border: none; border-radius: 12px; cursor: pointer; font-size: 16px; font-weight: 600; transition: all 0.25s ease; min-width: 120px; }

.btn-cancel { background: linear-gradient(90deg,#eceff4 0%, #e6eaef 100%); color: #495057; box-shadow: 0 2px 6px rgba(0,0,0,0.08); }
.btn-cancel:hover { transform: translateY(-1px); box-shadow: 0 6px 12px rgba(0,0,0,0.12); }
.btn-cancel:active { transform: translateY(1px); }

.btn-save { background: linear-gradient(90deg,#4A6FFF 0%, #6B8CFF 100%); color: #fff; box-shadow: 0 4px 10px rgba(74,111,255,0.28); }
.btn-save:hover { transform: translateY(-1px); box-shadow: 0 8px 16px rgba(74,111,255,0.35); }
.btn-save:active { transform: translateY(1px) scale(0.98); }

/* 护眼模式适配 */
.eye-care .user-card,
.eye-care .modal-content,
.eye-care .page-info,
.eye-care .page-btn {
  background-color: #f8f9e8;
}

.eye-care .modal-header {
  background-color: #f0f2e6;
}

.eye-care .search-input,
.eye-care .filter-select,
.eye-care .form-group input,
.eye-care .form-group select {
  background-color: #f0f2e6;
  border-color: #d1d5db;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .user-management-container {
    padding: 15px;
  }

  .search-section {
    flex-direction: column;
    gap: 15px;
  }

  .search-box {
    width: 100%;
    max-width: 100%;
  }
  
  .filter-section {
    width: 100%;
    max-width: 100%;
  }

  .users-grid {
    grid-template-columns: 1fr;
  }

  .user-card {
    padding: 15px;
  }

  .modal-content {
    width: 95%;
    margin: 10px;
  }
  
  .profile-avatar img {
    width: 60px;
    height: 60px;
  }
  
  .profile-name h2 {
    font-size: 1.2em;
  }
}
</style>

import { defineStore } from 'pinia';
import request from '@/utils/api/request';
import { getStorage, setStorage, removeStorage } from '@/utils/storage';
import { BASE_URL } from '@/utils/api/api-config';

const USER_API = {
  EYEMODE: `${BASE_URL}/api/user/eyemode`,
};

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    isLoggedIn: false,
    isEyeCareMode: false,
    themeSettings: null,
    userId: null,
    loading: false,
    error: null,
    // 添加设备标识，用于区分不同设备
    deviceId: null,
  }),

  actions: {
    // 生成设备标识
    generateDeviceId() {
      if (!this.deviceId) {
        // 生成唯一的设备标识
        this.deviceId = 'device_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
        console.log('Generated device ID:', this.deviceId);
        
      }
      return this.deviceId;
    },

    // 初始化store状态
    initializeStore() {
      console.log('Initializing user store...');
      this.generateDeviceId();
      this.loadUserFromStorage();
    },

    setUserInfo(info) {
      console.log('setUserInfo received info:', info);
      if (!info) {
        this.clearUserInfo();
        return;
      }

      if (!this.userInfo) {
          this.userInfo = {};
      }
      
      this.userInfo.userId = info.userId;
      this.userInfo.username = info.username;
      this.userInfo.nickname = info.nickname;
      this.userInfo.avatar = info.avatar;
      this.userInfo.avatarUrl = info.avatarUrl || info.avatar;
      this.userInfo.gender = info.gender;
      this.userInfo.userType = info.userType;
      // 处理护眼模式状态
      const eyeCareMode = info.eyeCareMode;
      const isEyeCareEnabled = eyeCareMode === 1 || eyeCareMode === true;
      
      this.userInfo.eyeCareMode = isEyeCareEnabled ? 1 : 0;
      this.userInfo.themeSettings = info.themeSettings || {
        eyeCareMode: isEyeCareEnabled,
        backgroundColor: '#f0f2e6'
      };

      this.userId = info.userId;
      this.isLoggedIn = true;
      
      // 从用户信息中获取护眼模式设置
      this.isEyeCareMode = isEyeCareEnabled;
      this.themeSettings = info.themeSettings || {
        eyeCareMode: isEyeCareEnabled,
        backgroundColor: '#f0f2e6'
      };
      
      console.log('用户信息已设置，护眼模式状态:', this.isEyeCareMode);
    },

    clearUserInfo() {
      this.userInfo = null;
      this.isLoggedIn = false;
      this.isEyeCareMode = false;
      this.themeSettings = null;
      this.userId = null;
      // 不清除设备标识，保持设备唯一性
    },

    async logout() {
      removeStorage('token');
      removeStorage('userInfo');
      this.clearUserInfo();
      console.log('前端认证信息已清除');
    },

    async setEyeCareMode(value) {
      if (!this.isLoggedIn) throw new Error('请先登录');
      this.loading = true;
      this.error = null;
      try {
        const response = await request.put(USER_API.EYEMODE, {
          eyeCareMode: value ? 1 : 0,
        });
        if (response.code === 200) {
          // 根据后端返回的数据更新护眼模式状态
          const eyeCareMode = response.data.eyeCareMode;
          this.isEyeCareMode = eyeCareMode === 1 || eyeCareMode === true;
          
          // 更新主题设置
          this.themeSettings = {
            eyeCareMode: this.isEyeCareMode,
            backgroundColor: '#f0f2e6'
          };
          
          // 更新本地存储中的用户信息
          if (this.userInfo) {
            this.userInfo.eyeCareMode = this.isEyeCareMode ? 1 : 0;
            setStorage('userInfo', JSON.stringify(this.userInfo));
          }
          
          console.log('护眼模式已更新:', this.isEyeCareMode);
        } else {
          throw new Error(response.message || '更新失败');
        }
      } catch (error) {
        this.error = error.message || '更新护眼模式失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    // 从本地存储恢复护眼模式状态
    initializeSettings() {
      if (!this.isLoggedIn || !this.userInfo) {
        this.clearUserInfo();
        return;
      }
      
      // 从本地存储恢复护眼模式状态
      if (this.userInfo && this.userInfo.eyeCareMode !== undefined) {
        this.isEyeCareMode = this.userInfo.eyeCareMode === 1 || this.userInfo.eyeCareMode === true;
        console.log('从本地存储恢复护眼模式状态:', this.isEyeCareMode);
      }
    },

    // 从本地存储加载用户数据
    loadUserFromStorage() {
      const userInfo = getStorage('userInfo');
      const token = getStorage('token');

      if (userInfo && token) {
        // 检查 token 是否过期 (可选)
        try {
            const parsedUserInfo = JSON.parse(userInfo);
            // 更新 Store 状态
            this.setUserInfo(parsedUserInfo); // 调用 setUserInfo 更新 userInfo 对象和其他属性
            // 确保 userId 也被设置到 Store 的单独状态中
            this.userId = parsedUserInfo.userId; // 显式设置 userId 状态
            this.isLoggedIn = true; // 确保登录状态正确
            
            console.log('从本地存储加载用户数据，护眼模式状态:', this.isEyeCareMode);
            console.log('当前设备ID:', this.deviceId);
        } catch (e) {
            console.error('Failed to parse userInfo from storage in loadUserFromStorage:', e);
            this.clearUserInfo(); // 解析失败则清空用户数据
        }
      } else {
        this.clearUserInfo();
      }
    },

    // 检查用户登录状态
    checkLoginStatus() {
      const token = getStorage('token');
      const userInfo = getStorage('userInfo');
      
      if (!token || !userInfo) {
        this.clearUserInfo();
        return false;
      }
      
      // 如果store中没有用户信息，从存储中加载
      if (!this.userInfo || !this.isLoggedIn) {
        this.loadUserFromStorage();
      }
      
      return this.isLoggedIn;
    }
  },

  getters: {
    getUserInfo: (state) => state.userInfo,
    getLoginStatus: (state) => state.isLoggedIn,
    getEyeCareMode: (state) => state.isEyeCareMode,
    getThemeSettings: (state) => state.themeSettings
  }
}); 

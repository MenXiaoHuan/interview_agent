<template>
  <div class="admin-container" :class="{ 'eye-care': isEyeCareMode }">
    <!-- 气泡背景效果 -->
    <div class="bubbles">
      <div v-for="n in 30" :key="n" class="bubble"></div>
    </div>
    
    <div class="admin-content">
      <div class="header">
        <span class="back-icon" @click="goBack" aria-label="返回" role="button">‹</span>
        <h1 class="page-title">管理员中心</h1>
      </div>

      <div class="admin-menu">
        <div class="menu-section">
          <h3 class="section-title">用户管理</h3>
          <div class="menu-grid">
            <div class="menu-item" @click="goToUserList">
              <div class="menu-icon">
                <svg class="menu-svg-inner" width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="9" cy="8" r="3" stroke="currentColor" stroke-width="2"/>
                  <circle cx="16" cy="10" r="2.5" stroke="currentColor" stroke-width="2"/>
                  <path d="M3 20c2-3.5 5-5 8-5s6 1.5 8 5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M12 14c2.8 0 5.2 1.4 7 4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </div>
              <div class="menu-content">
                <h4>用户列表</h4>
                <p>查看和管理所有用户信息</p>
              </div>
              <span class="arrow">></span>
            </div>
            <div class="menu-item" @click="goToUserPermission">
              <div class="menu-icon">
                <svg class="menu-svg-inner" width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="4" y="10" width="16" height="9" rx="2" stroke="currentColor" stroke-width="2"/>
                  <path d="M8 10V8a4 4 0 0 1 8 0v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M12 13v3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </div>
              <div class="menu-content">
                <h4>权限管理</h4>
                <p>修改用户权限和角色</p>
              </div>
              <span class="arrow">></span>
            </div>
          </div>
        </div>

        <div class="menu-section">
          <h3 class="section-title">岗位管理</h3>
          <div class="menu-grid">
            <div class="menu-item" @click="goToJobCategory">
              <div class="menu-icon">
                <svg class="menu-svg-inner" width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="4" y="8" width="16" height="10" rx="2" stroke="currentColor" stroke-width="2"/>
                  <path d="M9 8V6a3 3 0 0 1 6 0v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M4 12h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </div>
              <div class="menu-content">
                <h4>岗位分类管理</h4>
                <p>管理一级和二级岗位分类</p>
              </div>
              <span class="arrow">></span>
            </div>
            <div class="menu-item" @click="goToJobPosition">
              <div class="menu-icon">
                <svg class="menu-svg-inner" width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="5" y="12" width="3" height="6" rx="1" stroke="currentColor" stroke-width="2"/>
                  <rect x="10.5" y="9" width="3" height="9" rx="1" stroke="currentColor" stroke-width="2"/>
                  <rect x="16" y="6" width="3" height="12" rx="1" stroke="currentColor" stroke-width="2"/>
                  <path d="M4 18h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </div>
              <div class="menu-content">
                <h4>岗位职位管理</h4>
                <p>管理各分类下的具体岗位</p>
              </div>
              <span class="arrow">></span>
            </div>
          </div>
        </div>

        <div class="menu-section">
          <h3 class="section-title">题库管理</h3>
          <div class="menu-grid">
            <div class="menu-item" @click="goToQuestionManagement">
              <div class="menu-icon">
                <svg class="menu-svg-inner" width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="6" y="4" width="12" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
                  <path d="M9 4h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M8 9h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M8 13h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </div>
              <div class="menu-content">
                <h4>试题评测管理</h4>
                <p>管理试题评测试题和记录</p>
              </div>
              <span class="arrow">></span>
            </div>
            <div class="menu-item" @click="goToScenarioManagement">
              <div class="menu-icon">
                <svg class="menu-svg-inner" width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M6 6h7v6c0 2.5-2 4.5-4.5 4.5S4 14.5 4 12V6h2z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                  <path d="M18 6h2v6c0 2.5-2 4.5-4.5 4.5-1 0-1.9-.3-2.7-.9" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                  <path d="M7 9h2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M14.5 9h2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </div>
              <div class="menu-content">
                <h4>场景评测管理</h4>
                <p>管理场景评测题库和记录</p>
              </div>
              <span class="arrow">></span>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';

const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);



// 导航方法
const goToUserList = () => {
  uni.navigateTo({
    url: '/pages/admin/user-list/index'
  });
};

const goToUserPermission = () => {
  uni.navigateTo({
    url: '/pages/admin/user-permission/index'
  });
};

const goToQuestionManagement = () => {
  uni.navigateTo({
    url: '/pages/admin/question-management/index'
  });
};

const goToScenarioManagement = () => {
  uni.navigateTo({
    url: '/pages/admin/scenario-management/index'
  });
};

const goToJobCategory = () => {
  uni.navigateTo({
    url: '/pages/admin/job-category/index'
  });
};

const goToJobPosition = () => {
  uni.navigateTo({
    url: '/pages/admin/job-position/index'
  });
};

const goBack = () => {
  // 直接返回到个人中心页面
  uni.redirectTo({
    url: '/pages/personal-center/index'
  });
};


</script>

<style scoped>
.admin-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  justify-content: center;
  align-items: center;
  padding: 0;
}

.admin-container.eye-care {
  background: #f0f2e6 !important;
  background-image: none !important;
}

/* 气泡背景 */
.bubbles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.bubble {
  position: absolute;
  display: block;
  width: 20px;
  height: 20px;
  background-color: rgba(74, 111, 255, 0.2);
  border-radius: 50%;
  bottom: -150px;
  animation: float 25s linear infinite, fadeInOut 7s ease-in-out infinite alternate;
  z-index: 0;
}

.bubbles .bubble:nth-child(1) { left: 10%; width: 40px; height: 40px; animation-delay: 0s, 0.5s; }
.bubbles .bubble:nth-child(2) { left: 20%; width: 25px; height: 25px; animation-delay: 2s, 1s; }
.bubbles .bubble:nth-child(3) { left: 35%; width: 30px; height: 30px; animation-delay: 4s, 1.5s; }
.bubbles .bubble:nth-child(4) { left: 50%; width: 50px; height: 50px; animation-delay: 0s, 2s; }
.bubbles .bubble:nth-child(5) { left: 65%; width: 35px; height: 35px; animation-delay: 3s, 2.5s; }
.bubbles .bubble:nth-child(6) { left: 75%; width: 60px; height: 60px; animation-delay: 0s, 3s; }
.bubbles .bubble:nth-child(7) { left: 88%; width: 30px; height: 30px; animation-delay: 1s, 3.5s; }
.bubbles .bubble:nth-child(8) { left: 25%; width: 45px; height: 45px; animation-delay: 5s, 4s; }
.bubbles .bubble:nth-child(9) { left: 40%; width: 55px; height: 55px; animation-delay: 2s, 4.5s; }
.bubbles .bubble:nth-child(10) { left: 60%; width: 30px; height: 30px; animation-delay: 6s, 5s; }

@keyframes float {
  0% { bottom: -100px; transform: translateX(0); }
  100% { bottom: 100%; transform: translateX(200px); }
}

@keyframes fadeInOut {
  0% { opacity: 0.5; }
  50% { opacity: 1; }
  100% { opacity: 0.5; }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.admin-content {
  padding: 15px 20px;
  max-width: 800px;
  width: 95%;
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: slideInUp 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  margin: auto 0;
}

.header {
  position: relative;
  text-align: center;
  margin-bottom: 15px;
  padding-left: 60px;
  padding-right: 60px;
}

.back-icon {
  position: absolute;
  left: -150px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 2em;
  color: #666;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 0 15px;
  z-index: 10;
}

.back-icon:hover {
  color: #333;
  transform: translateY(-50%) scale(1.1);
}

.page-title {
  margin: 0;
  font-size: 1.8em;
  color: #333;
  text-align: center;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
  font-weight: 600;
  letter-spacing: 1px;
}

.admin-menu {
  display: flex;
  flex-direction: column;
  gap: 15px;
  width: 100%;
}

.menu-section {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 10px;
}

.menu-section:hover {
  background: rgba(255, 255, 255, 1);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.section-title {
  margin: 0 0 15px 0;
  font-size: 1.4em;
  color: #2d3748;
  padding-bottom: 10px;
  border-bottom: 2px solid #C99700;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 18px;
  border-radius: 12px;
  background: rgba(248, 249, 250, 0.8);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  position: relative;
  overflow: hidden;
}

.menu-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.1), transparent);
  transition: left 0.6s ease;
}

.menu-item:hover::before {
  left: 100%;
}

.menu-item:hover {
  background: rgba(240, 235, 220, 0.9);
  border-color: #C99700;
  transform: translateX(8px) scale(1.02);
  box-shadow: 0 8px 24px rgba(201, 151, 0, 0.18);
}

.menu-icon {
  font-size: 1.8em;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #8A6BF7, #C99700);
  border-radius: 50%;
  color: white;
  box-shadow: 0 4px 12px rgba(201, 151, 0, 0.28);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.menu-item:hover .menu-icon {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 6px 16px rgba(201, 151, 0, 0.36);
}

.menu-content {
  flex: 1;
}

.menu-content h4 {
  margin: 0 0 4px 0;
  font-size: 1em;
  color: #2d3748;
  font-weight: 600;
  position: relative;
  z-index: 1;
}

.menu-content p {
  margin: 0;
  color: #718096;
  font-size: 0.85em;
  line-height: 1.3;
  position: relative;
  z-index: 1;
}

.arrow {
  font-size: 1.3em;
  color: #a38a3a;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.menu-item:hover .arrow {
  transform: translateX(6px);
  color: #C99700;
  text-shadow: 0 2px 4px rgba(201, 151, 0, 0.3);
}

.menu-svg-inner {
  display: block;
  width: 22px;
  height: 22px;
}

/* 护眼模式适配 */
.eye-care .stat-card,
.eye-care .menu-section {
  background-color: rgba(248, 249, 232, 0.95);
}

.eye-care .menu-item {
  background-color: rgba(240, 242, 230, 0.8);
}

.eye-care .menu-item:hover {
  background-color: rgba(230, 232, 217, 0.9);
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .admin-content {
    padding: 10px;
    width: 95%;
  }

  .page-title {
    font-size: 1.3em;
  }

  .menu-grid {
    grid-template-columns: 1fr;
  }

  .menu-item {
    padding: 10px;
  }

  .menu-icon {
    font-size: 1.2em;
    width: 32px;
    height: 32px;
  }
  
  .menu-content h4 {
    font-size: 0.9em;
  }
  
  .menu-content p {
    font-size: 0.8em;
  }
}

@media screen and (min-width: 769px) and (max-height: 800px) {
  .admin-container {
    padding: 5px 0;
  }
  
  .admin-content {
    padding: 10px;
  }
  
  .header {
    margin-bottom: 10px;
  }
  
  .admin-menu {
    gap: 10px;
  }
  
  .menu-section {
    padding: 12px;
  }
  
  .section-title {
    margin: 0 0 8px 0;
    padding-bottom: 6px;
  }
}
</style> 

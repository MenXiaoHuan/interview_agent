<template>
  <!-- 1. 把 div 改成 uni-app 必须的 view -->
  <view id="app" class="app-container" :class="{ 'eye-care': userStore.isEyeCareMode }" :style="appStyle">
    <!-- 2. uni-app 不用 router-view，直接页面会自动渲染 -->
  </view>
</template>

<script setup>
import { useUserStore } from '@/stores/user';
import { computed, onMounted } from 'vue';
import { preloadStaticImages } from '@/utils/preload-static-assets';

const userStore = useUserStore();

// 初始化
onMounted(() => {
  userStore.initializeStore();
  // Warm cache for commonly used static images (avatar placeholder/backgrounds).
  preloadStaticImages();
});

// 护眼模式样式
const appStyle = computed(() => {
  if (userStore.isEyeCareMode) {
    return {
      background: '#f0f2e6'
    };
  }
  return {
    background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)'
  };
});
</script>

<script>
export default {
  onLaunch() {
    console.log('App Launch');
  },
  onShow() {
    console.log('App Show');
  },
  onHide() {
    console.log('App Hide');
  }
};
</script>

<style>
/* 全局样式：修复页面展示不全 + 铺满全屏 + 可滚动 */
#app {
  width: 100%;
  min-height: 100vh;
  height: auto;
  box-sizing: border-box;
  overflow-y: auto; /* 允许滚动 */
  transition: background-color 0.3s ease;
}

/* 页面容器样式 */
.page-container {
  width: 100%;
  min-height: 100vh;
  height: auto;
  overflow: visible;
}

/* 全局禁止横向滚动 */
page, body, html {
  width: 100%;
  height: auto;
  min-height: 100vh;
  overflow-x: hidden;
  overflow-y: auto;
  margin: 0;
  padding: 0;
}

/* 隐藏导航栏（保持你原来的逻辑） */
uni-nav-bar {
  display: none !important;
}
</style>

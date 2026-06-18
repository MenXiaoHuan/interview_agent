<template>
  <div class="centered-container" :class="{ 'eye-care': isEyeCareMode }">
    <!-- 气泡背景效果 -->
    <div class="bubbles">
      <div v-for="n in 20" :key="n" class="bubble"></div>
    </div>
    
    <div class="history-container">
      <div class="header">
        <span class="back-icon" @click="goBack" aria-label="返回" role="button">‹</span>
        <h2 class="title">历史记录</h2>
      </div>

      <!-- 分类切换标签 -->
      <div class="tab-container">
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'comprehensive' }"
          @click="activeTab = 'comprehensive'"
        >
          综合记录
        </div>
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'specialized' }"
          @click="activeTab = 'specialized'"
        >
          专项记录
        </div>
      </div>

      <!-- 综合记录内容 -->
      <div v-if="activeTab === 'comprehensive'" class="records-list">
        <div v-if="comprehensiveRecords.length > 0">
          <div v-for="record in comprehensiveRecords" :key="record.id" class="record-item">
            <div class="record-link" @click="openComprehensiveDetail(record)">
              <div class="record-info">
                <span class="record-date">{{ record.date }}</span>
                <span class="record-job">{{ record.jobTitle }}</span>
                <span class="record-score">综合得分: {{ record.score }}</span>
              </div>
              <div class="record-details">
                <div class="score-breakdown">
                  <div class="score-item">
                    <span class="score-label">简历评分:</span>
                    <span class="score-value">{{ record.resumeScore }}</span>
                  </div>
                  <div class="score-item">
                    <span class="score-label">试题得分:</span>
                    <span class="score-value">{{ record.questionScore }}</span>
                  </div>
                  <div class="score-item">
                    <span class="score-label">场景得分:</span>
                    <span class="score-value">{{ record.audioScore }}</span>
                  </div>
                </div>
                <div class="record-summary">{{ record.summary }}</div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <p v-if="!isLoading && !error">暂无综合面试记录</p>
          <p v-else-if="error">加载失败，请稍后重试</p>
        </div>
      </div>

      <!-- 专项记录内容 -->
      <div v-else class="specialized-records">
        <div class="specialized-grid">
          <div class="specialized-item" @click="showSpecializedHistory('resume')">
            <div class="specialized-icon">📄</div>
            <div class="specialized-title">简历评测记录</div>
            <div class="specialized-stats">
              <div class="stat">最新得分: {{ getLatestScore('resume') }}</div>
              <div class="stat">评测次数: {{ getTestCount('resume') }}</div>
            </div>
          </div>
          <div class="specialized-item" @click="showSpecializedHistory('questions')">
            <div class="specialized-icon">📝</div>
            <div class="specialized-title">试题作答记录</div>
            <div class="specialized-stats">
              <div class="stat">最新得分: {{ getLatestScore('questions') }}</div>
              <div class="stat">评测次数: {{ getTestCount('questions') }}</div>
            </div>
          </div>
          <div class="specialized-item" @click="showSpecializedHistory('audio')">
            <div class="specialized-icon">🎤</div>
            <div class="specialized-title">场景评测记录</div>
            <div class="specialized-stats">
              <div class="stat">最新得分: {{ getLatestScore('audio') }}</div>
              <div class="stat">评测次数: {{ getTestCount('audio') }}</div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="isLoading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在加载历史记录...</p>
      </div>
      <div v-if="error && !isLoading" class="error-message">
        <p>错误: {{ error }}</p>
        <button class="retry-button" @click="fetchHistoryData">重试</button>
      </div>
    </div>

    <!-- 专项历史记录弹窗 -->
    <div v-if="showSpecializedModal" class="specialized-modal" @click.self="closeSpecializedModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ getSpecializedTitle(selectedSpecialized) }}</h3>
          <button class="close-button" @click="closeSpecializedModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="chart-container">
            <div class="chart" ref="chartRef"></div>
          </div>
          <div class="history-list">
            <div v-for="item in getSpecializedRecords(selectedSpecialized)" 
                 :key="item.id" 
                 class="history-item"
                 @click="showSpecializedDetail(item)">
              <div class="history-main">
                <span class="history-date">{{ formatDate(item.date) }}</span>
                <span class="history-score">总分: {{ item.score ?? '-' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 专项记录详情弹窗 -->
    <div v-if="showSpecializedDetailModal" class="specialized-detail-modal" @click.self="closeSpecializedDetailModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ getSpecializedTitle(selectedSpecialized) }}详情</h3>
          <button class="close-button" @click="closeSpecializedDetailModal">&times;</button>
        </div>
        <div class="modal-body">
          <div v-if="selectedSpecializedRecord">
            <!-- 简历专项详情 -->
            <template v-if="selectedSpecialized === 'resume'">
              <div class="detail-section">
                <h4>简历内容</h4>
                <div class="content-block">{{ selectedSpecializedRecord.resumeContent || '暂无内容' }}</div>
              </div>
              
              <div class="detail-section">
                <h4>评分详情</h4>
                <div class="score-grid">
                  <div class="score-item">
                    <span class="score-label">总分:</span>
                    <span class="score-value">{{ selectedSpecializedRecord.score }}</span>
                  </div>
                  <div class="score-item">
                    <span class="score-label">专业技能:</span>
                    <span class="score-value">{{ selectedSpecializedRecord.professionalSkills }}</span>
                  </div>
                  <div class="score-item">
                    <span class="score-label">项目执行:</span>
                    <span class="score-value">{{ selectedSpecializedRecord.projectExecution }}</span>
                  </div>
                  <div class="score-item">
                    <span class="score-label">创新能力:</span>
                    <span class="score-value">{{ selectedSpecializedRecord.innovation }}</span>
                  </div>
                  <div class="score-item">
                    <span class="score-label">沟通能力:</span>
                    <span class="score-value">{{ selectedSpecializedRecord.communication }}</span>
                  </div>
                  <div class="score-item">
                    <span class="score-label">适应能力:</span>
                    <span class="score-value">{{ selectedSpecializedRecord.adaptability }}</span>
                  </div>
                </div>
              </div>
              
              <div class="detail-section">
                <h4>优势分析</h4>
                <div class="content-block">{{ selectedSpecializedRecord.advantages || '暂无分析' }}</div>
              </div>
              
              <div class="detail-section">
                <h4>不足分析</h4>
                <div class="content-block">{{ selectedSpecializedRecord.disadvantages || '暂无分析' }}</div>
              </div>
              
              <div class="detail-section">
                <h4>改进建议</h4>
                <div class="content-block">{{ selectedSpecializedRecord.improvementSuggestions || '暂无建议' }}</div>
              </div>
            </template>
            
            <!-- 其他专项详情 -->
            <template v-else>
              <div class="detail-section">
                <h4>评分详情</h4>
                <div class="score-grid">
                  <div class="score-item">
                    <span class="score-label">总分:</span>
                    <span class="score-value">{{ selectedSpecializedRecord.score }}</span>
                  </div>
                  <div v-for="(value, key) in selectedSpecializedRecord.details" :key="key" class="score-item">
                    <span class="score-label">{{ detailKeyToLabel(key) }}:</span>
                    <span class="score-value">{{ value }}</span>
                  </div>
                </div>
              </div>
              
              <div v-if="selectedSpecializedRecord.suggestions && selectedSpecializedRecord.suggestions.length > 0" class="detail-section">
                <h4>建议</h4>
                <div class="suggestions-list">
                  <div v-for="(suggestion, index) in selectedSpecializedRecord.suggestions" :key="index" class="suggestion-item">
                    {{ suggestion }}
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- 综合记录详情弹窗 -->
    <div v-if="showComprehensiveDetail" class="comprehensive-modal" @click.self="closeComprehensiveDetail">
      <div class="modal-content">
        <div class="modal-header">
          <h3>综合评估详情</h3>
          <button class="close-button" @click="closeComprehensiveDetail">&times;</button>
        </div>
        <div class="modal-body">
          <div v-if="selectedComprehensiveRecord">
            <div class="detail-row">
              <strong>岗位：</strong>{{ selectedComprehensiveRecord.jobTitle }}
            </div>
            <div class="detail-row">
              <strong>评估时间：</strong>{{ selectedComprehensiveRecord.date }}
            </div>
            <div class="detail-row">
              <strong>优点：</strong>
              <div class="detail-block" v-html="selectedComprehensiveRecord.strengthAnalysis?.replace(/\n/g, '<br>')"></div>
            </div>
            <div class="detail-row">
              <strong>改进建议：</strong>
              <div class="detail-block" v-html="selectedComprehensiveRecord.improvementAnalysis?.replace(/\n/g, '<br>')"></div>
            </div>
            <div class="detail-row">
              <strong>学习路线：</strong>
              <div class="detail-block">{{ selectedComprehensiveRecord.learningRoute }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
console.log('Executing history page script setup.'); // 添加日志

import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
import * as echarts from 'echarts';
import { API } from '@/utils/api';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
import { useRouter, useRoute } from 'vue-router';
import { getUserSession, subscribeUserSession, getSessionToken } from '@/utils/user-session';
import request from '@/utils/request';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);
const activeTab = ref('comprehensive');
const comprehensiveRecords = ref([]);
const specializedRecords = ref({
  resume: [],
  questions: [],
  audio: []
});
const isLoading = ref(false);
const error = ref(null);
const showSpecializedModal = ref(false);
const selectedSpecialized = ref('');
const chartRef = ref(null);
let chart = null;
const hasAppliedRouteAutoOpen = ref(false);

// 综合记录详情弹窗相关状态
const showComprehensiveDetail = ref(false);
const selectedComprehensiveRecord = ref(null);

const openComprehensiveDetail = (record) => {
  selectedComprehensiveRecord.value = record;
  showComprehensiveDetail.value = true;
};

const closeComprehensiveDetail = () => {
  showComprehensiveDetail.value = false;
  selectedComprehensiveRecord.value = null;
};

// 专项记录详情弹窗相关状态
const showSpecializedDetailModal = ref(false);
const selectedSpecializedRecord = ref(null);

const showSpecializedDetail = (record) => {
  selectedSpecializedRecord.value = record;
  showSpecializedDetailModal.value = true;
};

const closeSpecializedDetailModal = () => {
  showSpecializedDetailModal.value = false;
  selectedSpecializedRecord.value = null;
};


// 获取简历评估历史
const fetchResumeHistory = async (userId, token) => {
  try {
    const response = await request({
      url: API.RESUME.HISTORY(userId),
      method: 'GET'
    });

    console.log('简历评估历史响应:', response); // 添加日志

    if (response.code === 200) {
      specializedRecords.value.resume = response.data.map(record => ({
        id: record.id,
        date: record.createdAt,
        score: record.score,
        resumeContent: record.resumeContent,
        advantages: record.advantages,
        disadvantages: record.disadvantages,
        improvementSuggestions: record.improvementSuggestions,
        professionalSkills: record.professionalSkills,
        projectExecution: record.projectExecution,
        innovation: record.innovation,
        communication: record.communication,
        adaptability: record.adaptability
      }));
    }
  } catch (err) {
    console.error('Failed to fetch resume history:', err); // 添加日志
    throw err; // 抛出错误以便在 fetchHistoryData中捕获
  }
};

// 获取面试题目历史
const fetchQuestionHistory = async (userId, token) => {
  try {
    const response = await request({
      url: API.INTERVIEW.QUESTION_HISTORY(userId),
      method: 'GET'
    });
    
    console.log('试题作答历史响应:', response);

    if (response.code === 200) {
      specializedRecords.value.questions = response.data.map(record => ({
        id: record.id,
        date: record.createdAt,
        score: record.totalScore,
        details: {
          correctCount: record.correctCount,
          correctRate: record.correctRate,
        }
      }));
    }
  } catch (err) {
    console.error('Failed to fetch question history:', err); // 添加日志
    throw err;
  }
};

// 获取场景评估历史
const fetchAudioHistory = async (userId, token) => {
  try {
    const response = await request({
      url: API.SCENARIO.HISTORY(userId),
      method: 'GET'
    });

    console.log('场景评估历史响应:', response); // 注意：这里打印完整响应，便于调试

    if (response.code === 200) {
      if (Array.isArray(response.data)) {
        specializedRecords.value.audio = response.data.map(record => ({
          id: record.id,
          date: addHours(record.createTime, 0),
          score: record.totalScore,
          details: {
            professionalSkills: record.score1,
            projectExecution: record.score2,
            innovation: record.score3,
            communication: record.score4,
            adaptability: record.score5
          },
          suggestions: [
            record.suggestion1,
            record.suggestion2,
            record.suggestion3,
            record.suggestion4,
            record.suggestion5
          ].filter(Boolean)
        }));
      } else {
        console.error('Failed to fetch audio history: Invalid server response format', response);
      }
    } else {
      console.error('Failed to fetch audio history: HTTP request failed', response.code);
    }
  } catch (err) {
    console.error('Failed to fetch audio history:', err);
    throw err;
  }
};

const fetchComprehensiveRecords = async (userId, token) => {
  try {
    const response = await request({
      url: API.COMPREHENSIVE.HISTORY(userId),
      method: 'GET'
    });

    console.log('综合评估报告响应:', response);

    if (response.code === 200) {
      // 按createTime倒序排列
      const sortedData = response.data.slice().sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
      comprehensiveRecords.value = sortedData.map(record => ({
        id: record.id,
        date: formatDate(record.createTime),
        jobTitle: record.jobTitle,
        score: record.overallScore,
        resumeScore: record.resumeScore,
        questionScore: record.questionScore,
        audioScore: record.scenarioScore,
        summary: record.learningRoute ? record.learningRoute.slice(0, 30) + '...' : '',
        strengthAnalysis: record.strengthAnalysis,
        improvementAnalysis: record.improvementAnalysis,
        learningRoute: record.learningRoute
      }));
    } else {
      console.error('获取综合评估报告失败:', response.data.message);
      comprehensiveRecords.value = [];
    }
  } catch (err) {
    console.error('Failed to fetch comprehensive records:', err);
    comprehensiveRecords.value = [];
  }
};

const fetchHistoryData = async () => {
  const tokenRaw = getSessionToken();
  const userInfoStorage = getUserSession();

  const token = tokenRaw;
  const userId = userInfoStorage?.userId || (userStore.userInfo && userStore.userInfo.userId);

  // 检查 userId 和 token 是否存在
  if (!userId || !token) {
    console.warn("用户信息不完整（缺少 userId 或 token），无法加载历史记录", {
      currentUserId: userId,
      currentToken: token,
    });
    isLoading.value = false;
    error.value = '请先登录以查看历史记录';
    return;
  }
  
  console.log('Fetching history with userId:', userId, 'and token:', token ? '[Available]' : '[Missing]');

  isLoading.value = true;
  error.value = null;

  try {
    // 使用 Promise.allSettled 确保即使某个请求失败，其他请求仍能继续
    const results = await Promise.allSettled([
      fetchComprehensiveRecords(userId, token),
      fetchResumeHistory(userId, token),
      fetchQuestionHistory(userId, token),
      fetchAudioHistory(userId, token)
    ]);
    
    // 检查是否有请求失败
    const failedRequests = results.filter(result => result.status === 'rejected');
    if (failedRequests.length > 0) {
      console.warn('部分历史记录加载失败:', failedRequests);
      // 不设置全局错误，因为可能只是部分数据加载失败
    }
  } catch (err) {
    console.error("加载历史记录失败:", err);
    error.value = err.message || '加载历史记录失败';
  } finally {
    isLoading.value = false;
    await nextTick();
    applyRoutePreset();
  }
};

const normalizeHistoryTab = (value) => {
  const safeValue = String(value || '').trim().toLowerCase();
  return ['comprehensive', 'specialized'].includes(safeValue) ? safeValue : '';
};

const normalizeSpecializedType = (value) => {
  const safeValue = String(value || '').trim().toLowerCase();
  return ['resume', 'questions', 'audio'].includes(safeValue) ? safeValue : '';
};

const applyRoutePreset = () => {
  const presetType = normalizeSpecializedType(route.query.type);
  const presetTab = normalizeHistoryTab(route.query.tab);
  const shouldAutoOpen = String(route.query.autoOpen || '').trim() === '1';

  if (presetType) {
    activeTab.value = 'specialized';
    selectedSpecialized.value = presetType;
  } else if (presetTab) {
    activeTab.value = presetTab;
  }

  if (!presetType || !shouldAutoOpen || hasAppliedRouteAutoOpen.value) {
    return;
  }
  hasAppliedRouteAutoOpen.value = true;
  showSpecializedHistory(presetType);
};

const getLatestScore = (type) => {
  if (!specializedRecords.value || !specializedRecords.value[type]) {
    return '暂无';
  }
  const records = specializedRecords.value[type];
  return records.length > 0 ? records[0].score : '暂无';
};

const getTestCount = (type) => {
  if (!specializedRecords.value || !specializedRecords.value[type]) {
    return 0;
  }
  return specializedRecords.value[type].length;
};

const getSpecializedTitle = (type) => {
  const titles = {
    resume: '简历评测历史',
    questions: '试题作答历史',
    audio: '场景评测历史'
  };
  return titles[type] || '';
};

const showSpecializedHistory = (type) => {
  selectedSpecialized.value = type;
  showSpecializedModal.value = true;
  nextTick(() => {
    initChart();
  });
};

const closeSpecializedModal = () => {
  showSpecializedModal.value = false;
  if (chart) {
    chart.dispose();
    chart = null;
  }
};

const getSpecializedRecords = (type) => {
  if (!specializedRecords.value || !specializedRecords.value[type]) {
    return [];
  }
  return specializedRecords.value[type] || [];
};

const initChart = () => {
  if (!showSpecializedModal.value) return;

  const chartDom = document.querySelector('.chart');
  if (!chartDom) return;

  chart = echarts.init(chartDom);
  const records = getSpecializedRecords(selectedSpecialized.value);
  
  const option = {
    title: {
      text: getSpecializedTitle(selectedSpecialized.value),
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        if (!Array.isArray(params) || !params.length) return ''
        const p = params[0]
        const dt = formatDate(p.axisValue)
        return `${dt}\n${p.value}`
      }
    },
    xAxis: {
      type: 'category',
      data: records.map(record => record.date).reverse(),
      name: '日期',
      nameLocation: 'middle',
      nameGap: 30,
      axisLabel: {
        formatter: (value) => formatDate(value)
      }
    },
    yAxis: {
      type: 'value',
      name: '分数',
      min: 0,
      max: 100
    },
    series: [{
      data: records.map(record => record.score).reverse(),
      type: 'line',
      smooth: true,
      lineStyle: {
        color: '#4A6FFF'
      },
      itemStyle: {
        color: '#4A6FFF'
      }
    }]
  };

  chart.setOption(option);
};

// goToReport 跳转到综合报告详情页面
const goToReport = async (reportId) => {
  const token = uni.getStorageSync('token');
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    return;
  }
  
  try {
    // 跳转到综合报告详情页面
    uni.navigateTo({ 
      url: `/pages/comprehensive-report/index?id=${reportId}` 
    });
  } catch (err) {
    console.error('跳转失败:', err);
    uni.showToast({ title: '跳转失败', icon: 'none' });
  }
};

// 添加 watch 监听 isLoggedIn 和 userInfo 的变化
watch([() => userStore.isLoggedIn, () => userStore.userInfo], ([isLoggedIn, userInfo]) => {
  const token = uni.getStorageSync('token');
  
  console.log('Watch triggered:', {
    isLoggedIn: isLoggedIn,
    userInfo: userInfo,
    userId: userInfo?.userId,
    token: token
  }); // 添加日志

  // 只有当用户已登录、userInfo 存在且 token 存在时，才发起请求
  if (isLoggedIn && userInfo?.userId && token) {
       fetchHistoryData();
  } else {
      // 如果用户退出登录或信息不完整，可以清空数据
      comprehensiveRecords.value = [];
      specializedRecords.value = { resume: [], questions: [], audio: [] };
      isLoading.value = false;
      error.value = null;
  }
}, { immediate: true });

// 初始化护眼模式
const initEyeCareMode = () => {
  try {
    const savedEyeCareMode = localStorage.getItem('eyeCareMode');
    if (savedEyeCareMode === 'true') {
      isEyeCareMode.value = true;
    }
  } catch (error) {
    console.error('Error initializing eye care mode:', error);
  }
};

onMounted(() => {
  console.log('History page mounted.'); // 添加日志
  userStore.loadUserFromStorage(); // 在组件挂载时加载用户数据
  initEyeCareMode(); // 初始化护眼模式
  applyRoutePreset();
  try { subscribeUserSession(() => { fetchHistoryData(); }) } catch {}
});

watch(
  () => [route.query.tab, route.query.type, route.query.autoOpen],
  () => {
    applyRoutePreset();
  }
);

// Watch for window resize to handle chart responsiveness
const handleResize = () => {
  if (chart) {
    chart.resize();
  }
};

window.addEventListener('resize', handleResize);

// Clean up chart when component is unmounted
onUnmounted(() => {
  if (chart) {
    chart.dispose();
    chart = null;
  }
  window.removeEventListener('resize', handleResize);
});

const goBack = () => {
  uni.reLaunch({ url: '/pages/home/index' });
};

// 计算综合得分（基于优势分析和改进分析）
const calculateOverallScore = (record) => {
  // 这里可以根据实际业务逻辑计算综合得分
  // 暂时返回一个基于分析内容的简单评分
  const strengthCount = record.strengthAnalysis ? record.strengthAnalysis.split('\n').length : 0;
  const improvementCount = record.improvementAnalysis ? record.improvementAnalysis.split('\n').length : 0;
  
  // 基础分数 + 优势加分 - 改进项减分
  let score = 70; // 基础分数
  score += strengthCount * 3; // 每个优势项加3分
  score -= improvementCount * 2; // 每个改进项减2分
  
  return Math.max(0, Math.min(100, Math.round(score)));
};

// 从分析文本中提取分数（这里需要根据实际数据结构调整）
const extractScoreFromAnalysis = (analysis, type) => {
  // 由于API返回的是文本分析，这里暂时返回一个基于文本长度的估算分数
  if (!analysis) return 0;
  
  const textLength = analysis.length;
  const strengthWords = analysis.split('\n').length;
  
  // 根据文本长度和优势项数量估算分数
  let score = 60; // 基础分数
  score += Math.min(20, textLength / 10); // 文本长度加分
  score += strengthWords * 5; // 优势项加分
  
  return Math.max(0, Math.min(100, Math.round(score)));
};

// 生成摘要信息
const generateSummary = (strengthAnalysis, improvementAnalysis, learningRoute) => {
  const strengthCount = strengthAnalysis ? strengthAnalysis.split('\n').length : 0;
  const improvementCount = improvementAnalysis ? improvementAnalysis.split('\n').length : 0;
  
  if (strengthCount > improvementCount) {
    return '整体表现良好，优势明显。' + (learningRoute ? '建议按照学习路线继续提升。' : '');
  } else if (strengthCount === improvementCount) {
    return '表现均衡，有提升空间。' + (learningRoute ? '建议重点关注改进方向。' : '');
  } else {
    return '需要重点关注改进方向。' + (learningRoute ? '建议按照学习路线系统提升。' : '');
  }
};

const detailKeyToLabel = (key) => {
  const map = {
    expression: '表情',
    gesture: '姿态',
    eyeContact: '眼神',
    speech: '语言',
    professional: '专业性',
    correctCount: '答对题数',
    correctRate: '正确率',
    duration: '用时',
    professionalSkills: '专业技能',
    projectExecution: '项目执行',
    innovation: '创新能力',
    communication: '沟通能力',
    adaptability: '适应能力',
  };
  return map[key] || key;
};

function formatDate(dateStr) {
  if (!dateStr) return '-';
  if (typeof dateStr === 'string') {
    const m = dateStr.match(/^(\d{4}-\d{2}-\d{2})[T ](\d{2}:\d{2}:\d{2})/);
    if (m) return `${m[1]} ${m[2]}`;
  }
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return dateStr;
  const pad = n => n < 10 ? '0' + n : n;
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

function addHours(dateStr, hours = 0) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  const shifted = new Date(d.getTime() + hours * 3600000)
  const pad = n => n < 10 ? '0' + n : n
  return `${shifted.getFullYear()}-${pad(shifted.getMonth() + 1)}-${pad(shifted.getDate())} ${pad(shifted.getHours())}:${pad(shifted.getMinutes())}:${pad(shifted.getSeconds())}`
}
</script>

<style scoped>
.centered-container {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  min-height: 100vh;
  width: 100%;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  z-index: 0;
  transition: background-color 0.3s ease;
}

/* 使用更高的优先级确保护眼模式背景色生效 */
.centered-container.eye-care {
  background: #f0f2e6 !important;
  background-image: none !important;
}

/* 气泡容器 */
.bubbles {
  position: fixed;
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
  list-style: none;
  width: 20px;
  height: 20px;
  background-color: rgba(74, 111, 255, 0.2);
  border-radius: 50%;
  bottom: -150px;
  animation: float 25s linear infinite, fadeInOut 7s ease-in-out infinite alternate;
  z-index: 0;
}

/* Vary bubble sizes and positions */
.bubbles .bubble:nth-child(1) {
  left: 10%;
  width: 40px; height: 40px;
  animation-delay: 0s, 0.5s;
  animation-duration: 25s, 7s;
}
.bubbles .bubble:nth-child(2) {
  left: 20%;
  width: 25px; height: 25px;
  animation-delay: 2s, 1s;
  animation-duration: 12s, 6s;
}
.bubbles .bubble:nth-child(3) {
  left: 35%;
  width: 30px; height: 30px;
  animation-delay: 4s, 1.5s;
  animation-duration: 18s, 8s;
}
.bubbles .bubble:nth-child(4) {
  left: 50%;
  width: 50px; height: 50px;
  animation-delay: 0s, 2s;
  animation-duration: 20s, 5s;
}
.bubbles .bubble:nth-child(5) {
  left: 65%;
  width: 35px; height: 35px;
  animation-delay: 3s, 2.5s;
  animation-duration: 14s, 9s;
}
.bubbles .bubble:nth-child(6) {
  left: 75%;
  width: 60px; height: 60px;
  animation-delay: 0s, 3s;
  animation-duration: 22s, 7s;
}
.bubbles .bubble:nth-child(7) {
  left: 88%;
  width: 30px; height: 30px;
  animation-delay: 1s, 3.5s;
  animation-duration: 16s, 6s;
}
.bubbles .bubble:nth-child(8) {
  left: 25%;
  width: 45px; height: 45px;
  animation-delay: 5s, 4s;
  animation-duration: 28s, 8s;
}
.bubbles .bubble:nth-child(9) {
  left: 40%;
  width: 55px; height: 55px;
  animation-delay: 2s, 4.5s;
  animation-duration: 19s, 5s;
}
.bubbles .bubble:nth-child(10) {
  left: 60%;
  width: 30px; height: 30px;
  animation-delay: 6s, 5s;
  animation-duration: 24s, 9s;
}
.bubbles .bubble:nth-child(11) {
  left: 5%;
  width: 50px; height: 50px;
  animation-delay: 1s, 5.5s;
  animation-duration: 21s, 6s;
}
.bubbles .bubble:nth-child(12) {
  left: 95%;
  width: 40px; height: 40px;
  animation-delay: 4s, 6s;
  animation-duration: 23s, 8s;
}
.bubbles .bubble:nth-child(13) {
  left: 50%;
  width: 25px; height: 25px;
  animation-delay: 7s, 6.5s;
  animation-duration: 15s, 7s;
}
.bubbles .bubble:nth-child(14) {
  left: 15%;
  width: 35px; height: 35px;
  animation-delay: 3s, 7s;
  animation-duration: 26s, 5s;
}
.bubbles .bubble:nth-child(15) {
  left: 80%;
  width: 45px; height: 45px;
  animation-delay: 0s, 7.5s;
  animation-duration: 17s, 9s;
}

/* Float animation */
@keyframes float {
  0% {
    bottom: -100px;
    transform: translateX(0);
  }
  100% {
    bottom: 100%;
    transform: translateX(200px);
  }
}

/* Fade in and out animation */
@keyframes fadeInOut {
  0% { opacity: 0.5; }
  50% { opacity: 1; }
  100% { opacity: 0.5; }
}

.history-container {
  padding: 30px 20px;
  max-width: 800px;
  width: 90%;
  font-family: 'Arial', sans-serif;
  color: #333;
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
  margin: 60px auto 40px;
}

.header {
  position: relative;
  text-align: center;
  margin-bottom: 20px;
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

.title {
  display: inline-block;
  font-size: 1.8em;
  color: #333;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.1);
}

.tab-container {
  display: flex;
  justify-content: center;
  margin: 20px 0 40px;
  gap: 20px;
}

.tab-item {
  padding: 12px 40px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #f0f2f5;
  font-size: 16px;
}

.tab-item.active {
  background-color: #4A6FFF;
  color: white;
  border-bottom: 2px solid #4A6FFF;
}

.records-list {
    margin-top: 20px;
}

.record-item {
  background-color: #fff;
  border: 1px solid #e0e0e0;
  margin-bottom: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  padding: 25px;
}

.record-item:last-child {
  margin-bottom: 0;
}

.record-item:hover {
  transform: translateY(-5px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.record-link {
  display: block;
  text-decoration: none;
  color: inherit;
}

.record-info {
    display: flex;
    justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.record-date,
.record-job,
.record-score {
  font-size: 1.1em;
  color: #333;
  font-weight: 500;
}

.record-score {
  color: #4A6FFF;
}

.record-details {
  display: flex;
  flex-direction: column;
}

.score-breakdown {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin: 15px 0;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 12px;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
}

.score-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.score-label {
  color: #666;
  font-size: 0.95em;
}

.score-value {
    font-weight: bold;
  color: #4A6FFF;
  font-size: 1.1em;
}

.record-summary {
  margin-top: 15px;
  color: #555;
  line-height: 1.6;
    font-size: 1em;
  padding: 0 15px;
}

.specialized-records {
  margin-top: 20px;
}

.specialized-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  padding: 20px;
}

.specialized-item {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.specialized-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.specialized-icon {
  font-size: 2em;
  margin-bottom: 10px;
}

.specialized-title {
  font-size: 1.2em;
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
}

.specialized-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat {
  font-size: 0.9em;
  color: #666;
}

.specialized-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 16px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.modal-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
  font-weight: 500;
}

.close-button {
  position: absolute;
  right: 20px;
  top: 20px;
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #999;
  transition: color 0.3s ease;
  padding: 0;
  line-height: 1;
}

.close-button:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
}

.chart-container {
  width: 100%;
  height: 400px;
  margin-bottom: 30px;
}

.chart {
  width: 100%;
  height: 100%;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #666;
}

.loading-state {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4A6FFF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  color: #ff4d4f;
  text-align: center;
  margin-top: 20px;
  padding: 20px;
}

.retry-button {
  background-color: #4A6FFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  margin-top: 10px;
  font-size: 14px;
  transition: background-color 0.3s ease;
}

.retry-button:hover {
  background-color: #3a5fef;
}

.history-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.history-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 8px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 8px;
  background-color: white;
  border-radius: 4px;
  font-size: 0.9em;
}

.detail-label {
  color: #666;
}

.detail-value {
  font-weight: 500;
  color: #4A6FFF;
}

.suggestions {
  grid-column: 1 / -1;
  flex-direction: column;
  align-items: flex-start;
}

.suggestion-list {
  width: 100%;
  margin-top: 8px;
}

.suggestion-item {
  padding: 8px;
  background-color: #f0f2f5;
  border-radius: 4px;
  margin-bottom: 4px;
  font-size: 0.9em;
  color: #666;
}

.comprehensive-modal {
  position: fixed;
  top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center;
  z-index: 2000;
}
.comprehensive-modal .modal-content {
  background: #fff;
  border-radius: 12px;
  max-width: 500px;
  width: 90vw;
  padding: 24px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.15);
}
.comprehensive-modal .modal-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px;
}
.comprehensive-modal .close-button {
  background: none; border: none; font-size: 24px; cursor: pointer;
}
.detail-row { margin-bottom: 12px; }
.detail-block { background: #f8f9fa; border-radius: 6px; padding: 8px 12px; margin-top: 4px; }

/* 专项记录详情弹窗样式 */
.specialized-detail-modal {
  position: fixed;
  top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center;
  z-index: 2000;
}

.specialized-detail-modal .modal-content {
  background: #fff;
  border-radius: 12px;
  max-width: 800px;
  width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 24px rgba(0,0,0,0.15);
}

.specialized-detail-modal .modal-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px;
  padding: 20px 24px 0;
}

.specialized-detail-modal .modal-body {
  padding: 0 24px 24px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  color: #333;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f2f5;
}

.content-block {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  line-height: 1.6;
  color: #333;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 300px;
  overflow-y: auto;
}

.score-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.score-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #4A6FFF;
}

.score-label {
  color: #666;
  font-size: 14px;
}

.score-value {
  font-weight: 600;
  color: #4A6FFF;
  font-size: 16px;
}

.suggestions-list {
  margin-top: 12px;
}

.suggestion-item {
  padding: 12px 16px;
  background: #f0f2f5;
  border-radius: 8px;
  margin-bottom: 8px;
  color: #333;
  line-height: 1.5;
  border-left: 4px solid #4A6FFF;
}

/* 让历史记录项可点击 */
.history-item {
  cursor: pointer;
  transition: all 0.3s ease;
}

.history-item:hover {
  background-color: #f0f2f5;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

@media screen and (max-width: 768px) {
  .history-container {
    margin: 40px auto 30px;
    padding: 30px 20px;
  }

  .tab-item {
    padding: 10px 30px;
    font-size: 14px;
  }

  .specialized-grid {
    grid-template-columns: 1fr;
  }

  .modal-content {
    width: 95%;
    max-height: 95vh;
  }

  .chart-container {
    height: 300px;
  }

  .score-breakdown {
    grid-template-columns: 1fr;
  }

  .close-button {
    right: 15px;
    top: 15px;
    width: 28px;
    height: 28px;
    font-size: 22px;
  }

  .history-details {
    grid-template-columns: 1fr;
  }
  
  .specialized-detail-modal .modal-content {
    width: 95vw;
    max-height: 95vh;
  }
  
  .score-grid {
    grid-template-columns: 1fr;
  }
  
  .content-block {
    max-height: 200px;
  }
}
</style> 

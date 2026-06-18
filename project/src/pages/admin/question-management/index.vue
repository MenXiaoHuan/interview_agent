<template>
  <div class="question-management-container" :class="{ 'eye-care': isEyeCareMode }">
    <div class="header">
      <span class="back-icon" @click="goBack" aria-label="返回" role="button">‹</span>
      <h1 class="page-title">试题评测管理</h1>
    </div>

    <div class="management-content">

             <!-- 岗位选择区域 -->
       <div class="job-selection-panel">
         <h2 class="section-title">选择岗位</h2>
         <div class="filter-row">
           <div class="filter-group">
             <label>岗位</label>
             <select v-model="filters.jobPosition" @change="handleJobPositionChange">
               <option value="">全部岗位</option>
               <option v-for="job in jobs" :key="job.id" :value="job.id">
                 {{ job.name }}
               </option>
             </select>
           </div>
         </div>
        <div class="filter-row">
          <div class="filter-group search-group">
            <input type="text" v-model="filters.keyword" placeholder="搜索试题关键词...">
            <button class="search-btn" @click="searchQuestions">搜索</button>
          </div>
          <button class="add-btn" @click="showAddQuestionModal">
            <span class="add-icon">+</span> 添加试题
          </button>
        </div>
      </div>

      <!-- 试题列表 -->
      <div class="questions-panel">
        <h2 class="section-title">试题列表</h2>
        <div v-if="isLoading" class="loading-container">
          <div class="loading-spinner"></div>
          <p class="loading-text">加载中...</p>
        </div>
        <template v-else>
          <div class="questions-table">
            <div class="table-header">
              <div class="th question-title">试题标题</div>
              <div class="th question-type">类型</div>
              <div class="th question-difficulty">难度</div>
              <div class="th question-actions">操作</div>
            </div>
            <div v-for="question in questions" :key="question.id" class="table-row">
              <div class="td question-title">{{ question.title }}</div>
              <div class="td question-type">
                <span class="type-badge" :class="question.type">
                  {{ getQuestionTypeName(question.type) }}
                </span>
              </div>
              <div class="td question-difficulty">
                <div class="difficulty-stars">
                  <span v-for="n in 5" :key="n" class="star" :class="{ 'filled': n <= question.difficulty }">★</span>
                </div>
              </div>
              <div class="td question-actions">
                <button class="view-btn" @click="viewQuestion(question)">查看</button>
                <button class="edit-btn" @click="editQuestion(question)">编辑</button>
                <button class="delete-btn" @click="deleteQuestion(question)">删除</button>
              </div>
            </div>
          </div>
          <div v-if="allQuestions.length === 0" class="empty-list">
            暂无试题数据，请先添加试题
          </div>
          

        </template>

        <!-- 分页控件 -->
        <div class="pagination" v-if="!isLoading && allQuestions.length > pageSize">
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

    <!-- 添加/编辑试题弹窗 -->
    <div class="modal" v-if="showQuestionModal" @click.self="closeQuestionModal">
      <div class="modal-content question-modal">
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑' : '添加' }}试题</h3>
          <span class="close-btn" @click="closeQuestionModal">&times;</span>
        </div>
        <div class="modal-body">
                     <div class="form-group">
             <label for="questionTitle">试题标题</label>
             <input type="text" id="questionTitle" v-model="questionForm.title" placeholder="请输入试题标题">
           </div>
           <div class="form-group">
             <label for="questionJob">选择岗位</label>
             <select id="questionJob" v-model="questionForm.jobId" required>
               <option value="">请选择岗位</option>
               <option v-for="job in jobs" :key="job.id" :value="job.id">
                 {{ job.name }}
               </option>
             </select>
           </div>
          <div class="form-group">
            <label for="questionType">试题类型</label>
            <select id="questionType" v-model="questionForm.type">
              <option value="single">单选题</option>
              <option value="multiple">多选题</option>
              <option value="essay">问答题</option>
              <option value="coding">编程题</option>
            </select>
          </div>
          <div class="form-group">
            <label for="questionDifficulty">难度等级</label>
            <div class="difficulty-selector">
              <span 
                v-for="n in 5" 
                :key="n" 
                class="difficulty-star" 
                :class="{ 'filled': n <= questionForm.difficulty }"
                @click="questionForm.difficulty = n"
              >★</span>
            </div>
          </div>

          
          <!-- 选择题选项 -->
          <div class="form-group" v-if="questionForm.type === 'single' || questionForm.type === 'multiple'">
            <label>选项</label>
            <div v-for="(option, index) in questionForm.options" :key="index" class="option-item">
              <div class="option-input">
                <input 
                  type="checkbox" 
                  :id="'option-correct-' + index" 
                  v-model="option.isCorrect"
                  :disabled="questionForm.type === 'single' && hasSelectedOption && !option.isCorrect"
                >
                <label :for="'option-correct-' + index">正确答案</label>
              </div>
              <input type="text" v-model="option.text" :placeholder="'选项 ' + (index + 1)">
              <button type="button" class="remove-option-btn" @click="removeOption(index)">&times;</button>
            </div>
            <button type="button" class="add-option-btn" @click="addOption">
              <span class="add-icon">+</span> 添加选项
            </button>
          </div>
          
          <!-- 问答题答案 -->
          <div class="form-group" v-if="questionForm.type === 'essay'">
            <label for="questionAnswer">参考答案</label>
            <textarea id="questionAnswer" v-model="questionForm.answer" placeholder="请输入参考答案" rows="3"></textarea>
          </div>
          
          <!-- 编程题答案 -->
          <div class="form-group" v-if="questionForm.type === 'coding'">
            <label for="questionSolution">参考解法</label>
            <textarea id="questionSolution" v-model="questionForm.solution" placeholder="请输入参考解法代码" rows="5" class="code-textarea"></textarea>
          </div>
          
          <div class="form-group">
            <label for="questionExplanation">解析</label>
            <textarea id="questionExplanation" v-model="questionForm.explanation" placeholder="请输入试题解析" rows="3"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeQuestionModal">取消</button>
          <button class="save-btn" @click.prevent="saveQuestion" type="button">保存</button>
        </div>
      </div>
    </div>

    <!-- 查看试题弹窗 -->
    <div class="modal" v-if="showViewModal">
      <div class="modal-content view-modal">
        <div class="modal-header">
          <h3>查看试题</h3>
          <span class="close-btn" @click="closeViewModal">&times;</span>
        </div>
        <div class="modal-body">
          <div class="view-group">
            <h4>试题标题</h4>
            <div class="view-content">{{ currentQuestion.title }}</div>
          </div>
          <div class="view-group">
            <h4>试题类型</h4>
            <div class="view-content">
              <span class="type-badge" :class="currentQuestion.type">
                {{ getQuestionTypeName(currentQuestion.type) }}
              </span>
            </div>
          </div>
          <div class="view-group">
            <h4>难度等级</h4>
            <div class="view-content">
              <div class="difficulty-stars">
                <span v-for="n in 5" :key="n" class="star" :class="{ 'filled': n <= currentQuestion.difficulty }">★</span>
              </div>
            </div>
          </div>

          
          <!-- 选择题选项 -->
          <div class="view-group" v-if="currentQuestion.type === 'single' || currentQuestion.type === 'multiple'">
            <h4>选项</h4>
            <div class="view-content">
              <div v-for="(option, index) in currentQuestion.options" :key="index" class="option-view">
                <span class="option-marker" :class="{ 'correct': option.isCorrect }">
                  {{ String.fromCharCode(65 + index) }}
                </span>
                <span class="option-text">{{ option.text }}</span>
                <span v-if="option.isCorrect" class="correct-label">(正确答案)</span>
              </div>
            </div>
          </div>
          
          <!-- 问答题答案 -->
          <div class="view-group" v-if="currentQuestion.type === 'essay'">
            <h4>参考答案</h4>
            <div class="view-content content-box">{{ currentQuestion.answer }}</div>
          </div>
          
          <!-- 编程题答案 -->
          <div class="view-group" v-if="currentQuestion.type === 'coding'">
            <h4>参考解法</h4>
            <div class="view-content content-box code-box">{{ currentQuestion.solution }}</div>
          </div>
          
          <div class="view-group">
            <h4>解析</h4>
            <div class="view-content content-box">{{ currentQuestion.explanation }}</div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="edit-btn" @click="editFromView">编辑</button>
          <button class="close-view-btn" @click="closeViewModal">关闭</button>
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
          <p>确定要删除试题"{{ deleteTarget.title }}"吗？</p>
          <p class="warning-text">
            注意：删除试题将同时删除与该试题相关的所有评测记录！
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
import { API } from '@/utils/api';
import { getAllChoiceQuestions, getChoiceQuestionsByJob, addChoiceQuestion, updateChoiceQuestion, deleteChoiceQuestion } from '@/utils/api';
import request from '@/utils/request';

const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);

// 数据状态
const isLoading = ref(false);
const jobs = ref([]);
const allQuestions = ref([]); // 存储所有试题数据
const questions = ref([]); // 当前页显示的试题

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

// 模态框状态
const showQuestionModal = ref(false);
const showViewModal = ref(false);
const showDeleteModal = ref(false);
const isEditing = ref(false);

// 当前查看的试题
const currentQuestion = ref({});

// 表单数据
const questionForm = ref({
  id: '',
  title: '',
  jobId: '',
  type: 'single',
  difficulty: 3,
  options: [],
  answer: '',
  solution: '',
  explanation: ''
});

// 删除目标
const deleteTarget = ref({
  id: '',
  title: ''
});

// 计算属性
const hasSelectedOption = computed(() => {
  return questionForm.value.options.some(option => option.isCorrect);
});





// 获取岗位数据
const fetchJobs = async () => {
  try {
    const token = uni.getStorageSync('token');
    if (!token) {
      throw new Error('请先登录');
    }

    const response = await request({
      url: API.JOB.LIST,
      method: 'GET'
    });

    if (response.code === 200) {
      const data = response.data;
      jobs.value = data || [];
    } else {
      throw new Error(response.message || '获取岗位数据失败');
    }
  } catch (error) {
    console.error('获取岗位数据失败:', error);
    uni.showToast({
      title: '获取岗位数据失败',
      icon: 'none'
    });
  }
};

// 获取试题数据
const fetchQuestions = async () => {
  isLoading.value = true;
  try {
    const token = uni.getStorageSync('token');
    if (!token) {
      throw new Error('请先登录');
    }

    let response;
    if (filters.value.jobPosition) {
      // 如果选择了岗位，获取该岗位的试题
      response = await getChoiceQuestionsByJob(filters.value.jobPosition);
    } else {
      // 如果没有选择岗位，获取所有试题
      response = await getAllChoiceQuestions();
    }

    // response 已经是处理过的数据，直接使用
    const data = response.data;
    // 转换数据格式以适配现有的UI
    // 检查数据结构，可能是 data.data 或直接是 data
    const questionsData = data.data || data;
    
    // 确保数据是数组格式
    const questionsArray = Array.isArray(questionsData) ? questionsData : [];
    
    const mapped = questionsArray.map(item => {
      // 解析选项，options是字符串化的JSON数组
      let optionsArray = [];
      try {
        if (typeof item.options === 'string') {
          optionsArray = JSON.parse(item.options);
        } else if (Array.isArray(item.options)) {
          optionsArray = item.options;
        }
      } catch (e) {
        console.error('解析选项失败:', e);
        optionsArray = [];
      }
      
      // 找到正确答案的索引
      const correctIndex = optionsArray.findIndex(option => option === item.correctAnswer);
      
      return {
        id: item.id,
        title: item.question,
        type: 'single', // 选择试题默认为单选题
        difficulty: item.difficulty || 3, // 使用API返回的难度或默认值
        content: item.question,
        options: optionsArray.map((option, index) => ({
          text: option,
          isCorrect: index === correctIndex
        })),
        jobId: item.jobId,
        explanation: item.explanation || ''
      };
    });

    // 关键词过滤
    const keyword = (filters.value.keyword || '').trim().toLowerCase();
    allQuestions.value = keyword
      ? mapped.filter(q => ((q.title || '').toLowerCase().includes(keyword)))
      : mapped;
    
    // 设置分页信息
    totalItems.value = allQuestions.value.length;
    
    // 应用客户端分页
    updatePagedQuestions();
  } catch (error) {
    console.error('获取试题数据失败:', error);
    uni.showToast({
      title: '获取试题数据失败',
      icon: 'none'
    });
    allQuestions.value = [];
    questions.value = [];
    totalItems.value = 0;
  } finally {
    isLoading.value = false;
  }
};



// 更新分页后的试题数据
const updatePagedQuestions = () => {
  // 确保当前页不超过总页数
  if (totalPages.value > 0 && currentPage.value > totalPages.value) {
    currentPage.value = totalPages.value;
  }
  
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  questions.value = allQuestions.value.slice(startIndex, endIndex);
};

// 处理岗位选择变化
const handleJobPositionChange = () => {
  currentPage.value = 1;
  totalItems.value = 0;
  fetchQuestions();
};

// 搜索试题
const searchQuestions = () => {
  currentPage.value = 1;
  totalItems.value = 0;
  fetchQuestions();
};

// 切换页码
const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  updatePagedQuestions();
};

// 获取试题类型名称
const getQuestionTypeName = (type) => {
  const typeMap = {
    'single': '单选题',
    'multiple': '多选题',
    'essay': '问答题',
    'coding': '编程题'
  };
  return typeMap[type] || '未知类型';
};

// 添加试题
const showAddQuestionModal = () => {
  isEditing.value = false;
  questionForm.value = {
    id: '',
    title: '',
    jobId: filters.value.jobPosition || '', // 使用当前选择的岗位或空
    type: 'single',
    difficulty: 3,
    options: [
      { text: '', isCorrect: false },
      { text: '', isCorrect: false },
      { text: '', isCorrect: false },
      { text: '', isCorrect: false }
    ],
    answer: '',
    solution: '',
    explanation: ''
  };
  showQuestionModal.value = true;
};

// 编辑试题
const editQuestion = (question) => {
  isEditing.value = true;
  // 深拷贝并确保选项格式正确
  const questionCopy = JSON.parse(JSON.stringify(question));
  
  // 确保选项数组存在且格式正确
  if (!Array.isArray(questionCopy.options)) {
    questionCopy.options = [];
  }
  
  questionForm.value = questionCopy;
  showQuestionModal.value = true;
};

// 从查看页面编辑
const editFromView = () => {
  closeViewModal();
  editQuestion(currentQuestion.value);
};

// 查看试题
const viewQuestion = (question) => {
  // 深拷贝并确保数据格式正确
  const questionCopy = JSON.parse(JSON.stringify(question));
  
  // 确保选项数组存在且格式正确
  if (!Array.isArray(questionCopy.options)) {
    questionCopy.options = [];
  }
  
  currentQuestion.value = questionCopy;
  showViewModal.value = true;
};

// 关闭查看弹窗
const closeViewModal = () => {
  showViewModal.value = false;
};

// 添加选项
const addOption = () => {
  questionForm.value.options.push({ text: '', isCorrect: false });
};

// 移除选项
const removeOption = (index) => {
  questionForm.value.options.splice(index, 1);
};

// 保存试题
const saveQuestion = async () => {
  // 表单验证
  if (!questionForm.value.title) {
    uni.showToast({
      title: '请输入试题标题',
      icon: 'none'
    });
    return;
  }

  if (!questionForm.value.jobId) {
    uni.showToast({
      title: '请选择岗位',
      icon: 'none'
    });
    return;
  }

  // 选择题验证
  if ((questionForm.value.type === 'single' || questionForm.value.type === 'multiple') && 
      (!questionForm.value.options.length || !hasSelectedOption.value)) {
    uni.showToast({
      title: '请添加选项并标记正确答案',
      icon: 'none'
    });
    return;
  }

  // 问答题验证
  if (questionForm.value.type === 'essay' && !questionForm.value.answer) {
    uni.showToast({
      title: '请输入参考答案',
      icon: 'none'
    });
    return;
  }

  // 编程题验证
  if (questionForm.value.type === 'coding' && !questionForm.value.solution) {
    uni.showToast({
      title: '请输入参考解法',
      icon: 'none'
    });
    return;
  }

  try {
    const token = uni.getStorageSync('token');
    if (!token) {
      throw new Error('请先登录');
    }

    // 转换数据格式以适配后端API
    const requestData = {
      question: questionForm.value.title,
      options: JSON.stringify(questionForm.value.options.map(option => option.text)), // 字符串化选项数组
      correctAnswer: questionForm.value.options.find(option => option.isCorrect)?.text || questionForm.value.options[0]?.text || '', // 正确答案的完整文本
      explanation: questionForm.value.explanation || '',
      difficulty: questionForm.value.difficulty || 3,
      score: 10, // 默认分数
      jobId: questionForm.value.jobId // 使用表单中选择的岗位
    };

    if (isEditing.value) {
      requestData.id = questionForm.value.id;
    }

    const response = isEditing.value 
      ? await updateChoiceQuestion(requestData)
      : await addChoiceQuestion(requestData);

    // response 已经是处理过的数据，直接使用
    uni.showToast({
      title: isEditing.value ? '更新成功' : '添加成功',
      icon: 'success'
    });
    
    // 刷新试题数据
    await fetchQuestions();
    closeQuestionModal();
  } catch (error) {
    console.error(isEditing.value ? '更新试题失败:' : '添加试题失败:', error);
    uni.showToast({
      title: error.message || (isEditing.value ? '更新失败' : '添加失败'),
      icon: 'none'
    });
    
    // 模拟成功，更新本地数据
    if (isEditing.value) {
      const index = allQuestions.value.findIndex(item => item.id === questionForm.value.id);
      if (index !== -1) {
        allQuestions.value[index] = { ...questionForm.value };
        updatePagedQuestions();
      }
    } else {
      const newId = String(Date.now());
      const newQuestion = {
        id: newId,
        ...questionForm.value
      };
      allQuestions.value.push(newQuestion);
      totalItems.value = allQuestions.value.length;
      updatePagedQuestions();
    }
    
    closeQuestionModal();
  }
};

// 关闭试题弹窗
const closeQuestionModal = () => {
  showQuestionModal.value = false;
};

// 删除试题
const deleteQuestion = (question) => {
  deleteTarget.value = {
    id: question.id,
    title: question.title
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

    const response = await deleteChoiceQuestion(deleteTarget.value.id);

    // response 已经是处理过的数据，直接使用
    uni.showToast({
      title: '删除成功',
      icon: 'success'
    });
    
    // 刷新试题数据
    await fetchQuestions();
    closeDeleteModal();
  } catch (error) {
    console.error('删除试题失败:', error);
    uni.showToast({
      title: error.message || '删除失败',
      icon: 'none'
    });
    
    // 模拟成功，更新本地数据
    allQuestions.value = allQuestions.value.filter(item => item.id !== deleteTarget.value.id);
    totalItems.value = allQuestions.value.length;
    updatePagedQuestions();
    
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





// 生命周期钩子
onMounted(async () => {
  await fetchJobs();
  await fetchQuestions(); // 页面加载时获取所有试题
});
</script>

<style scoped>
.question-management-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  transition: background-color 0.3s ease;
}

.question-management-container.eye-care {
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

.search-group {
  display: flex;
  gap: 10px;
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
  background: white;
  color: #4A6FFF;
  border: 1px solid #4A6FFF;
  border-radius: 6px;
  height: 40px;
  padding: 0 15px;
  font-size: 0.9em;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.add-btn:hover {
  background: #f0f4ff;
}

.add-btn:disabled {
  background: #a0aec0;
  cursor: not-allowed;
}

.add-icon {
  font-size: 1.2em;
  font-weight: bold;
}

/* 试题列表 */
.questions-panel {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.questions-table {
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

.question-title {
  font-weight: 500;
}

.question-type {
  text-align: center;
}

.question-difficulty {
  text-align: center;
}

.question-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.type-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.85em;
  font-weight: 500;
}

.type-badge.single {
  background: #d1ecf1;
  color: #0c5460;
}

.type-badge.multiple {
  background: #d4edda;
  color: #155724;
}

.type-badge.essay {
  background: #fff3cd;
  color: #856404;
}

.type-badge.coding {
  background: #f8d7da;
  color: #721c24;
}

.difficulty-stars {
  display: flex;
  justify-content: center;
  gap: 2px;
}

.star {
  color: #ccc;
  font-size: 1.2em;
}

.star.filled {
  color: #ffc107;
}

.view-btn, .edit-btn, .delete-btn {
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 0.85em;
  cursor: pointer;
  border: none;
  transition: background-color 0.3s ease;
}

.view-btn {
  background: #17a2b8;
  color: white;
}

.view-btn:hover {
  background: #138496;
}

.edit-btn {
  background: #4A6FFF;
  color: white;
}

.edit-btn:hover {
  background: #3A5FEF;
}

.delete-btn {
  background: #dc3545;
  color: white;
}

.delete-btn:hover {
  background: #c82333;
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
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
}

.question-modal {
  max-width: 700px;
}

.view-modal {
  max-width: 700px;
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
  position: sticky;
  top: 0;
  background: white;
  z-index: 1;
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
  position: sticky;
  bottom: 0;
  background: white;
  z-index: 1;
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

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1em;
}

.form-group textarea {
  min-height: 100px;
  resize: vertical;
}

.code-textarea {
  font-family: monospace;
}

.difficulty-selector {
  display: flex;
  gap: 5px;
}

.difficulty-star {
  font-size: 1.5em;
  cursor: pointer;
  color: #ccc;
}

.difficulty-star.filled {
  color: #ffc107;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.option-input {
  display: flex;
  align-items: center;
  gap: 5px;
  min-width: 120px;
}

.remove-option-btn {
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.add-option-btn {
  background: #4A6FFF;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 0.9em;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.view-group {
  margin-bottom: 20px;
}

.view-group h4 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 1em;
}

.view-content {
  color: #666;
}

.content-box {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 6px;
  white-space: pre-wrap;
}

.code-box {
  font-family: monospace;
}

.option-view {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.option-marker {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #e9ecef;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.option-marker.correct {
  background: #28a745;
  color: white;
}

.option-text {
  flex: 1;
}

.correct-label {
  color: #28a745;
  font-weight: bold;
  margin-left: 10px;
}

.cancel-btn,
.save-btn,
.delete-confirm-btn,
.close-view-btn {
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

.close-view-btn {
  background: #6c757d;
  color: white;
}

.close-view-btn:hover {
  background: #5a6268;
}

.warning-text {
  color: #dc3545;
  font-weight: 500;
}

/* 护眼模式适配 */
.question-management-container.eye-care .job-selection-panel,
.question-management-container.eye-care .questions-panel,
.question-management-container.eye-care .modal-content {
  background-color: #f8f9e8;
}

.question-management-container.eye-care .table-header {
  background-color: #f0f2e6;
}

.question-management-container.eye-care .table-row:hover {
  background-color: #f0f2e6;
}

.question-management-container.eye-care .content-box {
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
  
  .question-actions {
    justify-content: flex-start;
  }
}
</style>

<template>
  <div class="centered-container" :class="{ 'eye-care': isEyeCareMode }">
    <!-- 气泡背景效果 -->
    <div class="bubbles">
      <div v-for="n in 20" :key="n" class="bubble"></div>
    </div>
    
    <div class="personal-center-container">
      <div class="header">
        <span class="back-icon" @click="goBack" aria-label="返回" role="button">‹</span>
      <h1 class="page-title">个人中心</h1>
      </div>

      <div class="user-avatar-section">
        <div class="avatar-wrapper">
          <img 
            :src="userAvatar" 
            alt="用户头像" 
            class="user-avatar"
            @click="showFullImage"
            @error="handleAvatarError"
          >
          <div class="upload-overlay" @click="chooseAvatar">
            <span class="upload-icon">+</span>
          </div>
        </div>
        <div class="nickname-section">
          <div v-if="!isEditingNickname" class="nickname-display" @click="startEditingNickname">
            <span class="nickname">{{ nickname }}</span>
            <span class="edit-hint">点击修改</span>
          </div>
          <div v-else class="nickname-edit">
          <input 
              ref="nicknameInput"
              v-model="editingNickname"
              type="text"
            class="nickname-input" 
              @blur="saveNickname"
              @keyup.enter="saveNickname"
              maxlength="20"
          >
          </div>
        </div>
      </div>

      <div v-if="showModal" class="image-modal" @click="hideFullImage">
        <img 
          :src="userAvatar" 
          alt="用户头像" 
          class="full-size-image"
          @error="handleAvatarError"
        >
      </div>

      <div v-if="isAdmin" class="menu-item admin-item" @click="goToAdminCenter">
        <div class="menu-item-left">
          <svg class="menu-svg" width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M5 15 L7 10 L9.5 12 L12 8 L14.5 12 L17 10 L19 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M5 16 Q12 19 19 16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <circle cx="7" cy="10" r="1.1" fill="currentColor"/>
            <circle cx="9.5" cy="12" r="1.1" fill="currentColor"/>
            <circle cx="12" cy="8" r="1.1" fill="currentColor"/>
            <circle cx="14.5" cy="12" r="1.1" fill="currentColor"/>
            <circle cx="17" cy="10" r="1.1" fill="currentColor"/>
          </svg>
          <span>管理员中心</span>
        </div>
        <span class="arrow">></span>
      </div>

      <div class="menu-list">
        <div class="menu-item" @click="goToProfile">
          <div class="menu-item-left">
            <svg class="menu-svg" width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/>
              <path d="M4 20c1.8-3.2 5-5 8-5s6.2 1.8 8 5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <span>修改个人资料</span>
          </div>
          <span class="arrow">></span>
        </div>
        <div class="menu-item" @click="showFeedbackDialog">
          <div class="menu-item-left">
            <svg class="menu-svg" width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="6" width="14" height="10" rx="3" stroke="currentColor" stroke-width="2"/>
              <path d="M7 16 L5 18 L5 16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M6 9h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M6 12h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <span>意见反馈</span>
          </div>
          <span class="arrow">></span>
        </div>
        <div class="menu-item" @click="showAboutDialog">
          <div class="menu-item-left">
            <svg class="menu-svg" width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
              <path d="M12 8v0.5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M12 11v6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <span>关于我们</span>
          </div>
          <span class="arrow">></span>
        </div>
        <div class="menu-item warning" @click="handleLogout">
          <div class="menu-item-left">
            <svg class="menu-svg" width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M10 5h7v14h-7" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M7 12h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M9 9l-3 3 3 3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>退出登录</span>
          </div>
          <span class="arrow">></span>
        </div>
      </div>
    </div>

    <div class="profile-dialog" v-if="showProfileModal" @click.self="closeProfileModal" role="dialog" aria-labelledby="profile-title" aria-modal="true">
      <div class="profile-content">
        <div class="profile-header">
          <h2 id="profile-title">个人资料</h2>
          <span class="close-btn" @click="closeProfileModal">×</span>
        </div>
        <div class="profile-body" v-if="activeProfileTab==='menu'">
          <div class="info-card">
            <div class="info-row"><span class="label">用户名</span><span class="value">{{ username || '未设置用户名' }}</span></div>
            <div class="info-row"><span class="label">用户昵称</span><span class="value">{{ nickname || '未命名用户' }}</span></div>
            <div class="info-row"><span class="label">注册时间</span><span class="value">{{ registerTime || '未知' }}</span></div>
            <div class="info-row"><span class="label">用户类型</span><span class="value">{{ identityText || '未知' }}</span></div>
          </div>
          <div class="menu-list">
            <div class="menu-item" @click="activeProfileTab='password'">
              <div class="menu-item-left"><svg class="menu-svg" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="5" y="10" width="14" height="9" rx="2" stroke="currentColor" stroke-width="2"/><path d="M8 10V8a4 4 0 0 1 8 0v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg><span>修改密码</span></div>
              <span class="arrow">></span>
            </div>
            <div class="menu-item" @click="activeProfileTab='email'">
              <div class="menu-item-left"><svg class="menu-svg" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M4 6h16v12H4z" stroke="currentColor" stroke-width="2"/><path d="M4 8l8 5 8-5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg><span>绑定邮箱</span><span class="status" :class="emailStatus.class">{{ emailStatus.text }}</span></div>
              <span class="arrow">></span>
            </div>
            <div class="menu-item" @click="activeProfileTab='phone'">
              <div class="menu-item-left"><svg class="menu-svg" width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="6" y="4" width="12" height="16" rx="2" stroke="currentColor" stroke-width="2"/><circle cx="12" cy="18" r="1" fill="currentColor"/><path d="M9 7h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg><span>绑定手机</span><span class="status" :class="phoneStatus.class">{{ phoneStatus.text }}</span></div>
              <span class="arrow">></span>
            </div>
          </div>
        </div>
        <div class="profile-body" v-else-if="activeProfileTab==='password'">
          <div class="sub-header" @click="activeProfileTab='menu'">‹ 返回</div>
          <div class="form-item">
            <label for="cur-pass">当前密码</label>
            <div class="input-wrapper" :class="{error: passwordErrors.currentPassword}"><svg class="input-svg" width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 7a4 4 0 1 1 8 0v3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><rect x="5" y="10" width="14" height="9" rx="2" stroke="currentColor" stroke-width="2"/></svg><input id="cur-pass" type="password" v-model="currentPassword" placeholder="请输入当前密码" /></div>
            <span class="error-message" v-if="passwordErrors.currentPassword">{{ passwordErrors.currentPassword }}</span>
          </div>
          <div class="form-item">
            <label for="new-pass">新密码</label>
            <div class="input-wrapper" :class="{error: passwordErrors.newPassword}"><svg class="input-svg" width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="5" y="10" width="14" height="9" rx="2" stroke="currentColor" stroke-width="2"/><path d="M8 10V8a4 4 0 0 1 8 0v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg><input id="new-pass" type="password" v-model="newPassword" placeholder="请输入新密码" /></div>
            <span class="error-message" v-if="passwordErrors.newPassword">{{ passwordErrors.newPassword }}</span>
          </div>
          <div class="form-item">
            <label for="conf-pass">确认新密码</label>
            <div class="input-wrapper" :class="{error: passwordErrors.confirmPassword}"><svg class="input-svg" width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="5" y="10" width="14" height="9" rx="2" stroke="currentColor" stroke-width="2"/><path d="M8 10V8a4 4 0 0 1 8 0v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg><input id="conf-pass" type="password" v-model="confirmPassword" placeholder="请再次输入新密码" /></div>
            <span class="error-message" v-if="passwordErrors.confirmPassword">{{ passwordErrors.confirmPassword }}</span>
          </div>
          <div class="password-rules"><h3>密码要求：</h3><ul><li :class="{valid: passwordRules.length}">长度至少8位</li><li :class="{valid: passwordRules.allowedChars}">只能包含字母、数字或下划线</li></ul></div>
          <button class="submit-btn" @click="handlePasswordChange" :disabled="isSubmittingProfile">{{ isSubmittingProfile ? '提交中...' : '确认修改' }}</button>
        </div>
        <div class="profile-body" v-else-if="activeProfileTab==='email'">
          <div class="sub-header" @click="activeProfileTab='menu'">‹ 返回</div>
          <div class="form-item"><label for="email-input">邮箱地址</label><div class="input-wrapper" :class="{error: emailErrors.email}"><svg class="input-svg" width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M4 6h16v12H4z" stroke="currentColor" stroke-width="2"/><path d="M4 8l8 5 8-5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg><input id="email-input" type="email" v-model="email" placeholder="请输入邮箱地址" /></div><span class="error-message" v-if="emailErrors.email">{{ emailErrors.email }}</span></div>
          <button class="submit-btn" @click="handleEmailBinding" :disabled="isSubmittingProfile">{{ isSubmittingProfile ? '提交中...' : '确认绑定' }}</button>
        </div>
        <div class="profile-body" v-else-if="activeProfileTab==='phone'">
          <div class="sub-header" @click="activeProfileTab='menu'">‹ 返回</div>
          <div class="form-item"><label for="phone-input">手机号码</label><div class="input-wrapper" :class="{error: phoneErrors.phone}"><svg class="input-svg" width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="6" y="4" width="12" height="16" rx="2" stroke="currentColor" stroke-width="2"/><circle cx="12" cy="18" r="1" fill="currentColor"/><path d="M9 7h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg><input id="phone-input" type="tel" v-model="phone" placeholder="请输入手机号码" /></div><span class="error-message" v-if="phoneErrors.phone">{{ phoneErrors.phone }}</span></div>
          <button class="submit-btn" @click="handlePhoneBinding" :disabled="isSubmittingProfile">{{ isSubmittingProfile ? '提交中...' : '确认绑定' }}</button>
        </div>
      </div>
    </div>

    <!-- 关于我们弹窗 -->
    <div class="about-dialog" 
      v-if="showAbout" 
      @click.self="closeAboutDialog"
      role="dialog"
      aria-labelledby="about-title"
      aria-modal="true"
    >
      <div class="about-content">
        <div class="about-header">
          <h2 id="about-title">关于我们</h2>
          <span class="close-btn" @click="closeAboutDialog">×</span>
        </div>
        <div class="about-body">
          <div class="about-section">
            <h3>梦之队 · 智能面试助手</h3>
            <div class="team-description">
              我们专注于多模态面试评估与训练，结合语音、文本与表情识别，提供真实、可量化的面试体验与成长路径。
            </div>
          </div>

          <div class="about-section">
            <h3>核心功能</h3>
            <div class="feature-list">
              <div class="feature-item">
                <span class="feature-icon">🎯</span>
                <div class="feature-content">
                  <h4>智能模拟面试</h4>
                  <p>还原真实面试语境，支持算法/前端/后端等多岗位题库与场景。</p>
                </div>
              </div>
              <div class="feature-item">
                <span class="feature-icon">📝</span>
                <div class="feature-content">
                  <h4>实时反馈分析</h4>
                  <p>从专业能力、表达清晰度、情绪稳定性等维度给出即时评分与建议。</p>
                </div>
              </div>
              <div class="feature-item">
                <span class="feature-icon">📊</span>
                <div class="feature-content">
                  <h4>个性化报告</h4>
                  <p>生成结构化报告，定位强弱项，输出提升路线与练习清单。</p>
                </div>
              </div>
              <div class="feature-item">
                <span class="feature-icon">🔄</span>
                <div class="feature-content">
                  <h4>持续学习</h4>
                  <p>通过 AIview 对话沉淀面试过程与成长轨迹，结合周/月榜激励持续练习。</p>
                </div>
              </div>
              <div class="feature-item">
                <span class="feature-icon">🎨</span>
                <div class="feature-content">
                  <h4>多场景模拟</h4>
                  <p>支持技术面、HR面、群面等形式，覆盖线上与线下典型环节。</p>
                </div>
              </div>
              <div class="feature-item">
                <span class="feature-icon">📱</span>
                <div class="feature-content">
                  <h4>便捷操作</h4>
                  <p>简洁直观的界面与护眼模式，移动端体验友好。</p>
                </div>
              </div>
            </div>
          </div>

          <div class="about-section">
            <h3>项目进展</h3>
            <div class="status-content">
              <p>核心模块已上线：AI 面试、试题作答、场景评测与排行榜。正在迭代优化体验，后续将开放更多岗位题库与个性化训练。</p>
            </div>
          </div>

          <div class="about-section">
            <h3>联系我们</h3>
            <div class="contact-content">
              <div class="contact-item">
                <span class="contact-icon">📧</span>
                <div class="contact-info">
                  <h4>邮箱</h4>
                  <p>2849883090@qq.com</p>
                </div>
              </div>
            </div>
          </div>

          <div class="about-footer">
            <p class="version">当前版本：v1.0.0</p>
            <p class="copyright">© 2025 梦之队. All rights reserved.</p>
          </div>
        </div>
      </div>
    </div>


    <!-- 意见反馈弹窗 -->
    <div class="feedback-dialog" 
      v-if="showFeedback" 
      @click.self="closeFeedbackDialog"
      role="dialog"
      aria-labelledby="feedback-title"
      aria-modal="true"
    >
      <div class="feedback-content">
        <div class="feedback-header">
          <h2 id="feedback-title">意见反馈</h2>
          <span class="close-btn" @click="closeFeedbackDialog" aria-label="关闭">×</span>
        </div>
        <div class="feedback-body">
          <div class="feedback-type" role="radiogroup" aria-label="反馈类型">
            <div 
              v-for="type in feedbackTypes" 
              :key="type.id"
              class="type-item"
              :class="{ 
                'active': selectedType === type.id,
                'error': formErrors.type && !selectedType 
              }"
              @click="selectedType = type.id"
              @keydown.space.prevent="selectedType = type.id"
              :tabindex="0"
              role="radio"
              :aria-checked="selectedType === type.id"
            >
              <span class="type-icon" aria-hidden="true">{{ type.icon }}</span>
              <span class="type-text">{{ type.text }}</span>
            </div>
          </div>
          <div class="error-message" v-if="formErrors.type" role="alert">{{ formErrors.type }}</div>
          <div class="input-wrapper" :class="{ 'error': formErrors.title }">
            <label for="feedback-title-input" class="sr-only">反馈标题</label>
            <input
              id="feedback-title-input"
              v-model="feedbackTitle"
              placeholder="请输入反馈标题"
              maxlength="100"
              class="feedback-title-input"
              @focus="isInputFocused = true"
              @blur="isInputFocused = false"
              aria-label="反馈标题"
              :aria-invalid="!!formErrors.title"
              :aria-errormessage="formErrors.title ? 'title-error' : undefined"
            />
            <div 
              id="title-error"
              class="error-message" 
              v-if="formErrors.title"
              role="alert"
            >{{ formErrors.title }}</div>
          </div>

          <div class="input-wrapper" :class="{ 'error': formErrors.content }">
            <label for="feedback-input" class="sr-only">反馈内容</label>
            <textarea
              id="feedback-input"
              v-model="feedbackContent"
              :placeholder="getPlaceholder"
              maxlength="500"
              class="feedback-input"
              @focus="isInputFocused = true"
              @blur="isInputFocused = false"
              aria-label="反馈内容"
              :aria-invalid="!!formErrors.content"
              :aria-errormessage="formErrors.content ? 'content-error' : undefined"
            ></textarea>
            <div 
              id="content-error"
              class="error-message" 
              v-if="formErrors.content"
              role="alert"
            >{{ formErrors.content }}</div>
            <div class="input-footer">
              <div 
                class="char-count" 
                :class="{ 'warning': feedbackContent.length >= 450 }"
                aria-live="polite"
              >
                {{ feedbackContent.length }}/500
              </div>
              <div class="submit-wrapper">
                <button 
                  class="submit-btn" 
                  @click="submitFeedback"
                  :disabled="isSubmitting"
                  aria-label="提交反馈"
                >
                  <span class="submit-text">{{ isSubmitting ? '提交中...' : '提交反馈' }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted, computed, watch } from 'vue';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
import { API } from '@/utils/api/pages/personal-center';
import { getUserSession, setUserSessionPatch, subscribeUserSession, getSessionToken } from '@/utils/user-session';
import { applyAvatarFallback, resolveUserAvatar } from '@/utils/avatar';
import request from '@/utils/api/request';

const showModal = ref(false);
const userAvatar = ref(resolveUserAvatar(getUserSession()));
const nickname = ref('');
const isEditingNickname = ref(false);
const editingNickname = ref('');
const nicknameInput = ref(null);
const showAbout = ref(false);
const showProfileModal = ref(false);
const activeProfileTab = ref('menu');
const username = ref('');
const registerTime = ref('');
const identityText = ref('');
const emailStatus = ref({ text: '未绑定', class: 'status-unbind' });
const phoneStatus = ref({ text: '未绑定', class: 'status-unbind' });
const showFeedback = ref(false);
const feedbackContent = ref('');
const selectedType = ref('');
const isInputFocused = ref(false);
const isSubmitting = ref(false);
const feedbackTitle = ref('');
const formErrors = ref({
  type: '',
  title: '',
  content: ''
});

const feedbackTypes = [
  { id: 'bug', icon: '🐛', text: '问题反馈' },
  { id: 'suggestion', icon: '💡', text: '功能建议' },
  { id: 'experience', icon: '🌟', text: '体验问题' },
  { id: 'other', icon: '💭', text: '其他' }
];

const getPlaceholder = computed(() => {
  const type = feedbackTypes.find(t => t.id === selectedType.value);
  switch (type?.id) {
    case 'bug':
      return '请描述您遇到的问题，包括问题发生的步骤和现象...';
    case 'suggestion':
      return '请描述您的功能建议，我们会认真考虑...';
    case 'experience':
      return '请描述您使用过程中遇到的体验问题...';
    default:
      return '请输入您的意见或建议...';
  }
});

const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);

// 检查是否为管理员
const isAdmin = ref(false);

// 处理头像加载错误
const handleAvatarError = (event) => {
  applyAvatarFallback(event, getUserSession());
};

// 从本地存储获取用户信息
const getUserInfoFromStorage = () => {
  try {
    const parsedInfo = getUserSession();
    if (parsedInfo && Object.keys(parsedInfo).length) {
      // nickname为空或全是空格时显示'未命名用户'
      let nick = parsedInfo.nickname || parsedInfo.username || '';
      if (!nick || String(nick).trim() === '') {
        nick = '未命名用户';
      }
      nickname.value = nick;
      userAvatar.value = resolveUserAvatar(parsedInfo);
      
      // 检查是否为管理员 - 支持多种字段名
      const userType = parsedInfo.userType || parsedInfo.user_type || parsedInfo.type;
      isAdmin.value = userType === 3 || userType === '3';
    } else {
      userAvatar.value = resolveUserAvatar();
      nickname.value = '未命名用户';
      isAdmin.value = false;
    }
  } catch (error) {
    userAvatar.value = resolveUserAvatar();
    nickname.value = '未命名用户';
    isAdmin.value = false;
    console.error('获取用户信息失败:', error);
  }
};

// Show full image
const showFullImage = () => {
  showModal.value = true;
};

const hideFullImage = () => {
  showModal.value = false;
};

// Avatar upload handling
const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0];
      uploadAvatarFile(tempFilePath);
    },
    fail: (err) => {
      console.error('选择图片失败:', err);
      uni.showToast({
        title: '选择图片失败',
        icon: 'error'
      });
    }
  });
};

// 上传头像文件（服务端保存为可访问 URL），并更新用户头像
const uploadAvatarFile = async (filePath) => {
  try {
    const token = getSessionToken();
    if (!token) {
      throw new Error('请先登录');
    }

  uni.showLoading({
      title: '更新中...'
  });

    const uploadRes = await uni.uploadFile({
      url: `${API.USER.AVATAR}/upload`,
      filePath,
      name: 'file',
      header: { 'Authorization': `Bearer ${token}` }
    });

    const body = typeof uploadRes.data === 'string' ? JSON.parse(uploadRes.data) : uploadRes.data;
    if (uploadRes.statusCode === 200 && body && body.code === 200) {
      const avatarUrl = body?.data?.avatarUrl;
      if (!avatarUrl) {
        throw new Error('服务端未返回头像地址');
      }
      setUserSessionPatch({ avatar: avatarUrl });
      // 同步刷新头像
      getUserInfoFromStorage();
        uni.showToast({
          title: '头像更新成功',
          icon: 'success'
        });
      } else {
      throw new Error(body?.message || '更新失败');
      }
  } catch (error) {
    console.error('更新头像失败:', error);
      uni.showToast({
      title: error.message || '更新失败，请重试',
      icon: 'none'
      });
  } finally {
      uni.hideLoading();
    }
};

// 开始编辑昵称
const startEditingNickname = () => {
  editingNickname.value = nickname.value;
  isEditingNickname.value = true;
  // 使用 nextTick 确保 DOM 更新后再获取元素
  nextTick(() => {
    const input = document.querySelector('.nickname-input');
    if (input) {
      input.focus();
    }
  });
};

// 保存昵称
const saveNickname = async () => {
  // 基本验证
  const newNickname = editingNickname.value.trim();
  
  // 验证昵称是否为空
  if (!newNickname) {
    uni.showToast({
      title: '昵称不能为空',
      icon: 'none',
      duration: 2000
    });
    return;
  }

  // 验证昵称长度
  if (newNickname.length < 1 || newNickname.length > 6) {
    uni.showToast({
      title: '昵称长度必须在1-6个字符之间',
      icon: 'none',
      duration: 2000
    });
    return;
  }

  // 验证昵称格式（只允许字母或汉字）
  if (!/^[\u4e00-\u9fa5a-zA-Z]+$/.test(newNickname)) {
    uni.showToast({
      title: '昵称只能包含字母或汉字',
      icon: 'none',
      duration: 2000
    });
    return;
  }

  // 验证敏感词（简单示例）
  const sensitiveWords = ['admin', 'root', 'system', '管理员', '超级管理员'];
  if (sensitiveWords.some(word => newNickname.toLowerCase().includes(word))) {
    uni.showToast({
      title: '昵称包含敏感词',
      icon: 'none',
      duration: 2000
    });
    return;
  }

  try {
    const token = getSessionToken();
    if (!token) {
      uni.showToast({
        title: '请先登录',
        icon: 'none',
        duration: 2000
      });
      // 可以在这里添加跳转到登录页面的逻辑
      return;
    }

    uni.showLoading({
      title: '保存中...',
      mask: true
    });

    const response = await request({
      url: API.USER.NICKNAME,
      method: 'PUT',
      data: {
        nickname: newNickname
      }
    });

    const { code, data, message } = response;

    if (code === 200) {
      setUserSessionPatch({ nickname: newNickname, updateTime: data?.updateTime });
      try { userStore.setUserInfo(getUserSession()); } catch {}
      
      // 更新显示的昵称
      nickname.value = newNickname;
      
        uni.showToast({
        title: '昵称修改成功',
        icon: 'success',
        duration: 2000
        });
    } else {
      throw new Error(message || '修改失败');
    }
  } catch (error) {
    console.error('保存昵称失败:', error);
    uni.showToast({
      title: error.message || '保存失败，请重试',
      icon: 'none',
      duration: 2000
    });
  } finally {
    uni.hideLoading();
    isEditingNickname.value = false;
  }
};

// Navigation

const goToProfile = () => {
  showProfileModal.value = true;
  activeProfileTab.value = 'menu';
};

const closeProfileModal = () => {
  showProfileModal.value = false;
  activeProfileTab.value = 'menu';
};

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('token');
        uni.removeStorageSync('userInfo');
        try {
          window.sessionStorage.removeItem('token');
          window.sessionStorage.removeItem('userInfo');
          window.sessionStorage.removeItem('activeUserId');
        } catch {}
        uni.reLaunch({ url: '/pages/login/index' });
      }
    }
  });
};

const goToAdminCenter = () => {
  uni.navigateTo({
    url: '/pages/admin/index'
  });
};



const goBack = () => {
  uni.reLaunch({
    url: '/pages/home/index'
  });
};

// 显示关于我们弹窗
const showAboutDialog = () => {
  showAbout.value = true;
};

// 关闭关于我们弹窗
const closeAboutDialog = () => {
  showAbout.value = false;
};

// 组件挂载时获取用户信息和初始化护眼模式
onMounted(() => {
  getUserInfoFromStorage();
  window.addEventListener('keydown', handleKeydown);
  fetchUserProfile();
  try { unSubUserInfo = subscribeUserSession(() => { getUserInfoFromStorage(); }) } catch {}
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown);
  try { if (unSubUserInfo) unSubUserInfo(); } catch {}
});

// 帮助中心模块已移除

const passwordErrors = ref({ currentPassword: '', newPassword: '', confirmPassword: '' });
const emailErrors = ref({ email: '' });
const phoneErrors = ref({ phone: '' });
const currentPassword = ref('');
const newPassword = ref('');
const confirmPassword = ref('');
const email = ref('');
const phone = ref('');
const isSubmittingProfile = ref(false);
const passwordRules = ref({ length: false, allowedChars: false });
let unSubUserInfo = null;

watch(newPassword, (value) => {
  passwordRules.value = {
    length: value.length >= 8,
    allowedChars: /^[a-zA-Z0-9_]*$/.test(value)
  };
});

const validatePassword = () => {
  const errs = { currentPassword: '', newPassword: '', confirmPassword: '' };
  let ok = true;
  if (!currentPassword.value) { errs.currentPassword = '请输入当前密码'; ok = false; }
  if (!newPassword.value) { errs.newPassword = '请输入新密码'; ok = false; }
  else if (newPassword.value.length < 8) { errs.newPassword = '密码长度至少8位'; ok = false; }
  else if (!/^[a-zA-Z0-9_]{8,}$/.test(newPassword.value)) { errs.newPassword = '密码必须大于等于8位，只能包含字母、数字或下划线'; ok = false; }
  if (!confirmPassword.value) { errs.confirmPassword = '请确认新密码'; ok = false; }
  else if (confirmPassword.value !== newPassword.value) { errs.confirmPassword = '两次输入的密码不一致'; ok = false; }
  passwordErrors.value = errs;
  return ok;
};

const validateEmail = (val) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val);
const validatePhone = (val) => /^1[3-9]\d{9}$/.test(val);

const handlePasswordChange = async () => {
  if (!validatePassword()) return;
  try {
    isSubmittingProfile.value = true;
    const token = getSessionToken();
    if (!token) { uni.showToast({ title: '请先登录', icon: 'none' }); return; }
    const response = await request({
      url: API.USER.CHANGE_PASSWORD,
      method: 'PUT',
      data: { oldPassword: currentPassword.value, newPassword: newPassword.value }
    });
    if (response.code === 200) {
      uni.showToast({ title: '密码修改成功', icon: 'success' });
      activeProfileTab.value = 'menu';
      currentPassword.value = newPassword.value = confirmPassword.value = '';
    } else { throw new Error(response.message || '修改失败'); }
  } catch (e) {
    uni.showToast({ title: e.message || '修改失败，请重试', icon: 'none' });
  } finally { isSubmittingProfile.value = false; }
};

const handleEmailBinding = async () => {
  emailErrors.value = { email: '' };
  if (!email.value) { emailErrors.value.email = '请输入邮箱地址'; return; }
  if (!validateEmail(email.value)) { emailErrors.value.email = '请输入有效的邮箱地址'; return; }
  try {
    isSubmittingProfile.value = true;
    const token = getSessionToken();
    if (!token) { uni.showToast({ title: '请先登录', icon: 'none' }); return; }
    const response = await request({
      url: API.USER.BIND_EMAIL,
      method: 'PUT',
      data: { email: email.value }
    });
    if (response.code === 200) {
      uni.showToast({ title: '邮箱绑定成功', icon: 'success' });
      setUserSessionPatch({ email: email.value });
      emailStatus.value = { text: '已绑定', class: 'status-bind' };
      activeProfileTab.value = 'menu';
    } else { throw new Error(response.message || '绑定失败'); }
  } catch (e) {
    uni.showToast({ title: e.message || '绑定失败，请重试', icon: 'none' });
  } finally { isSubmittingProfile.value = false; }
};

const handlePhoneBinding = async () => {
  phoneErrors.value = { phone: '' };
  if (!phone.value) { phoneErrors.value.phone = '请输入手机号码'; return; }
  if (!validatePhone(phone.value)) { phoneErrors.value.phone = '请输入有效的手机号码'; return; }
  try {
    isSubmittingProfile.value = true;
    const token = getSessionToken();
    if (!token) { uni.showToast({ title: '请先登录', icon: 'none' }); return; }
    const response = await request({
      url: API.USER.BIND_PHONE,
      method: 'PUT',
      data: { phone: phone.value }
    });
    if (response.code === 200) {
      uni.showToast({ title: '手机号绑定成功', icon: 'success' });
      setUserSessionPatch({ phone: phone.value });
      phoneStatus.value = { text: '已绑定', class: 'status-bind' };
      activeProfileTab.value = 'menu';
    } else { throw new Error(response.message || '绑定失败'); }
  } catch (e) {
    uni.showToast({ title: e.message || '绑定失败，请重试', icon: 'none' });
  } finally { isSubmittingProfile.value = false; }
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const d = new Date(dateString);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}年${m}月${day}日`;
};

const fetchUserProfile = async () => {
  try {
    const token = getSessionToken();
    if (!token) return;
    const response = await request({ url: API.USER.PROFILE, method: 'GET' });
    if (response.code === 200) {
      const userData = response.data;
      username.value = userData.username || username.value;
      registerTime.value = formatDate(userData.createdAt) || registerTime.value;
      identityText.value = (function(ut){ const n = Number(ut); if (n===3) return '管理员'; if (n===2) return '会员'; if (n===1) return '普通用户'; return '未知'; })(userData.userType);
      emailStatus.value = userData.email ? { text:'已绑定', class:'status-bind' } : { text:'未绑定', class:'status-unbind' };
      phoneStatus.value = userData.phone ? { text:'已绑定', class:'status-bind' } : { text:'未绑定', class:'status-unbind' };
      const nick = (userData.nickname && String(userData.nickname).trim() !== '') ? userData.nickname : '未命名用户';
      nickname.value = nick;
      try {
        const updated = setUserSessionPatch(userData || {});
        try { userStore.setUserInfo(updated); } catch {}
      } catch {}
    }
  } catch {}
};

const showFeedbackDialog = () => {
  const token = getSessionToken();
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none', duration: 2000 });
    return;
  }
  showFeedback.value = true;
};

const closeFeedbackDialog = () => {
  showFeedback.value = false;
  selectedType.value = '';
  feedbackTitle.value = '';
  feedbackContent.value = '';
  formErrors.value = { type: '', title: '', content: '' };
};

const handleKeydown = (event) => {
  if (event.key === 'Escape') closeFeedbackDialog();
};

const submitFeedback = async () => {
  formErrors.value = { type: '', title: '', content: '' };
  const token = getSessionToken();
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none', duration: 2000 });
    return;
  }
  if (!selectedType.value) { formErrors.value.type = '请选择反馈类型'; return; }
  if (!feedbackTitle.value.trim()) { formErrors.value.title = '请输入反馈标题'; return; }
  if (feedbackTitle.value.trim().length > 100) { formErrors.value.title = '标题长度不能超过100个字符'; return; }
  if (!feedbackContent.value.trim()) { formErrors.value.content = '请输入反馈内容'; return; }
  if (feedbackContent.value.trim().length > 500) { formErrors.value.content = '内容长度不能超过500个字符'; return; }

  try {
    isSubmitting.value = true;
    const response = await fetch(API.FEEDBACK.SUBMIT, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        type: selectedType.value,
        title: feedbackTitle.value.trim(),
        content: feedbackContent.value.trim()
      })
    });
    const result = await response.json();
    if (result.code === 200) {
      uni.showToast({ title: '感谢您的反馈！', icon: 'success', duration: 2000 });
      closeFeedbackDialog();
    } else {
      uni.showToast({ title: result.message || '提交失败，请稍后重试', icon: 'none', duration: 2000 });
    }
  } catch (error) {
    console.error('提交反馈失败:', error);
    uni.showToast({ title: '网络错误，请稍后重试', icon: 'none', duration: 2000 });
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
.centered-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  width: 100%;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  transition: background-color 0.3s ease;
}

/* 使用更高的优先级确保护眼模式背景色生效 */
.centered-container.eye-care {
  background: #f0f2e6 !important;
  background-image: none !important;
}

/* 气泡容器 */
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

.personal-center-container {
  padding: 30px 20px;
  max-width: 600px;
  width: 90%;
  font-family: 'Arial', sans-serif;
  color: #333;
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  margin-top: 0;
  position: relative;
  z-index: 1;
  transition: box-shadow 0.3s ease;
  text-align: center;
}

.personal-center-container:hover {
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
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

.page-title {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 1.8em;
  color: #333;
  text-align: center;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
  display: inline-block;
}

.user-avatar-section {
  margin-bottom: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  transition: transform 0.3s ease;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
}

.user-avatar {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  cursor: pointer;
}

.avatar-wrapper:hover .upload-overlay {
  opacity: 1;
}

.upload-icon {
  font-size: 2em;
  transform: translateY(20px);
  transition: transform 0.3s ease;
}

.avatar-wrapper:hover .upload-icon {
  transform: translateY(0);
}

.nickname-section {
  margin-top: 15px;
  text-align: center;
}

.nickname-display {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: color 0.3s ease;
}

.nickname-display:hover {
  color: #007AFF;
}

.nickname {
  font-size: 1.2em;
  font-weight: 600;
}

.edit-hint {
  font-size: 0.8em;
  color: #999;
}

.nickname-edit {
  display: inline-block;
}

.nickname-input {
  border: 1px solid #ccc;
  border-radius: 4px;
  padding: 5px 10px;
  font-size: 1em;
  text-align: center;
}

.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.full-size-image {
  max-width: 90%;
  max-height: 90%;
  border-radius: 8px;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
}

.menu-list {
  list-style: none;
  padding: 0;
  margin: 0;
  width: 100%;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  margin: 5px 0;
  position: relative;
  overflow: hidden;
}

.menu-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(45deg, transparent, rgba(0, 123, 255, 0.05));
  transform: translateX(-100%);
  transition: transform 0.3s ease;
}

.menu-item:hover::before {
  transform: translateX(0);
}

.menu-item:hover {
  background-color: rgba(0, 123, 255, 0.02);
  transform: translateX(5px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.admin-item {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.1), rgba(255, 193, 7, 0.1));
  border: 2px solid transparent;
}

.admin-item:hover {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.2), rgba(255, 193, 7, 0.2));
  border-color: #ffc107;
}



.menu-item:active {
  transform: scale(0.98);
}

.menu-item-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.menu-svg {
  color: #333;
  transition: color 0.3s ease, filter 0.2s ease, transform 0.2s ease;
}

.menu-item:hover .menu-svg {
  color: #4A6FFF;
  filter: drop-shadow(0 1px 2px rgba(74, 111, 255, 0.25));
}

.warning .menu-svg {
  color: #ff4d4f;
}

.menu-icon {
  font-size: 1.4em;
  transition: transform 0.3s ease;
}

.menu-item:hover .menu-icon {
  transform: scale(1.1);
}

.input-svg {
  color: #666;
  min-width: 18px;
  margin-right: 8px;
  transition: color 0.3s ease, filter 0.2s ease;
}

.input-wrapper:focus-within .input-svg {
  color: #4A6FFF;
  filter: drop-shadow(0 1px 2px rgba(74, 111, 255, 0.25));
}

.arrow {
  font-size: 1.1em;
  color: #999;
  transition: transform 0.3s ease, color 0.3s ease;
}

.menu-item:hover .arrow {
  transform: translateX(3px);
  color: #666;
}

.warning {
  color: #ff4d4f;
}

.warning:hover {
  background-color: rgba(255, 77, 79, 0.05);
}

.warning:hover::before {
  background: linear-gradient(45deg, transparent, rgba(255, 77, 79, 0.05));
}

@media screen and (max-width: 480px) {
  .personal-center-container {
    padding: 20px 15px;
    width: 95%;
    margin-top: -80px;
  }

  .page-title {
    font-size: 1.5em;
    margin-bottom: 30px;
  }

  .user-avatar-section {
    margin-bottom: 30px;
  }

  .avatar-wrapper {
    width: 80px;
    height: 80px;
  }

  .upload-icon {
    font-size: 1.5em;
  }

  .nickname {
    font-size: 1em;
  }

  .edit-hint {
    font-size: 0.7em;
  }

  .menu-item {
    padding: 12px 15px;
    margin: 3px 0;
  }

  .menu-item-left {
    gap: 10px;
  }

  .menu-icon {
    font-size: 1.2em;
  }

  .arrow {
    font-size: 1em;
  }
}

/* 关于我们弹窗样式 */
.about-dialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
  backdrop-filter: blur(4px);
  animation: fadeIn 0.3s ease;
}

.about-content {
  width: 90%;
  max-width: 600px;
  background-color: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease;
}

.about-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  background: linear-gradient(to right, #4A6FFF, #6B8CFF);
  text-align: center;
  position: relative;
}

.about-header h2 {
  margin: 0;
  font-size: 1.4em;
  color: #fff;
  font-weight: 500;
}

.close-btn {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  color: #fff;
  font-size: 24px;
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.3s ease;
}

.close-btn:hover {
  opacity: 1;
}

.about-body {
  padding: 24px;
  line-height: 1.6;
  color: #333;
  max-height: 70vh;
  overflow-y: auto;
}

.about-section {
  margin-bottom: 32px;
}

.about-section h3 {
  font-size: 1.2em;
  color: #4A6FFF;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #4A6FFF;
}

.team-description {
  font-size: 1.1em;
  color: #666;
  line-height: 1.6;
  margin-bottom: 24px;
}

.feature-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 12px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.feature-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 24px;
}

.feature-content h4 {
  margin: 0 0 8px;
  color: #333;
}

.feature-content p {
  margin: 0;
  color: #666;
  font-size: 0.9em;
}

.status-content {
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 12px;
  color: #666;
}

.contact-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 12px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.contact-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.contact-icon {
  font-size: 24px;
}

.contact-info h4 {
  margin: 0 0 4px;
  color: #333;
  font-size: 1em;
}

.contact-info p {
  margin: 0;
  color: #4A6FFF;
  font-size: 0.9em;
  font-weight: 500;
}

.about-footer {
  margin-top: 32px;
  padding-top: 16px;
  border-top: 1px solid #eee;
  text-align: center;
  color: #999;
  font-size: 0.9em;
}

.version {
  margin-bottom: 8px;
}

  .copyright {
    margin: 0;
  }

/* 个人资料弹窗样式 */
.profile-dialog { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.6); display: flex; justify-content: center; align-items: center; z-index: 100; backdrop-filter: blur(4px); animation: fadeIn 0.3s ease; }
.profile-content { width: 90%; max-width: 600px; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 15px 40px rgba(0,0,0,0.15); animation: slideUp 0.3s ease; }
.profile-header { padding: 20px; border-bottom: 1px solid #eee; background: linear-gradient(to right, #4A6FFF, #6B8CFF); text-align: center; position: relative; }
.profile-header h2 { margin: 0; font-size: 1.4em; color: #fff; font-weight: 500; }
.profile-body { padding: 20px; }
.sub-header { color: #4A6FFF; cursor: pointer; margin-bottom: 12px; text-align: left; }
.info-card { background-color: #f8f9fa; border-radius: 12px; padding: 12px 16px; margin-bottom: 16px; }
.info-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #eee; }
.info-row:last-child { border-bottom: none; }
.label { color: #666; }
.value { color: #333; }

/* 护眼模式适配 */
.centered-container.eye-care .profile-dialog { background-color: rgba(0,0,0,0.35); }
.centered-container.eye-care .profile-content { background-color: #f0f2e6; box-shadow: 0 15px 40px rgba(127,176,105,0.15); }
.centered-container.eye-care .profile-header { background: linear-gradient(to right, #3A5FEF, #4A6FFF); border-color: #e6e6d9; }
.centered-container.eye-care .info-card { background-color: #f8f9e8; }
.centered-container.eye-care .input-wrapper { background: #e8ecd9; border-color: #dfe3d0; }
.centered-container.eye-care .input-wrapper:focus-within { box-shadow: 0 0 0 3px rgba(74,111,255,0.15), inset 0 1px 3px rgba(0,0,0,0.05); background-color: #fff; }
.centered-container.eye-care .submit-btn { background: linear-gradient(90deg, #4A6FFF 0%, #6B8CFF 100%); box-shadow: 0 2px 4px rgba(74,111,255,0.25); }
.centered-container.eye-care .status-bind { background-color: #e6f0ff; color: #4A6FFF; }

.centered-container.eye-care .menu-svg { color: #2f3a21; }
.centered-container.eye-care .menu-item:hover .menu-svg { color: #3A5FEF; filter: drop-shadow(0 1px 2px rgba(58, 95, 239, 0.25)); }
.centered-container.eye-care .input-svg { color: #4a5330; }
.centered-container.eye-care .input-wrapper:focus-within .input-svg { color: #3A5FEF; filter: drop-shadow(0 1px 2px rgba(58, 95, 239, 0.25)); }

/* 帮助中心模块已移除 */

/* 意见反馈样式 */
.feedback-dialog { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.6); display: flex; justify-content: center; align-items: center; z-index: 100; backdrop-filter: blur(4px); animation: fadeIn 0.3s ease; }
.feedback-content { width: 90%; max-width: 500px; background-color: #fff; border-radius: 16px; overflow: hidden; box-shadow: 0 20px 40px rgba(0,0,0,0.2); animation: slideUp 0.3s ease; transform-origin: center; }
.feedback-header { padding: 20px; border-bottom: 1px solid #eee; background: linear-gradient(to right, #4A6FFF, #6B8CFF); text-align: center; position: relative; }
.feedback-header h2 { margin: 0; font-size: 1.2em; color: #fff; font-weight: 500; }
.feedback-body { padding: 20px; }
.feedback-type { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin-bottom: 20px; }
.type-item { padding: 12px 8px; border-radius: 12px; background-color: #f5f5f5; display: flex; flex-direction: column; align-items: center; gap: 8px; cursor: pointer; transition: all 0.3s ease; }
.type-item:hover { transform: translateY(-2px); background-color: #e8f5e9; }
.type-item.active { background-color: #4CAF50; color: white; }
.type-item.error { border: 1px solid #ff4d4f; animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both; }
.type-icon { font-size: 24px; }
.type-text { font-size: 12px; text-align: center; }
.input-wrapper { position: relative; background-color: #f9f9f9; border-radius: 12px; padding: 12px 16px; margin-bottom: 16px; transition: all 0.3s ease; }
.input-wrapper:focus-within { background-color: #fff; box-shadow: 0 0 0 2px #4CAF50; }
.input-wrapper.error { border: 1px solid #ff4d4f; background-color: #fff2f0; }
.feedback-title-input { width: 100%; padding: 0; border: none; background: transparent; font-size: 14px; line-height: 1.5; color: #333; }
.feedback-title-input:focus { outline: none; }
.feedback-input { width: 100%; height: 150px; padding: 0; border: none; background: transparent; resize: none; font-size: 14px; line-height: 1.6; color: #333; }
.feedback-input:focus { outline: none; }
.input-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; padding-top: 12px; border-top: 1px solid #eee; }
.char-count { color: #999; font-size: 12px; transition: color 0.3s ease; }
.char-count.warning { color: #ff4d4f; }
.submit-wrapper { position: relative; overflow: hidden; }
.submit-btn { position: relative; min-width: 120px; height: 40px; padding: 0 24px; border: none; border-radius: 20px; background: linear-gradient(90deg, #4A6FFF 0%, #6B8CFF 100%); color: white; font-size: 14px; font-weight: 500; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); overflow: hidden; box-shadow: 0 2px 4px rgba(74,111,255,0.25); }
.submit-btn:hover { background: linear-gradient(90deg, #3A5FEF 0%, #4A6FFF 100%); transform: translateY(-1px); box-shadow: 0 4px 8px rgba(74,111,255,0.35); }
.submit-btn:active { transform: translateY(1px); box-shadow: 0 1px 2px rgba(74,111,255,0.25); }
.sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip: rect(0, 0, 0, 0); white-space: nowrap; border: 0; }

:deep(.uni-toast) { z-index: 10000 !important; }
:deep(.uni-toast--top) { top: 20px !important; transform: translateX(-50%); }

/* 护眼模式适配 */
.eye-care .about-content {
  background-color: #f8f9e8;
}

.eye-care .about-header {
  background: linear-gradient(to right, #3A5FEF, #4A6FFF);
  border-color: #e6e6d9;
}

.eye-care .feature-item,
.eye-care .status-content,
.eye-care .contact-item {
  background-color: #f0f2e6;
}

.eye-care .avatar-wrapper:hover {
  border-color: #3A5FEF;
}

/* 响应式适配 */
@media screen and (max-width: 480px) {
  .about-content {
    width: 95%;
    margin: 20px;
  }
  
  .about-body {
    padding: 16px;
  }

  .feature-list {
    grid-template-columns: 1fr;
  }

  .feature-item {
    padding: 12px;
  }

  .about-section h3 {
    font-size: 1.1em;
  }

  .team-description {
    font-size: 1em;
  }
}

/* 添加动画 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style> 

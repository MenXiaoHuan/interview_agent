<template>
  <div class="register-cosmic-container">
    <div id="stars"></div>
    <div id="stars2"></div>
    <div id="stars3"></div>
    <div class="bubbles">
      <div v-for="n in 20" :key="n" class="bubble"></div>
    </div>
    <div class="cosmic-content-container">
      <div class="cosmic-logo-area">
        <div class="cosmic-welcome-text">用户注册</div>
        <div class="cosmic-subtitle">请设置您的账号和密码</div>
      </div>
      <div class="cosmic-form-area">
        <!-- 防止自动填充的隐藏输入框 -->
        <input type="text" style="display:none" autocomplete="username">
        <input type="password" style="display:none" autocomplete="new-password">
        <div class="cosmic-form-group">
          <label class="cosmic-form-label">账号</label>
          <div class="cosmic-input-wrapper">
            <span class="cosmic-input-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/>
                <path d="M4 20c1.8-3.2 5-5 8-5s6.2 1.8 8 5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </span>
            <input type="text" class="cosmic-form-input" placeholder="请输入账号（仅限数字和字母，最多16位）" maxlength="16" v-model="registerForm.username" autocomplete="off" />
    </div>
        </div>
        <div class="cosmic-form-group">
          <label class="cosmic-form-label">密码</label>
          <div class="cosmic-input-wrapper">
            <span class="cosmic-input-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="5" y="10" width="14" height="9" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M8 10V8a4 4 0 0 1 8 0v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </span>
            <input type="password" class="cosmic-form-input" placeholder="请设置密码（大小写字母+数字，≥8位）" v-model="registerForm.password" autocomplete="off" />
      </div>
        </div>
        <div class="cosmic-form-group">
          <label class="cosmic-form-label">确认密码</label>
          <div class="cosmic-input-wrapper">
            <span class="cosmic-input-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="5" y="10" width="14" height="9" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M8 10V8a4 4 0 0 1 8 0v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </span>
            <input type="password" class="cosmic-form-input" placeholder="请再次输入密码" v-model="registerForm.confirmPassword" autocomplete="off" />
      </div>
        </div>
        <div class="cosmic-form-group">
          <label class="cosmic-form-label">邮箱（选填）</label>
          <div class="cosmic-input-wrapper">
            <span class="cosmic-input-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M4 6h16v12H4z" stroke="currentColor" stroke-width="2"/>
                <path d="M4 8l8 5 8-5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </span>
            <input type="email" class="cosmic-form-input" placeholder="请输入邮箱（选填）" v-model="registerForm.email" />
      </div>
      </div>
        <div class="cosmic-form-group">
          <label class="cosmic-form-label">手机号（选填）</label>
          <div class="cosmic-input-wrapper">
            <span class="cosmic-input-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M6 4h12v16H6z" stroke="currentColor" stroke-width="2"/>
                <circle cx="12" cy="18" r="1" fill="currentColor"/>
                <path d="M9 6h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </span>
            <input type="tel" class="cosmic-form-input" placeholder="请输入手机号（选填）" v-model="registerForm.phone" />
      </div>
      </div>
      <transition name="fade-slide">
          <div v-if="registerError || usernameError || passwordError || confirmPasswordError" class="cosmic-error-message">
           <div v-if="registerError">{{ registerError }}</div>
           <div v-if="usernameError">{{ usernameError }}</div>
           <div v-if="passwordError">{{ passwordError }}</div>
           <div v-if="confirmPasswordError">{{ confirmPasswordError }}</div>
         </div>
      </transition>
        <button class="cosmic-start-btn" @click="handleRegister">注 册</button>
        <div class="cosmic-other-options">
        <span>已有账号? </span>
          <router-link to="/pages/login/index" class="cosmic-link-text">返回登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { API } from '@/utils/api'

const router = useRouter()
const usernameError = ref('')
const passwordError = ref('')
const confirmPasswordError = ref('')
const registerError = ref('')

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: ''
})

const clearErrors = () => {
  usernameError.value = ''
  passwordError.value = ''
  confirmPasswordError.value = ''
  registerError.value = ''
}

const validateForm = () => {
  clearErrors()
  let isValid = true

  // 验证用户名
  if (!registerForm.username) {
    usernameError.value = '请输入账号'
    isValid = false
  } else if (!/^[a-zA-Z0-9]{6,16}$/.test(registerForm.username)) {
    usernameError.value = '账号长度6-16位，只能包含字母和数字'
    isValid = false
  }

  // 验证密码
  if (!registerForm.password) {
    passwordError.value = '请设置密码'
    isValid = false
  } else if (!/^[a-zA-Z0-9]{8,}$/.test(registerForm.password)) {
    passwordError.value = '密码必须大于等于8位，只能包含大小写字母和数字'
    isValid = false
  }

  // 验证确认密码
  if (!registerForm.confirmPassword) {
    confirmPasswordError.value = '请确认密码'
    isValid = false
  } else if (registerForm.password !== registerForm.confirmPassword) {
    confirmPasswordError.value = '两次输入的密码不一致'
    isValid = false
  }

  // 验证邮箱（选填）
  if (registerForm.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email)) {
    registerError.value = '邮箱格式不正确'
    isValid = false
  }

  // 验证手机号（选填）
  if (registerForm.phone && !/^1\d{10}$/.test(registerForm.phone)) {
    registerError.value = '手机号格式不正确'
    isValid = false
  }

  return isValid
}

const handleRegister = async () => {
  if (!validateForm()) {
    return
  }

  try {
    // 显示加载提示
    uni.showLoading({
      title: '注册中...',
      mask: true // 添加遮罩防止重复点击
    })

    const response = await request.post(API.AUTH.REGISTER, {
      username: registerForm.username,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      email: registerForm.email || undefined,
      phone: registerForm.phone || undefined
    })

    // 对接后端错误响应
    if (response.code !== 200) {
      registerError.value = response.message || '注册失败';
      uni.hideLoading();
      return;
    }

    // 保存token和用户信息
    const { token, userId, username } = response
    uni.setStorageSync('token', token)
    uni.setStorageSync('userInfo', JSON.stringify({
      userId,
      username
    }))

    // 隐藏加载提示
    uni.hideLoading()

    // 显示成功提示
    uni.showToast({
      title: '注册成功',
      icon: 'success',
      duration: 2000
    })

    // 注册成功后跳转到登录页
    setTimeout(() => {
      router.push('/pages/login/index')
    }, 2000)

  } catch (error) {
    registerError.value = error?.response?.data?.message || error.message || '注册失败';
    uni.hideLoading();
    // 错误处理已在请求拦截器中完成
    console.error('注册失败：', error)
  }
}

onMounted(() => {
  registerForm.username = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerForm.email = ''
  registerForm.phone = ''
})
</script>

<style scoped lang="scss">
@use "sass:math";
@use "sass:string";
.register-cosmic-container {
  min-height: 100vh;
  width: 100vw;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0a1929 0%, #1a1f35 100%);
  transition: background 0.4s;
}
// 星空box-shadow星点实现
@function multiple-box-shadow($n) {
  $value: '#{math.random(2560)}px #{math.random(2560)}px #FFF';
  @for $i from 2 through $n {
    $value: '#{$value}, #{math.random(2560)}px #{math.random(2560)}px #FFF';
  }
  @return string.unquote($value);
}
$shadows-small: multiple-box-shadow(1000);
$shadows-medium: multiple-box-shadow(300);
$shadows-large: multiple-box-shadow(150);
#stars, #stars2, #stars3 {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: auto;
  height: auto;
  display: block;
  z-index: 0;
  transform: translateZ(0);
  background: transparent;
  pointer-events: none;
}
#stars {
  width: 1px;
  height: 1px;
  box-shadow: $shadows-small;
  animation: animStar 150s linear infinite;
}
#stars2 {
  width: 2px;
  height: 2px;
  box-shadow: $shadows-medium;
  animation: animStar 100s linear infinite;
}
#stars3 {
  width: 3px;
  height: 3px;
  box-shadow: $shadows-large;
  animation: animStar 50s linear infinite;
}
@keyframes animStar {
  from { transform: translateY(0px); }
  to { transform: translateY(-2560px); }
}
.bubbles { position: absolute; inset: 0; z-index: 2; pointer-events: none; }
.cosmic-content-container {
  margin: -24px auto 0 auto;
  max-width: 380px;
  position: relative;
  z-index: 3;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}
.cosmic-logo-area { text-align: center; margin-bottom: 16px; z-index: 1; }
.cosmic-welcome-text {
  margin-top: 0;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(90deg, #7c4dff 0%, #00e5ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 18px #7c4dff44, 0 0 8px #00e5ff33;
  letter-spacing: 2px;
}
.cosmic-subtitle {
  margin-top: 6px;
  font-size: 14px;
  color: #a8c6ff;
}
.cosmic-form-area {
  width: 100%;
  max-width: 360px;
  background: rgba(20, 24, 40, 0.92);
  border-radius: 20px;
  box-shadow: 0 6px 32px 0 #7c4dff22, 0 1px 4px #00e5ff11;
  border: 1.5px solid rgba(124,77,255,0.18);
  backdrop-filter: blur(16px);
  padding: 28px 24px 24px 24px;
  margin: 0 auto;
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.cosmic-form-group {
  width: 100%;
  margin-bottom: 16px;
}
.cosmic-form-label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  color: #a8c6ff;
  font-weight: 500;
}
.cosmic-input-wrapper {
  display: flex;
  align-items: center;
  background: rgba(30, 40, 70, 0.85);
  border-radius: 8px;
  padding: 0 10px;
  border: 1.5px solid #3a4a6f;
  transition: border-color 0.3s;
}
.cosmic-input-wrapper:focus-within {
  border-color: #7c4dff;
}
.cosmic-input-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  color: #7c4dff;
  margin-right: 6px;
}
.cosmic-form-input {
  flex: 1;
  border: none;
  background: transparent;
  height: 40px;
  font-size: 15px;
  color: #eaf2ff;
  outline: none;
}
.cosmic-form-input::placeholder {
  color: #a8c6ff;
  opacity: 0.7;
}
.cosmic-error-message {
  color: #ff4d4f;
  font-size: 13px;
  margin: 8px 0 0 0;
  animation: shake 0.3s;
}
@keyframes shake {
  0% { transform: translateX(0); }
  20% { transform: translateX(-8px); }
  40% { transform: translateX(8px); }
  60% { transform: translateX(-8px); }
  80% { transform: translateX(8px); }
  100% { transform: translateX(0); }
}
.cosmic-start-btn {
  padding: 8px 40px;
  font-size: 1rem;
  color: #fff;
  background: linear-gradient(90deg, #7c4dff 0%, #00e5ff 100%);
  background-size: 200% 100%;
  background-position: left center;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-weight: bold;
  letter-spacing: 1.5px;
  box-shadow: 0 4px 18px #7c4dff33;
  position: relative;
  overflow: hidden;
  transition: box-shadow 0.4s;
  display: flex;
  align-items: center;
  gap: 6px;
}
.cosmic-start-btn::before {
  content: '';
  position: absolute;
  left: -60%;
  top: 0;
  width: 60%;
  height: 100%;
  background: linear-gradient(120deg, rgba(255,255,255,0.18) 0%, rgba(255,255,255,0.04) 100%);
  filter: blur(2px);
  opacity: 0;
  transition: opacity 0.3s, left 0.5s;
  pointer-events: none;
}
.cosmic-start-btn:hover {
  box-shadow: 0 0 24px 4px #7c4dff88, 0 12px 32px #00e5ff33;
  transform: translateY(-2px);
}
.cosmic-start-btn:hover::before {
  left: 110%;
  opacity: 1;
  transition: opacity 0.3s, left 0.5s;
}
.cosmic-other-options {
  text-align: center;
  font-size: 13px;
  color: #a8c6ff;
  margin-top: 14px;
}
.cosmic-link-text {
  color: #00e5ff;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s;
}
.cosmic-link-text:hover {
  color: #7c4dff;
  text-decoration: underline;
}
@media (max-width: 500px) {
  .cosmic-form-area { padding: 16px 6px 16px 6px; }
  .cosmic-form-input { height: 36px; font-size: 14px; }
  .cosmic-logo { width: 48px; height: 48px; font-size: 22px; }
}
</style> 

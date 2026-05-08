<template>
  <div class="login-cosmic-container">
    <div class="login-back" @click="router.replace('/pages/landing/index')" aria-label="返回">
      <svg class="login-back-ico" width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M15 6l-6 6 6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        <path d="M21 12H9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
    </div>
    <div id="stars"></div>
    <div id="stars2"></div>
    <div id="stars3"></div>
    <div class="bubbles">
      <div v-for="n in 20" :key="n" class="bubble"></div>
    </div>
    <div v-if="isTransitioning" class="page-transition-overlay"></div>
    <div class="cosmic-content-container">
      <div class="cosmic-logo-area">
        <div class="cosmic-welcome-text">欢迎登录</div>
        <div class="cosmic-subtitle">请使用您的账号密码登录系统</div>
      </div>
      <div class="cosmic-form-area">
        <!-- 隐藏占位输入框：尽量削弱浏览器对登录表单的自动填充识别 -->
        <input type="text" style="display:none" tabindex="-1" autocomplete="off">
        <input type="password" style="display:none" tabindex="-1" autocomplete="new-password">
        <div class="cosmic-form-group">
          <label class="cosmic-form-label">账号</label>
          <div class="cosmic-input-wrapper">
            <span class="cosmic-input-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/>
                <path d="M4 20c1.8-3.2 5-5 8-5s6.2 1.8 8 5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </span>
            <input
              type="text"
              class="cosmic-form-input"
              placeholder="请输入您的账号"
              maxlength="16"
              v-model="loginForm.username"
              autocomplete="off"
              autocapitalize="off"
              autocorrect="off"
              spellcheck="false"
              name="no-autofill-user-123"
              :readonly="readonlyUser"
              @focus="readonlyUser = false"
              @keydown.enter.prevent="unlockPasswordInput"
            />
          </div>
        </div>
        <div class="cosmic-form-group">
          <label class="cosmic-form-label">密码</label>
          <div class="cosmic-input-wrapper">
            <span class="cosmic-input-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="5" y="10" width="14" height="9" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M8 10V8a4 4 0 0 1 8 0v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </span>
            <input
              type="password"
              class="cosmic-form-input"
              placeholder="请输入您的密码"
              v-model="loginForm.password"
              autocomplete="new-password"
              name="no-autofill-pass-456"
              :readonly="readonlyPass"
              @focus="readonlyPass = false"
              @keydown.enter.prevent="handleLogin"
            />
          </div>
        </div>
        <div class="cosmic-info-row">
          <transition name="fade-slide">
            <div v-if="loginError" class="cosmic-error-message cosmic-error-inline">{{ loginError }}</div>
            <div v-else class="cosmic-error-message cosmic-error-inline" style="visibility:hidden">占位</div>
          </transition>
          <a href="#" class="cosmic-forgot-password" @click.prevent="openForgotModal">忘记密码?</a>
        </div>
        <button class="cosmic-start-btn" @click="handleLogin">登 录</button>
        <div class="cosmic-other-options">
          <span>还没有账号? </span>
          <router-link to="/pages/register/index" class="cosmic-link-text">立即注册</router-link>
        </div>
      </div>
      <div v-if="forgotVisible" class="fp-overlay">
        <div class="fp-card">
          <div class="fp-header">
            <div class="fp-title">找回密码</div>
            <div class="fp-seg">
              <button :class="{active: forgotChannel==='email'}" @click="forgotChannel='email'">邮箱</button>
              <button :class="{active: forgotChannel==='phone'}" @click="forgotChannel='phone'">手机号</button>
            </div>
          </div>
          <div class="fp-body">
            <div class="fp-field" v-if="forgotChannel==='email'">
              <div class="fp-input">
                <span class="fp-ico">
                  <svg viewBox="0 0 24 24" width="18" height="18"><path d="M4 6h16v12H4z" fill="none" stroke="currentColor" stroke-width="2"/><path d="M22 6l-10 7L2 6" fill="none" stroke="currentColor" stroke-width="2"/></svg>
                </span>
                <input placeholder="注册邮箱" v-model="forgotEmail" type="email" />
              </div>
            </div>
            <div class="fp-field" v-else>
              <div class="fp-input">
                <span class="fp-ico">
                  <svg viewBox="0 0 24 24" width="18" height="18"><rect x="7" y="2" width="10" height="20" rx="2" fill="none" stroke="currentColor" stroke-width="2"/><circle cx="12" cy="18" r="1" fill="currentColor"/></svg>
                </span>
                <input placeholder="绑定手机号" v-model="forgotPhone" type="tel" />
              </div>
            </div>
            <div class="fp-code">
              <div class="fp-input">
                <span class="fp-ico">
                  <svg viewBox="0 0 24 24" width="18" height="18"><rect x="5" y="10" width="14" height="9" rx="2" fill="none" stroke="currentColor" stroke-width="2"/><path d="M8 10V8a4 4 0 0 1 8 0v2" fill="none" stroke="currentColor" stroke-width="2"/></svg>
                </span>
                <input placeholder="6位验证码" v-model="forgotCode" maxlength="6" />
              </div>
              <button class="fp-btn fp-send" :disabled="sendLoading||sendCooldown>0||!canSend" @click="sendCode">{{ sendCooldown>0 ? `重新获取(${sendCooldown}s)` : (sendLoading ? '获取中…' : '获取验证码') }}</button>
            </div>
            <div class="fp-field">
              <div class="fp-input">
                <span class="fp-ico">
                  <svg viewBox="0 0 24 24" width="18" height="18"><path d="M12 2a5 5 0 0 1 5 5v3h-10V7a5 5 0 0 1 5-5z" fill="none" stroke="currentColor" stroke-width="2"/><rect x="6" y="10" width="12" height="10" rx="2" fill="none" stroke="currentColor" stroke-width="2"/></svg>
                </span>
                <input placeholder="新密码（大小写字母+数字，≥8位）" type="password" v-model="forgotNewPwd" />
              </div>
            </div>
            <div class="fp-error" v-if="forgotError">{{ forgotError }}</div>
            <div class="fp-actions fp-duo">
              <button class="fp-btn fp-primary" :disabled="resetLoading||!canReset" @click="resetPassword">{{ resetLoading ? '重置中…' : '确认' }}</button>
              <button class="fp-btn fp-ghost" @click="closeForgot">取消</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { onMounted } from 'vue'
import request from '@/utils/request'
import { API } from '@/utils/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loginError = ref('')
const loginForm = reactive({
  username: '',
  password: ''
})

// 新增防自动填充的readonly变量
const readonlyUser = ref(true)
const readonlyPass = ref(true)
const isTransitioning = ref(false)

// 在页面加载时，强制清空表单和旧的登录状态
onMounted(() => {
  loginForm.username = '';
  loginForm.password = '';
  // 为确保每次进入登录页都是干净的状态，我们清除之前的登录缓存
  // 这可以防止因热更新或缓存导致的数据重新填充问题
  uni.removeStorageSync('token');
  uni.removeStorageSync('userInfo');
  // 清除用户store状态
  userStore.clearUserInfo();
})

const unlockPasswordInput = () => {
  readonlyPass.value = false
}

const handleLogin = async () => {
  try {
    if (!loginForm.username) {
      loginError.value = '请输入账号'
      return
    }
    if (!loginForm.password) {
      loginError.value = '请输入密码'
      return
    }

    // 显示加载提示
    uni.showLoading({
      title: '登录中...',
      mask: true // 添加遮罩防止重复点击
    })

    const response = await request.post(API.AUTH.LOGIN, {
      username: loginForm.username,
      password: loginForm.password
    })

    // 检查后端返回的业务码，而不是仅仅依赖 HTTP 状态码
    if (response.code === 200) {
      // 保存token和用户信息
      const { token, userInfo } = response.data
      // 兼容头像字段
      if (userInfo.avatarUrl) {
        userInfo.avatar = userInfo.avatarUrl;
      }
      // 写入当前标签页会话优先
      try {
        window.sessionStorage.setItem('token', token)
        window.sessionStorage.setItem('userInfo', JSON.stringify(userInfo))
        window.sessionStorage.setItem('activeUserId', String(userInfo.userId || ''))
      } catch {}
      // 持久化副本
      uni.setStorageSync('token', token)
      uni.setStorageSync('userInfo', JSON.stringify(userInfo))

      // 更新用户store状态
      userStore.setUserInfo(userInfo)
      // 确保store状态与本地存储同步
      userStore.initializeStore()

      // 显示成功提示
      uni.showToast({
        title: '登录成功',
        icon: 'success',
        duration: 1000
      })

      setTimeout(() => {
        try {
          const sevenDays = 7 * 24 * 3600 * 1000
          uni.setStorageSync('rememberUntil', Date.now() + sevenDays)
        } catch {}
        isTransitioning.value = true
        setTimeout(() => { router.push('/pages/home/index') }, 180)
      }, 800)
    } else {
      // 后端返回非200业务码，认为是业务错误
      // 错误处理已在请求拦截器中完成，这里只需要显示错误信息
      // 但是为了确保 loading 被隐藏，我们还是在 finally 里面处理
      // throw new Error(response.message || '登录失败'); // 不再抛出错误，由拦截器处理或直接显示消息
      console.error('Login failed with backend code:', response.code, 'message:', response.message);
      loginError.value = response.message || '登录失败'; // 直接显示后端返回的消息
    }

  } catch (error) {
    // 错误处理已在请求拦截器中完成
    // 拦截器可能会抛出带 response 信息的错误
    console.error('登录失败（catch block）：', error)
    loginError.value = error?.response?.data?.message || error.message || '登录失败，请重试'; // 尝试从错误对象获取更详细信息
  } finally {
    // 无论成功或失败，都隐藏加载提示
    uni.hideLoading();
  }
}

const forgotVisible = ref(false)
const forgotChannel = ref('email')
const forgotEmail = ref('')
const forgotPhone = ref('')
const forgotCode = ref('')
const forgotNewPwd = ref('')
const forgotError = ref('')
const sendLoading = ref(false)
const resetLoading = ref(false)
const sendCooldown = ref(0)
let cooldownTimer = null
const openForgotModal = () => { forgotVisible.value = true }
const closeForgot = (force = false) => {
  if (!force && (resetLoading.value || sendLoading.value)) return
  forgotVisible.value = false
  forgotEmail.value = ''
  forgotPhone.value = ''
  forgotCode.value = ''
  forgotNewPwd.value = ''
  forgotError.value = ''
  sendCooldown.value = 0
  if (cooldownTimer) { clearInterval(cooldownTimer); cooldownTimer = null }
}
const isValidEmail = (v) => /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(String(v||'').trim())
const isValidPhone = (v) => /^\+?\d{6,20}$/.test(String(v||'').trim())
const canSend = computed(() => forgotChannel.value==='email' ? isValidEmail(forgotEmail.value) : isValidPhone(forgotPhone.value))
const canReset = computed(() => {
  const pwd = String(forgotNewPwd.value||'').trim()
  return canSend.value && /^\d{6}$/.test(forgotCode.value.trim()) && /^[a-zA-Z0-9]{8,}$/.test(pwd)
})
const sendCode = async () => {
  if (!canSend.value) { forgotError.value='请填写有效的联系方式'; return }
  try {
    sendLoading.value = true
    forgotError.value = ''
    const contact = forgotChannel.value==='email' ? String(forgotEmail.value).trim() : String(forgotPhone.value).trim()
    const body = forgotChannel.value==='email' ? { email: contact } : { phone: contact }
    const resp = await request.post(API.AUTH.FORGOT.SEND_CODE, body)
    if (resp && (resp.code===200 || resp.code===20000)) {
      const getUrl = forgotChannel.value==='email' ? API.AUTH.FORGOT.EMAIL_CODE(contact) : API.AUTH.FORGOT.PHONE_CODE(contact)
      try {
        const resp2 = await request.get(getUrl)
        if (resp2 && (resp2.code===200 || resp2.code===20000) && resp2.data && resp2.data.code) {
          forgotCode.value = String(resp2.data.code)
          uni.showToast({ title: '获取验证码成功', icon: 'success', duration: 1200 })
        } else {
          uni.showModal({ title: '提示', content: resp2?.message || '获取验证码失败，请重试', showCancel: false })
        }
      } catch (e) {
        uni.showModal({ title: '提示', content: '获取验证码失败，请重试', showCancel: false })
      }
      sendCooldown.value = 60
      cooldownTimer = setInterval(()=>{ if (sendCooldown.value>0) sendCooldown.value--; else { clearInterval(cooldownTimer); cooldownTimer=null } },1000)
    } else { uni.showModal({ title: '提示', content: resp?.message || '获取验证码失败，请重试', showCancel: false }) }
  } finally { sendLoading.value = false }
}
const resetPassword = async () => {
  if (!canReset.value) { forgotError.value='请检查验证码与新密码'; return }
  try {
    resetLoading.value = true
    forgotError.value = ''
    const body = forgotChannel.value==='email'
      ? { email: String(forgotEmail.value).trim(), code: String(forgotCode.value).trim(), newPassword: String(forgotNewPwd.value).trim() }
      : { phone: String(forgotPhone.value).trim(), code: String(forgotCode.value).trim(), newPassword: String(forgotNewPwd.value).trim() }
    const resp = await request.post(API.AUTH.FORGOT.RESET, body)
    if (resp && (resp.code===200 || resp.code===20000)) {
      uni.showToast({ title: '密码重置成功', icon: 'success', duration: 1000 })
      try { forgotVisible.value = false } catch {}
      closeForgot(true)
      setTimeout(() => { try { router.replace('/pages/login/index') } catch {} }, 500)
    } else { forgotError.value = resp?.message || '重置失败' }
  } finally { resetLoading.value = false }
}
onUnmounted(() => { if (cooldownTimer) { clearInterval(cooldownTimer); cooldownTimer = null } })
</script>

<style scoped lang="scss">
@use 'sass:math';
@use 'sass:string';
.login-cosmic-container {
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
.page-transition-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  pointer-events: none;
  background: radial-gradient(circle at 50% 50%, rgba(124,77,255,0.18), rgba(0,0,0,0.55));
  animation: fadeOutSoft 220ms ease forwards;
}
@keyframes fadeOutSoft { from { opacity: 0; } to { opacity: 1; } }
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
  position: relative;
  z-index: 3;
  width: 100%;
  margin: 0 auto 0 auto;
  max-width: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}
.cosmic-logo-area { text-align: center; margin-bottom: 24px; z-index: 1; }
.cosmic-welcome-text {
  margin-top: 0;
  font-size: 30px;
  font-weight: 700;
  background: linear-gradient(90deg, #7c4dff 0%, #00e5ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 18px #7c4dff44, 0 0 8px #00e5ff33;
  letter-spacing: 2px;
}
.cosmic-subtitle {
  margin-top: 8px;
  font-size: 16px;
  color: #a8c6ff;
}
.cosmic-form-area {
  width: 100%;
  max-width: 420px;
  background: linear-gradient(135deg, rgba(22,26,44,0.96), rgba(28,32,58,0.92));
  border-radius: 16px;
  box-shadow: 0 10px 28px rgba(0,0,0,0.22);
  border: 1px solid rgba(106,118,255,0.25);
  backdrop-filter: blur(12px);
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
  margin-bottom: 20px;
}
.cosmic-form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 15px;
  color: #a8c6ff;
  font-weight: 500;
}
.cosmic-input-wrapper {
  display: flex;
  align-items: center;
  background: rgba(30, 40, 70, 0.75);
  border-radius: 12px;
  padding: 0 12px;
  border: 1px solid rgba(106,118,255,0.25);
  transition: border-color 0.3s;
}
.cosmic-input-wrapper:focus-within {
  border-color: #6a76ff;
}
.cosmic-input-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  color: #6a76ff;
  margin-right: 8px;
}
.cosmic-form-input {
  flex: 1;
  border: none;
  background: transparent;
  height: 48px;
  font-size: 16px;
  color: #eaf2ff;
  outline: none;
}
.cosmic-form-input::placeholder {
  color: #a8c6ff;
  opacity: 0.7;
}
.cosmic-error-message {
  color: #ff4d4f;
  font-size: 15px;
  margin: 10px 0 0 0;
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
.cosmic-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  height: 20px;
  width: 100%;
}
.cosmic-error-inline {
  color: #ff4d4f;
  font-size: 15px;
  margin: 0;
  flex: 1;
  text-align: left;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cosmic-forgot-password {
  color: #7c4dff;
  text-decoration: none;
  transition: all 0.3s;
  font-size: 14px;
  margin-left: 12px;
  flex-shrink: 0;
}
.cosmic-forgot-password:hover {
  color: #00e5ff;
  text-decoration: underline;
}
.cosmic-start-btn {
  padding: 10px 48px;
  font-size: 1.1rem;
  color: #fff;
  background: linear-gradient(90deg, #6a76ff 0%, #00ccff 100%);
  background-size: 200% 100%;
  background-position: left center;
  border: none;
  border-radius: 14px;
  cursor: pointer;
  font-weight: bold;
  letter-spacing: 2px;
  box-shadow: 0 10px 28px rgba(106,118,255,0.32);
  position: relative;
  overflow: hidden;
  transition: box-shadow 0.4s;
  display: flex;
  align-items: center;
  gap: 8px;
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
  font-size: 14px;
  color: #a8c6ff;
  margin-top: 18px;
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
.fp-overlay { position: fixed; inset:0; z-index: 9999; display:flex; align-items:center; justify-content:center; background: radial-gradient(1200px 700px at 50% 50%, rgba(24,28,48,0.75) 0%, rgba(0,0,0,0.45) 100%); backdrop-filter: blur(6px); }
.fp-card { width: 600px; max-width: 92vw; border-radius: 26px; background: linear-gradient(135deg, rgba(20,24,40,0.96) 0%, rgba(28,32,58,0.96) 100%); border: 1.5px solid rgba(124,77,255,0.45); box-shadow: 0 26px 90px rgba(124,77,255,0.30), 0 12px 34px rgba(0,229,255,0.14); padding: 24px 22px 28px; display:flex; flex-direction:column; gap: 8px; }
.fp-header { display:flex; align-items:center; justify-content:space-between; margin-bottom: 12px; }
.fp-title { font-size: 1.32rem; font-weight: 800; color: #ffd54f; letter-spacing: .5px; }
.fp-seg { display:flex; gap:10px; margin-bottom: 4px; }
.fp-seg button { padding:10px 16px; border-radius:12px; border:1px solid rgba(124,77,255,0.35); background: rgba(255,255,255,0.06); color:#eaf2ff; cursor:pointer; transition: all .25s ease; }
.fp-seg button:hover { transform: translateY(-1px); box-shadow: 0 6px 16px rgba(124,77,255,0.22); }
.fp-seg button.active { background: linear-gradient(90deg,#7c4dff 0%, #00e5ff 100%); color:#fff; border:none; box-shadow: 0 8px 24px rgba(124,77,255,0.28); }
.fp-body { display:flex; flex-direction:column; gap:16px; }
.fp-field { display:flex; align-items:center; justify-content:center; }
.fp-input { width: 92%; display:flex; align-items:center; gap:10px; background: rgba(30,40,70,0.85); border: 1px solid #3a4a6f; border-radius: 12px; padding: 10px 14px; transition: all .2s ease; }
.fp-input:focus-within { border-color: #7c4dff; box-shadow: 0 0 0 3px rgba(124,77,255,0.18); }
.fp-ico { display:inline-flex; align-items:center; justify-content:center; color:#7c4dff; }
.fp-input input { flex:1; height: 40px; border: none; background: transparent; color:#eaf2ff; outline:none; }
.fp-input input::placeholder { color:#a8c6ff; opacity:.8; }
.fp-actions { display:flex; align-items:center; justify-content:center; gap: 10px; margin-top: 8px; width: 92%; margin-left: auto; margin-right: auto; }
.fp-actions.fp-duo { display:grid; grid-template-columns: repeat(2, 1fr); gap: 12px; width: 92%; margin-left: auto; margin-right: auto; align-items: center; }
.fp-btn { border-radius:12px; border:none; cursor:pointer; transition: background-position .6s ease, transform .18s ease, box-shadow .28s ease; display:flex; align-items:center; justify-content:center; }
.fp-actions.fp-duo .fp-btn { width: 100%; height: 56px; }
.fp-send { padding:12px 18px; color:#1a1f35; background: linear-gradient(90deg,#ffd54f 0%, #ff9100 100%); background-size: 200% 100%; min-width: 220px; box-shadow: 0 6px 18px rgba(255,190,0,0.24); width: 100%; }
.fp-send:hover { background-position: right center; transform: translateY(-1px); box-shadow: 0 10px 26px rgba(255,190,0,0.32); }
.fp-send:active { transform: translateY(1px) scale(0.98); }
.fp-send[disabled] { opacity:0.6; cursor:not-allowed; box-shadow: none; }
.fp-primary { padding:12px 18px; color:#fff; background: linear-gradient(90deg,#7c4dff 0%, #00e5ff 100%); background-size: 200% 100%; box-shadow: 0 6px 18px rgba(124,77,255,0.24); }
.fp-primary:hover { background-position: right center; transform: translateY(-1px); box-shadow: 0 10px 26px rgba(124,77,255,0.32); }
.fp-primary:active { transform: translateY(1px) scale(0.98); }
.fp-primary[disabled] { opacity:0.6; cursor:not-allowed; box-shadow: none; }
.fp-ghost { padding:12px 18px; color:#eaf2ff; background: rgba(255,255,255,0.08); border:1px solid rgba(255,255,255,0.22); }
.fp-ghost:hover { transform: translateY(-1px); box-shadow: 0 6px 16px rgba(255,255,255,0.18); }
.fp-error { color:#ff4d4f; text-align:center; }
.fp-seg button { min-width: 120px; height: 40px; display: inline-flex; align-items: center; justify-content: center; }
.fp-code { width: 96%; margin: 0 auto; display: grid; grid-template-columns: 1fr 220px; gap: 12px; align-items: center; }
@media (max-width: 500px) {
  .cosmic-form-area { padding: 18px 8px 18px 8px; }
  .cosmic-form-input { height: 40px; font-size: 15px; }
  .cosmic-logo { width: 56px; height: 56px; font-size: 26px; }
}

.login-back { position: fixed; top: 12px; left: 12px; z-index: 9; display: inline-flex; align-items: center; justify-content: center; padding: 4px; border-radius: 8px; cursor: pointer; }
.login-back-ico { color: #7c4dff; filter: drop-shadow(0 0 6px rgba(124,77,255,0.35)); }
.login-back:hover .login-back-ico { transform: translateX(-2px); }
</style> 

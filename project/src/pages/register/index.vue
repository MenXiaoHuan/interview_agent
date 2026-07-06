<template>
  <div class="register-cosmic-container">
    <div
      class="register-back"
      @click="router.replace('/pages/login/index')"
      aria-label="返回登录"
    >
      <svg
        class="register-back-ico"
        width="22"
        height="22"
        viewBox="0 0 24 24"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          d="M15 6l-6 6 6 6"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M21 12H9"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
        />
      </svg>
    </div>
    <div id="stars"></div>
    <div id="stars2"></div>
    <div id="stars3"></div>

    <div class="register-layout">
      <section class="register-showcase">
        <div class="register-showcase-inner">
          <div class="register-brand-name">AI Interview Agent</div>
          <div class="register-brand-subtitle">统一认证入口</div>

          <div class="register-showcase-content">
            <h1>创建你的训练账号</h1>
            <p>
              完成账号注册后，即可进入岗位选择、模拟面试、报告复盘等完整训练流程。
            </p>

            <div class="register-feature-list">
              <div class="register-feature-item">
                <span class="register-feature-dot"></span>
                <span>账号创建后可直接进入登录链路</span>
              </div>
              <div class="register-feature-item">
                <span class="register-feature-dot"></span>
                <span>注册页与登录页保持统一视觉与交互节奏</span>
              </div>
              <div class="register-feature-item">
                <span class="register-feature-dot"></span>
                <span>邮箱和手机号保留为可选补充信息</span>
              </div>
            </div>
          </div>

          <div class="register-showcase-copy">© 2026 AI Interview Agent</div>
        </div>
      </section>

      <section class="register-panel">
        <div class="register-panel-header">
          <div class="register-panel-eyebrow">创建账号</div>
          <h2 class="register-panel-title">注册系统</h2>
          <p class="register-panel-subtitle">
            填写必要信息后即可创建个人训练空间
          </p>
        </div>

        <input
          type="text"
          style="display: none"
          tabindex="-1"
          autocomplete="username"
        />
        <input
          type="password"
          style="display: none"
          tabindex="-1"
          autocomplete="new-password"
        />

        <div class="register-form-group">
          <label class="register-form-label">账号</label>
          <div class="register-input-wrapper">
            <span class="register-input-icon">
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <circle
                  cx="12"
                  cy="8"
                  r="4"
                  stroke="currentColor"
                  stroke-width="2"
                />
                <path
                  d="M4 20c1.8-3.2 5-5 8-5s6.2 1.8 8 5"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
            </span>
            <input
              type="text"
              class="register-form-input"
              placeholder="请输入账号（仅限数字和字母，最多16位）"
              maxlength="16"
              v-model="registerForm.username"
              autocomplete="off"
            />
          </div>
        </div>

        <div class="register-form-group">
          <label class="register-form-label">密码</label>
          <div class="register-input-wrapper">
            <span class="register-input-icon">
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <rect
                  x="5"
                  y="10"
                  width="14"
                  height="9"
                  rx="2"
                  stroke="currentColor"
                  stroke-width="2"
                />
                <path
                  d="M8 10V8a4 4 0 0 1 8 0v2"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
            </span>
            <input
              type="password"
              class="register-form-input"
              placeholder="请设置密码（大小写字母+数字，≥8位）"
              v-model="registerForm.password"
              autocomplete="off"
            />
          </div>
        </div>

        <div class="register-form-group">
          <label class="register-form-label">确认密码</label>
          <div class="register-input-wrapper">
            <span class="register-input-icon">
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <rect
                  x="5"
                  y="10"
                  width="14"
                  height="9"
                  rx="2"
                  stroke="currentColor"
                  stroke-width="2"
                />
                <path
                  d="M8 10V8a4 4 0 0 1 8 0v2"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
            </span>
            <input
              type="password"
              class="register-form-input"
              placeholder="请再次输入密码"
              v-model="registerForm.confirmPassword"
              autocomplete="off"
            />
          </div>
        </div>

        <div class="register-grid-row">
          <div class="register-form-group">
            <label class="register-form-label">邮箱（选填）</label>
            <div class="register-input-wrapper">
              <span class="register-input-icon">
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    d="M4 6h16v12H4z"
                    stroke="currentColor"
                    stroke-width="2"
                  />
                  <path
                    d="M4 8l8 5 8-5"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                  />
                </svg>
              </span>
              <input
                type="email"
                class="register-form-input"
                placeholder="请输入邮箱（选填）"
                v-model="registerForm.email"
              />
            </div>
          </div>

          <div class="register-form-group">
            <label class="register-form-label">手机号（选填）</label>
            <div class="register-input-wrapper">
              <span class="register-input-icon">
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    d="M6 4h12v16H6z"
                    stroke="currentColor"
                    stroke-width="2"
                  />
                  <circle cx="12" cy="18" r="1" fill="currentColor" />
                  <path
                    d="M9 6h6"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                  />
                </svg>
              </span>
              <input
                type="tel"
                class="register-form-input"
                placeholder="请输入手机号（选填）"
                v-model="registerForm.phone"
              />
            </div>
          </div>
        </div>

        <transition name="fade-slide">
          <div
            v-if="
              registerError ||
              usernameError ||
              passwordError ||
              confirmPasswordError
            "
            class="register-error-message"
          >
            <div v-if="registerError">{{ registerError }}</div>
            <div v-if="usernameError">{{ usernameError }}</div>
            <div v-if="passwordError">{{ passwordError }}</div>
            <div v-if="confirmPasswordError">{{ confirmPasswordError }}</div>
          </div>
        </transition>

        <button class="register-start-btn" @click="handleRegister">
          立即注册
        </button>

        <div class="register-divider">
          <span></span>
          <em>完成注册后返回登录</em>
          <span></span>
        </div>

        <div class="register-other-options">
          <span>已有账号? </span>
          <router-link to="/pages/login/index" class="register-link-text"
            >返回登录</router-link
          >
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import request from "@/utils/api/request";
import { API } from "@/utils/api/pages/register";

const router = useRouter();
const usernameError = ref("");
const passwordError = ref("");
const confirmPasswordError = ref("");
const registerError = ref("");

const registerForm = reactive({
  username: "",
  password: "",
  confirmPassword: "",
  email: "",
  phone: "",
});

const clearErrors = () => {
  usernameError.value = "";
  passwordError.value = "";
  confirmPasswordError.value = "";
  registerError.value = "";
};

const validateForm = () => {
  clearErrors();
  let isValid = true;

  // 验证用户名
  if (!registerForm.username) {
    usernameError.value = "请输入账号";
    isValid = false;
  } else if (!/^[a-zA-Z0-9]{6,16}$/.test(registerForm.username)) {
    usernameError.value = "账号长度6-16位，只能包含字母和数字";
    isValid = false;
  }

  // 验证密码
  if (!registerForm.password) {
    passwordError.value = "请设置密码";
    isValid = false;
  } else if (!/^[a-zA-Z0-9]{8,}$/.test(registerForm.password)) {
    passwordError.value = "密码必须大于等于8位，只能包含大小写字母和数字";
    isValid = false;
  }

  // 验证确认密码
  if (!registerForm.confirmPassword) {
    confirmPasswordError.value = "请确认密码";
    isValid = false;
  } else if (registerForm.password !== registerForm.confirmPassword) {
    confirmPasswordError.value = "两次输入的密码不一致";
    isValid = false;
  }

  // 验证邮箱（选填）
  if (
    registerForm.email &&
    !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email)
  ) {
    registerError.value = "邮箱格式不正确";
    isValid = false;
  }

  // 验证手机号（选填）
  if (registerForm.phone && !/^1\d{10}$/.test(registerForm.phone)) {
    registerError.value = "手机号格式不正确";
    isValid = false;
  }

  return isValid;
};

const handleRegister = async () => {
  if (!validateForm()) {
    return;
  }

  try {
    // 显示加载提示
    uni.showLoading({
      title: "注册中...",
      mask: true, // 添加遮罩防止重复点击
    });

    const response = await request.post(API.AUTH.REGISTER, {
      username: registerForm.username,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      email: registerForm.email || undefined,
      phone: registerForm.phone || undefined,
    });

    // 对接后端错误响应
    if (response.code !== 200) {
      registerError.value = response.message || "注册失败";
      uni.hideLoading();
      return;
    }

    // 保存token和用户信息
    const { token, userId, username } = response;
    uni.setStorageSync("token", token);
    uni.setStorageSync(
      "userInfo",
      JSON.stringify({
        userId,
        username,
      }),
    );

    // 隐藏加载提示
    uni.hideLoading();

    // 显示成功提示
    uni.showToast({
      title: "注册成功",
      icon: "success",
      duration: 2000,
    });

    // 注册成功后跳转到登录页
    setTimeout(() => {
      router.push("/pages/login/index");
    }, 2000);
  } catch (error) {
    registerError.value =
      error?.response?.data?.message || error.message || "注册失败";
    uni.hideLoading();
    // 错误处理已在请求拦截器中完成
    console.error("注册失败：", error);
  }
};

onMounted(() => {
  registerForm.username = "";
  registerForm.password = "";
  registerForm.confirmPassword = "";
  registerForm.email = "";
  registerForm.phone = "";
});
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
  padding: 0;
  background:
    radial-gradient(
      circle at top left,
      rgba(0, 229, 255, 0.12),
      transparent 22%
    ),
    radial-gradient(
      circle at bottom right,
      rgba(124, 77, 255, 0.18),
      transparent 24%
    ),
    radial-gradient(1200px 700px at 50% 0%, #12203a, #0a1929);
}

@function multiple-box-shadow($n) {
  $value: "#{math.random(2560)}px #{math.random(2560)}px #FFF";
  @for $i from 2 through $n {
    $value: "#{$value}, #{math.random(2560)}px #{math.random(2560)}px #FFF";
  }
  @return string.unquote($value);
}

$shadows-small: multiple-box-shadow(1000);
$shadows-medium: multiple-box-shadow(300);
$shadows-large: multiple-box-shadow(150);

#stars,
#stars2,
#stars3 {
  position: absolute;
  inset: 0;
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
  from {
    transform: translateY(0px);
  }
  to {
    transform: translateY(-2560px);
  }
}

.register-layout {
  position: relative;
  z-index: 3;
  width: 100%;
  min-height: 100vh;
  display: flex;
  overflow: hidden;
}

.register-showcase {
  width: 45%;
  padding: 64px 52px 44px;
  color: #f4f8ff;
  background:
    linear-gradient(160deg, rgba(18, 32, 58, 0.9), rgba(11, 24, 46, 0.86)),
    linear-gradient(135deg, rgba(0, 229, 255, 0.08), rgba(124, 77, 255, 0.12));
  position: relative;
}

.register-showcase::after {
  content: "";
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 22% 18%, rgba(0, 255, 255, 0.1), transparent 18%),
    radial-gradient(
      circle at 78% 82%,
      rgba(124, 77, 255, 0.14),
      transparent 22%
    );
  pointer-events: none;
}

.register-showcase-inner {
  position: relative;
  z-index: 1;
  display: flex;
  min-height: 100%;
  flex-direction: column;
  justify-content: flex-start;
  padding-top: 28px;
}

.register-brand-name {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}

.register-brand-subtitle {
  margin-top: 6px;
  margin-bottom: 54px;
  font-size: 14px;
  opacity: 0.82;
}

.register-showcase-content h1 {
  margin: 0 0 18px;
  font-size: clamp(34px, 4vw, 44px);
  line-height: 1.28;
}

.register-showcase-content p {
  max-width: 460px;
  font-size: 16px;
  line-height: 1.85;
  opacity: 0.92;
  margin-bottom: 38px;
}

.register-feature-list {
  display: grid;
  gap: 16px;
}

.register-feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  line-height: 1.6;
}

.register-feature-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  flex: 0 0 auto;
  background: linear-gradient(135deg, #00e5ff, #7c4dff);
  box-shadow: 0 0 16px rgba(0, 229, 255, 0.32);
}

.register-showcase-copy {
  margin-top: auto;
  padding-top: 40px;
  font-size: 13px;
  opacity: 0.72;
}

.register-panel {
  width: 55%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 72px 32px;
  background: rgba(9, 19, 36, 0.8);
}

.register-panel-header {
  width: min(420px, 100%);
  margin-bottom: 20px;
}

.register-panel-eyebrow {
  font-size: 14px;
  font-weight: 600;
  color: #00e5ff;
  letter-spacing: 0.04em;
}

.register-panel-title {
  margin: 10px 0 8px;
  font-size: 28px;
  color: #f5f9ff;
  line-height: 1.2;
}

.register-panel-subtitle {
  font-size: 13px;
  line-height: 1.7;
  color: #9fb4d9;
}

.register-form-group {
  width: min(420px, 100%);
  margin-bottom: 14px;
}

.register-form-label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: #d7e7ff;
  font-weight: 600;
}

.register-grid-row {
  width: min(420px, 100%);
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.register-grid-row .register-form-group {
  width: 100%;
}

.register-input-wrapper {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 10px;
  padding: 0 12px;
  border: 1px solid rgba(112, 143, 186, 0.32);
  transition: all 0.3s ease;
}

.register-input-wrapper:focus-within {
  border-color: #00e5ff;
  box-shadow: 0 0 0 3px rgba(0, 229, 255, 0.1);
  background: rgba(255, 255, 255, 0.06);
}

.register-input-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  color: #00e5ff;
  margin-right: 8px;
}

.register-form-input {
  flex: 1;
  border: none;
  background: transparent;
  height: 44px;
  font-size: 15px;
  color: #f5f9ff;
  outline: none;
}

.register-form-input::placeholder {
  color: #7f95bb;
  opacity: 1;
}

.register-error-message {
  width: min(420px, 100%);
  color: #ff4d4f;
  font-size: 13px;
  margin: 0 0 12px;
}

.register-start-btn {
  width: min(420px, 100%);
  min-height: 46px;
  justify-content: center;
  padding: 0 24px;
  font-size: 16px;
  color: #061324;
  background: linear-gradient(90deg, #00e5ff 0%, #00bfff 100%);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 700;
  letter-spacing: 0.06em;
  box-shadow: 0 10px 26px rgba(0, 200, 255, 0.22);
  display: flex;
  align-items: center;
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease;
}

.register-start-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 32px rgba(0, 200, 255, 0.28);
}

.register-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 16px 0 12px;
  width: min(420px, 100%);
}

.register-divider span {
  flex: 1;
  height: 1px;
  background: linear-gradient(
    90deg,
    rgba(0, 229, 255, 0),
    rgba(0, 229, 255, 0.26),
    rgba(0, 229, 255, 0)
  );
}

.register-divider em {
  font-style: normal;
  font-size: 12px;
  color: #7f95bb;
  white-space: nowrap;
}

.register-other-options {
  width: min(420px, 100%);
  text-align: center;
  font-size: 14px;
  color: #9fb4d9;
}

.register-link-text {
  color: #00e5ff;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s;
}

.register-link-text:hover {
  color: #7c4dff;
  text-decoration: underline;
}

.register-back {
  position: fixed;
  top: 18px;
  left: 18px;
  z-index: 9;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 4px;
}

.register-back-ico {
  color: #dfe9ff;
  filter: drop-shadow(0 0 8px rgba(0, 229, 255, 0.22));
  transition:
    transform 0.2s ease,
    opacity 0.2s ease;
}

.register-back:hover .register-back-ico {
  transform: translateX(-2px);
  opacity: 0.9;
}

@media (max-width: 960px) {
  .register-layout {
    min-height: auto;
    flex-direction: column;
  }

  .register-showcase,
  .register-panel {
    width: 100%;
  }

  .register-showcase {
    padding: 36px 28px 28px;
  }

  .register-brand-subtitle {
    margin-bottom: 36px;
  }

  .register-showcase-content p {
    max-width: none;
  }

  .register-panel {
    padding: 32px 28px 36px;
  }
}

@media (max-width: 640px) {
  .register-showcase {
    padding: 28px 20px 24px;
  }

  .register-panel {
    padding: 28px 20px 32px;
  }

  .register-panel-title {
    font-size: 26px;
  }

  .register-grid-row {
    grid-template-columns: 1fr;
    gap: 0;
  }
}
</style>

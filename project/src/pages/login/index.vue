<template>
  <div class="login-cosmic-container">
    <div
      class="login-back"
      @click="router.replace('/pages/landing/index')"
      aria-label="返回"
    >
      <svg
        class="login-back-ico"
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
    <div v-if="isTransitioning" class="page-transition-overlay"></div>

    <div class="login-layout">
      <section class="login-showcase">
        <div class="showcase-top">
          <div class="showcase-brand-text">
            <div class="showcase-brand-name">AI Interview Agent</div>
            <div class="showcase-brand-subtitle">智能求职实战平台</div>
          </div>

          <div class="showcase-content">
            <h1>一站式智能面试体验</h1>
            <p>
              AI 面试、专项题作答、简历评测、场景评测与综合报告整合到一个入口，
              覆盖岗位选择到报告复盘的完整训练闭环。
            </p>

            <div class="showcase-feature-list">
              <div class="showcase-feature-item">
                <span class="feature-dot"></span>
                <span>多模态评测结合 AI 能力生成针对性反馈</span>
              </div>
              <div class="showcase-feature-item">
                <span class="feature-dot"></span>
                <span>历史记录与综合报告沉淀每一次训练结果</span>
              </div>
              <div class="showcase-feature-item">
                <span class="feature-dot"></span>
                <span>岗位体系、训练流程与社区互动统一协同</span>
              </div>
            </div>
          </div>
          <div class="showcase-copyright">© 2026 AI Interview Agent</div>
        </div>
      </section>

      <section class="login-panel">
        <template v-if="!forgotVisible">
          <div class="login-panel-header">
            <div class="panel-eyebrow">欢迎回来</div>
            <h2 class="panel-title">登录系统</h2>
            <p class="panel-subtitle">请使用您的账号密码继续访问个人训练空间</p>
          </div>

          <!-- 隐藏占位输入框：尽量削弱浏览器对登录表单的自动填充识别 -->
          <input
            type="text"
            style="display: none"
            tabindex="-1"
            autocomplete="off"
          />
          <input
            type="password"
            style="display: none"
            tabindex="-1"
            autocomplete="new-password"
          />

          <div class="cosmic-form-group">
            <label class="cosmic-form-label">账号</label>
            <div class="cosmic-input-wrapper">
              <span class="cosmic-input-icon">
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
                ref="usernameInputRef"
                type="text"
                class="cosmic-form-input"
                placeholder="请输入您的账号"
                maxlength="16"
                v-model="loginForm.username"
                autocomplete="username"
                autocapitalize="off"
                autocorrect="off"
                spellcheck="false"
                name="no-autofill-user-123"
                @keydown.enter.prevent.stop="unlockPasswordInput"
                @keyup.enter.prevent.stop
              />
            </div>
          </div>

          <div class="cosmic-form-group">
            <label class="cosmic-form-label">密码</label>
            <div class="cosmic-input-wrapper">
              <span class="cosmic-input-icon">
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
                ref="passwordInputRef"
                type="password"
                class="cosmic-form-input"
                placeholder="请输入您的密码"
                v-model="loginForm.password"
                autocomplete="current-password"
                name="no-autofill-pass-456"
                @keydown.enter.prevent.stop="handleLogin"
              />
            </div>
          </div>

          <div class="cosmic-info-row">
            <transition name="fade-slide">
              <div
                v-if="loginError"
                class="cosmic-error-message cosmic-error-inline"
              >
                {{ loginError }}
              </div>
              <div
                v-else
                class="cosmic-error-message cosmic-error-inline"
                style="visibility: hidden"
              >
                占位
              </div>
            </transition>
            <a
              href="#"
              class="cosmic-forgot-password"
              @click.prevent="openForgotModal"
              >忘记密码?</a
            >
          </div>

          <button class="cosmic-start-btn" @click="handleLogin">
            立即登录
          </button>

          <div class="panel-divider">
            <span></span>
            <em>开始你的训练旅程</em>
            <span></span>
          </div>

          <div class="panel-assist-list">
            <div class="panel-assist-item">岗位选择与面试模式一体进入</div>
            <div class="panel-assist-item">账号状态与本地会话同步保留</div>
          </div>

          <div class="cosmic-other-options">
            <span>还没有账号? </span>
            <router-link to="/pages/register/index" class="cosmic-link-text"
              >立即注册</router-link
            >
          </div>
        </template>

        <template v-else>
          <div class="login-panel-header forgot-panel-header">
            <div class="fp-heading">
              <div class="panel-eyebrow">账号恢复</div>
              <h2 class="panel-title">找回密码</h2>
              <div class="fp-subtitle">选择验证方式后重置账号密码</div>
            </div>
          </div>
          <div class="forgot-panel-body">
            <div class="fp-seg">
              <button
                :class="{ active: forgotChannel === 'email' }"
                @click="forgotChannel = 'email'"
              >
                邮箱验证
              </button>
              <button
                :class="{ active: forgotChannel === 'phone' }"
                @click="forgotChannel = 'phone'"
              >
                手机号验证
              </button>
            </div>
            <div class="fp-section-label">验证账号</div>
            <div class="fp-field" v-if="forgotChannel === 'email'">
              <div class="fp-input">
                <span class="fp-ico">
                  <svg viewBox="0 0 24 24" width="18" height="18">
                    <path
                      d="M4 6h16v12H4z"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    />
                    <path
                      d="M22 6l-10 7L2 6"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    />
                  </svg>
                </span>
                <input
                  placeholder="注册邮箱"
                  v-model="forgotEmail"
                  type="email"
                />
              </div>
            </div>
            <div class="fp-field" v-else>
              <div class="fp-input">
                <span class="fp-ico">
                  <svg viewBox="0 0 24 24" width="18" height="18">
                    <rect
                      x="7"
                      y="2"
                      width="10"
                      height="20"
                      rx="2"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    />
                    <circle cx="12" cy="18" r="1" fill="currentColor" />
                  </svg>
                </span>
                <input
                  placeholder="绑定手机号"
                  v-model="forgotPhone"
                  type="tel"
                />
              </div>
            </div>
            <div class="fp-section-label">验证码与新密码</div>
            <div class="fp-code">
              <div class="fp-code-field">
                <div class="fp-input">
                  <span class="fp-ico">
                    <svg viewBox="0 0 24 24" width="18" height="18">
                      <rect
                        x="5"
                        y="10"
                        width="14"
                        height="9"
                        rx="2"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                      />
                      <path
                        d="M8 10V8a4 4 0 0 1 8 0v2"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                      />
                    </svg>
                  </span>
                  <input
                    placeholder="6位验证码"
                    v-model="forgotCode"
                    maxlength="6"
                  />
                </div>
              </div>
              <button
                class="fp-btn fp-send"
                :disabled="sendLoading || sendCooldown > 0 || !canSend"
                @click="sendCode"
              >
                {{
                  sendCooldown > 0
                    ? `重新获取(${sendCooldown}s)`
                    : sendLoading
                      ? "获取中…"
                      : "获取验证码"
                }}
              </button>
            </div>
            <div class="fp-field">
              <div class="fp-input">
                <span class="fp-ico">
                  <svg viewBox="0 0 24 24" width="18" height="18">
                    <path
                      d="M12 2a5 5 0 0 1 5 5v3h-10V7a5 5 0 0 1 5-5z"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    />
                    <rect
                      x="6"
                      y="10"
                      width="12"
                      height="10"
                      rx="2"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    />
                  </svg>
                </span>
                <input
                  placeholder="新密码（大小写字母+数字，≥8位）"
                  type="password"
                  v-model="forgotNewPwd"
                />
              </div>
            </div>
            <div class="fp-error" v-if="forgotError">{{ forgotError }}</div>
            <div class="fp-actions">
              <button
                class="fp-btn fp-primary"
                :disabled="resetLoading || !canReset"
                @click="resetPassword"
              >
                {{ resetLoading ? "重置中…" : "确认" }}
              </button>
            </div>
            <div class="cosmic-other-options forgot-panel-footer">
              <span>想起密码了? </span>
              <a href="#" class="cosmic-link-text" @click.prevent="closeForgot"
                >回到登录</a
              >
            </div>
          </div>
        </template>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onUnmounted, nextTick } from "vue";
import { useRouter } from "vue-router";
import { onMounted } from "vue";
import request from "@/utils/request";
import { API } from "@/utils/api";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const userStore = useUserStore();
const loginError = ref("");
const loginForm = reactive({
  username: "",
  password: "",
});

const isTransitioning = ref(false);
const usernameInputRef = ref(null);
const passwordInputRef = ref(null);

const isHtmlElement = (node) =>
  typeof HTMLElement !== "undefined" && node instanceof HTMLElement;
const resolveNativeInput = (inputRefLike) => {
  const candidate = inputRefLike?.$el || inputRefLike;
  if (!candidate) return null;
  if (isHtmlElement(candidate)) {
    if (
      typeof candidate.matches === "function" &&
      candidate.matches("input, textarea")
    ) {
      return candidate;
    }
    return typeof candidate.querySelector === "function"
      ? candidate.querySelector("input, textarea, .uni-input-input")
      : null;
  }
  if (typeof candidate?.querySelector === "function") {
    return candidate.querySelector("input, textarea, .uni-input-input");
  }
  return null;
};

// 在页面加载时，强制清空表单和旧的登录状态
onMounted(() => {
  loginForm.username = "";
  loginForm.password = "";
  // 为确保每次进入登录页都是干净的状态，我们清除之前的登录缓存
  // 这可以防止因热更新或缓存导致的数据重新填充问题
  uni.removeStorageSync("token");
  uni.removeStorageSync("userInfo");
  // 清除用户store状态
  userStore.clearUserInfo();
});

const forceFocusInput = (input) => {
  const nativeInput = resolveNativeInput(input);
  if (!nativeInput || typeof nativeInput.focus !== "function") {
    return;
  }
  nativeInput.focus();
  if (typeof nativeInput.select === "function") {
    nativeInput.select();
  }
};

const focusPasswordInput = async () => {
  await nextTick();
  const passwordInput = passwordInputRef.value;
  if (!passwordInput) return;
  forceFocusInput(passwordInput);
  setTimeout(() => forceFocusInput(passwordInput), 0);
  setTimeout(() => forceFocusInput(passwordInput), 48);
  setTimeout(() => forceFocusInput(passwordInput), 120);
};

const unlockPasswordInput = async (event) => {
  event?.preventDefault?.();
  event?.stopPropagation?.();
  const usernameInput = resolveNativeInput(usernameInputRef.value);
  if (usernameInput && typeof usernameInput.blur === "function") {
    usernameInput.blur();
  }
  await focusPasswordInput();
};

const handleLogin = async () => {
  try {
    if (!loginForm.username) {
      loginError.value = "请输入账号";
      return;
    }
    if (!loginForm.password) {
      loginError.value = "请输入密码";
      return;
    }

    // 显示加载提示
    uni.showLoading({
      title: "登录中...",
      mask: true, // 添加遮罩防止重复点击
    });

    const response = await request.post(API.AUTH.LOGIN, {
      username: loginForm.username,
      password: loginForm.password,
    });

    // 检查后端返回的业务码，而不是仅仅依赖 HTTP 状态码
    if (response.code === 200) {
      // 保存token和用户信息
      const { token, userInfo } = response.data;
      // 兼容头像字段
      if (userInfo.avatarUrl) {
        userInfo.avatar = userInfo.avatarUrl;
      }
      // 写入当前标签页会话优先
      try {
        window.sessionStorage.setItem("token", token);
        window.sessionStorage.setItem("userInfo", JSON.stringify(userInfo));
        window.sessionStorage.setItem(
          "activeUserId",
          String(userInfo.userId || ""),
        );
      } catch {}
      // 持久化副本
      uni.setStorageSync("token", token);
      uni.setStorageSync("userInfo", JSON.stringify(userInfo));

      // 更新用户store状态
      userStore.setUserInfo(userInfo);
      // 确保store状态与本地存储同步
      userStore.initializeStore();

      // 显示成功提示
      uni.showToast({
        title: "登录成功",
        icon: "success",
        duration: 1000,
      });

      setTimeout(() => {
        try {
          const sevenDays = 7 * 24 * 3600 * 1000;
          uni.setStorageSync("rememberUntil", Date.now() + sevenDays);
        } catch {}
        isTransitioning.value = true;
        setTimeout(() => {
          router.push("/pages/home/index");
        }, 180);
      }, 800);
    } else {
      // 后端返回非200业务码，认为是业务错误
      // 错误处理已在请求拦截器中完成，这里只需要显示错误信息
      // 但是为了确保 loading 被隐藏，我们还是在 finally 里面处理
      // throw new Error(response.message || '登录失败'); // 不再抛出错误，由拦截器处理或直接显示消息
      console.error(
        "Login failed with backend code:",
        response.code,
        "message:",
        response.message,
      );
      loginError.value = response.message || "登录失败"; // 直接显示后端返回的消息
    }
  } catch (error) {
    // 错误处理已在请求拦截器中完成
    // 拦截器可能会抛出带 response 信息的错误
    console.error("登录失败（catch block）：", error);
    loginError.value =
      error?.response?.data?.message || error.message || "登录失败，请重试"; // 尝试从错误对象获取更详细信息
  } finally {
    // 无论成功或失败，都隐藏加载提示
    uni.hideLoading();
  }
};

const forgotVisible = ref(false);
const forgotChannel = ref("email");
const forgotEmail = ref("");
const forgotPhone = ref("");
const forgotCode = ref("");
const forgotNewPwd = ref("");
const forgotError = ref("");
const sendLoading = ref(false);
const resetLoading = ref(false);
const sendCooldown = ref(0);
let cooldownTimer = null;
const openForgotModal = () => {
  forgotVisible.value = true;
};
const closeForgot = (force = false) => {
  if (!force && (resetLoading.value || sendLoading.value)) return;
  forgotVisible.value = false;
  forgotEmail.value = "";
  forgotPhone.value = "";
  forgotCode.value = "";
  forgotNewPwd.value = "";
  forgotError.value = "";
  sendCooldown.value = 0;
  if (cooldownTimer) {
    clearInterval(cooldownTimer);
    cooldownTimer = null;
  }
};
const isValidEmail = (v) =>
  /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(
    String(v || "").trim(),
  );
const isValidPhone = (v) => /^\+?\d{6,20}$/.test(String(v || "").trim());
const canSend = computed(() =>
  forgotChannel.value === "email"
    ? isValidEmail(forgotEmail.value)
    : isValidPhone(forgotPhone.value),
);
const canReset = computed(() => {
  const pwd = String(forgotNewPwd.value || "").trim();
  return (
    canSend.value &&
    /^\d{6}$/.test(forgotCode.value.trim()) &&
    /^[a-zA-Z0-9]{8,}$/.test(pwd)
  );
});
const sendCode = async () => {
  if (!canSend.value) {
    forgotError.value = "请填写有效的联系方式";
    return;
  }
  try {
    sendLoading.value = true;
    forgotError.value = "";
    const contact =
      forgotChannel.value === "email"
        ? String(forgotEmail.value).trim()
        : String(forgotPhone.value).trim();
    const body =
      forgotChannel.value === "email" ? { email: contact } : { phone: contact };
    const resp = await request.post(API.AUTH.FORGOT.SEND_CODE, body);
    if (resp && (resp.code === 200 || resp.code === 20000)) {
      const getUrl =
        forgotChannel.value === "email"
          ? API.AUTH.FORGOT.EMAIL_CODE(contact)
          : API.AUTH.FORGOT.PHONE_CODE(contact);
      try {
        const resp2 = await request.get(getUrl);
        if (
          resp2 &&
          (resp2.code === 200 || resp2.code === 20000) &&
          resp2.data &&
          resp2.data.code
        ) {
          forgotCode.value = String(resp2.data.code);
          uni.showToast({
            title: "获取验证码成功",
            icon: "success",
            duration: 1200,
          });
        } else {
          uni.showModal({
            title: "提示",
            content: resp2?.message || "获取验证码失败，请重试",
            showCancel: false,
          });
        }
      } catch (e) {
        uni.showModal({
          title: "提示",
          content: "获取验证码失败，请重试",
          showCancel: false,
        });
      }
      sendCooldown.value = 60;
      cooldownTimer = setInterval(() => {
        if (sendCooldown.value > 0) sendCooldown.value--;
        else {
          clearInterval(cooldownTimer);
          cooldownTimer = null;
        }
      }, 1000);
    } else {
      uni.showModal({
        title: "提示",
        content: resp?.message || "获取验证码失败，请重试",
        showCancel: false,
      });
    }
  } finally {
    sendLoading.value = false;
  }
};
const resetPassword = async () => {
  if (!canReset.value) {
    forgotError.value = "请检查验证码与新密码";
    return;
  }
  try {
    resetLoading.value = true;
    forgotError.value = "";
    const body =
      forgotChannel.value === "email"
        ? {
            email: String(forgotEmail.value).trim(),
            code: String(forgotCode.value).trim(),
            newPassword: String(forgotNewPwd.value).trim(),
          }
        : {
            phone: String(forgotPhone.value).trim(),
            code: String(forgotCode.value).trim(),
            newPassword: String(forgotNewPwd.value).trim(),
          };
    const resp = await request.post(API.AUTH.FORGOT.RESET, body);
    if (resp && (resp.code === 200 || resp.code === 20000)) {
      uni.showToast({ title: "密码重置成功", icon: "success", duration: 1000 });
      try {
        forgotVisible.value = false;
      } catch {}
      closeForgot(true);
      setTimeout(() => {
        try {
          router.replace("/pages/login/index");
        } catch {}
      }, 500);
    } else {
      forgotError.value = resp?.message || "重置失败";
    }
  } finally {
    resetLoading.value = false;
  }
};
onUnmounted(() => {
  if (cooldownTimer) {
    clearInterval(cooldownTimer);
    cooldownTimer = null;
  }
});
</script>

<style scoped lang="scss">
@use "sass:math";
@use "sass:string";

.login-cosmic-container {
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
  transition: background 0.4s;
}
.page-transition-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  pointer-events: none;
  background: radial-gradient(
    circle at 50% 50%,
    rgba(124, 77, 255, 0.18),
    rgba(0, 0, 0, 0.55)
  );
  animation: fadeOutSoft 220ms ease forwards;
}
@keyframes fadeOutSoft {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
// 星空box-shadow星点实现
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
  from {
    transform: translateY(0px);
  }
  to {
    transform: translateY(-2560px);
  }
}
.login-layout {
  position: relative;
  z-index: 3;
  width: 100%;
  min-height: 100vh;
  display: flex;
  overflow: hidden;
}

.login-showcase {
  width: 45%;
  padding: 64px 52px 44px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #f4f8ff;
  background:
    linear-gradient(160deg, rgba(18, 32, 58, 0.9), rgba(11, 24, 46, 0.86)),
    linear-gradient(135deg, rgba(0, 229, 255, 0.08), rgba(124, 77, 255, 0.12));
  position: relative;
}

.login-showcase::after {
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

.showcase-top,
.showcase-content {
  position: relative;
  z-index: 1;
}

.showcase-top {
  display: flex;
  min-height: 100%;
  flex-direction: column;
  justify-content: flex-start;
  padding-top: 28px;
}

.showcase-brand-name {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}

.showcase-brand-subtitle {
  margin-top: 6px;
  font-size: 14px;
  opacity: 0.82;
  margin-bottom: 54px;
}

.showcase-content h1 {
  margin: 22px 0 18px;
  font-size: clamp(34px, 4vw, 44px);
  line-height: 1.28;
  letter-spacing: 0.01em;
}

.showcase-content p {
  max-width: 460px;
  font-size: 16px;
  line-height: 1.85;
  opacity: 0.92;
  margin-bottom: 38px;
}

.showcase-feature-list {
  display: grid;
  gap: 16px;
}

.showcase-feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  line-height: 1.6;
}

.feature-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  flex: 0 0 auto;
  background: linear-gradient(135deg, #00e5ff, #7c4dff);
  box-shadow: 0 0 16px rgba(0, 229, 255, 0.32);
}

.showcase-copyright {
  margin-top: auto;
  padding-top: 40px;
  font-size: 13px;
  opacity: 0.72;
}

.login-panel {
  width: 55%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 72px;
  background: rgba(9, 19, 36, 0.8);
}

.login-panel-header {
  width: min(420px, 100%);
  margin-bottom: 32px;
}

.panel-eyebrow {
  font-size: 14px;
  font-weight: 600;
  color: #00e5ff;
  letter-spacing: 0.04em;
}

.panel-title {
  margin: 12px 0 10px;
  font-size: 30px;
  color: #f5f9ff;
  line-height: 1.2;
}

.panel-subtitle {
  font-size: 14px;
  line-height: 1.8;
  color: #9fb4d9;
}
.cosmic-form-group {
  width: min(420px, 100%);
  margin-bottom: 20px;
}
.cosmic-form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #d7e7ff;
  font-weight: 600;
}
.cosmic-input-wrapper {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 10px;
  padding: 0 12px;
  border: 1px solid rgba(112, 143, 186, 0.32);
  transition: all 0.3s ease;
}
.cosmic-input-wrapper:focus-within {
  border-color: #00e5ff;
  box-shadow: 0 0 0 3px rgba(0, 229, 255, 0.1);
  background: rgba(255, 255, 255, 0.06);
}
.cosmic-input-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  color: #00e5ff;
  margin-right: 8px;
}
.cosmic-form-input {
  flex: 1;
  border: none;
  background: transparent;
  height: 48px;
  font-size: 16px;
  color: #f5f9ff;
  outline: none;
}
.cosmic-form-input::placeholder {
  color: #7f95bb;
  opacity: 1;
}
.cosmic-error-message {
  color: #ff4d4f;
  font-size: 15px;
  margin: 10px 0 0 0;
  animation: shake 0.3s;
}
@keyframes shake {
  0% {
    transform: translateX(0);
  }
  20% {
    transform: translateX(-8px);
  }
  40% {
    transform: translateX(8px);
  }
  60% {
    transform: translateX(-8px);
  }
  80% {
    transform: translateX(8px);
  }
  100% {
    transform: translateX(0);
  }
}
.cosmic-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22px;
  min-height: 20px;
  width: min(420px, 100%);
}
.cosmic-error-inline {
  color: #ff4d4f;
  font-size: 13px;
  margin: 0;
  flex: 1;
  text-align: left;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cosmic-forgot-password {
  color: #00e5ff;
  text-decoration: none;
  transition: all 0.3s;
  font-size: 14px;
  margin-left: 12px;
  flex-shrink: 0;
}
.cosmic-forgot-password:hover {
  color: #7c4dff;
  text-decoration: underline;
}
.cosmic-start-btn {
  width: min(420px, 100%);
  justify-content: center;
  min-height: 48px;
  padding: 0 24px;
  font-size: 16px;
  color: #061324;
  background: linear-gradient(90deg, #00e5ff 0%, #00bfff 100%);
  background-size: 200% 100%;
  background-position: left center;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 700;
  letter-spacing: 0.06em;
  box-shadow: 0 10px 26px rgba(0, 200, 255, 0.22);
  position: relative;
  overflow: hidden;
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease,
    background-position 0.5s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}
.cosmic-start-btn::before {
  content: "";
  position: absolute;
  left: -60%;
  top: 0;
  width: 60%;
  height: 100%;
  background: linear-gradient(
    120deg,
    rgba(255, 255, 255, 0.18) 0%,
    rgba(255, 255, 255, 0.04) 100%
  );
  filter: blur(2px);
  opacity: 0;
  transition:
    opacity 0.3s,
    left 0.5s;
  pointer-events: none;
}
.cosmic-start-btn:hover {
  box-shadow: 0 14px 32px rgba(0, 200, 255, 0.28);
  transform: translateY(-2px);
}
.cosmic-start-btn:hover::before {
  left: 110%;
  opacity: 1;
  transition:
    opacity 0.3s,
    left 0.5s;
}
.cosmic-other-options {
  width: min(420px, 100%);
  text-align: center;
  font-size: 14px;
  color: #9fb4d9;
  margin-top: 24px;
}
.cosmic-link-text {
  color: #00e5ff;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s;
}
.cosmic-link-text:hover {
  color: #7c4dff;
  text-decoration: underline;
}
.panel-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 24px 0 18px;
  width: min(420px, 100%);
}

.panel-divider span {
  flex: 1;
  height: 1px;
  background: linear-gradient(
    90deg,
    rgba(0, 229, 255, 0),
    rgba(0, 229, 255, 0.26),
    rgba(0, 229, 255, 0)
  );
}

.panel-divider em {
  font-style: normal;
  font-size: 12px;
  color: #7f95bb;
  white-space: nowrap;
}

.panel-assist-list {
  display: grid;
  gap: 10px;
  width: min(420px, 100%);
}

.panel-assist-item {
  position: relative;
  padding-left: 16px;
  font-size: 13px;
  line-height: 1.6;
  color: #9fb4d9;
}

.panel-assist-item::before {
  content: "";
  position: absolute;
  left: 0;
  top: 8px;
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: #00e5ff;
}
.forgot-panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
}
.forgot-panel-body {
  width: min(420px, 100%);
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.fp-heading {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.fp-title {
  font-size: 1.24rem;
  font-weight: 700;
  color: #f5f9ff;
  letter-spacing: 0.02em;
}
.fp-subtitle {
  font-size: 13px;
  line-height: 1.6;
  color: #8ea8cf;
}
.fp-seg {
  display: flex;
  gap: 8px;
  margin-bottom: 2px;
  width: min(420px, 100%);
}
.fp-seg button {
  flex: 1;
  min-width: 0;
  min-height: 44px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid rgba(112, 143, 186, 0.28);
  background: rgba(255, 255, 255, 0.03);
  color: #9fb4d9;
  cursor: pointer;
  transition: all 0.25s ease;
}
.fp-seg button:hover {
  border-color: rgba(0, 229, 255, 0.3);
  color: #d7e7ff;
}
.fp-seg button.active {
  background: rgba(0, 229, 255, 0.12);
  color: #00e5ff;
  border-color: rgba(0, 229, 255, 0.32);
  box-shadow: none;
}
.fp-body {
  display: contents;
}
.fp-section-label {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: #7f95bb;
}
.fp-field {
  display: flex;
  align-items: center;
  justify-content: flex-start;
}
.fp-input {
  width: min(420px, 100%);
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(112, 143, 186, 0.32);
  border-radius: 10px;
  padding: 0 12px;
  transition: all 0.2s ease;
}
.fp-input:focus-within {
  border-color: #00e5ff;
  box-shadow: 0 0 0 3px rgba(0, 229, 255, 0.1);
}
.fp-ico {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #00e5ff;
}
.fp-input input {
  flex: 1;
  height: 48px;
  border: none;
  background: transparent;
  color: #eaf2ff;
  outline: none;
}
.fp-input input::placeholder {
  color: #a8c6ff;
  opacity: 0.8;
}
.fp-code-field {
  flex: 0 0 160px;
  min-width: 0;
  max-width: 160px;
  display: flex;
  flex-direction: column;
  gap: 0;
}
.fp-code-field .fp-input {
  box-sizing: border-box;
  width: 100%;
  min-width: 0;
}
.fp-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-top: 10px;
  width: min(420px, 100%);
}
.fp-btn {
  border-radius: 12px;
  border: none;
  cursor: pointer;
  transition:
    background-position 0.6s ease,
    transform 0.18s ease,
    box-shadow 0.28s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}
.fp-send {
  flex: 1 1 auto;
  padding: 0 18px;
  color: #061324;
  background: linear-gradient(90deg, #ffd54f 0%, #ff9100 100%);
  background-size: 200% 100%;
  min-width: 0;
  box-shadow: 0 6px 18px rgba(255, 190, 0, 0.24);
}
.fp-send:hover {
  background-position: right center;
  transform: translateY(-1px);
  box-shadow: 0 10px 26px rgba(255, 190, 0, 0.32);
}
.fp-send:active {
  transform: translateY(1px) scale(0.98);
}
.fp-send[disabled] {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}
.fp-primary {
  width: 100%;
  min-height: 48px;
  padding: 0 24px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: #061324;
  background: linear-gradient(90deg, #00e5ff 0%, #00bfff 100%);
  background-size: 200% 100%;
  box-shadow: 0 6px 18px rgba(0, 200, 255, 0.2);
}
.fp-primary:hover {
  background-position: right center;
  transform: translateY(-1px);
  box-shadow: 0 10px 26px rgba(0, 200, 255, 0.28);
}
.fp-primary:active {
  transform: translateY(1px) scale(0.98);
}
.fp-primary[disabled] {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}
.fp-error {
  color: #ff4d4f;
  text-align: center;
}
.fp-seg button {
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.fp-code {
  width: min(420px, 100%);
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: flex-start;
}
.forgot-panel-footer {
  margin-top: 4px;
}

.login-back {
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

.login-back-ico {
  color: #dfe9ff;
  filter: drop-shadow(0 0 8px rgba(0, 229, 255, 0.22));
  transition:
    transform 0.2s ease,
    opacity 0.2s ease;
}

.login-back:hover .login-back-ico {
  transform: translateX(-2px);
  opacity: 0.9;
}

@media (max-width: 960px) {
  .login-cosmic-container {
    padding: 0;
  }

  .login-layout {
    min-height: auto;
    flex-direction: column;
  }

  .login-showcase,
  .login-panel {
    width: 100%;
  }

  .login-showcase {
    padding: 36px 28px 28px;
  }

  .showcase-brand {
    margin-bottom: 36px;
  }

  .showcase-content p {
    max-width: none;
  }

  .login-panel {
    padding: 36px 28px 40px;
  }
}

@media (max-width: 640px) {
  .login-showcase {
    padding: 28px 20px 24px;
  }

  .login-panel {
    padding: 28px 20px 32px;
  }

  .panel-title {
    font-size: 26px;
  }

  .fp-card {
    padding: 0;
  }

  .fp-code {
    grid-template-columns: 1fr;
  }

  .fp-send {
    min-width: 0;
  }
}
</style>

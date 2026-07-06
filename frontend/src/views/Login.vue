<template>
  <!-- 登录页面：全屏左右分栏布局，左侧品牌展示，右侧登录表单 -->
  <div class="login-page">
    <!-- 左侧品牌区域：展示社区Logo、名称和核心功能列表 -->
    <div class="brand-side">
      <div class="brand-content">
        <!-- 品牌Logo图标（房屋SVG） -->
        <div class="brand-logo">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
            <path d="M3 9.5L12 4l9 5.5V20a1 1 0 01-1 1H4a1 1 0 01-1-1V9.5z" stroke="white" stroke-width="1.8" stroke-linejoin="round"/>
            <path d="M9 21V12h6v9" stroke="white" stroke-width="1.8" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1>青青社区</h1>
        <p class="brand-desc">便民服务平台</p>
        <!-- 核心功能列表 -->
        <div class="brand-features">
          <div class="feature-item">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M22 11.08V12a10 10 0 11-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>跑腿帮忙</span>
          </div>
          <div class="feature-item">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M22 11.08V12a10 10 0 11-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>检修服务</span>
          </div>
          <div class="feature-item">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M22 11.08V12a10 10 0 11-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>闲置交易</span>
          </div>
          <div class="feature-item">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M22 11.08V12a10 10 0 11-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>邻里社交</span>
          </div>
        </div>
      </div>
      <!-- 底部版权信息 -->
      <div class="brand-footer">
        <p>&copy; 2026 青青社区 · 让生活更便捷</p>
      </div>
    </div>

    <!-- 右侧登录表单区域 -->
    <div class="form-side">
      <div class="form-container">
        <!-- 表单头部 -->
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录您的账户，继续使用服务</p>
        </div>

        <!-- 登录表单：用户名 + 密码 -->
        <form class="login-form" @submit.prevent="onSubmit">
          <!-- 用户名字段 -->
          <div class="form-field">
            <label>用户名</label>
            <el-input v-model="form.username" placeholder="请输入用户名" size="large" prefix-icon="User" />
          </div>
          <!-- 密码字段：支持回车提交、显示/隐藏密码 -->
          <div class="form-field">
            <label>密码</label>
            <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" prefix-icon="Lock" show-password @keyup.enter="onSubmit" />
          </div>
          <!-- 提交按钮：登录中时显示加载状态 -->
          <el-button type="primary" size="large" :loading="busy" native-type="submit" class="submit-btn">
            {{ busy ? '登录中...' : '登 录' }}
          </el-button>
        </form>

        <!-- 表单底部：默认账号提示 + 注册链接 -->
        <div class="form-footer">
          <!-- 开发/演示用的默认账号提示 -->
          <div class="hint-box">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <path d="M12 16v-4M12 8h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <span>默认账号: admin / admin123</span>
          </div>
          <!-- 跳转到注册页面 -->
          <p class="register-link">还没有账号？<router-link to="/register">立即注册</router-link></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * Login.vue - 登录页面
 *
 * 功能说明：
 * - 提供用户名+密码的登录表单
 * - 登录成功后根据用户角色（role）跳转：
 *   · role >= 1（管理员）→ /admin/dashboard
 *   · role = 0（普通用户）→ /（首页）
 * - 表单验证：检查用户名和密码是否为空
 * - 登录状态通过 useUserStore 的 login action 管理
 */

import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

/** Vue Router 实例，用于登录成功后的页面跳转 */
const router = useRouter()
/** Pinia 用户状态管理 store：管理登录状态、用户信息、token 等 */
const store = useUserStore()
/** 登录表单数据：绑定用户名和密码 */
const form = reactive({ username: '', password: '' })
/** 登录提交中的加载状态：true 时按钮显示"登录中..."并禁用 */
const busy = ref(false)

/**
 * 处理登录表单提交
 *
 * 流程：
 * 1. 前端校验：检查用户名和密码是否为空
 * 2. 调用 store.login() 向后端 POST /auth/login 发送登录请求
 * 3. 成功后根据用户角色跳转：管理员 → /admin/dashboard，普通用户 → /
 * 4. 失败时通过 ElMessage 显示错误提示
 */
async function onSubmit () {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写完整');
    return
  }
  busy.value = true
  try {
    await store.login(form.username, form.password)
    ElMessage.success('登录成功')
    // role: 0 = 普通用户，1 = 管理员，2 = 超级管理员
    router.push(store.user?.role >= 1 ? '/admin/dashboard' : '/')
  } catch (e) {
    ElMessage.error(e?.message || '登录失败')
  } finally {
    busy.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: #f7f9fc;
  font-family: "Microsoft YaHei", sans-serif;
}

/* ========== 左侧品牌区域 ========== */
.brand-side {
  flex: 1.1;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px 56px;
  position: relative;
  overflow: hidden;
}

/* 装饰性渐变光晕（右上角） */
.brand-side::before {
  content: "";
  position: absolute;
  top: -20%;
  right: -10%;
  width: 520px;
  height: 520px;
  background: radial-gradient(circle at center, rgba(99, 102, 241, 0.08), transparent 60%);
  border-radius: 50%;
}

/* 装饰性渐变光晕（左下角） */
.brand-side::after {
  content: "";
  position: absolute;
  bottom: -30%;
  left: -20%;
  width: 480px;
  height: 480px;
  background: radial-gradient(circle at center, rgba(236, 72, 153, 0.06), transparent 60%);
  border-radius: 50%;
}

.brand-content {
  position: relative;
  z-index: 1;
  margin-top: 24px;
}

/* 品牌Logo：紫色渐变圆角方块 */
.brand-logo {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 16px;
  display: grid;
  place-items: center;
  margin-bottom: 28px;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.15);
}

.brand-content h1 {
  font-size: 42px;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 8px;
  letter-spacing: -0.5px;
}

.brand-desc {
  font-size: 18px;
  color: #64748b;
  margin: 0 0 40px;
}

/* 功能列表 */
.brand-features {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #475569;
  font-size: 16px;
  padding: 6px 10px;
  border-radius: 12px;
  transition: background 0.2s ease;
}

.feature-item:hover {
  background: #f1f5f9;
}

.feature-item svg {
  color: #6366f1;
  width: 20px;
  height: 20px;
}

.brand-footer {
  position: relative;
  z-index: 1;
}

.brand-footer p {
  color: #94a3b8;
  font-size: 13px;
  margin: 0;
}

/* ========== 右侧表单区域 ========== */
.form-side {
  flex: 0.9;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 32px;
}

.form-container {
  width: 100%;
  max-width: 420px;
}

.form-header {
  margin-bottom: 36px;
}

.form-header h2 {
  font-size: 30px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px;
}

.form-header p {
  font-size: 15px;
  color: #64748b;
  margin: 0;
}

/* 登录表单布局 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 28px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-field label {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}

/* 深度样式：自定义 Element Plus 输入框外观 */
.form-field :deep(.el-input__wrapper) {
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  padding: 10px 16px;
  background: #fcfcfd;
  box-shadow: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.form-field :deep(.el-input__wrapper:hover) {
  border-color: #cbd5e1;
  background: #ffffff;
}

.form-field :deep(.el-input__wrapper.is-focus) {
  border-color: #6366f1;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.08);
}

/* 登录按钮 */
.submit-btn {
  width: 100%;
  height: 54px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: 14px !important;
  background: #6366f1 !important;
  border: none !important;
  margin-top: 8px;
  transition: background 0.2s ease, transform 0.15s ease, box-shadow 0.2s ease !important;
}

.submit-btn:hover {
  background: #4f46e5 !important;
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(99, 102, 241, 0.18);
}

.submit-btn:active {
  transform: translateY(0);
}

/* 表单底部区域 */
.form-footer {
  text-align: center;
}

/* 默认账号提示框 */
.hint-box {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 12px;
  font-size: 13px;
  color: #475569;
  margin-bottom: 20px;
  border: 1px solid #e2e8f0;
}

.hint-box svg {
  color: #94a3b8;
  flex-shrink: 0;
}

/* 注册链接 */
.register-link {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.register-link a {
  color: #6366f1;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s ease;
}

.register-link a:hover {
  color: #4f46e5;
  text-decoration: underline;
}

/* ========== 移动端适配 ========== */
@media (max-width: 1024px) {
  /* 平板及以下隐藏左侧品牌区 */
  .brand-side {
    display: none;
  }
  .form-side {
    padding: 32px 20px;
  }
}

@media (max-width: 480px) {
  .form-header h2 {
    font-size: 26px;
  }
  .form-container {
    max-width: 100%;
  }
}
</style>

<template>
  <!-- 注册页面：全屏左右分栏布局，左侧品牌介绍，右侧注册表单 -->
  <div class="register-page">
    <!-- 左侧品牌区：展示社区Logo、注册引导和核心优势 -->
    <div class="brand-side">
      <div class="brand-content">
        <!-- 品牌Logo图标（房屋SVG） -->
        <div class="brand-logo">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
            <path d="M3 9.5L12 4l9 5.5V20a1 1 0 01-1 1H4a1 1 0 01-1-1V9.5z" stroke="white" stroke-width="1.8" stroke-linejoin="round"/>
            <path d="M9 21V12h6v9" stroke="white" stroke-width="1.8" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1>加入青青社区</h1>
        <p class="brand-desc">创建账户，享受便捷社区服务</p>
        <!-- 注册优势列表（带序号） -->
        <div class="brand-benefits">
          <div class="benefit-item">
            <div class="benefit-icon">01</div>
            <div>
              <strong>发布需求</strong>
              <span>跑腿、检修、交易一站搞定</span>
            </div>
          </div>
          <div class="benefit-item">
            <div class="benefit-icon">02</div>
            <div>
              <strong>邻里互助</strong>
              <span>社区邻居互帮互助更温馨</span>
            </div>
          </div>
          <div class="benefit-item">
            <div class="benefit-icon">03</div>
            <div>
              <strong>安全保障</strong>
              <span>实名认证，交易更放心</span>
            </div>
          </div>
        </div>
      </div>
      <!-- 底部版权信息 -->
      <div class="brand-footer">
        <p>&copy; 2026 青青社区 · 让生活更便捷</p>
      </div>
    </div>

    <!-- 右侧注册表单区域 -->
    <div class="form-side">
      <div class="form-container">
        <!-- 表单头部 -->
        <div class="form-header">
          <h2>创建账号</h2>
          <p>填写以下信息完成注册</p>
        </div>

        <!-- 注册表单 -->
        <form class="register-form" @submit.prevent="onSubmit">
          <!-- 第一行：用户名 + 真实姓名（两列并排） -->
          <div class="form-row">
            <div class="form-field">
              <label>用户名 <span class="required">*</span></label>
              <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
            </div>
            <div class="form-field">
              <label>真实姓名 <span class="required">*</span></label>
              <el-input v-model="form.realName" placeholder="请输入真实姓名" size="large" />
            </div>
          </div>

          <!-- 密码字段：不少于6位，需包含数字和字母 -->
          <div class="form-field">
            <label>密码 <span class="required">*</span></label>
            <el-input v-model="form.password" type="password" placeholder="至少6位，包含数字和字母" size="large" show-password />
          </div>

          <!-- 手机号字段 -->
          <div class="form-field">
            <label>手机号 <span class="required">*</span></label>
            <el-input v-model="form.phone" placeholder="请输入手机号" size="large" />
          </div>

          <!-- 第二行：楼栋号 + 房间号（两列并排，非必填） -->
          <div class="form-row">
            <div class="form-field">
              <label>楼栋号</label>
              <el-input v-model="form.building" placeholder="如：3栋" size="large" />
            </div>
            <div class="form-field">
              <label>房间号</label>
              <el-input v-model="form.room" placeholder="如：502" size="large" />
            </div>
          </div>

          <!-- 提交按钮 -->
          <el-button type="primary" size="large" :loading="busy" native-type="submit" class="submit-btn">
            {{ busy ? '注册中...' : '立即注册' }}
          </el-button>
        </form>

        <!-- 表单底部：已有账号则跳转登录 -->
        <div class="form-footer">
          <p>已有账号？<router-link to="/login">返回登录</router-link></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * Register.vue - 用户注册页面
 *
 * 功能说明：
 * - 提供用户注册表单，包含用户名、真实姓名、密码、手机号、楼栋号、房间号
 * - 必填项：用户名、真实姓名、密码、手机号
 * - 密码校验：至少6位，必须同时包含数字和字母
 * - 注册成功：POST /auth/register → 跳转到登录页面
 * - 注册失败：显示后端返回的错误信息
 */

import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'

/** Vue Router 实例，注册成功后跳转到登录页面 */
const router = useRouter()
/** 注册表单数据对象 */
const form = reactive({ username: '', password: '', realName: '', phone: '', building: '', room: '' })
/** 注册提交中的加载状态：true 时按钮显示"注册中..."并禁用 */
const busy = ref(false)

/**
 * 处理注册表单提交
 *
 * 校验流程：
 * 1. 检查必填项（用户名、密码、姓名、手机号）是否为空
 * 2. 密码长度 >= 6 位
 * 3. 密码必须同时包含数字和字母
 *
 * API 调用：POST /auth/register
 * - 请求体：{ username, password, realName, phone, building, room }
 * - 响应 code == 200 → 注册成功，跳转到 /login
 * - 响应 code != 200 → 显示后端返回的错误信息
 */
async function onSubmit () {
  // 必填项校验
  if (!form.username || !form.password || !form.realName || !form.phone) { ElMessage.warning('请填写必填项'); return }
  // 密码长度校验
  if (form.password.length < 6) { ElMessage.warning('密码至少6位'); return }
  // 密码复杂度校验：必须同时包含字母和数字
  if (!/[a-zA-Z]/.test(form.password) || !/[0-9]/.test(form.password)) { ElMessage.warning('密码必须包含数字和字母'); return }
  busy.value = true
  try {
    const { data } = await api.post('/auth/register', form)
    if (data.code === 200) { ElMessage.success('注册成功'); router.push('/login') }
    else ElMessage.error(data.message || '注册失败')
  } catch { ElMessage.error('注册失败') }
  finally { busy.value = false }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
}

/* ========== 左侧品牌区 ========== */
.brand-side {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 60px;
  position: relative;
  overflow: hidden;
}

/* 装饰性呼吸动画光晕 */
.brand-side::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 50%);
  animation: pulse 8s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.brand-content {
  position: relative;
  z-index: 1;
}

/* 品牌Logo：毛玻璃效果 */
.brand-logo {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  display: grid;
  place-items: center;
  margin-bottom: 32px;
  backdrop-filter: blur(10px);
}

.brand-content h1 {
  font-size: 48px;
  font-weight: 800;
  color: white;
  margin: 0 0 12px;
  letter-spacing: -1px;
}

.brand-desc {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0 0 48px;
}

/* 优势列表 */
.brand-benefits {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.benefit-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

/* 序号圆形图标 */
.benefit-icon {
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-size: 16px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.benefit-item strong {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: white;
  margin-bottom: 4px;
}

.benefit-item span {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.brand-footer {
  position: relative;
  z-index: 1;
}

.brand-footer p {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  margin: 0;
}

/* ========== 右侧表单区 ========== */
.form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: #f8fafc;
  overflow-y: auto;
}

.form-container {
  width: 100%;
  max-width: 480px;
}

.form-header {
  margin-bottom: 36px;
}

.form-header h2 {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px;
}

.form-header p {
  font-size: 16px;
  color: #64748b;
  margin: 0;
}

/* 注册表单布局 */
.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 28px;
}

/* 双列行布局 */
.form-row {
  display: flex;
  gap: 16px;
}

.form-row .form-field {
  flex: 1;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-field label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

/* 必填标记红色星号 */
.required {
  color: #ef4444;
}

/* 深度样式：自定义 Element Plus 输入框外观 */
.form-field :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 8px 16px;
}

.form-field :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.form-field :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.3);
}

/* 注册按钮：渐变紫色背景 */
.submit-btn {
  width: 100%;
  height: 52px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: 12px !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  margin-top: 8px;
}

.submit-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 表单底部 */
.form-footer {
  text-align: center;
}

.form-footer p {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.form-footer a {
  color: #667eea;
  font-weight: 600;
  text-decoration: none;
}

.form-footer a:hover {
  text-decoration: underline;
}

/* ========== 响应式适配 ========== */
@media (max-width: 1024px) {
  /* 平板及以下隐藏左侧品牌区 */
  .brand-side {
    display: none;
  }

  .form-side {
    padding: 40px 24px;
  }
}

@media (max-width: 640px) {
  /* 小屏幕将双列改为单列 */
  .form-row {
    flex-direction: column;
  }

  .form-header h2 {
    font-size: 28px;
  }
}
</style>

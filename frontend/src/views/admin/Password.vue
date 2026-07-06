<template>
  <!-- 修改密码页面：当前密码/新密码/确认密码表单 -->
  <div class="page-container">
    <div class="page-header"><h1>修改密码</h1><p class="subtitle">修改管理员登录密码</p></div>

    <!-- 密码修改表单 -->
    <el-card class="form-card">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="120px" class="password-form">
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input v-model="formData.currentPassword" type="password" show-password placeholder="请输入当前密码" style="width: 400px;" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="formData.newPassword" type="password" show-password placeholder="请输入新密码" style="width: 400px;" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="formData.confirmPassword" type="password" show-password placeholder="请再次输入新密码" style="width: 400px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">保存修改</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
/**
 * Password.vue - 管理员修改密码页面
 *
 * 功能说明：
 * - 提供管理员修改自身密码的表单
 * - 校验规则：
 *   · 当前密码必填
 *   · 新密码必填，长度6-20位
 *   · 确认密码必填，且必须与新密码一致
 * - 修改成功后自动清空表单
 * - API: updatePassword({ oldPassword, newPassword })
 */

import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { updatePassword } from '@/api/admin'

/** 提交按钮加载状态 */
const loading = ref(false)
/** 表单引用 */
const formRef = ref(null)

/** 密码表单数据 */
const formData = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

/**
 * 自定义校验器：确认密码需与新密码一致
 */
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== formData.newPassword) { callback(new Error('两次输入的密码不一致')) }
  else { callback() }
}

/** 表单校验规则 */
const formRules = {
  currentPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

/** 重置表单并清除校验状态 */
const handleReset = () => {
  formData.currentPassword = ''; formData.newPassword = ''; formData.confirmPassword = ''
  if (formRef.value) formRef.value.clearValidate()
}

/**
 * 提交修改密码
 * API: updatePassword({ oldPassword, newPassword })
 * 成功后自动清空表单
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await updatePassword({ oldPassword: formData.currentPassword, newPassword: formData.newPassword })
      ElMessage.success('密码修改成功'); handleReset()
    } catch (error) { console.error('修改密码失败:', error) }
    finally { loading.value = false }
  })
}
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h1 { margin: 0 0 8px 0; font-size: 24px; font-weight: 600; }
.page-header .subtitle { margin: 0; color: #909399; font-size: 14px; }
.form-card { max-width: 600px; }
.password-form { padding: 20px 0; }
</style>

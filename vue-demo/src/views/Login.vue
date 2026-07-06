<template>
  <div class="login-page">
    <div class="login-card">
      <h2>用户登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-item">
          <label for="name">用户名</label>
          <input 
            id="name" 
            v-model.trim="form.name" 
            type="text" 
            placeholder="请输入用户名" 
          />
        </div>
        <div class="form-item">
          <label for="password">密码</label>
          <input
            id="password"
            v-model.trim="form.password"
            type="password"
            placeholder="请输入密码"
          />
        </div>
        <button type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { loginApi } from '@/api/user'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')

const form = reactive({
  name: '',
  password: ''
})

const handleLogin = async () => {
  errorMsg.value = ''
  
  if (!form.name || !form.password) {
    errorMsg.value = '请输入用户名和密码'
    return
  }
  
  try {
    loading.value = true
    const res = await loginApi({
      name: form.name,
      password: form.password
    })
    
    localStorage.setItem('loginUser', JSON.stringify(res.data || {}))
    router.push('/students')
  } catch (error) {
    errorMsg.value = error?.message || '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
}

.login-card {
  width: 380px;
  max-width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 24px;
  box-sizing: border-box;
  background: #fff;
  text-align: left;
}

h2 {
  margin: 0 0 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

input {
  height: 38px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 0 12px;
  box-sizing: border-box;
}

button {
  width: 100%;
  height: 40px;
  border: none;
  border-radius: 6px;
  background: #2563eb;
  color: #fff;
  cursor: pointer;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.error-msg {
  margin: 14px 0 0;
  color: #dc2626;
  font-size: 14px;
}
</style>

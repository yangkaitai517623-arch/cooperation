<template>
  <div class="student-page">
    <div class="header">
      <h2>学生管理</h2>
      <div class="actions">
        <button class="secondary" @click="loadList">刷新</button>
        <button @click="logout">退出登录</button>
      </div>
    </div>
    
    <p v-if="loading">数据加载中...</p>
    <p v-else-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
    
    <table v-else class="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>姓名</th>
          <th>年龄</th>
          <th>性别</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="students.length === 0">
          <td colspan="4">暂无数据</td>
        </tr>
        <tr v-for="item in students" :key="item.id || item.name">
          <td>{{ item.id ?? '-' }}</td>
          <td>{{ item.name ?? '-' }}</td>
          <td>{{ item.age ?? '-' }}</td>
          <td>{{ item.gender ?? '-' }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getUserListApi, getStudentListApi } from '@/api/user'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')
const students = ref([])

const loadList = async () => {
  try {
    loading.value = true
    errorMsg.value = ''
    
    const res = await getStudentListApi()
    students.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    errorMsg.value = error?.message || '获取列表失败'
  } finally {
    loading.value = false
  }
}

const logout = () => {
  localStorage.removeItem('loginUser')
  router.push('/login')
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.student-page {
  padding: 24px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.actions {
  display: flex;
  gap: 10px;
}

button {
  border: none;
  border-radius: 6px;
  padding: 8px 14px;
  color: #fff;
  background: #2563eb;
  cursor: pointer;
}

button.secondary {
  background: #4b5563;
}

.table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #e5e7eb;
  background: #fff;
}

.table th,
.table td {
  border: 1px solid #e5e7eb;
  padding: 10px;
  text-align: left;
}

.table thead {
  background: #f9fafb;
}

.error-msg {
  color: #dc2626;
}
</style>

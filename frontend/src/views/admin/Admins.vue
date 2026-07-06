<template>
  <!-- 管理员管理页面：管理员表格 + 添加/编辑对话框 -->
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header" style="display: flex; justify-content: space-between; align-items: center;">
      <div>
        <h1>管理员管理</h1>
        <p class="subtitle">管理系统管理员账号</p>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 添加管理员
      </el-button>
    </div>

    <!-- 管理员表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <!-- 用户信息列 -->
        <el-table-column label="用户信息" min-width="250">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="40">{{ row.realName?.charAt(0) || row.username?.charAt(0) }}</el-avatar>
              <div class="user-detail"><span class="user-name">{{ row.realName || row.username }}</span><span class="user-email">{{ row.phone || row.email }}</span></div>
            </div>
          </template>
        </el-table-column>
        <!-- 角色列：role=1管理员, role=2超级管理员 -->
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 2 ? 'danger' : 'primary'">
              {{ row.role === 2 ? '超级管理员' : '管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 状态列：status=1启用, status=0禁用 -->
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <!-- 操作列：编辑/删除（超级管理员不可删除） -->
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)" :disabled="row.role === 2">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage" v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]" :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange" @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加管理员对话框：从普通用户中选择提升 -->
    <el-dialog v-model="addDialogVisible" title="添加管理员" width="500px">
      <el-alert type="info" :closable="false" style="margin-bottom: 20px;">
        从普通用户中选择一个提升为管理员
      </el-alert>
      <el-form label-width="100px">
        <el-form-item label="选择用户">
          <el-select v-model="selectedUserId" placeholder="请选择要提升为管理员的用户" filterable style="width: 100%">
            <el-option v-for="user in userList" :key="user.id" :label="user.realName + ' (' + user.username + ')'" :value="user.id" />
          </el-select>
        </el-form-item>
        <!-- 分配角色：1=管理员, 2=超级管理员 -->
        <el-form-item label="分配角色">
          <el-select v-model="selectedRole" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" :value="1" />
            <el-option label="超级管理员" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddSubmit" :loading="submitLoading" :disabled="!selectedUserId">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑管理员对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑管理员" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="用户名"><el-input v-model="editForm.username" disabled /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="editForm.realName" placeholder="请输入姓名" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="editForm.phone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="editForm.email" placeholder="请输入邮箱" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" placeholder="请选择角色">
            <el-option label="管理员" :value="1" />
            <el-option label="超级管理员" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * Admins.vue - 管理员管理页面
 *
 * 功能说明：
 * - 分页展示所有管理员列表
 * - 添加管理员：从普通用户中选择，分配 role（1=管理员, 2=超级管理员）
 * - 编辑管理员：修改姓名、手机号、邮箱、角色
 * - 删除管理员：将 role 设为 0（恢复为普通用户），超级管理员不可删除
 * - 用户状态枚举：status: 0=禁用, 1=启用; role: 0=普通用户, 1=管理员, 2=超级管理员
 */

import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAdminList, createAdmin, updateAdmin, deleteAdmin } from '@/api/admin'
import api from '@/api'

/** 表格加载状态 */
const loading = ref(false)
/** 提交按钮加载状态 */
const submitLoading = ref(false)
/** 表格数据 */
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

/** 添加管理员对话框 */
const addDialogVisible = ref(false)
/** 编辑管理员对话框 */
const editDialogVisible = ref(false)

/** 可选用户列表 */
const userList = ref([])
/** 选中的用户ID */
const selectedUserId = ref(null)
/** 选中的角色：1=管理员, 2=超级管理员 */
const selectedRole = ref(1)

/** 编辑表单数据 */
const editForm = reactive({
  id: null, username: '', realName: '', phone: '', email: '', role: 1
})

/**
 * 加载管理员列表
 * API: getAdminList({ page, size })
 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminList({ page: currentPage.value, size: pageSize.value })
    tableData.value = res.data?.data?.records || []
    total.value = res.data?.data?.total || 0
  } catch (error) { console.error('加载管理员数据失败:', error) }
  finally { loading.value = false }
}

/**
 * 加载普通用户列表（用于添加管理员时选择）
 * API: GET /admin/users?page=1&size=100
 */
const loadUserList = async () => {
  try {
    const res = await api.get('/admin/users', { params: { page: 1, size: 100 } })
    userList.value = res.data?.data?.records || []
  } catch (error) { console.error('加载用户列表失败:', error) }
}

const handleSizeChange = (val) => { pageSize.value = val; loadData() }
const handleCurrentChange = (val) => { currentPage.value = val; loadData() }

/** 打开添加管理员对话框 */
const handleAdd = () => { selectedUserId.value = null; selectedRole.value = 1; loadUserList(); addDialogVisible.value = true }

/**
 * 提交添加管理员
 * API: PUT /admin/users/{userId}/role?role={role}
 * 将选中用户的 role 提升为管理员
 */
const handleAddSubmit = async () => {
  if (!selectedUserId.value) { ElMessage.warning('请选择用户'); return }
  submitLoading.value = true
  try {
    await api.put(`/admin/users/${selectedUserId.value}/role`, null, { params: { role: selectedRole.value } })
    ElMessage.success('管理员添加成功'); addDialogVisible.value = false; loadData()
  } catch { ElMessage.error('添加失败') }
  finally { submitLoading.value = false }
}

/**
 * 打开编辑管理员对话框
 * @param {Object} row - 管理员行数据
 */
const handleEdit = (row) => {
  editForm.id = row.id; editForm.username = row.username || ''; editForm.realName = row.realName || ''
  editForm.phone = row.phone || ''; editForm.email = row.email || ''; editForm.role = row.role || 1
  editDialogVisible.value = true
}

/**
 * 提交编辑管理员
 * API: updateAdmin(id, editForm)
 */
const handleEditSubmit = async () => {
  submitLoading.value = true
  try { await updateAdmin(editForm.id, editForm); ElMessage.success('更新成功'); editDialogVisible.value = false; loadData() }
  catch { ElMessage.error('更新失败') }
  finally { submitLoading.value = false }
}

/**
 * 删除管理员（恢复为普通用户）
 * 将 role 设为 0，超级管理员（role=2）不可删除
 * @param {Object} row - 管理员行数据
 */
const handleDelete = async (row) => {
  if (row.role === 2) { ElMessage.warning('不能删除超级管理员'); return }
  try {
    await ElMessageBox.confirm('确定要删除该管理员吗? 删除后将恢复为普通用户', '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    await api.put(`/admin/users/${row.id}/role`, null, { params: { role: 0 } })
    ElMessage.success('已恢复为普通用户'); loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h1 { font-size: 24px; font-weight: 600; margin-bottom: 8px; }
.page-subtitle { color: #909399; font-size: 14px; }
.table-card { margin-bottom: 20px; }
.user-info { display: flex; align-items: center; gap: 12px; }
.user-detail { display: flex; flex-direction: column; }
.user-name { font-weight: 500; font-size: 14px; }
.user-email { font-size: 12px; color: #909399; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>

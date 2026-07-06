<template>
  <!-- 用户管理页面：搜索筛选 + 用户表格 + 编辑/查看对话框 -->
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>用户管理</h1>
      <p class="subtitle">管理平台所有注册用户</p>
    </div>

    <!-- 搜索筛选区 -->
    <el-card class="search-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="8">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户名/姓名/手机号"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <!-- 状态筛选：1=活跃, 0=未活跃 -->
          <el-select v-model="filterStatus" placeholder="状态筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="活跃" :value="1" />
            <el-option label="未活跃" :value="0" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 用户表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <!-- 用户信息列：头像 + 姓名/联系方式 -->
        <el-table-column label="用户信息" min-width="250">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="40">
                {{ row.realName?.charAt(0) || row.username?.charAt(0) }}
              </el-avatar>
              <div class="user-detail">
                <span class="user-name">{{ row.realName || row.username }}</span>
                <span class="user-email">{{ row.phone || row.email }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="building" label="楼栋" width="120" />
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <!-- 状态列：status=1活跃, status=0未活跃 -->
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '活跃' : '未活跃' }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 操作列：编辑/启用禁用/查看 -->
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="info" size="small" @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 编辑用户对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="editForm.realName" placeholder="请输入姓名" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="editForm.phone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="editForm.email" placeholder="请输入邮箱" /></el-form-item>
        <el-form-item label="楼栋"><el-input v-model="editForm.building" placeholder="请输入楼栋" /></el-form-item>
        <el-form-item label="房间号"><el-input v-model="editForm.room" placeholder="请输入房间号" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看用户详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="查看用户详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户名">{{ viewData.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ viewData.realName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ viewData.phone || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ viewData.email || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="楼栋">{{ viewData.building || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ viewData.room || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === 1 ? 'success' : 'info'">
            {{ viewData.status === 1 ? '活跃' : '未活跃' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ viewData.createdAt }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * Users.vue - 用户管理页面
 *
 * 功能说明：
 * - 分页展示所有注册用户列表
 * - 支持按关键字搜索（用户名/姓名/手机号）和状态筛选
 * - 用户状态：1=活跃, 0=未活跃
 * - 操作：编辑用户资料、启用/禁用切换、查看用户详情
 * - API 调用：getUserList(分页+筛选)、updateUser(编辑/切换状态)
 */

import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getUserList, updateUser } from '@/api/user'

/** 表格加载状态 */
const loading = ref(false)
/** 提交按钮加载状态 */
const submitLoading = ref(false)
/** 表格数据 */
const tableData = ref([])
/** 当前页码 */
const currentPage = ref(1)
/** 每页条数 */
const pageSize = ref(10)
/** 总条数 */
const total = ref(0)
/** 搜索关键字 */
const searchKeyword = ref('')
/** 状态筛选值：''=全部, 1=活跃, 0=未活跃 */
const filterStatus = ref('')

/** 编辑对话框是否显示 */
const editDialogVisible = ref(false)
/** 查看详情对话框是否显示 */
const viewDialogVisible = ref(false)
/** 编辑表单数据 */
const editForm = reactive({
  id: null,
  realName: '',
  phone: '',
  email: '',
  building: '',
  room: ''
})
/** 查看用的用户数据 */
const viewData = ref({})

/**
 * 加载用户列表
 * API: getUserList({ page, size, keyword?, status? })
 */
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    if (filterStatus.value !== '' && filterStatus.value !== null) {
      params.status = filterStatus.value
    }
    const res = await getUserList(params)
    tableData.value = res.data?.data?.records || []
    total.value = res.data?.data?.total || 0
  } catch (error) {
    console.error('加载用户数据失败:', error)
  } finally {
    loading.value = false
  }
}

/** 搜索：重置页码后加载 */
const handleSearch = () => { currentPage.value = 1; loadData() }
/** 重置搜索条件 */
const resetSearch = () => { searchKeyword.value = ''; filterStatus.value = ''; currentPage.value = 1; loadData() }
/** 改变每页条数 */
const handleSizeChange = (val) => { pageSize.value = val; loadData() }
/** 改变页码 */
const handleCurrentChange = (val) => { currentPage.value = val; loadData() }

/**
 * 打开编辑对话框并填充当前行数据
 * @param {Object} row - 用户行数据
 */
const handleEdit = (row) => {
  editForm.id = row.id; editForm.realName = row.realName || ''; editForm.phone = row.phone || ''
  editForm.email = row.email || ''; editForm.building = row.building || ''; editForm.room = row.room || ''
  editDialogVisible.value = true
}

/**
 * 提交编辑
 * API: updateUser(id, editForm)
 */
const handleEditSubmit = async () => {
  submitLoading.value = true
  try { await updateUser(editForm.id, editForm); ElMessage.success('更新成功'); editDialogVisible.value = false; loadData() }
  catch { ElMessage.error('更新失败') }
  finally { submitLoading.value = false }
}

/**
 * 查看用户详情
 * @param {Object} row - 用户行数据
 */
const handleView = (row) => { viewData.value = { ...row }; viewDialogVisible.value = true }

/**
 * 切换用户启用/禁用状态
 * status: 1→0 禁用, 0→1 启用
 * @param {Object} row - 用户行数据
 */
const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗?`, '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    await updateUser(row.id, { status: newStatus })
    ElMessage.success(`${action}成功`); loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(`${action}失败`)
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h1 { font-size: 24px; font-weight: 600; margin-bottom: 8px; }
.page-subtitle { color: #909399; font-size: 14px; }
.search-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
.user-info { display: flex; align-items: center; gap: 12px; }
.user-detail { display: flex; flex-direction: column; }
.user-name { font-weight: 500; font-size: 14px; }
.user-email { font-size: 12px; color: #909399; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>

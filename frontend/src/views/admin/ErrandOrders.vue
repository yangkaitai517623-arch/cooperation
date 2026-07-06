<template>
  <!-- 跑腿订单管理页面：搜索筛选 + 订单表格 + 详情对话框 -->
  <div class="page-container">
    <div class="page-header"><h1>跑腿订单管理</h1><p class="subtitle">管理跑腿服务订单</p></div>

    <!-- 搜索筛选区 -->
    <el-card class="search-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="8">
          <el-input v-model="searchKeyword" placeholder="搜索需求标题" clearable @clear="handleSearch" @keyup.enter="handleSearch">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <!-- 状态筛选：1=已接单, 2=进行中, 3=已完成 -->
          <el-select v-model="filterStatus" placeholder="状态筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="已接单" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已完成" :value="3" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 订单表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="title" label="需求标题" min-width="200" />
        <el-table-column prop="errandType" label="类型" width="100" />
        <el-table-column prop="reward" label="报酬" width="100" align="center">
          <template #default="{ row }"><span style="color: #E8A838; font-weight: 600;">¥{{ row.reward }}</span></template>
        </el-table-column>
        <!-- 状态列 -->
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }"><el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag></template>
        </el-table-column>
        <!-- 紧急程度列 -->
        <el-table-column label="紧急程度" width="100" align="center">
          <template #default="{ row }"><el-tag :type="getUrgencyType(row.urgency)">{{ getUrgencyText(row.urgency) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="180" />
        <el-table-column label="操作" min-width="180">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看详情</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage" v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]" :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange" @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="订单详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="需求标题">{{ currentRow.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentRow.errandType }}</el-descriptions-item>
        <el-descriptions-item label="取货地址">{{ currentRow.pickupAddress }}</el-descriptions-item>
        <el-descriptions-item label="送达地址">{{ currentRow.deliveryAddress }}</el-descriptions-item>
        <el-descriptions-item label="报酬">¥{{ currentRow.reward }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusText(currentRow.status) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * ErrandOrders.vue - 跑腿订单管理页面
 *
 * 功能说明：
 * - 分页展示已接单的跑腿订单
 * - 支持按标题搜索和状态筛选
 * - 订单状态：1=已接单, 2=进行中, 3=已完成
 * - 操作：查看详情、删除（需输入原因）
 * - API: GET /admin/errand-orders, deleteErrand(id, reason)
 */

import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import api from '@/api'
import { deleteErrand } from '@/api/errand'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const filterStatus = ref('')
const viewDialogVisible = ref(false)
const currentRow = ref({})

/** 状态映射 */
const getStatusType = (s) => ({ 1: 'warning', 2: 'primary', 3: 'success' }[s] || 'info')
const getStatusText = (s) => ({ 1: '已接单', 2: '进行中', 3: '已完成' }[s] || '未知')
/** 紧急程度映射 */
const getUrgencyType = (u) => ({ 1: 'danger', 2: 'warning', 3: 'info' }[u] || 'info')
const getUrgencyText = (u) => ({ 1: '紧急', 2: '一般', 3: '不急' }[u] || '未知')

/**
 * 加载订单列表
 * API: GET /admin/errand-orders?page={page}&size={size}
 */
const loadData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterStatus.value !== '' && filterStatus.value !== null) params.status = filterStatus.value
    const res = await api.get('/admin/errand-orders', { params })
    tableData.value = res.data?.data?.records || []
    total.value = res.data?.data?.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleSearch = () => { currentPage.value = 1; loadData() }
const resetSearch = () => { searchKeyword.value = ''; filterStatus.value = ''; currentPage.value = 1; loadData() }
const handleSizeChange = (v) => { pageSize.value = v; loadData() }
const handleCurrentChange = (v) => { currentPage.value = v; loadData() }
const handleView = (row) => { currentRow.value = row; viewDialogVisible.value = true }

/**
 * 删除订单
 * API: deleteErrand(id, reason)
 * @param {Object} row - 订单行数据
 */
const handleDelete = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入删除原因（将通知相关人员）', '删除订单', {
      confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning', inputPlaceholder: '请输入删除原因...'
    })
    await deleteErrand(row.id, reason); ElMessage.success('删除成功'); loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
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
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>

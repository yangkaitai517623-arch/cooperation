<template>
  <!-- 跑腿需求管理页面：搜索筛选 + 需求表格 + 分配接单人/查看详情/删除对话框 -->
  <div class="page-container">
    <div class="page-header"><h1>跑腿需求管理</h1><p class="subtitle">管理用户发布的跑腿需求</p></div>

    <!-- 搜索筛选区：标题搜索 + 状态 + 类型 + 紧急程度 -->
    <el-card class="search-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="8">
          <el-input v-model="searchKeyword" placeholder="搜索跑腿需求" clearable @clear="handleSearch" @keyup.enter="handleSearch">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <!-- 状态筛选：pending=待接单, in_progress=进行中, completed=已完成 -->
          <el-select v-model="filterStatus" placeholder="状态筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="待接单" value="pending" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <!-- 类型筛选：代买/代取/代送/排队/其他 -->
          <el-select v-model="filterType" placeholder="类型筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="代买" value="代买" />
            <el-option label="代取" value="代取" />
            <el-option label="代送" value="代送" />
            <el-option label="排队" value="排队" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <!-- 紧急程度筛选：1=紧急, 2=一般, 3=不急 -->
          <el-select v-model="filterUrgency" placeholder="紧急程度" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="紧急" value="1" />
            <el-option label="一般" value="2" />
            <el-option label="不急" value="3" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 需求表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column label="需求信息" min-width="250">
          <template #default="{ row }"><div class="errand-info"><span class="errand-title">{{ row.title }}</span><span class="errand-desc">{{ row.description }}</span></div></template>
        </el-table-column>
        <el-table-column prop="userId" label="发布者ID" width="100" />
        <el-table-column prop="errandType" label="类型" width="80" align="center">
          <template #default="{ row }"><el-tag>{{ row.errandType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="reward" label="报酬" width="100" align="center">
          <template #default="{ row }"><span class="price">¥{{ row.reward?.toFixed(2) }}</span></template>
        </el-table-column>
        <!-- 状态列：0=待接单, 1=已接单, 2=进行中, 3=已完成, 4=已取消 -->
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }"><el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag></template>
        </el-table-column>
        <!-- 紧急程度列：1=紧急, 2=一般, 3=不急 -->
        <el-table-column prop="urgency" label="紧急程度" width="100" align="center">
          <template #default="{ row }"><el-tag :type="getUrgencyType(row.urgency)">{{ getUrgencyText(row.urgency) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" min-width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看详情</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage" v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]" :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange" @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 分配接单人对话框 -->
    <el-dialog v-model="assignDialogVisible" title="分配接单人" width="400px">
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="选择接单人">
          <el-select v-model="assignForm.runnerId" placeholder="请选择接单人" style="width: 100%;">
            <el-option v-for="item in runnerList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看需求详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="需求详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="需求标题" :span="2">{{ currentRow.title }}</el-descriptions-item>
        <el-descriptions-item label="需求描述" :span="2">{{ currentRow.description }}</el-descriptions-item>
        <el-descriptions-item label="发布者">{{ currentRow.publisherName || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="需求类型">{{ currentRow.errandType }}</el-descriptions-item>
        <el-descriptions-item label="报酬"><span style="color: #E8A838; font-weight: 600;">¥{{ currentRow.reward }}</span></el-descriptions-item>
        <el-descriptions-item label="取货地址">{{ currentRow.pickupAddress || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="送达地址">{{ currentRow.deliveryAddress || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="紧急程度">{{ getUrgencyText(currentRow.urgency) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusText(currentRow.status) }}</el-descriptions-item>
        <el-descriptions-item label="接单人">{{ currentRow.runnerName || '未分配' }}</el-descriptions-item>
        <el-descriptions-item label="发布时间" :span="2">{{ currentRow.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * Errand.vue - 跑腿需求管理页面
 *
 * 功能说明：
 * - 分页展示所有跑腿需求
 * - 支持多维度筛选：关键字搜索、状态、类型、紧急程度
 * - 跑腿状态：0=待接单, 1=已接单, 2=进行中, 3=已完成, 4=已取消
 * - 跑腿类型：代买/代取/代送/排队/其他
 * - 紧急程度：1=紧急(danger), 2=一般(warning), 3=不急(info)
 * - 操作：查看详情、删除（需输入原因）
 * - API: getErrandList, assignRunner, getRunnerList, deleteErrand
 */

import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getErrandList, assignRunner, getRunnerList, deleteErrand } from '@/api/errand'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const filterStatus = ref('')
const filterType = ref('')
const filterUrgency = ref('')
const assignDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const currentRow = ref({})
const runnerList = ref([])

const assignForm = reactive({ errandId: null, runnerId: null })

/** 状态映射 */
const getStatusType = (status) => ({ 0: 'warning', 1: '', 2: 'primary', 3: 'success', 4: 'danger' }[status] || 'info')
const getStatusText = (status) => ({ 0: '待接单', 1: '已接单', 2: '进行中', 3: '已完成', 4: '已取消' }[status] || status)
/** 紧急程度映射 */
const getUrgencyType = (urgency) => ({ 1: 'danger', 2: 'warning', 3: 'info' }[urgency] || 'info')
const getUrgencyText = (urgency) => ({ 1: '紧急', 2: '一般', 3: '不急' }[urgency] || urgency)

/** 筛选器值映射 */
const statusMap = { 'pending': 0, 'in_progress': 2, 'completed': 3 }

/**
 * 加载跑腿需求列表
 * API: getErrandList({ page, size, keyword?, status?, errandType?, urgency? })
 */
const loadData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterStatus.value) params.status = statusMap[filterStatus.value]
    if (filterType.value) params.errandType = filterType.value
    if (filterUrgency.value) params.urgency = filterUrgency.value
    const res = await getErrandList(params)
    tableData.value = res.data?.data?.records || []
    total.value = res.data?.data?.total || 0
  } catch (error) { console.error('加载跑腿需求数据失败:', error) }
  finally { loading.value = false }
}

/** 加载接单人列表 */
const loadRunners = async () => {
  try { const res = await getRunnerList(); runnerList.value = res.data?.data?.records || [] }
  catch (error) { console.error('加载跑腿人列表失败:', error) }
}

const handleSearch = () => { currentPage.value = 1; loadData() }
const resetSearch = () => { searchKeyword.value = ''; filterStatus.value = ''; filterType.value = ''; filterUrgency.value = ''; currentPage.value = 1; loadData() }
const handleSizeChange = (val) => { pageSize.value = val; loadData() }
const handleCurrentChange = (val) => { currentPage.value = val; loadData() }

/** 打开分配接单人对话框 */
const handleAssign = (row) => { assignForm.errandId = row.id; assignForm.runnerId = null; assignDialogVisible.value = true }

/**
 * 提交分配接单人
 * API: assignRunner(errandId, runnerId)
 */
const handleAssignSubmit = async () => {
  if (!assignForm.runnerId) { ElMessage.warning('请选择接单人'); return }
  submitLoading.value = true
  try { await assignRunner(assignForm.errandId, assignForm.runnerId); ElMessage.success('分配成功'); assignDialogVisible.value = false; loadData() }
  catch (error) { console.error('分配失败:', error); ElMessage.error(error.response?.data?.message || '分配失败') }
  finally { submitLoading.value = false }
}

const handleView = (row) => { currentRow.value = row; viewDialogVisible.value = true }

/**
 * 删除跑腿需求
 * API: deleteErrand(id, reason)
 * @param {Object} row - 需求行数据
 */
const handleDelete = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入删除原因（将通知发布者）', '删除需求', {
      confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning', inputPlaceholder: '请输入删除原因...'
    })
    await deleteErrand(row.id, reason); ElMessage.success('删除成功，已通知发布者'); loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

onMounted(() => { loadData(); loadRunners() })
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h1 { margin: 0 0 8px 0; font-size: 24px; font-weight: 600; }
.page-header .subtitle { margin: 0; color: #909399; font-size: 14px; }
.search-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
.errand-info { display: flex; flex-direction: column; }
.errand-title { font-weight: 500; color: #303133; }
.errand-desc { font-size: 12px; color: #909399; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.price { color: #f56c6c; font-weight: 600; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>

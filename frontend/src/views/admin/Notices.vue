<template>
  <!-- 通知管理页面：搜索筛选 + 通知表格 + 发送通知对话框 -->
  <div class="page-container">
    <div class="page-header">
      <div class="header-left"><h1>通知管理</h1><p class="subtitle">管理系统通知消息</p></div>
      <div class="header-right"><el-button type="primary" @click="handleCreate"><el-icon><Plus /></el-icon> 发送通知</el-button></div>
    </div>

    <!-- 搜索筛选区 -->
    <el-card class="search-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="8">
          <el-input v-model="searchKeyword" placeholder="搜索通知标题" clearable @clear="handleSearch" @keyup.enter="handleSearch">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <!-- 类型筛选：system=系统通知, order=订单通知, activity=活动通知 -->
          <el-select v-model="filterType" placeholder="类型筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="系统通知" value="system" />
            <el-option label="订单通知" value="order" />
            <el-option label="活动通知" value="activity" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 通知表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="200">
          <template #default="{ row }"><div class="content-cell">{{ row.content }}</div></template>
        </el-table-column>
        <!-- 类型列：1=系统通知, 2=订单通知, 3=活动通知 -->
        <el-table-column prop="type" label="类型" width="120" align="center">
          <template #default="{ row }"><el-tag>{{ getTypeText(row.type) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="receiver" label="接收者" width="120">
          <template #default="{ row }"><span>{{ row.receiver || '全部用户' }}</span></template>
        </el-table-column>
        <!-- 已读状态 -->
        <el-table-column prop="isRead" label="已读状态" width="100" align="center">
          <template #default="{ row }"><el-tag :type="row.isRead ? 'success' : 'warning'">{{ row.isRead ? '已读' : '未读' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }"><el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button></template>
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

    <!-- 发送通知对话框 -->
    <el-dialog v-model="dialogVisible" title="发送通知" width="600px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="通知标题" prop="title"><el-input v-model="formData.title" placeholder="请输入通知标题" /></el-form-item>
        <!-- 通知类型：system/order/activity -->
        <el-form-item label="通知类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择通知类型" style="width: 100%;">
            <el-option label="系统通知" value="system" />
            <el-option label="订单通知" value="order" />
            <el-option label="活动通知" value="activity" />
          </el-select>
        </el-form-item>
        <el-form-item label="接收者"><el-input v-model="formData.receiver" placeholder="留空则发送给全部用户" /></el-form-item>
        <el-form-item label="通知内容" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="4" placeholder="请输入通知内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * Notices.vue - 通知管理页面
 *
 * 功能说明：
 * - 分页展示所有系统通知
 * - 支持按标题搜索和类型筛选
 * - 通知类型：1=系统通知, 2=订单通知, 3=活动通知
 * - 操作：发送新通知（可选择接收者）、删除通知
 * - 发送时：type 和 userId 需转换为数字类型传给后端
 * - API: getNoticeList, createNotice, deleteNotice
 */

import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getNoticeList, createNotice, deleteNotice } from '@/api/notice'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const filterType = ref('')
const dialogVisible = ref(false)
const formRef = ref(null)

/** 发送通知表单 */
const formData = reactive({ title: '', type: 'system', receiver: '', content: '' })

/** 表单校验规则：标题、类型、内容为必填 */
const formRules = {
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }]
}

/**
 * 获取类型文本
 * 后端 type 为数字：1=系统通知, 2=订单通知, 3=活动通知
 */
const getTypeText = (type) => ({ 1: '系统通知', 2: '订单通知', 3: '活动通知' }[type] || type)

/** 类型值映射（前端字符串 → 后端数字） */
const typeMap = { 'system': 1, 'order': 2, 'activity': 3 }

/**
 * 加载通知列表
 * API: getNoticeList({ page, size, keyword?, type? })
 */
const loadData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterType.value) params.type = typeMap[filterType.value]
    const res = await getNoticeList(params)
    tableData.value = res.data?.data?.records || []
    total.value = res.data?.data?.total || 0
  } catch (error) { console.error('加载通知数据失败:', error) }
  finally { loading.value = false }
}

const handleSearch = () => { currentPage.value = 1; loadData() }
const resetSearch = () => { searchKeyword.value = ''; filterType.value = ''; currentPage.value = 1; loadData() }
const handleSizeChange = (val) => { pageSize.value = val; loadData() }
const handleCurrentChange = (val) => { currentPage.value = val; loadData() }

/** 重置发送表单 */
const resetForm = () => { formData.title = ''; formData.type = 'system'; formData.receiver = ''; formData.content = '' }

/** 打开发送通知对话框 */
const handleCreate = () => { resetForm(); dialogVisible.value = true }

/**
 * 删除通知
 * API: deleteNotice(id)
 * @param {Object} row - 通知行数据
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该通知吗?', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deleteNotice(row.id); ElMessage.success('删除成功'); loadData()
  } catch (error) { if (error !== 'cancel') console.error('删除失败:', error) }
}

/**
 * 发送通知
 * API: createNotice({ title, content, type: number, userId: number|null })
 * type 需从字符串转换为数字，receiver 留空表示发送给全部用户(userId=null)
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const sendData = {
        title: formData.title,
        content: formData.content,
        type: typeMap[formData.type] || 1,
        userId: formData.receiver ? parseInt(formData.receiver) : null
      }
      await createNotice(sendData); ElMessage.success('发送成功'); dialogVisible.value = false; loadData()
    } catch (error) { console.error('发送失败:', error) }
    finally { submitLoading.value = false }
  })
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h1 { margin: 0 0 8px 0; font-size: 24px; font-weight: 600; }
.page-header .subtitle { margin: 0; color: #909399; font-size: 14px; }
.search-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
.content-cell { max-height: 60px; overflow: hidden; text-overflow: ellipsis; color: #606266; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>

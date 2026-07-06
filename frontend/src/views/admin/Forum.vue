<template>
  <!-- 论坛管理页面：搜索筛选 + 帖子表格 + 详情对话框 -->
  <div class="page-container">
    <div class="page-header"><h1>资讯论坛管理</h1><p class="subtitle">管理论坛帖子内容</p></div>

    <!-- 搜索筛选区 -->
    <el-card class="search-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="8">
          <el-input v-model="searchKeyword" placeholder="搜索帖子标题" clearable @clear="handleSearch" @keyup.enter="handleSearch">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <!-- 状态筛选：pending=审核中, published=已发布, off_shelf=已下架 -->
          <el-select v-model="filterStatus" placeholder="状态筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="审核中" value="pending" />
            <el-option label="已发布" value="published" />
            <el-option label="已下架" value="off_shelf" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 帖子表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="userId" label="发布者ID" width="100" />
        <el-table-column prop="viewCount" label="浏览数" width="100" align="center" />
        <el-table-column prop="commentCount" label="评论数" width="100" align="center" />
        <!-- 状态列：0=审核中, 1=已发布, 2=已下架 -->
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }"><el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="170" />
        <!-- 操作列：审核/下架/删除 -->
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'pending'" type="success" size="small" @click="handleAudit(row)">审核</el-button>
            <el-button v-if="row.status === 'published'" type="warning" size="small" @click="handleOffShelf(row)">下架</el-button>
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

    <!-- 帖子详情对话框 -->
    <el-dialog v-model="dialogVisible" title="帖子详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="帖子标题" :span="2">{{ currentRow.title }}</el-descriptions-item>
        <el-descriptions-item label="发布者ID">{{ currentRow.userId }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusText(currentRow.status) }}</el-descriptions-item>
        <el-descriptions-item label="浏览数">{{ currentRow.viewCount }}</el-descriptions-item>
        <el-descriptions-item label="评论数">{{ currentRow.commentCount }}</el-descriptions-item>
        <el-descriptions-item label="发布时间" :span="2">{{ currentRow.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="帖子内容" :span="2">
          <div class="post-content">{{ currentRow.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * Forum.vue - 论坛管理页面
 *
 * 功能说明：
 * - 分页展示所有论坛帖子
 * - 支持按标题搜索和状态筛选
 * - 帖子状态：0=审核中, 1=已发布, 2=已下架
 * - 操作：审核通过(status→1)、下架(status→2)、删除
 * - API: getForumList, auditPost, offShelfPost, deletePost
 */

import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getForumList, auditPost, offShelfPost, deletePost } from '@/api/forum'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const filterStatus = ref('')
const dialogVisible = ref(false)
const currentRow = ref({})

/** 状态映射：Tag类型 */
const getStatusType = (status) => ({ 0: 'success', 1: 'warning', 2: 'info' }[status] || 'info')
/** 状态文本：0=审核中, 1=已发布, 2=已下架 */
const getStatusText = (status) => ({ 0: '已发布', 1: '审核中', 2: '已下架' }[status] || status)

/** 筛选器值映射 */
const statusMap = { 'pending': 0, 'published': 1, 'off_shelf': 2 }

/**
 * 加载帖子列表
 * API: getForumList({ page, size, keyword?, status? })
 */
const loadData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterStatus.value) params.status = statusMap[filterStatus.value]
    const res = await getForumList(params)
    tableData.value = res.data?.data?.records || []
    total.value = res.data?.data?.total || 0
  } catch (error) { console.error('加载论坛数据失败:', error) }
  finally { loading.value = false }
}

const handleSearch = () => { currentPage.value = 1; loadData() }
const resetSearch = () => { searchKeyword.value = ''; filterStatus.value = ''; currentPage.value = 1; loadData() }
const handleSizeChange = (val) => { pageSize.value = val; loadData() }
const handleCurrentChange = (val) => { currentPage.value = val; loadData() }
const handleView = (row) => { currentRow.value = row; dialogVisible.value = true }

/**
 * 审核通过帖子
 * API: auditPost(id) — 帖子状态变为已发布(1)
 * @param {Object} row - 帖子行数据
 */
const handleAudit = async (row) => {
  try {
    await ElMessageBox.confirm('确定要审核通过该帖子吗?', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' })
    await auditPost(row.id); ElMessage.success('审核通过'); loadData()
  } catch (error) { if (error !== 'cancel') console.error('审核失败:', error) }
}

/**
 * 下架帖子
 * API: offShelfPost(id) — 帖子状态变为已下架(2)
 * @param {Object} row - 帖子行数据
 */
const handleOffShelf = async (row) => {
  try {
    await ElMessageBox.confirm('确定要下架该帖子吗?', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await offShelfPost(row.id); ElMessage.success('下架成功'); loadData()
  } catch (error) { if (error !== 'cancel') console.error('下架失败:', error) }
}

/**
 * 删除帖子
 * API: deletePost(id)
 * @param {Object} row - 帖子行数据
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该帖子吗? 删除后不可恢复', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deletePost(row.id); ElMessage.success('删除成功'); loadData()
  } catch (error) { if (error !== 'cancel') console.error('删除失败:', error) }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h1 { margin: 0 0 8px 0; font-size: 24px; font-weight: 600; }
.page-header .subtitle { margin: 0; color: #909399; font-size: 14px; }
.search-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
.post-content { max-height: 200px; overflow-y: auto; line-height: 1.6; color: #606266; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>

<template>
  <!-- 评论管理页面：搜索筛选 + 评论表格 -->
  <div class="page-container">
    <div class="page-header"><h1>评论管理</h1><p class="subtitle">管理论坛帖子评论</p></div>

    <!-- 搜索筛选区 -->
    <el-card class="search-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="8">
          <el-input v-model="searchKeyword" placeholder="搜索评论内容" clearable @clear="handleSearch" @keyup.enter="handleSearch">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <!-- 状态筛选：pending=审核中, published=已发布 -->
          <el-select v-model="filterStatus" placeholder="状态筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="审核中" value="pending" />
            <el-option label="已发布" value="published" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 评论表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="content" label="评论内容" min-width="250">
          <template #default="{ row }"><div class="comment-content"><span>{{ row.content }}</span></div></template>
        </el-table-column>
        <el-table-column prop="userName" label="评论者" width="120" />
        <!-- 所属帖子链接 -->
        <el-table-column prop="postTitle" label="所属帖子" width="180">
          <template #default="{ row }"><el-link type="primary" :underline="false">{{ row.postTitle }}</el-link></template>
        </el-table-column>
        <el-table-column prop="likeCount" label="点赞数" width="80" align="center" />
        <!-- 状态列：0=审核中, 1=已发布 -->
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }"><el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="170" />
        <!-- 操作列：审核/删除 -->
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'pending'" type="success" size="small" @click="handleAudit(row)">审核</el-button>
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
  </div>
</template>

<script setup>
/**
 * Comments.vue - 评论管理页面
 *
 * 功能说明：
 * - 分页展示所有论坛评论
 * - 支持按内容搜索和状态筛选
 * - 评论状态：0=审核中, 1=已发布
 * - 操作：审核通过(status→1)、删除
 * - API: getCommentList, auditComment, deleteComment
 */

import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getCommentList, auditComment, deleteComment } from '@/api/comment'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const filterStatus = ref('')

/** 状态映射：0=审核中(warning), 1=已发布(success) */
const getStatusType = (status) => ({ 0: 'warning', 1: 'success' }[status] || 'info')
const getStatusText = (status) => ({ 0: '审核中', 1: '已发布' }[status] || status)

/** 筛选器值映射 */
const statusMap = { 'pending': 0, 'published': 1 }

/**
 * 加载评论列表
 * API: getCommentList({ page, size, keyword?, status? })
 */
const loadData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterStatus.value) params.status = statusMap[filterStatus.value]
    const res = await getCommentList(params)
    tableData.value = res.data?.data?.records || []
    total.value = res.data?.data?.total || 0
  } catch (error) { console.error('加载评论数据失败:', error) }
  finally { loading.value = false }
}

const handleSearch = () => { currentPage.value = 1; loadData() }
const resetSearch = () => { searchKeyword.value = ''; filterStatus.value = ''; currentPage.value = 1; loadData() }
const handleSizeChange = (val) => { pageSize.value = val; loadData() }
const handleCurrentChange = (val) => { currentPage.value = val; loadData() }

/**
 * 审核通过评论
 * API: auditComment(id) — 评论状态变为已发布(1)
 * @param {Object} row - 评论行数据
 */
const handleAudit = async (row) => {
  try {
    await ElMessageBox.confirm('确定要审核通过该评论吗?', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' })
    await auditComment(row.id); ElMessage.success('审核通过'); loadData()
  } catch (error) { if (error !== 'cancel') console.error('审核失败:', error) }
}

/**
 * 删除评论
 * API: deleteComment(id)
 * @param {Object} row - 评论行数据
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该评论吗? 删除后不可恢复', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deleteComment(row.id); ElMessage.success('删除成功'); loadData()
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
.comment-content { max-height: 60px; overflow: hidden; text-overflow: ellipsis; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>

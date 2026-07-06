<template>
  <!-- 邻里广场（论坛）页面：帖子列表 + 发帖对话框 + 点赞功能 -->
  <div class="forum-page">
    <!-- 页面头部 -->
    <header class="page-header">
      <div><h2>邻里广场</h2><p>和邻居们聊聊天</p></div>
      <el-button type="primary" @click="dlg=true"><el-icon><Edit /></el-icon> 发帖</el-button>
    </header>

    <!-- 帖子列表卡片 -->
    <el-card shadow="never" class="list-card">
      <template #header><span>帖子列表</span></template>
      <div class="post-list">
        <div v-for="(p, i) in posts" :key="p.id" class="post-card" :style="{ '--i': i }">
          <!-- 帖子头部：作者头像 + 名称 + 时间 -->
          <div class="post-header">
            <div class="post-avatar">{{ p.authorName?.[0] || '用' }}</div>
            <div class="post-meta"><strong>{{ p.authorName }}</strong><time>{{ p.createdAt }}</time></div>
          </div>
          <!-- 帖子标题：点击进入详情页 -->
          <h3 class="post-title" @click="router.push(`/forum/${p.id}`)">{{ p.title }}</h3>
          <!-- 帖子内容：最多显示3行 -->
          <p class="post-body">{{ p.content }}</p>
          <!-- 帖子底部：浏览数 + 评论数 + 点赞（可切换） -->
          <div class="post-footer">
            <!-- 浏览量 -->
            <span>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="1.8"/><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.8"/></svg>
              {{ p.viewCount || 0 }}
            </span>
            <!-- 评论数：点击进入详情页 -->
            <span @click="router.push(`/forum/${p.id}`)">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/></svg>
              {{ p.commentCount || 0 }}
            </span>
            <!-- 点赞按钮：已点赞时填充红色 -->
            <span :class="{ 'liked': likedPosts.has(p.id) }" @click="toggleLike(p)">
              <svg width="16" height="16" viewBox="0 0 24 24" :fill="likedPosts.has(p.id) ? 'currentColor' : 'none'"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/></svg>
              {{ p.likeCount || 0 }}
            </span>
          </div>
        </div>
        <!-- 空状态 -->
        <EmptyState v-if="posts.length===0" text="还没有帖子" />
      </div>
    </el-card>

    <!-- 发帖对话框 -->
    <el-dialog v-model="dlg" title="发帖" width="520px">
      <el-form :model="pf" label-width="80px">
        <el-form-item label="标题"><el-input v-model="pf.title" placeholder="起个标题" /></el-form-item>
        <!-- 帖子内容：最多2000字 -->
        <el-form-item label="内容"><el-input v-model="pf.content" type="textarea" :rows="6" placeholder="分享想法..." maxlength="2000" show-word-limit /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" :loading="ld" @click="doPost">发布</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * Forum.vue - 邻里广场（论坛）页面
 *
 * 功能说明：
 * - 展示帖子列表（作者、标题、内容摘要、浏览量、评论数、点赞数）
 * - 支持发布新帖（标题 + 内容，最多2000字）
 * - 支持点赞/取消点赞（使用本地 Set 记录已点赞帖子）
 * - 点击标题或评论数跳转到帖子详情页
 */

import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import EmptyState from '@/components/EmptyState.vue'
import api from '@/api'
import { likePost, unlikePost } from '@/api/forum'

/** Vue Router 实例 */
const router = useRouter()

/** 帖子列表数据 */
const posts = ref([])
/** 发帖对话框是否显示 */
const dlg = ref(false)
/** 发帖按钮加载状态 */
const ld = ref(false)
/** 发帖表单数据 */
const pf = reactive({ title: '', content: '' })
/** 已点赞的帖子ID集合：用于本地追踪点赞状态 */
const likedPosts = ref(new Set())

/**
 * 加载帖子列表（最近20条）
 * API: GET /forum/posts?page=1&size=20
 */
async function loadPosts () { try { const { data } = await api.get('/forum/posts', { params: { page: 1, size: 20 } }); if (data.code === 200) posts.value = data.data?.records || [] } catch {} }

/**
 * 发布新帖
 * API: POST /forum/posts { title, content }
 */
async function doPost () { if (!pf.title || !pf.content) { ElMessage.warning('请填写标题和内容'); return }; ld.value = true; try { const { data } = await api.post('/forum/posts', pf); if (data.code === 200) { ElMessage.success('发布成功'); dlg.value = false; pf.title = ''; pf.content = ''; loadPosts() } } catch {} finally { ld.value = false } }

/**
 * 切换帖子的点赞状态
 * 已点赞 → 调用 unlikePost API
 * 未点赞 → 调用 likePost API
 * @param {Object} post - 帖子对象
 */
async function toggleLike (post) {
  try {
    const isLiked = likedPosts.value.has(post.id)
    if (isLiked) {
      const { data } = await unlikePost(post.id)
      if (data.code === 200) {
        post.likeCount = data.data
        likedPosts.value.delete(post.id)
        ElMessage.success('已取消点赞')
      }
    } else {
      const { data } = await likePost(post.id)
      if (data.code === 200) {
        post.likeCount = data.data
        likedPosts.value.add(post.id)
        ElMessage.success('点赞成功')
      }
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 页面挂载时加载帖子列表
onMounted(loadPosts)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: var(--s6); }
.page-header h2 { font-size: var(--fs-xl); font-weight: var(--fw-700); margin: 0; color: var(--ink); }
.page-header p { font-size: var(--fs-sm); color: var(--ink-muted); margin: 2px 0 0; }

.list-card { margin-bottom: var(--s4); }
.list-card :deep(.el-card__header span) { font-weight: var(--fw-600); color: var(--ink); }

.post-list { display: flex; flex-direction: column; }
.post-card { padding: var(--s4) 0; border-bottom: 1px solid var(--bg-soft); }
.post-card:last-child { border-bottom: none; }
.post-header { display: flex; align-items: center; gap: var(--s3); margin-bottom: var(--s3); }
.post-avatar { width: 38px; height: 38px; border-radius: var(--r-md); background: var(--brand-bg); color: var(--brand); display: grid; place-items: center; font-size: var(--fs-sm); font-weight: var(--fw-700); flex-shrink: 0; }
.post-meta strong { display: block; font-size: var(--fs-base); font-weight: var(--fw-600); color: var(--ink); }
.post-meta time { font-size: var(--fs-xs); color: var(--ink-faint); }
.post-title { font-size: var(--fs-md); font-weight: var(--fw-600); margin: 0 0 var(--s2); color: var(--ink); cursor: pointer; }
.post-title:hover { color: var(--brand); }
.post-body { font-size: var(--fs-base); color: var(--ink-muted); line-height: 1.6; margin: 0 0 var(--s4); display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.post-footer { display: flex; gap: var(--s5); font-size: var(--fs-sm); color: var(--ink-muted); }
.post-footer span { display: flex; align-items: center; gap: 5px; cursor: pointer; transition: color var(--fast); }
.post-footer span:hover { color: var(--brand); }
.post-footer span.liked { color: var(--brand); }
</style>

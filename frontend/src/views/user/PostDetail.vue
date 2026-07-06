<template>
  <!-- 帖子详情页：返回按钮 + 帖子内容 + 评论输入框 + 评论列表 + 点赞功能 -->
  <div class="post-detail-page">
    <!-- 页面返回头部 -->
    <el-page-header @back="$router.back()">
      <template #content>
        <span>帖子详情</span>
      </template>
    </el-page-header>

    <!-- 帖子内容卡片 -->
    <el-card v-if="post" class="post-card" shadow="never">
      <!-- 作者信息 -->
      <div class="post-header">
        <div class="post-avatar">{{ post.authorName?.[0] || '用' }}</div>
        <div class="post-meta">
          <strong>{{ post.authorName }}</strong>
          <time>{{ post.createdAt }}</time>
        </div>
      </div>
      <h1 class="post-title">{{ post.title }}</h1>
      <p class="post-body">{{ post.content }}</p>
      <!-- 帖子底部操作栏：浏览数 + 点赞 + 评论数 -->
      <div class="post-footer">
        <span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="1.8"/><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.8"/></svg>
          {{ post.viewCount || 0 }}
        </span>
        <!-- 帖子点赞按钮 -->
        <span :class="{ 'liked': isLiked }" @click="toggleLikePost">
          <svg width="16" height="16" viewBox="0 0 24 24" :fill="isLiked ? 'currentColor' : 'none'"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/></svg>
          {{ post.likeCount || 0 }}
        </span>
        <span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/></svg>
          {{ comments.length }}
        </span>
      </div>
    </el-card>

    <!-- 评论输入区 -->
    <el-card class="comment-input-card" shadow="never">
      <el-input
        v-model="commentContent"
        type="textarea"
        :rows="3"
        placeholder="写下你的评论..."
        maxlength="500"
        show-word-limit
      />
      <div class="comment-actions">
        <el-button type="primary" :loading="commentLoading" @click="submitComment">发表评论</el-button>
      </div>
    </el-card>

    <!-- 评论列表 -->
    <el-card class="comments-card" shadow="never">
      <template #header>
        <span>评论 ({{ comments.length }})</span>
      </template>
      <div v-if="comments.length === 0" class="no-comments">暂无评论，快来抢沙发吧~</div>
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <!-- 评论者信息 -->
        <div class="comment-header">
          <div class="comment-avatar">{{ comment.userName?.[0] || '用' }}</div>
          <div class="comment-meta">
            <strong>{{ comment.userName || '匿名用户' }}</strong>
            <time>{{ comment.createdAt }}</time>
          </div>
        </div>
        <p class="comment-content">{{ comment.content }}</p>
        <!-- 评论点赞按钮 -->
        <div class="comment-footer">
          <span
            :class="{ 'liked': likedComments.has(comment.id) }"
            @click="toggleLikeComment(comment)"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" :fill="likedComments.has(comment.id) ? 'currentColor' : 'none'"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/></svg>
            {{ comment.likeCount || 0 }}
          </span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
/**
 * PostDetail.vue - 帖子详情页面
 *
 * 功能说明：
 * - 展示帖子完整内容（标题、正文、作者、时间、浏览量）
 * - 展示帖子下的评论列表
 * - 支持发表评论（最多500字）
 * - 支持对帖子和评论点赞/取消点赞
 * - 路由参数：:id（帖子ID）来自 /forum/:id
 */

import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'
import { likePost, unlikePost, likeComment, unlikeComment } from '@/api/forum'

/** Vue Router 当前路由实例 */
const route = useRoute()
/** 从路由参数获取帖子ID */
const postId = route.params.id

/** 帖子详情数据 */
const post = ref(null)
/** 评论列表数据 */
const comments = ref([])
/** 评论输入内容 */
const commentContent = ref('')
/** 评论提交加载状态 */
const commentLoading = ref(false)
/** 当前帖子是否已点赞 */
const isLiked = ref(false)
/** 已点赞的评论ID集合：用于本地追踪点赞状态 */
const likedComments = ref(new Set())

/**
 * 加载帖子详情
 * API: GET /forum/posts/{id}
 * 获取后会自动增加浏览量
 */
async function loadPost () {
  try {
    const { data } = await api.get(`/forum/posts/${postId}`)
    if (data.code === 200) {
      post.value = data.data
    }
  } catch (e) {
    console.error('加载帖子失败:', e)
  }
}

/**
 * 加载帖子评论列表
 * API: GET /forum/posts/{id}/comments
 */
async function loadComments () {
  try {
    const { data } = await api.get(`/forum/posts/${postId}/comments`)
    if (data.code === 200) {
      comments.value = data.data || []
    }
  } catch (e) {
    console.error('加载评论失败:', e)
  }
}

/**
 * 提交评论
 * API: POST /forum/comments { postId, content }
 */
async function submitComment () {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  commentLoading.value = true
  try {
    const { data } = await api.post('/forum/comments', {
      postId: postId,
      content: commentContent.value
    })
    if (data.code === 200) {
      ElMessage.success('评论成功')
      commentContent.value = ''
      loadComments()
    }
  } catch (e) {
    ElMessage.error('评论失败')
  } finally {
    commentLoading.value = false
  }
}

/**
 * 切换帖子的点赞状态
 * 已点赞 → 调用 unlikePost API
 * 未点赞 → 调用 likePost API
 */
async function toggleLikePost () {
  try {
    if (isLiked.value) {
      const { data } = await unlikePost(postId)
      if (data.code === 200) {
        post.value.likeCount = data.data
        isLiked.value = false
        ElMessage.success('已取消点赞')
      }
    } else {
      const { data } = await likePost(postId)
      if (data.code === 200) {
        post.value.likeCount = data.data
        isLiked.value = true
        ElMessage.success('点赞成功')
      }
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

/**
 * 切换评论的点赞状态
 * 已点赞 → 调用 unlikeComment API
 * 未点赞 → 调用 likeComment API
 * @param {Object} comment - 评论对象
 */
async function toggleLikeComment (comment) {
  try {
    const isCommentLiked = likedComments.value.has(comment.id)
    if (isCommentLiked) {
      const { data } = await unlikeComment(comment.id)
      if (data.code === 200) {
        comment.likeCount = data.data
        likedComments.value.delete(comment.id)
        ElMessage.success('已取消点赞')
      }
    } else {
      const { data } = await likeComment(comment.id)
      if (data.code === 200) {
        comment.likeCount = data.data
        likedComments.value.add(comment.id)
        ElMessage.success('点赞成功')
      }
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 页面挂载时加载帖子详情和评论列表
onMounted(() => {
  loadPost()
  loadComments()
})
</script>

<style scoped>
.post-detail-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.post-card {
  margin-top: 20px;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.post-avatar {
  width: 42px;
  height: 42px;
  border-radius: 8px;
  background: #ecf5ff;
  color: #409eff;
  display: grid;
  place-items: center;
  font-size: 14px;
  font-weight: 700;
}

.post-meta strong {
  display: block;
  font-size: 15px;
  color: #303133;
}

.post-meta time {
  font-size: 12px;
  color: #909399;
}

.post-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 12px;
  color: #303133;
}

.post-body {
  font-size: 15px;
  color: #606266;
  line-height: 1.8;
  margin: 0 0 20px;
}

.post-footer {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #909399;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.post-footer span {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: color 0.2s;
}

.post-footer span:hover {
  color: #409eff;
}

.post-footer span.liked {
  color: #409eff;
}

.comment-input-card {
  margin-top: 16px;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.comments-card {
  margin-top: 16px;
}

.no-comments {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.comment-avatar {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: #f0f9eb;
  color: #67c23a;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
}

.comment-meta strong {
  font-size: 14px;
  color: #303133;
}

.comment-meta time {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.comment-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

.comment-footer {
  margin-top: 8px;
}

.comment-footer span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
  cursor: pointer;
  transition: color 0.2s;
}

.comment-footer span:hover {
  color: #409eff;
}

.comment-footer span.liked {
  color: #409eff;
}
</style>

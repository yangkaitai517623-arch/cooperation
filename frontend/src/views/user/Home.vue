<template>
  <!-- 用户首页：统计卡片 + 服务入口 + 通知列表 + 动态列表 -->
  <div class="home-page">
    <!-- 页面头部 -->
    <header class="page-header"><h2>首页</h2><p>欢迎回来，{{ userName }}</p></header>

    <!-- 统计卡片区：我的订单、已发布、已完成、未读消息 -->
    <div class="stat-grid">
      <div v-for="(s, i) in stats" :key="s.label" class="stat-card" :style="{ '--i': i }" @click="router.push(s.path)">
        <div class="stat-icon" :style="{ background: s.bg }">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" :style="{ color: s.color }" v-html="s.svg"></svg>
        </div>
        <div class="stat-body">
          <div class="stat-label">{{ s.label }}</div>
          <div class="stat-value">{{ s.value }}</div>
        </div>
      </div>
    </div>

    <!-- 服务入口卡片：跑腿帮忙、检修服务、闲置好物、邻里广场 -->
    <div class="func-grid">
      <div v-for="s in services" :key="s.path" class="func-card" @click="$router.push(s.path)">
        <div class="func-icon" :style="{ background: s.bg }">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" :style="{ color: s.color }" v-html="s.svg"></svg>
        </div>
        <div class="func-body">
          <strong>{{ s.title }}</strong>
          <span>{{ s.desc }}</span>
        </div>
        <svg class="func-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M9 18l6-6-6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </div>
    </div>

    <!-- 底部两栏布局 -->
    <div class="bottom-grid">
      <!-- 左栏：最新通知（最近5条） -->
      <el-card shadow="never" class="notice-card">
        <template #header>
          <div class="card-head">
            <span>最新通知</span>
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="head-badge">
              <el-button type="primary" size="small" @click="showAllNotices">查看全部</el-button>
            </el-badge>
          </div>
        </template>
        <div v-if="notices.length" class="notice-list">
          <div v-for="n in notices.slice(0, 5)" :key="n.id" class="notice-item" :class="{ unread: !n.isRead }">
            <div class="notice-dot" :class="{ unread: !n.isRead }"></div>
            <div class="notice-body">
              <strong>{{ n.title }}</strong>
              <p>{{ n.content }}</p>
              <time>{{ n.createdAt }}</time>
            </div>
          </div>
        </div>
        <div v-else class="empty-hint">暂无通知</div>
      </el-card>

      <!-- 右栏：我的动态（最近5条，含跑腿和检修订单） -->
      <el-card shadow="never" class="activity-card">
        <template #header>
          <div class="card-head">
            <span>我的动态</span>
            <el-button type="primary" size="small" @click="$router.push('/profile')">查看全部</el-button>
          </div>
        </template>
        <div v-if="activities.length" class="activity-list">
          <div v-for="a in activities" :key="a.id + a.type" class="activity-item" @click="$router.push(a.link)">
            <div class="activity-icon" :style="{ background: a.bg, color: a.color }">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" v-html="a.svg"></svg>
            </div>
            <div class="activity-body">
              <strong>{{ a.title }}</strong>
              <span>{{ a.typeLabel }} · {{ a.time }}</span>
            </div>
            <!-- 订单状态标签 -->
            <el-tag :type="a.tagType" size="small">{{ a.statusLabel }}</el-tag>
          </div>
        </div>
        <div v-else class="empty-hint">暂无动态，去发布一个需求吧</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
/**
 * Home.vue - 用户首页仪表盘
 *
 * 功能说明：
 * - 展示用户的统计摘要（我的订单数、已发布数、已完成数、未读消息数）
 * - 提供四大功能入口卡片（跑腿、检修、闲置、论坛）
 * - 底部两栏显示最新通知和最近动态（合并跑腿和检修订单）
 * - 支持点击跳转到对应页面
 */

import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import api from '@/api'

/** Vue Router 实例 */
const router = useRouter()
/** Pinia 用户状态管理 */
const store = useUserStore()
/** 当前登录用户名（计算属性） */
const userName = computed(() => store.user?.realName || '用户')

/** 通知列表 */
const notices = ref([])
/** 活动/动态列表 */
const activities = ref([])
/** 未读通知数量 */
const unreadCount = ref(0)
/** 用户统计：订单数、已发布数、已完成数 */
const myStats = ref({ orders: 0, publish: 0, completed: 0 })

/** 跑腿图标 SVG 路径 */
const svgPos = 'M14.7 6.3a1 1 0 000 1.4l1.6 1.6a1 1 0 001.4 0l3.77-3.77a6 6 0 01-7.94 7.94l-6.91 6.91a2.12 2.12 0 01-3-3l6.91-6.91a6 6 0 017.94-7.94l-3.76 3.76z'

/**
 * 统计卡片数据（计算属性）
 * 包含：我的订单、已发布、已完成、未读消息
 * 每个卡片点击后跳转到对应路径
 */
const stats = computed(() => [
  { label: '我的订单', value: myStats.value.orders, path: '/profile?scroll=orders', svg: '<path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round" stroke-linecap="round"/>', bg: 'rgba(14,165,233,.1)', color: '#0ea5e9' },
  { label: '已发布', value: myStats.value.publish, path: '/profile?scroll=orders', svg: '<path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>', bg: 'rgba(249,115,22,.1)', color: '#f97316' },
  { label: '已完成', value: myStats.value.completed, path: '/profile?scroll=orders', svg: '<path d="M22 11.08V12a10 10 0 11-5.93-9.14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/><path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>', bg: 'rgba(16,185,129,.1)', color: '#10b981' },
  { label: '未读消息', value: unreadCount.value, path: '/profile?scroll=edit', svg: '<path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/><path d="M13.73 21a2 2 0 01-3.46 0" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>', bg: 'rgba(139,92,246,.1)', color: '#8b5cf6' }
])

/** 服务入口卡片列表 */
const services = [
  { path: '/errands', title: '跑腿帮忙', desc: '社区互助，有求必应', svg: `<path d="${svgPos}" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>`, bg: 'rgba(249,115,22,.1)', color: '#f97316' }
]

/**
 * 订单状态枚举映射
 * 0 = 待接单，1 = 已接单，2 = 进行中，3 = 已完成，4 = 已取消
 */
const statusMap = { 0: '待接单', 1: '已接单', 2: '进行中', 3: '已完成', 4: '已取消' }
/** 订单状态对应的 Element Plus Tag 类型 */
const statusTypeMap = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }

/** 通过自定义事件通知 Layout 打开通知抽屉 */
function showAllNotices () { window.dispatchEvent(new CustomEvent('open-notices')) }

/**
 * 加载最新通知（最多5条）
 * API: GET /notifications?page=1&size=5
 */
async function loadNotices () { try { const { data } = await api.get('/notifications', { params: { page: 1, size: 5 } }); if (data.code === 200) notices.value = data.data?.records || [] } catch {} }

/**
 * 加载未读通知数量
 * API: GET /notifications/unread-count → { data: number }
 */
async function loadUnreadCount () { try { const { data } = await api.get('/notifications/unread-count'); if (data.code === 200) unreadCount.value = data.data || 0 } catch {} }

/**
 * 加载用户统计数据
 * 请求跑腿的"我的"订单，汇总已发布和已完成数量
 * API: GET /errand-requests/my
 */
async function loadMyStats () {
  try {
    const { data } = await api.get('/errand-requests/my')
    let publish = 0, completed = 0
    if (data.code === 200) { const records = data.data?.records || []; publish += records.length; completed += records.filter(r => r.status === 3).length }
    myStats.value = { orders: publish, publish, completed }
  } catch {}
}

/**
 * 加载用户活动动态
 * 获取跑腿订单，按时间排序，取最近5条
 * API: GET /errand-requests/my
 */
async function loadActivities () {
  try {
    const { data } = await api.get('/errand-requests/my')
    const list = []
    if (data.code === 200) {
      (data.data?.records || []).slice(0, 5).forEach(item => {
        list.push({ id: item.id, type: 'errand', title: item.title, typeLabel: '跑腿', time: item.createdAt, statusLabel: statusMap[item.status] || '未知', tagType: statusTypeMap[item.status] || 'info', svg: `<path d="${svgPos}" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>`, bg: 'rgba(249,115,22,.1)', color: '#f97316', link: '/errands' })
      })
    }
    activities.value = list
  } catch {}
}

// 页面挂载时加载所有数据
onMounted(() => { loadNotices(); loadUnreadCount(); loadMyStats(); loadActivities() })
</script>

<style scoped>
.page-header { margin-bottom: var(--s6); }
.page-header h2 { font-size: var(--fs-xl); font-weight: var(--fw-700); margin: 0; color: var(--ink); }
.page-header p { font-size: var(--fs-sm); color: var(--ink-muted); margin: 2px 0 0; }

/* ========== 统计卡片区 ========== */
.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--s3); margin-bottom: var(--s5); }
.stat-card {
  background: var(--white); border: 1px solid var(--border);
  border-radius: var(--r-lg); padding: var(--s4);
  display: flex; align-items: center; gap: var(--s3);
  cursor: pointer; transition: all var(--fast);
}
.stat-card:hover { border-color: var(--brand); box-shadow: var(--sh-sm); }
.stat-icon { width: 44px; height: 44px; border-radius: var(--r-md); display: grid; place-items: center; flex-shrink: 0; }
.stat-body { flex: 1; }
.stat-label { font-size: var(--fs-xs); color: var(--ink-muted); margin-bottom: 2px; }
.stat-value { font-size: var(--fs-lg); font-weight: var(--fw-700); color: var(--ink); }

/* ========== 服务入口区 ========== */
.func-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: var(--s3); margin-bottom: var(--s5); }
.func-card {
  background: var(--white); border: 1px solid var(--border);
  border-radius: var(--r-lg); padding: var(--s4);
  display: flex; align-items: center; gap: var(--s3);
  cursor: pointer; transition: all var(--fast);
}
.func-card:hover { border-color: var(--brand); box-shadow: var(--sh-sm); }
.func-icon { width: 48px; height: 48px; border-radius: var(--r-md); display: grid; place-items: center; flex-shrink: 0; }
.func-body { flex: 1; }
.func-body strong { display: block; font-size: var(--fs-md); font-weight: var(--fw-600); color: var(--ink); }
.func-body span { font-size: var(--fs-sm); color: var(--ink-muted); }
.func-arrow { color: var(--ink-faint); flex-shrink: 0; }

/* ========== 底部两栏 ========== */
.bottom-grid { display: grid; grid-template-columns: 1fr 1fr; gap: var(--s3); }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.card-head span { font-weight: var(--fw-600); color: var(--ink); }
.head-badge { display: flex; align-items: center; }

/* 通知列表 */
.notice-list { display: flex; flex-direction: column; }
.notice-item {
  display: flex; align-items: flex-start; gap: var(--s3);
  padding: var(--s3) 0;
  border-bottom: 1px solid var(--bg-soft);
}
.notice-item:last-child { border-bottom: none; }
.notice-item.unread .notice-body strong { color: var(--brand); }
.notice-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--border); margin-top: 7px; flex-shrink: 0; }
.notice-dot.unread { background: var(--brand); }
.notice-body { flex: 1; min-width: 0; }
.notice-body strong { display: block; font-size: var(--fs-sm); font-weight: var(--fw-600); color: var(--ink); margin-bottom: 2px; }
.notice-body p { font-size: var(--fs-xs); color: var(--ink-muted); margin: 0 0 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.notice-body time { font-size: var(--fs-xs); color: var(--ink-faint); }

/* 动态列表 */
.activity-list { display: flex; flex-direction: column; }
.activity-item {
  display: flex; align-items: center; gap: var(--s3);
  padding: var(--s3) 0;
  border-bottom: 1px solid var(--bg-soft);
  cursor: pointer;
}
.activity-item:last-child { border-bottom: none; }
.activity-item:hover .activity-body strong { color: var(--brand); }
.activity-icon { width: 32px; height: 32px; border-radius: var(--r-md); display: grid; place-items: center; flex-shrink: 0; }
.activity-body { flex: 1; min-width: 0; }
.activity-body strong { display: block; font-size: var(--fs-sm); font-weight: var(--fw-600); color: var(--ink); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 2px; transition: color var(--fast); }
.activity-body span { font-size: var(--fs-xs); color: var(--ink-muted); }

.empty-hint { text-align: center; padding: var(--s8) 0; color: var(--ink-faint); font-size: var(--fs-sm); }

/* ========== 响应式 ========== */
@media (max-width: 1200px) { .stat-grid, .func-grid { grid-template-columns: repeat(2, 1fr); } .bottom-grid { grid-template-columns: 1fr; } }
@media (max-width: 768px) { .stat-grid { grid-template-columns: 1fr 1fr; } }
@media (max-width: 480px) { .stat-grid { grid-template-columns: 1fr; } .func-grid { grid-template-columns: 1fr; } }
</style>

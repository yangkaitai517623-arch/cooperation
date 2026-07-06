<template>
  <!-- 管理员数据概览页：统计卡片 + 功能入口 + 最新动态 + 待处理列表 -->
  <div class="dashboard-page">
    <!-- 页面头部 -->
    <header class="dash-header"><h2>数据概览</h2><p>青青社区运营数据</p></header>

    <!-- 统计卡片区：注册用户/本月订单/待处理/满意度（带变化率） -->
    <div class="stat-grid">
      <div v-for="(s, i) in stats" :key="s.label" class="stat-card" :style="{ '--i': i }">
        <div class="stat-icon" :style="{ background: s.bg }"><el-icon :size="22"><component :is="s.icon" /></el-icon></div>
        <div class="stat-body"><div class="stat-label">{{ s.label }}</div><div class="stat-value">{{ s.value }}</div></div>
        <!-- 变化率指示器 -->
        <div class="stat-delta" :class="s.change > 0 ? 'up' : 'dn'">{{ s.change > 0 ? '↑' : '↓' }}{{ Math.abs(s.change) }}%</div>
      </div>
    </div>

    <!-- 功能入口区：跑腿服务/检修服务/二手交易/社区论坛 -->
    <div class="func-grid">
      <div v-for="s in services" :key="s.name" class="func-card" :style="{ '--i': s.idx }" @click="router.push(s.to)">
        <div class="func-icon" :style="{ background: s.bg }"><el-icon :size="24"><component :is="s.icon" /></el-icon></div>
        <div class="func-body"><strong>{{ s.name }}</strong><span>{{ s.desc }}</span></div>
        <el-icon class="func-arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- 底部两栏 -->
    <div class="bottom-grid">
      <!-- 左栏：最新动态时间线 -->
      <el-card shadow="never" class="activity-card">
        <template #header><div class="card-head"><span>最新动态</span></div></template>
        <el-timeline><el-timeline-item v-for="a in activities" :key="a.id" :type="a.type" :timestamp="a.time"><strong>{{ a.title }}</strong><p>{{ a.desc }}</p></el-timeline-item></el-timeline>
      </el-card>
      <!-- 右栏：待处理列表 -->
      <el-card shadow="never" class="pending-card">
        <template #header><span>待处理</span></template>
        <div class="pending-list"><div v-for="p in pending" :key="p.title" class="pending-item" @click="router.push(p.to)"><el-tag :type="p.type" size="small" effect="dark">{{ p.tag }}</el-tag><span>{{ p.title }}</span><el-icon><ArrowRight /></el-icon></div></div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
/**
 * Dashboard.vue - 管理员数据概览仪表盘
 *
 * 功能说明：
 * - 展示平台核心运营数据：注册用户数、本月订单数、待处理数、满意度
 * - 每个统计卡片显示变化率（↑上升/↓下降）
 * - 功能入口卡片快速跳转到各管理页面
 * - 底部双栏布局：最新动态时间线 + 待处理事项列表
 * - API: GET /dashboard/stats
 */

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, ShoppingCart, Clock, Star, Position, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

/** Vue Router 实例 */
const router = useRouter()

/**
 * 统计卡片数据
 * label: 指标名称, value: 数值, change: 变化率（正=上升, 负=下降）
 */
const stats = ref([
  { label: '注册用户', value: 0, change: 0, icon: User, bg: 'rgba(16,185,129,.1)' },
  { label: '本月订单', value: 0, change: 0, icon: ShoppingCart, bg: 'rgba(14,165,233,.1)' },
  { label: '待处理', value: 0, change: 0, icon: Clock, bg: 'rgba(249,115,22,.1)' },
  { label: '满意度', value: '0%', change: 0, icon: Star, bg: 'rgba(139,92,246,.1)' }
])

/** 功能入口卡片列表 */
const services = ref([
  { name: '跑腿服务', desc: '代买代送', icon: Position, bg: 'rgba(249,115,22,.1)', to: '/admin/errand', idx: 0 }
])

/** 最新动态列表 */
const activities = ref([])
/** 待处理事项列表 */
const pending = ref([])

/**
 * 加载仪表盘统计数据
 * API: GET /dashboard/stats
 * 响应包含：
 * - totalUsers: 注册用户总数
 * - monthlyOrders: 本月订单数
 * - pendingRepairs/pendingErrands: 待处理检修/跑腿数
 * - satisfactionRate: 满意度百分比
 * - change*: 各项变化率
 * - recentActivities: 最新动态
 * - pendingItems: 待处理列表
 */
async function loadStats () {
  try {
    const { data } = await api.get('/dashboard/stats')
    if (data.code === 200) {
      const d = data.data
      stats.value = [
        { label: '注册用户', value: d.totalUsers || 0, change: d.changeUsers || 0, icon: User, bg: 'rgba(16,185,129,.1)' },
        { label: '本月订单', value: d.monthlyOrders || 0, change: d.changeOrders || 0, icon: ShoppingCart, bg: 'rgba(14,165,233,.1)' },
        { label: '待处理', value: (d.pendingRepairs || 0) + (d.pendingErrands || 0), change: d.changePending || 0, icon: Clock, bg: 'rgba(249,115,22,.1)' },
        { label: '满意度', value: (d.satisfactionRate || 0) + '%', change: d.changeSatisfaction || 0, icon: Star, bg: 'rgba(139,92,246,.1)' }
      ]
      if (d.recentActivities) {
        activities.value = d.recentActivities.map((a, i) => ({ id: i, ...a }))
      }
      if (d.pendingItems) {
        pending.value = d.pendingItems.map(p => ({ ...p, to: p.link }))
      }
    }
  } catch {
    ElMessage.error('加载仪表盘数据失败')
  }
}
onMounted(loadStats)
</script>

<style scoped>
.dash-header { margin-bottom: var(--s6); }
.dash-header h2 { font-size: var(--fs-xl); font-weight: var(--fw-700); margin: 0; }
.dash-header p { font-size: var(--fs-sm); color: var(--ink-muted); margin: 2px 0 0; }

.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--s3); margin-bottom: var(--s5); }
.stat-card { background: var(--white); border: 1px solid var(--border); border-radius: var(--r-lg); padding: var(--s4); display: flex; align-items: center; gap: var(--s3); }
.stat-icon { width: 44px; height: 44px; border-radius: var(--r-md); display: grid; place-items: center; flex-shrink: 0; }
.stat-body { flex: 1; }
.stat-label { font-size: var(--fs-xs); color: var(--ink-muted); margin-bottom: 2px; }
.stat-value { font-size: var(--fs-lg); font-weight: var(--fw-700); }
.stat-delta { padding: 2px 8px; border-radius: var(--r-full); font-size: var(--fs-xs); font-weight: var(--fw-500); }
.stat-delta.up { background: var(--green-bg); color: var(--green); }
.stat-delta.dn { background: var(--brand-bg); color: var(--brand); }

.func-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: var(--s3); margin-bottom: var(--s5); }
.func-card { background: var(--white); border: 1px solid var(--border); border-radius: var(--r-lg); padding: var(--s4); display: flex; align-items: center; gap: var(--s3); cursor: pointer; }
.func-card:hover { border-color: var(--brand); }
.func-icon { width: 48px; height: 48px; border-radius: var(--r-md); display: grid; place-items: center; flex-shrink: 0; }
.func-body { flex: 1; }
.func-body strong { display: block; font-size: var(--fs-md); font-weight: var(--fw-600); }
.func-body span { font-size: var(--fs-sm); color: var(--ink-muted); }
.func-arrow { color: var(--ink-faint); }

.bottom-grid { display: grid; grid-template-columns: 2fr 1fr; gap: var(--s3); }
.card-head { display: flex; justify-content: space-between; align-items: center; }

.pending-list { display: flex; flex-direction: column; gap: 4px; }
.pending-item { display: flex; align-items: center; gap: var(--s3); padding: var(--s3); border-radius: var(--r-md); cursor: pointer; }
.pending-item:hover { background: var(--bg-soft); }
.pending-item span { flex: 1; font-size: var(--fs-base); color: var(--ink-light); }
.pending-item .el-icon { color: var(--ink-faint); }

@media (max-width: 1200px) { .stat-grid, .func-grid { grid-template-columns: repeat(2, 1fr); } .bottom-grid { grid-template-columns: 1fr; } }
@media (max-width: 768px) { .stat-grid { grid-template-columns: 1fr; } }
</style>

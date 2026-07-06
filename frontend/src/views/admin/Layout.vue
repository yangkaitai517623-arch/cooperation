<template>
  <!-- 管理员端整体布局：侧边栏 + 顶栏 + 面包屑 + 内容区 -->
  <div class="admin-shell">
    <!-- 侧边栏：Element Plus Menu，支持折叠 -->
    <aside class="admin-sidebar" :class="{ collapsed }">
      <!-- 侧边栏品牌区域 -->
      <div class="sidebar-brand"><span class="brand-icon">🏘️</span><span v-show="!collapsed" class="brand-text">青青社区</span></div>
      <!-- 导航菜单：router 模式根据路由高亮 -->
      <el-menu :default-active="active" :collapse="collapsed" router class="sidebar-nav">
        <!-- 数据概览 -->
        <el-menu-item index="/admin/dashboard"><el-icon><DataAnalysis /></el-icon><template #title>数据概览</template></el-menu-item>
        <!-- 信息管理子菜单 -->
        <el-sub-menu index="info"><template #title><el-icon><Grid /></el-icon><span>信息管理</span></template>
          <el-menu-item index="/admin/errand">跑腿需求</el-menu-item><el-menu-item index="/admin/errand-orders">跑腿订单</el-menu-item><el-menu-item index="/admin/forum">资讯论坛</el-menu-item><el-menu-item index="/admin/comments">评论管理</el-menu-item><el-menu-item index="/admin/notices">通知管理</el-menu-item>
        </el-sub-menu>
        <!-- 用户管理子菜单 -->
        <el-sub-menu index="user"><template #title><el-icon><User /></el-icon><span>用户管理</span></template>
          <el-menu-item index="/admin/admins">管理员</el-menu-item><el-menu-item index="/admin/users">用户列表</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </aside>

    <!-- 主区域 -->
    <div class="admin-main" :style="{ marginLeft: collapsed ? '64px' : '220px' }">
      <!-- 顶栏：折叠按钮 + 面包屑导航 + 通知铃铛 + 用户区 -->
      <header class="admin-topbar">
        <div class="topbar-left">
          <!-- 折叠/展开按钮 -->
          <button class="toggle-btn" @click="collapsed = !collapsed"><el-icon :size="18"><Fold v-if="!collapsed" /><Expand v-else /></el-icon></button>
          <!-- 面包屑导航：首页 / 当前页面 -->
          <el-breadcrumb separator="/"><el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item><el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item></el-breadcrumb>
        </div>
        <div class="topbar-right">
          <!-- 通知铃铛：带未读角标 -->
          <el-badge :value="unread" :hidden="unread === 0"><button class="bell-btn" @click="router.push('/admin/notices')"><el-icon :size="18"><Bell /></el-icon></button></el-badge>
          <!-- 用户下拉菜单：修改密码 / 退出 -->
          <el-dropdown trigger="click" @command="onCmd"><div class="user-chip"><div class="chip-avatar">{{ store.user?.realName?.[0] || '管' }}</div><span class="chip-name">{{ store.user?.realName || '管理员' }}</span></div><template #dropdown><el-dropdown-menu><el-dropdown-item command="pw">🔑 修改密码</el-dropdown-item><el-dropdown-item command="out" divided>🚪 退出</el-dropdown-item></el-dropdown-menu></template></el-dropdown>
        </div>
      </header>
      <!-- 内容区：通过 router-view 渲染子路由页面 -->
      <main class="admin-content"><router-view /></main>
    </div>
  </div>
</template>

<script setup>
/**
 * Layout.vue - 管理员端整体布局页面
 *
 * 功能说明：
 * - 提供管理员端统一的页面壳布局
 * - 桌面端：左侧侧边栏（支持折叠） + 顶栏 + 面包屑 + 内容区
 * - 侧边栏使用 Element Plus Menu 组件，自动根据路由高亮当前项
 * - 顶栏包含：折叠按钮、面包屑导航、通知铃铛（带未读角标）、用户下拉菜单
 * - 用户下拉菜单：修改密码、退出登录
 * - 子菜单组织：
 *   · 信息管理：商品分类、商品、订单、检修需求/订单、跑腿需求/订单、论坛、评论、通知
 *   · 用户管理：管理员、用户列表
 */

import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import api from '@/api'

/** Vue Router 实例 */
const route = useRoute()
const router = useRouter()
/** Pinia 用户状态管理 */
const store = useUserStore()

/** 侧边栏是否折叠 */
const collapsed = ref(false)
/** 未读通知数量 */
const unread = ref(0)
/** 当前活动路由路径（计算属性，用于菜单高亮） */
const active = computed(() => route.path)

/**
 * 处理用户下拉菜单命令
 * @param {string} cmd - 'out' = 退出登录, 'pw' = 修改密码
 */
function onCmd (cmd) { if (cmd === 'out') { store.logout(); router.push('/login') } else if (cmd === 'pw') router.push('/admin/password') }

/**
 * 加载未读通知数量
 * API: GET /notifications/unread-count → { data: number }
 */
async function loadUnread () { try { const { data } = await api.get('/notifications/unread-count'); if (data.code === 200) unread.value = data.data } catch {} }

// 页面挂载时加载未读数量
onMounted(loadUnread)
</script>

<style scoped>
.admin-shell { display: flex; min-height: 100vh; }
.admin-sidebar { width: 220px; background: var(--ink); position: fixed; top: 0; bottom: 0; left: 0; z-index: 100; transition: width .3s; overflow-y: auto; overflow-x: hidden; }
.admin-sidebar.collapsed { width: 64px; }
.sidebar-brand { height: 56px; display: flex; align-items: center; justify-content: center; gap: var(--s2); border-bottom: 1px solid rgba(255,255,255,.06); }
.brand-icon { font-size: 22px; }
.brand-text { font-size: var(--fs-lg); font-weight: var(--fw-900); background: var(--grad-brand); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; white-space: nowrap; }
.sidebar-nav { border-right: none; background: transparent; padding: var(--s2); }
.sidebar-nav:not(.el-menu--collapse) { width: 204px; }

.admin-main { flex: 1; display: flex; flex-direction: column; background: var(--bg-page); transition: margin-left .3s var(--ease); }
.admin-topbar { height: 56px; background: rgba(255,255,255,.88); backdrop-filter: saturate(180%) blur(16px); border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; padding: 0 var(--s5); position: sticky; top: 0; z-index: 50; }
.topbar-left { display: flex; align-items: center; gap: var(--s3); }
.toggle-btn { width: 34px; height: 34px; display: grid; place-items: center; border: none; background: transparent; border-radius: var(--r-sm); color: var(--ink-muted); transition: background var(--fast); }
.toggle-btn:hover { background: var(--bg-soft); }
.topbar-right { display: flex; align-items: center; gap: var(--s3); }
.bell-btn { width: 34px; height: 34px; display: grid; place-items: center; border: none; background: var(--bg-soft); border-radius: var(--r-sm); color: var(--ink-muted); }
.user-chip { display: flex; align-items: center; gap: var(--s2); cursor: pointer; padding: 3px 10px 3px 3px; border-radius: var(--r-full); transition: background var(--fast); }
.user-chip:hover { background: var(--bg-soft); }
.chip-avatar { width: 30px; height: 30px; border-radius: var(--r-sm); background: var(--grad-brand); color: var(--white); display: grid; place-items: center; font-size: var(--fs-sm); font-weight: var(--fw-700); }
.chip-name { font-size: var(--fs-sm); font-weight: var(--fw-500); color: var(--ink-light); }
.admin-content { flex: 1; padding: var(--s5); overflow-y: auto; }

/* Element Plus Menu 深色主题重写 */
:deep(.el-menu) { --el-menu-bg-color: transparent; --el-menu-text-color: rgba(255,255,255,.6); --el-menu-hover-bg-color: rgba(255,255,255,.06); --el-menu-active-color: #fff; }
:deep(.el-menu-item) { border-radius: var(--r-md); margin: 2px 0; height: 38px; line-height: 38px; font-size: var(--fs-sm); }
:deep(.el-menu-item.is-active) { background: rgba(255,255,255,.1) !important; color: #fff !important; font-weight: var(--fw-600); }
:deep(.el-sub-menu__title) { border-radius: var(--r-md); margin: 2px 0; height: 38px; line-height: 38px; font-size: var(--fs-sm); color: rgba(255,255,255,.6) !important; }
:deep(.el-sub-menu__title:hover) { background: rgba(255,255,255,.06) !important; color: var(--white) !important; }
:deep(.el-sub-menu .el-menu-item) { padding-left: 48px !important; min-width: auto; }
</style>

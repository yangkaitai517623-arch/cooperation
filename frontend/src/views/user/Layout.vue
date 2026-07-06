<template>
  <!-- 用户端整体布局：侧边栏（桌面） + 顶栏 + 内容区 + 底部导航（移动端） + 通知抽屉 -->
  <div class="app-shell">
    <!-- 侧边栏 - 桌面端显示，支持折叠 -->
    <aside class="app-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <!-- 侧边栏品牌Logo区域 -->
      <div class="sidebar-brand">
        <svg class="brand-icon" width="22" height="22" viewBox="0 0 24 24" fill="none">
          <path d="M3 9.5L12 4l9 5.5V20a1 1 0 01-1 1H4a1 1 0 01-1-1V9.5z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
          <path d="M9 21V12h6v9" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
        </svg>
        <!-- 品牌文字：折叠时隐藏 -->
        <span v-show="!sidebarCollapsed" class="brand-text">青青社区</span>
      </div>
      <!-- 导航菜单列表 -->
      <nav class="sidebar-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          <!-- SVG 图标：通过 v-html 动态渲染 -->
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" v-html="item.svg"></svg>
          <!-- 导航文字：折叠时隐藏 -->
          <span v-show="!sidebarCollapsed" class="nav-label">{{ item.label }}</span>
        </router-link>
      </nav>
    </aside>

    <!-- 主区域：含顶栏 + 内容区，左侧 margin 随侧边栏折叠状态变化 -->
    <div class="app-main" :style="{ marginLeft: sidebarCollapsed ? '64px' : '220px' }">
      <!-- 顶栏：切换按钮 + 页面标题 + 通知铃铛 + 用户下拉菜单 -->
      <header class="app-topbar">
        <div class="topbar-left">
          <!-- 侧边栏折叠/展开按钮 -->
          <button class="toggle-btn" @click="sidebarCollapsed = !sidebarCollapsed">
            <svg v-if="!sidebarCollapsed" width="18" height="18" viewBox="0 0 24 24" fill="none"><path d="M3 12h18M3 6h18M3 18h18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none"><path d="M3 12h18M3 6h18M3 18h18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </button>
          <!-- 当前页面标题 -->
          <div class="topbar-title">
            <h2>{{ currentTitle }}</h2>
          </div>
        </div>
        <div class="topbar-right">
          <!-- 通知铃铛：带未读数量角标 -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
            <button class="bell-btn" @click="showDrawer = true">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M13.73 21a2 2 0 01-3.46 0" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </el-badge>
          <!-- 用户区：头像 + 名称 + 下拉菜单 -->
          <el-dropdown trigger="click" @command="onCommand">
            <div class="user-chip">
              <div class="chip-avatar">{{ userName[0] || '用' }}</div>
              <span class="chip-name">{{ userName }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile"><el-icon><User /></el-icon> 个人信息</el-dropdown-item>
                <!-- 仅管理员角色可见管理后台入口 -->
                <el-dropdown-item v-if="isAdmin" command="admin"><el-icon><Setting /></el-icon> 管理后台</el-dropdown-item>
                <el-dropdown-item command="logout" divided><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区：通过 router-view 渲染子路由页面 -->
      <main class="app-content"><router-view /></main>
    </div>

    <!-- 移动端底部导航：仅小屏幕显示 -->
    <nav class="app-navbar-mobile">
      <router-link v-for="item in navItems" :key="item.path" :to="item.path" class="mobile-nav-link" :class="{ active: isActive(item.path) }">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" v-html="item.svg"></svg>
        <span>{{ item.label }}</span>
      </router-link>
    </nav>

    <!-- 通知抽屉：点击铃铛打开 -->
    <el-drawer v-model="showDrawer" title="消息通知" size="350px">
      <!-- 空状态 -->
      <div v-if="noticeList.length === 0" class="drawer-empty">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
          <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M13.73 21a2 2 0 01-3.46 0" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <p>暂无新消息</p>
      </div>
      <!-- 通知列表：区分已读/未读样式 -->
      <div v-for="item in noticeList" :key="item.id" class="drawer-item" :class="{ unread: !item.isRead }" @click="readNotice(item)">
        <div class="drawer-icon" :class="{ unread: !item.isRead }">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="drawer-body">
          <strong>{{ item.title }}</strong>
          <p>{{ item.content }}</p>
          <time>{{ item.createdAt }}</time>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
/**
 * Layout.vue - 用户端整体布局页面
 *
 * 功能说明：
 * - 提供用户端统一的页面壳布局
 * - 桌面端：左侧侧边栏（支持折叠） + 顶栏 + 内容区
 * - 移动端：隐藏侧边栏，显示底部导航栏
 * - 顶栏包含：折叠按钮、页面标题、通知铃铛（带未读角标）、用户下拉菜单
 * - 用户下拉菜单：个人信息、管理后台（仅管理员可见）、退出登录
 * - 通知抽屉：显示最近20条通知，支持标记已读
 */

import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { User, Setting, SwitchButton } from '@element-plus/icons-vue'
import api from '@/api'

/** Vue Router 实例 */
const route = useRoute()
const router = useRouter()
/** Pinia 用户状态管理 */
const store = useUserStore()

/** 未读通知数量 */
const unreadCount = ref(0)
/** 通知抽屉是否显示 */
const showDrawer = ref(false)
/** 通知列表数据 */
const noticeList = ref([])
/** 侧边栏是否折叠 */
const sidebarCollapsed = ref(false)

/** 当前登录用户的真实姓名（计算属性） */
const userName = computed(() => store.user?.realName || '用户')
/**
 * 是否为管理员（计算属性）
 * role: 0 = 普通用户，1 = 管理员，2 = 超级管理员
 */
const isAdmin = computed(() => store.user?.role >= 1)

/**
 * 导航项列表
 * 每个项目包含：
 * - path: 路由路径
 * - label: 显示文字
 * - svg: 内联 SVG 图标字符串
 */
const navItems = [
  { path: '/home', label: '首页', svg: '<path d="M3 9.5L12 4l9 5.5V20a1 1 0 01-1 1H4a1 1 0 01-1-1V9.5z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/><path d="M9 21V12h6v9" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>' },
  { path: '/errands', label: '跑腿帮忙', svg: '<path d="M14.7 6.3a1 1 0 000 1.4l1.6 1.6a1 1 0 001.4 0l3.77-3.77a6 6 0 01-7.94 7.94l-6.91 6.91a2.12 2.12 0 01-3-3l6.91-6.91a6 6 0 017.94-7.94l-3.76 3.76z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>' },
  { path: '/profile', label: '个人中心', svg: '<path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/><circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="1.8"/>' }
]

/** 路由路径与页面标题的映射表 */
const routeTitleMap = { '/home': '首页', '/errands': '跑腿帮忙', '/profile': '个人中心' }
/** 当前页面标题（计算属性），根据当前路由路径计算 */
const currentTitle = computed(() => routeTitleMap[route.path] || '首页')

/**
 * 判断导航项是否处于活动状态（当前路由匹配）
 * @param {string} path - 导航路径
 * @returns {boolean}
 */
function isActive (path) { return route.path === path }

/**
 * 处理用户下拉菜单命令
 * @param {string} cmd - 命令名称：'profile' | 'admin' | 'logout'
 */
function onCommand (cmd) {
  if (cmd === 'logout') { store.logout(); router.push('/login') }
  else if (cmd === 'admin') router.push('/admin/dashboard')
  else if (cmd === 'profile') router.push('/profile')
}

/**
 * 加载未读通知数量
 * API: GET /notifications/unread-count → { data: number }
 */
async function loadUnread () { try { const { data } = await api.get('/notifications/unread-count'); if (data.code === 200) unreadCount.value = data.data } catch {} }

/**
 * 加载通知列表（最近20条）
 * API: GET /notifications?page=1&size=20 → { data: { records: [...] } }
 */
async function loadNotices () { try { const { data } = await api.get('/notifications', { params: { page: 1, size: 20 } }); if (data.code === 200) noticeList.value = data.data?.records || [] } catch {} }

/**
 * 标记通知为已读
 * API: PUT /notifications/{id}/read
 * 成功后更新本地未读计数
 * @param {Object} item - 通知对象
 */
async function readNotice (item) { if (item.isRead) return; try { await api.put(`/notifications/${item.id}/read`); item.isRead = 1; unreadCount.value = Math.max(0, unreadCount.value - 1) } catch {} }

// 页面挂载时加载未读通知数量，并监听自定义事件 open-notices（首页触发）
onMounted(() => { loadUnread(); window.addEventListener('open-notices', () => { showDrawer.value = true }) })
// 监听通知抽屉打开，打开时自动加载通知列表
watch(showDrawer, v => { if (v) loadNotices() })
</script>

<style scoped>
.app-shell { display: flex; min-height: 100vh; }

/* ========== 侧边栏 ========== */
.app-sidebar {
  width: 220px;
  background: var(--ink);
  position: fixed;
  top: 0; bottom: 0; left: 0;
  z-index: 100;
  transition: width .3s;
  overflow-y: auto;
  overflow-x: hidden;
}
.app-sidebar.collapsed { width: 64px; }
.sidebar-brand {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--s2);
  border-bottom: 1px solid rgba(255,255,255,.06);
}
.brand-icon { color: var(--brand-light); }
.brand-text {
  font-size: var(--fs-lg);
  font-weight: var(--fw-900);
  color: var(--white);
  white-space: nowrap;
}
.sidebar-nav {
  padding: var(--s2);
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: var(--s3);
  padding: 0 var(--s3);
  height: 38px;
  border-radius: var(--r-md);
  color: rgba(255,255,255,.6);
  text-decoration: none;
  font-size: var(--fs-sm);
  font-weight: var(--fw-500);
  transition: all var(--fast);
  white-space: nowrap;
}
.nav-item:hover {
  background: rgba(255,255,255,.06);
  color: var(--white);
}
.nav-item.active {
  background: rgba(255,255,255,.1);
  color: var(--white);
  font-weight: var(--fw-600);
}
.nav-item svg { flex-shrink: 0; }

/* ========== 主区域 ========== */
.app-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg-page);
  transition: margin-left .3s var(--ease);
}
/* 顶栏：毛玻璃效果 */
.app-topbar {
  height: 56px;
  background: rgba(255,255,255,.88);
  backdrop-filter: saturate(180%) blur(16px);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--s5);
  position: sticky;
  top: 0;
  z-index: 50;
}
.topbar-left { display: flex; align-items: center; gap: var(--s3); }
.toggle-btn {
  width: 34px; height: 34px;
  display: grid; place-items: center;
  border: none; background: transparent;
  border-radius: var(--r-sm);
  color: var(--ink-muted);
  cursor: pointer;
  transition: background var(--fast);
}
.toggle-btn:hover { background: var(--bg-soft); }
.topbar-title h2 {
  font-size: var(--fs-md);
  font-weight: var(--fw-600);
  margin: 0;
  color: var(--ink);
}
.topbar-right { display: flex; align-items: center; gap: var(--s3); }
.bell-btn {
  width: 34px; height: 34px;
  display: grid; place-items: center;
  border: none; background: var(--bg-soft);
  border-radius: var(--r-sm);
  color: var(--ink-muted);
  cursor: pointer;
  transition: all var(--fast);
}
.bell-btn:hover { background: var(--border); }
/* 用户信息区域（头像 + 名称） */
.user-chip {
  display: flex; align-items: center; gap: var(--s2);
  cursor: pointer; padding: 3px 10px 3px 3px;
  border-radius: var(--r-full);
  transition: background var(--fast);
}
.user-chip:hover { background: var(--bg-soft); }
.chip-avatar {
  width: 30px; height: 30px;
  border-radius: var(--r-sm);
  background: var(--brand);
  color: var(--white);
  display: grid; place-items: center;
  font-size: var(--fs-sm);
  font-weight: var(--fw-700);
}
.chip-name {
  font-size: var(--fs-sm);
  font-weight: var(--fw-500);
  color: var(--ink-light);
}

/* 内容区 */
.app-content {
  flex: 1;
  padding: var(--s5);
  overflow-y: auto;
}

/* ========== 通知抽屉 ========== */
.drawer-empty {
  text-align: center;
  padding: var(--s12) var(--s4);
  color: var(--ink-faint);
}
.drawer-empty p { margin-top: var(--s3); color: var(--ink-muted); font-size: var(--fs-sm); }
.drawer-item {
  display: flex; gap: var(--s3); padding: var(--s4);
  border-bottom: 1px solid var(--bg-soft);
  cursor: pointer; transition: background var(--fast);
}
.drawer-item:hover { background: var(--bg-soft); }
.drawer-item.unread { background: var(--brand-bg); }
.drawer-icon {
  width: 32px; height: 32px;
  border-radius: var(--r-md);
  background: var(--bg-soft);
  display: grid; place-items: center;
  flex-shrink: 0; color: var(--ink-muted);
}
.drawer-icon.unread { background: var(--brand); color: var(--white); }
.drawer-body { flex: 1; min-width: 0; }
.drawer-body strong { display: block; font-size: var(--fs-base); font-weight: var(--fw-600); margin-bottom: 4px; color: var(--ink); }
.drawer-body p { font-size: var(--fs-sm); color: var(--ink-muted); margin: 0 0 6px; line-height: 1.5; }
.drawer-body time { font-size: var(--fs-xs); color: var(--ink-faint); }

/* ========== 移动端底部导航 ========== */
.app-navbar-mobile {
  display: none;
  position: fixed; bottom: 0; left: 0; right: 0;
  height: 56px;
  background: rgba(255,255,255,.92);
  backdrop-filter: saturate(180%) blur(16px);
  border-top: 1px solid var(--border);
  justify-content: space-around;
  align-items: center;
  z-index: 100;
}
.mobile-nav-link {
  display: flex; flex-direction: column; align-items: center; gap: 2px;
  text-decoration: none; color: var(--ink-faint);
  font-size: 11px; padding: var(--s1) var(--s3);
  border-radius: var(--r-md); transition: color var(--fast);
}
.mobile-nav-link:hover { color: var(--ink-muted); }
.mobile-nav-link.active { color: var(--brand); }
.mobile-nav-link.active::before {
  content: ''; position: absolute; top: -1px;
  left: 50%; transform: translateX(-50%);
  width: 20px; height: 2px;
  background: var(--brand); border-radius: 1px;
}
.mobile-nav-link span { font-weight: var(--fw-500); }

/* ========== 响应式：小屏隐藏侧边栏，显示底部导航 ========== */
@media (max-width: 768px) {
  .app-sidebar { display: none; }
  .app-main { margin-left: 0 !important; }
  .app-navbar-mobile { display: flex; }
  .app-content { padding: var(--s4); padding-bottom: 72px; }
  .toggle-btn { display: none; }
  .chip-name { display: none; }
}
</style>

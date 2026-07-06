/*
 * ============================================================================
 * 社区平台前端路由配置
 * ============================================================================
 * 本文件定义了应用的全部路由，包含两大类：
 *   1. 管理员路由（/admin/*）：后台管理端，需 admin 角色 + 登录认证
 *   2. 用户路由（/*）：前端用户界面，需登录认证
 *
 * 路由守卫 (beforeEach) 负责：
 *   - 验证用户是否已登录（token 是否存在）
 *   - 验证管理员角色是否有权限访问 admin 路由
 *   - 未登录用户自动跳转到登录页
 *   - 非管理员用户访问 admin 路由时重定向到首页
 * ============================================================================
 */

/* 导入 Vue Router 的创建函数 */
import { createRouter, createWebHistory } from 'vue-router'

/*
 * 路由配置数组
 * 包含公开路由（登录/注册）、管理员路由和用户路由三部分
 */
const routes = [
  /*
   * ========== 公开路由（无需登录） ==========
   */

  /* 登录页面路由 */
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')  /* 按需加载，减小首屏打包体积 */
  },

  /* 注册页面路由 */
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },

  /*
   * ========== 管理员后台路由 /admin ==========
   * 所有子路由需要：
   *   - requiresAuth: true   → 用户必须已登录
   *   - role: 'admin'        → 用户角色必须 >= 1（管理员或超级管理员）
   */

  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/Layout.vue'),  /* 管理员布局组件（侧边栏+顶栏+内容区） */
    meta: { requiresAuth: true, role: 'admin' },           /* 路由元信息：需要认证 + 管理员角色 */
    redirect: '/admin/dashboard',                          /* 默认重定向到数据概览页 */
    children: [
      /* 数据概览仪表盘 */
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据概览' }
      },

      /* 用户管理 */
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue'),
        meta: { title: '用户管理' }
      },

      /* 管理员管理（超级管理员可操作） */
      {
        path: 'admins',
        name: 'AdminAdmins',
        component: () => import('@/views/admin/Admins.vue'),
        meta: { title: '管理员管理' }
      },

      /* 跑腿需求管理 */
      {
        path: 'errand',
        name: 'AdminErrand',
        component: () => import('@/views/admin/Errand.vue'),
        meta: { title: '跑腿需求管理' }
      },

      /* 跑腿订单管理 */
      {
        path: 'errand-orders',
        name: 'AdminErrandOrders',
        component: () => import('@/views/admin/ErrandOrders.vue'),
        meta: { title: '跑腿订单管理' }
      },

      /* 论坛帖子管理 */
      {
        path: 'forum',
        name: 'AdminForum',
        component: () => import('@/views/admin/Forum.vue'),
        meta: { title: '资讯论坛管理' }
      },

      /* 评论管理 */
      {
        path: 'comments',
        name: 'AdminComments',
        component: () => import('@/views/admin/Comments.vue'),
        meta: { title: '评论管理' }
      },

      /* 通知管理 */
      {
        path: 'notices',
        name: 'AdminNotices',
        component: () => import('@/views/admin/Notices.vue'),
        meta: { title: '通知管理' }
      },

      /* 修改密码 */
      {
        path: 'password',
        name: 'AdminPassword',
        component: () => import('@/views/admin/Password.vue'),
        meta: { title: '修改密码' }
      }
    ]
  },

  /*
   * ========== 用户端路由 / ==========
   * 所有子路由需要 requiresAuth: true → 用户必须已登录
   */

  {
    path: '/',
    name: 'UserLayout',
    component: () => import('@/views/user/Layout.vue'),  /* 用户端布局组件（底部导航栏+内容区） */
    meta: { requiresAuth: true },                         /* 需要登录认证 */
    redirect: '/home',                                    /* 默认重定向到首页 */
    children: [
      /* 首页 */
      {
        path: 'home',
        name: 'UserHome',
        component: () => import('@/views/user/Home.vue'),
        meta: { title: '首页' }
      },

      /* 跑腿需求列表页 */
      {
        path: 'errands',
        name: 'UserErrands',
        component: () => import('@/views/user/Errands.vue'),
        meta: { title: '跑腿需求' }
      },

      /* 社区论坛（邻里广场） */
      {
        path: 'forum',
        name: 'UserForum',
        component: () => import('@/views/user/Forum.vue'),
        meta: { title: '邻里广场' }
      },

      /* 帖子详情页 */
      {
        path: 'forum/:id',
        name: 'PostDetail',
        component: () => import('@/views/user/PostDetail.vue'),
        meta: { title: '帖子详情' }
      },

      /* 个人信息页 */
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人信息' }
      }
    ]
  }
]

/* 创建路由实例，使用 HTML5 History 模式（URL 无 # 号） */
const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * ========== 全局前置路由守卫 ==========
 * 在每次路由跳转前执行，进行权限验证
 *
 * 验证逻辑：
 *   1. 如果目标路由需要登录但未登录（无 token） → 重定向到 /login
 *   2. 如果目标路由需要管理员角色但用户角色不足（role < 1） → 重定向到首页 /
 *   3. 其他情况正常放行
 *
 * @param {Object} to   - 目标路由对象
 * @param {Object} from - 来源路由对象
 * @param {Function} next - 解析钩子，调用 next() 放行或 next(path) 重定向
 */
router.beforeEach((to, from, next) => {
  /* 从 localStorage 中获取登录令牌 */
  const token = localStorage.getItem('token')

  /* 尝试解析本地存储的用户信息，失败时默认为空对象 */
  let user = {}
  try { user = JSON.parse(localStorage.getItem('user') || '{}') } catch { user = {} }

  /* 情况1：目标页面需要登录认证，但当前无 token（未登录或 token 失效） */
  if (to.meta.requiresAuth && !token) {
    /* 重定向到登录页 */
    next('/login')
  }
  /* 情况2：目标页面需要管理员角色，但当前用户角色不足（非管理员） */
  else if (to.meta.role === 'admin' && (!user.role || user.role < 1)) {
    /* 重定向到用户端首页 */
    next('/')
  }
  /* 情况3：权限验证通过，正常放行 */
  else {
    next()
  }
})

export default router

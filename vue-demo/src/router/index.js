import { createRouter, createWebHistory } from 'vue-router'

const Login = () => import('@/views/Login.vue')
const StudentManage = () => import('@/views/StudentManage.vue')

const routes = [
  {
    path: '/',
    name: 'Login',
    component: Login,
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/login',
    redirect: '/'
  },
  {
    path: '/students',
    name: 'StudentManage',
    component: StudentManage,
    meta: { title: '学生管理', requiresAuth: true }
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title + ' - Vue3 企业级项目'
  }
  
  if (to.meta.requiresAuth) {
    const loginUser = localStorage.getItem('loginUser')
    if (!loginUser) {
      next('/')
      return
    }
  }
  
  next()
})

export default router

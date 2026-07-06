/*
 * ============================================================================
 * 用户状态管理 Store (Pinia)
 * ============================================================================
 * 使用 Vue 3 Composition API 风格的 Pinia 状态管理
 *
 * 管理内容：
 *   - token（登录令牌）和 user（用户信息）的本地持久化
 *   - 三个计算属性：isLoggedIn（是否登录）、isAdmin（是否管理员）、isSuperAdmin（是否超级管理员）
 *   - login（登录）和 logout（登出）两个异步/同步操作方法
 *
 * 数据持久化策略：
 *   - 状态初始化时从 localStorage 恢复 token 和 user 信息
 *   - 登录成功后自动写入 localStorage
 *   - 登出时清除 localStorage 中对应的数据
 * ============================================================================
 */

/* 导入 Pinia 的 defineStore 函数，用于创建状态存储 */
import { defineStore } from 'pinia'

/* 导入 Vue 3 的响应式 API */
import { ref, computed } from 'vue'

/* 导入配置好的 axios 实例，用于发送 API 请求 */
import api from '@/api'

/*
 * 定义并导出 user store
 * 第一个参数 'user' 是该 store 的唯一标识符
 * 第二个参数是一个 setup 函数，使用 Composition API 风格定义
 */
export const useUserStore = defineStore('user', () => {
  /* ========== 状态 (State) ========== */

  /*
   * 登录令牌（JWT token）
   * 初始值从 localStorage 中读取，实现刷新后自动恢复登录状态
   */
  const token = ref(localStorage.getItem('token') || '')

  /*
   * 用户信息对象
   * 从 localStorage 中解析已保存的用户信息，解析失败则为 null
   * 使用安全解析：try/catch 包裹 JSON.parse 防止损坏数据导致崩溃
   */
  let savedUser = null
  try { savedUser = JSON.parse(localStorage.getItem('user') || 'null') } catch { savedUser = null }
  const user = ref(savedUser)

  /* ========== 计算属性 (Getters) ========== */

  /*
   * 是否已登录
   * 判断依据：token 是否存在且非空
   */
  const isLoggedIn = computed(() => !!token.value)

  /*
   * 是否管理员
   * 判断依据：用户角色 role >= 1（role: 0=普通用户, 1=管理员, 2=超级管理员）
   */
  const isAdmin = computed(() => user.value?.role >= 1)

  /*
   * 是否超级管理员
   * 判断依据：用户角色 role === 2
   */
  const isSuperAdmin = computed(() => user.value?.role === 2)

  /* ========== 操作方法 (Actions) ========== */

  /**
   * 用户登录
   * 向 /auth/login 发送登录请求，成功后保存 token 和用户信息到状态和 localStorage
   *
   * @param {string} username - 用户名
   * @param {string} password - 密码
   * @returns {Promise<boolean>} 登录成功返回 true
   * @throws {Error} 登录失败时抛出包含错误信息的异常
   */
  async function login(username, password) {
    /* 发送 POST 请求到登录接口 */
    const res = await api.post('/auth/login', { username, password })

    /* 检查响应状态码，200 表示登录成功 */
    if (res.data.code === 200) {
      /* 保存 token 到响应式状态 */
      token.value = res.data.data.token
      /* 保存用户信息到响应式状态 */
      user.value = res.data.data.user
      /* 将 token 持久化到 localStorage，页面刷新后仍可保持登录 */
      localStorage.setItem('token', token.value)
      /* 将用户信息持久化到 localStorage */
      localStorage.setItem('user', JSON.stringify(user.value))
      return true
    }
    /* 登录失败时抛出异常，由调用方处理错误提示 */
    throw new Error(res.data.message)
  }

  /**
   * 用户登出
   * 清除状态中的 token 和 user，并移除 localStorage 中的持久化数据
   */
  function logout() {
    /* 清空内存中的 token 状态 */
    token.value = ''
    /* 清空内存中的用户信息 */
    user.value = null
    /* 移除 localStorage 中的 token */
    localStorage.removeItem('token')
    /* 移除 localStorage 中的用户信息 */
    localStorage.removeItem('user')
  }

  /* 导出供外部使用的状态、计算属性和方法 */
  return { token, user, isLoggedIn, isAdmin, isSuperAdmin, login, logout }
})

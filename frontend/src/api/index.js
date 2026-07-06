/*
 * ============================================================================
 * API 请求核心模块 - axios 实例配置
 * ============================================================================
 * 本文件创建并导出一个配置好的 axios 实例，作为整个应用的 HTTP 请求基础层
 *
 * 配置内容：
 *   1. 基础 URL（/api）和超时时间（30 秒）
 *   2. 请求拦截器：自动从 localStorage 读取 token 并附加到请求头
 *   3. 响应拦截器：统一处理 HTTP 错误状态码（401/403 等），显示友好提示
 *
 * 使用方式：
 *   其他 API 模块通过 `import api from './index'` 引入此实例，
 *   然后调用 api.get(), api.post() 等方法
 * ============================================================================
 */

/* 导入 axios HTTP 请求库 */
import axios from 'axios'

/* 导入 Element Plus 的消息提示组件（用于错误提示） */
import { ElMessage } from 'element-plus'

/* 导入路由实例（用于 401 时跳转登录页） */
import router from '@/router'

/*
 * 创建 axios 实例，配置全局默认参数
 *
 * baseURL: 所有请求的基础路径前缀，实际请求路径会拼接在此之后
 *          例如 api.get('/user/info') 实际请求 /api/user/info
 * timeout: 请求超时时间（毫秒），超时后自动取消请求并触发错误
 */
const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

/*
 * ========== 请求拦截器 ==========
 * 在每次请求发出前执行，用于统一添加认证信息
 *
 * 成功回调：
 *   从 localStorage 中读取 token，如果存在则设置 Authorization 请求头
 *   Bearer 是 JWT（JSON Web Token）的标准认证方案
 */
api.interceptors.request.use(
  config => {
    /* 从本地存储中获取登录令牌 */
    const token = localStorage.getItem('token')
    /* 如果存在 token 则添加到请求头的 Authorization 字段 */
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    /* 返回处理后的配置，继续发送请求 */
    return config
  },
  /* 请求配置错误时直接拒绝 */
  error => Promise.reject(error)
)

/*
 * ========== 响应拦截器 ==========
 * 在每次收到响应后执行，用于统一处理业务错误和 HTTP 错误
 *
 * HTTP 状态码处理策略：
 *   - 401 Unauthorized：token 过期或无效 → 清除本地存储，跳转登录页
 *   - 403 Forbidden：无权限访问 → 提示无权限
 *   - 其他 4xx/5xx：显示服务端返回的错误消息，或默认"请求失败"
 *   - 网络错误（无 response）：提示"网络错误"
 */
api.interceptors.response.use(
  /* 成功响应：直接返回，由具体 API 调用方处理业务逻辑 */
  response => {
    return response
  },
  /* 错误响应：根据状态码分类处理 */
  error => {
    /* 判断是否收到了服务端响应（与网络错误的区分） */
    if (error.response) {
      const { status, data } = error.response

      if (status === 401) {
        /* 401: 未授权，token 已过期或无效 */
        ElMessage.error('登录已过期，请重新登录')
        /* 清除本地存储的认证信息 */
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        /* 跳转到登录页面 */
        router.push('/login')
      } else if (status === 403) {
        /* 403: 禁止访问，当前用户角色无权操作 */
        ElMessage.error('无权限访问')
      } else {
        /* 其他错误状态码：显示服务端返回的消息，无消息时显示默认文案 */
        ElMessage.error(data?.message || '请求失败')
      }
    } else {
      /* 无响应对象：网络连接失败或超时 */
      ElMessage.error('网络错误')
    }
    /* 继续抛出错误，让调用方可以进一步处理（如 try/catch） */
    return Promise.reject(error)
  }
)

export default api

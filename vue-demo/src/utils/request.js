import axios from 'axios'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 5000
})

const getToken = () => {
  const token = localStorage.getItem('token')
  if (token) return token
  
  const loginUser = localStorage.getItem('loginUser')
  if (!loginUser) return ''
  
  try {
    const parsed = JSON.parse(loginUser)
    return parsed?.token || ''
  } catch {
    return ''
  }
}

const getErrorMessage = (payload, fallback = '请求失败') => {
  return payload?.msg || payload?.message || fallback
}

service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {
    const res = response.data
    
    if (res && typeof res === 'object' && Object.prototype.hasOwnProperty.call(res, 'code')) {
      if (res.code !== 200) {
        return Promise.reject(new Error(getErrorMessage(res)))
      }
      return res
    }
    
    return { code: 200, msg: 'success', data: res }
  },
  (error) => {
    console.error('响应错误：', error)
    const status = error?.response?.status
    const message =
      status === 401 ? '登录已失效，请重新登录' :
      getErrorMessage(error?.response?.data, '服务器错误')
    
    return Promise.reject(new Error(message))
  }
)

export default service

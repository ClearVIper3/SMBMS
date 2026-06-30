import axios, { type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

/**
 * Axios 实例：
 *  - baseURL = /api（开发期由 Vite proxy 转发到后端）
 *  - 请求拦截器自动加 Authorization
 *  - 响应拦截器把后端统一 Result 解包；非 2xx 或 code!=200 弹错并返回 reject
 */
const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

export interface Result<T = unknown> {
  code: number
  message?: string
  data?: T
}

http.interceptors.response.use(
  (resp: AxiosResponse<Result>) => {
    const body = resp.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) return body.data as any
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body as any
  },
  (err) => {
    if (err.response?.status === 401) {
      const auth = useAuthStore()
      auth.clear()
      ElMessage.error('登录已失效，请重新登录')
      window.location.hash = '#/login'
    } else {
      ElMessage.error(err.response?.data?.message || err.message || '网络错误')
    }
    return Promise.reject(err)
  }
)

export default http

import axios from 'axios'
import router from '../router'

const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器（修正版）
service.interceptors.response.use(
  // ✅ 关键修正：直接返回 response，而不是 response.data
  // 这样在业务代码里，我们依然可以使用 res.data 来取值
  response => response, 
  error => {
    if (error.response && error.response.status === 401) {
      alert('登录已过期或未登录，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default service
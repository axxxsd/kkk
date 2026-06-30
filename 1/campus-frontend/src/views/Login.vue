<template>
  <div class="login-container">
    <div class="login-box">
      <h2>用户登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名</label>
          <input 
            type="text" 
            v-model="username" 
            placeholder="请输入用户名" 
            required 
            :disabled="loading"
          />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input 
            type="password" 
            v-model="password" 
            placeholder="请输入密码" 
            required 
            :disabled="loading"
          />
        </div>
        <button type="submit" class="btn-login" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>
      <p class="tip">测试账号：student001 / 123456</p>
      <p class="register-tip">
        没有账号？
        <router-link to="/register">去注册</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import service from '../utils/request'

const router = useRouter()
const userStore = useUserStore()
const username = ref('')
const password = ref('')
const loading = ref(false)

// ✅ 初始化 Store
onMounted(() => {
  userStore.initialize()
  if (userStore.isLoggedIn) {
    router.push('/')
  }
})

const handleLogin = async () => {
  if (!username.value.trim() || !password.value.trim()) {
    alert('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const res = await service.post('/user/login', {
      username: username.value,
      password: password.value
    })

    if (res.data.code === 200) {
      alert('登录成功！')
      // ✅ 关键：传入两个参数
      userStore.login(res.data.userInfo, res.data.token)
      router.push('/')
    } else {
      alert(res.data.msg || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        alert('账号或密码错误')
      } else if (status === 404) {
        alert('后端接口不存在，请检查服务是否启动')
      } else if (status >= 500) {
        alert('服务器繁忙，请稍后再试')
      } else {
        alert(`登录失败：${error.response.data?.msg || '未知错误'}`)
      }
    } else {
      alert('无法连接后端服务，请检查服务是否启动在8080端口')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container { 
  display: flex; 
  justify-content: center; 
  align-items: center; 
  height: 100vh; 
  background: #f0f2f5; 
}
.login-box { 
  width: 400px; 
  padding: 40px; 
  background: #fff; 
  border-radius: 12px; 
  box-shadow: 0 10px 30px rgba(0,0,0,0.1); 
}
h2 { 
  text-align: center; 
  margin-bottom: 30px; 
  color: #2c3e50; 
  font-size: 1.8rem;
}
.form-group { 
  margin-bottom: 20px; 
}
.form-group label { 
  display: block; 
  margin-bottom: 8px; 
  color: #666; 
  font-size: 0.95rem;
}
.form-group input { 
  width: 100%; 
  padding: 12px 16px; 
  border: 1px solid #ddd; 
  border-radius: 8px; 
  box-sizing: border-box; 
  font-size: 0.95rem;
  transition: all 0.3s;
}
.form-group input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.2);
}
.btn-login { 
  width: 100%; 
  padding: 12px; 
  background: linear-gradient(90deg, #3498db, #2980b9); 
  color: white; 
  border: none; 
  border-radius: 20px; 
  font-size: 16px; 
  font-weight: bold;
  cursor: pointer; 
  transition: all 0.3s;
  margin-top: 10px;
}
.btn-login:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(52, 152, 219, 0.4);
}
.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}
.tip { 
  text-align: center; 
  margin-top: 20px; 
  color: #999; 
  font-size: 0.85rem; 
}
.register-tip {
  text-align: center;
  margin-top: 10px;
  color: #666;
  font-size: 0.9rem;
}
.register-tip a {
  color: #3498db;
  text-decoration: none;
  font-weight: bold;
}
.register-tip a:hover {
  text-decoration: underline;
}
</style>
<template>
  <div class="publish-container">
    <div class="publish-box">
      <h2>发布商品</h2>
      <form @submit.prevent="handlePublish">
        <div class="form-group">
          <label>商品标题</label>
          <input type="text" v-model="product.title" placeholder="例如：九成新 iPad" required />
        </div>
        <div class="form-group">
          <label>价格 (元)</label>
          <input type="number" v-model="product.price" placeholder="例如：2000" required />
        </div>
        <!-- 👇 核心修改：分类从输入框改为固定下拉框 -->
        <div class="form-group">
          <label>商品分类</label>
          <select v-model="product.category" class="category-select" required>
            <option value="" disabled selected>请选择商品分类</option>
            <option value="数码电子">数码电子</option>
            <option value="书籍教材">书籍教材</option>
            <option value="生活用品">生活用品</option>
            <option value="运动户外">运动户外</option>
            <option value="美妆护肤">美妆护肤</option>
            <option value="其他闲置">其他闲置</option>
          </select>
          <small style="color: #666; display: block; margin-top: 5px;">

          </small>
        </div>
        <div class="form-group">
          <label>商品描述</label>
          <textarea v-model="product.description" rows="4" placeholder="详细描述商品的新旧程度、使用情况..."></textarea>
        </div>
        <div class="form-group">
          <label>图片文件名</label>
          <input type="text" v-model="product.images" placeholder="例如：1.png" />
          <small style="color: #666; display: block; margin-top: 5px;">
            注意：图片必须已存在于后端的 uploads 文件夹中
          </small>
        </div>
        <button type="submit" class="btn-publish" :disabled="publishing">
          {{ publishing ? '发布中...' : '确认发布' }}
        </button>
      </form>
      
      <!-- 调试信息面板（完全保留你原来的代码，未做任何修改） -->
      <div class="debug-panel" v-if="debugMode">
        <h3>🛠️ 调试信息</h3>
        <div class="debug-item">
          <strong>请求URL:</strong> {{ requestUrl }}
        </div>
        <div class="debug-item">
          <strong>登录状态:</strong> {{ isLoggedIn ? '✅ 已登录' : '❌ 未登录' }}
        </div>
        <div class="debug-item">
          <strong>Token存在:</strong> {{ hasToken ? '✅ 是' : '❌ 否' }}
        </div>
        <div class="debug-item">
          <strong>用户信息:</strong> {{ userInfo ? userInfo.username : '未获取' }}
        </div>
        <div class="debug-buttons">
          <button @click="testBackend" class="btn-test">测试后端连接</button>
          <button @click="checkLoginStatus" class="btn-test">检查登录状态</button>
          <button @click="debugMode = !debugMode" class="btn-test">
            {{ debugMode ? '隐藏调试' : '显示调试' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import service from '../utils/request'

const router = useRouter()

const product = ref({
  title: '',
  price: '',
  category: '', // 现在只会是下拉框选中的固定值，不会乱填
  description: '',
  images: '1.png'
})

const publishing = ref(false)

const handlePublish = async () => {
  if (!product.value.title.trim()) {
    alert('请输入商品标题！')
    return
  }
  
  if (!product.value.price || product.value.price <= 0) {
    alert('请输入有效的价格！')
    return
  }

  // 👇 新增校验：必须选择分类才能发布
  if (!product.value.category) {
    alert('请选择商品分类！')
    return
  }
  
  publishing.value = true
  
  try {
    console.log('开始发布商品...')
    console.log('请求URL: /product/publish')
    console.log('请求数据:', product.value)
    
    const res = await service.post('/product/publish', product.value)
    
    console.log('发布成功，响应:', res)
    alert('✅ 发布成功！')
    router.push('/')
    
  } catch (error) {
    console.error('发布失败，详细错误:', error)
    
    let errorMessage = '发布失败！'
    
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      
      console.error(`HTTP状态码: ${status}`)
      console.error('响应数据:', data)
      
      if (status === 401) {
        errorMessage = '❌ 请先登录！'
        router.push('/login')
      } else if (status === 403) {
        errorMessage = '❌ 没有权限！'
      } else if (status === 404) {
        errorMessage = '❌ 接口不存在！请检查后端服务。'
      } else if (status === 500) {
        errorMessage = '❌ 服务器内部错误！请检查后端日志。'
      } else {
        errorMessage = `❌ 请求失败(${status}): ${data}`
      }
    } else if (error.request) {
      console.error('无响应:', error.request)
      errorMessage = '❌ 无法连接到服务器！请检查后端是否启动在8080端口。'
    } else {
      console.error('请求配置错误:', error.message)
      errorMessage = `❌ 请求配置错误: ${error.message}`
    }
    
    alert(errorMessage)
  } finally {
    publishing.value = false
  }
}

// 页面加载时检查登录状态
onMounted(() => {
  const token = localStorage.getItem('token')
  if (!token) {
    alert('请先登录！')
    router.push('/login')
  }
})
</script>

<style scoped>
.publish-container { 
  padding: 40px 20px; 
  background: #f0f2f5; 
  min-height: 100vh; 
}
.publish-box { 
  max-width: 600px; 
  margin: 0 auto; 
  padding: 40px; 
  background: #fff; 
  border-radius: 12px; 
  box-shadow: 0 4px 15px rgba(0,0,0,0.06);
}
h2 { 
  text-align: center; 
  margin-bottom: 30px; 
  color: #2c3e50;
}
.form-group { 
  margin-bottom: 20px; 
}
.form-group label { 
  display: block; 
  margin-bottom: 8px; 
  font-weight: bold; 
  color: #333;
}
.form-group input, 
.form-group textarea {
  width: 100%; 
  padding: 12px; 
  border: 1px solid #ddd; 
  border-radius: 8px; 
  box-sizing: border-box; 
  font-size: 0.95rem;
  transition: border-color 0.3s;
}
/* 👇 新增：下拉框样式，和原有输入框完全统一 */
.form-group select.category-select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-sizing: border-box;
  font-size: 0.95rem;
  transition: border-color 0.3s;
  background: #fff;
  appearance: none; /* 去掉默认下拉箭头，统一风格 */
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24'%3E%3Cpath fill='%23999' d='M7 10l5 5 5-5z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 36px; /* 给箭头留位置 */
}
.form-group input:focus,
.form-group textarea:focus,
.form-group select.category-select:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.2);
}
.btn-publish { 
  width: 100%; 
  padding: 14px; 
  background: linear-gradient(90deg, #3498db, #2980b9); 
  color: white; 
  border: none; 
  border-radius: 8px; 
  font-size: 16px; 
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}
.btn-publish:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(52, 152, 219, 0.4);
}
.btn-publish:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 调试面板样式（完全保留你原来的代码，未做任何修改） */
.debug-panel {
  margin-top: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #3498db;
}
.debug-panel h3 {
  margin-top: 0;
  color: #2c3e50;
}
.debug-item {
  margin: 8px 0;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 0.85rem;
  word-break: break-all;
}
.debug-buttons {
  display: flex;
  gap: 10px;
  margin-top: 15px;
  flex-wrap: wrap;
}
.btn-test {
  padding: 8px 16px;
  background: #ecf0f1;
  color: #333;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.3s;
}
.btn-test:hover {
  background: #dcdcdc;
}
</style>
<template>
  <div id="app">
    <!-- 顶部导航栏（完全保留你之前的样式和逻辑） -->
    <nav class="navbar">
      <div class="logo">校园二手</div>
      <div class="nav-links">
        <template v-if="!userStore.isLoggedIn">
          <button class="nav-btn primary" @click="goToLogin">登录</button>
          <button class="nav-btn secondary" @click="goToRegister">注册</button>
        </template>
        <template v-else>
          <span class="welcome">欢迎，{{ userStore.userInfo.username }}！</span>
          <button class="nav-btn primary" @click="goToPublish">发布商品</button>
          <button class="nav-btn secondary" @click="goToOrders">我的订单</button>
          <button class="nav-btn secondary" @click="goToCategory">分类</button>
          <button class="nav-btn secondary" @click="goToProfile">个人中心</button>
          <button class="nav-btn secondary message-btn" @click="goToMessages">
            我的消息
            <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
          </button>
          <button class="nav-btn danger" @click="logout">退出登录</button>
        </template>
      </div>
    </nav>

    <!-- 搜索区域 -->
    <div class="search-section">
      <input 
        v-model="searchTitle" 
        type="text" 
        placeholder="搜索商品标题..." 
        class="search-input"
      />
      <input 
        v-model="searchPrice" 
        type="number" 
        placeholder="最高价格" 
        class="search-input"
      />
      <button class="btn primary" @click="fetchData">查询</button>
      <button class="btn secondary" @click="resetSearch">重置</button>
      <span v-if="currentCategory" class="current-category">当前分类：{{ currentCategory }}</span>
    </div>

    <!-- 商品列表区域 -->
    <div class="content">
      <h2>商品列表</h2>
      <div v-if="loading" class="status-message">正在加载数据中...</div>
      <div v-else-if="productList.length === 0" class="status-message empty">
        😢 暂无该类商品，快去发布第一个吧！
      </div>

      <div v-else class="card-list">
        <div 
          v-for="item in productList" 
          :key="item.id" 
          class="product-card"
          @click="goToPurchase(item.id)"
          style="cursor: pointer;"
        >
          <div class="image-wrapper">
            <img 
              :src="getImageUrl(item.images)" 
              :alt="item.title"
              class="product-image"
              @error="handleImageError"
            />
          </div>
          <div class="card-content">
            <h3>{{ item.title }}</h3>
            <!-- ✅ 关键修复：p标签严格闭合，不会再报错 -->
            <p class="desc">{{ item.description }}</p>
            <div class="meta">
              <span class="price">¥ {{ item.price }}</span>
              <span class="category-tag">{{ item.category || '未分类' }}</span>
              <template v-if="userStore.userInfo?.role === 1">
                <button class="offline-btn" @click.stop="handleOffline(item.id)">下架</button>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user' // ✅ 使用Pinia Store
import service from '../utils/request'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式变量
const productList = ref([])
const loading = ref(false)
const searchTitle = ref('')
const searchPrice = ref('')
const unreadCount = ref(0)
const currentCategory = ref('')
let timer = null

// 常量
const backendBaseUrl = 'http://localhost:8080'
const defaultImage = `${backendBaseUrl}/uploads/1.png`

// 图片路径处理
const getImageUrl = (imagePath) => {
  if (!imagePath) return defaultImage
  return imagePath.startsWith('http') ? imagePath : `${backendBaseUrl}/uploads/${imagePath}`
}

// 图片加载失败兜底
const handleImageError = (event) => {
  event.target.src = defaultImage
}

// 获取商品数据（支持分类筛选和搜索）
const fetchData = async (category) => {
  loading.value = true
  try {
    let res
    if (category) {
      // 调用分类查询接口（和你的后端接口完全匹配）
      res = await service.get('/product/listByCategory', {
        params: { category }
      })
    } else {
      // 无分类参数，查询全部
      const params = {}
      if (searchTitle.value) params.title = searchTitle.value
      if (searchPrice.value) params.price = searchPrice.value
      res = await service.get('/product/list', { params })
    }
    productList.value = res.data || []
  } catch (error) {
    console.error("获取数据失败:", error)
    alert("商品加载失败，请检查网络或后端服务！")
  } finally {
    loading.value = false
  }
}

// 获取未读消息数
const fetchUnreadCount = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await service.get('/message/unread/count')
    unreadCount.value = res.data || 0
  } catch (e) {
    console.error("获取未读数失败", e)
  }
}

// 下架商品
const handleOffline = async (productId) => {
  if (!confirm('确定要下架这个商品吗？')) return
  try {
    const res = await service.put(`/product/offline/${productId}`)
    if (res.data.includes('成功')) {
      alert('商品下架成功！')
      fetchData(currentCategory.value)
    } else {
      alert(res.data || '下架失败，请重试')
    }
  } catch (error) {
    alert(error.response?.data || '下架失败，请检查权限或重新登录')
  }
}

// 重置搜索
const resetSearch = () => {
  searchTitle.value = ''
  searchPrice.value = ''
  fetchData(currentCategory.value)
}

// 跳转方法
const goToLogin = () => router.push('/login')
const goToRegister = () => router.push('/register')
const goToPublish = () => router.push('/publish')
const goToPurchase = (id) => router.push(`/purchase/${id}`)
const goToOrders = () => router.push('/orders')
const goToMessages = () => router.push('/messages')
const goToCategory = () => router.push('/category')
const goToProfile = () => router.push('/profile')

// 退出登录（调用Pinia的logout方法，同步清除所有状态）
const logout = () => {
  userStore.logout()
  unreadCount.value = 0
  alert('已退出登录')
  router.push('/login')
}

// 监听路由分类参数变化
watch(
  () => route.query.category,
  (newCategory) => {
    currentCategory.value = newCategory || ''
    fetchData(newCategory)
  },
  { immediate: true }
)

// 初始化
onMounted(() => {
  // 初始化用户状态（从localStorage恢复，解决刷新丢失问题）
  userStore.initialize()
  // 加载商品数据
  fetchData(currentCategory.value)
  // 启动未读消息轮询
  timer = setInterval(fetchUnreadCount, 30000)
})

onUnmounted(() => {
  // 清除定时器，避免内存泄漏
  clearInterval(timer)
})
</script>

<style scoped>
/* 完全保留你之前的所有样式，无任何修改 */
.current-category {
  padding: 8px 16px;
  background: #eaf6ff;
  color: #3498db;
  border-radius: 20px;
  font-size: 0.9rem;
  margin-left: 12px;
}
.message-btn { position: relative; }
.badge { position: absolute; top: -5px; right: -5px; background: #e74c3c; color: white; border-radius: 50%; width: 18px; height: 18px; font-size: 12px; display: flex; align-items: center; justify-content: center; }
.navbar { display: flex; justify-content: space-between; align-items: center; padding: 15px 40px; background: #fff; box-shadow: 0 2px 10px rgba(0,0,0,0.05); margin-bottom: 20px; position: sticky; top: 0; z-index: 100; }
.logo { font-size: 1.5rem; font-weight: bold; color: #3498db; display: flex; align-items: center; gap: 8px; }
.logo::before { content: "🛒"; font-size: 1.2rem; }
.nav-links { display: flex; align-items: center; gap: 15px; }
.welcome { color: #666; font-size: 0.9rem; margin-right: 10px; }
.nav-btn { padding: 8px 18px; border-radius: 20px; border: none; cursor: pointer; font-weight: bold; transition: all 0.3s; font-size: 0.95rem; }
.nav-btn.primary { background: #3498db; color: white; }
.nav-btn.primary:hover { background: #2980b9; }
.nav-btn.secondary { background-color: #ecf0f1; color: #7f8c8d; border: 1px solid #dcdcdc; }
.nav-btn.secondary:hover { background-color: #dcdcdc; }
.nav-btn.danger { background: #e74c3c; color: white; }
.nav-btn.danger:hover { background: #c0392b; }
.search-section { display: flex; gap: 12px; justify-content: center; margin-bottom: 30px; flex-wrap: wrap; padding: 0 20px; }
.search-input { padding: 10px 18px; border: 1px solid #dcdcdc; background-color: #fff; color: #333; border-radius: 30px; outline: none; transition: all 0.3s; min-width: 180px; font-size: 0.95rem; }
.search-input:focus { border-color: #3498db; box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.2); }
.btn { padding: 10px 22px; border: none; border-radius: 30px; cursor: pointer; font-weight: bold; transition: all 0.3s; font-size: 0.95rem; white-space: nowrap; }
.btn.primary { background: linear-gradient(90deg, #3498db, #2980b9); color: white; }
.btn.primary:hover { opacity: 0.9; transform: translateY(-2px); }
.btn.secondary { background-color: #ecf0f1; color: #7f8c8d; border: 1px solid #dcdcdc; }
.btn.secondary:hover { background-color: #dcdcdc; }
.content { width: 100%; max-width: 1400px; margin: 0 auto; padding: 0 20px; box-sizing: border-box; }
.content h2 { text-align: left; color: #2c3e50; font-size: 1.8rem; margin-bottom: 20px; font-weight: 700; }
.card-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 24px; }
.product-card { background: #ffffff; border-radius: 12px; padding: 20px; box-shadow: 0 4px 15px rgba(0,0,0,0.06); transition: all 0.3s ease; overflow: hidden; border: none; }
.product-card:hover { transform: translateY(-6px); box-shadow: 0 8px 25px rgba(0,0,0,0.1); }
.image-wrapper { width: 100%; height: 180px; overflow: hidden; border-radius: 8px; margin-bottom: 16px; background-color: #f5f5f5; }
.product-image { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s ease; }
.product-card:hover .product-image { transform: scale(1.05); }
.card-content h3 { margin: 0 0 10px 0; color: #2c3e50; font-size: 1.1rem; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.desc { font-size: 0.9rem; color: #666; margin-bottom: 14px; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; text-overflow: ellipsis; min-height: 42px; }
.meta { display: flex; justify-content: space-between; align-items: center; }
.price { font-size: 1.3rem; font-weight: bold; color: #e74c3c; }
.category-tag { background-color: #eaf6ff; color: #3498db; padding: 4px 10px; border-radius: 20px; font-size: 0.75rem; font-weight: 500; border: 1px solid #d6eaf8; }
.offline-btn { padding: 4px 12px; border-radius: 20px; border: none; background-color: #e74c3c; color: white; font-size: 0.75rem; cursor: pointer; transition: all 0.3s; margin-left: 8px; }
.offline-btn:hover { background-color: #c0392b; transform: translateY(-1px); }
.status-message { text-align: center; font-size: 1.1rem; color: #999; margin-top: 40px; padding: 20px; background: #fff; border-radius: 12px; }
.status-message.empty { color: #95a5a6; }
</style>
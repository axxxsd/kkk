<template>
  <div class="my-products-page">
    <div class="header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h2>我发布的商品</h2>
    </div>

    <div v-if="loading" class="status-message">加载中...</div>
    <div v-else-if="products.length === 0" class="status-message empty">
      😢 你还没有发布过商品，快去发布吧！
    </div>

    <div v-else class="product-list">
      <div v-for="item in products" :key="item.id" class="product-card">
        <img :src="getImageUrl(item.images)" :alt="item.title" class="product-img" />
        <div class="product-info">
          <h3>{{ item.title }}</h3>
          <p class="price">¥ {{ item.price }}</p>
          <p class="category">{{ item.category }}</p>
          <div class="status-and-action">
            <span :class="['status', item.status === 1 ? 'on-sale' : 'off-sale']">
              {{ item.status === 1 ? '在售' : '已下架' }}
            </span>
            <button 
              v-if="item.status === 1" 
              class="offline-btn" 
              @click.stop="handleOffline(item.id)"
            >
              下架
            </button>
          </div>
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
const products = ref([])
const loading = ref(true)

// 获取我的商品（适配你现有的/my接口返回格式）
const fetchMyProducts = async () => {
  try {
    const res = await service.get('/product/my')
    if (res.data.code === 200) {
      products.value = res.data.data || []
    } else {
      alert(res.data.msg || '获取商品失败')
    }
  } catch (error) {
    console.error('获取我的商品失败:', error)
    alert('获取商品失败，请检查登录状态')
  } finally {
    loading.value = false
  }
}

// 图片路径处理（和你Home.vue逻辑一致）
const getImageUrl = (path) => {
  if (!path) return 'https://via.placeholder.com/150'
  return path.startsWith('http') ? path : `http://localhost:8080/uploads/${path}`
}

// 下架商品（适配你修改后的offline接口）
const handleOffline = async (id) => {
  if (!confirm('确定要下架这个商品吗？')) return
  try {
    const res = await service.put(`/product/offline/${id}`)
    if (res.data.includes('成功')) {
      alert('下架成功！')
      fetchMyProducts() // 刷新列表
    } else {
      alert(res.data || '下架失败')
    }
  } catch (error) {
    alert('下架失败，请检查权限')
  }
}

const goBack = () => router.back()

onMounted(() => {
  fetchMyProducts()
})
</script>

<style scoped>
.my-products-page {
  padding: 20px;
  background: #f0f2f5;
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

.back-btn {
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 20px;
  cursor: pointer;
  margin-right: 20px;
}

h2 {
  margin: 0;
  color: #2c3e50;
}

.status-message {
  text-align: center;
  padding: 40px;
  color: #666;
  background: #fff;
  border-radius: 12px;
  max-width: 1200px;
  margin: 0 auto;
}

.empty {
  color: #95a5a6;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0,0,0,0.06);
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
}

.product-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.product-info {
  padding: 15px;
}

.product-info h3 {
  margin: 0 0 10px;
  font-size: 1.1rem;
  color: #2c3e50;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.price {
  font-size: 1.3rem;
  font-weight: bold;
  color: #e74c3c;
  margin-bottom: 5px;
}

.category {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 15px;
}

.status-and-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status {
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status.on-sale {
  background: #eaf6ff;
  color: #3498db;
}

.status.off-sale {
  background: #f8d7da;
  color: #721c24;
}

.offline-btn {
  padding: 6px 12px;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.3s;
}

.offline-btn:hover {
  background: #c0392b;
}
</style>
<template>
  <div class="category-page">
    <button class="back-btn" @click="goBack">← 返回首页</button>
    <h2>商品分类</h2>
    <div class="category-grid">
      <div 
        v-for="cat in categories" 
        :key="cat.name" 
        class="category-card"
        @click="filterByCategory(cat.name)"
      >
        <div class="icon">{{ cat.icon }}</div>
        <div class="name">{{ cat.name }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 分类名称和数据库里的category字段完全一致
const categories = [
  { name: '全部', icon: '📦' },
  { name: '数码电子', icon: '📱' },
  { name: '书籍教材', icon: '📚' },
  { name: '生活用品', icon: '🧴' },
  { name: '运动户外', icon: '⚽' },
  { name: '美妆护肤', icon: '💄' },
  { name: '其他闲置', icon: '🎁' },
]

// 跳回首页并携带分类参数
const filterByCategory = (category) => {
  router.push({ 
    path: '/', 
    query: { category }
  })
}

const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.category-page {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}
.back-btn {
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 20px;
  cursor: pointer;
  margin-bottom: 20px;
  transition: all 0.3s;
}
.back-btn:hover {
  background: #f5f5f5;
}
h2 {
  font-size: 1.8rem;
  color: #2c3e50;
  margin-bottom: 30px;
}
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 20px;
}
.category-card {
  background: #fff;
  border-radius: 12px;
  padding: 30px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}
.category-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
}
.icon {
  font-size: 2.5rem;
  margin-bottom: 12px;
}
.name {
  font-size: 1rem;
  color: #333;
  font-weight: 500;
}
</style>
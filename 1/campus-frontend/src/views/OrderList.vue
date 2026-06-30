<template>
  <div class="page-container">
    <button class="back-btn" @click="router.back()">← 返回</button>
    <h2>我的订单</h2>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else>
      <div v-if="orders.length === 0" class="empty">暂无订单</div>
      <div v-else class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <p><strong>订单号：</strong>{{ order.orderNo }}</p>
          <p><strong>商品ID：</strong>{{ order.productId }}</p>
          <p><strong>交易金额：</strong><span class="price">¥{{ order.totalPrice }}</span></p>
          <p><strong>交易地点：</strong>{{ order.address }}</p>
          <p><strong>状态：</strong>
            <span :class="['status', 'status-' + order.status]">
              {{ order.status === 1 ? '待交易' : order.status === 2 ? '已完成' : '已取消' }}
            </span>
          </p>
          <p><strong>创建时间：</strong>{{ formatDate(order.createdAt) }}</p>
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
const orders = ref([])
const loading = ref(true)

const fetchOrders = async () => {
  try {
    const res = await service.get('/order/my')
    orders.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleString()
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.page-container { padding: 20px; max-width: 1000px; margin: 0 auto; }
.back-btn { padding: 8px 16px; background: #eee; border: none; border-radius: 20px; cursor: pointer; margin-bottom: 20px; }
h2 { color: #2c3e50; }
.loading, .empty { text-align: center; padding: 40px; color: #999; }
.order-list { display: flex; flex-direction: column; gap: 20px; }
.order-card { background: #fff; padding: 20px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.order-card p { margin: 8px 0; color: #333; }
.price { color: #e74c3c; font-weight: bold; font-size: 1.1rem; }
.status { padding: 4px 10px; border-radius: 20px; font-size: 0.8rem; }
.status-1 { background: #fff3cd; color: #856404; }
.status-2 { background: #d4edda; color: #155724; }
.status-3 { background: #f8d7da; color: #721c24; }
</style>
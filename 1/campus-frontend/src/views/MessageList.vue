<template>
  <div class="page-container">
    <button class="back-btn" @click="router.back()">← 返回</button>
    <h2>我的消息</h2>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else>
      <div v-if="messages.length === 0" class="empty">暂无消息</div>
      <div v-else class="message-list">
        <div 
          v-for="msg in messages" 
          :key="msg.id" 
          class="message-card"
          :class="{ 'unread': msg.isRead === 0 }"
          @click="markAsRead(msg.id)"
        >
          <p class="content">{{ msg.content }}</p>
          <p class="time">{{ formatDate(msg.createdAt) }}</p>
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
const messages = ref([])
const loading = ref(true)

const fetchMessages = async () => {
  try {
    const res = await service.get('/message/my')
    messages.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const markAsRead = async (id) => {
  try {
    await service.put(`/message/read/${id}`)
    // 更新本地状态
    const msg = messages.value.find(m => m.id === id)
    if (msg) msg.isRead = 1
  } catch (e) {
    console.error(e)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleString()
}

onMounted(() => {
  fetchMessages()
})
</script>

<style scoped>
.page-container { padding: 20px; max-width: 1000px; margin: 0 auto; }
.back-btn { padding: 8px 16px; background: #eee; border: none; border-radius: 20px; cursor: pointer; margin-bottom: 20px; }
h2 { color: #2c3e50; }
.loading, .empty { text-align: center; padding: 40px; color: #999; }
.message-list { display: flex; flex-direction: column; gap: 15px; }
.message-card { background: #fff; padding: 15px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); cursor: pointer; transition: background 0.3s; }
.message-card.unread { border-left: 4px solid #3498db; background: #f0f8ff; }
.message-card .content { margin: 0 0 8px 0; color: #333; line-height: 1.5; }
.message-card .time { margin: 0; color: #999; font-size: 0.85rem; }
</style>
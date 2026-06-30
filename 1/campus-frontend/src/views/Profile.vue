<template>
  <div class="profile-page">
    <div class="profile-card">
      <div class="avatar-section">
        <img :src="userInfo.avatar || defaultAvatar" alt="头像" class="avatar" />
        <h2>{{ userInfo.nickname || userInfo.username }}</h2>
        <span v-if="userInfo.role === 1" class="role-badge">管理员</span>
      </div>
      
      <div class="info-section">
        <div class="info-item">
          <span class="label">用户名</span>
          <span class="value">{{ userInfo.username }}</span>
        </div>
        <div class="info-item">
          <span class="label">手机号</span>
          <span class="value">{{ userInfo.phone || '未绑定' }}</span>
        </div>
        <div class="info-item">
          <span class="label">用户ID</span>
          <span class="value">{{ userInfo.id }}</span>
        </div>
        <div class="info-item">
          <span class="label">登录状态</span>
          <span class="value" :class="{ 'status-online': isLoggedIn, 'status-offline': !isLoggedIn }">
            {{ isLoggedIn ? '在线' : '离线' }}
          </span>
        </div>
      </div>

      <div class="action-list">
        <div class="action-item" @click="goToMyProducts">
          <span>📦 我发布的商品</span>
          <span class="arrow">›</span>
        </div>
        <div class="action-item" @click="goToOrders">
          <span>📋 我的订单</span>
          <span class="arrow">›</span>
        </div>
        <div class="action-item" @click="goToMessages">
          <span>💬 我的消息</span>
          <span class="arrow">›</span>
        </div>
        <div class="action-item danger" @click="handleLogout">
          <span>🚪 退出登录</span>
          <span class="arrow">›</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user' // ✅ 关键：引入Pinia Store
import service from '../utils/request'

const router = useRouter()
const userStore = useUserStore() // ✅ 获取Store实例

// ✅ 关键：从Store中获取用户信息，而不是本地ref
const userInfo = computed(() => userStore.userInfo || {})
const isLoggedIn = computed(() => userStore.isLoggedIn)

const defaultAvatar = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'

// ✅ 移除原来的fetchUserInfo方法，改为从Store初始化
onMounted(() => {
  // 初始化Store（从localStorage恢复数据）
  userStore.initialize()
  
  // 如果Store中没有用户信息，跳转到登录页
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  
  // 可选：如果需要实时获取最新用户信息，可以调用后端接口
  // fetchLatestUserInfo()
})

// 可选：从后端获取最新用户信息（如果Store中的数据可能不是最新的）
const fetchLatestUserInfo = async () => {
  try {
    const res = await service.get('/user/info')
    if (res.data && res.data.code === 200) {
      // 更新Store中的数据
      userStore.login(res.data.data, userStore.token)
    }
  } catch (error) {
    console.error('获取最新用户信息失败:', error)
  }
}

const goToMyProducts = () => router.push('/my-products')
const goToOrders = () => router.push('/orders')
const goToMessages = () => router.push('/messages')

// ✅ 关键：使用Store的logout方法，确保状态同步清除
const handleLogout = () => {
  userStore.logout() // 这会清除Store和localStorage中的数据
  alert('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.profile-page {
  padding: 20px;
  background: #f0f2f5;
  min-height: 100vh;
}

.profile-card {
  max-width: 600px;
  margin: 0 auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.06);
  overflow: hidden;
}

.avatar-section {
  text-align: center;
  padding: 40px 20px 20px;
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 4px solid rgba(255,255,255,0.5);
  object-fit: cover;
  margin-bottom: 15px;
}

.avatar-section h2 {
  margin: 0 0 10px;
  font-size: 1.5rem;
}

.role-badge {
  background: rgba(231, 76, 60, 0.9);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
}

.info-section {
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  font-size: 0.95rem;
}

.info-item .label {
  color: #666;
}

.info-item .value {
  color: #333;
  font-weight: 500;
}

/* 新增：登录状态样式 */
.status-online {
  color: #27ae60;
  font-weight: bold;
}

.status-offline {
  color: #e74c3c;
  font-weight: bold;
}

.action-list {
  padding: 10px 0;
}

.action-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.3s;
}

.action-item:hover {
  background: #f9f9f9;
}

.action-item.danger {
  color: #e74c3c;
}

.action-item .arrow {
  color: #ccc;
  font-size: 1.5rem;
}
</style>
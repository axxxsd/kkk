<template>
  <div class="purchase-container">
    <!-- 顶部导航栏 -->
    <div class="header-bar">
      <button class="back-btn" @click="goBack">
        ← 返回
      </button>
      <h2>商品购买</h2>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="status-message">加载中...</div>
    <!-- 商品不存在状态 -->
    <div v-else-if="!product" class="status-message empty">商品不存在或已被删除</div>

    <!-- 商品详情&购买表单 -->
    <div v-else class="purchase-content">
      <!-- 商品信息卡片 -->
      <div class="product-card">
        <div class="image-wrapper">
          <img :src="getImageUrl(product.images)" alt="商品图片" @error="handleImageError" />
        </div>
        <div class="info">
          <h3>{{ product.title }}</h3>
          <p class="desc">{{ product.description }}</p>
          <!-- ✅ 修复：价格格式化为人民币显示 -->
          <div class="price">¥{{ formatPrice(product.price) }}</div>
        </div>
      </div>

      <!-- 购买表单卡片 -->
      <div class="form-card">
        <h3>确认交易信息</h3>
        
        <!-- 不能买自己商品的提示 -->
        <div v-if="isOwner" class="warning-box">
          ⚠️ 不能购买自己的商品！
        </div>

        <div class="form-group">
          <label>交易地点 / 收货地址</label>
          <input
            type="text"
            v-model="address"
            placeholder="请输入宿舍号、楼栋或详细地址"
          />
        </div>

        <div class="tips">
          <strong>💡 温馨提示：</strong>
          <ul>
            <li>二手交易请当面验货，确认无误后再确认收货</li>
            <li>建议在校内公共场所（食堂/图书馆门口）交易</li>
            <li>交易完成后请及时在「我的订单」中确认收货</li>
          </ul>
        </div>

        <button
          class="submit-btn"
          :disabled="isOwner || !address.trim()"
          @click="handlePurchase"
        >
          {{ isOwner ? '无法购买自己发布的商品' : '确认购买' }}
        </button>
      </div>

      <!-- 👇 新增：评论区模块（完全适配现有风格） -->
      <div class="comment-section">
        <h3>商品评价 ({{ comments.length }})</h3>

        <!-- 1. 发表评论（仅登录用户可见） -->
        <div v-if="userInfo.id" class="comment-editor">
          <div class="editor-header">
            <span class="editor-user">当前用户：{{ userInfo.username }}</span>
            <div class="score-picker">
              <label>评分：</label>
              <select v-model="newComment.score">
                <option value="5">⭐⭐⭐⭐⭐ 5分</option>
                <option value="4">⭐⭐⭐⭐ 4分</option>
                <option value="3">⭐⭐⭐ 3分</option>
                <option value="2">⭐⭐ 2分</option>
                <option value="1">⭐ 1分</option>
              </select>
            </div>
          </div>
          <textarea
            v-model="newComment.content"
            placeholder="写下你对商品的真实评价..."
            rows="4"
            class="comment-input"
          ></textarea>
          <button
            class="comment-submit"
            :disabled="submitting || !newComment.content.trim()"
            @click="submitComment"
          >
            {{ submitting ? '发布中...' : '发表评论' }}
          </button>
        </div>
        <div v-else class="comment-login-tip">
          请 <a href="/login" @click.prevent="goToLogin">登录</a> 后参与评论
        </div>

        <!-- 2. 评论列表 -->
        <div class="comment-list">
          <!-- 加载状态 -->
          <div v-if="loadingComments" class="comment-loading">
            <span class="loading-spinner"></span>
            加载评论中...
          </div>
          <!-- 空状态 -->
          <div v-else-if="comments.length === 0" class="comment-empty">
            😊 暂无评价，快来抢沙发吧！
          </div>
          <!-- 评论项 -->
          <div v-else class="comment-items">
            <div v-for="item in comments" :key="item.id" class="comment-item">
              <div class="comment-meta">
                <span class="comment-user">用户{{ item.userId }}</span>
                <span class="comment-score">
                  <span v-for="_ in item.score" :key="'full-' + _">⭐</span>
                  <span v-for="_ in (5 - item.score)" :key="'empty-' + _">☆</span>
                  <span class="score-num">{{ item.score }}.0</span>
                </span>
                <span class="comment-time">{{ formatTime(item.createdAt) }}</span>
                <!-- 管理员删除按钮 -->
                <button
                  v-if="userInfo.role === 1"
                  class="comment-delete"
                  @click="deleteComment(item.id)"
                >
                  删除
                </button>
              </div>
              <div class="comment-content">{{ item.content }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import service from '../utils/request'

// 路由参数和跳转实例
const route = useRoute()
const router = useRouter()

// 👇 原有变量（完全保留）
const productId = ref(route.params.id)
const product = ref(null)
const loading = ref(true)
const address = ref('')
const isOwner = ref(false)

// 👇 新增：用户信息（从localStorage提取，供评论模块使用）
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

// 👇 新增：评论相关变量
const comments = ref([])
const loadingComments = ref(false)
const submitting = ref(false)
const newComment = ref({
  content: '',
  score: 5
})

/**
 * 获取商品详情（原有逻辑，仅优化userInfo提取）
 */
const fetchProduct = async () => {
  loading.value = true
  try {
    const res = await service.get(`/product/${productId.value}`)
    if (!res.data) {
      alert('商品不存在或已被下架')
      router.push('/')
      return
    }
    product.value = res.data
    // 判断是否为自己的商品
    if (userInfo.value.id === product.value.userId) {
      isOwner.value = true
    }
  } catch (error) {
    console.error('获取商品失败:', error)
    alert('商品加载失败，请检查网络')
    router.push('/')
  } finally {
    loading.value = false
  }
}

/**
 * 👇 新增：获取商品评论列表
 */
const fetchComments = async () => {
  loadingComments.value = true
  try {
    const res = await service.get(`/comment/list/${productId.value}`)
    if (res.data.code === 200) {
      comments.value = res.data.data
    } else {
      console.error('获取评论失败:', res.data.msg)
    }
  } catch (error) {
    console.error('获取评论接口异常:', error)
  } finally {
    loadingComments.value = false
  }
}

/**
 * 👇 新增：提交评论
 */
const submitComment = async () => {
  if (!newComment.value.content.trim()) {
    alert('评论内容不能为空')
    return
  }
  if (!userInfo.value.id) {
    alert('请先登录')
    goToLogin()
    return
  }
  submitting.value = true
  try {
    const res = await service.post('/comment/publish', {
      productId: parseInt(productId.value),
      content: newComment.value.content,
      score: parseInt(newComment.value.score) // ✅ 转换为整数
    })
    if (res.data.code === 200) {
      alert('评论发布成功！')
      newComment.value.content = '' // 清空输入框
      newComment.value.score = 5 // 重置评分
      fetchComments() // 刷新评论列表
    } else {
      alert(res.data.msg || '评论发布失败')
    }
  } catch (error) {
    console.error('提交评论失败:', error)
    alert('评论发布失败，请检查网络或登录状态')
  } finally {
    submitting.value = false
  }
}

/**
 * 👇 新增：删除评论（仅管理员）
 */
const deleteComment = async (commentId) => {
  if (!confirm('确定要删除这条评论吗？')) return
  try {
    const res = await service.put(`/comment/delete/${commentId}`)
    if (res.data.includes('成功')) {
      alert('评论删除成功')
      fetchComments() // 刷新评论列表
    } else {
      alert(res.data || '删除失败')
    }
  } catch (error) {
    console.error('删除评论失败:', error)
    alert('删除失败，请检查权限')
  }
}

/**
 * 👇 新增：格式化价格（人民币格式）
 */
const formatPrice = (price) => {
  if (price === null || price === undefined) return '0.00'
  const numPrice = typeof price === 'string' ? parseFloat(price) : price
  return numPrice.toFixed(2) + ' 元'
}

/**
 * 👇 新增：格式化时间（适配后端LocalDateTime）
 */
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/**
 * 原有方法（完全保留）
 */
const getImageUrl = (path) => {
  if (!path) return 'https://via.placeholder.com/300x200?text=暂无图片'
  return path.startsWith('http') ? path : `http://localhost:8080/uploads/${path}`
}

const handleImageError = (e) => {
  e.target.src = 'https://via.placeholder.com/300x200?text=图片加载失败'
}

const handlePurchase = async () => {
  if (!address.value.trim()) {
    alert('请填写交易地址')
    return
  }
  try {
    await service.post('/order/create', {
      productId: product.value.id,
      totalPrice: product.value.price,
      address: address.value
    })
    alert('购买成功！已生成订单，请前往「我的订单」查看')
    router.push('/orders')
  } catch (error) {
    console.error('购买失败:', error)
    alert(`购买失败：${error.response?.data || '请稍后再试'}`)
  }
}

const goBack = () => {
  router.go(-1)
}

const goToLogin = () => {
  router.push('/login')
}

// 初始化加载（同时加载商品和评论）
onMounted(() => {
  fetchProduct()
  fetchComments()
})
</script>

<style scoped>
/* 原有样式（100%保留，无修改） */
.purchase-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  background-color: #f0f2f5;
  min-height: 100vh;
  box-sizing: border-box;
}

.header-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  height: 48px;
  margin-bottom: 24px;
}

.back-btn {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  cursor: pointer;
  color: #374151;
  font-size: 0.9rem;
  z-index: 10;
  transition: all 0.2s;
}

.back-btn:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
}

.header-bar h2 {
  margin: 0;
  font-size: 1.25rem;
  color: #111827;
  font-weight: 600;
  padding-left: 100px;
}

.status-message {
  text-align: center;
  padding: 40px 20px;
  color: #6b7280;
  font-size: 1rem;
  background: #fff;
  border-radius: 12px;
  margin-top: 20px;
}

.status-message.empty {
  color: #9ca3af;
}

.purchase-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-card {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.product-card .image-wrapper {
  width: 100%;
  height: 220px;
  overflow: hidden;
  border-radius: 8px;
  margin-bottom: 16px;
  background: #f5f5f5;
}

.product-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-card .info h3 {
  margin: 0 0 10px 0;
  font-size: 1.3rem;
  color: #111827;
  font-weight: 600;
}

.product-card .desc {
  color: #6b7280;
  font-size: 0.9rem;
  line-height: 1.5;
  margin-bottom: 12px;
}

.product-card .price {
  font-size: 1.5rem;
  color: #ef4444;
  font-weight: 700;
}

.form-card {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.form-card h3 {
  margin: 0 0 20px 0;
  font-size: 1.1rem;
  color: #111827;
  font-weight: 600;
}

.warning-box {
  background: #fffbeb;
  color: #92400e;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #fde68a;
  font-size: 0.9rem;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #374151;
  font-size: 0.95rem;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 1rem;
  box-sizing: border-box;
  transition: all 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.tips {
  background: #eff6ff;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
  font-size: 0.9rem;
  color: #1e40af;
}

.tips strong {
  display: block;
  margin-bottom: 8px;
  color: #1e40af;
}

.tips ul {
  margin: 0;
  padding-left: 20px;
}

.tips li {
  margin-bottom: 6px;
  line-height: 1.4;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(90deg, #3b82f6, #2563eb);
  color: white;
  border: none;
  border-radius: 25px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.3);
}

.submit-btn:disabled {
  background: #d1d5db;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 👇 新增：评论区样式（完全适配现有风格） */
.comment-section {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  margin-top: 20px;
}

.comment-section h3 {
  margin: 0 0 20px 0;
  font-size: 1.1rem;
  color: #111827;
  font-weight: 600;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

/* 评论编辑器 */
.comment-editor {
  margin-bottom: 30px;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.editor-user {
  font-size: 0.9rem;
  color: #374151;
  font-weight: 500;
}

.score-picker {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: #6b7280;
}

.score-picker select {
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  background: #fff;
  font-size: 0.9rem;
  color: #374151;
  cursor: pointer;
}

.comment-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 0.95rem;
  line-height: 1.5;
  resize: vertical;
  box-sizing: border-box;
  margin-bottom: 12px;
  transition: all 0.2s;
}

.comment-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.comment-submit {
  padding: 10px 24px;
  background: linear-gradient(90deg, #10b981, #059669);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  float: right;
}

.comment-submit:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.comment-submit:disabled {
  background: #d1d5db;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.comment-login-tip {
  text-align: center;
  padding: 30px;
  background: #f9fafb;
  border-radius: 8px;
  color: #6b7280;
  font-size: 0.95rem;
  margin-bottom: 30px;
}

.comment-login-tip a {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 600;
}

.comment-login-tip a:hover {
  text-decoration: underline;
}

/* 评论列表 */
.comment-list {
  margin-top: 20px;
}

.comment-loading {
  text-align: center;
  padding: 30px;
  color: #6b7280;
  font-size: 0.95rem;
}

.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 8px;
  vertical-align: middle;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.comment-empty {
  text-align: center;
  padding: 40px;
  color: #9ca3af;
  font-size: 0.95rem;
}

.comment-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  border-left: 4px solid #3b82f6;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 10px;
  font-size: 0.85rem;
}

.comment-user {
  font-weight: 600;
  color: #374151;
}

.comment-score {
  color: #f59e0b;
  font-weight: 500;
}

.score-num {
  color: #6b7280;
  margin-left: 4px;
}

.comment-time {
  color: #9ca3af;
  margin-left: auto;
}

.comment-delete {
  padding: 4px 10px;
  background: #fee2e2;
  color: #dc2626;
  border: none;
  border-radius: 4px;
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.2s;
}

.comment-delete:hover {
  background: #fecaca;
}

.comment-content {
  color: #4b5563;
  font-size: 0.95rem;
  line-height: 1.6;
}
</style>
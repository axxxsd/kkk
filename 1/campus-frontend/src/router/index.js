import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Publish from '../views/Publish.vue'
import Purchase from '../views/Purchase.vue'
import OrderList from '../views/OrderList.vue'
import MessageList from '../views/MessageList.vue'
import Category from '../views/Category.vue'
// 1. 放开个人中心导入
import Profile from '../views/Profile.vue'
// 2. 放开我的商品导入
import MyProducts from '../views/MyProducts.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/publish', component: Publish },
  { path: '/purchase/:id', component: Purchase },
  { path: '/orders', component: OrderList },
  { path: '/messages', component: MessageList },
  { path: '/category', component: Category },
  // 3. 放开个人中心路由
  { path: '/profile', component: Profile },
  // 4. 放开我的商品路由
  { path: '/my-products', component: MyProducts }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
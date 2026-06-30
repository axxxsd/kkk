// src/main.js
import { createApp } from 'vue'
import { createPinia } from 'pinia' // 必须引入
import App from './App.vue'
import router from './router'

const app = createApp(App)

// ⚠️ 致命检查点：这两行必须都有，且不能漏掉任何符号！
app.use(createPinia()) 
app.use(router)

app.mount('#app')
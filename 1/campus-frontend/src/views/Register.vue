<template>
  <div class="register-container">
    <h2>注册页面（待完善）</h2>
    <p>测试用，点击下面的按钮返回登录页</p>
    <button @click="$router.push('/login')" class="btn">去登录</button>
  </div>
</template>

<script setup>
</script>

<style scoped>
.register-container {
  max-width: 400px;
  margin: 100px auto;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
}
.btn {
  padding: 12px 24px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
}
</style>
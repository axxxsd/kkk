<template>
  <div id="app">
    <!-- 所有路由匹配的页面都会渲染到这里 -->
    <router-view />
  </div>
</template>

<script setup>
// 根组件只放全局逻辑，比如监听登录态变化
</script>

<style scoped>
/* 全局样式放这里，彻底解决黑边问题 */
:global(html), :global(body), :global(#app) {
  margin: 0 !important;
  padding: 0 !important;
  width: 100%;
  min-height: 100vh;
  background-color: #f0f2f5;
  box-sizing: border-box;
}
</style>
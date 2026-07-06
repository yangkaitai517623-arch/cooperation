<!--
  社区平台前端根组件
  作为整个应用的容器，提供：
    1. 顶层 <router-view> 用于渲染匹配路由的页面组件
    2. 页面切换过渡动画（fade 淡入淡出效果）
    3. 全局基础样式重置（移除 body 默认边距）
-->
<template>
  <!-- router-view 配合 v-slot 获取当前路由组件引用，实现过渡动画 -->
  <router-view v-slot="{ Component }">
    <!-- 过渡动画：使用 fade 动画，mode="out-in" 表示旧页面先淡出、新页面再淡入 -->
    <transition name="fade" mode="out-in">
      <!-- 动态组件渲染当前路由对应的页面 -->
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<style>
/* 全局基础样式：移除默认边距，确保最小高度覆盖整个视口 */
body { margin: 0; padding: 0; min-height: 100vh; }

/* ========== 页面切换过渡动画 ========== */

/* fade 进入和离开的激活状态：定义动画时长和缓动函数 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity .2s ease;
}

/* fade 进入起始状态和离开结束状态：完全透明 */
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

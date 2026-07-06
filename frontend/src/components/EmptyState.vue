<!--
  空状态组件 (EmptyState)
  用于在列表无数据时显示提示信息，避免空白页面给用户带来困惑
  包含一个图标（盒子形状的 SVG）和一行提示文字
-->
<template>
  <div class="empty-state">
    <!--
      空状态图标：使用内联 SVG 绘制一个盒子图标
      size 属性控制图标大小（默认 56px）
    -->
    <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" class="empty-icon">
      <path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round" stroke-linecap="round"/>
    </svg>
    <!--
      提示文字，默认显示"暂无数据"，可通过 text 属性自定义
    -->
    <p>{{ text }}</p>
  </div>
</template>

<script setup>
/**
 * 组件 Props 定义
 *
 * 注意：在 defineProps 的对象属性位置不能使用 JSDoc 块注释，
 * 否则 Vue/Babel 会将其识别为类型注解片段，导致 Unexpected token 编译错误。
 * 以下使用普通块注释说明每个 prop 的用途。
 */
defineProps({
  /* 提示文字内容 */
  text: { type: String, default: '暂无数据' },

  /* 图标尺寸（px） */
  size: { type: Number, default: 56 }
})
</script>

<style scoped>
/* 空状态容器：居中布局，内边距使用设计系统变量 */
.empty-state {
  text-align: center;
  padding: var(--s12) var(--s4);
  color: var(--ink-faint);
}

/* 空状态图标：降低透明度使其作为次要视觉元素 */
.empty-icon {
  opacity: .4;
}

/* 提示文字样式：略大字号，次要颜色 */
.empty-state p {
  margin-top: var(--s3);
  font-size: var(--fs-md);
  color: var(--ink-muted);
}
</style>

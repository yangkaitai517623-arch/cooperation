<!--
  筛选栏组件 (FilterBar)
  用于页面顶部的内容筛选，提供一组可切换的按钮
  支持 v-model 双向绑定当前选中的筛选项值
  按钮横向排列，支持横向滚动（移动端适配）
-->
<template>
  <div class="filter-bar">
    <!--
      遍历 filters 数组生成筛选按钮
      当按钮的 value 与 modelValue 匹配时添加 active 类名
      点击按钮时通过 emit 更新父组件的 modelValue
    -->
    <button
      v-for="f in filters"
      :key="f.value"
      class="filter-btn"
      :class="{ active: modelValue === f.value }"
      @click="$emit('update:modelValue', f.value)"
    >{{ f.text }}</button>
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
  /* 筛选项数组，每项包含 text（显示文字）和 value（筛选值） */
  filters: { type: Array, required: true },

  /* 当前选中的筛选项值（支持 v-model 双向绑定） */
  modelValue: { default: null }
})

/*
 * 声明组件可触发的事件
 * update:modelValue: v-model 更新事件
 */
defineEmits(['update:modelValue'])
</script>

<style scoped>
/* 筛选栏容器：flex 横向排列，支持横向滚动 */
.filter-bar {
  display: flex;
  gap: var(--s2);
  margin-bottom: var(--s4);
  overflow-x: auto;                          /* 筛选项过多时允许横向滚动 */
  -webkit-overflow-scrolling: touch;         /* iOS 平滑滚动 */
  scrollbar-width: none;                     /* Firefox 隐藏滚动条 */
}

/* Chrome/Safari 隐藏滚动条 */
.filter-bar::-webkit-scrollbar { display: none; }

/* 筛选按钮基础样式：圆角胶囊按钮 */
.filter-btn {
  padding: 7px var(--s4);
  border-radius: var(--r-full);              /* 完全圆角，形成胶囊形状 */
  border: 1px solid var(--border);
  background: var(--white);
  color: var(--ink-muted);
  font-size: var(--fs-sm);
  font-weight: var(--fw-500);
  white-space: nowrap;                       /* 文字不换行 */
  transition: all var(--fast);
  cursor: pointer;
}

/* 按钮悬停态：边框颜色略微变深 */
.filter-btn:hover {
  border-color: #d4d4d8;
  color: var(--ink-light);
}

/* 按钮激活态：品牌色背景 + 白色文字 + 微阴影 */
.filter-btn.active {
  background: var(--brand);
  color: var(--white);
  border-color: var(--brand);
  box-shadow: 0 1px 4px rgba(59,130,246,.2);
}
</style>

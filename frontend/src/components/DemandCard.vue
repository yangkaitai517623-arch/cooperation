<!--
  需求卡片组件 (DemandCard)
  可复用的需求/商品展示卡片，用于在列表页面中统一展示各类需求信息

  适用场景：
    - 跑腿需求列表
    - 检修需求列表
    - 二手商品列表

  卡片结构（从左到右）：
    1. 左侧条：紧急程度指示点 + 类型图标
    2. 主体：标题 + 状态标签 + 描述（最多2行） + 元数据行
    3. 底部：价格信息 + 操作按钮插槽

  灵活性设计：
    - 通过 props（icon, typeKey, typeIcon, statusMap, statusTypeMap）适配不同业务类型
    - 通过插槽（extra, actions）允许父组件自定义额外内容和操作按钮
-->
<template>
  <div class="demand-card" :style="{ '--i': index }">
    <!-- 左侧栏：紧急程度指示点 + 类型图标 -->
    <div class="card-left">
      <!-- 紧急程度指示点：urg-1（红色/紧急）、urg-2（黄色/中等）、urg-3（绿色/普通） -->
      <div class="card-urgency" :class="'urg-' + (item.urgency || 2)"></div>
      <!-- 类型图标，默认使用 Position 图标 -->
      <div class="card-icon">
        <el-icon :size="20"><component :is="icon" /></el-icon>
      </div>
    </div>

    <!-- 卡片主体内容区 -->
    <div class="card-body">
      <!-- 标题行：标题 + 状态标签 -->
      <div class="card-header">
        <span class="card-title">{{ item.title }}</span>
        <!-- 状态标签：颜色通过 statusType 计算属性动态绑定 -->
        <el-tag :type="statusType" size="small">{{ statusLabel }}</el-tag>
      </div>

      <!-- 描述文字：最多显示 2 行，超出部分省略 -->
      <p class="card-desc">{{ item.description }}</p>

      <!-- 元数据行：发布者名称、业务类型、发布时间 -->
      <div class="card-meta">
        <!-- 发布者/卖家名称（如果有） -->
        <span v-if="item.publisherName || item.sellerName"><el-icon><User /></el-icon> {{ item.publisherName || item.sellerName }}</span>
        <!-- 业务类型信息（如跑腿类型、商品类别等） -->
        <span v-if="item[typeKey]"><el-icon><component :is="typeIcon" /></el-icon> {{ item[typeKey] }}</span>
        <!-- 发布时间 -->
        <span><el-icon><Clock /></el-icon> {{ item.createdAt }}</span>
      </div>

      <!-- 额外内容插槽：允许父组件在元数据下方插入自定义内容 -->
      <slot name="extra" />

      <!-- 底部栏：价格 + 操作按钮 -->
      <div class="card-footer">
        <!-- 价格信息：优先显示奖励金额，其次显示预估价格 -->
        <span v-if="item.reward != null" class="card-price"><small>¥</small>{{ item.reward }}</span>
        <span v-else-if="item.estimatedPrice" class="card-price"><small>预估 ¥</small>{{ item.estimatedPrice }}</span>
        <span v-else></span>
        <!-- 操作按钮插槽 -->
        <div class="card-actions">
          <slot name="actions" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/* 导入 Vue 3 的 computed 函数 */
import { computed } from 'vue'

/*
 * 导入 Element Plus 图标
 * Clock: 时间图标，Location: 位置图标，Tools: 工具图标
 * Position: 位置标记图标，User: 用户图标
 */
import { Clock, Location, Tools, Position, User } from '@element-plus/icons-vue'

/**
 * 组件 Props 定义
 *
 * 所有 props 均通过 defineProps 声明为运行时校验。
 * 注意：在 defineProps 的对象属性位置不能使用 JSDoc 块注释，
 * 否则 Vue/Babel 会将其识别为类型注解片段而导致 Unexpected token。
 * 因此对象内部使用普通块注释说明每个 prop 的含义。
 */
const props = defineProps({
  /* 需求/商品数据对象：必须包含 title/description/status/createdAt 等 */
  item: { type: Object, required: true },

  /* 列表中的索引位置，用于计算交错动画延迟时间 */
  index: { type: Number, default: 0 },

  /* 左侧显示的类型图标组件 */
  icon: { type: [Object, String], default: Position },

  /* 元数据中业务类型字段的键名，如 errandType、repairType */
  typeKey: { type: String, default: 'errandType' },

  /* 业务类型字段旁显示的图标组件 */
  typeIcon: { type: [Object, String], default: Location },

  /* 状态码到中文标签的映射：0=待接单, 1=已接单, 2=进行中, 3=已完成, 4=已取消 */
  statusMap: { type: Object, default: () => ({ 0: '待接单', 1: '已接单', 2: '进行中', 3: '已完成', 4: '已取消' }) },

  /* 状态码到 Element Plus Tag 类型的映射：0=info, 1=warning, 2=primary, 3=success, 4=danger */
  statusTypeMap: { type: Object, default: () => ({ 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }) }
})

/**
 * 计算状态标签文本
 * 根据 item.status 从 statusMap 中查找对应的中文标签
 * @returns {String} 状态中文标签
 */
const statusLabel = computed(() => props.statusMap[props.item.status] || '未知')

/**
 * 计算状态标签的 Element Plus Tag 类型（颜色）
 * 根据 item.status 从 statusTypeMap 中查找对应的类型
 * @returns {String} Element Plus Tag 的 type 属性值（info/warning/primary/success/danger）
 */
const statusType = computed(() => props.statusTypeMap[props.item.status] || 'info')
</script>

<style scoped>
/*
 * 需求卡片容器
 * 使用 flex 布局，左侧固定宽度 + 右侧自适应
 * 卡片入场动画：从下方淡入，通过 --i CSS 变量实现交错延迟
 */
.demand-card {
  background: var(--white);
  border-radius: var(--r-lg);
  overflow: hidden;
  display: flex;
  border: 1px solid var(--border);
  animation: fadeUp .3s var(--ease) calc(var(--i) * 40ms) both;
  transition: all var(--fast);
}

/* 卡片悬停态：边框加深 + 微阴影 */
.demand-card:hover {
  border-color: #d4d4d8;
  box-shadow: var(--sh-sm);
}

/* ========== 左侧栏 ========== */

/* 左侧栏容器：垂直排列，居中 */
.card-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--s4);
  gap: var(--s2);
}

/* 紧急程度指示点：圆形、小尺寸 */
.card-urgency { width: 7px; height: 7px; border-radius: 50%; }

/* 紧急程度颜色：1=红色（紧急），2=黄色（中等），3=绿色（普通） */
.urg-1 { background: #ef4444; }
.urg-2 { background: #f59e0b; }
.urg-3 { background: #22c55e; }

/* 类型图标容器：圆角正方形，浅色背景 */
.card-icon {
  width: 38px;
  height: 38px;
  border-radius: var(--r-md);
  background: var(--bg-soft);
  display: grid;
  place-items: center;
  color: var(--ink-muted);
}

/* ========== 卡片主体 ========== */

/* 主体容器：占据剩余空间，禁止溢出 */
.card-body { flex: 1; padding: var(--s4); min-width: 0; }

/* 标题行：左右分布（标题 + 状态标签） */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--s2);
}

/* 标题文字样式 */
.card-title {
  font-size: var(--fs-md);
  font-weight: var(--fw-600);
  color: var(--ink);
}

/* 描述文字：最多显示 2 行，超出省略 */
.card-desc {
  font-size: var(--fs-base);
  color: var(--ink-muted);
  margin: 0 0 var(--s3);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 元数据行：flex 横向排列，可换行 */
.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--s3);
  font-size: var(--fs-xs);
  color: var(--ink-faint);
  margin-bottom: var(--s3);
}

/* 元数据每一项：flex 布局，图标和文字对齐 */
.card-meta span { display: flex; align-items: center; gap: 4px; }

/* ========== 底部栏 ========== */

/* 底部栏：左右分布，支持换行 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--s3);
}

/* 价格文字：大号字体，品牌色，加粗 */
.card-price {
  font-size: var(--fs-lg);
  font-weight: var(--fw-700);
  color: var(--brand);
}

/* 价格中的辅助小字（如 ¥ 符号） */
.card-price small {
  font-size: var(--fs-sm);
  font-weight: var(--fw-500);
}

/* 操作按钮容器：横向排列，可换行 */
.card-actions {
  display: flex;
  gap: var(--s2);
  align-items: center;
  flex-wrap: wrap;
}
</style>

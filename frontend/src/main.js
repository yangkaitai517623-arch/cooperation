/*
 * ============================================================================
 * 社区平台前端应用入口文件
 * ============================================================================
 * 本文件负责初始化 Vue 应用，包括：
 *   1. 注册 Vue 核心插件（Pinia 状态管理、Vue Router 路由、Element Plus UI 框架）
 *   2. 注册 Element Plus 所有图标组件（全局可用）
 *   3. 设置 Element Plus 中文语言包
 *   4. 挂载应用到 #app DOM 节点
 * ============================================================================
 */

/* 导入 Vue 3 应用创建工厂函数 */
import { createApp } from 'vue'

/* 导入 Pinia 状态管理库（Vue 3 官方推荐的状态管理方案） */
import { createPinia } from 'pinia'

/* 导入 Element Plus UI 组件库 */
import ElementPlus from 'element-plus'

/* 导入 Element Plus 全局样式 */
import 'element-plus/dist/index.css'

/* 导入项目自定义设计系统 CSS 变量（颜色、间距、圆角、阴影等） */
import './assets/design-system.css'

/* 导入 Element Plus 中文本地化语言包，用于日期选择器等组件的中文显示 */
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

/* 导入 Element Plus 所有图标组件（按需导入所有图标以避免单独引入） */
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

/* 导入根组件 */
import App from './App.vue'

/* 导入路由配置实例 */
import router from './router'

/* 创建 Vue 3 应用实例，传入根组件 */
const app = createApp(App)

/* 注册所有 Element Plus 图标组件为全局组件
 * 遍历 @element-plus/icons-vue 导出的所有图标，
 * 以图标名称作为组件名注册到应用中，
 * 方便在模板中直接通过 <el-icon><IconName /></el-icon> 使用 */
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

/* 安装 Pinia 状态管理插件 */
app.use(createPinia())

/* 安装 Vue Router 路由插件 */
app.use(router)

/* 安装 Element Plus UI 框架，并配置中文语言环境 */
app.use(ElementPlus, { locale: zhCn })

/* 将 Vue 应用挂载到 id 为 "app" 的 DOM 元素上 */
app.mount('#app')

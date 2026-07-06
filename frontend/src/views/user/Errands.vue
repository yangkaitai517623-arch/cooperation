<template>
  <!-- 跑腿帮忙页面：统计卡片 + 筛选栏 + 需求列表 + 发布/编辑对话框 + AI分析 -->
  <div class="errand-page">
    <!-- 页面头部 -->
    <header class="page-header">
      <div><h2>跑腿帮忙</h2><p>社区互助，有求必应</p></div>
      <el-button type="primary" @click="showPublish = true"><el-icon><Plus /></el-icon> 发布需求</el-button>
    </header>

    <!-- 统计卡片区：待接单、进行中、已完成、总计（支持快速筛选） -->
    <div class="stat-grid">
      <div v-for="(s, i) in quickItems" :key="s.label" class="stat-card" :style="{ '--i': i }" @click="onQuickClick(s)">
        <div class="stat-icon" :style="{ background: s.bg }">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" :style="{ color: s.color }" v-html="s.svg"></svg>
        </div>
        <div class="stat-body">
          <div class="stat-label">{{ s.label }}</div>
          <div class="stat-value">{{ s.count }}</div>
        </div>
      </div>
    </div>

    <!-- 需求列表卡片 -->
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-head">
          <span>需求列表</span>
          <!-- 状态筛选栏 -->
          <FilterBar v-model="activeFilter" :filters="filters" />
        </div>
      </template>
      <!-- 使用 DemandCard 组件渲染每个需求 -->
      <DemandCard
        v-for="(item, idx) in demands"
        :key="item.id"
        :item="item"
        :index="idx"
        icon="Position"
        type-key="errandType"
      >
        <!-- 额外信息插槽：取货/送达地址 -->
        <template #extra>
          <div v-if="item.pickupAddress || item.deliveryAddress" class="card-addr">
            <div v-if="item.pickupAddress"><el-icon><Box /></el-icon> 取货：{{ item.pickupAddress }}</div>
            <div v-if="item.deliveryAddress"><el-icon><Promotion /></el-icon> 送达：{{ item.deliveryAddress }}</div>
          </div>
        </template>
        <!-- 操作按钮插槽：接单/完成/编辑/删除 + 身份标签 -->
        <template #actions>
          <!-- status=0（待接单）且不是自己发布的 → 可接单 -->
          <el-button v-if="item.status === 0 && item.userId !== currentUid" type="primary" size="small" @click="acceptDemand(item)">接单</el-button>
          <!-- status=1（已接单）且是自己发布的 → 可确认完成 -->
          <el-button v-if="item.status === 1 && item.userId === currentUid" type="success" size="small" @click="completeDemand(item)">确认完成</el-button>
          <!-- 自己的 + 待接单 → 可编辑 -->
          <el-button v-if="item.userId === currentUid && item.status === 0" type="warning" size="small" @click="editDemand(item)">编辑</el-button>
          <!-- 自己的 + 待接单 → 可删除 -->
          <el-button v-if="item.userId === currentUid && item.status === 0" type="danger" size="small" @click="deleteDemand(item)">删除</el-button>
          <!-- 自己的需求标记 -->
          <el-tag v-if="item.userId === currentUid" type="info" size="small">我的</el-tag>
          <!-- 已接单且未完成的标记 -->
          <el-tag v-if="item.runnerId === currentUid && item.status !== 3" type="warning" size="small">已接</el-tag>
        </template>
      </DemandCard>
      <!-- 空状态 -->
      <EmptyState v-if="demands.length === 0" text="暂无跑腿需求" />
    </el-card>

    <!-- 发布需求对话框 -->
    <el-dialog v-model="showPublish" title="发布跑腿需求" width="520px">
      <el-form :model="pubForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="pubForm.title" placeholder="简短描述需求" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="pubForm.description" type="textarea" :rows="3" placeholder="详细描述需求，AI自动分析" /></el-form-item>
        <!-- AI分析按钮 -->
        <el-form-item>
          <el-button :loading="aiBusy" @click="runAi"><el-icon><MagicStick /></el-icon> AI分析</el-button>
          <span v-if="aiResult" class="ai-ok"><el-icon><CircleCheck /></el-icon> {{ aiResult.serviceType }}</span>
        </el-form-item>
        <el-form-item label="类型"><el-select v-model="pubForm.errandType" style="width:100%"><el-option v-for="t in ['代买','代取','代送','排队','其他']" :key="t" :label="t" :value="t" /></el-select></el-form-item>
        <el-form-item label="取货地址"><el-input v-model="pubForm.pickupAddress" /></el-form-item>
        <el-form-item label="送达地址"><el-input v-model="pubForm.deliveryAddress" /></el-form-item>
        <!-- 紧急程度：1=紧急, 2=一般, 3=不急 -->
        <el-form-item label="紧急程度"><el-radio-group v-model="pubForm.urgency"><el-radio :value="1">紧急</el-radio><el-radio :value="2">一般</el-radio><el-radio :value="3">不急</el-radio></el-radio-group></el-form-item>
        <el-form-item label="报酬(¥)"><el-input-number v-model="pubForm.reward" :min="0" :step="5" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showPublish = false">取消</el-button><el-button type="primary" :loading="pubBusy" @click="submitPublish">发布</el-button></template>
    </el-dialog>

    <!-- 编辑需求对话框 -->
    <el-dialog v-model="editDlg" title="编辑需求" width="520px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="editForm.title" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="editForm.errandType" style="width:100%"><el-option v-for="t in ['代买','代取','代送','排队','其他']" :key="t" :label="t" :value="t" /></el-select></el-form-item>
        <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="取货地址"><el-input v-model="editForm.pickupAddress" /></el-form-item>
        <el-form-item label="送达地址"><el-input v-model="editForm.deliveryAddress" /></el-form-item>
        <el-form-item label="紧急程度"><el-radio-group v-model="editForm.urgency"><el-radio :value="1">紧急</el-radio><el-radio :value="2">一般</el-radio><el-radio :value="3">不急</el-radio></el-radio-group></el-form-item>
        <el-form-item label="报酬(¥)"><el-input-number v-model="editForm.reward" :min="0" :step="5" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="editDlg = false">取消</el-button><el-button type="primary" :loading="editBusy" @click="submitEdit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * Errands.vue - 跑腿帮忙页面
 *
 * 功能说明：
 * - 发布跑腿需求（代买、代取、代送、排队等）
 * - 状态管理：0=待接单, 1=已接单, 2=进行中, 3=已完成, 4=已取消
 * - 支持AI智能分析需求描述自动归类
 * - 支持接单、确认完成、编辑、删除操作
 * - 统计卡片支持快速筛选
 */

import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Box, Promotion, MagicStick, CircleCheck } from '@element-plus/icons-vue'
import FilterBar from '@/components/FilterBar.vue'
import DemandCard from '@/components/DemandCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import api from '@/api'

/** 筛选器选项：全部/待接单(0)/进行中(2)/已完成(3) */
const filters = [{ value: null, text: '全部' }, { value: 0, text: '待接单' }, { value: 2, text: '进行中' }, { value: 3, text: '已完成' }]
/** SVG图标路径：加号 */
const svgPlus = '<path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>'
/** SVG图标路径：时钟 */
const svgClock = '<circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.8"/><path d="M12 6v6l4 2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>'
/** SVG图标路径：位置/跑腿 */
const svgPos = 'M14.7 6.3a1 1 0 000 1.4l1.6 1.6a1 1 0 001.4 0l3.77-3.77a6 6 0 01-7.94 7.94l-6.91 6.91a2.12 2.12 0 01-3-3l6.91-6.91a6 6 0 017.94-7.94l-3.76 3.76z'
/** SVG图标路径：勾选 */
const svgCheck = '<path d="M22 11.08V12a10 10 0 11-5.93-9.14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/><path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>'

/**
 * 统计卡片数据
 * 包含待接单(0)、进行中(2)、已完成(3)、总计的数量
 * 点击可快速筛选对应状态
 */
const quickItems = ref([
  { label: '待接单', count: 0, svg: svgClock, bg: 'rgba(249,115,22,.1)', color: '#f97316', action: 'filter-0' },
  { label: '进行中', count: 0, svg: `<path d="${svgPos}" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>`, bg: 'rgba(14,165,233,.1)', color: '#0ea5e9', action: 'filter-2' },
  { label: '已完成', count: 0, svg: svgCheck, bg: 'rgba(16,185,129,.1)', color: '#10b981', action: 'filter-3' },
  { label: '总计', count: 0, svg: svgPlus, bg: 'rgba(139,92,246,.1)', color: '#8b5cf6', action: 'filter-null' }
])

/** 当前选中的状态筛选值 */
const activeFilter = ref(null)
/** 需求列表数据 */
const demands = ref([])
/** 当前登录用户ID */
const currentUid = ref(null)
/** 发布对话框是否显示 */
const showPublish = ref(false)
/** 发布按钮加载状态 */
const pubBusy = ref(false)
/** AI分析按钮加载状态 */
const aiBusy = ref(false)
/** AI分析结果 */
const aiResult = ref(null)
/** 发布表单数据 */
const pubForm = reactive({ title: '', description: '', errandType: '', pickupAddress: '', deliveryAddress: '', urgency: 2, reward: 10 })
/** 编辑对话框是否显示 */
const editDlg = ref(false)
/** 编辑按钮加载状态 */
const editBusy = ref(false)
/** 当前编辑的需求ID */
const editId = ref(null)
/** 编辑表单数据 */
const editForm = reactive({ title: '', description: '', errandType: '', pickupAddress: '', deliveryAddress: '', urgency: 2, reward: 0 })

/**
 * 统计卡片点击处理
 * 根据 action 设置筛选值：filter-null → 全部, filter-{n} → 状态=n
 * @param {Object} item - 统计卡片数据对象
 */
function onQuickClick (item) {
  if (item.action === 'filter-null') activeFilter.value = null
  else if (item.action?.startsWith('filter-')) activeFilter.value = Number(item.action.split('-')[1])
}

/**
 * 加载当前用户ID
 * API: GET /user/profile → { data: { id } }
 */
async function loadUid () { try { const { data } = await api.get('/user/profile'); if (data.code === 200) currentUid.value = data.data?.id } catch {} }

/**
 * 加载各状态数量统计
 * API: GET /errand-requests?page=1&size=999
 * 按状态计算：待接单(0)、进行中(2)、已完成(3)、总计
 */
async function loadCounts () {
  try {
    const { data } = await api.get('/errand-requests', { params: { page: 1, size: 999 } })
    if (data.code === 200) {
      const all = data.data?.records || []
      quickItems.value[0].count = all.filter(d => d.status === 0).length
      quickItems.value[1].count = all.filter(d => d.status === 2).length
      quickItems.value[2].count = all.filter(d => d.status === 3).length
      quickItems.value[3].count = all.length
    }
  } catch {}
}

/**
 * 加载需求列表（支持状态筛选）
 * API: GET /errand-requests?page=1&size=50&status={status}
 */
async function loadDemands () {
  try {
    const p = { page: 1, size: 50 }
    if (activeFilter.value !== null) p.status = activeFilter.value
    const { data } = await api.get('/errand-requests', { params: p })
    if (data.code === 200) demands.value = data.data?.records || []
  } catch {}
}

/**
 * 接单操作
 * API: PUT /errand-requests/{id}/accept
 * @param {Object} item - 需求对象
 */
async function acceptDemand (item) { try { await ElMessageBox.confirm('确定接单？'); const { data } = await api.put(`/errand-requests/${item.id}/accept`); if (data.code === 200) { ElMessage.success('接单成功'); loadDemands() } } catch {} }

/**
 * 确认完成操作
 * API: PUT /errand-requests/{id}/complete
 * @param {Object} item - 需求对象
 */
async function completeDemand (item) { try { await ElMessageBox.confirm('确认完成？', '完成', { type: 'success' }); const { data } = await api.put(`/errand-requests/${item.id}/complete`); if (data.code === 200) { ElMessage.success('已完成'); loadDemands() } } catch {} }

/**
 * 打开编辑对话框，填充当前需求数据
 * @param {Object} item - 需求对象
 */
function editDemand (item) { editId.value = item.id; Object.assign(editForm, { title: item.title, description: item.description, errandType: item.errandType, pickupAddress: item.pickupAddress, deliveryAddress: item.deliveryAddress, urgency: item.urgency, reward: item.reward }); editDlg.value = true }

/**
 * 提交编辑
 * API: PUT /errand-requests/{id}
 */
async function submitEdit () { editBusy.value = true; try { const { data } = await api.put(`/errand-requests/${editId.value}`, editForm); if (data.code === 200) { ElMessage.success('已修改'); editDlg.value = false; loadDemands() } } catch {} finally { editBusy.value = false } }

/**
 * 删除需求
 * API: DELETE /errand-requests/{id}
 * @param {Object} item - 需求对象
 */
async function deleteDemand (item) { try { await ElMessageBox.confirm('删除后无法恢复', '确认删除', { type: 'warning' }); const { data } = await api.delete(`/errand-requests/${item.id}`); if (data.code === 200) { ElMessage.success('已删除'); loadDemands() } } catch {} }

/**
 * AI智能分析需求描述
 * API: POST /ai/classify { description, type: 'errand' }
 * 分析出服务类型和紧急程度，自动填充表单
 */
async function runAi () { if (!pubForm.description) { ElMessage.warning('先输入描述'); return }; aiBusy.value = true; try { const { data } = await api.post('/ai/classify', { description: pubForm.description, type: 'errand' }); if (data.code === 200) { aiResult.value = data.data; const m = { '代买': '代买', '代取': '代取', '代送': '代送' }; for (const [k, v] of Object.entries(m)) { if (aiResult.value.serviceType?.includes(k)) { pubForm.errandType = v; break } }; if (aiResult.value.urgency) pubForm.urgency = { high: 1, medium: 2, low: 3 }[aiResult.value.urgency] || 2; ElMessage.success('AI分析完成') } } catch {} finally { aiBusy.value = false } }

/**
 * 提交发布需求
 * API: POST /errand-requests
 */
async function submitPublish () { pubBusy.value = true; try { const { data } = await api.post('/errand-requests', pubForm); if (data.code === 200) { ElMessage.success('发布成功'); showPublish.value = false; aiResult.value = null; loadDemands() } } catch {} finally { pubBusy.value = false } }

// 监听筛选变化重新加载列表
watch(activeFilter, loadDemands)
// 页面挂载时加载用户ID、统计数量、需求列表
onMounted(() => { loadUid(); loadCounts(); loadDemands() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: var(--s6); }
.page-header h2 { font-size: var(--fs-xl); font-weight: var(--fw-700); margin: 0; color: var(--ink); }
.page-header p { font-size: var(--fs-sm); color: var(--ink-muted); margin: 2px 0 0; }

.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--s3); margin-bottom: var(--s5); }
.stat-card {
  background: var(--white); border: 1px solid var(--border);
  border-radius: var(--r-lg); padding: var(--s4);
  display: flex; align-items: center; gap: var(--s3);
  cursor: pointer; transition: all var(--fast);
}
.stat-card:hover { border-color: var(--brand); box-shadow: var(--sh-sm); }
.stat-icon { width: 44px; height: 44px; border-radius: var(--r-md); display: grid; place-items: center; flex-shrink: 0; }
.stat-body { flex: 1; }
.stat-label { font-size: var(--fs-xs); color: var(--ink-muted); margin-bottom: 2px; }
.stat-value { font-size: var(--fs-lg); font-weight: var(--fw-700); color: var(--ink); }

.list-card { margin-bottom: var(--s4); }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.card-head span { font-weight: var(--fw-600); color: var(--ink); }

.card-addr {
  background: var(--bg-soft); border-radius: var(--r-md);
  padding: var(--s3); margin-bottom: var(--s3);
  font-size: var(--fs-sm); color: var(--ink-light);
}
.card-addr div { display: flex; align-items: center; gap: var(--s2); margin-bottom: var(--s1); }
.card-addr div:last-child { margin-bottom: 0; }

.ai-ok { margin-left: var(--s3); font-size: var(--fs-sm); color: var(--green); display: inline-flex; align-items: center; gap: 4px; }

@media (max-width: 768px) { .stat-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px) { .stat-grid { grid-template-columns: 1fr; } }
</style>

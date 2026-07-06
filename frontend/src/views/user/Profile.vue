<template>
  <!-- 个人中心页面：用户卡片 + 功能入口 + 编辑资料/修改密码 + 多标签订单列表 -->
  <div class="profile-page">
    <!-- 页面头部 -->
    <header class="page-header"><h2>个人中心</h2><p>管理你的账户信息</p></header>

    <!-- 用户信息卡片 -->
    <el-card shadow="never" class="user-card">
      <div class="user-info">
        <div class="user-avatar">{{ user?.realName?.[0] || '用' }}</div>
        <div class="user-detail">
          <h3>{{ user?.realName || '用户' }}</h3>
          <p>{{ user?.username }} · {{ user?.building || '' }} {{ user?.room || '' }}</p>
        </div>
      </div>
    </el-card>

    <!-- 功能入口区：我的订单、编辑资料、修改密码、退出登录 -->
    <div class="func-grid">
      <div class="func-card" @click="scrollTo('orders')">
        <div class="func-icon" style="background:rgba(14,165,233,.1);color:#0ea5e9">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round" stroke-linecap="round"/></svg>
        </div>
        <div class="func-body"><strong>我的订单</strong><span>查看所有订单</span></div>
        <svg class="func-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M9 18l6-6-6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </div>
      <div class="func-card" @click="scrollTo('edit')">
        <div class="func-icon" style="background:rgba(249,115,22,.1);color:#f97316">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <div class="func-body"><strong>编辑资料</strong><span>修改个人信息</span></div>
        <svg class="func-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M9 18l6-6-6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </div>
      <div class="func-card" @click="scrollTo('password')">
        <div class="func-icon" style="background:rgba(139,92,246,.1);color:#8b5cf6">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none"><rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="1.8"/><path d="M7 11V7a5 5 0 0110 0v4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <div class="func-body"><strong>修改密码</strong><span>更新账户密码</span></div>
        <svg class="func-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M9 18l6-6-6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </div>
      <div class="func-card" @click="doLogout">
        <div class="func-icon" style="background:rgba(244,63,94,.1);color:#f43f5e">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/><path d="M16 17l5-5-5-5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/><path d="M21 12H9" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <div class="func-body"><strong>退出登录</strong><span>安全退出</span></div>
        <svg class="func-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M9 18l6-6-6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </div>
    </div>

    <!-- 底部两栏 -->
    <div class="bottom-grid">
      <!-- 左侧：编辑资料表单 -->
      <el-card shadow="never" id="edit">
        <template #header><span>编辑资料</span></template>
        <el-form :model="form" label-width="70px" class="edit-form">
          <el-form-item label="用户名"><el-input v-model="form.username" disabled /></el-form-item>
          <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
          <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
          <!-- 楼栋号 + 房间号并排 -->
          <div class="form-row"><el-form-item label="楼栋" class="form-half"><el-input v-model="form.building" /></el-form-item><el-form-item label="房间" class="form-half"><el-input v-model="form.room" /></el-form-item></div>
          <el-form-item><el-button type="primary" :loading="saving" @click="doSave"><el-icon><Check /></el-icon> 保存修改</el-button></el-form-item>
        </el-form>
      </el-card>

      <!-- 右侧：修改密码表单 -->
      <el-card shadow="never" id="password">
        <template #header><span>修改密码</span></template>
        <el-form :model="pwForm" label-width="80px" class="edit-form" :rules="pwRules" ref="pwFormRef">
          <el-form-item label="旧密码" prop="oldPassword"><el-input v-model="pwForm.oldPassword" type="password" placeholder="请输入旧密码" show-password /></el-form-item>
          <el-form-item label="新密码" prop="newPassword"><el-input v-model="pwForm.newPassword" type="password" placeholder="至少6位" show-password /></el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="pwForm.confirmPassword" type="password" placeholder="再次输入新密码" show-password /></el-form-item>
          <el-form-item><el-button type="primary" :loading="pwSaving" @click="doChangePw"><el-icon><Check /></el-icon> 修改密码</el-button></el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 我的订单：多标签页 -->
    <el-card shadow="never" id="orders" class="order-card">
      <template #header><span>我的订单</span></template>
      <el-tabs v-model="tab" class="order-tabs">
        <!-- 跑腿订单标签页 -->
        <el-tab-pane label="跑腿" name="errand">
          <div class="order-list">
            <div v-for="o in errandList" :key="o.id" class="order-row">
              <div class="order-info"><strong>{{ o.title }}</strong><time>{{ o.createdAt }}</time><div class="order-tags"><el-tag v-if="o.userId===uid" type="info" size="small">我发布</el-tag><el-tag v-if="o.runnerId===uid" type="warning" size="small">我接单</el-tag></div></div>
              <div class="order-right">
                <!-- 状态标签：使用 statusMap 映射 -->
                <el-tag :type="getStatusType(o.status)" size="small">{{ stTxt(o.status) }}</el-tag>
                <!-- 自己的 + 待接单(0) → 可编辑 -->
                <el-button v-if="o.userId===uid && o.status===0" type="primary" size="small" @click="editErrand(o)">编辑</el-button>
                <!-- 自己的 + 待接单(0) → 可删除 -->
                <el-button v-if="o.userId===uid && o.status===0" type="danger" size="small" @click="deleteErrandOrder(o)">删除</el-button>
              </div>
            </div>
            <el-empty v-if="errandList.length===0" description="暂无" :image-size="60" />
          </div>
        </el-tab-pane>
        <!-- 检修订单标签页 -->
        <el-tab-pane label="检修" name="repair">
          <div class="order-list">
            <div v-for="o in repairList" :key="o.id" class="order-row">
              <div class="order-info"><strong>{{ o.title }}</strong><time>{{ o.createdAt }}</time><div class="order-tags"><el-tag v-if="o.userId===uid" type="info" size="small">我发布</el-tag><el-tag v-if="o.workerId===uid" type="warning" size="small">我接单</el-tag></div></div>
              <div class="order-right">
                <el-tag :type="getStatusType(o.status)" size="small">{{ stTxt(o.status) }}</el-tag>
                <el-button v-if="o.userId===uid && o.status===0" type="primary" size="small" @click="editRepair(o)">编辑</el-button>
                <el-button v-if="o.userId===uid && o.status===0" type="danger" size="small" @click="deleteRepairOrder(o)">删除</el-button>
              </div>
            </div>
            <el-empty v-if="repairList.length===0" description="暂无" :image-size="60" />
          </div>
        </el-tab-pane>
        <!-- 购买订单标签页 -->
        <el-tab-pane label="购买" name="buyer">
          <div class="order-list">
            <div v-for="o in buyerList" :key="o.id" class="order-row">
              <div class="order-info">
                <strong>{{ o.goodsTitle || '#' + o.orderNo }}</strong>
                <span class="order-price">¥{{ o.amount }}</span>
                <time>{{ o.createdAt }}</time>
              </div>
              <div class="order-right">
                <el-tag :type="getOrderStatusType(o.status)" size="small">{{ ostTxt(o.status) }}</el-tag>
                <!-- status=1（已确认）且是自己购买的 → 可确认收货 -->
                <el-button v-if="o.status===1 && o.buyerId===uid" type="success" size="small" @click="buyerOk(o)">确认收货</el-button>
              </div>
            </div>
            <el-empty v-if="buyerList.length===0" description="暂无" :image-size="60" />
          </div>
        </el-tab-pane>
        <!-- 出售订单标签页 -->
        <el-tab-pane label="出售" name="seller">
          <div class="order-list">
            <div v-for="o in sellerList" :key="o.id" class="order-row">
              <div class="order-info">
                <strong>{{ o.goodsTitle || '#' + o.orderNo }}</strong>
                <span class="order-price">¥{{ o.amount }}</span>
                <time>{{ o.createdAt }}</time>
              </div>
              <div class="order-right">
                <el-tag :type="getOrderStatusType(o.status)" size="small">{{ ostTxt(o.status) }}</el-tag>
                <!-- status=0（待确认）且是自己卖的 → 可确认订单 -->
                <el-button v-if="o.status===0 && o.sellerId===uid" type="primary" size="small" @click="sellerOk(o)">确认</el-button>
              </div>
            </div>
            <el-empty v-if="sellerList.length===0" description="暂无" :image-size="60" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 编辑跑腿订单对话框 -->
    <el-dialog v-model="errandDlg" title="编辑跑腿需求" width="500px">
      <el-form :model="errandForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="errandForm.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="errandForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="errandForm.errandType" style="width:100%">
            <el-option label="代买" value="代买" /><el-option label="代取" value="代取" /><el-option label="代送" value="代送" /><el-option label="排队" value="排队" /><el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="报酬"><el-input-number v-model="errandForm.reward" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="取货地址"><el-input v-model="errandForm.pickupAddress" /></el-form-item>
        <el-form-item label="送达地址"><el-input v-model="errandForm.deliveryAddress" /></el-form-item>
        <!-- 紧急程度：1=紧急, 2=一般, 3=不急 -->
        <el-form-item label="紧急程度">
          <el-radio-group v-model="errandForm.urgency">
            <el-radio :value="1">紧急</el-radio><el-radio :value="2">一般</el-radio><el-radio :value="3">不急</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="errandDlg=false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="saveErrand">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑检修订单对话框 -->
    <el-dialog v-model="repairDlg" title="编辑检修需求" width="500px">
      <el-form :model="repairForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="repairForm.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="repairForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="repairForm.repairType" style="width:100%">
            <el-option label="水电维修" value="水电维修" /><el-option label="门窗维修" value="门窗维修" /><el-option label="家电维修" value="家电维修" /><el-option label="管道疏通" value="管道疏通" /><el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="维修地址"><el-input v-model="repairForm.location" /></el-form-item>
        <el-form-item label="紧急程度">
          <el-radio-group v-model="repairForm.urgency">
            <el-radio :value="1">紧急</el-radio><el-radio :value="2">一般</el-radio><el-radio :value="3">不急</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="repairDlg=false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="saveRepair">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * Profile.vue - 个人中心页面
 *
 * 功能说明：
 * - 展示用户信息卡片（头像、姓名、用户名、楼栋号+房间号）
 * - 功能入口：我的订单、编辑资料、修改密码、退出登录
 * - 编辑资料表单（用户名只读）
 * - 修改密码表单（带规则校验：最小6位、必须含数字和字母、两次输入一致）
 * - 多标签页订单管理：跑腿订单、检修订单、购买订单、出售订单
 * - 支持编辑/删除跑腿和检修订单
 * - 订单状态枚举：
 *   · 跑腿/检修：0=待接单, 1=已接单, 2=进行中, 3=已完成, 4=已取消
 *   · 商品订单：0=待确认, 1=已确认, 2=已完成
 */

import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import api from '@/api'

/** Vue Router 实例 */
const router = useRouter()
/** 当前路由实例（用于读取 query 参数实现锚点滚动） */
const route = useRoute()
/** Pinia 用户状态管理 */
const store = useUserStore()
/** 当前用户对象（从 store 获取） */
const user = ref(store.user)
/** 保存资料按钮加载状态 */
const saving = ref(false)
/** 当前订单标签页名称 */
const tab = ref('errand')
/** 当前登录用户ID */
const uid = ref(null)

/** 跑腿订单列表 */
const errandList = ref([])
/** 检修订单列表 */
const repairList = ref([])
/** 购买订单列表 */
const buyerList = ref([])
/** 出售订单列表 */
const sellerList = ref([])

/** 编辑资料表单 */
const form = reactive({ username: user.value?.username||'', realName: user.value?.realName||'', phone: user.value?.phone||'', email: user.value?.email||'', building: user.value?.building||'', room: user.value?.room||'' })

// 编辑订单相关状态
/** 编辑跑腿对话框是否显示 */
const errandDlg = ref(false)
/** 编辑检修对话框是否显示 */
const repairDlg = ref(false)
/** 编辑订单加载状态 */
const editLoading = ref(false)
/** 编辑跑腿表单 */
const errandForm = reactive({ id: null, title: '', description: '', errandType: '', reward: 0, pickupAddress: '', deliveryAddress: '', urgency: 2 })
/** 编辑检修表单 */
const repairForm = reactive({ id: null, title: '', description: '', repairType: '', location: '', urgency: 2 })

/** 修改密码表单引用（用于触发表单校验） */
const pwFormRef = ref(null)
/** 修改密码按钮加载状态 */
const pwSaving = ref(false)
/** 修改密码表单数据 */
const pwForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
/**
 * 修改密码表单校验规则
 * - 旧密码必填
 * - 新密码必填 + 最小6位 + 必须同时包含数字和字母
 * - 确认密码必填 + 必须与新密码一致
 */
const pwRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }, { validator: (rule, value, callback) => { if (value && (!/[a-zA-Z]/.test(value) || !/[0-9]/.test(value))) { callback(new Error('密码必须包含数字和字母')) } else { callback() } }, trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }, { validator: (rule, value, callback) => { if (value !== pwForm.newPassword) callback(new Error('两次密码不一致')); else callback() }, trigger: 'blur' }]
}

/**
 * 跑腿/检修订单状态文本映射
 * 0=待接单, 1=已接单, 2=进行中, 3=已完成, 4=已取消
 */
const stTxt = s => ({ 0: '待接单', 1: '已接单', 2: '进行中', 3: '已完成', 4: '已取消' }[s] || '未知')
/**
 * 商品订单状态文本映射
 * 0=待确认, 1=已确认, 2=已完成
 */
const ostTxt = s => ({ 0: '待确认', 1: '已确认', 2: '已完成' }[s] || '未知')
/** 跑腿/检修状态对应的 Tag 类型 */
const getStatusType = s => ({ 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }[s] || 'info')
/** 商品订单状态对应的 Tag 类型 */
const getOrderStatusType = s => ({ 0: 'warning', 1: 'primary', 2: 'success' }[s] || 'info')

/**
 * 平滑滚动到指定ID区域
 * @param {string} id - 目标元素的ID
 */
function scrollTo (id) { document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' }) }

/** 退出登录：清除 store 状态并跳转到登录页 */
function doLogout () { store.logout(); router.push('/login') }

/**
 * 加载当前用户ID
 * API: GET /user/profile → { data: { id } }
 */
async function loadUid () { try { const { data } = await api.get('/user/profile'); if (data.code === 200) uid.value = data.data?.id } catch {} }

/**
 * 加载所有订单
 * 同时请求跑腿/检修/商品订单
 * API: GET /errand-requests/my + /repair-requests/my + /goods-orders/my
 */
async function loadOrders () { try { const [e, r, b] = await Promise.all([api.get('/errand-requests/my'), api.get('/repair-requests/my'), api.get('/goods-orders/my')]); if (e.data.code === 200) errandList.value = e.data.data?.records||[]; if (r.data.code === 200) repairList.value = r.data.data?.records||[]; if (b.data.code === 200) { buyerList.value = b.data.data?.buyerOrders||[]; sellerList.value = b.data.data?.sellerOrders||[] } } catch {} }

/**
 * 保存个人资料
 * API: PUT /user/profile
 */
async function doSave () { saving.value = true; try { const { data } = await api.put('/user/profile', form); if (data.code === 200) { ElMessage.success('保存成功'); store.user = { ...store.user, ...form }; localStorage.setItem('user', JSON.stringify(store.user)) } } catch {} finally { saving.value = false } }

/**
 * 修改密码
 * API: PUT /user/password { oldPassword, newPassword }
 * 成功后清除表单数据、退出登录到登录页
 */
async function doChangePw () {
  if (!pwFormRef.value) return
  const valid = await pwFormRef.value.validate().catch(() => false)
  if (!valid) return
  pwSaving.value = true
  try {
    const { data } = await api.put('/user/password', { oldPassword: pwForm.oldPassword, newPassword: pwForm.newPassword })
    if (data.code === 200) { ElMessage.success('密码修改成功，请重新登录'); pwForm.oldPassword = ''; pwForm.newPassword = ''; pwForm.confirmPassword = ''; store.logout(); router.push('/login') }
    else { ElMessage.error(data.message || '修改失败') }
  } catch { ElMessage.error('修改失败') }
  finally { pwSaving.value = false }
}

/**
 * 卖家确认订单
 * API: PUT /goods-orders/{id}/confirm
 * @param {Object} o - 商品订单对象
 */
async function sellerOk (o) { try { await ElMessageBox.confirm('确认订单？'); const { data } = await api.put(`/goods-orders/${o.id}/confirm`); if (data.code === 200) { ElMessage.success('已确认'); loadOrders() } } catch {} }

/**
 * 买家确认收货
 * API: PUT /goods-orders/{id}/complete
 * @param {Object} o - 商品订单对象
 */
async function buyerOk (o) { try { await ElMessageBox.confirm('确认收到？', '收货', { type: 'success' }); const { data } = await api.put(`/goods-orders/${o.id}/complete`); if (data.code === 200) { ElMessage.success('已确认'); loadOrders() } } catch {} }

/**
 * 打开编辑跑腿订单对话框并填充数据
 * @param {Object} o - 跑腿订单对象
 */
function editErrand (o) {
  errandForm.id = o.id
  errandForm.title = o.title
  errandForm.description = o.description
  errandForm.errandType = o.errandType
  errandForm.reward = o.reward
  errandForm.pickupAddress = o.pickupAddress
  errandForm.deliveryAddress = o.deliveryAddress
  errandForm.urgency = o.urgency || 2
  errandDlg.value = true
}

/**
 * 保存编辑的跑腿订单
 * API: PUT /errand-requests/{id}
 */
async function saveErrand () {
  if (!errandForm.title) { ElMessage.warning('请输入标题'); return }
  editLoading.value = true
  try {
    const { data } = await api.put(`/errand-requests/${errandForm.id}`, errandForm)
    if (data.code === 200) { ElMessage.success('修改成功'); errandDlg.value = false; loadOrders() }
    else { ElMessage.error(data.message || '修改失败') }
  } catch { ElMessage.error('修改失败') }
  finally { editLoading.value = false }
}

/**
 * 删除跑腿订单
 * API: DELETE /errand-requests/{id}
 * @param {Object} o - 跑腿订单对象
 */
async function deleteErrandOrder (o) {
  try {
    await ElMessageBox.confirm('确定删除该跑腿需求吗？删除后不可恢复', '提示', { type: 'warning' })
    const { data } = await api.delete(`/errand-requests/${o.id}`)
    if (data.code === 200) { ElMessage.success('删除成功'); loadOrders() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

/**
 * 打开编辑检修订单对话框并填充数据
 * @param {Object} o - 检修订单对象
 */
function editRepair (o) {
  repairForm.id = o.id
  repairForm.title = o.title
  repairForm.description = o.description
  repairForm.repairType = o.repairType
  repairForm.location = o.location
  repairForm.urgency = o.urgency || 2
  repairDlg.value = true
}

/**
 * 保存编辑的检修订单
 * API: PUT /repair-requests/{id}
 */
async function saveRepair () {
  if (!repairForm.title) { ElMessage.warning('请输入标题'); return }
  editLoading.value = true
  try {
    const { data } = await api.put(`/repair-requests/${repairForm.id}`, repairForm)
    if (data.code === 200) { ElMessage.success('修改成功'); repairDlg.value = false; loadOrders() }
    else { ElMessage.error(data.message || '修改失败') }
  } catch { ElMessage.error('修改失败') }
  finally { editLoading.value = false }
}

/**
 * 删除检修订单
 * API: DELETE /repair-requests/{id}
 * @param {Object} o - 检修订单对象
 */
async function deleteRepairOrder (o) {
  try {
    await ElMessageBox.confirm('确定删除该检修需求吗？删除后不可恢复', '提示', { type: 'warning' })
    const { data } = await api.delete(`/repair-requests/${o.id}`)
    if (data.code === 200) { ElMessage.success('删除成功'); loadOrders() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

// 页面挂载时加载用户ID和订单数据
onMounted(() => {
  loadUid()
  loadOrders()
  // 从首页跳转过来时自动滚动到对应区域（orders/edit/password）
  nextTick(() => {
    const scrollTarget = route.query.scroll
    if (scrollTarget) {
      setTimeout(() => {
        document.getElementById(scrollTarget)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }, 300)
    }
  })
})
</script>

<style scoped>
.page-header { margin-bottom: var(--s6); }
.page-header h2 { font-size: var(--fs-xl); font-weight: var(--fw-700); margin: 0; color: var(--ink); }
.page-header p { font-size: var(--fs-sm); color: var(--ink-muted); margin: 2px 0 0; }

/* 用户卡片 */
.user-card { margin-bottom: var(--s5); }
.user-info { display: flex; align-items: center; gap: var(--s4); }
.user-avatar { width: 56px; height: 56px; border-radius: var(--r-md); background: var(--brand); color: var(--white); display: grid; place-items: center; font-size: 22px; font-weight: var(--fw-700); flex-shrink: 0; }
.user-detail h3 { font-size: var(--fs-lg); font-weight: var(--fw-700); margin: 0 0 2px; color: var(--ink); }
.user-detail p { font-size: var(--fs-sm); color: var(--ink-muted); margin: 0; }

/* 功能入口区 */
.func-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: var(--s3); margin-bottom: var(--s5); }
.func-card { background: var(--white); border: 1px solid var(--border); border-radius: var(--r-lg); padding: var(--s4); display: flex; align-items: center; gap: var(--s3); cursor: pointer; transition: all var(--fast); }
.func-card:hover { border-color: var(--brand); box-shadow: var(--sh-sm); }
.func-icon { width: 48px; height: 48px; border-radius: var(--r-md); display: grid; place-items: center; flex-shrink: 0; }
.func-body { flex: 1; }
.func-body strong { display: block; font-size: var(--fs-md); font-weight: var(--fw-600); color: var(--ink); }
.func-body span { font-size: var(--fs-sm); color: var(--ink-muted); }
.func-arrow { color: var(--ink-faint); flex-shrink: 0; }

/* 底部两栏：编辑资料 + 修改密码 */
.bottom-grid { display: grid; grid-template-columns: 1fr 1fr; gap: var(--s3); margin-bottom: var(--s5); }
.edit-form :deep(.el-input__wrapper) { border-radius: var(--r-md) !important; }
.form-row { display: flex; gap: var(--s3); }
.form-half { flex: 1; }

/* 订单区 */
.order-card { margin-bottom: var(--s4); }
.order-tabs :deep(.el-tabs__item.is-active) { color: var(--brand); font-weight: var(--fw-600); }
.order-tabs :deep(.el-tabs__active-bar) { background: var(--brand); }
.order-list { display: flex; flex-direction: column; gap: var(--s2); }
.order-row { display: flex; justify-content: space-between; align-items: center; padding: var(--s3) var(--s4); background: var(--bg-soft); border-radius: var(--r-md); }
.order-info strong { display: block; font-size: var(--fs-base); font-weight: var(--fw-600); color: var(--ink); }
.order-info time { font-size: var(--fs-xs); color: var(--ink-muted); }
.order-price { font-size: var(--fs-base); font-weight: var(--fw-700); color: var(--brand); display: block; margin: 4px 0; }
.order-tags { display: flex; gap: 4px; margin-top: 4px; }
.order-right { display: flex; align-items: center; gap: var(--s2); }

@media (max-width: 768px) { .func-grid, .bottom-grid { grid-template-columns: 1fr; } }
</style>

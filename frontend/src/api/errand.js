/*
 * ============================================================================
 * 跑腿需求 API 模块
 * ============================================================================
 * 提供管理员端和用户端对跑腿需求的管理和操作接口
 *
 * 角色说明：
 *   - publisher: 发布跑腿需求的用户
 *   - runner: 接单的跑腿员（普通用户 role=0，但角色名称为跑腿员）
 *
 * 跑腿需求状态流转：
 *   0（待接单）→ 1（已接单）→ 2（进行中）→ 3（已完成）/ 4（已取消）
 *
 * 接口分为两大类：
 *   - 管理员端（/admin/errand/*）：查看全部需求、分配跑腿员、删除需求、获取跑腿员列表
 *   - 用户端（/errand-requests/*）：查看自己的需求、创建/编辑/删除需求、接单/完成
 * ============================================================================
 */

/* 导入配置好的 axios 实例 */
import api from './index'

/* ========== 管理员端接口 ========== */

/**
 * 获取跑腿需求列表（管理员端，支持分页和筛选）
 * @method GET
 * @route /api/admin/errand
 * @param {Object} [params] - 查询参数（page, size, status 等筛选条件）
 * @returns {Promise} axios 响应对象
 */
export const getErrandList = (params) => api.get('/admin/errand', { params })

/**
 * 为跑腿需求分配跑腿员
 * @method PUT
 * @route /api/admin/errand/:id/assign
 * @param {number|string} id - 跑腿需求 ID
 * @param {number|string} runnerId - 跑腿员用户 ID
 * @returns {Promise} axios 响应对象
 */
export const assignRunner = (id, runnerId) => api.put(`/admin/errand/${id}/assign`, null, { params: { runnerId } })

/**
 * 删除跑腿需求（管理员强制删除）
 * @method DELETE
 * @route /api/admin/errand/:id
 * @param {number|string} id - 跑腿需求 ID
 * @param {string} reason - 删除原因
 * @returns {Promise} axios 响应对象
 */
export const deleteErrand = (id, reason) => api.delete(`/admin/errand/${id}`, { params: { reason } })

/**
 * 获取可用的跑腿员列表（管理员端，用于分配跑腿员时选择）
 * 仅返回普通用户（role=0），每次最多返回 100 条
 * @method GET
 * @route /api/admin/users
 * @returns {Promise} axios 响应对象，data.data 为跑腿员用户列表
 */
export const getRunnerList = () => api.get('/admin/users', { params: { role: 0, size: 100 } })

/* ========== 用户端接口 ========== */

/**
 * 获取用户自己的跑腿需求列表
 * @method GET
 * @route /api/errand-requests
 * @param {Object} [params] - 查询参数（page, size, status 等筛选条件）
 * @returns {Promise} axios 响应对象
 */
export const getUserErrandList = (params) => api.get('/errand-requests', { params })

/**
 * 创建跑腿需求
 * @method POST
 * @route /api/errand-requests
 * @param {Object} data - 跑腿需求数据（title 标题, description 描述, reward 报酬, urgency 紧急程度, errandType 类型等）
 * @returns {Promise} axios 响应对象
 */
export const createErrandRequest = (data) => api.post('/errand-requests', data)

/**
 * 更新跑腿需求（仅发布者本人可修改）
 * @method PUT
 * @route /api/errand-requests/:id
 * @param {number|string} id - 跑腿需求 ID
 * @param {Object} data - 要更新的字段
 * @returns {Promise} axios 响应对象
 */
export const updateErrandRequest = (id, data) => api.put(`/errand-requests/${id}`, data)

/**
 * 删除跑腿需求（仅发布者本人可删除）
 * @method DELETE
 * @route /api/errand-requests/:id
 * @param {number|string} id - 跑腿需求 ID
 * @returns {Promise} axios 响应对象
 */
export const deleteErrandRequest = (id) => api.delete(`/errand-requests/${id}`)

/**
 * 接单跑腿需求（跑腿员确认接单）
 * @method PUT
 * @route /api/errand-requests/:id/accept
 * @param {number|string} id - 跑腿需求 ID
 * @returns {Promise} axios 响应对象
 */
export const acceptErrandRequest = (id) => api.put(`/errand-requests/${id}/accept`)

/**
 * 完成跑腿需求（跑腿员标记已完成）
 * @method PUT
 * @route /api/errand-requests/:id/complete
 * @param {number|string} id - 跑腿需求 ID
 * @returns {Promise} axios 响应对象
 */
export const completeErrandRequest = (id) => api.put(`/errand-requests/${id}/complete`)

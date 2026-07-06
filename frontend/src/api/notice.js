/*
 * ============================================================================
 * 通知管理 API 模块（管理员端）
 * ============================================================================
 * 提供管理员对系统通知的 CRUD 操作接口
 *
 * 通知机制：
 *   - 管理员可在后台创建通知（系统公告、活动通知等）
 *   - 通知创建后在用户端展示（如首页公告栏、消息通知列表）
 *   - 支持分页查询和单个删除
 *
 * 注意：通知发布后应避免修改（如需修改，建议删除后重新创建）
 * ============================================================================
 */

/* 导入配置好的 axios 实例 */
import api from './index'

/**
 * 获取通知列表（分页查询）
 * @method GET
 * @route /api/admin/notices
 * @param {Object} [params] - 查询参数（page 页码, size 每页条数）
 * @returns {Promise} axios 响应对象，data.data.records 为通知列表
 */
export const getNoticeList = (params) => api.get('/admin/notices', { params })

/**
 * 创建新通知
 * @method POST
 * @route /api/admin/notices
 * @param {Object} data - 通知数据（title 标题, content 内容, type 通知类型等）
 * @returns {Promise} axios 响应对象
 */
export const createNotice = (data) => api.post('/admin/notices', data)

/**
 * 删除通知
 * @method DELETE
 * @route /api/admin/notices/:id
 * @param {number|string} id - 要删除的通知 ID
 * @returns {Promise} axios 响应对象
 */
export const deleteNotice = (id) => api.delete(`/admin/notices/${id}`)

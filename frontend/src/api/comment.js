/*
 * ============================================================================
 * 评论管理 API 模块（管理员端）
 * ============================================================================
 * 提供管理员对用户评论的管理操作接口
 *
 * 操作权限：
 *   - 查看评论列表：管理员可查看所有评论
 *   - 审核评论：通过/拒绝（status 参数控制）
 *   - 删除评论：硬删除
 * ============================================================================
 */

/* 导入配置好的 axios 实例 */
import api from './index'

/**
 * 获取评论列表（分页，支持筛选条件）
 * @method GET
 * @route /api/admin/comments
 * @param {Object} [params] - 查询参数（page 页码, size 每页条数, status 审核状态等）
 * @returns {Promise} axios 响应对象，data.data.records 为评论列表
 */
export const getCommentList = (params) => api.get('/admin/comments', { params })

/**
 * 审核评论（通过或拒绝）
 * @method PUT
 * @route /api/admin/comments/:id/audit
 * @param {number|string} id - 评论 ID
 * @param {number} status - 审核状态（0=待审核, 1=审核通过, 2=审核拒绝）
 * @returns {Promise} axios 响应对象
 */
export const auditComment = (id, status) => api.put(`/admin/comments/${id}/audit`, null, { params: { status } })

/**
 * 删除评论
 * @method DELETE
 * @route /api/admin/comments/:id
 * @param {number|string} id - 要删除的评论 ID
 * @returns {Promise} axios 响应对象
 */
export const deleteComment = (id) => api.delete(`/admin/comments/${id}`)

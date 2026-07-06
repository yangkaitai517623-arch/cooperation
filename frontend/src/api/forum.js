/*
 * ============================================================================
 * 社区论坛 API 模块
 * ============================================================================
 * 提供管理员端和用户端对论坛帖子的管理操作接口
 *
 * 论坛帖子状态说明：
 *   - 0: 待审核（用户发布后默认为待审核状态）
 *   - 1: 审核通过（公开可见）
 *   - 2: 下架/审核拒绝（不可见）
 *
 * 接口分类：
 *   - 管理员端（/admin/forum/*）：查看全部帖子、审核通过/拒绝、下架、删除
 *   - 用户端（/forum/posts/*，/forum/comments/*）：点赞/取消点赞帖子，点赞/取消点赞评论
 * ============================================================================
 */

/* 导入配置好的 axios 实例 */
import api from './index'

/* ========== 管理员端接口 ========== */

/**
 * 获取论坛帖子列表（管理员端，支持分页和状态筛选）
 * @method GET
 * @route /api/admin/forum
 * @param {Object} [params] - 查询参数（page, size, status 审核状态等筛选条件）
 * @returns {Promise} axios 响应对象
 */
export const getForumList = (params) => api.get('/admin/forum', { params })

/**
 * 审核论坛帖子（通过或拒绝）
 * @method PUT
 * @route /api/admin/forum/:id/audit
 * @param {number|string} id - 帖子 ID
 * @param {number} status - 审核状态（0=待审核, 1=审核通过, 2=拒绝）
 * @returns {Promise} axios 响应对象
 */
export const auditPost = (id, status) => api.put(`/admin/forum/${id}/audit`, null, { params: { status } })

/**
 * 下架帖子（将帖子状态设为 2=下架）
 * 复用审核接口，强制传入 status=2
 * @method PUT
 * @route /api/admin/forum/:id/audit
 * @param {number|string} id - 帖子 ID
 * @returns {Promise} axios 响应对象
 */
export const offShelfPost = (id) => api.put(`/admin/forum/${id}/audit`, null, { params: { status: 2 } })

/**
 * 删除论坛帖子
 * @method DELETE
 * @route /api/admin/forum/:id
 * @param {number|string} id - 要删除的帖子 ID
 * @returns {Promise} axios 响应对象
 */
export const deletePost = (id) => api.delete(`/admin/forum/${id}`)

/* ========== 用户端接口 ========== */

/**
 * 点赞帖子
 * @method PUT
 * @route /api/forum/posts/:id/like
 * @param {number|string} id - 帖子 ID
 * @returns {Promise} axios 响应对象
 */
export const likePost = (id) => api.put(`/forum/posts/${id}/like`)

/**
 * 取消点赞帖子
 * @method PUT
 * @route /api/forum/posts/:id/unlike
 * @param {number|string} id - 帖子 ID
 * @returns {Promise} axios 响应对象
 */
export const unlikePost = (id) => api.put(`/forum/posts/${id}/unlike`)

/**
 * 点赞评论
 * @method PUT
 * @route /api/forum/comments/:id/like
 * @param {number|string} id - 评论 ID
 * @returns {Promise} axios 响应对象
 */
export const likeComment = (id) => api.put(`/forum/comments/${id}/like`)

/**
 * 取消点赞评论
 * @method PUT
 * @route /api/forum/comments/:id/unlike
 * @param {number|string} id - 评论 ID
 * @returns {Promise} axios 响应对象
 */
export const unlikeComment = (id) => api.put(`/forum/comments/${id}/unlike`)

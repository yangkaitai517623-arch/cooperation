/*
 * ============================================================================
 * 管理员管理 API 模块
 * ============================================================================
 * 提供管理员账号的 CRUD 操作、用户角色管理及密码修改接口
 *
 * 权限说明：
 *   - 管理员列表/创建/更新/删除  → 超级管理员 (role=2) 操作
 *   - 修改用户角色               → 管理员 (role>=1) 操作
 *   - 修改密码                   → 所有已登录用户可操作
 * ============================================================================
 */

/* 导入配置好的 axios 实例 */
import api from './index'

/**
 * 获取管理员列表（分页）
 * @method GET
 * @route /api/admin/admins
 * @param {Object} [params] - 查询参数（可选，支持 page、size 等分页参数）
 * @returns {Promise} axios 响应对象，data.data 为管理员列表
 */
export const getAdminList = (params) => api.get('/admin/admins', { params })

/**
 * 创建新管理员账号
 * @method POST
 * @route /api/admin/admins
 * @param {Object} data - 管理员信息（username, password, role 等）
 * @returns {Promise} axios 响应对象
 */
export const createAdmin = (data) => api.post('/admin/admins', data)

/**
 * 更新管理员信息
 * @method PUT
 * @route /api/admin/admins/:id
 * @param {number|string} id - 管理员 ID
 * @param {Object} data - 要更新的管理员字段
 * @returns {Promise} axios 响应对象
 */
export const updateAdmin = (id, data) => api.put(`/admin/admins/${id}`, data)

/**
 * 删除管理员账号
 * @method DELETE
 * @route /api/admin/admins/:id
 * @param {number|string} id - 要删除的管理员 ID
 * @returns {Promise} axios 响应对象
 */
export const deleteAdmin = (id) => api.delete(`/admin/admins/${id}`)

/**
 * 更新用户角色
 * @method PUT
 * @route /api/admin/users/:id/role
 * @param {number|string} id - 用户 ID
 * @param {number} role - 新角色值（0=普通用户, 1=管理员, 2=超级管理员）
 * @returns {Promise} axios 响应对象
 */
export const updateUserRole = (id, role) => api.put(`/admin/users/${id}/role`, null, { params: { role } })

/**
 * 修改当前登录用户的密码
 * @method PUT
 * @route /api/user/password
 * @param {Object} data - 密码信息（oldPassword 旧密码, newPassword 新密码）
 * @returns {Promise} axios 响应对象
 */
export const updatePassword = (data) => api.put('/user/password', data)

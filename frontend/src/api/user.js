/*
 * ============================================================================
 * 用户管理 API 模块（管理员端）
 * ============================================================================
 * 提供管理员对用户信息的管理操作接口
 *
 * 用户角色说明：
 *   - role=0: 普通用户（社区住户/居民）
 *   - role=1: 管理员（后台管理）
 *   - role=2: 超级管理员（最高权限）
 *   - role=3: 专职人员（如维修师傅）
 *
 * 管理员可操作：
 *   - 查看所有用户列表（支持分页和角色筛选）
 *   - 编辑用户基本信息
 *   - 修改用户角色（通过 admin.js 中的 updateUserRole 接口）
 * ============================================================================
 */

/* 导入配置好的 axios 实例 */
import api from './index'

/**
 * 获取用户列表（分页，支持角色筛选）
 * @method GET
 * @route /api/admin/users
 * @param {Object} [params] - 查询参数（page 页码, size 每页条数, role 角色筛选, keyword 搜索关键词等）
 * @returns {Promise} axios 响应对象，data.data.records 为用户列表
 */
export const getUserList = (params) => api.get('/admin/users', { params })

/**
 * 更新用户信息
 * @method PUT
 * @route /api/admin/users/:id
 * @param {number|string} id - 用户 ID
 * @param {Object} data - 要更新的用户字段（如 nickname, phone, avatar 等）
 * @returns {Promise} axios 响应对象
 */
export const updateUser = (id, data) => api.put(`/admin/users/${id}`, data)

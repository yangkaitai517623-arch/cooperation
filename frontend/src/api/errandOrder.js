/*
 * ============================================================================
 * 跑腿订单管理 API 模块（管理员端）
 * ============================================================================
 * 提供管理员查看和管理跑腿订单的接口
 *
 * 跑腿订单：记录跑腿服务的完整交易信息，包括：
 *   - 发布者信息
 *   - 跑腿员信息
 *   - 服务内容
 *   - 费用信息
 *   - 订单状态和时间线
 * ============================================================================
 */

/* 导入配置好的 axios 实例 */
import api from './index'

/**
 * 获取跑腿订单列表（分页，支持筛选条件）
 * @method GET
 * @route /api/admin/errand-orders
 * @param {Object} [params] - 查询参数（page 页码, size 每页条数, status 订单状态筛选等）
 * @returns {Promise} axios 响应对象，data.data.records 为订单列表
 */
export const getErrandOrderList = (params) => api.get('/admin/errand-orders', { params })

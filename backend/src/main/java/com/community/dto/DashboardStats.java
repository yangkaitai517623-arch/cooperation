package com.community.dto;

import lombok.Data;
import java.util.List;

/**
 * 仪表盘统计数据 DTO — 管理后台首页的数据概览
 *
 * <p>聚合了平台的各项核心指标，通过一个 API 调用即可获取所有统计数据，
 * 避免前端发起多个请求。数据在 Controller/Service 层计算后组装到此对象中返回。</p>
 *
 * <p>统计维度包含：</p>
 * <ul>
 *   <li><b>基础统计</b>：用户数、商品数、待处理数等绝对值</li>
 *   <li><b>月度数据</b>：当月订单数、满意度</li>
 *   <li><b>环比变化</b>：相对于上月的百分比变化（正值=增长，负值=下降）</li>
 *   <li><b>动态列表</b>：最新活动时间线</li>
 *   <li><b>待处理清单</b>：需要管理员关注的事项</li>
 * </ul>
 *
 * @see ActivityItem 最新动态条目
 * @see PendingItem 待处理条目
 */
@Data
public class DashboardStats {

    // ========== 基础统计（绝对值）==========

    /** 注册用户总数 */
    private long totalUsers;

    /** 活跃用户数（近期有登录或操作的用户） */
    private long activeUsers;

    /** 商品总数（所有状态的二手商品） */
    private long totalGoods;

    /** 在售商品数（status=1 的商品） */
    private long onSaleGoods;

    /** 待处理检修需求数（status=0 待接单的检修） */
    private long pendingRepairs;

    /** 待处理跑腿需求数（status=0 待接单的跑腿） */
    private long pendingErrands;

    /** 当前管理员未读通知数 */
    private long unreadNotifications;

    // ========== 月度数据（本月新增）==========

    /** 本月订单总数（商品订单 + 跑腿订单 + 检修订单） */
    private long monthlyOrders;

    /** 本月用户满意度（百分比，如 95.5 表示 95.5%） */
    private double satisfactionRate;

    // ========== 环比变化（%）==========

    /** 用户数环比变化百分比（正值=增长，负值=下降） */
    private double changeUsers;

    /** 订单数环比变化百分比 */
    private double changeOrders;

    /** 待处理数环比变化百分比 */
    private double changePending;

    /** 满意度环比变化百分比 */
    private double changeSatisfaction;

    // ========== 审核相关 ==========

    /** 待审核商品数（status=0 待审核的商品） */
    private long pendingGoodsAudit;

    // ========== 动态列表 ==========

    /** 最新动态时间线列表（最近 N 条操作记录） */
    private List<ActivityItem> recentActivities;

    /** 待处理事项列表（管理员需关注的高优先级任务） */
    private List<PendingItem> pendingItems;

    /**
     * 最新动态条目
     * <p>例如：张三 发布了跑腿需求、李四 注册了账号</p>
     */
    @Data
    public static class ActivityItem {
        /** 动态标题（如 "张工 完成了检修订单"） */
        private String title;

        /** 动态描述（补充说明） */
        private String desc;

        /** 动态发生时间 */
        private String time;

        /** 动态类型（如 "order"、"register"、"repair"） */
        private String type;
    }

    /**
     * 待处理事项条目
     * <p>点击可跳转到对应的管理页面进行处理</p>
     */
    @Data
    public static class PendingItem {
        /** 待处理事项标题（如 "有 5 件商品待审核"） */
        private String title;

        /** 标签（如 "待审核"、"紧急"） */
        private String tag;

        /** 事项类型（如 "goods_audit"、"repair"、"errand"） */
        private String type;

        /** 跳转链接（前端路由路径，如 "/admin/goods"） */
        private String link;
    }
}

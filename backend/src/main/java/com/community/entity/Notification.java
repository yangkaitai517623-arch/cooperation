package com.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统通知实体 — 映射 notification 表
 *
 * <p>用于向用户推送系统公告、订单状态变更提醒、需求进展通知等消息。
 * 采用"拉取"模式：用户在前端主动拉取通知列表，标记已读。</p>
 *
 * <p>通知类型：</p>
 * <ul>
 *   <li><b>系统通知</b>（type=1）：管理员发布的公告、系统维护通知等</li>
 *   <li><b>订单通知</b>（type=2）：订单状态变更、有人下单/接单等</li>
 *   <li><b>需求通知</b>（type=3）：跑腿/检修需求的状态变更提醒</li>
 * </ul>
 *
 * <p>发送目标：</p>
 * <ul>
 *   <li>userId != null → 指定用户（个人通知）</li>
 *   <li>userId == null → 全体用户（全局公告，管理员批量群发）</li>
 * </ul>
 */
@Data
@TableName("notification")
public class Notification {

    /** 通知 ID（主键，自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户 ID
     * <ul>
     *   <li>null — 全局通知（面向所有用户）</li>
     *   <li>非 null — 指定用户通知</li>
     * </ul>
     */
    private Long userId;

    /** 通知标题（简短摘要，如 "系统维护通知"、"您的订单已确认"） */
    private String title;

    /** 通知正文内容（详细说明） */
    private String content;

    /**
     * 通知类型
     * <ul>
     *   <li>1 — 系统通知（公告、维护提醒等）</li>
     *   <li>2 — 订单通知（商品/跑腿/检修订单状态变更）</li>
     *   <li>3 — 需求通知（跑腿/检修需求进展提醒）</li>
     * </ul>
     */
    private Integer type;

    /**
     * 已读状态
     * <ul>
     *   <li>0 — 未读（前端小红点提示）</li>
     *   <li>1 — 已读</li>
     * </ul>
     */
    private Integer isRead;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

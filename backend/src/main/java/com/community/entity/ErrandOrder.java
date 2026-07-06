package com.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 跑腿订单实体 — 映射 errand_order 表
 *
 * <p>跑腿需求被接单后生成的正式订单记录，追踪跑腿服务的执行和支付状态。
 * 与 {@link ErrandRequest} 是一对一关系：每个跑腿需求在被接单后生成一条订单。</p>
 *
 * <p>订单生命周期：待支付 → 已支付 → 配送中 → 已完成 / 已取消</p>
 *
 * <p>关联关系：</p>
 * <ul>
 *   <li>requestId → ErrandRequest（关联原始需求）</li>
 *   <li>userId → SysUser（需求发布者/付款方）</li>
 *   <li>runnerId → SysUser（接单跑腿员）</li>
 * </ul>
 */
@Data
@TableName("errand_order")
public class ErrandOrder {

    /** 订单 ID（主键，自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号（唯一标识，格式如 "EO20240601001"） */
    private String orderNo;

    /** 关联的跑腿需求 ID（外键关联 errand_request 表） */
    private Long requestId;

    /** 需求发布者（付款方）用户 ID（外键关联 sys_user 表） */
    private Long userId;

    /** 接单跑腿员用户 ID（外键关联 sys_user 表） */
    private Long runnerId;

    /** 订单金额（即跑腿需求的赏金 reward） */
    private BigDecimal amount;

    /**
     * 订单状态
     * <ul>
     *   <li>0 — 待支付</li>
     *   <li>1 — 已支付</li>
     *   <li>2 — 配送中（跑腿员正在执行）</li>
     *   <li>3 — 已完成</li>
     *   <li>4 — 已取消</li>
     * </ul>
     */
    private Integer status;

    /** 评分（1-5 星，完成后的评价分数） */
    private Integer rating;

    /** 评价内容（完成后的文字评价） */
    private String comment;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

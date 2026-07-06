package com.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 跑腿需求实体 — 映射 errand_request 表
 *
 * <p>社区居民发布的跑腿服务需求，是"跑腿代办"模块的核心数据表。
 * 用户可以发布取快递、代买东西、送文件等跑腿需求，其他用户或专职跑腿员可以接单。</p>
 *
 * <p>特色功能：</p>
 * <ul>
 *   <li>支持 AI 智能分析需求描述，自动识别跑腿类型和紧急程度</li>
 *   <li>完整的接单→配送→完成→评价流程</li>
 *   <li>赏金机制（reward 字段），激励社区互助</li>
 * </ul>
 *
 * @see ErrandOrder 跑腿订单（接单后生成）
 */
@Data
@TableName("errand_request")
public class ErrandRequest {

    /** 需求 ID（主键，自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布者用户 ID（外键关联 sys_user 表） */
    private Long userId;

    /** 需求标题（简短描述，如 "帮我取个快递"） */
    private String title;

    /** 需求详细描述（自由文本，说明具体要做什么） */
    private String description;

    /**
     * 跑腿类型
     * <p>常见取值：取快递、代购买、送文件、代办事务、其他</p>
     */
    private String errandType;

    /** 取件地址（从何处取物） */
    private String pickupAddress;

    /** 送达地址（送到何处） */
    private String deliveryAddress;

    /**
     * 紧急程度
     * <ul>
     *   <li>0 — 不急</li>
     *   <li>1 — 一般</li>
     *   <li>2 — 紧急</li>
     * </ul>
     */
    private Integer urgency;

    /** 赏金/报酬（发布者愿意支付的金额，单位：元） */
    private BigDecimal reward;

    /** AI 分析标签（AI 对需求描述分析后打上的分类标签，逗号分隔） */
    private String aiTags;

    /** AI 分析的紧急程度（AI 自动判断的结果，可能与用户手动选择不一致） */
    private String aiUrgency;

    /**
     * 需求状态
     * <ul>
     *   <li>0 — 待接单（已发布，等待有人接单）</li>
     *   <li>1 — 已接单（有人接了单，但尚未完成）</li>
     *   <li>2 — 配送中（接单者正在执行跑腿任务）</li>
     *   <li>3 — 已完成（跑腿任务完成，可进行评价）</li>
     *   <li>4 — 已取消（需求被取消）</li>
     * </ul>
     */
    private Integer status;

    /** 接单者用户 ID（外键关联 sys_user 表，被接单后赋值） */
    private Long runnerId;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

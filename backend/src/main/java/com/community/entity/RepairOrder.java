package com.community.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("repair_order")
public class RepairOrder {

    public static final int STATUS_PROCESSING = 0;
    public static final int STATUS_COMPLETED = 1;
    public static final int STATUS_REVIEWED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long requestId;

    private Long userId;

    private Long workerId;

    private BigDecimal amount;

    private Integer status;

    private Integer rating;

    private String comment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

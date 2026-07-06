package com.community.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("second_hand_goods")
public class SecondHandGoods {

    public static final int STATUS_PENDING_AUDIT = 0;
    public static final int STATUS_ON_SALE = 1;
    public static final int STATUS_SOLD = 2;
    public static final int STATUS_OFF_SHELF = 3;
    public static final int STATUS_REJECTED = 4;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private Long categoryId;

    private BigDecimal originalPrice;

    private BigDecimal sellingPrice;

    private BigDecimal aiEstimatedPrice;

    private String conditionLevel;

    private String images;

    private Long sellerId;

    private Long buyerId;

    private Integer status;

    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

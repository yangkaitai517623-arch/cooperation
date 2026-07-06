package com.community.dto;

import com.community.entity.ErrandRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 跑腿需求视图对象 — 继承 ErrandRequest 并补充关联数据
 *
 * <p>在原始需求实体的基础上，通过 JOIN 查询添加发布者和接单人的姓名，
 * 使得前端页面直接展示用户的真实姓名而非用户 ID。</p>
 *
 * <p>继承关系：ErrandRequest（基类）← ErrandRequestVO（子类扩展关联字段）</p>
 *
 * @see ErrandRequest 原始跑腿需求实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ErrandRequestVO extends ErrandRequest {

    /** 发布者姓名（来自 sys_user.real_name） */
    private String publisherName;

    /** 接单人姓名（来自 sys_user.real_name，未接单时为空） */
    private String runnerName;

    public ErrandRequestVO() {}

    /**
     * 通过 ErrandRequest 实体构造视图对象
     * <p>将原始实体的所有字段复制到视图对象，关联字段（publisherName、runnerName）
     * 需在 Service 层额外通过 JOIN 查询填充。</p>
     *
     * @param request 原始跑腿需求实体
     */
    public ErrandRequestVO(ErrandRequest request) {
        this.setId(request.getId());
        this.setUserId(request.getUserId());
        this.setTitle(request.getTitle());
        this.setDescription(request.getDescription());
        this.setErrandType(request.getErrandType());
        this.setPickupAddress(request.getPickupAddress());
        this.setDeliveryAddress(request.getDeliveryAddress());
        this.setUrgency(request.getUrgency());
        this.setReward(request.getReward());
        this.setAiTags(request.getAiTags());
        this.setAiUrgency(request.getAiUrgency());
        this.setStatus(request.getStatus());
        this.setRunnerId(request.getRunnerId());
        this.setCreatedAt(request.getCreatedAt());
        this.setUpdatedAt(request.getUpdatedAt());
    }
}

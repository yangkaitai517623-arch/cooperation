package com.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.dto.PageResult;
import com.community.dto.Result;
import com.community.entity.RepairOrder;
import com.community.entity.RepairRequest;
import com.community.repository.RepairOrderMapper;
import com.community.repository.RepairRequestMapper;
import com.community.service.RepairRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/repair-orders")
@RequiredArgsConstructor
public class AdminRepairOrderController {

    private final RepairOrderMapper orderMapper;
    private final RepairRequestMapper requestMapper;
    private final RepairRequestService repairService;

    @GetMapping
    public Result<PageResult<RepairRequest>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<RepairRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(RepairRequest::getStatus,
                RepairRequest.STATUS_ACCEPTED,
                RepairRequest.STATUS_REPAIRING,
                RepairRequest.STATUS_COMPLETED);
        if (status != null) {
            wrapper.eq(RepairRequest::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(RepairRequest::getTitle, keyword);
        }
        wrapper.orderByDesc(RepairRequest::getCreatedAt);
        IPage<RepairRequest> pageResult = requestMapper.selectPage(new Page<>(page, size), wrapper);
        enrichOrderFields(pageResult.getRecords());
        return Result.success(new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), page, size));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return repairService.updateStatus(id, status);
    }

    private void enrichOrderFields(Iterable<RepairRequest> records) {
        for (RepairRequest request : records) {
            if (request == null || request.getId() == null) {
                continue;
            }
            RepairOrder order = orderMapper.findByRequestId(request.getId());
            if (order == null) {
                request.setOrderStatus(null);
                request.setOrderRating(null);
                request.setOrderComment(null);
                continue;
            }
            request.setOrderStatus(order.getStatus());
            request.setOrderRating(order.getRating());
            request.setOrderComment(order.getComment());
        }
    }
}

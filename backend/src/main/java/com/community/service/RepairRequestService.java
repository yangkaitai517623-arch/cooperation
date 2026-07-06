package com.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.dto.PageResult;
import com.community.dto.Result;
import com.community.entity.RepairOrder;
import com.community.entity.RepairRequest;
import com.community.repository.RepairOrderMapper;
import com.community.repository.RepairRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RepairRequestService {

    private static final DateTimeFormatter ORDER_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final RepairRequestMapper requestMapper;
    private final RepairOrderMapper orderMapper;

    public PageResult<RepairRequest> listRequests(int page, int size, Integer status, String keyword) {
        LambdaQueryWrapper<RepairRequest> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(RepairRequest::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(RepairRequest::getTitle, keyword)
                    .or()
                    .like(RepairRequest::getDescription, keyword));
        }
        wrapper.orderByDesc(RepairRequest::getCreatedAt);
        IPage<RepairRequest> pageResult = requestMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    public RepairRequest getRequestById(Long id) {
        return requestMapper.selectById(id);
    }

    public Result<Void> addRequest(RepairRequest request, Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        if (!StringUtils.hasText(request.getTitle())) {
            return Result.error("检修标题不能为空");
        }
        request.setId(null);
        request.setUserId(userId);
        request.setWorkerId(null);
        request.setStatus(RepairRequest.STATUS_PENDING);
        request.setDeleted(0);
        requestMapper.insert(request);
        return Result.success("检修需求发布成功", null);
    }

    public PageResult<RepairRequest> getMyRequests(Long userId, int page, int size) {
        LambdaQueryWrapper<RepairRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(RepairRequest::getUserId, userId)
                .or()
                .eq(RepairRequest::getWorkerId, userId))
                .orderByDesc(RepairRequest::getCreatedAt);
        IPage<RepairRequest> pageResult = requestMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    public Result<Void> acceptRequest(Long id, Long workerId) {
        RepairRequest request = requestMapper.selectById(id);
        if (request == null) {
            return Result.error("检修需求不存在");
        }
        if (!Integer.valueOf(RepairRequest.STATUS_PENDING).equals(request.getStatus())) {
            return Result.error("该需求已被接单或已完成");
        }
        if (workerId == null) {
            return Result.error("请先登录");
        }
        if (workerId.equals(request.getUserId())) {
            return Result.error("不能接自己发布的检修需求");
        }

        request.setWorkerId(workerId);
        request.setStatus(RepairRequest.STATUS_ACCEPTED);
        requestMapper.updateById(request);
        createOrUpdateOrder(request, RepairOrder.STATUS_PROCESSING);
        return Result.success("接单成功", null);
    }

    public Result<Void> completeRequest(Long id, Long userId) {
        RepairRequest request = requestMapper.selectById(id);
        if (request == null) {
            return Result.error("检修需求不存在");
        }
        if (!userId.equals(request.getUserId())) {
            return Result.error("只有发布者可以确认完成");
        }
        request.setStatus(RepairRequest.STATUS_COMPLETED);
        requestMapper.updateById(request);
        createOrUpdateOrder(request, RepairOrder.STATUS_COMPLETED);
        return Result.success("检修需求已完成", null);
    }

    public Result<Void> assignWorker(Long id, Long workerId) {
        RepairRequest request = requestMapper.selectById(id);
        if (request == null) {
            return Result.error("检修需求不存在");
        }
        request.setWorkerId(workerId);
        request.setStatus(RepairRequest.STATUS_ACCEPTED);
        requestMapper.updateById(request);
        createOrUpdateOrder(request, RepairOrder.STATUS_PROCESSING);
        return Result.success("师傅分配成功", null);
    }

    public Result<Void> updateStatus(Long id, Integer status) {
        RepairRequest request = requestMapper.selectById(id);
        if (request == null) {
            return Result.error("检修需求不存在");
        }
        request.setStatus(status);
        requestMapper.updateById(request);
        return Result.success("状态更新成功", null);
    }

    public Result<Void> updateRequest(RepairRequest request, Long userId) {
        RepairRequest existing = requestMapper.selectById(request.getId());
        if (existing == null) {
            return Result.error("检修需求不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            return Result.error("只能修改自己发布的检修需求");
        }
        if (!Integer.valueOf(RepairRequest.STATUS_PENDING).equals(existing.getStatus())) {
            return Result.error("已接单的检修需求不能修改");
        }
        request.setUserId(userId);
        request.setWorkerId(existing.getWorkerId());
        request.setStatus(existing.getStatus());
        requestMapper.updateById(request);
        return Result.success("检修需求更新成功", null);
    }

    public Result<Void> deleteRequest(Long id, Long userId) {
        RepairRequest existing = requestMapper.selectById(id);
        if (existing == null) {
            return Result.error("检修需求不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            return Result.error("只能删除自己发布的检修需求");
        }
        if (!Integer.valueOf(RepairRequest.STATUS_PENDING).equals(existing.getStatus())) {
            return Result.error("已接单的检修需求不能删除");
        }
        requestMapper.deleteById(id);
        return Result.success("检修需求删除成功", null);
    }

    private void createOrUpdateOrder(RepairRequest request, Integer orderStatus) {
        RepairOrder order = orderMapper.findByRequestId(request.getId());
        if (order == null) {
            order = new RepairOrder();
            order.setOrderNo(generateOrderNo());
            order.setRequestId(request.getId());
        }
        order.setUserId(request.getUserId());
        order.setWorkerId(request.getWorkerId());
        order.setAmount(request.getActualPrice() != null ? request.getActualPrice() : request.getEstimatedPrice());
        order.setStatus(orderStatus);
        if (order.getId() == null) {
            orderMapper.insert(order);
        } else {
            orderMapper.updateById(order);
        }
    }

    private String generateOrderNo() {
        return "RO" + LocalDateTime.now().format(ORDER_TIME_FORMAT)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}

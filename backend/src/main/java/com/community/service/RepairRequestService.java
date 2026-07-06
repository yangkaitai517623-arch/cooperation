package com.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.dto.PageResult;
import com.community.dto.Result;
import com.community.entity.RepairOrder;
import com.community.entity.RepairRequest;
import com.community.entity.SysUser;
import com.community.repository.RepairOrderMapper;
import com.community.repository.RepairRequestMapper;
import com.community.repository.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RepairRequestService {

    private static final DateTimeFormatter ORDER_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final RepairRequestMapper requestMapper;
    private final RepairOrderMapper orderMapper;
    private final SysUserMapper userMapper;
    private final NotificationService notificationService;

    public PageResult<RepairRequest> listRequests(
            int page,
            int size,
            Integer status,
            List<Integer> statuses,
            String scope,
            String keyword,
            Long userId) {
        LambdaQueryWrapper<RepairRequest> wrapper = new LambdaQueryWrapper<>();
        applyScope(wrapper, status, statuses, scope, userId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(RepairRequest::getTitle, keyword)
                    .or()
                    .like(RepairRequest::getDescription, keyword));
        }
        wrapper.orderByDesc(RepairRequest::getCreatedAt);

        IPage<RepairRequest> pageResult = requestMapper.selectPage(new Page<>(page, size), wrapper);
        enrichRepairOrders(pageResult.getRecords());
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    public RepairRequest getRequestById(Long id) {
        RepairRequest request = requestMapper.selectById(id);
        enrichRepairOrder(request);
        return request;
    }

    public Result<Void> addRequest(RepairRequest request, Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        Result<Void> validation = validateRepairRequest(request);
        if (validation.getCode() != 200) {
            return validation;
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
        if (userId == null) {
            return new PageResult<>(List.of(), 0, page, size);
        }
        LambdaQueryWrapper<RepairRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(RepairRequest::getUserId, userId)
                .or(q -> q.eq(RepairRequest::getWorkerId, userId)
                        .in(RepairRequest::getStatus,
                                RepairRequest.STATUS_ACCEPTED,
                                RepairRequest.STATUS_REPAIRING,
                                RepairRequest.STATUS_COMPLETED)))
                .orderByDesc(RepairRequest::getCreatedAt);
        IPage<RepairRequest> pageResult = requestMapper.selectPage(new Page<>(page, size), wrapper);
        enrichRepairOrders(pageResult.getRecords());
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> acceptRequest(Long id, Long workerId) {
        if (workerId == null) {
            return Result.error("请先登录");
        }
        RepairRequest request = requestMapper.selectById(id);
        if (request == null) {
            return Result.error("检修需求不存在");
        }
        if (!Integer.valueOf(RepairRequest.STATUS_PENDING).equals(request.getStatus())) {
            return Result.error("该需求已被接单或已完成");
        }
        if (workerId.equals(request.getUserId())) {
            return Result.error("不能接自己发布的检修需求");
        }
        if (hasActiveTask(workerId, id)) {
            return Result.error("你已有进行中的检修任务");
        }

        LambdaUpdateWrapper<RepairRequest> update = new LambdaUpdateWrapper<>();
        update.eq(RepairRequest::getId, id)
                .eq(RepairRequest::getStatus, RepairRequest.STATUS_PENDING)
                .isNull(RepairRequest::getWorkerId)
                .set(RepairRequest::getWorkerId, workerId)
                .set(RepairRequest::getStatus, RepairRequest.STATUS_ACCEPTED);
        int affected = requestMapper.update(null, update);
        if (affected == 0) {
            return Result.error("该需求已被其他人接单");
        }

        request.setWorkerId(workerId);
        request.setStatus(RepairRequest.STATUS_ACCEPTED);
        createOrUpdateOrder(request, RepairOrder.STATUS_PROCESSING);
        notificationService.sendNotification(request.getUserId(), "检修需求已接单",
                "您的检修需求已被接单：" + request.getTitle(), 2);
        return Result.success("接单成功", null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> assignWorker(Long id, Long workerId) {
        RepairRequest request = requestMapper.selectById(id);
        if (request == null) {
            return Result.error("检修需求不存在");
        }
        if (request.getStatus() == null
                || (request.getStatus() != RepairRequest.STATUS_PENDING
                && request.getStatus() != RepairRequest.STATUS_ACCEPTED
                && request.getStatus() != RepairRequest.STATUS_REPAIRING)) {
            return Result.error("只有待接单或进行中的检修需求可以分配");
        }
        Result<Void> workerCheck = validateWorker(request, workerId);
        if (workerCheck.getCode() != 200) {
            return workerCheck;
        }
        if (hasActiveTask(workerId, id)) {
            return Result.error("该师傅已有进行中的检修任务");
        }

        Long oldWorkerId = request.getWorkerId();
        request.setWorkerId(workerId);
        request.setStatus(RepairRequest.STATUS_ACCEPTED);
        requestMapper.updateById(request);
        createOrUpdateOrder(request, RepairOrder.STATUS_PROCESSING);

        notificationService.sendNotification(request.getUserId(), "检修需求已分配",
                "您的检修需求已由管理员分配：" + request.getTitle(), 2);
        notificationService.sendNotification(workerId, "新的检修任务",
                "管理员已为您分配检修任务：" + request.getTitle(), 2);
        if (oldWorkerId != null && !oldWorkerId.equals(workerId)) {
            notificationService.sendNotification(oldWorkerId, "检修任务已改派",
                    "您的检修任务已被管理员改派：" + request.getTitle(), 2);
        }
        return Result.success("师傅分配成功", null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelAccept(Long id, Long workerId) {
        if (workerId == null) {
            return Result.error("请先登录");
        }
        RepairRequest request = requestMapper.selectById(id);
        if (request == null) {
            return Result.error("检修需求不存在");
        }
        if (!workerId.equals(request.getWorkerId())) {
            return Result.error("只有当前接单人可以取消接单");
        }
        if (request.getStatus() == null
                || (request.getStatus() != RepairRequest.STATUS_ACCEPTED
                && request.getStatus() != RepairRequest.STATUS_REPAIRING)) {
            return Result.error("当前状态不能取消接单");
        }

        request.setWorkerId(null);
        request.setStatus(RepairRequest.STATUS_PENDING);
        requestMapper.updateById(request);
        deleteOrderByRequestId(id);
        notificationService.sendNotification(request.getUserId(), "检修需求回到待接单",
                "接单人已取消接单，需求已回到待接单：" + request.getTitle(), 2);
        return Result.success("已取消接单", null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> completeRequest(Long id, Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        RepairRequest request = requestMapper.selectById(id);
        if (request == null) {
            return Result.error("检修需求不存在");
        }
        if (!userId.equals(request.getUserId())) {
            return Result.error("只有发布者可以确认完成");
        }
        if (request.getStatus() == null
                || (request.getStatus() != RepairRequest.STATUS_ACCEPTED
                && request.getStatus() != RepairRequest.STATUS_REPAIRING)) {
            return Result.error("当前状态不能确认完成");
        }
        if (request.getWorkerId() == null) {
            return Result.error("该需求尚未分配师傅");
        }

        request.setStatus(RepairRequest.STATUS_COMPLETED);
        requestMapper.updateById(request);
        createOrUpdateOrder(request, RepairOrder.STATUS_COMPLETED);
        notificationService.sendNotification(request.getWorkerId(), "检修需求已完成",
                "发布者已确认检修完成：" + request.getTitle(), 2);
        return Result.success("检修需求已完成", null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> reviewOrder(Long id, Long userId, Integer rating, String comment) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        RepairRequest request = requestMapper.selectById(id);
        if (request == null) {
            return Result.error("检修需求不存在");
        }
        if (!userId.equals(request.getUserId())) {
            return Result.error("只有发布者可以评价该检修订单");
        }
        if (!Integer.valueOf(RepairRequest.STATUS_COMPLETED).equals(request.getStatus())) {
            return Result.error("只有已完成检修订单可以评价");
        }
        if (rating == null || rating < 1 || rating > 5) {
            return Result.error("评分必须为1到5");
        }

        RepairOrder order = orderMapper.findByRequestId(id);
        if (order == null) {
            if (request.getWorkerId() == null) {
                return Result.error("该检修需求没有师傅");
            }
            createOrUpdateOrder(request, RepairOrder.STATUS_COMPLETED);
            order = orderMapper.findByRequestId(id);
        }
        if (order == null) {
            return Result.error("检修订单不存在");
        }

        order.setRating(rating);
        order.setComment(comment == null ? null : comment.trim());
        order.setStatus(RepairOrder.STATUS_REVIEWED);
        orderMapper.updateById(order);
        return Result.success("评价提交成功", null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateStatus(Long id, Integer status) {
        RepairRequest request = requestMapper.selectById(id);
        if (request == null) {
            return Result.error("检修需求不存在");
        }
        if (!isValidRequestStatus(status)) {
            return Result.error("检修状态不正确");
        }
        if (Integer.valueOf(RepairRequest.STATUS_CANCELLED).equals(status)) {
            if (Integer.valueOf(RepairRequest.STATUS_COMPLETED).equals(request.getStatus())) {
                return Result.error("已完成需求不能取消");
            }
            request.setStatus(RepairRequest.STATUS_CANCELLED);
            request.setWorkerId(null);
            requestMapper.updateById(request);
            deleteOrderByRequestId(id);
            return Result.success("状态更新成功", null);
        }
        if (Integer.valueOf(RepairRequest.STATUS_PENDING).equals(status)) {
            if (Integer.valueOf(RepairRequest.STATUS_COMPLETED).equals(request.getStatus())) {
                return Result.error("已完成需求不能回退为待接单");
            }
            request.setStatus(RepairRequest.STATUS_PENDING);
            request.setWorkerId(null);
            requestMapper.updateById(request);
            deleteOrderByRequestId(id);
            return Result.success("状态更新成功", null);
        }
        if ((status == RepairRequest.STATUS_ACCEPTED
                || status == RepairRequest.STATUS_REPAIRING
                || status == RepairRequest.STATUS_COMPLETED)
                && request.getWorkerId() == null) {
            return Result.error("请先分配师傅");
        }

        request.setStatus(status);
        requestMapper.updateById(request);
        if (status == RepairRequest.STATUS_ACCEPTED || status == RepairRequest.STATUS_REPAIRING) {
            createOrUpdateOrder(request, RepairOrder.STATUS_PROCESSING);
        } else if (status == RepairRequest.STATUS_COMPLETED) {
            createOrUpdateOrder(request, RepairOrder.STATUS_COMPLETED);
        }
        return Result.success("状态更新成功", null);
    }

    public Result<Void> updateRequest(RepairRequest request, Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
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
        Result<Void> validation = validateRepairRequest(request);
        if (validation.getCode() != 200) {
            return validation;
        }

        request.setUserId(userId);
        request.setWorkerId(existing.getWorkerId());
        request.setStatus(existing.getStatus());
        requestMapper.updateById(request);
        return Result.success("检修需求更新成功", null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteRequest(Long id, Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
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

    private void applyScope(
            LambdaQueryWrapper<RepairRequest> wrapper,
            Integer status,
            List<Integer> statuses,
            String scope,
            Long userId) {
        if ("pending".equals(scope)) {
            wrapper.eq(RepairRequest::getStatus, RepairRequest.STATUS_PENDING);
        } else if ("active".equals(scope)) {
            wrapper.in(RepairRequest::getStatus, RepairRequest.STATUS_ACCEPTED, RepairRequest.STATUS_REPAIRING);
            applyUserVisibleActiveScope(wrapper, userId);
        } else if ("completed".equals(scope)) {
            wrapper.eq(RepairRequest::getStatus, RepairRequest.STATUS_COMPLETED);
            applyUserVisibleActiveScope(wrapper, userId);
        } else if (statuses != null && !statuses.isEmpty()) {
            wrapper.in(RepairRequest::getStatus, statuses);
            if (!statuses.contains(RepairRequest.STATUS_PENDING)) {
                applyUserVisibleActiveScope(wrapper, userId);
            }
        } else if (status != null) {
            wrapper.eq(RepairRequest::getStatus, status);
            if (status != RepairRequest.STATUS_PENDING) {
                applyUserVisibleActiveScope(wrapper, userId);
            }
        } else if (userId != null) {
            wrapper.and(w -> w.eq(RepairRequest::getStatus, RepairRequest.STATUS_PENDING)
                    .or()
                    .eq(RepairRequest::getUserId, userId)
                    .or()
                    .eq(RepairRequest::getWorkerId, userId));
        } else {
            wrapper.eq(RepairRequest::getStatus, RepairRequest.STATUS_PENDING);
        }
    }

    private void applyUserVisibleActiveScope(LambdaQueryWrapper<RepairRequest> wrapper, Long userId) {
        if (userId != null) {
            wrapper.and(w -> w.eq(RepairRequest::getUserId, userId)
                    .or()
                    .eq(RepairRequest::getWorkerId, userId));
        }
    }

    private Result<Void> validateRepairRequest(RepairRequest request) {
        if (request == null) {
            return Result.error("检修信息不能为空");
        }
        if (!StringUtils.hasText(request.getTitle())) {
            return Result.error("检修标题不能为空");
        }
        if (!StringUtils.hasText(request.getLocation())) {
            return Result.error("检修地址不能为空");
        }
        return Result.success();
    }

    private Result<Void> validateWorker(RepairRequest request, Long workerId) {
        if (workerId == null) {
            return Result.error("请选择师傅");
        }
        if (request.getUserId() != null && request.getUserId().equals(workerId)) {
            return Result.error("发布者不能被分配为师傅");
        }
        SysUser worker = userMapper.selectById(workerId);
        if (worker == null || worker.getStatus() == null || worker.getStatus() != 1) {
            return Result.error("师傅不存在或已禁用");
        }
        if (worker.getRole() == null || worker.getRole() != 3) {
            return Result.error("只能分配维修师傅角色用户");
        }
        return Result.success();
    }

    private boolean hasActiveTask(Long workerId, Long excludeRequestId) {
        return requestMapper.countActiveByWorkerIdExcludeRequest(workerId, excludeRequestId) > 0;
    }

    private boolean isValidRequestStatus(Integer status) {
        return status != null
                && (status == RepairRequest.STATUS_PENDING
                || status == RepairRequest.STATUS_ACCEPTED
                || status == RepairRequest.STATUS_REPAIRING
                || status == RepairRequest.STATUS_COMPLETED
                || status == RepairRequest.STATUS_CANCELLED);
    }

    private void enrichRepairOrders(List<RepairRequest> requests) {
        for (RepairRequest request : requests) {
            enrichRepairOrder(request);
        }
    }

    private void enrichRepairOrder(RepairRequest request) {
        if (request == null || request.getId() == null) {
            return;
        }
        RepairOrder order = orderMapper.findByRequestId(request.getId());
        if (order == null) {
            return;
        }
        request.setOrderStatus(order.getStatus());
        request.setOrderRating(order.getRating());
        request.setOrderComment(order.getComment());
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
        order.setAmount(resolveRepairAmount(request));
        order.setStatus(orderStatus);
        if (order.getId() == null) {
            orderMapper.insert(order);
        } else {
            orderMapper.updateById(order);
        }
    }

    private BigDecimal resolveRepairAmount(RepairRequest request) {
        if (request.getActualPrice() != null) {
            return request.getActualPrice();
        }
        return request.getEstimatedPrice();
    }

    private void deleteOrderByRequestId(Long requestId) {
        orderMapper.delete(new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getRequestId, requestId));
    }

    private String generateOrderNo() {
        return "RO" + LocalDateTime.now().format(ORDER_TIME_FORMAT)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}

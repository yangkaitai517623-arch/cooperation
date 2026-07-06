package com.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.dto.Result;
import com.community.entity.ErrandRequest;
import com.community.entity.Notification;
import com.community.entity.SysUser;
import com.community.repository.ErrandRequestMapper;
import com.community.repository.NotificationMapper;
import com.community.repository.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 跑腿需求管理控制器
 * 
 * <p>负责社区跑腿需求的后台管理，提供需求的查询、分配接单人、状态更新和删除功能。
 * 管理员可以查看所有居民发布的跑腿需求（包括待接单、进行中和已完成的），
 * 支持按状态、类型、紧急程度和关键词进行多维度筛选。
 * 删除需求时会自动给发布者发送系统通知，告知删除原因。</p>
 * 
 * <p><b>基础路径：</b> /api/admin/errand</p>
 * <p><b>所需角色：</b> ADMIN（管理员）或以上</p>
 * <p><b>主要功能：</b>跑腿需求列表查询与筛选、需求详情查看、分配接单人、状态更新、删除需求</p>
 * 
 * @see AdminErrandOrderController
 */
@RestController
@RequestMapping("/api/admin/errand")
@RequiredArgsConstructor
public class AdminErrandController {

    /** 跑腿需求数据访问层，用于需求的增删改查 */
    private final ErrandRequestMapper errandRequestMapper;
    /** 系统用户数据访问层，预留用于关联查询用户信息 */
    private final SysUserMapper userMapper;
    /** 通知数据访问层，用于在删除需求时向发布者发送通知 */
    private final NotificationMapper notificationMapper;

    /**
     * 分页查询跑腿需求列表
     * 
     * <p><b>请求方式：</b> GET /api/admin/errand</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>支持多维度筛选：按状态（待接单/已接单/进行中等）、按跑腿类型（取快递/代买/代办等）、
     * 按紧急程度以及按标题/描述关键词模糊搜索。</p>
     * 
     * @param page      页码，默认第1页
     * @param size      每页大小，默认10条
     * @param status    需求状态，可选：null=全部, 0=待接单, 1=已接单, 2=进行中, 3=已完成, 4=已取消
     * @param keyword   搜索关键词，按标题和描述模糊匹配，可选
     * @param errandType 跑腿类型，可选：如"pickup"（取快递）、"shopping"（代买）等
     * @param urgency   紧急程度，可选：1=普通, 2=紧急, 3=非常紧急
     * @return 包含 records（需求列表）、total、page、size 的分页结果
     */
    @GetMapping
    public Result<Map<String, Object>> listRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String errandType,
            @RequestParam(required = false) Integer urgency) {

        // 构建查询条件
        LambdaQueryWrapper<ErrandRequest> wrapper = new LambdaQueryWrapper<>();
        // 按需求状态筛选
        if (status != null) {
            wrapper.eq(ErrandRequest::getStatus, status);
        }
        // 按标题和描述关键词模糊搜索（用 and 包裹 or，避免破坏外层条件优先级）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ErrandRequest::getTitle, keyword)
                    .or()
                    .like(ErrandRequest::getDescription, keyword));
        }
        // 按跑腿类型筛选
        if (StringUtils.hasText(errandType)) {
            wrapper.eq(ErrandRequest::getErrandType, errandType);
        }
        // 按紧急程度筛选
        if (urgency != null) {
            wrapper.eq(ErrandRequest::getUrgency, urgency);
        }
        // 按创建时间倒序排列
        wrapper.orderByDesc(ErrandRequest::getCreatedAt);

        // 执行分页查询
        Page<ErrandRequest> pageResult = errandRequestMapper.selectPage(new Page<>(page, size), wrapper);

        // 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("size", size);

        return Result.success(result);
    }

    /**
     * 查看跑腿需求详情
     * 
     * <p><b>请求方式：</b> GET /api/admin/errand/{id}</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>获取指定跑腿需求的完整信息，包括标题、描述、发布者、接单人、状态等。</p>
     * 
     * @param id 跑腿需求ID
     * @return 需求详情实体，不存在时返回错误提示
     */
    @GetMapping("/{id}")
    public Result<ErrandRequest> getRequest(@PathVariable Long id) {
        ErrandRequest request = errandRequestMapper.selectById(id);
        if (request == null) {
            return Result.error("跑腿需求不存在");
        }
        return Result.success(request);
    }

    /**
     * 分配/重新分配接单人
     * 
     * <p><b>请求方式：</b> PUT /api/admin/errand/{id}/assign</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>为跑腿需求指定接单人。仅在需求状态为"已接单"(1)或"进行中"(2)时才能重新分配，
     * 待接单状态下不适用此接口（由跑腿员自行接单）。</p>
     * 
     * @param id       跑腿需求ID
     * @param runnerId 新接单人（跑腿员）的用户ID
     * @return 操作结果，状态不满足分配条件时返回错误提示
     */
    @PutMapping("/{id}/assign")
    public Result<Void> assignRunner(@PathVariable Long id, @RequestParam Long runnerId) {
        ErrandRequest request = errandRequestMapper.selectById(id);
        if (request == null) {
            return Result.error("跑腿需求不存在");
        }
        // 只有在已接单状态(1)或进行中(2)才能重新分配
        if (request.getStatus() != 1 && request.getStatus() != 2) {
            return Result.error("只有在已接单或进行中状态才能重新分配接单人");
        }
        // 更新接单人ID
        request.setRunnerId(runnerId);
        errandRequestMapper.updateById(request);
        return Result.success("接单人已重新分配", null);
    }

    /**
     * 更新跑腿需求状态
     * 
     * <p><b>请求方式：</b> PUT /api/admin/errand/{id}/status</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>直接更新需求的业务状态（如：已完成、已取消等）。</p>
     * 
     * @param id     跑腿需求ID
     * @param status 新的需求状态：0=待接单, 1=已接单, 2=进行中, 3=已完成, 4=已取消
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        ErrandRequest request = errandRequestMapper.selectById(id);
        if (request == null) {
            return Result.error("跑腿需求不存在");
        }
        request.setStatus(status);
        errandRequestMapper.updateById(request);
        return Result.success("状态更新成功", null);
    }

    /**
     * 删除跑腿需求
     * 
     * <p><b>请求方式：</b> DELETE /api/admin/errand/{id}</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>物理删除指定跑腿需求。删除前会向需求发布者发送一条系统通知，
     * 告知其需求已被管理员删除及具体原因，避免用户不知情。</p>
     * 
     * @param id     要删除的跑腿需求ID
     * @param reason 删除原因，可选，会在通知中告知用户
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRequest(@PathVariable Long id, @RequestParam(required = false) String reason) {
        ErrandRequest request = errandRequestMapper.selectById(id);
        if (request == null) {
            return Result.error("需求不存在");
        }
        // 发送通知给发布者，告知需求被删除及原因
        if (request.getUserId() != null) {
            Notification notification = new Notification();
            notification.setUserId(request.getUserId());
            notification.setTitle("需求被删除");
            String content = "您发布的跑腿需求「" + request.getTitle() + "」已被管理员删除";
            if (reason != null && !reason.isEmpty()) {
                content += "。原因：" + reason;
            }
            notification.setContent(content);
            // 通知类型为系统通知
            notification.setType(1);
            notification.setIsRead(0);
            notificationMapper.insert(notification);
        }
        errandRequestMapper.deleteById(id);
        return Result.success("删除成功", null);
    }
}

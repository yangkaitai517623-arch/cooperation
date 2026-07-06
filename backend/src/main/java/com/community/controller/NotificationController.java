package com.community.controller;

import com.community.dto.PageResult;
import com.community.dto.Result;
import com.community.entity.Notification;
import com.community.entity.SysUser;
import com.community.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器 - 处理系统消息通知的查询、标记已读等功能
 * <p>
 * 映射路径：{@code /api/notifications}<br>
 * 所属模块：消息通知模块<br>
 * 开放范围：已登录的普通用户（需要有效的JWT令牌）
 * </p>
 * <p>
 * 通知类型说明：
 * <ul>
 *   <li>1 - 系统通知：平台发布的系统级消息</li>
 *   <li>2 - 订单通知：订单状态变更通知</li>
 *   <li>3 - 互动通知：评论、点赞等互动消息</li>
 * </ul>
 * </p>
 *
 * @author community-platform
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    /** 通知服务，处理通知的查询、标记已读等业务逻辑 */
    private final NotificationService notificationService;

    /**
     * 分页获取当前用户的通知列表
     * <p>
     * GET /api/notifications<br>
     * 查询当前登录用户的所有通知，按时间倒序分页返回。<br>
     * 包括系统通知、订单通知、互动通知等各类消息。
     * </p>
     *
     * @param page 页码，从1开始，默认值为1
     * @param size 每页条数，默认值为10
     * @return 成功时返回包含通知列表的分页结果；失败时返回错误信息
     * @see NotificationService#listNotifications(Long, int, int)
     */
    @GetMapping
    public Result<PageResult<Notification>> listNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long userId = getCurrentUserId();
            PageResult<Notification> pageResult = notificationService.listNotifications(userId, page, size);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取未读通知数量
     * <p>
     * GET /api/notifications/unread-count<br>
     * 查询当前用户所有未读通知的数量，<br>
     * 通常用于前端在导航栏或消息图标上显示未读红点数字。
     * </p>
     *
     * @return 成功时返回未读通知的数量；失败时返回错误信息
     * @see NotificationService#countUnread(Long)
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        try {
            Long userId = getCurrentUserId();
            int count = notificationService.countUnread(userId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 标记单条通知为已读
     * <p>
     * PUT /api/notifications/{id}/read<br>
     * 将指定ID的通知标记为已读状态。<br>
     * 用户点击通知后调用此接口。
     * </p>
     *
     * @param id 通知ID（路径参数）
     * @return 成功时返回null；失败时返回错误信息
     * @see NotificationService#markAsRead(Long)
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        try {
            return notificationService.markAsRead(id);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 一键标记所有通知为已读
     * <p>
     * PUT /api/notifications/read-all<br>
     * 将当前用户的所有未读通知批量标记为已读，<br>
     * 通常由前端的"全部已读"按钮触发。
     * </p>
     *
     * @return 成功时返回null；失败时返回错误信息
     * @see NotificationService#markAllAsRead(Long)
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        try {
            Long userId = getCurrentUserId();
            return notificationService.markAllAsRead(userId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从Spring Security上下文中获取当前登录用户的ID
     * <p>
     * 如果用户未登录或认证信息无效，抛出RuntimeException。<br>
     * 此方法严格要求用户已登录，未登录时不允许访问任何通知接口。
     * </p>
     *
     * @return 当前登录用户的ID
     * @throws RuntimeException 用户未登录或认证信息无效时抛出
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SysUser) {
            SysUser user = (SysUser) authentication.getPrincipal();
            return user.getId();
        }
        throw new RuntimeException("用户未登录或认证信息无效");
    }
}

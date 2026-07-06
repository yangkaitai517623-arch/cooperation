package com.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.dto.PageResult;
import com.community.dto.Result;
import com.community.entity.Notification;
import com.community.repository.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 消息通知服务类，负责社区平台中系统通知的发送、查询和已读管理。
 *
 * <p>该服务是社区平台的通用基础设施组件，为各个业务模块提供统一的消息通知能力。
 * 当社区中发生关键事件时（如跑腿接单、检修分配、订单状态变更等），相关系统会
 * 通过此服务向目标用户发送通知。</p>
 *
 * <h3>通知类型定义</h3>
 * <ul>
 *   <li><b>1</b> - 系统通知：由系统自动发送的通用通知</li>
 *   <li><b>2</b> - 业务通知：与具体业务相关的通知（如订单确认、需求接单等）</li>
 *   <li><b>3</b> - 互动通知：社区互动相关（如评论回复、点赞等）</li>
 * </ul>
 *
 * <h3>已读状态</h3>
 * <ul>
 *   <li><b>0</b> - 未读</li>
 *   <li><b>1</b> - 已读</li>
 * </ul>
 *
 * <h3>关键设计决策</h3>
 * <ul>
 *   <li>通知列表按创建时间降序排列，最新通知优先展示</li>
 *   <li>支持单条标记已读和批量全部已读两种操作模式</li>
 *   <li>批量标记已读使用 {@link LambdaUpdateWrapper} 进行条件更新，避免先查询再逐条更新的N+1问题</li>
 *   <li>未读数量通过 Mapper 层自定义 {@code countUnread} 方法直接在数据库层面统计，避免加载全量数据</li>
 * </ul>
 *
 * @author qingqing
 * @see Notification 通知消息实体
 * @see NotificationMapper 通知消息数据访问层
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    /**
     * 通知消息数据访问对象，用于执行通知的增删改查操作。
     */
    private final NotificationMapper notificationMapper;

    /**
     * 分页查询指定用户的通知列表，按创建时间降序排列。
     *
     * <p>该方法根据用户ID精确筛选通知，返回包含未读和已读状态的全部通知。
     * 最新收到的通知排在最前面，方便用户查看。</p>
     *
     * @param userId 目标用户ID
     * @param page   页码，从1开始
     * @param size   每页条数
     * @return 包含通知实体列表和分页信息的分页结果
     */
    public PageResult<Notification> listNotifications(Long userId, int page, int size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt);

        IPage<Notification> pageResult = notificationMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    /**
     * 将单条通知标记为已读。
     *
     * <p>通过主键ID定位通知，将其 {@code isRead} 字段设置为 <b>1（已读）</b>。
     * 如果通知不存在，返回错误提示。</p>
     *
     * @param id 通知主键ID
     * @return 操作结果
     */
    public Result<Void> markAsRead(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            return Result.error("通知不存在");
        }
        notification.setIsRead(1);
        notificationMapper.updateById(notification);
        return Result.success("标记已读", null);
    }

    /**
     * 将指定用户的所有未读通知批量标记为已读（一键全部已读）。
     *
     * <p>该方法使用 {@link LambdaUpdateWrapper} 构造条件更新SQL，
     * 直接在数据库层面将满足条件（userId匹配 且 isRead=0）的所有记录
     * 的 {@code isRead} 字段更新为1。</p>
     *
     * <p><b>性能优势：</b>相比先查询出所有未读通知再逐条更新，此方式仅需一次
     * 数据库操作即可完成，避免了O(N)的网络往返开销。</p>
     *
     * @param userId 目标用户ID
     * @return 操作结果
     */
    public Result<Void> markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1);
        notificationMapper.update(null, wrapper);
        return Result.success("全部已读", null);
    }

    /**
     * 统计指定用户的未读通知数量。
     *
     * <p>该方法调用 Mapper 层自定义的 {@code countUnread} 方法，
     * 在数据库层面直接统计，不加载通知实体数据，性能高效。
     * 常用于前端导航栏的"未读红色角标"显示。</p>
     *
     * @param userId 目标用户ID
     * @return 未读通知的数量
     */
    public int countUnread(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    /**
     * 向指定用户发送一条系统通知。
     *
     * <p>创建通知时自动设置以下默认值：</p>
     * <ul>
     *   <li>已读状态：<b>0（未读）</b></li>
     * </ul>
     *
     * <p>发送后通知立即生效，用户可在通知列表中查看。该方法常被其他业务服务
     * 调用，用于在业务事件发生时自动推送通知（例如：订单确认后通知买家）。</p>
     *
     * @param userId  接收通知的用户ID
     * @param title   通知标题，简明扼要地概述通知内容
     * @param content 通知正文，详细描述通知的具体信息
     * @param type    通知类型（1=系统通知, 2=业务通知, 3=互动通知）
     * @return 操作结果
     */
    public Result<Void> sendNotification(Long userId, String title, String content, Integer type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
        return Result.success("通知发送成功", null);
    }
}

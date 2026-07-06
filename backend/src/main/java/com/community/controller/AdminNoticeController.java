package com.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.dto.Result;
import com.community.entity.Notification;
import com.community.entity.SysUser;
import com.community.repository.NotificationMapper;
import com.community.repository.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统通知管理控制器
 * 
 * <p>负责平台系统通知的创建、查询和删除管理。
 * 管理员可以向指定用户发送通知，也可以进行全员广播（不指定接收者时自动发给所有活跃用户）。
 * 全员广播时会排除超级管理员账号，只为普通用户创建通知。
 * 支持按通知类型筛选和按标题/内容关键词搜索。</p>
 * 
 * <p><b>基础路径：</b> /api/admin/notices</p>
 * <p><b>所需角色：</b> ADMIN（管理员）或以上</p>
 * <p><b>主要功能：</b>通知列表分页查询与筛选、创建通知（支持全员广播和定向发送）、删除通知</p>
 */
@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    /** 通知数据访问层，用于通知的增删改查 */
    private final NotificationMapper notificationMapper;
    /** 系统用户数据访问层，用于全员广播时获取所有活跃用户列表 */
    private final SysUserMapper userMapper;

    /**
     * 分页查询通知列表
     * 
     * <p><b>请求方式：</b> GET /api/admin/notices</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>支持按通知类型筛选和按标题/内容关键词模糊搜索。
     * 结果按创建时间倒序排列，最新发布的通知优先展示。</p>
     * 
     * @param page    页码，默认第1页
     * @param size    每页大小，默认10条
     * @param keyword 搜索关键词，按标题和内容模糊匹配，可选
     * @param type    通知类型，可选：1=系统通知, 2=业务通知等（具体取值以业务定义为准）
     * @return 包含 records（通知列表）、total、page、size 的分页结果
     */
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type) {

        // 构建查询条件
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        // 按标题和内容关键词模糊搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Notification::getTitle, keyword)
                   .or()
               .like(Notification::getContent, keyword);
        }
        // 按通知类型筛选
        if (type != null) {
            wrapper.eq(Notification::getType, type);
        }
        // 按创建时间倒序排列
        wrapper.orderByDesc(Notification::getCreatedAt);

        // 执行分页查询
        Page<Notification> pageResult = notificationMapper.selectPage(new Page<>(page, size), wrapper);

        // 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("size", size);

        return Result.success(result);
    }

    /**
     * 创建/发送通知
     * 
     * <p><b>请求方式：</b> POST /api/admin/notices</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>创建新通知。根据是否指定接收者有两种行为模式：
     * <ul>
     *   <li><b>定向发送：</b>指定了接收者（userId 不为空），直接为该用户创建一条通知</li>
     *   <li><b>全员广播：</b>未指定接收者（userId 为 null），系统自动查询所有活跃普通用户
     *       （status=1 且非超级管理员），为每个用户分别创建一条通知</li>
     * </ul>
     * 新创建的通知默认标记为未读（isRead=0）。</p>
     * 
     * @param notification 通知实体，需包含标题、内容和类型，userId可选（不填则全员广播）
     * @return 操作结果，全员广播时提示"通知已发送给所有用户"
     */
    @PostMapping
    public Result<Void> create(@RequestBody Notification notification) {
        // 默认标记为未读
        notification.setIsRead(0);

        // 如果接收者为空，发送给所有用户（全员广播）
        if (notification.getUserId() == null) {
            // 查询所有活跃的普通用户（排除超级管理员）
            List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getStatus, 1)
                    .ne(SysUser::getRole, 2)); // 排除超级管理员
            // 为每个用户创建独立的通知记录
            for (SysUser user : users) {
                Notification notif = new Notification();
                notif.setUserId(user.getId());
                notif.setTitle(notification.getTitle());
                notif.setContent(notification.getContent());
                notif.setType(notification.getType());
                notif.setIsRead(0);
                notificationMapper.insert(notif);
            }
            return Result.success("通知已发送给所有用户", null);
        } else {
            // 定向发送：直接插入指定用户的单条通知
            notificationMapper.insert(notification);
            return Result.success("通知发送成功", null);
        }
    }

    /**
     * 删除通知
     * 
     * <p><b>请求方式：</b> DELETE /api/admin/notices/{id}</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>物理删除指定通知记录。注意：如果该通知是通过全员广播创建的，
     * 此操作只会删除选中的那一条，不会删除其他用户的相同通知。</p>
     * 
     * @param id 需要删除的通知ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        notificationMapper.deleteById(id);
        return Result.success("删除成功", null);
    }
}

package com.community.controller;

import com.community.dto.Result;
import com.community.entity.SysUser;
import com.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器 - 处理当前登录用户的个人信息管理
 * <p>
 * 映射路径：{@code /api/user}<br>
 * 所属模块：用户管理模块<br>
 * 开放范围：已登录的普通用户（需要有效的JWT令牌）
 * </p>
 *
 * @author community-platform
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    /** 用户服务，处理用户信息查询和更新业务逻辑 */
    private final UserService userService;

    /**
     * 获取当前登录用户的个人信息
     * <p>
     * GET /api/user/profile<br>
     * 从安全上下文中获取当前登录用户ID，查询并返回完整的用户信息，<br>
     * 包括用户名、真实姓名、联系方式、楼栋房间等。
     * </p>
     *
     * @return 成功时返回当前用户的完整信息实体；用户不存在时返回错误
     * @see UserService#getUserById(Long)
     */
    @GetMapping("/profile")
    public Result<SysUser> getProfile() {
        try {
            // 从安全上下文中获取当前登录用户ID
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.unauthorized();
            }
            // 根据ID查询用户信息
            SysUser user = userService.getUserById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新当前登录用户的个人信息
     * <p>
     * PUT /api/user/profile<br>
     * 允许用户修改个人资料，如真实姓名、手机号、邮箱、楼栋房间等。<br>
     * 用户名不可通过此接口修改。
     * </p>
     *
     * @param user 包含更新字段的用户实体对象（ID会被自动设置为当前用户ID）
     * @return 更新成功返回null；失败返回错误信息
     * @see UserService#updateUser(SysUser)
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody SysUser user) {
        try {
            // 确保只能修改当前用户自己的信息
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.unauthorized();
            }
            user.setId(userId);
            return userService.updateUser(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改当前登录用户的密码
     * <p>
     * PUT /api/user/password<br>
     * 用户需要提供旧密码进行验证，新密码会经过加密后存储。<br>
     * 旧密码验证失败时返回错误。
     * </p>
     *
     * @param params 包含oldPassword（旧密码）和newPassword（新密码）的Map
     * @return 修改成功返回null；旧密码错误或其他异常时返回错误信息
     * @see UserService#changePassword(Long, String, String)
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params) {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.unauthorized();
            }
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            return userService.changePassword(userId, oldPassword, newPassword);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从Spring Security上下文中获取当前登录用户的ID
     * <p>
     * 通过SecurityContextHolder获取当前认证信息，<br>
     * 从Principal中提取SysUser对象并返回其ID。<br>
     * 如果用户未登录或认证信息无效，返回null。
     * </p>
     *
     * @return 当前登录用户的ID，未登录时返回null
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SysUser) {
            SysUser user = (SysUser) authentication.getPrincipal();
            return user.getId();
        }
        return null;
    }
}

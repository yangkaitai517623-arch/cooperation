package com.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.dto.Result;
import com.community.entity.SysUser;
import com.community.repository.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 普通用户管理控制器
 * 
 * <p>负责平台的普通居民用户管理，与 {@link AdminAdminController} 分工明确：
 * 本控制器管理普通用户（role=0），AdminAdminController 管理管理员和超级管理员。
 * 提供用户的查询、信息编辑、状态管理和角色变更功能。</p>
 * 
 * <p>角色定义：</p>
 * <ul>
 *   <li>0 = 普通用户（居民）</li>
 *   <li>1 = 管理员</li>
 *   <li>2 = 超级管理员</li>
 * </ul>
 * 
 * <p><b>基础路径：</b> /api/admin/users</p>
 * <p><b>所需角色：</b> ADMIN（管理员）或以上</p>
 * <p><b>主要功能：</b>用户列表分页查询与筛选、用户详情查看、编辑用户信息、启用/禁用用户、角色变更</p>
 * 
 * @see AdminAdminController
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    /** 系统用户数据访问层，用于普通用户的管理操作 */
    private final SysUserMapper userMapper;

    /**
     * 分页查询用户列表
     * 
     * <p><b>请求方式：</b> GET /api/admin/users</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>默认只返回普通用户（role=0），但可通过 role 参数切换查询范围。
     * 支持按状态筛选和按姓名/用户名/手机号关键词模糊搜索。
     * 结果按创建时间倒序排列，最新注册的用户优先展示。</p>
     * 
     * @param page    页码，默认第1页
     * @param size    每页大小，默认10条
     * @param keyword 搜索关键词，按姓名/用户名/手机号模糊匹配，可选
     * @param status  账号状态，可选：1=启用, 0=禁用
     * @param role    用户角色，可选：0=普通用户（默认）, 1=管理员, 2=超级管理员
     * @return 包含 records（用户列表）、total、page、size 的分页结果
     */
    @GetMapping
    public Result<Map<String, Object>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer role) {

        // 构建查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        // 默认只显示普通用户（role=0），可切换
        if (role != null) {
            wrapper.eq(SysUser::getRole, role);
        } else {
            // 默认显示普通用户
            wrapper.eq(SysUser::getRole, 0); // 默认显示普通用户
        }

        // 按账号状态筛选
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        // 按姓名/用户名/手机号关键词模糊搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysUser::getRealName, keyword)
                   .or()
               .like(SysUser::getUsername, keyword)
                   .or()
               .like(SysUser::getPhone, keyword);
        }
        // 按创建时间倒序排列
        wrapper.orderByDesc(SysUser::getCreatedAt);

        // 执行分页查询
        Page<SysUser> pageResult = userMapper.selectPage(new Page<>(page, size), wrapper);

        // 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("size", size);

        return Result.success(result);
    }

    /**
     * 查看用户详情
     * 
     * <p><b>请求方式：</b> GET /api/admin/users/{id}</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>获取指定用户的完整信息，包括用户名、真实姓名、手机号、角色、状态等。</p>
     * 
     * @param id 用户ID
     * @return 用户详情实体，不存在时返回错误提示
     */
    @GetMapping("/{id}")
    public Result<SysUser> getUser(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 编辑用户基本信息
     * 
     * <p><b>请求方式：</b> PUT /api/admin/users/{id}</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>更新指定用户的基本信息（如姓名、手机号等）。
     * 注意：该接口不支持修改密码，传入的密码字段会被忽略。
     * 如需修改密码请使用单独的重置密码功能。</p>
     * 
     * @param id   用户ID
     * @param user 需要更新的用户信息（密码字段将被忽略）
     * @return 操作结果，用户不存在时返回错误提示
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        SysUser existing = userMapper.selectById(id);
        if (existing == null) {
            return Result.error("用户不存在");
        }
        user.setId(id);
        // 不允许通过此接口修改密码，设为null避免误更新
        user.setPassword(null); // 不允许通过此接口修改密码
        userMapper.updateById(user);
        return Result.success("更新成功", null);
    }

    /**
     * 更新用户账号状态（启用/禁用）
     * 
     * <p><b>请求方式：</b> PUT /api/admin/users/{id}/status</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>启用或禁用指定用户账号。禁用后该用户将无法登录和使用平台功能。
     * 管理员应谨慎操作，建议先了解原因再禁用。</p>
     * 
     * @param id     用户ID
     * @param status 新的账号状态：1=启用, 0=禁用
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success("状态更新成功", null);
    }

    /**
     * 更新用户角色
     * 
     * <p><b>请求方式：</b> PUT /api/admin/users/{id}/role</p>
     * <p><b>所需角色：</b> SUPER_ADMIN（超级管理员）</p>
     * <p>变更用户的角色等级。角色取值：0=普通用户, 1=管理员, 2=超级管理员。
     * 注意：此操作涉及权限变更，通常只有超级管理员才有权限执行。
     * 前端应对此接口做角色校验。</p>
     * 
     * @param id   用户ID
     * @param role 新的角色等级：0=普通用户, 1=管理员, 2=超级管理员
     * @return 操作结果
     */
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @RequestParam Integer role) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setRole(role);
        userMapper.updateById(user);
        return Result.success("角色更新成功", null);
    }
}

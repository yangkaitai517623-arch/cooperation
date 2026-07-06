package com.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.dto.Result;
import com.community.entity.SysUser;
import com.community.repository.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员账号管理控制器
 * 
 * <p>负责平台管理员和超级管理员账号的增删改查操作。
 * 只管理角色为管理员（role=1）和超级管理员（role=2）的用户，
 * 不包含普通用户（role=0）的管理，普通用户管理请参见 {@link AdminUserController}。</p>
 * 
 * <p><b>基础路径：</b> /api/admin/admins</p>
 * <p><b>所需角色：</b> SUPER_ADMIN（超级管理员）</p>
 * <p><b>主要功能：</b>管理员列表查询、添加管理员、编辑管理员信息、删除管理员</p>
 * 
 * @see AdminUserController
 */
@RestController
@RequestMapping("/api/admin/admins")
@RequiredArgsConstructor
public class AdminAdminController {

    /** 系统用户数据访问层，用于管理员账号的增删改查 */
    private final SysUserMapper userMapper;
    /** 密码编码器，用于对新添加管理员的密码进行加密存储 */
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询管理员列表
     * 
     * <p><b>请求方式：</b> GET /api/admin/admins</p>
     * <p><b>所需角色：</b> SUPER_ADMIN</p>
     * <p>默认只返回管理员（role=1）和超级管理员（role=2），不包含普通用户。
     * 支持按姓名、用户名、手机号进行关键词模糊搜索。</p>
     * 
     * @param page    页码，默认第1页
     * @param size    每页大小，默认10条
     * @param keyword 搜索关键词，可选，支持姓名/用户名/手机号模糊匹配
     * @return 包含 records（管理员列表）、total（总记录数）、page（当前页）、size（每页大小）的分页结果
     */
    @GetMapping
    public Result<Map<String, Object>> listAdmins(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        // 构建查询条件：只查询管理员和超级管理员
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        // 只显示管理员（role=1）和超级管理员（role=2）
        wrapper.in(SysUser::getRole, 1, 2);

        // 关键词搜索：匹配姓名、用户名或手机号
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
     * 添加新管理员
     * 
     * <p><b>请求方式：</b> POST /api/admin/admins</p>
     * <p><b>所需角色：</b> SUPER_ADMIN</p>
     * <p>创建新的管理员账号，默认角色为管理员（role=1）。
     * 系统会自动对密码进行加密处理，并设置账号状态为启用（status=1）。</p>
     * <p>注意：添加的始终是普通管理员，如需创建超级管理员请直接操作数据库。</p>
     * 
     * @param user 管理员用户信息，需包含用户名、密码、真实姓名等字段
     * @return 操作结果，用户名重复时返回错误提示
     */
    @PostMapping
    public Result<Void> addAdmin(@RequestBody SysUser user) {
        // 检查用户名是否已存在
        SysUser existing = userMapper.findByUsername(user.getUsername());
        if (existing != null) {
            return Result.error("用户名已存在");
        }
        // 密码加密存储
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 设置角色为管理员
        user.setRole(1); // 管理员
        // 设置状态为启用
        user.setStatus(1);
        userMapper.insert(user);
        return Result.success("管理员添加成功", null);
    }

    /**
     * 编辑管理员信息
     * 
     * <p><b>请求方式：</b> PUT /api/admin/admins/{id}</p>
     * <p><b>所需角色：</b> SUPER_ADMIN</p>
     * <p>更新指定管理员的基本信息。注意：该接口不支持修改密码，
     * 传入的密码字段会被忽略。如需修改密码请使用单独的重置密码功能。</p>
     * 
     * @param id   管理员用户ID
     * @param user 需要更新的管理员信息（密码字段将被忽略）
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<Void> updateAdmin(@PathVariable Long id, @RequestBody SysUser user) {
        SysUser existing = userMapper.selectById(id);
        if (existing == null) {
            return Result.error("管理员不存在");
        }
        user.setId(id);
        // 不允许通过此接口修改密码，设为null避免误更新
        user.setPassword(null); // 不允许通过此接口修改密码
        userMapper.updateById(user);
        return Result.success("更新成功", null);
    }

    /**
     * 删除管理员
     * 
     * <p><b>请求方式：</b> DELETE /api/admin/admins/{id}</p>
     * <p><b>所需角色：</b> SUPER_ADMIN</p>
     * <p>删除指定管理员账号。系统会阻止删除超级管理员（role=2），
     * 确保至少有一个超级管理员账号存在。</p>
     * 
     * @param id 要删除的管理员用户ID
     * @return 操作结果，尝试删除超级管理员时返回错误提示
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("管理员不存在");
        }
        // 禁止删除超级管理员，防止系统没有超级管理员
        if (user.getRole() == 2) {
            return Result.error("不能删除超级管理员");
        }
        userMapper.deleteById(id);
        return Result.success("删除成功", null);
    }
}

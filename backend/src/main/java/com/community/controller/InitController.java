package com.community.controller;

import com.community.dto.Result;
import com.community.entity.SysUser;
import com.community.repository.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 初始化控制器 - 提供系统维护和密码重置等初始化功能
 * <p>
 * 映射路径：{@code /api/init}<br>
 * 所属模块：系统管理模块<br>
 * 开放范围：无权限限制（建议仅在开发环境使用，生产环境应删除或添加严格的安全校验）
 * </p>
 * <p>
 * <b>安全警告：</b>此控制器中的接口用于开发调试和系统初始化，<br>
 * 将密码统一重置为固定值存在安全风险。<br>
 * 部署到生产环境前应删除此控制器，或添加管理员权限校验。
 * </p>
 *
 * @author community-platform
 */
@RestController
@RequestMapping("/api/init")
@RequiredArgsConstructor
public class InitController {

    /** 用户数据访问层，用于查询和更新用户信息 */
    private final SysUserMapper userMapper;

    /** 密码加密器，用于对密码进行BCrypt加密存储 */
    private final PasswordEncoder passwordEncoder;

    /**
     * 重置所有用户密码为固定值 admin123
     * <p>
     * POST /api/init/reset-passwords<br>
     * 遍历系统中所有用户（包括管理员admin账号），<br>
     * 将密码统一使用BCrypt加密后重置为"admin123"。<br>
     * 此接口主要用于开发阶段密码遗忘时的快速恢复。
     * </p>
     * <p>
     * <b>注意：</b>生产环境中绝对不可保留此接口！
     * </p>
     *
     * @return 成功时返回提示消息"密码已重置为: admin123"
     */
    @PostMapping("/reset-passwords")
    public Result resetPasswords() {
        // 对固定密码进行BCrypt加密
        String encodedPassword = passwordEncoder.encode("admin123");

        // 遍历所有普通用户，逐一更新密码
        userMapper.findAllUsers().forEach(user -> {
            SysUser update = new SysUser();
            update.setId(user.getId());
            update.setPassword(encodedPassword);
            userMapper.updateById(update);
        });

        // 单独处理管理员账号密码重置
        SysUser admin = userMapper.findByUsername("admin");
        if (admin != null) {
            SysUser update = new SysUser();
            update.setId(admin.getId());
            update.setPassword(encodedPassword);
            userMapper.updateById(update);
        }

        return Result.success("密码已重置为: admin123");
    }
}

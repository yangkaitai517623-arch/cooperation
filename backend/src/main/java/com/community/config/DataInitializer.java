package com.community.config;

import com.community.entity.SysUser;
import com.community.repository.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 应用启动时的数据初始化器
 *
 * <p>实现 {@link CommandLineRunner} 接口，在 Spring Boot 应用启动完成后自动执行。
 * 主要职责：检查所有用户的密码状态，为密码为空（null 或空字符串）的用户设置默认密码。</p>
 *
 * <p>设计考量：</p>
 * <ul>
 *   <li>不强制重置已有密码 — 仅处理密码缺失的情况，不会覆盖用户已设置的合法密码</li>
 *   <li>使用 BCrypt 加密存储 — 密码不直接存储明文，通过 {@link PasswordEncoder} 加密</li>
 *   <li>适用于数据库初始化后的首次启动，或者后续添加新用户的场景</li>
 * </ul>
 *
 * <p>默认密码：admin123（建议运维人员在部署后及时修改）</p>
 *
 * @see CommandLineRunner Spring Boot 启动后执行的回调接口
 * @see PasswordEncoder   Spring Security 密码编码器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    /** 数据库用户表数据访问接口 */
    private final SysUserMapper userMapper;

    /** Spring Security 密码加密器（BCrypt） */
    private final PasswordEncoder passwordEncoder;

    /**
     * 应用启动后自动执行用户密码检查与初始化
     *
     * <p>遍历数据库中的所有用户：</p>
     * <ol>
     *   <li>读取全部用户记录</li>
     *   <li>过滤出密码为 null 或空字符串的用户</li>
     *   <li>为这些用户生成 BCrypt 加密后的默认密码 "admin123" 并更新数据库</li>
     * </ol>
     *
     * @param args 命令行参数（本方法中未使用）
     */
    @Override
    public void run(String... args) {
        log.info("正在检查用户密码...");

        // 默认密码明文（生产环境建议通过外部配置文件注入）
        String defaultPassword = "admin123";
        // 预先计算一次 BCrypt 哈希，避免在循环中重复加密
        String defaultHash = passwordEncoder.encode(defaultPassword);

        // 遍历所有用户，仅对无密码用户初始化密码
        userMapper.selectList(null).forEach(user -> {
            // 判断条件：密码为 null 或为空字符串才处理
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                // 构造更新对象，仅设置 ID 和密码字段，MyBatis Plus 按 ID 进行局部更新
                SysUser update = new SysUser();
                update.setId(user.getId());
                update.setPassword(defaultHash);
                userMapper.updateById(update);
                log.info("已为用户 {} 设置默认密码", user.getUsername());
            }
        });

        log.info("用户密码检查完成");
    }
}

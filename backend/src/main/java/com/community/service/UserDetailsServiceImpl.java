package com.community.service;

import com.community.entity.SysUser;
import com.community.repository.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security 用户详情加载服务实现类，负责从数据库加载用户认证和授权信息。
 *
 * <p>该服务实现了 Spring Security 框架的 {@link UserDetailsService} 接口，
 * 是系统的认证入口点。当用户尝试登录时，Spring Security 框架会自动调用
 * {@link #loadUserByUsername(String)} 方法查找用户并返回认证信息。</p>
 *
 * <h3>认证流程</h3>
 * <ol>
 *   <li>根据用户名从数据库查询用户记录</li>
 *   <li>验证用户是否存在</li>
 *   <li>检查用户状态是否被禁用</li>
 *   <li>根据角色映射 Spring Security 权限</li>
 *   <li>返回 {@link UserDetails} 对象供框架进行密码比对和权限校验</li>
 * </ol>
 *
 * <h3>用户角色与权限映射</h3>
 * <table border="1">
 *   <tr><th>数据库 role 值</th><th>角色名称</th><th>Spring Security 权限</th></tr>
 *   <tr><td>0</td><td>普通用户</td><td>{@code ROLE_USER}</td></tr>
 *   <tr><td>1</td><td>管理员</td><td>{@code ROLE_ADMIN}</td></tr>
 *   <tr><td>2</td><td>超级管理员</td><td>{@code ROLE_SUPER_ADMIN}</td></tr>
 * </table>
 *
 * <h3>用户状态定义</h3>
 * <ul>
 *   <li><b>0</b> - 禁用（无法登录）</li>
 *   <li><b>1</b> - 启用（正常使用）</li>
 * </ul>
 *
 * <h3>关键设计决策</h3>
 * <ul>
 *   <li>采用 {@code role} 整数字段区分角色，而非多表关联，简化了权限数据结构</li>
 *   <li>使用 Java 14+ 的 {@code switch} 表达式进行角色到权限的映射，代码简洁且类型安全</li>
 *   <li>被禁用的用户（status=0）直接抛出异常，阻止登录，不返回 UserDetails 对象</li>
 *   <li>密码比对由 Spring Security 的 {@code DaoAuthenticationProvider} 自动完成，无需在此手动处理</li>
 * </ul>
 *
 * @author qingqing
 * @see UserDetailsService Spring Security 用户详情服务接口
 * @see SysUserMapper 系统用户数据访问层
 * @see SysUser 系统用户实体
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * 系统用户数据访问对象，用于根据用户名查询用户记录。
     */
    private final SysUserMapper userMapper;

    /**
     * 根据用户名加载用户认证和授权信息。
     *
     * <p>这是 Spring Security 认证链路的入口方法，框架在用户登录时自动调用。
     * 方法执行以下步骤：</p>
     *
     * <ol>
     *   <li><b>查找用户：</b>通过 {@code userMapper.findByUsername(username)}
     *       从数据库查询用户记录</li>
     *   <li><b>存在性校验：</b>如果未找到用户，抛出 {@link UsernameNotFoundException}</li>
     *   <li><b>状态校验：</b>如果用户状态为0（禁用），抛出异常阻止登录</li>
     *   <li><b>角色映射：</b>将数据库中的role整数值转换为Spring Security权限字符串</li>
     *   <li><b>构建认证对象：</b>创建 {@link User} 对象，包含用户名、加密密码和权限列表</li>
     * </ol>
     *
     * <p><b>注意：</b>返回的 {@link UserDetails} 对象中的密码应为数据库中存储的
     * 加密密码，Spring Security 会自动使用配置的 {@code PasswordEncoder} 与用户输入的
     * 明文密码进行比对。</p>
     *
     * @param username 用户登录时输入的用户名
     * @return Spring Security 用户详情对象，包含用户名、加密密码和权限信息
     * @throws UsernameNotFoundException 如果用户不存在或已被禁用
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        String authority = switch (user.getRole()) {
            case 2 -> "ROLE_SUPER_ADMIN";
            case 1 -> "ROLE_ADMIN";
            default -> "ROLE_USER";
        };

        return new User(
            user.getUsername(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(authority))
        );
    }
}

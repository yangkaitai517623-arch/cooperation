package com.community.config;

import com.community.entity.SysUser;
import com.community.repository.SysUserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器 — 每次请求的鉴权入口
 *
 * <p>继承 {@link OncePerRequestFilter}，确保每个请求只执行一次过滤，
 * 在 Spring Security 的过滤器链中位于 {@code UsernamePasswordAuthenticationFilter} 之前。</p>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li>从请求头 Authorization 中提取 Bearer Token</li>
 *   <li>验证 Token 的合法性和有效期（通过 {@link JwtConfig}）</li>
 *   <li>从 Token 中解析 userId 和 role</li>
 *   <li>查询数据库确认用户存在且状态正常（status=1）</li>
 *   <li>根据 role 构建 Spring Security 的 Authentication 对象并存入 SecurityContext</li>
 * </ol>
 *
 * <p>角色映射关系：</p>
 * <ul>
 *   <li>role=2 → ROLE_SUPER_ADMIN（超级管理员）</li>
 *   <li>role=1 → ROLE_ADMIN（普通管理员）</li>
 *   <li>role=3 → ROLE_WORKER（维修师傅/跑腿人员等专职人员）</li>
 *   <li>role=0 → ROLE_USER（普通社区用户）</li>
 * </ul>
 *
 * @see JwtConfig JWT 生成与验证工具
 * @see OncePerRequestFilter Spring 单次执行过滤器
 * @see SecurityContextHolder Spring Security 上下文持有者
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** JWT 配置工具：生成、解析、验证 Token */
    private final JwtConfig jwtConfig;
    /** 用户表数据库访问接口，用于查询用户的实时状态 */
    private final SysUserMapper userMapper;

    /**
     * 请求过滤的核心逻辑
     *
     * <p>对每个进入应用的 HTTP 请求执行 JWT 认证。如果 Token 有效且用户状态正常，
     * 则将认证信息注入 Spring Security 上下文，后续的权限控制（如 @PreAuthorize、
     * hasRole() 等）即可正常工作。</p>
     *
     * @param request     当前 HTTP 请求
     * @param response    当前 HTTP 响应
     * @param filterChain 过滤器链，调用 doFilter 将请求传递给下一个过滤器
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 第 1 步：从请求的 Authorization 头中提取 JWT Token
        String token = getTokenFromRequest(request);

        // 第 2 步：仅当 Token 非空且校验通过时才进行后续认证
        if (StringUtils.hasText(token) && jwtConfig.validateToken(token)) {
            // 第 3 步：从 Token 中提取用户身份信息
            Long userId = jwtConfig.getUserIdFromToken(token);
            Integer role = jwtConfig.getRoleFromToken(token);

            // 第 4 步：查询数据库，验证用户是否存在且未被禁用（status=1 为正常状态）
            SysUser user = userMapper.selectById(userId);
            if (user != null && user.getStatus() == 1) {
                // 第 5 步：将数字角色码映射为 Spring Security 的权限字符串
                String authority = switch (role) {
                    case 2 -> "ROLE_SUPER_ADMIN";  // 超级管理员
                    case 1 -> "ROLE_ADMIN";         // 普通管理员
                    case 3 -> "ROLE_WORKER";        // 专职服务人员
                    default -> "ROLE_USER";          // 普通用户
                };

                // 第 6 步：构建 UsernamePasswordAuthenticationToken 并设置到 SecurityContext
                // 第一个参数是 principal（用户实体），第三个参数是权限列表
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        user,                                              // principal
                        null,                                              // credentials（密码无需在此传递）
                        Collections.singletonList(new SimpleGrantedAuthority(authority)) // 权限
                    );

                // 将认证信息存入 Spring Security 上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 第 7 步：无论认证成功与否，继续执行过滤器链中的后续过滤器
        filterChain.doFilter(request, response);
    }

    /**
     * 从 HTTP 请求头中提取 JWT Token
     *
     * <p>约定格式：Authorization: Bearer &lt;token&gt;</p>
     * <p>提取逻辑：检查 Authorization 头是否以 "Bearer " 开头（含空格），
     * 若是则截取第 7 个字符之后的部分作为 Token 返回；否则返回 null。</p>
     *
     * @param request HTTP 请求对象
     * @return JWT Token 字符串，若无则返回 null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // 检查 Authorization 头非空且以 "Bearer " 开头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // 截取 "Bearer " 之后的 Token 部分（长度为7，因为 "Bearer " 共7个字符）
            return bearerToken.substring(7);
        }
        return null;
    }
}

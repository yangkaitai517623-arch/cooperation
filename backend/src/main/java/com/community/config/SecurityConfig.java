package com.community.config;

import com.community.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 核心安全配置
 *
 * <p>这是整个应用安全体系的"大脑"，负责定义：</p>
 * <ul>
 *   <li>哪些接口公开访问、哪些需要认证、哪些需要特定角色</li>
 *   <li>密码加密策略（BCrypt）</li>
 *   <li>JWT 无状态认证模式（禁用 Session，启用 JWT 过滤器）</li>
 *   <li>CORS 配置（与 {@link CorsConfig} 配合）</li>
 * </ul>
 *
 * <p>安全架构总览：</p>
 * <pre>
 * 客户端请求 → CORS 过滤器 → JWT 过滤器 → 权限校验 → Controller
 *                                  ↑
 *             JwtAuthenticationFilter（从 Token 中恢复用户身份）
 * </pre>
 *
 * <p>接口权限分级：</p>
 * <table border="1">
 *   <tr><th>级别</th><th>路径模式</th><th>访问条件</th></tr>
 *   <tr><td>完全公开</td><td>/api/auth/**, /api/public/**, 静态资源等</td><td>无需认证</td></tr>
 *   <tr><td>管理员</td><td>/api/admin/**</td><td>需 ADMIN 或 SUPER_ADMIN 角色</td></tr>
 *   <tr><td>已认证用户</td><td>其他所有接口</td><td>需登录（持有有效 JWT）</td></tr>
 * </table>
 *
 * @see JwtAuthenticationFilter JWT 认证过滤器
 * @see UserDetailsServiceImpl   从数据库加载用户信息
 */
@Configuration
@EnableWebSecurity              // 启用 Spring Security 的 Web 安全功能
@RequiredArgsConstructor        // Lombok：为 final 字段生成构造函数注入
public class SecurityConfig {

    /** JWT 认证过滤器，在每次请求时校验 Token */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置安全过滤器链 — Spring Security 的核心入口
     *
     * <p>逐个配置安全策略：禁用 CSRF → 启用 CORS → 无状态 Session → 路由权限 → 注册 JWT 过滤器</p>
     *
     * @param http Spring Security 的 HTTP 安全构建器
     * @return 构建好的 SecurityFilterChain Bean
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. 禁用 CSRF（跨站请求伪造）保护
            //    原因：前后端分离 + JWT 无状态认证，不存在 Session 劫持风险，
            //    且不依赖 Cookie 传递认证信息，CSRF 保护不再必要
            .csrf(csrf -> csrf.disable())

            // 2. 配置 CORS（跨域资源共享）
            //    内联配置，允许所有来源、所有方法和所有请求头的跨域请求
            .cors(cors -> cors.configurationSource(request -> {
                org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
                config.addAllowedOriginPattern("*");   // 允许所有来源
                config.setAllowCredentials(true);      // 允许携带认证信息
                config.addAllowedMethod("*");           // 允许所有 HTTP 方法
                config.addAllowedHeader("*");           // 允许所有请求头
                config.addExposedHeader("Authorization"); // 暴露 Authorization 头给前端
                return config;
            }))

            // 3. 配置 Session 管理策略为"无状态"
            //    JWT 本身自包含用户信息，服务端无需维护 Session，从而实现水平扩展
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 4. 配置 URL 级别的访问权限（按匹配顺序从上到下）
            .authorizeHttpRequests(auth -> auth
                // ===== 完全公开的接口（无需登录即可访问）=====
                .requestMatchers("/api/auth/**").permitAll()      // 登录/注册
                .requestMatchers("/api/public/**").permitAll()    // 公共查询接口
                .requestMatchers("/api/init/**").permitAll()      // 初始化/健康检查
                .requestMatchers("/api/upload/**").permitAll()    // 文件上传
                .requestMatchers("/uploads/**").permitAll()       // 静态上传文件访问
                .requestMatchers("/doc.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // API 文档

                // ===== 预检请求（OPTIONS）也公开 ====
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ===== 需要管理员或超级管理员角色 ====
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                // ===== 其他所有接口需要已认证（持有有效 JWT Token）=====
                .anyRequest().authenticated()
            )

            // 5. 将 JWT 过滤器注册到 UsernamePasswordAuthenticationFilter 之前
            //    这样每次请求都会先经过 JWT 校验，通过后再走后续的权限判断
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器 Bean — 使用 BCrypt 单向哈希算法
     *
     * <p>BCrypt 特点：</p>
     * <ul>
     *   <li>自带随机盐值（Salt），每次加密生成的密文不同，防止彩虹表攻击</li>
     *   <li>计算速度较慢（可通过 strength 参数调节），增加暴力破解成本</li>
     *   <li>密文格式：$2a$10$...，其中 10 代表计算轮数为 2^10 = 1024 次</li>
     * </ul>
     *
     * <p>使用方式：注册时调用 encode(rawPassword) 加密存储；登录时调用 matches(raw, encoded) 验证。</p>
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器 Bean — 提供认证能力
     *
     * <p>AuthenticationManager 是 Spring Security 认证流程的入口，
     * 负责接收认证请求并委托给合适的 AuthenticationProvider 执行认证。</p>
     *
     * @param config Spring Security 的认证配置对象
     * @return 认证管理器实例
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

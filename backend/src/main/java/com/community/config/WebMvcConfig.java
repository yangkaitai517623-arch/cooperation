package com.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 *
 * <p>实现 {@link WebMvcConfigurer} 接口，自定义 Spring MVC 的行为，
 * 主要包括两个方面的配置：</p>
 *
 * <p><b>1. 静态资源映射（文件上传访问）</b></p>
 * <p>将本地磁盘的上传目录映射为 HTTP URL 路径，使得前端可以通过
 * {@code http://localhost:8080/uploads/xxx.jpg} 访问上传的文件。</p>
 * <p>例如：磁盘路径 {@code E:/code/新建文件夹/community-platform/uploads/avatar.png}
 * 对应 URL {@code /uploads/avatar.png}。</p>
 *
 * <p><b>2. CORS 跨域映射（已由 CorsConfig 的 CorsFilter 覆盖）</b></p>
 * <p>这里提供的是另一个 CORS 配置入口，与 {@code CorsFilter} Bean 形成双保险，
 * 确保跨域请求在任何层面都不会被拒绝。</p>
 *
 * @see WebMvcConfigurer Spring MVC 配置接口
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理器 — 让上传文件可以通过 HTTP URL 访问
     *
     * <p>工作机制：</p>
     * <ol>
     *   <li>将 URL 路径模式 {@code /uploads/**} 映射到磁盘上的绝对路径</li>
     *   <li>当浏览器请求 {@code /uploads/avatar.png} 时，Spring MVC 会从
     *       {@code E:/code/新建文件夹/community-platform/uploads/avatar.png} 读取文件并返回</li>
     * </ol>
     *
     * <p>注意：生产环境中应使用相对于项目根目录的相对路径，或通过配置文件注入路径，
     * 避免硬编码绝对路径。</p>
     *
     * @param registry 资源处理器注册中心
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // URL 路径模式                             磁盘绝对路径（file: 前缀表示本地文件系统）
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:E:/code/新建文件夹/community-platform/uploads/");
    }

    /**
     * 配置全局 CORS 映射（与 CorsFilter 形成双保险）
     *
     * <p>允许所有来源、指定 HTTP 方法、所有请求头的跨域请求。
     * 由于项目中同时注册了 {@code CorsFilter} Bean，此配置作为兜底备用。</p>
     *
     * @param registry CORS 注册中心
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")            // 所有路径
                .allowedOrigins("*")          // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的 HTTP 方法
                .allowedHeaders("*");         // 允许所有请求头
    }
}

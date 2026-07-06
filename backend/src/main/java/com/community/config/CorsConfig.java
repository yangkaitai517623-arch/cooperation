package com.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS（跨域资源共享）配置类
 *
 * <p>用于解决前后端分离开发时浏览器的同源策略限制问题。
 * 当前配置允许所有来源、所有请求方法、所有请求头的跨域请求，
 * 并支持携带 Cookie/Authorization 等认证信息。</p>
 *
 * <p>注意：生产环境中应限制 allowedOrigins 为具体域名以增强安全性。</p>
 *
 * @see CorsFilter Spring提供的CORS过滤器
 * @see CorsConfiguration CORS规则配置
 */
@Configuration
public class CorsConfig {

    /**
     * 注册一个全局的 CORS 过滤器 Bean
     *
     * <p>核心策略：</p>
     * <ul>
     *   <li>允许所有来源（addAllowedOriginPattern("*")），适用于开发环境</li>
     *   <li>允许携带 Cookie/Authorization 头（setAllowCredentials(true)）</li>
     *   <li>允许所有 HTTP 方法：GET、POST、PUT、DELETE、PATCH 等</li>
     *   <li>允许所有自定义请求头</li>
     *   <li>暴露 Authorization 头给前端 JS 可以读取</li>
     * </ul>
     *
     * @return 配置好的 CorsFilter 实例
     */
    @Bean
    public CorsFilter corsFilter() {
        // 1. 创建 CORS 配置对象，逐项设置允许策略
        CorsConfiguration config = new CorsConfiguration();

        // 允许所有来源访问（开发阶段；生产环境请改为具体域名如 "https://xxx.com"）
        config.addAllowedOriginPattern("*");

        // 允许请求携带 Cookie 和 HTTP 认证信息
        config.setAllowCredentials(true);

        // 允许所有 HTTP 方法（GET、POST、PUT、DELETE、OPTIONS 等）
        config.addAllowedMethod("*");

        // 允许所有请求头（如 Content-Type、Authorization、X-Requested-With 等）
        config.addAllowedHeader("*");

        // 暴露 Authorization 响应头，使前端 JS 可以读取该头部信息
        config.addExposedHeader("Authorization");

        // 2. 将 CORS 配置注册到所有的 URL 路径（/** 表示全局）
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        // 3. 返回基于 URL 规则的 CorsFilter
        return new CorsFilter(source);
    }
}

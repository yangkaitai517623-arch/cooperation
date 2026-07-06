package com.community.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson JSON 序列化/反序列化全局配置
 *
 * <p>Spring Boot 默认使用 Jackson 作为 JSON 处理库。本配置类通过
 * {@link Jackson2ObjectMapperBuilderCustomizer} 自定义 Jackson 的序列化行为，
 * 确保前后端数据交互时时间格式统一、数字无精度丢失。</p>
 *
 * <p>主要定制项：</p>
 * <ul>
 *   <li>LocalDateTime → 格式化为 "yyyy-MM-dd HH:mm:ss"</li>
 *   <li>LocalDate → 格式化为 "yyyy-MM-dd"</li>
 *   <li>Long 类型 → 序列化为字符串（避免 JavaScript 的 Number 精度丢失问题）</li>
 * </ul>
 *
 * <p>JavaScript 的 Number 类型只能安全表示 ±(2^53-1) 的整数，
 * 而 Java 的 Long 最大值为 2^63-1。直接将 Long 传给前端可能导致精度丢失，
 * 因此统一转为字符串传输。</p>
 *
 * @see Jackson2ObjectMapperBuilderCustomizer Jackson 构建器定制接口
 */
@Configuration
public class JacksonConfig {

    /** 日期时间格式：年-月-日 时:分:秒 */
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /** 日期格式：年-月-日 */
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 注册 Jackson 自定义配置 Bean
     *
     * <p>Spring Boot 启动时会自动检测所有
     * {@link Jackson2ObjectMapperBuilderCustomizer} Bean，
     * 并将配置应用到全局的 ObjectMapper 上。</p>
     *
     * @return 配置好的定制器实例
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // ========== 1. LocalDateTime 序列化/反序列化 ==========
            // 前后端统一使用 "yyyy-MM-dd HH:mm:ss" 格式传输日期时间
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

            // ========== 2. LocalDate 序列化/反序列化 ==========
            // 前后端统一使用 "yyyy-MM-dd" 格式传输日期（无时分秒）
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
            builder.serializerByType(LocalDate.class, new LocalDateSerializer(dateFormatter));
            builder.deserializerByType(LocalDate.class, new LocalDateDeserializer(dateFormatter));

            // ========== 3. Long 类型转字符串 ==========
            // 防止 JavaScript Number 精度丢失（JS 安全整数范围为 ±2^53-1）
            // Long.TYPE 处理基础类型 long，Long.class 处理包装类型
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}

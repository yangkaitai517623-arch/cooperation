package com.community.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 配置类
 *
 * <p>MyBatis Plus 是 MyBatis 的增强工具，在 MyBatis 基础上提供分页、自动填充等扩展功能。
 * 本配置类注册两个核心 Bean：</p>
 * <ol>
 *   <li><b>分页插件</b>：拦截 SQL 执行，自动追加 LIMIT 语句实现物理分页</li>
 *   <li><b>自动填充处理器</b>：在 INSERT 或 UPDATE 操作时自动设置 createdAt、updatedAt 时间字段</li>
 * </ol>
 *
 * <p>使用前提：实体类的时间字段需标注 {@code @TableField(fill = FieldFill.INSERT)} 或
 * {@code @TableField(fill = FieldFill.INSERT_UPDATE)} 注解，自动填充机制才会生效。</p>
 *
 * @see MybatisPlusInterceptor MyBatis Plus 核心拦截器
 * @see MetaObjectHandler 自动填充处理器
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 注册分页插件 — 为所有 SQL 查询提供物理分页能力
     *
     * <p>实现原理：MyBatis Plus 拦截器在执行 SQL 前检测是否传入了分页参数（Page 对象），
     * 若有则在原始 SQL 末尾追加 LIMIT ? OFFSET ? 子句，实现真正的数据库分页，
     * 而非将所有数据拉到内存再分页。</p>
     *
     * <p>使用方式：在 Mapper 或 Service 层调用 {@code page(new Page<>(pageNum, pageSize), wrapper)} 即可
     * 获得分页结果。</p>
     *
     * @return 配置好的 MybatisPlusInterceptor 实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建 MyBatis Plus 核心拦截器
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页内部拦截器，指定数据库类型为 MySQL（用于生成正确的分页 SQL）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 注册自动填充处理器 — 自动维护 createdAt 和 updatedAt 时间戳
     *
     * <p>自动填充规则：</p>
     * <ul>
     *   <li><b>INSERT 操作</b>：同时填充 createdAt（创建时间）和 updatedAt（更新时间）为当前时间</li>
     *   <li><b>UPDATE 操作</b>：仅更新 updatedAt（更新时间）为当前时间</li>
     * </ul>
     *
     * <p>使用 strictInsertFill/strictUpdateFill 方式：</p>
     * <ul>
     *   <li>只在目标字段值为 null 或未设置时自动填充</li>
     *   <li>如果代码中已显式设置了字段值，则不覆盖</li>
     * </ul>
     *
     * @return MetaObjectHandler 匿名实现类
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            /**
             * 执行 INSERT 时自动填充
             * @param metaObject MyBatis 元数据对象，包含当前操作的实体信息
             */
            @Override
            public void insertFill(MetaObject metaObject) {
                // 获取当前时间，同时设置 createdAt 和 updatedAt
                LocalDateTime now = LocalDateTime.now();
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
                this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
            }

            /**
             * 执行 UPDATE 时自动填充
             * @param metaObject MyBatis 元数据对象，包含当前操作的实体信息
             */
            @Override
            public void updateFill(MetaObject metaObject) {
                // 仅更新 updatedAt 为当前时间
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}

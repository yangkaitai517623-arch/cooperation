package com.community.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统用户数据访问层 Mapper 接口。
 * <p>
 * 映射实体：{@link SysUser}（系统用户表 sys_user）。
 * 继承 MyBatis-Plus 的 BaseMapper，自动获得基础的 CRUD 操作方法。
 * 额外提供按用户名查询、列出普通用户、统计用户数量等自定义查询。
 * </p>
 *
 * @author community-platform
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名精确查询系统用户。
     * <p>
     * 执行 SQL：{@code SELECT * FROM sys_user WHERE username = #{username}}
     * 适用于登录认证、用户存在性校验等场景。
     * </p>
     *
     * @param username 用户名（唯一标识）
     * @return 匹配的用户实体，未找到则返回 {@code null}
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser findByUsername(String username);

    /**
     * 查询所有普通角色用户列表（role = 0），按创建时间降序排列。
     * <p>
     * 执行 SQL：{@code SELECT * FROM sys_user WHERE role = 0 ORDER BY created_at DESC}
     * 适用于后台管理系统展示用户列表、用户管理等场景。
     * </p>
     *
     * @return 普通用户实体列表，按注册时间从新到旧排列
     */
    @Select("SELECT * FROM sys_user WHERE role = 0 ORDER BY created_at DESC")
    List<SysUser> findAllUsers();

    /**
     * 统计普通角色用户总数。
     * <p>
     * 执行 SQL：{@code SELECT COUNT(*) FROM sys_user WHERE role = 0}
     * 适用于仪表盘统计、用户数量展示等场景。
     * </p>
     *
     * @return 普通用户总数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE role = 0")
    int countUsers();

    /**
     * 统计活跃状态的普通用户数量（status = 1）。
     * <p>
     * 执行 SQL：{@code SELECT COUNT(*) FROM sys_user WHERE role = 0 AND status = 1}
     * 适用于统计平台日活/月活用户、活跃度分析等场景。
     * </p>
     *
     * @return 活跃普通用户数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE role = 0 AND status = 1")
    int countActiveUsers();
}

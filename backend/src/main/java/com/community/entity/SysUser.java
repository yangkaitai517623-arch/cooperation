package com.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统用户实体 — 映射 sys_user 表
 *
 * <p>系统的核心用户表，存储所有用户的基础信息、角色和状态。
 * 支持四级角色体系：普通用户、管理员、超级管理员、专职服务人员。</p>
 *
 * <p>表结构要点：</p>
 * <ul>
 *   <li>密码使用 BCrypt 加密存储，不可逆</li>
 *   <li>role 字段控制用户的身份和权限范围</li>
 *   <li>status 字段控制账户的启用/禁用</li>
 *   <li>createdAt/updatedAt 由 MyBatis Plus 自动填充</li>
 * </ul>
 */
@Data
@TableName("sys_user")
public class SysUser {

    /** 用户 ID（主键，自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（登录账号，唯一） */
    private String username;

    /** 密码（BCrypt 加密后的密文，不可逆） */
    private String password;

    /** 真实姓名（用于显示和身份识别） */
    private String realName;

    /** 手机号码（可用于短信通知、找回答密码） */
    private String phone;

    /** 电子邮箱（备用联系方式） */
    private String email;

    /** 头像 URL（存储上传后的文件路径） */
    private String avatar;

    /** 楼栋号（社区地址信息，如 "3栋"） */
    private String building;

    /** 房间号（社区地址信息，如 "502"） */
    private String room;

    /**
     * 角色标识码
     * <ul>
     *   <li>0 — 普通用户（社区居民）</li>
     *   <li>1 — 管理员（后台管理权限）</li>
     *   <li>2 — 超级管理员（最高权限，可管理其他管理员）</li>
     *   <li>3 — 专职服务人员（维修师傅、跑腿员等）</li>
     * </ul>
     */
    private Integer role;

    /**
     * 账户状态
     * <ul>
     *   <li>0 — 禁用（无法登录和使用系统）</li>
     *   <li>1 — 正常</li>
     * </ul>
     */
    private Integer status;

    /** 创建时间（自动填充 — INSERT 时设置） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间（自动填充 — INSERT 和 UPDATE 时都会更新） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

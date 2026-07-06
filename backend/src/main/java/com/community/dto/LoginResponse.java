package com.community.dto;

import com.community.entity.SysUser;
import lombok.Data;

/**
 * 登录响应 DTO — 登录成功后返回给前端的数据
 *
 * <p>包含两个核心信息：</p>
 * <ul>
 *   <li><b>token</b>：JWT Token 字符串，前端需存储并在后续请求的 Authorization 头中携带</li>
 *   <li><b>user</b>：当前登录用户的完整信息（用户名、角色、头像等），用于前端展示和权限判断</li>
 * </ul>
 *
 * <p>注意：返回的 user 对象中的 password 字段应清空或置为 null，
 * 避免密码密文泄露到前端。实际清空操作在 Service 层完成。</p>
 *
 * @see LoginRequest 登录请求对象
 */
@Data
public class LoginResponse {

    /** JWT Token 字符串（三段 Base64，以 . 分隔） */
    private String token;

    /** 登录用户的完整信息（password 字段应已在返回前清空） */
    private SysUser user;

    /**
     * 构造登录响应对象
     *
     * @param token 生成的 JWT Token
     * @param user  登录成功的用户实体
     */
    public LoginResponse(String token, SysUser user) {
        this.token = token;
        this.user = user;
    }
}

package com.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO — 接收前端登录表单的数据
 *
 * <p>前端提交登录表单时，将用户名和密码封装为此对象传递给后端。
 * 使用 Jakarta Bean Validation 注解进行基础参数校验，
 * 确保必填字段不为空。</p>
 *
 * <p>请求示例：</p>
 * <pre>
 * POST /api/auth/login
 * {
 *   "username": "zhangsan",
 *   "password": "mypassword123"
 * }
 * </pre>
 *
 * @see LoginResponse 登录成功后的应答对象
 * @see RegisterRequest 注册请求对象
 */
@Data
public class LoginRequest {

    /** 用户名（必填，不能为空） */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码（必填，不能为空；原始明文，后端与 BCrypt 密文比对） */
    @NotBlank(message = "密码不能为空")
    private String password;
}

package com.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册请求 DTO — 接收前端注册表单的数据
 *
 * <p>前端注册页面提交时，将表单数据封装为此对象传递给后端。
 * 用户名和密码为必填项，社区地址信息（楼栋号、房间号）和联系方式为选填。</p>
 *
 * <p>请求示例：</p>
 * <pre>
 * POST /api/auth/register
 * {
 *   "username": "lisi",
 *   "password": "Abc12345",
 *   "realName": "李四",
 *   "phone": "13800138000",
 *   "building": "3栋",
 *   "room": "502"
 * }
 * </pre>
 *
 * <p>密码规则（前端校验，后端存储 BCrypt）：</p>
 * <ul>
 *   <li>长度 >= 6</li>
 *   <li>必须同时包含数字和字母</li>
 * </ul>
 *
 * @see LoginRequest 登录请求对象
 */
@Data
public class RegisterRequest {

    /** 用户名（必填，登录凭证，全局唯一） */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码（必填，前端校验含数字和字母，后端 BCrypt 加密存储） */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 真实姓名（选填，用于社区身份识别和显示） */
    private String realName;

    /** 手机号码（选填，可用于短信通知和找回密码） */
    private String phone;

    /** 电子邮箱（选填，备用联系方式） */
    private String email;

    /** 楼栋号（选填，如 "3栋"） */
    private String building;

    /** 房间号（选填，如 "502"） */
    private String room;
}

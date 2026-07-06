package com.community.controller;

import com.community.dto.LoginRequest;
import com.community.dto.LoginResponse;
import com.community.dto.Result;
import com.community.entity.SysUser;
import com.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 - 处理用户登录和注册相关请求
 * <p>
 * 映射路径：{@code /api/auth}<br>
 * 所属模块：用户认证模块<br>
 * 开放范围：所有用户（无需登录即可访问）
 * </p>
 *
 * @author community-platform
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /** 用户服务，处理登录验证和注册业务逻辑 */
    private final UserService userService;

    /**
     * 用户登录
     * <p>
     * POST /api/auth/login<br>
     * 接收用户登录凭证，验证身份后返回JWT令牌和用户信息。<br>
     * 无需登录即可访问。
     * </p>
     *
     * @param loginRequest 登录请求体，包含用户名(username)和密码(password)
     * @return 登录成功返回包含token和用户信息的LoginResponse；失败返回错误信息
     * @see UserService#login(LoginRequest)
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 调用用户服务进行登录验证
            LoginResponse loginResponse = userService.login(loginRequest);
            return Result.success(loginResponse);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户注册
     * <p>
     * POST /api/auth/register<br>
     * 接收用户填写的注册信息，创建新的社区用户账号。<br>
     * 注册时需要提供用户名、密码、真实姓名、联系方式等信息。<br>
     * 无需登录即可访问。
     * </p>
     *
     * @param user 用户实体对象，包含注册所需的基本信息（用户名、密码、真实姓名、手机号、邮箱、楼栋、房间号）
     * @return 注册成功返回null；失败返回错误信息（如用户名已存在）
     * @see UserService#register(com.community.dto.RegisterRequest)
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody SysUser user) {
        try {
            // 将用户实体转换为注册请求对象
            com.community.dto.RegisterRequest request = new com.community.dto.RegisterRequest();
            request.setUsername(user.getUsername());
            request.setPassword(user.getPassword());
            request.setRealName(user.getRealName());
            request.setPhone(user.getPhone());
            request.setEmail(user.getEmail());
            request.setBuilding(user.getBuilding());
            request.setRoom(user.getRoom());
            return userService.register(request);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

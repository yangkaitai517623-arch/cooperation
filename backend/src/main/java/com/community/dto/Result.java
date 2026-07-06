package com.community.dto;

import lombok.Data;

/**
 * 统一 API 响应包装器 — 整个后端对前端的标准应答格式
 *
 * <p>所有 Controller 的返回值统一使用此类封装，确保前端能够用统一的逻辑
 * 处理所有 API 响应。泛型 {@code <T>} 允许携带任意类型的数据负载。</p>
 *
 * <p>标准响应格式：</p>
 * <pre>
 * {
 *   "code": 200,           // HTTP 状态码
 *   "message": "操作成功",  // 提示信息
 *   "data": { ... }        // 业务数据（可为 null）
 * }
 * </pre>
 *
 * <p>典型用法：</p>
 * <ul>
 *   <li>成功返回数据：{@code Result.success(userList)}</li>
 *   <li>成功无数据：{@code Result.success()}</li>
 *   <li>成功自定义消息：{@code Result.success("注册成功", user)}</li>
 *   <li>业务错误：{@code Result.error("用户名已存在")}</li>
 *   <li>自定义错误码：{@code Result.error(400, "参数校验失败")}</li>
 *   <li>未登录：{@code Result.unauthorized()}</li>
 *   <li>无权限：{@code Result.forbidden()}</li>
 * </ul>
 *
 * @param <T> 业务数据的类型
 */
@Data
public class Result<T> {

    /** HTTP 状态码（200=成功，400=参数错误，401=未登录，403=无权限，500=服务器错误） */
    private int code;

    /** 提示信息（成功或失败的具体描述） */
    private String message;

    /** 业务数据负载（泛型，成功时存放返回数据，失败时为 null） */
    private T data;

    /**
     * 快捷创建"操作成功"响应（无数据）
     *
     * @param <T>  泛型类型
     * @return code=200, message="操作成功", data=null
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 快捷创建"操作成功"响应（携带数据）
     *
     * @param data 要返回的业务数据
     * @param <T>  泛型类型
     * @return code=200, message="操作成功", data=传入的数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 快捷创建"成功"响应（自定义提示消息 + 携带数据）
     *
     * @param message 自定义成功消息（如 "注册成功"、"发布成功"）
     * @param data    要返回的业务数据
     * @param <T>     泛型类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 快捷创建"业务错误"响应（默认 code=500）
     *
     * @param message 错误消息（如 "用户名已存在"、"密码错误"）
     * @param <T>     泛型类型
     * @return code=500 的错误响应，data=null
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    /**
     * 快捷创建"业务错误"响应（自定义错误码）
     *
     * @param code    自定义 HTTP 错误码
     * @param message 错误描述
     * @param <T>     泛型类型
     * @return 错误响应对象
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 快捷创建"未登录"响应（code=401）
     * <p>当前端请求需要认证的接口但未提供有效 Token 时返回此响应</p>
     *
     * @param <T> 泛型类型
     * @return code=401, message="未登录或登录已过期"
     */
    public static <T> Result<T> unauthorized() {
        return error(401, "未登录或登录已过期");
    }

    /**
     * 快捷创建"无权限"响应（code=403）
     * <p>当已登录用户尝试访问超出其角色权限的接口时返回此响应</p>
     *
     * @param <T> 泛型类型
     * @return code=403, message="无权限访问"
     */
    public static <T> Result<T> forbidden() {
        return error(403, "无权限访问");
    }
}

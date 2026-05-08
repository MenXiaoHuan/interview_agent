package com.multimodal.interview.common.result;

import lombok.Data;

/**
 * 通用接口响应体。
 *
 * @param <T> 响应数据类型
 */
@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    /**
     * 创建响应对象。
     *
     * @param code 响应状态码
     * @param message 响应消息
     * @param data 响应数据
     */
    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 创建成功响应。
     *
     * @param data 响应数据
     * @return 响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 创建带自定义消息的成功响应。
     *
     * @param message 自定义响应消息
     * @param data 响应数据
     * @return 响应对象
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 创建错误响应。
     *
     * @param resultCode 错误码枚举
     * @return 响应对象
     */
    public static <T> ApiResponse<T> error(ResultCode resultCode) {
        return new ApiResponse<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 创建带自定义状态码与消息的错误响应。
     *
     * @param code 自定义状态码
     * @param message 自定义错误消息
     * @return 响应对象
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 创建带自定义消息的错误响应。
     *
     * @param resultCode 错误码枚举
     * @param message 自定义错误消息
     * @return 响应对象
     */
    public static <T> ApiResponse<T> error(ResultCode resultCode, String message) {
        return new ApiResponse<>(resultCode.getCode(), message, null);
    }

    /**
     * 创建 400 错误响应。
     *
     * @param message 错误消息
     * @return 响应对象
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return error(ResultCode.BAD_REQUEST, message);
    }

    /**
     * 创建 401 错误响应。
     *
     * @param message 错误消息
     * @return 响应对象
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return error(ResultCode.UNAUTHORIZED, message);
    }

    /**
     * 创建 403 错误响应。
     *
     * @param message 错误消息
     * @return 响应对象
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return error(ResultCode.FORBIDDEN, message);
    }

    /**
     * 创建 404 错误响应。
     *
     * @param message 错误消息
     * @return 响应对象
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return error(ResultCode.NOT_FOUND, message);
    }

    /**
     * 创建 500 错误响应。
     *
     * @param message 错误消息
     * @return 响应对象
     */
    public static <T> ApiResponse<T> internalError(String message) {
        return error(ResultCode.INTERNAL_ERROR, message);
    }
}

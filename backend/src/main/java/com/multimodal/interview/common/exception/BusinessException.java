package com.multimodal.interview.common.exception;

import lombok.Getter;

/**
 * 业务异常定义。
 *
 * <p>用于在业务处理过程中携带统一错误码与错误信息。</p>
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    private final String message;

    /**
     * 创建业务异常。
     *
     * @param code 错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 创建 400 业务异常。
     *
     * @param message 错误信息
     * @return 业务异常对象
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }

    /**
     * 创建 401 业务异常。
     *
     * @param message 错误信息
     * @return 业务异常对象
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }

    /**
     * 创建 403 业务异常。
     *
     * @param message 错误信息
     * @return 业务异常对象
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }

    /**
     * 创建 404 业务异常。
     *
     * @param message 错误信息
     * @return 业务异常对象
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }

    /**
     * 创建 500 业务异常。
     *
     * @param message 错误信息
     * @return 业务异常对象
     */
    public static BusinessException internalError(String message) {
        return new BusinessException(500, message);
    }

    /**
     * 创建 503 业务异常。
     *
     * @param message 错误信息
     * @return 业务异常对象
     */
    public static BusinessException serviceUnavailable(String message) {
        return new BusinessException(503, message);
    }
}

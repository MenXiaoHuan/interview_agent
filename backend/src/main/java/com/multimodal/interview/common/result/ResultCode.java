package com.multimodal.interview.common.result;

/**
 * 统一响应状态码定义。
 */
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证或token过期"),
    FORBIDDEN(403, "没有权限"),
    NOT_FOUND(404, "资源不存在"),
    SERVICE_UNAVAILABLE(503, "AI 服务当前不可用"),
    SYSTEM_ERROR(500, "系统错误"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    /**
     * 创建状态码枚举。
     *
     * @param code 状态码
     * @param message 状态描述
     */
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取状态码。
     *
     * @return 状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取状态描述。
     *
     * @return 状态描述
     */
    public String getMessage() {
        return message;
    }
}

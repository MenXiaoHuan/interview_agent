package com.multimodal.interview.common.exception;

import com.multimodal.interview.common.result.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * <p>统一转换业务异常、参数校验异常与系统异常，保证接口返回结构一致。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * @param e 业务异常对象
     * @return 统一响应体
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理方法参数校验异常。
     *
     * @param e 方法参数验证异常对象
     * @return 统一响应体
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(org.springframework.validation.FieldError::getDefaultMessage)
                .orElse("请求参数格式错误");
        log.error("参数验证异常: {}", message, e);
        return ApiResponse.badRequest(message);
    }

    /**
     * 处理约束校验异常。
     *
     * @param e 约束验证异常对象
     * @return 统一响应体
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().iterator().next().getMessage();
        log.error("约束验证异常: {}", message, e);
        return ApiResponse.badRequest(message);
    }

    /**
     * 处理未捕获异常。
     *
     * @param e 异常对象
     * @return 统一响应体
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleException(Exception e) {
        log.error("服务器内部错误: {}", e.getMessage(), e);
        return ApiResponse.internalError("服务器内部错误");
    }
}

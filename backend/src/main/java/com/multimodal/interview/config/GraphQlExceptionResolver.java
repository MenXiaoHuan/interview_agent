package com.multimodal.interview.config;

import com.multimodal.interview.common.exception.BusinessException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * GraphQL 异常解析器。
 */
@Slf4j
@Component
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof BusinessException businessException) {
            return GraphqlErrorBuilder.newError(env)
                    .message(businessException.getMessage())
                    .errorType(toErrorType(businessException.getCode()))
                    .extensions(Map.of("code", businessException.getCode()))
                    .build();
        }

        if (ex instanceof ConstraintViolationException validationException) {
            String message = validationException.getConstraintViolations().iterator().next().getMessage();
            return GraphqlErrorBuilder.newError(env)
                    .message(message)
                    .errorType(ErrorType.BAD_REQUEST)
                    .extensions(Map.of("code", 400))
                    .build();
        }

        log.error("GraphQL execution error on field '{}': {}", env.getField().getName(), ex.getMessage(), ex);
        return GraphqlErrorBuilder.newError(env)
                .message("服务器内部错误")
                .errorType(ErrorType.INTERNAL_ERROR)
                .extensions(Map.of("code", 500))
                .build();
    }

    private ErrorType toErrorType(int code) {
        return switch (code) {
            case 400 -> ErrorType.BAD_REQUEST;
            case 401 -> ErrorType.UNAUTHORIZED;
            case 403 -> ErrorType.FORBIDDEN;
            default -> ErrorType.INTERNAL_ERROR;
        };
    }
}

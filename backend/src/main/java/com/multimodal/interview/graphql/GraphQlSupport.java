package com.multimodal.interview.graphql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.common.result.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * GraphQL 适配层公共能力。
 */
public abstract class GraphQlSupport {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    protected GraphQlSupport(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    protected Authentication getOptionalAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if ("anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
            return null;
        }
        return authentication;
    }

    protected Authentication requireAuthentication() {
        Authentication authentication = getOptionalAuthentication();
        if (authentication == null) {
            throw BusinessException.unauthorized("未认证或token过期");
        }
        return authentication;
    }

    protected UserDetails requireUserDetails() {
        Authentication authentication = requireAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails;
        }
        throw BusinessException.unauthorized("未认证或token过期");
    }

    protected <T> T validate(T target) {
        Set<ConstraintViolation<T>> violations = validator.validate(target);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return target;
    }

    protected boolean unwrapSuccess(ApiResponse<?> response) {
        unwrap(response);
        return true;
    }

    protected <T> T unwrap(ApiResponse<T> response) {
        if (response == null) {
            return null;
        }
        if (response.getCode() != 200) {
            throw new BusinessException(response.getCode(), response.getMessage());
        }
        return response.getData();
    }

    protected Object unwrap(ResponseEntity<?> responseEntity) {
        if (responseEntity == null) {
            return null;
        }
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            Object body = responseEntity.getBody();
            throw BusinessException.badRequest(body == null ? "请求失败" : String.valueOf(body));
        }

        Object body = responseEntity.getBody();
        if (body instanceof ApiResponse<?> apiResponse) {
            return unwrap(apiResponse);
        }
        return body;
    }

    protected List<String> parseOptions(String optionsRaw) {
        if (optionsRaw == null || optionsRaw.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(optionsRaw, new TypeReference<List<String>>() {
            });
        } catch (Exception ignore) {
            return Arrays.stream(optionsRaw.split("[,\\n]"))
                    .map(String::trim)
                    .filter(item -> !item.isEmpty())
                    .collect(Collectors.toList());
        }
    }
}

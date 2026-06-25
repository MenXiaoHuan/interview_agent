package com.multimodal.interview.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final Set<String> SENSITIVE_QUERY_KEYS = Set.of(
            "password", "pwd", "token", "access_token", "refresh_token", "jwt",
            "secret", "api_key", "apikey", "key", "authorization"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.currentTimeMillis();
        Throwable failure = null;
        try {
            filterChain.doFilter(request, response);
        } catch (ServletException | IOException | RuntimeException ex) {
            failure = ex;
            throw ex;
        } finally {
            writeAccessLog(request, response, startedAt, failure);
        }
    }

    private void writeAccessLog(HttpServletRequest request,
                                HttpServletResponse response,
                                long startedAt,
                                Throwable failure) {
        int status = failure == null ? response.getStatus() : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        long costMs = System.currentTimeMillis() - startedAt;
        String traceId = valueOrFallback(MDC.get(TraceIdFilter.MDC_TRACE_ID_KEY), "-");
        String error = failure == null ? "-" : valueOrFallback(failure.getMessage(), failure.getClass().getSimpleName());
        String message = String.format(
                "access traceId=%s method=%s path=%s query=%s status=%d costMs=%d clientIp=%s user=%s userAgent=%s error=%s",
                traceId,
                request.getMethod(),
                request.getRequestURI(),
                maskQuery(request.getQueryString()),
                status,
                costMs,
                resolveClientIp(request),
                resolveUser(),
                valueOrFallback(request.getHeader("User-Agent"), "-"),
                error
        );

        if (failure != null || status >= 500) {
            log.error(message);
        } else if (status >= 400) {
            log.warn(message);
        } else {
            log.info(message);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return valueOrFallback(request.getRemoteAddr(), "-");
    }

    private String resolveUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "anonymous";
        }
        String name = authentication.getName();
        if (name == null || name.isBlank() || "anonymousUser".equals(name)) {
            return "anonymous";
        }
        return name;
    }

    private String maskQuery(String query) {
        if (query == null || query.isBlank()) {
            return "-";
        }
        return Arrays.stream(query.split("&"))
                .map(this::maskQueryPart)
                .reduce((left, right) -> left + "&" + right)
                .orElse("-");
    }

    private String maskQueryPart(String part) {
        int separatorIndex = part.indexOf('=');
        if (separatorIndex < 0) {
            return part;
        }
        String rawKey = part.substring(0, separatorIndex);
        String key = decode(rawKey).toLowerCase(Locale.ROOT);
        if (SENSITIVE_QUERY_KEYS.contains(key)) {
            return rawKey + "=***";
        }
        return rawKey + "=" + URLEncoder.encode(decode(part.substring(separatorIndex + 1)), StandardCharsets.UTF_8);
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}

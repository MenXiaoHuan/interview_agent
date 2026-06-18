package com.multimodal.interview.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.common.result.ResultCode;
import com.multimodal.interview.entity.User;
import com.multimodal.interview.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器。
 *
 * <p>负责解析请求头中的令牌，并将认证用户写入 Spring Security 上下文。</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final String secretKey;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;

    /**
     * 创建 JWT 认证过滤器。
     *
     * @param secretKey JWT 解码密钥
     * @param objectMapper JSON 处理对象
     * @param userMapper 用户数据访问对象
     */
    public JwtAuthenticationFilter(String secretKey, ObjectMapper objectMapper, UserMapper userMapper) {
        this.secretKey = secretKey;
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
    }

    /**
     * 执行 JWT 认证逻辑。
     *
     * @param request HTTP 请求对象
     * @param response HTTP 响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException IO 异常
     */
    @Operation(summary = "通过的用户信息注入到 Spring Security 的安全上下文（SecurityContext）中，从而实现基于令牌的身份认证和授权。")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token != null) {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey.getBytes())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String username = claims.getSubject();
                User user = userMapper.findByUsername(username);

                if (user != null) {
                    UserDetails userDetails = org.springframework.security.core.userdetails.User
                            .withUsername(username)
                            .password(user.getPassword())
                            .authorities(resolveAuthorities(user))
                            .build();

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            handleAuthenticationError(response, e);
            return;
        }

        filterChain.doFilter(request, response);
    }

    public static List<SimpleGrantedAuthority> resolveAuthorities(User user) {
        if (user != null && Integer.valueOf(3).equals(user.getUserType())) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * 从请求头中提取 JWT。
     *
     * @param request HTTP 请求对象
     * @return JWT 字符串，不存在时返回 {@code null}
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 处理认证失败并输出统一响应。
     *
     * @param response HTTP 响应对象
     * @param e 认证异常
     * @throws IOException IO 异常
     */
    private void handleAuthenticationError(HttpServletResponse response, Exception e) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.error(ResultCode.UNAUTHORIZED, "无效的token或token已过期")
        ));
    }
}

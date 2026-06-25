package com.multimodal.interview.config;

import com.multimodal.interview.common.filter.JwtAuthenticationFilter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 安全配置。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final boolean swaggerEnabled;

    /**
     * 创建安全配置。
     *
     * @param jwtAuthenticationFilter JWT 认证过滤器
     * @param corsConfigurationSource 跨域配置源
     * @param swaggerEnabled 是否允许匿名访问 Swagger
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CorsConfigurationSource corsConfigurationSource,
                          @Value("${app.swagger.enabled:false}") boolean swaggerEnabled) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
        this.swaggerEnabled = swaggerEnabled;
    }

    /**
     * 注册密码编码器。
     *
     * @return 密码编码器
     */
    @Operation(summary = "密码编码器")
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置 Spring Security 过滤器链。
     *
     * @param http HttpSecurity 配置对象
     * @return 安全过滤器链
     * @throws Exception 配置异常
     */
    @Operation(summary = "安全过滤器链")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(this::configureAuthorization)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"code\":200,\"message\":\"登出成功\",\"data\":null}");
                        })
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .permitAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"code\":401,\"message\":\"未认证或token过期\",\"data\":null}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"code\":403,\"message\":\"权限不足\",\"data\":null}");
                        }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/forgot/**", "/api/auth/rsa-public-key").permitAll()
                .requestMatchers("/api/avatar", "/api/avatar/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/job-categories/**", "/api/job/**", "/api/jobs/**", "/jobs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/interview/questions/**", "/interview/questions/**").permitAll();
        if (swaggerEnabled) {
            auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll();
        }
        auth.requestMatchers(HttpMethod.POST,
                        "/api/job-categories", "/api/job-categories/**", "/api/jobs", "/api/jobs/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,
                        "/api/job-categories", "/api/job-categories/**", "/api/jobs", "/api/jobs/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE,
                        "/api/job-categories", "/api/job-categories/**", "/api/jobs", "/api/jobs/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN");
        auth.anyRequest().authenticated();
    }
}

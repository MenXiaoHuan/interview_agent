package com.multimodal.interview.service.impl;

import com.multimodal.interview.dto.LoginRequest;
import com.multimodal.interview.dto.RegisterRequest;
import com.multimodal.interview.entity.User;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.mapper.UserMapper;
import com.multimodal.interview.service.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 用户认证服务实现。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    public AuthServiceImpl(UserMapper userMapper, @Value("${jwt.secret}") String secretKey) {
        this.userMapper = userMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.secretKey = secretKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw BusinessException.badRequest("用户名已存在");
        }

        // 检查两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw BusinessException.badRequest("两次密码不一致");
        }

        // 校验邮箱唯一（如果提供）
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userMapper.findByEmail(request.getEmail()) != null) {
                throw BusinessException.badRequest("邮箱已被其他账号使用");
            }
        }

        // 校验手机号唯一（如果提供）
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (userMapper.findByPhone(request.getPhone()) != null) {
                throw BusinessException.badRequest("手机号已被其他账号使用");
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setUserType(1);
        user.setStatus(1);

        userMapper.insert(user);
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BusinessException.badRequest("用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw BusinessException.badRequest("账号已被禁用");
        }

        userMapper.updateLastLoginTime(user.getId());
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("userType", user.getUserType())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }
} 

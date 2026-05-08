package com.multimodal.interview.service.impl;

import com.multimodal.interview.dto.ForgotResetRequest;
import com.multimodal.interview.dto.ForgotSendCodeRequest;
import com.multimodal.interview.entity.PasswordResetToken;
import com.multimodal.interview.entity.User;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.mapper.PasswordResetTokenMapper;
import com.multimodal.interview.mapper.UserMapper;
import com.multimodal.interview.service.ForgotPasswordService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * 忘记密码服务实现。
 */
@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {
    private final UserMapper userMapper;
    private final PasswordResetTokenMapper tokenMapper;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordServiceImpl(UserMapper userMapper, PasswordResetTokenMapper tokenMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.tokenMapper = tokenMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendCode(ForgotSendCodeRequest request) {
        String contact;
        String channel;
        User user = null;
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            contact = request.getEmail();
            channel = "email";
            user = userMapper.findByEmail(contact);
        } else if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            contact = request.getPhone();
            channel = "phone";
            user = userMapper.findByPhone(contact);
        } else {
            throw BusinessException.badRequest("请输入邮箱或手机号");
        }
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        String code = generateCode();
        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(user.getId());
        token.setChannel(channel);
        token.setContact(contact);
        token.setCode(code);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(1));
        tokenMapper.insert(token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(ForgotResetRequest request) {
        String contact = request.getEmail() != null && !request.getEmail().isEmpty() ? request.getEmail() : request.getPhone();
        if (contact == null || contact.isEmpty()) {
            throw BusinessException.badRequest("请输入邮箱或手机号");
        }
        PasswordResetToken token = tokenMapper.findValid(contact, request.getCode());
        if (token == null) {
            throw BusinessException.badRequest("验证码无效或已过期");
        }
        User user = userMapper.findById(token.getUserId());
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        String hashed = passwordEncoder.encode(request.getNewPassword());
        userMapper.updatePassword(user.getId(), hashed);
        tokenMapper.markUsed(token.getId());
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int num = 100000 + random.nextInt(900000);
        return String.valueOf(num);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PasswordResetToken getLatestValidByContact(String contact) {
        return tokenMapper.findLatestValidByContact(contact);
    }
}

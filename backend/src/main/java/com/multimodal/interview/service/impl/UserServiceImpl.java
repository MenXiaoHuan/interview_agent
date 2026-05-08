package com.multimodal.interview.service.impl;

import com.multimodal.interview.dto.UserSettingsResponse;
import com.multimodal.interview.entity.User;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.mapper.UserMapper;
import com.multimodal.interview.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 用户信息服务实现。
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final SensitiveWordServiceImpl sensitiveWordServiceImpl;
    private final PasswordEncoder passwordEncoder;
    private final String fileRoot;

    public UserServiceImpl(
            UserMapper userMapper,
            SensitiveWordServiceImpl sensitiveWordServiceImpl,
            PasswordEncoder passwordEncoder,
            @Value("${app.file.root:/home/file}") String fileRoot
    ) {
        this.userMapper = userMapper;
        this.sensitiveWordServiceImpl = sensitiveWordServiceImpl;
        this.passwordEncoder = passwordEncoder;
        this.fileRoot = fileRoot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserProfile(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserSettingsResponse updateEyeMode(String username, UserSettingsResponse settings) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        user.setEyeCareMode(settings.getEyeCareMode());

        int rows = userMapper.updateEyeMode(user);
        if (rows != 1) {
            throw BusinessException.internalError("主题设置更新失败");
        }

        return settings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public User updateNickname(String username, String nickname) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        // 检查敏感词
        if (sensitiveWordServiceImpl.containsSensitiveWord(nickname)) {
            throw BusinessException.badRequest("昵称包含敏感词");
        }

        // 更新昵称
        int rows = userMapper.updateNickname(user.getId(), nickname);
        if (rows != 1) {
            throw BusinessException.internalError("更新昵称失败");
        }

        // 获取最新的用户信息
        return userMapper.findById(user.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateAvatar(String username, String avatarUrl) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        int rows = userMapper.updateAvatar(user.getId(), avatarUrl);
        if (rows != 1) {
            throw BusinessException.internalError("更新头像失败");
        }
    }

    @Override
    @Transactional
    public String uploadAvatar(String username, MultipartFile file, String baseUrl) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("头像文件不能为空");
        }
        if (file.getSize() > 5L * 1024 * 1024) {
            throw BusinessException.badRequest("头像文件过大（最大 5MB）");
        }

        String contentType = String.valueOf(file.getContentType());
        String ext;
        if (contentType.contains("png")) {
            ext = ".png";
        } else if (contentType.contains("jpeg") || contentType.contains("jpg")) {
            ext = ".jpg";
        } else if (contentType.contains("webp")) {
            ext = ".webp";
        } else {
            throw BusinessException.badRequest("不支持的头像格式，仅支持 png/jpg/webp");
        }

        Path avatarDir = Paths.get(fileRoot, "avatar");
        try {
            Files.createDirectories(avatarDir);
        } catch (IOException e) {
            throw BusinessException.internalError("创建头像目录失败");
        }

        String filename = "user" + user.getId() + "_" + System.currentTimeMillis() + ext;
        Path target = avatarDir.resolve(filename);
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw BusinessException.internalError("保存头像文件失败");
        }

        String avatarUrl = baseUrl + "/avatar/" + filename;
        int rows = userMapper.updateAvatar(user.getId(), avatarUrl);
        if (rows != 1) {
            throw BusinessException.internalError("更新头像失败");
        }
        return avatarUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updatePassword(String username, String oldPassword, String newPassword) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw BusinessException.badRequest("旧密码不正确");
        }
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        int rows = userMapper.updatePassword(user.getId(), encodedNewPassword);
        if (rows != 1) {
            throw BusinessException.internalError("修改密码失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void bindEmail(String username, String email) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        User existing = userMapper.findByEmail(email);
        if (existing != null && !existing.getId().equals(user.getId())) {
            throw BusinessException.badRequest("该邮箱已被其他账号绑定");
        }
        int rows = userMapper.updateEmail(user.getId(), email);
        if (rows != 1) {
            throw BusinessException.internalError("绑定邮箱失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void bindPhone(String username, String phone) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        User existing = userMapper.findByPhone(phone);
        if (existing != null && !existing.getId().equals(user.getId())) {
            throw BusinessException.badRequest("该手机号已被其他账号绑定");
        }
        int rows = userMapper.updatePhone(user.getId(), phone);
        if (rows != 1) {
            throw BusinessException.internalError("绑定手机号失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateSurpriseMode(String username, Integer surpriseMode) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        user.setSurpriseMode(surpriseMode);
        int rows = userMapper.updateSurpriseMode(user);
        if (rows != 1) {
            throw BusinessException.internalError("更新惊喜模式失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> findAll(int page, int size) {
        int total = userMapper.countAllUsers();
        int totalPages = (int) Math.ceil((double) total / size);
        if (page < 1 || page > totalPages) {
            throw BusinessException.badRequest("页码超出范围");
        }
        return userMapper.findAll(size, (page - 1) * size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteByUsername(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        int rows = userMapper.deleteByUsername(username);
        if (rows != 1) {
            throw BusinessException.internalError("删除用户失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateStatus(String username, Integer status) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        int rows = userMapper.updateStatus(user.getId(), status);
        if (rows != 1) {
            throw BusinessException.internalError("更新用户状态失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateUserType(String username, Integer userType) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        int rows = userMapper.updateUserType(user.getId(), userType);
        if (rows != 1) {
            throw BusinessException.internalError("更新用户类型失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateGender(String username, Integer gender) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        int rows = userMapper.updateGender(user.getId(), gender);
        if (rows != 1) {
            throw BusinessException.internalError("更新用户性别失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public User updateNicknameByUsername(String username, String nickname) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        // 检查敏感词
        if (sensitiveWordServiceImpl.containsSensitiveWord(nickname)) {
            throw BusinessException.badRequest("昵称包含敏感词");
        }

        // 更新昵称
        int rows = userMapper.updateNickname(user.getId(), nickname);
        if (rows != 1) {
            throw BusinessException.internalError("更新昵称失败");
        }

        // 获取最新的用户信息
        return userMapper.findById(user.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public User updateEmailByUsername(String username, String email) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        User existing = userMapper.findByEmail(email);
        if (existing != null && !existing.getId().equals(user.getId())) {
            throw BusinessException.badRequest("该邮箱已被其他账号绑定");
        }

        int rows = userMapper.updateEmail(user.getId(), email);
        if (rows != 1) {
            throw BusinessException.internalError("更新邮箱失败");
        }

        return userMapper.findById(user.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public User updatePhoneByUsername(String username, String phone) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        User existing = userMapper.findByPhone(phone);
        if (existing != null && !existing.getId().equals(user.getId())) {
            throw BusinessException.badRequest("该手机号已被其他账号绑定");
        }

        int rows = userMapper.updatePhone(user.getId(), phone);
        if (rows != 1) {
            throw BusinessException.internalError("更新手机号失败");
        }

        return userMapper.findById(user.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public User updateGenderByUsername(String username, Integer gender) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        int rows = userMapper.updateGender(user.getId(), gender);
        if (rows != 1) {
            throw BusinessException.internalError("更新用户性别失败");
        }

        return userMapper.findById(user.getId());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public User updateUserTypeByUsername(String username, Integer userType) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        int rows = userMapper.updateUserType(user.getId(), userType);
        if (rows != 1) {
            throw BusinessException.internalError("更新用户类型失败");
        }

        return userMapper.findById(user.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public User updateStatusByUsername(String username, Integer status) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        int rows = userMapper.updateStatus(user.getId(), status);
        if (rows != 1) {
            throw BusinessException.internalError("更新用户状态失败");
        }

        return userMapper.findById(user.getId());
    }
} 

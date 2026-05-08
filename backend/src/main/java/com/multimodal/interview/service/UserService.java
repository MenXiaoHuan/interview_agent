package com.multimodal.interview.service;

import com.multimodal.interview.dto.UserSettingsResponse;
import com.multimodal.interview.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户信息服务接口。
 */
public interface UserService {
    /**
     * 获取用户信息
     */
    User getUserProfile(String username);

    /**
     * 更新用户设置
     */
    UserSettingsResponse updateEyeMode(String username, UserSettingsResponse settings);

    /**
     * 更新用户昵称
     */
    User updateNickname(String username, String nickname);

    /**
     * 更新用户头像
     */
    void updateAvatar(String username, String avatarUrl);

    /**
     * 上传头像文件并更新用户头像 URL。
     *
     * @param username 当前用户名
     * @param file 头像文件
     * @param baseUrl 当前请求的 baseUrl（scheme://host:port）
     * @return 可访问的头像 URL
     */
    String uploadAvatar(String username, MultipartFile file, String baseUrl);

    /**
     * 更新用户密码
     */
    void updatePassword(String username, String oldPassword, String newPassword);

    /**
     * 绑定邮箱
     */
    void bindEmail(String username, String email);

    /**
     * 绑定手机号
     */
    void bindPhone(String username, String phone);

    void updateSurpriseMode(String username, Integer surpriseMode);

    /**
     * 查询所有用户
     */
    List<User> findAll(int page, int size);

    /**
     * 根据用户名删除用户
     */
    void deleteByUsername(String username);

    /**
     * 更新用户状态
     */
    void updateStatus(String username, Integer status);

    /**
     * 更新用户类型
     */
    void updateUserType(String username, Integer userType);

    /**
     * 更新用户性别
     */
    void updateGender(String username, Integer gender);

    /**
     * 根据用户名更新用户昵称
     */
    User updateNicknameByUsername(String username, String nickname);

    /**
     * 根据用户名更新用户邮箱
     */
    User updateEmailByUsername(String username, String email);

    /**
     * 根据用户名更新用户手机号
     */
    User updatePhoneByUsername(String username, String phone);

    /**
     * 根据用户名更新用户性别（字符串格式）
     */
    User updateGenderByUsername(String username, Integer gender);

    /**
     * 根据用户名更新用户类型
     */
    User updateUserTypeByUsername(String username, Integer userType);

    /**
     * 根据用户名更新用户状态
     */
    User updateStatusByUsername(String username, Integer status);
}

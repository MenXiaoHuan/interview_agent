package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.dto.*;
import com.multimodal.interview.entity.User;
import com.multimodal.interview.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 用户信息管理接口控制器。
 */
@Tag(name = "用户信息编辑", description = "用户信息编辑相关接口")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    /**
     * 获取用户资料。
     *
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "获取用户资料")
    @GetMapping("/profile")
    public ApiResponse<User> getProfile(Authentication authentication){
        String username = authentication.getName();
        User user = userService.getUserProfile(username);
        return ApiResponse.success("获取用户资料成功", user);
    }
    /**
     * 用户护眼模式设置。
     *
     * @param settings 用户设置参数
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "用户护眼模式设置")
    @PutMapping("/eyemode")
    public ApiResponse<UserSettingsResponse> updateEyeMode(@Valid @RequestBody UserSettingsResponse settings, Authentication authentication){
        String username = authentication.getName();
        UserSettingsResponse updatedSettings = userService.updateEyeMode(username, settings);
        return ApiResponse.success("设置成功", updatedSettings);
    }
    /**
     * 用户惊喜模式设置。
     *
     * @param request 请求体参数
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "用户惊喜模式设置")
    @PutMapping("/surprisemode")
    public ApiResponse<Map<String, Object>> updateSurpriseMode(@Valid @RequestBody SurpriseModeUpdateRequest request, Authentication authentication){
        String username = authentication.getName();
        userService.updateSurpriseMode(username, request.getSurpriseMode());
        User user = userService.getUserProfile(username);
        Map<String, Object> resp = new HashMap<>();
        resp.put("surpriseMode", user.getSurpriseMode());
        resp.put("updateTime", user.getUpdatedAt());
        return ApiResponse.success("设置成功", resp);
    }
    /**
     * 用户昵称更新。
     *
     * @param request 请求体参数
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "用户昵称更新")
    @PutMapping("/nickname")
    public ApiResponse<Map<String, Object>> updateNickname(@Valid @RequestBody NicknameUpdateRequest request, Authentication authentication){
        String username = authentication.getName();
        User updatedUser = userService.updateNickname(username, request.getNickname());

        Map<String, Object> response = new HashMap<>();
        response.put("userId", updatedUser.getId());
        response.put("nickname", updatedUser.getNickname());
        response.put("updateTime", updatedUser.getUpdatedAt());

        return ApiResponse.success("昵称更新成功", response);
    }
    /**
     * 用户头像更新。
     *
     * @param request 请求体参数
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "用户头像更新")
    @PostMapping(value = "/avatar/upload", consumes = "application/json")
    public ApiResponse<Void> updateAvatar(@Valid @RequestBody AvatarUpdateRequest request, Authentication authentication){
        String username = authentication.getName();
        userService.updateAvatar(username, request.getAvatarUrl());
        return ApiResponse.success("头像更新成功", null);
    }

    /**
     * 上传头像文件并更新用户头像。
     *
     * 不改变现有业务逻辑：仍然沿用 `/api/user/avatar/upload`，只是增加对 multipart 的支持。
     *
     * @param file 头像文件（form field: file）
     * @param authentication 当前登录用户认证信息
     * @return 返回可访问的头像 URL
     */
    @Operation(summary = "上传头像文件并更新用户头像", description = "上传图片文件，服务端保存到本地磁盘并返回可访问 URL（/avatar/**）")
    @PostMapping(value = "/avatar/upload", consumes = "multipart/form-data")
    public ApiResponse<Map<String, Object>> uploadAvatarFile(
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        String username = authentication.getName();
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String avatarUrl = userService.uploadAvatar(username, file, baseUrl);
        Map<String, Object> resp = new HashMap<>();
        resp.put("avatarUrl", avatarUrl);
        return ApiResponse.success("头像更新成功", resp);
    }
    /**
     * 用户密码更新。
     *
     * @param request 请求体参数
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "用户密码更新")
    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request, Authentication authentication){
        String username = authentication.getName();
        userService.updatePassword(username, request.getOldPassword(), request.getNewPassword());
        return ApiResponse.success("密码修改成功", null);
    }
    /**
     * 用户邮箱绑定。
     *
     * @param request 请求体参数
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "用户邮箱绑定")
    @PutMapping("/email")
    public ApiResponse<Void> bindEmail(@Valid @RequestBody EmailBindRequest request, Authentication authentication){
        String username = authentication.getName();
        userService.bindEmail(username, request.getEmail());
        return ApiResponse.success("邮箱绑定成功", null);
    }
    /**
     * 用户手机号绑定。
     *
     * @param request 请求体参数
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "用户手机号绑定")
    @PutMapping("/phone")
    public ApiResponse<Void> bindPhone(@Valid @RequestBody PhoneBindRequest request, Authentication authentication){
        String username = authentication.getName();
        userService.bindPhone(username, request.getPhone());
        return ApiResponse.success("手机号绑定成功", null);
    }
    /**
     * 更新用户状态。
     *
     * @param request 请求体参数
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "更新用户状态")
    @PutMapping("/status")
    public ApiResponse<Void> updateStatus(@Valid @RequestBody StatusUpdateRequest request, Authentication authentication){
        String username = authentication.getName();
        userService.updateStatus(username, request.getStatus());
        return ApiResponse.success("更新用户状态成功", null);
    }
    /**
     * 更新用户类型。
     *
     * @param request 请求体参数
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "更新用户类型")
    @PutMapping("/userType")
    public ApiResponse<Void> updateUserType(@Valid @RequestBody UserTypeUpdateRequest request, Authentication authentication){
        String username = authentication.getName();
        userService.updateUserType(username, request.getUserType());
        return ApiResponse.success("更新用户类型成功", null);
    }
    /**
     * 更新用户性别。
     *
     * @param request 请求体参数
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "更新用户性别")
    @PutMapping("/gender")
    public ApiResponse<Void> updateGender(@Valid @RequestBody GenderUpdateRequest request, Authentication authentication){
        String username = authentication.getName();
        userService.updateGender(username, request.getGender());
        return ApiResponse.success("更新用户性别成功", null);
    }
    /**
     * 获取用户列表。
     *
     * @param page 页码
     * @param size 返回数量
     * @return 统一响应体
     */
    @Operation(summary = "获取用户列表")
    @GetMapping("/list")
    public ApiResponse<List<User>> getUserList(@RequestParam int page, @RequestParam int size){
        List<User> userList = userService.findAll(page, size);
        return ApiResponse.success("获取用户列表成功", userList);
    }
    /**
     * 删除用户（根据username）。
     *
     * @param username 用户名
     * @return 统一响应体
     */
    @Operation(summary = "删除用户（根据username）")
    @DeleteMapping("/delete/{username}")
    public ApiResponse<Void> deleteUser(@PathVariable String username){
        userService.deleteByUsername(username);
        return ApiResponse.success("删除用户成功", null);
    }
    /**
     * 更新用户昵称（指定用户名）。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "更新用户昵称（指定用户名）",
            description = "根据用户名更新用户昵称，支持管理员操作")
    @PutMapping("/update/nickname")
    public ApiResponse<Map<String, Object>> updateNicknameByUsername(@Valid @RequestBody User request){
        User updatedUser = userService.updateNicknameByUsername(request.getUsername(), request.getNickname());

        Map<String, Object> response = new HashMap<>();
        response.put("username", updatedUser.getUsername());
        response.put("nickname", updatedUser.getNickname());

        return ApiResponse.success("更新成功", response);
    }
    /**
     * 更新用户邮箱（指定用户名）。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "更新用户邮箱（指定用户名）",
            description = "根据用户名更新用户邮箱，支持管理员操作")
    @PutMapping("/update/email")
    public ApiResponse<Map<String, Object>> updateEmailByUsername(@Valid @RequestBody User request){
        User updatedUser = userService.updateEmailByUsername(request.getUsername(), request.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("username", updatedUser.getUsername());
        response.put("email", updatedUser.getEmail());

        return ApiResponse.success("更新成功", response);
    }
    /**
     * 更新用户手机号（指定用户名）。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "更新用户手机号（指定用户名）",
            description = "根据用户名更新用户手机号，支持管理员操作")
    @PutMapping("/update/phone")
    public ApiResponse<Map<String, Object>> updatePhoneByUsername(@Valid @RequestBody User request){
        User updatedUser = userService.updatePhoneByUsername(request.getUsername(), request.getPhone());

        Map<String, Object> response = new HashMap<>();
        response.put("username", updatedUser.getUsername());
        response.put("phone", updatedUser.getPhone());

        return ApiResponse.success("更新成功", response);
    }
    /**
     * 更新用户性别（指定用户名）。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "更新用户性别（指定用户名）",
            description = "根据用户名更新用户性别，支持 MALE/FEMALE/OTHER 格式")
    @PutMapping("/update/gender")
    public ApiResponse<Map<String, Object>> updateGenderByUsername(@Valid @RequestBody User request){
        User updatedUser = userService.updateGenderByUsername(request.getUsername(), request.getGender());

        Map<String, Object> response = new HashMap<>();
        response.put("username", updatedUser.getUsername());
        response.put("gender", request.getGender()); // 返回原始字符串格式

        return ApiResponse.success("更新成功", response);
    }
    /**
     * 更新用户类型（指定用户名）。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "更新用户类型（指定用户名）",
            description = "根据用户名更新用户类型，支持管理员操作")
    @PutMapping("/update/userType")
    public ApiResponse<Map<String, Object>> updateUserTypeByUsername(@Valid @RequestBody User request){
        User updatedUser = userService.updateUserTypeByUsername(request.getUsername(), request.getUserType());

        Map<String, Object> response = new HashMap<>();
        response.put("username", updatedUser.getUsername());
        response.put("userType", updatedUser.getUserType());

        return ApiResponse.success("更新成功", response);
    }
    /**
     * 更新用户状态（指定用户名）。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "更新用户状态（指定用户名）",
            description = "根据用户名更新用户状态，支持管理员操作")
    @PutMapping("/update/status")
    public ApiResponse<Map<String, Object>> updateStatusByUsername(@Valid @RequestBody User request){
        User updatedUser = userService.updateStatusByUsername(request.getUsername(), request.getStatus());

        Map<String, Object> response = new HashMap<>();
        response.put("username", updatedUser.getUsername());
        response.put("status", updatedUser.getStatus());

        return ApiResponse.success("更新成功", response);
    }

} 

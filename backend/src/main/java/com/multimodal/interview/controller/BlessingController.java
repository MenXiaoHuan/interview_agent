package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.dto.BlessingCreateRequest;
import com.multimodal.interview.dto.BlessingResponse;
import com.multimodal.interview.dto.BlessingUpdateRequest;
import com.multimodal.interview.entity.Blessing;
import com.multimodal.interview.entity.User;
import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.mapper.UserMapper;
import com.multimodal.interview.service.BlessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 祝福语管理接口控制器。
 */
@Tag(name = "祝福语", description = "祝福语增删改查接口")
@RestController
@RequestMapping("/api/blessings")
public class BlessingController {
    private final BlessingService blessingService;
    private final UserMapper userMapper;

    public BlessingController(BlessingService blessingService, UserMapper userMapper) {
        this.blessingService = blessingService;
        this.userMapper = userMapper;
    }
    /**
     * 创建祝福语。
     *
     * @param request 请求体参数
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "创建祝福语")
    @PostMapping
    public ApiResponse<Blessing> create(@Valid @RequestBody BlessingCreateRequest request, Authentication authentication){
        User user = userMapper.findByUsername(authentication.getName());
        if (user.getUserType() != 3) throw BusinessException.forbidden("无权限");
        Blessing b = blessingService.create(user.getId(), request);
        return ApiResponse.success("创建成功", b);
    }
    /**
     * 更新祝福语。
     *
     * @param id 主键 ID
     * @param request 请求体参数
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "更新祝福语")
    @PutMapping("/{id}")
    public ApiResponse<Blessing> update(@PathVariable Long id, @Valid @RequestBody BlessingUpdateRequest request, Authentication authentication){
        User user = userMapper.findByUsername(authentication.getName());
        if (user.getUserType() != 3) throw BusinessException.forbidden("无权限");
        Blessing b = blessingService.update(id, request);
        return ApiResponse.success("更新成功", b);
    }
    /**
     * 删除祝福语。
     *
     * @param id 主键 ID
     * @param authentication 当前登录用户认证信息
     * @return 统一响应体
     */
    @Operation(summary = "删除祝福语")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication authentication){
        User user = userMapper.findByUsername(authentication.getName());
        if (user.getUserType() != 3) throw BusinessException.forbidden("无权限");
        blessingService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
    /**
     * 获取祝福语列表。
     *
     * @param status 状态筛选条件
     * @param type 类型筛选条件
     * @return 统一响应体
     */
    @Operation(summary = "获取祝福语列表")
    @GetMapping
    public ApiResponse<List<BlessingResponse>> list(@RequestParam(required = false) String status,
                                                    @RequestParam(required = false) String type){
        return ApiResponse.success(blessingService.list(status, type));
    }
    /**
     * 获取祝福语详情。
     *
     * @param id 主键 ID
     * @return 统一响应体
     */
    @Operation(summary = "获取祝福语详情")
    @GetMapping("/{id}")
    public ApiResponse<Blessing> get(@PathVariable Long id){
        return ApiResponse.success(blessingService.get(id));
    }
}


package com.multimodal.interview.service.impl;

import com.multimodal.interview.entity.User;
import com.multimodal.interview.mapper.UserMapper;
import com.multimodal.interview.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplAvatarUploadTest {

    @Test
    void uploadsAvatarThroughStorageServiceAndSavesProxyUrl() {
        UserMapper userMapper = mock(UserMapper.class);
        SensitiveWordServiceImpl sensitiveWordService = mock(SensitiveWordServiceImpl.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        StorageService storageService = mock(StorageService.class);
        UserServiceImpl userService = new UserServiceImpl(
                userMapper,
                sensitiveWordService,
                passwordEncoder,
                storageService
        );
        User user = new User();
        user.setId(7L);
        user.setUsername("alice");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        when(userMapper.findByUsername("alice")).thenReturn(user);
        when(storageService.uploadAvatar(eq(7L), any())).thenReturn("/api/avatar?object=avatar%2Fuser-7.png");
        when(userMapper.updateAvatar(7L, "/api/avatar?object=avatar%2Fuser-7.png")).thenReturn(1);

        String avatarUrl = userService.uploadAvatar("alice", file);

        assertThat(avatarUrl).isEqualTo("/api/avatar?object=avatar%2Fuser-7.png");
        verify(storageService).uploadAvatar(7L, file);
        verify(userMapper).updateAvatar(7L, "/api/avatar?object=avatar%2Fuser-7.png");
    }
}

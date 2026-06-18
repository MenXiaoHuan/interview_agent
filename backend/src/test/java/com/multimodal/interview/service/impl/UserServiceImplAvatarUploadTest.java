package com.multimodal.interview.service.impl;

import com.multimodal.interview.entity.User;
import com.multimodal.interview.mapper.UserMapper;
import com.multimodal.interview.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplAvatarUploadTest {

    @Test
    void uploadsAvatarThroughStorageServiceAndPersistsReturnedUrl() {
        UserMapper userMapper = mock(UserMapper.class);
        SensitiveWordServiceImpl sensitiveWordService = mock(SensitiveWordServiceImpl.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        StorageService storageService = mock(StorageService.class);
        UserServiceImpl service = new UserServiceImpl(userMapper, sensitiveWordService, passwordEncoder, storageService);
        User user = new User();
        user.setId(7L);
        user.setUsername("alice");
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "demo".getBytes());
        when(userMapper.findByUsername("alice")).thenReturn(user);
        when(storageService.uploadAvatar(7L, file)).thenReturn("http://localhost:9000/interview-agent/avatar/7/demo.png");
        when(userMapper.updateAvatar(7L, "http://localhost:9000/interview-agent/avatar/7/demo.png")).thenReturn(1);

        String avatarUrl = service.uploadAvatar("alice", file);

        assertThat(avatarUrl).isEqualTo("http://localhost:9000/interview-agent/avatar/7/demo.png");
        verify(storageService).uploadAvatar(7L, file);
        verify(userMapper).updateAvatar(7L, "http://localhost:9000/interview-agent/avatar/7/demo.png");
    }
}

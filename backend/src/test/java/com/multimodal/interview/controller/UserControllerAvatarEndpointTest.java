package com.multimodal.interview.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerAvatarEndpointTest {

    @Test
    void avatarUploadEndpointDoesNotAcceptJsonUrlUpdates() {
        boolean hasJsonAvatarUpload = Arrays.stream(UserController.class.getDeclaredMethods())
                .map(method -> method.getAnnotation(PostMapping.class))
                .filter(mapping -> mapping != null)
                .anyMatch(mapping -> Arrays.asList(mapping.value()).contains("/avatar/upload")
                        && Arrays.asList(mapping.consumes()).contains("application/json"));

        assertThat(hasJsonAvatarUpload).isFalse();
    }
}

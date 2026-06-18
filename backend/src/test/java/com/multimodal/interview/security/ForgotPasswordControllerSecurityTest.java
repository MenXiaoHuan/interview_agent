package com.multimodal.interview.security;

import com.multimodal.interview.controller.ForgotPasswordController;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ForgotPasswordControllerSecurityTest {

    @Test
    void controllerDoesNotExposeGetEndpointsThatReturnResetCode() {
        boolean hasGetMapping = Arrays.stream(ForgotPasswordController.class.getDeclaredMethods())
                .anyMatch(method -> method.isAnnotationPresent(GetMapping.class));

        assertThat(hasGetMapping).isFalse();
    }
}

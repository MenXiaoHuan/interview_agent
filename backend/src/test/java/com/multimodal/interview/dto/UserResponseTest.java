package com.multimodal.interview.dto;

import com.multimodal.interview.entity.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseTest {

    @Test
    void excludesPasswordFromUserResponse() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("secret");

        UserResponse response = UserResponse.from(user);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("alice");
        assertThat(UserResponse.class.getDeclaredFields())
                .extracting("name")
                .doesNotContain("password");
    }
}

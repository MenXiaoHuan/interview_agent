package com.multimodal.interview.security;

import com.multimodal.interview.common.filter.JwtAuthenticationFilter;
import com.multimodal.interview.entity.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtRoleMappingTest {

    @Test
    void mapsUserTypeThreeToAdminRole() {
        User user = new User();
        user.setUserType(3);

        assertThat(JwtAuthenticationFilter.resolveAuthorities(user))
                .extracting(Object::toString)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void mapsOtherUserTypesToUserRole() {
        User user = new User();
        user.setUserType(1);

        assertThat(JwtAuthenticationFilter.resolveAuthorities(user))
                .extracting(Object::toString)
                .containsExactly("ROLE_USER");
    }
}

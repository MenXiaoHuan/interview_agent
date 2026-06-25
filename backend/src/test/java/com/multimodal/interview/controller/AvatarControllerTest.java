package com.multimodal.interview.controller;

import com.multimodal.interview.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AvatarControllerTest {

    @Test
    void avatarEndpointIsUnderApiPrefix() throws Exception {
        RequestMapping classMapping = AvatarController.class.getAnnotation(RequestMapping.class);
        Method method = AvatarController.class.getDeclaredMethod("getAvatar", String.class);
        GetMapping getMapping = method.getAnnotation(GetMapping.class);

        assertThat(classMapping.value()).containsExactly("/api");
        assertThat(getMapping.value()).containsExactly("/avatar");
    }

    @Test
    void getAvatarReturnsImageBytesFromStorageService() {
        StorageService storageService = mock(StorageService.class);
        AvatarController controller = new AvatarController(storageService);
        StorageService.StoredObject storedObject = new StorageService.StoredObject(
                new byte[]{1, 2, 3},
                "image/png"
        );

        when(storageService.readObject("avatar/user-7/avatar.png")).thenReturn(storedObject);

        var response = controller.getAvatar("avatar/user-7/avatar.png");

        assertThat(response.getBody()).containsExactly(1, 2, 3);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("image/png");
        assertThat(response.getHeaders().getCacheControl())
                .isEqualTo(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic().getHeaderValue());
        verify(storageService).readObject("avatar/user-7/avatar.png");
    }
}

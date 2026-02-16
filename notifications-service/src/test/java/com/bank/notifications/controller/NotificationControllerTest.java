package com.bank.notifications.controller;

import com.bank.notifications.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void send_withValidScope_returnsOk() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt.claim("scope", "notifications")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"ivanov\", \"message\": \"Тест\"}"))
                .andExpect(status().isOk());

        verify(notificationService).send(any());
    }

    @Test
    void send_withoutAuth_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"ivanov\", \"message\": \"Тест\"}"))
                .andExpect(status().isForbidden());
    }
}

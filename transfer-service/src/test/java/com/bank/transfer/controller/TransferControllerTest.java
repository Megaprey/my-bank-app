package com.bank.transfer.controller;

import com.bank.transfer.dto.TransferResponseDto;
import com.bank.transfer.service.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void transfer_success() throws Exception {
        when(transferService.transfer(eq("ivanov"), eq("petrov"), any(BigDecimal.class)))
                .thenReturn(new TransferResponseDto(
                        "Успешно переведено 100 руб клиенту Петров Петр",
                        new BigDecimal("0.00")));

        mockMvc.perform(post("/api/transfer")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt.claim("scope", "transfer")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromUsername\": \"ivanov\", \"toUsername\": \"petrov\", \"amount\": 100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Успешно переведено 100 руб клиенту Петров Петр"));
    }

    @Test
    void transfer_insufficientFunds_returnsBadRequest() throws Exception {
        when(transferService.transfer(eq("ivanov"), eq("petrov"), any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Недостаточно средств на счету"));

        mockMvc.perform(post("/api/transfer")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt.claim("scope", "transfer")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromUsername\": \"ivanov\", \"toUsername\": \"petrov\", \"amount\": 500}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Недостаточно средств на счету"));
    }

    @Test
    void transfer_withoutAuth_returns401or403() throws Exception {
        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromUsername\": \"ivanov\", \"toUsername\": \"petrov\", \"amount\": 100}"))
                .andExpect(status().isForbidden());
    }
}

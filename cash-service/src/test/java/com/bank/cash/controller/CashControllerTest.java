package com.bank.cash.controller;

import com.bank.cash.dto.CashResponseDto;
import com.bank.cash.service.CashService;
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

@WebMvcTest(CashController.class)
class CashControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CashService cashService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void deposit_returnsSuccess() throws Exception {
        when(cashService.deposit(eq("ivanov"), any(BigDecimal.class)))
                .thenReturn(new CashResponseDto("Положено 100 руб", new BigDecimal("200.00")));

        mockMvc.perform(post("/api/cash/deposit")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt.claim("scope", "cash")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"ivanov\", \"amount\": 100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Положено 100 руб"))
                .andExpect(jsonPath("$.newBalance").value(200.00));
    }

    @Test
    void withdraw_insufficientFunds_returnsBadRequest() throws Exception {
        when(cashService.withdraw(eq("ivanov"), any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Недостаточно средств на счету"));

        mockMvc.perform(post("/api/cash/withdraw")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt.claim("scope", "cash")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"ivanov\", \"amount\": 500}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Недостаточно средств на счету"));
    }
}

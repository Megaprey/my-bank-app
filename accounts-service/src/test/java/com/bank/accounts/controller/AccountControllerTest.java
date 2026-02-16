package com.bank.accounts.controller;

import com.bank.accounts.client.NotificationClient;
import com.bank.accounts.dto.AccountDto;
import com.bank.accounts.dto.AccountShortDto;
import com.bank.accounts.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void getMyAccount_withValidJwt_returnsAccount() throws Exception {
        AccountDto dto = new AccountDto(1L, "ivanov", "Иванов Иван",
                LocalDate.of(2001, 1, 1), new BigDecimal("100.00"));
        when(accountService.getAccount("ivanov")).thenReturn(dto);

        mockMvc.perform(get("/api/accounts/me")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt.claim("username", "ivanov")
                                        .claim("scope", "accounts"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ivanov"))
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void getOtherAccounts_returnsListWithoutCurrent() throws Exception {
        when(accountService.getOtherAccounts("ivanov"))
                .thenReturn(List.of(new AccountShortDto("petrov", "Петров Петр")));

        mockMvc.perform(get("/api/accounts/others")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt.claim("username", "ivanov")
                                        .claim("scope", "accounts"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("petrov"));
    }

    @Test
    void deposit_withValidAmount_returnsUpdatedAccount() throws Exception {
        AccountDto dto = new AccountDto(1L, "ivanov", "Иванов Иван",
                LocalDate.of(2001, 1, 1), new BigDecimal("200.00"));
        when(accountService.deposit(eq("ivanov"), any(BigDecimal.class))).thenReturn(dto);

        mockMvc.perform(put("/api/accounts/ivanov/deposit")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt.claim("scope", "accounts")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(200.00));
    }

    @Test
    void withdraw_insufficientFunds_returnsBadRequest() throws Exception {
        when(accountService.withdraw(eq("ivanov"), any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Недостаточно средств на счету"));

        mockMvc.perform(put("/api/accounts/ivanov/withdraw")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt.claim("scope", "accounts")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 500}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Недостаточно средств на счету"));
    }

    @Test
    void anyEndpoint_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/accounts/me"))
                .andExpect(status().isUnauthorized());
    }
}

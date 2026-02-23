package com.bank.accounts.contract;

import com.bank.accounts.client.NotificationClient;
import com.bank.accounts.service.AccountService;
import com.bank.api.dto.AccountDto;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureMessageVerifier
@ActiveProfiles("contract-test")
abstract class AccountContractBase {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AccountService accountService;

    @MockBean
    NotificationClient notificationClient;

    @MockBean
    JwtDecoder jwtDecoder;

    @MockBean
    ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    OAuth2AuthorizedClientRepository authorizedClientRepository;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        when(accountService.deposit(eq("ivanov"), any(BigDecimal.class)))
                .thenReturn(new AccountDto(1L, "ivanov", "Иванов Иван",
                        LocalDate.of(2001, 1, 1), new BigDecimal("200.00")));
        when(accountService.withdraw(eq("ivanov"), any(BigDecimal.class)))
                .thenReturn(new AccountDto(1L, "ivanov", "Иванов Иван",
                        LocalDate.of(2001, 1, 1), new BigDecimal("50.00")));
        when(accountService.withdraw(eq("ivanov"), eq(new BigDecimal("500"))))
                .thenThrow(new IllegalArgumentException("Недостаточно средств на счету"));
    }
}

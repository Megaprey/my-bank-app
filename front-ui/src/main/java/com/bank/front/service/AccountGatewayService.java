package com.bank.front.service;

import com.bank.front.dto.AccountDto;
import com.bank.front.dto.AccountShortDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AccountGatewayService {

    private final WebClient webClient;

    public AccountGatewayService(WebClient webClient) {
        this.webClient = webClient;
    }

    public AccountDto getMyAccount() {
        return webClient.get()
                .uri("/api/accounts/me")
                .retrieve()
                .bodyToMono(AccountDto.class)
                .block();
    }

    public AccountDto updateAccount(String fullName, LocalDate birthDate) {
        return webClient.put()
                .uri("/api/accounts/me")
                .bodyValue(Map.of("fullName", fullName, "birthDate", birthDate.toString()))
                .retrieve()
                .bodyToMono(AccountDto.class)
                .block();
    }

    public List<AccountShortDto> getOtherAccounts() {
        try {
            return webClient.get()
                    .uri("/api/accounts/others")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AccountShortDto>>() {})
                    .block();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}

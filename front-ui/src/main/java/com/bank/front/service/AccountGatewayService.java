package com.bank.front.service;

import com.bank.api.dto.AccountDto;
import com.bank.api.dto.AccountShortDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class AccountGatewayService {

    private static final Logger log = LoggerFactory.getLogger(AccountGatewayService.class);

    private final WebClient webClient;

    public AccountGatewayService(@Qualifier("accountsWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public AccountDto getMyAccount() {
        log.debug("[AccountGatewayService.getMyAccount] requesting account");
        AccountDto result = webClient.get()
                .uri("/api/accounts/me")
                .retrieve()
                .bodyToMono(AccountDto.class)
                .block();
        log.info("[AccountGatewayService.getMyAccount] success");
        return result;
    }

    public AccountDto updateAccount(String fullName, LocalDate birthDate) {
        log.debug("[AccountGatewayService.updateAccount] fullName={}, birthDate={}", fullName, birthDate);
        AccountDto result = webClient.put()
                .uri("/api/accounts/me")
                .bodyValue(new AccountUpdateRequest(fullName, birthDate))
                .retrieve()
                .bodyToMono(AccountDto.class)
                .block();
        log.info("[AccountGatewayService.updateAccount] success");
        return result;
    }

    private record AccountUpdateRequest(String fullName, LocalDate birthDate) {}

    public List<AccountShortDto> getOtherAccounts() {
        log.debug("[AccountGatewayService.getOtherAccounts] requesting other accounts");
        try {
            List<AccountShortDto> result = webClient.get()
                    .uri("/api/accounts/others")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AccountShortDto>>() {})
                    .block();
            log.info("[AccountGatewayService.getOtherAccounts] success, count={}", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("[AccountGatewayService.getOtherAccounts] failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}

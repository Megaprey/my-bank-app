package com.bank.transfer.client;

import com.bank.api.dto.AccountDto;
import com.bank.api.dto.BalanceOperationDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;

@Component
public class AccountClient {

    private static final Logger log = LoggerFactory.getLogger(AccountClient.class);

    private final WebClient webClient;
    private final String accountsUrl;

    public AccountClient(WebClient webClient,
                         @Value("${services.accounts.url:http://localhost:8081}") String accountsUrl) {
        this.webClient = webClient;
        this.accountsUrl = accountsUrl;
    }

    @CircuitBreaker(name = "accountClient", fallbackMethod = "depositFallback")
    @Retry(name = "accountClient")
    public AccountDto deposit(String username, BigDecimal amount) {
        log.debug("[AccountClient.deposit] username={}, amount={}", username, amount);
        try {
            AccountDto result = webClient.put()
                    .uri(accountsUrl + "/api/accounts/{username}/deposit", username)
                    .bodyValue(new BalanceOperationDto(amount))
                    .retrieve()
                    .bodyToMono(AccountDto.class)
                    .block();
            log.info("[AccountClient.deposit] success, newBalance={}", result != null ? result.balance() : null);
            return result;
        } catch (WebClientResponseException e) {
            log.error("[AccountClient.deposit] failed username={}, amount={}, status={}",
                    username, amount, e.getStatusCode());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    private AccountDto depositFallback(String username, BigDecimal amount, Exception e) {
        log.warn("[AccountClient.deposit] fallback triggered for username={}, error={}", username, e.getMessage());
        throw new RuntimeException("Сервис аккаунтов недоступен. Попробуйте позже.", e);
    }

    @CircuitBreaker(name = "accountClient", fallbackMethod = "withdrawFallback")
    @Retry(name = "accountClient")
    public AccountDto withdraw(String username, BigDecimal amount) {
        log.debug("[AccountClient.withdraw] username={}, amount={}", username, amount);
        try {
            AccountDto result = webClient.put()
                    .uri(accountsUrl + "/api/accounts/{username}/withdraw", username)
                    .bodyValue(new BalanceOperationDto(amount))
                    .retrieve()
                    .bodyToMono(AccountDto.class)
                    .block();
            log.info("[AccountClient.withdraw] success, newBalance={}", result != null ? result.balance() : null);
            return result;
        } catch (WebClientResponseException.BadRequest e) {
            log.error("[AccountClient.withdraw] insufficient funds username={}, amount={}", username, amount);
            throw new IllegalArgumentException("Недостаточно средств на счету");
        } catch (WebClientResponseException e) {
            log.error("[AccountClient.withdraw] failed username={}, amount={}, status={}",
                    username, amount, e.getStatusCode());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    private AccountDto withdrawFallback(String username, BigDecimal amount, Exception e) {
        log.warn("[AccountClient.withdraw] fallback triggered for username={}, error={}", username, e.getMessage());
        throw new RuntimeException("Сервис аккаунтов недоступен. Попробуйте позже.", e);
    }
}

package com.bank.transfer.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class AccountClient {

    private final WebClient webClient;

    public AccountClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Map<String, Object> deposit(String username, BigDecimal amount) {
        return webClient.put()
                .uri("http://accounts-service/api/accounts/{username}/deposit", username)
                .bodyValue(Map.of("amount", amount))
                .retrieve()
                .bodyToMono(Map.class)
                .map(m -> (Map<String, Object>) m)
                .block();
    }

    public Map<String, Object> withdraw(String username, BigDecimal amount) {
        try {
            return webClient.put()
                    .uri("http://accounts-service/api/accounts/{username}/withdraw", username)
                    .bodyValue(Map.of("amount", amount))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(m -> (Map<String, Object>) m)
                    .block();
        } catch (WebClientResponseException.BadRequest e) {
            throw new IllegalArgumentException("Недостаточно средств на счету");
        }
    }
}

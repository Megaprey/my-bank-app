package com.bank.front.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class CashGatewayService {

    private final WebClient webClient;

    public CashGatewayService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Map<String, Object> deposit(String username, BigDecimal amount) {
        return webClient.post()
                .uri("/api/cash/deposit")
                .bodyValue(Map.of("username", username, "amount", amount))
                .retrieve()
                .bodyToMono(Map.class)
                .map(m -> (Map<String, Object>) m)
                .block();
    }

    public Map<String, Object> withdraw(String username, BigDecimal amount) {
        try {
            return webClient.post()
                    .uri("/api/cash/withdraw")
                    .bodyValue(Map.of("username", username, "amount", amount))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(m -> (Map<String, Object>) m)
                    .block();
        } catch (WebClientResponseException.BadRequest e) {
            throw new IllegalArgumentException("Недостаточно средств на счету");
        }
    }
}

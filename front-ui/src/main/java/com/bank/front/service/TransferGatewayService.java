package com.bank.front.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class TransferGatewayService {

    private final WebClient webClient;

    public TransferGatewayService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Map<String, Object> transfer(String fromUsername, String toUsername, BigDecimal amount) {
        try {
            return webClient.post()
                    .uri("/api/transfer")
                    .bodyValue(Map.of(
                            "fromUsername", fromUsername,
                            "toUsername", toUsername,
                            "amount", amount
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(m -> (Map<String, Object>) m)
                    .block();
        } catch (WebClientResponseException.BadRequest e) {
            throw new IllegalArgumentException("Недостаточно средств на счету");
        }
    }
}

package com.bank.front.service;

import com.bank.front.dto.CashResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;

@Service
public class CashGatewayService {

    private static final Logger log = LoggerFactory.getLogger(CashGatewayService.class);

    private final WebClient webClient;

    public CashGatewayService(@Qualifier("cashWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public CashResponseDto deposit(String username, BigDecimal amount) {
        log.debug("[CashGatewayService.deposit] username={}, amount={}", username, amount);
        CashResponseDto result = webClient.post()
                .uri("/api/cash/deposit")
                .bodyValue(new CashRequest(username, amount))
                .retrieve()
                .bodyToMono(CashResponseDto.class)
                .block();
        log.info("[CashGatewayService.deposit] success");
        return result;
    }

    public CashResponseDto withdraw(String username, BigDecimal amount) {
        log.debug("[CashGatewayService.withdraw] username={}, amount={}", username, amount);
        try {
            CashResponseDto result = webClient.post()
                    .uri("/api/cash/withdraw")
                    .bodyValue(new CashRequest(username, amount))
                    .retrieve()
                    .bodyToMono(CashResponseDto.class)
                    .block();
            log.info("[CashGatewayService.withdraw] success");
            return result;
        } catch (WebClientResponseException.BadRequest e) {
            log.error("[CashGatewayService.withdraw] failed username={}, amount={}", username, amount);
            throw new IllegalArgumentException("Недостаточно средств на счету");
        }
    }

    private record CashRequest(String username, BigDecimal amount) {}
}

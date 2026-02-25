package com.bank.front.service;

import com.bank.front.dto.TransferResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;

@Service
public class TransferGatewayService {

    private static final Logger log = LoggerFactory.getLogger(TransferGatewayService.class);

    private final WebClient webClient;

    public TransferGatewayService(@Qualifier("transferWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public TransferResponseDto transfer(String fromUsername, String toUsername, BigDecimal amount) {
        log.debug("[TransferGatewayService.transfer] from={}, to={}, amount={}", fromUsername, toUsername, amount);
        try {
            TransferResponseDto result = webClient.post()
                    .uri("/api/transfer")
                    .bodyValue(new TransferRequest(fromUsername, toUsername, amount))
                    .retrieve()
                    .bodyToMono(TransferResponseDto.class)
                    .block();
            log.info("[TransferGatewayService.transfer] success");
            return result;
        } catch (WebClientResponseException.BadRequest e) {
            log.error("[TransferGatewayService.transfer] failed from={}, to={}, amount={}", fromUsername, toUsername, amount);
            throw new IllegalArgumentException("Недостаточно средств на счету");
        } catch (WebClientResponseException e) {
            log.error("[TransferGatewayService.transfer] failed from={}, to={}, amount={}, status={}",
                    fromUsername, toUsername, amount, e.getStatusCode());
            String body = e.getResponseBodyAsString();
            if (body != null && body.contains("\"error\"")) {
                int start = body.indexOf("\"error\":\"") + 9;
                int end = body.indexOf("\"", start);
                if (start > 8 && end > start) {
                    throw new IllegalStateException(body.substring(start, end));
                }
            }
            throw new IllegalStateException("Сервис перевода временно недоступен. Попробуйте позже.");
        }
    }

    private record TransferRequest(String fromUsername, String toUsername, BigDecimal amount) {}
}

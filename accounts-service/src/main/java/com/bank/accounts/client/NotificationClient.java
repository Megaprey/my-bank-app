package com.bank.accounts.client;

import com.bank.api.dto.NotificationDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final WebClient webClient;
    private final String notificationsUrl;

    public NotificationClient(WebClient webClient,
                              @Value("${services.notifications.url:http://localhost:8084}") String notificationsUrl) {
        this.webClient = webClient;
        this.notificationsUrl = notificationsUrl;
    }

    @CircuitBreaker(name = "notificationClient", fallbackMethod = "sendFallback")
    @Retry(name = "notificationClient")
    public void send(String username, String message) {
        webClient.post()
                .uri(notificationsUrl + "/api/notifications")
                .bodyValue(new NotificationDto(username, message))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @SuppressWarnings("unused")
    private void sendFallback(String username, String message, Exception e) {
        log.warn("[NotificationClient.send] fallback triggered, notification skipped: {}", e.getMessage());
    }
}

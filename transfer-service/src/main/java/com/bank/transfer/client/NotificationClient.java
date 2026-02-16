package com.bank.transfer.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final WebClient webClient;

    public NotificationClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public void send(String username, String message) {
        try {
            webClient.post()
                    .uri("http://notifications-service/api/notifications")
                    .bodyValue(Map.of("username", username, "message", message))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.warn("Не удалось отправить уведомление: {}", e.getMessage());
        }
    }
}

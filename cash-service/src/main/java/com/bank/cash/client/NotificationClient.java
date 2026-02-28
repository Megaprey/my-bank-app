package com.bank.cash.client;

import com.bank.api.dto.NotificationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);
    private static final String TOPIC = "bank-notifications";

    private final KafkaTemplate<String, NotificationDto> kafkaTemplate;

    public NotificationClient(KafkaTemplate<String, NotificationDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String username, String message) {
        var dto = new NotificationDto(username, message);
        kafkaTemplate.send(TOPIC, username, dto);
        log.debug("[NotificationClient.send] sent to Kafka topic={}, key={}", TOPIC, username);
    }
}

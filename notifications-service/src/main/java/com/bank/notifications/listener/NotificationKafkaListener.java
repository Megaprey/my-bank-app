package com.bank.notifications.listener;

import com.bank.api.dto.NotificationDto;
import com.bank.notifications.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class NotificationKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaListener.class);

    private final NotificationService notificationService;

    public NotificationKafkaListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "bank-notifications", groupId = "notifications-service")
    public void onMessage(NotificationDto dto, Acknowledgment acknowledgment) {
        log.debug("[NotificationKafkaListener.onMessage] received: username={}", dto.username());
        notificationService.send(dto);
        acknowledgment.acknowledge();
    }
}

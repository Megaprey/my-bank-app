package com.bank.notifications.service;

import com.bank.api.dto.NotificationDto;
import com.bank.notifications.model.Notification;
import com.bank.notifications.repository.NotificationRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final MeterRegistry meterRegistry;

    public NotificationService(NotificationRepository notificationRepository,
                               MeterRegistry meterRegistry) {
        this.notificationRepository = notificationRepository;
        this.meterRegistry = meterRegistry;
    }

    public void send(NotificationDto dto) {
        try {
            Notification notification = new Notification(dto.username(), dto.message());
            notificationRepository.save(notification);
            log.info("Notification sent: user={}, message={}", dto.username(), dto.message());
        } catch (Exception e) {
            Counter.builder("bank.notification.failures")
                    .tag("login", dto.username())
                    .description("Failed notification send attempts")
                    .register(meterRegistry)
                    .increment();
            log.error("Failed to send notification: user={}, error={}", dto.username(), e.getMessage());
            throw e;
        }
    }
}

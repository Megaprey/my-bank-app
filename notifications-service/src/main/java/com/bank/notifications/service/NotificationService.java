package com.bank.notifications.service;

import com.bank.notifications.dto.NotificationDto;
import com.bank.notifications.model.Notification;
import com.bank.notifications.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void send(NotificationDto dto) {
        Notification notification = new Notification(dto.getUsername(), dto.getMessage());
        notificationRepository.save(notification);
        log.warn("УВЕДОМЛЕНИЕ [{}]: {}", dto.getUsername(), dto.getMessage());
    }
}

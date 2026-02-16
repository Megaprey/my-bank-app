package com.bank.notifications.controller;

import com.bank.notifications.dto.NotificationDto;
import com.bank.notifications.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Void> send(@RequestBody NotificationDto dto) {
        notificationService.send(dto);
        return ResponseEntity.ok().build();
    }
}

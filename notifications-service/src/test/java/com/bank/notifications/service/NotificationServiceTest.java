package com.bank.notifications.service;

import com.bank.api.dto.NotificationDto;
import com.bank.notifications.model.Notification;
import com.bank.notifications.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void send_savesNotification() {
        NotificationDto dto = new NotificationDto("ivanov", "Пополнение счёта на 100 руб");

        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.send(dto);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals("ivanov", saved.getUsername());
        assertEquals("Пополнение счёта на 100 руб", saved.getMessage());
        assertNotNull(saved.getCreatedAt());
    }
}

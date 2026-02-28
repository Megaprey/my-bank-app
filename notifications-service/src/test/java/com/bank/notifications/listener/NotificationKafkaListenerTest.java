package com.bank.notifications.listener;

import com.bank.api.dto.NotificationDto;
import com.bank.notifications.model.Notification;
import com.bank.notifications.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "bank-notifications",
        brokerProperties = {"listeners=PLAINTEXT://localhost:0"})
@DirtiesContext
class NotificationKafkaListenerTest {

    @Autowired
    private KafkaTemplate<String, NotificationDto> kafkaTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void shouldConsumeNotificationFromKafka() {
        var dto = new NotificationDto("ivanov", "Пополнение счёта на 100 руб");

        kafkaTemplate.send("bank-notifications", dto.username(), dto);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findAll();
            assertFalse(notifications.isEmpty());
            Notification last = notifications.get(notifications.size() - 1);
            assertEquals("ivanov", last.getUsername());
            assertEquals("Пополнение счёта на 100 руб", last.getMessage());
        });
    }
}

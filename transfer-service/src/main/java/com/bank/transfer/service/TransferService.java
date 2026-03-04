package com.bank.transfer.service;

import com.bank.transfer.client.AccountClient;
import com.bank.transfer.client.NotificationClient;
import com.bank.transfer.dto.TransferResponseDto;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountClient accountClient;
    private final NotificationClient notificationClient;
    private final MeterRegistry meterRegistry;

    public TransferService(AccountClient accountClient, NotificationClient notificationClient,
                           MeterRegistry meterRegistry) {
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
        this.meterRegistry = meterRegistry;
    }

    public TransferResponseDto transfer(String fromUsername, String toUsername, BigDecimal amount) {
        log.info("[TransferService.transfer] START from={}, to={}, amount={}", fromUsername, toUsername, amount);

        BigDecimal newBalance;
        try {
            var senderAccount = accountClient.withdraw(fromUsername, amount);
            log.debug("[TransferService.transfer] withdraw OK, newBalance={}", senderAccount.balance());
            newBalance = senderAccount.balance();
        } catch (Exception e) {
            Counter.builder("bank.transfer.failures")
                    .tag("from_login", fromUsername)
                    .tag("to_login", toUsername)
                    .description("Failed money transfer attempts")
                    .register(meterRegistry)
                    .increment();
            log.warn("Transfer failed at withdraw stage: from={}, to={}, amount={}: {}",
                    fromUsername, toUsername, amount, e.getMessage());
            throw e;
        }

        try {
            var receiverAccount = accountClient.deposit(toUsername, amount);
            log.debug("[TransferService.transfer] deposit OK");
            String receiverName = receiverAccount.fullName();

            String message = "Успешно переведено " + amount.intValue() + " руб клиенту " + receiverName;
            notificationClient.send(fromUsername, message);
            notificationClient.send(toUsername, "Получен перевод " + amount.intValue() + " руб от " + fromUsername);

            log.info("[TransferService.transfer] END status=SUCCESS");
            return new TransferResponseDto(message, newBalance);
        } catch (Exception e) {
            Counter.builder("bank.transfer.failures")
                    .tag("from_login", fromUsername)
                    .tag("to_login", toUsername)
                    .description("Failed money transfer attempts")
                    .register(meterRegistry)
                    .increment();
            log.warn("[TransferService.transfer] COMPENSATE: deposit failed, refunding sender. Error: {}", e.getMessage());
            accountClient.deposit(fromUsername, amount);
            log.info("[TransferService.transfer] END status=FAILED_COMPENSATED");
            throw new IllegalStateException("Перевод отменён. Средства возвращены.", e);
        }
    }
}

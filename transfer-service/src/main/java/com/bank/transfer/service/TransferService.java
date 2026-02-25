package com.bank.transfer.service;

import com.bank.transfer.client.AccountClient;
import com.bank.transfer.client.NotificationClient;
import com.bank.transfer.dto.TransferResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountClient accountClient;
    private final NotificationClient notificationClient;

    public TransferService(AccountClient accountClient, NotificationClient notificationClient) {
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
    }

    public TransferResponseDto transfer(String fromUsername, String toUsername, BigDecimal amount) {
        log.info("[TransferService.transfer] START from={}, to={}, amount={}", fromUsername, toUsername, amount);

        var senderAccount = accountClient.withdraw(fromUsername, amount);
        log.debug("[TransferService.transfer] withdraw OK, newBalance={}", senderAccount.balance());
        BigDecimal newBalance = senderAccount.balance();

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
            log.warn("[TransferService.transfer] COMPENSATE: deposit failed, refunding sender. Error: {}", e.getMessage());
            accountClient.deposit(fromUsername, amount);
            log.info("[TransferService.transfer] END status=FAILED_COMPENSATED");
            throw new IllegalStateException("Перевод отменён. Средства возвращены.", e);
        }
    }
}

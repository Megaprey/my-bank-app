package com.bank.transfer.service;

import com.bank.transfer.client.AccountClient;
import com.bank.transfer.client.NotificationClient;
import com.bank.transfer.dto.TransferResponseDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class TransferService {

    private final AccountClient accountClient;
    private final NotificationClient notificationClient;

    public TransferService(AccountClient accountClient, NotificationClient notificationClient) {
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
    }

    public TransferResponseDto transfer(String fromUsername, String toUsername, BigDecimal amount) {
        Map<String, Object> senderAccount = accountClient.withdraw(fromUsername, amount);
        BigDecimal newBalance = new BigDecimal(senderAccount.get("balance").toString());

        Map<String, Object> receiverAccount = accountClient.deposit(toUsername, amount);
        String receiverName = (String) receiverAccount.get("fullName");

        String message = "Успешно переведено " + amount.intValue() + " руб клиенту " + receiverName;
        notificationClient.send(fromUsername, message);
        notificationClient.send(toUsername, "Получен перевод " + amount.intValue() + " руб от " + fromUsername);

        return new TransferResponseDto(message, newBalance);
    }
}

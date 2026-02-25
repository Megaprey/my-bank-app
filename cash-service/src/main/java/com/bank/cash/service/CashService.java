package com.bank.cash.service;

import com.bank.cash.client.AccountClient;
import com.bank.cash.client.NotificationClient;
import com.bank.cash.dto.CashResponseDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CashService {

    private final AccountClient accountClient;
    private final NotificationClient notificationClient;

    public CashService(AccountClient accountClient, NotificationClient notificationClient) {
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
    }

    public CashResponseDto deposit(String username, BigDecimal amount) {
        var account = accountClient.deposit(username, amount);
        String message = "Положено " + amount.intValue() + " руб";
        notificationClient.send(username, message);
        return new CashResponseDto(message, account.balance());
    }

    public CashResponseDto withdraw(String username, BigDecimal amount) {
        var account = accountClient.withdraw(username, amount);
        String message = "Снято " + amount.intValue() + " руб";
        notificationClient.send(username, message);
        return new CashResponseDto(message, account.balance());
    }
}

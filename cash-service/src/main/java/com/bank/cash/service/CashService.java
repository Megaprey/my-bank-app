package com.bank.cash.service;

import com.bank.cash.client.AccountClient;
import com.bank.cash.client.NotificationClient;
import com.bank.cash.dto.CashResponseDto;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CashService {

    private static final Logger log = LoggerFactory.getLogger(CashService.class);

    private final AccountClient accountClient;
    private final NotificationClient notificationClient;
    private final MeterRegistry meterRegistry;

    public CashService(AccountClient accountClient, NotificationClient notificationClient,
                       MeterRegistry meterRegistry) {
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
        this.meterRegistry = meterRegistry;
    }

    public CashResponseDto deposit(String username, BigDecimal amount) {
        var account = accountClient.deposit(username, amount);
        String message = "Положено " + amount.intValue() + " руб";
        notificationClient.send(username, message);
        return new CashResponseDto(message, account.balance());
    }

    public CashResponseDto withdraw(String username, BigDecimal amount) {
        try {
            var account = accountClient.withdraw(username, amount);
            String message = "Снято " + amount.intValue() + " руб";
            notificationClient.send(username, message);
            return new CashResponseDto(message, account.balance());
        } catch (Exception e) {
            Counter.builder("bank.cash.withdraw.failures")
                    .description("Failed cash withdrawal attempts")
                    .register(meterRegistry)
                    .increment();
            log.warn("Withdraw failed for user={}, amount={}: {}", username, amount, e.getMessage());
            throw e;
        }
    }
}

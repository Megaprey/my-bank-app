package com.bank.cash.service;

import com.bank.api.dto.AccountDto;
import com.bank.cash.client.AccountClient;
import com.bank.cash.client.NotificationClient;
import com.bank.cash.dto.CashResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashServiceTest {

    @Mock
    private AccountClient accountClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private CashService cashService;

    @Test
    void deposit_success() {
        when(accountClient.deposit("ivanov", new BigDecimal("100")))
                .thenReturn(new AccountDto(1L, "ivanov", "Иванов Иван", null, new BigDecimal("200")));

        CashResponseDto result = cashService.deposit("ivanov", new BigDecimal("100"));

        assertNotNull(result);
        assertEquals(0, new BigDecimal("200").compareTo(result.newBalance()));
        assertTrue(result.message().contains("100"));
        verify(notificationClient).send(eq("ivanov"), anyString());
    }

    @Test
    void withdraw_success() {
        when(accountClient.withdraw("ivanov", new BigDecimal("50")))
                .thenReturn(new AccountDto(1L, "ivanov", "Иванов Иван", null, new BigDecimal("50")));

        CashResponseDto result = cashService.withdraw("ivanov", new BigDecimal("50"));

        assertNotNull(result);
        assertEquals(0, new BigDecimal("50").compareTo(result.newBalance()));
        verify(notificationClient).send(eq("ivanov"), anyString());
    }

    @Test
    void withdraw_insufficientFunds_throwsException() {
        when(accountClient.withdraw("ivanov", new BigDecimal("500")))
                .thenThrow(new IllegalArgumentException("Недостаточно средств на счету"));

        assertThrows(IllegalArgumentException.class,
                () -> cashService.withdraw("ivanov", new BigDecimal("500")));
    }
}

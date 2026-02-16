package com.bank.cash.service;

import com.bank.cash.client.AccountClient;
import com.bank.cash.client.NotificationClient;
import com.bank.cash.dto.CashResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

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
                .thenReturn(Map.of("balance", 200.0, "username", "ivanov"));

        CashResponseDto result = cashService.deposit("ivanov", new BigDecimal("100"));

        assertNotNull(result);
        assertEquals(new BigDecimal("200.0"), result.getNewBalance());
        assertTrue(result.getMessage().contains("100"));
        verify(notificationClient).send(eq("ivanov"), anyString());
    }

    @Test
    void withdraw_success() {
        when(accountClient.withdraw("ivanov", new BigDecimal("50")))
                .thenReturn(Map.of("balance", 50.0, "username", "ivanov"));

        CashResponseDto result = cashService.withdraw("ivanov", new BigDecimal("50"));

        assertNotNull(result);
        assertEquals(new BigDecimal("50.0"), result.getNewBalance());
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

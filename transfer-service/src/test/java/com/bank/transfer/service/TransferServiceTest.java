package com.bank.transfer.service;

import com.bank.transfer.client.AccountClient;
import com.bank.transfer.client.NotificationClient;
import com.bank.transfer.dto.TransferResponseDto;
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
class TransferServiceTest {

    @Mock
    private AccountClient accountClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private TransferService transferService;

    @Test
    void transfer_success() {
        when(accountClient.withdraw("ivanov", new BigDecimal("100")))
                .thenReturn(Map.of("balance", 0.0, "username", "ivanov", "fullName", "Иванов Иван"));
        when(accountClient.deposit("petrov", new BigDecimal("100")))
                .thenReturn(Map.of("balance", 200.0, "username", "petrov", "fullName", "Петров Петр"));

        TransferResponseDto result = transferService.transfer("ivanov", "petrov", new BigDecimal("100"));

        assertNotNull(result);
        assertEquals(new BigDecimal("0.0"), result.getNewBalance());
        assertTrue(result.getMessage().contains("Петров Петр"));
        verify(notificationClient, times(2)).send(anyString(), anyString());
    }

    @Test
    void transfer_insufficientFunds_throwsException() {
        when(accountClient.withdraw("ivanov", new BigDecimal("500")))
                .thenThrow(new IllegalArgumentException("Недостаточно средств на счету"));

        assertThrows(IllegalArgumentException.class,
                () -> transferService.transfer("ivanov", "petrov", new BigDecimal("500")));
    }
}

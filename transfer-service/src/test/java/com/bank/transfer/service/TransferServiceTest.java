package com.bank.transfer.service;

import com.bank.api.dto.AccountDto;
import com.bank.transfer.client.AccountClient;
import com.bank.transfer.client.NotificationClient;
import com.bank.transfer.dto.TransferResponseDto;
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
                .thenReturn(new AccountDto(1L, "ivanov", "Иванов Иван", null, BigDecimal.ZERO));
        when(accountClient.deposit("petrov", new BigDecimal("100")))
                .thenReturn(new AccountDto(2L, "petrov", "Петров Петр", null, new BigDecimal("200")));

        TransferResponseDto result = transferService.transfer("ivanov", "petrov", new BigDecimal("100"));

        assertNotNull(result);
        assertEquals(0, BigDecimal.ZERO.compareTo(result.newBalance()));
        assertTrue(result.message().contains("Петров Петр"));
        verify(notificationClient, times(2)).send(anyString(), anyString());
    }

    @Test
    void transfer_insufficientFunds_throwsException() {
        when(accountClient.withdraw("ivanov", new BigDecimal("500")))
                .thenThrow(new IllegalArgumentException("Недостаточно средств на счету"));

        assertThrows(IllegalArgumentException.class,
                () -> transferService.transfer("ivanov", "petrov", new BigDecimal("500")));
    }

    @Test
    void transfer_depositFails_compensatesAndThrows() {
        when(accountClient.withdraw("ivanov", new BigDecimal("100")))
                .thenReturn(new AccountDto(1L, "ivanov", "Иванов Иван", null, BigDecimal.ZERO));
        when(accountClient.deposit("petrov", new BigDecimal("100")))
                .thenThrow(new RuntimeException("Service unavailable"));
        when(accountClient.deposit("ivanov", new BigDecimal("100")))
                .thenReturn(new AccountDto(1L, "ivanov", "Иванов Иван", null, new BigDecimal("100")));

        var ex = assertThrows(IllegalStateException.class,
                () -> transferService.transfer("ivanov", "petrov", new BigDecimal("100")));

        assertTrue(ex.getMessage().contains("Перевод отменён"));
        verify(accountClient).deposit("ivanov", new BigDecimal("100"));
    }
}

package com.bank.accounts.service;

import com.bank.accounts.dto.AccountDto;
import com.bank.accounts.dto.AccountUpdateDto;
import com.bank.accounts.model.Account;
import com.bank.accounts.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setUsername("ivanov");
        testAccount.setFullName("Иванов Иван");
        testAccount.setBirthDate(LocalDate.of(2001, 1, 1));
        testAccount.setBalance(new BigDecimal("100.00"));
    }

    @Test
    void getAccount_existingUser_returnsAccount() {
        when(accountRepository.findByUsername("ivanov")).thenReturn(Optional.of(testAccount));

        AccountDto dto = accountService.getAccount("ivanov");

        assertEquals("ivanov", dto.getUsername());
        assertEquals("Иванов Иван", dto.getFullName());
        assertEquals(new BigDecimal("100.00"), dto.getBalance());
    }

    @Test
    void updateAccount_validData_updatesSuccessfully() {
        when(accountRepository.findByUsername("ivanov")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        AccountUpdateDto updateDto = new AccountUpdateDto();
        updateDto.setFullName("Иванов Иван Иванович");
        updateDto.setBirthDate(LocalDate.of(2000, 6, 15));

        AccountDto result = accountService.updateAccount("ivanov", updateDto);

        assertNotNull(result);
    }

    @Test
    void updateAccount_underAge_throwsException() {
        AccountUpdateDto updateDto = new AccountUpdateDto();
        updateDto.setFullName("Иванов Иван");
        updateDto.setBirthDate(LocalDate.now().minusYears(17));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.updateAccount("ivanov", updateDto));
    }

    @Test
    void deposit_addsBalance() {
        when(accountRepository.findByUsername("ivanov")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountDto result = accountService.deposit("ivanov", new BigDecimal("50.00"));

        assertEquals(new BigDecimal("150.00"), result.getBalance());
    }

    @Test
    void withdraw_sufficientFunds_subtractsBalance() {
        when(accountRepository.findByUsername("ivanov")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountDto result = accountService.withdraw("ivanov", new BigDecimal("50.00"));

        assertEquals(new BigDecimal("50.00"), result.getBalance());
    }

    @Test
    void withdraw_insufficientFunds_throwsException() {
        when(accountRepository.findByUsername("ivanov")).thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.withdraw("ivanov", new BigDecimal("200.00")));
    }

    @Test
    void getOtherAccounts_excludesCurrentUser() {
        Account other = new Account();
        other.setUsername("petrov");
        other.setFullName("Петров Петр");

        when(accountRepository.findByUsernameNot("ivanov")).thenReturn(List.of(other));

        var result = accountService.getOtherAccounts("ivanov");

        assertEquals(1, result.size());
        assertEquals("petrov", result.get(0).getUsername());
    }
}

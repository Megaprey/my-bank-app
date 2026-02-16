package com.bank.accounts.service;

import com.bank.accounts.dto.*;
import com.bank.accounts.model.Account;
import com.bank.accounts.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public AccountDto getAccount(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseGet(() -> createDefaultAccount(username));
        return toDto(account);
    }

    @Transactional
    public AccountDto updateAccount(String username, AccountUpdateDto dto) {
        if (dto.getBirthDate() != null) {
            int age = Period.between(dto.getBirthDate(), LocalDate.now()).getYears();
            if (age < 18) {
                throw new IllegalArgumentException("Возраст должен быть старше 18 лет");
            }
        }

        Account account = accountRepository.findByUsername(username)
                .orElseGet(() -> createDefaultAccount(username));
        account.setFullName(dto.getFullName());
        account.setBirthDate(dto.getBirthDate());
        account = accountRepository.save(account);
        return toDto(account);
    }

    @Transactional
    public AccountDto deposit(String username, BigDecimal amount) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт не найден: " + username));
        account.setBalance(account.getBalance().add(amount));
        account = accountRepository.save(account);
        return toDto(account);
    }

    @Transactional
    public AccountDto withdraw(String username, BigDecimal amount) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Аккаунт не найден: " + username));
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Недостаточно средств на счету");
        }
        account.setBalance(account.getBalance().subtract(amount));
        account = accountRepository.save(account);
        return toDto(account);
    }

    @Transactional(readOnly = true)
    public List<AccountShortDto> getOtherAccounts(String currentUsername) {
        return accountRepository.findByUsernameNot(currentUsername).stream()
                .map(a -> new AccountShortDto(a.getUsername(), a.getFullName()))
                .toList();
    }

    private Account createDefaultAccount(String username) {
        Account account = new Account();
        account.setUsername(username);
        account.setBalance(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    private AccountDto toDto(Account account) {
        return new AccountDto(
                account.getId(),
                account.getUsername(),
                account.getFullName(),
                account.getBirthDate(),
                account.getBalance()
        );
    }
}

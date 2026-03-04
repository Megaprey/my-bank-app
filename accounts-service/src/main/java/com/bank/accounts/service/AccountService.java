package com.bank.accounts.service;

import com.bank.accounts.dto.AccountUpdateDto;
import com.bank.accounts.model.Account;
import com.bank.api.dto.AccountDto;
import com.bank.api.dto.AccountShortDto;
import com.bank.accounts.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public AccountDto getAccount(String username) {
        log.debug("[AccountService.getAccount] username={}", username);
        Account account = accountRepository.findByUsername(username)
                .orElseGet(() -> createDefaultAccount(username));
        return toDto(account);
    }

    @Transactional
    public AccountDto updateAccount(String username, AccountUpdateDto dto) {
        log.debug("[AccountService.updateAccount] username={}, fullName={}", username, dto.fullName());
        if (dto.birthDate() != null) {
            int age = Period.between(dto.birthDate(), LocalDate.now()).getYears();
            if (age < 18) {
                log.warn("[AccountService.updateAccount] underage user rejected: username={}, age={}", username, age);
                throw new IllegalArgumentException("Возраст должен быть старше 18 лет");
            }
        }

        Account account = accountRepository.findByUsername(username)
                .orElseGet(() -> createDefaultAccount(username));
        account.setFullName(dto.fullName());
        account.setBirthDate(dto.birthDate());
        account = accountRepository.save(account);
        log.info("[AccountService.updateAccount] success: username={}", username);
        return toDto(account);
    }

    @Transactional
    public AccountDto deposit(String username, BigDecimal amount) {
        log.debug("[AccountService.deposit] username={}, amount={}", username, amount);
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("[AccountService.deposit] account not found: username={}", username);
                    return new IllegalArgumentException("Аккаунт не найден: " + username);
                });
        account.setBalance(account.getBalance().add(amount));
        account = accountRepository.save(account);
        log.info("[AccountService.deposit] success: username={}, newBalance={}", username, account.getBalance());
        return toDto(account);
    }

    @Transactional
    public AccountDto withdraw(String username, BigDecimal amount) {
        log.debug("[AccountService.withdraw] username={}, amount={}", username, amount);
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("[AccountService.withdraw] account not found: username={}", username);
                    return new IllegalArgumentException("Аккаунт не найден: " + username);
                });
        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("[AccountService.withdraw] insufficient funds: username={}, balance={}, requested={}",
                    username, account.getBalance(), amount);
            throw new IllegalArgumentException("Недостаточно средств на счету");
        }
        account.setBalance(account.getBalance().subtract(amount));
        account = accountRepository.save(account);
        log.info("[AccountService.withdraw] success: username={}, newBalance={}", username, account.getBalance());
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

package com.bank.accounts.controller;

import com.bank.accounts.client.NotificationClient;
import com.bank.accounts.dto.AccountUpdateDto;
import com.bank.accounts.service.AccountService;
import com.bank.api.dto.AccountDto;
import com.bank.api.dto.AccountShortDto;
import com.bank.api.dto.BalanceOperationDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final NotificationClient notificationClient;

    public AccountController(AccountService accountService, NotificationClient notificationClient) {
        this.accountService = accountService;
        this.notificationClient = notificationClient;
    }

    @GetMapping("/me")
    public ResponseEntity<AccountDto> getMyAccount(@AuthenticationPrincipal Jwt jwt) {
        String username = extractUsername(jwt);
        return ResponseEntity.ok(accountService.getAccount(username));
    }

    @PutMapping("/me")
    public ResponseEntity<AccountDto> updateMyAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AccountUpdateDto dto) {
        String username = extractUsername(jwt);
        AccountDto result = accountService.updateAccount(username, dto);
        notificationClient.send(username, "Данные аккаунта обновлены");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/others")
    public ResponseEntity<List<AccountShortDto>> getOtherAccounts(@AuthenticationPrincipal Jwt jwt) {
        String username = extractUsername(jwt);
        return ResponseEntity.ok(accountService.getOtherAccounts(username));
    }

    @PutMapping("/{username}/deposit")
    public ResponseEntity<AccountDto> deposit(
            @PathVariable String username,
            @Valid @RequestBody BalanceOperationDto dto) {
        return ResponseEntity.ok(accountService.deposit(username, dto.amount()));
    }

    @PutMapping("/{username}/withdraw")
    public ResponseEntity<AccountDto> withdraw(
            @PathVariable String username,
            @Valid @RequestBody BalanceOperationDto dto) {
        return ResponseEntity.ok(accountService.withdraw(username, dto.amount()));
    }

    private String extractUsername(Jwt jwt) {
        String username = jwt.getClaimAsString("username");
        if (username == null) {
            username = jwt.getSubject();
        }
        return username;
    }
}

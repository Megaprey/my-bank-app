package com.bank.front.controller;

import com.bank.api.dto.AccountDto;
import com.bank.api.dto.AccountShortDto;
import com.bank.front.dto.MainPageDto;
import com.bank.front.service.AccountGatewayService;
import com.bank.front.service.CashGatewayService;
import com.bank.front.service.TransferGatewayService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class MainController {

    private final AccountGatewayService accountService;
    private final CashGatewayService cashService;
    private final TransferGatewayService transferService;

    public MainController(AccountGatewayService accountService,
                          CashGatewayService cashService,
                          TransferGatewayService transferService) {
        this.accountService = accountService;
        this.cashService = cashService;
        this.transferService = transferService;
    }

    @GetMapping({"/", "/account"})
    public String account(Model model) {
        String successMessage = (String) model.getAttribute("successMessage");
        String errorMessage = (String) model.getAttribute("errorMessage");
        MainPageDto dto = buildPageDto(successMessage, errorMessage);
        model.addAttribute("page", dto);
        model.addAttribute("successMessage", dto.successMessage());
        model.addAttribute("errorMessage", dto.errorMessage());
        return "main";
    }

    @PostMapping("/account")
    public String saveAccount(@RequestParam String fullName,
                              @RequestParam String birthDate,
                              RedirectAttributes redirectAttributes) {
        try {
            LocalDate date = LocalDate.parse(birthDate);
            accountService.updateAccount(fullName, date);
            redirectAttributes.addFlashAttribute("successMessage", "Данные сохранены");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractError(e));
        }
        return "redirect:/account";
    }

    @PostMapping("/cash/deposit")
    public String deposit(@RequestParam BigDecimal amount,
                          RedirectAttributes redirectAttributes) {
        try {
            AccountDto account = accountService.getMyAccount();
            var result = cashService.deposit(account.username(), amount);
            redirectAttributes.addFlashAttribute("successMessage", result.message());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractError(e));
        }
        return "redirect:/account";
    }

    @PostMapping("/cash/withdraw")
    public String withdraw(@RequestParam BigDecimal amount,
                           RedirectAttributes redirectAttributes) {
        try {
            AccountDto account = accountService.getMyAccount();
            var result = cashService.withdraw(account.username(), amount);
            redirectAttributes.addFlashAttribute("successMessage", result.message());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractError(e));
        }
        return "redirect:/account";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam String toUsername,
                           @RequestParam BigDecimal amount,
                           RedirectAttributes redirectAttributes) {
        try {
            AccountDto account = accountService.getMyAccount();
            var result = transferService.transfer(
                    account.username(), toUsername, amount);
            redirectAttributes.addFlashAttribute("successMessage", result.message());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractError(e));
        }
        return "redirect:/account";
    }

    private MainPageDto buildPageDto(String successMessage, String errorMessage) {
        String fullName = null;
        LocalDate birthDate = null;
        BigDecimal balance = null;
        List<AccountShortDto> otherAccounts = null;
        String errorFromLoad = null;
        try {
            AccountDto account = accountService.getMyAccount();
            fullName = account.fullName();
            birthDate = account.birthDate();
            balance = account.balance();
            otherAccounts = accountService.getOtherAccounts();
        } catch (Exception e) {
            errorFromLoad = "Ошибка загрузки данных: " + e.getMessage();
        }
        String finalError = errorMessage != null ? errorMessage : errorFromLoad;
        return new MainPageDto(fullName, birthDate, balance, null, null, null,
                otherAccounts, successMessage, finalError);
    }

    private String extractError(Exception e) {
        String message = e.getMessage();
        if (message != null && message.contains("\"error\":")) {
            int start = message.indexOf("\"error\":\"") + 9;
            int end = message.indexOf("\"", start);
            if (start > 8 && end > start) {
                return message.substring(start, end);
            }
        }
        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            return message;
        }
        return "Произошла ошибка";
    }
}

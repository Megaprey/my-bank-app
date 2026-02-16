package com.bank.front.controller;

import com.bank.front.dto.AccountDto;
import com.bank.front.dto.AccountShortDto;
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
import java.util.Map;

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
        MainPageDto dto = buildPageDto(null, null);
        model.addAttribute("page", dto);
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
            Map<String, Object> result = cashService.deposit(account.getUsername(), amount);
            redirectAttributes.addFlashAttribute("successMessage", result.get("message"));
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
            Map<String, Object> result = cashService.withdraw(account.getUsername(), amount);
            redirectAttributes.addFlashAttribute("successMessage", result.get("message"));
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
            Map<String, Object> result = transferService.transfer(
                    account.getUsername(), toUsername, amount);
            redirectAttributes.addFlashAttribute("successMessage", result.get("message"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractError(e));
        }
        return "redirect:/account";
    }

    private MainPageDto buildPageDto(String successMessage, String errorMessage) {
        MainPageDto dto = new MainPageDto();
        try {
            AccountDto account = accountService.getMyAccount();
            dto.setFullName(account.getFullName());
            dto.setBirthDate(account.getBirthDate());
            dto.setBalance(account.getBalance());

            List<AccountShortDto> others = accountService.getOtherAccounts();
            dto.setOtherAccounts(others);
        } catch (Exception e) {
            dto.setErrorMessage("Ошибка загрузки данных: " + e.getMessage());
        }
        dto.setSuccessMessage(successMessage);
        dto.setErrorMessage(errorMessage);
        return dto;
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
        if (e instanceof IllegalArgumentException) {
            return message;
        }
        return "Произошла ошибка";
    }
}

package com.bank.front.dto;

import com.bank.api.dto.AccountShortDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MainPageDto(
        String fullName,
        LocalDate birthDate,
        BigDecimal balance,
        BigDecimal cashAmount,
        String transferTo,
        BigDecimal transferAmount,
        List<AccountShortDto> otherAccounts,
        String successMessage,
        String errorMessage
) {}

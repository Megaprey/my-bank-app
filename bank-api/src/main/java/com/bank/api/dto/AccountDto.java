package com.bank.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountDto(
        Long id,
        String username,
        String fullName,
        LocalDate birthDate,
        BigDecimal balance
) {}

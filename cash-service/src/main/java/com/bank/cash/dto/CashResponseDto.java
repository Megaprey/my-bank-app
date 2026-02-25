package com.bank.cash.dto;

import java.math.BigDecimal;

public record CashResponseDto(
        String message,
        BigDecimal newBalance
) {}

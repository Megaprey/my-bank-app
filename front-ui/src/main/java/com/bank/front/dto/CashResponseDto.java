package com.bank.front.dto;

import java.math.BigDecimal;

public record CashResponseDto(
        String message,
        BigDecimal newBalance
) {}

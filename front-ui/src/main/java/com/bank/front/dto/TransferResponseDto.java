package com.bank.front.dto;

import java.math.BigDecimal;

public record TransferResponseDto(
        String message,
        BigDecimal newBalance
) {}

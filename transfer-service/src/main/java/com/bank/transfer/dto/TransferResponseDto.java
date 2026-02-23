package com.bank.transfer.dto;

import java.math.BigDecimal;

public record TransferResponseDto(
        String message,
        BigDecimal newBalance
) {}

package com.bank.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BalanceOperationDto(
        @NotNull(message = "Сумма обязательна")
        @Positive(message = "Сумма должна быть положительной")
        BigDecimal amount
) {}

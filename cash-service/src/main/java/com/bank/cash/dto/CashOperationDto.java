package com.bank.cash.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CashOperationDto(
        @NotNull(message = "Имя пользователя обязательно")
        String username,

        @NotNull(message = "Сумма обязательна")
        @Positive(message = "Сумма должна быть положительной")
        BigDecimal amount
) {}

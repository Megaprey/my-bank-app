package com.bank.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferDto(
        @NotBlank(message = "Отправитель обязателен")
        String fromUsername,

        @NotBlank(message = "Получатель обязателен")
        String toUsername,

        @NotNull(message = "Сумма обязательна")
        @Positive(message = "Сумма должна быть положительной")
        BigDecimal amount
) {}

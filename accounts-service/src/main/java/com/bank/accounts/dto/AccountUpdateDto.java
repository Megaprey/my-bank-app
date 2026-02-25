package com.bank.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record AccountUpdateDto(
        @NotBlank(message = "Фамилия и имя обязательны")
        String fullName,

        @NotNull(message = "Дата рождения обязательна")
        @Past(message = "Дата рождения должна быть в прошлом")
        LocalDate birthDate
) {}

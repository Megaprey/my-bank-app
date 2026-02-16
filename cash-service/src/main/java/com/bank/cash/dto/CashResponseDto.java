package com.bank.cash.dto;

import java.math.BigDecimal;

public class CashResponseDto {

    private String message;
    private BigDecimal newBalance;

    public CashResponseDto() {
    }

    public CashResponseDto(String message, BigDecimal newBalance) {
        this.message = message;
        this.newBalance = newBalance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }
}

package com.bank.accounts.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AccountDto {

    private Long id;
    private String username;
    private String fullName;
    private LocalDate birthDate;
    private BigDecimal balance;

    public AccountDto() {
    }

    public AccountDto(Long id, String username, String fullName, LocalDate birthDate, BigDecimal balance) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}

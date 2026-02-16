package com.bank.front.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MainPageDto {

    private String fullName;
    private LocalDate birthDate;
    private BigDecimal balance;
    private BigDecimal cashAmount;
    private String transferTo;
    private BigDecimal transferAmount;
    private List<AccountShortDto> otherAccounts;
    private String successMessage;
    private String errorMessage;

    public MainPageDto() {
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

    public BigDecimal getCashAmount() {
        return cashAmount;
    }

    public void setCashAmount(BigDecimal cashAmount) {
        this.cashAmount = cashAmount;
    }

    public String getTransferTo() {
        return transferTo;
    }

    public void setTransferTo(String transferTo) {
        this.transferTo = transferTo;
    }

    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
    }

    public List<AccountShortDto> getOtherAccounts() {
        return otherAccounts;
    }

    public void setOtherAccounts(List<AccountShortDto> otherAccounts) {
        this.otherAccounts = otherAccounts;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

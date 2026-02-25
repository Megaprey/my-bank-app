package com.bank.cash.controller;

import com.bank.cash.dto.CashOperationDto;
import com.bank.cash.dto.CashResponseDto;
import com.bank.cash.service.CashService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cash")
public class CashController {

    private final CashService cashService;

    public CashController(CashService cashService) {
        this.cashService = cashService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<CashResponseDto> deposit(@Valid @RequestBody CashOperationDto dto) {
        return ResponseEntity.ok(cashService.deposit(dto.username(), dto.amount()));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<CashResponseDto> withdraw(@Valid @RequestBody CashOperationDto dto) {
        return ResponseEntity.ok(cashService.withdraw(dto.username(), dto.amount()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}

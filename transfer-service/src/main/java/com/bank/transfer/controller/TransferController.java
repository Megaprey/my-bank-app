package com.bank.transfer.controller;

import com.bank.transfer.dto.TransferDto;
import com.bank.transfer.dto.TransferResponseDto;
import com.bank.transfer.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transfer")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponseDto> transfer(@Valid @RequestBody TransferDto dto) {
        return ResponseEntity.ok(
                transferService.transfer(dto.fromUsername(), dto.toUsername(), dto.amount()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(503).body(Map.of("error", ex.getMessage()));
    }
}

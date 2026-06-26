package com.example.financeapi.controller;

import com.example.financeapi.dto.request.RecurringRequest;
import com.example.financeapi.dto.response.RecurringResponse;
import com.example.financeapi.dto.response.TransactionResponse;
import com.example.financeapi.service.RecurringTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recurring")
public class RecurringController {

    private final RecurringTransactionService recurringService;

    public RecurringController(RecurringTransactionService recurringService) {
        this.recurringService = recurringService;
    }

    @GetMapping
    public List<RecurringResponse> list() {
        return recurringService.listMine();
    }

    @PostMapping
    public ResponseEntity<RecurringResponse> create(@Valid @RequestBody RecurringRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recurringService.create(request));
    }

    @PutMapping("/{id}")
    public RecurringResponse update(@PathVariable Long id, @Valid @RequestBody RecurringRequest request) {
        return recurringService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recurringService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** POST /api/recurring/{id}/run — tạo ngay 1 giao dịch từ khoản định kỳ này. */
    @PostMapping("/{id}/run")
    public TransactionResponse runNow(@PathVariable Long id) {
        return recurringService.runNow(id);
    }
}

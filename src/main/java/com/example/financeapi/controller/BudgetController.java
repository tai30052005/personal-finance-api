package com.example.financeapi.controller;

import com.example.financeapi.dto.request.BudgetRequest;
import com.example.financeapi.dto.response.BudgetResponse;
import com.example.financeapi.dto.response.BudgetStatusResponse;
import com.example.financeapi.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    /** POST /api/budgets — đặt/cập nhật ngân sách tháng cho một danh mục. */
    @PostMapping
    public BudgetResponse setBudget(@Valid @RequestBody BudgetRequest request) {
        return budgetService.setBudget(request);
    }

    /** GET /api/budgets/status?month=&year= — trạng thái + cảnh báo vượt ngân sách. */
    @GetMapping("/status")
    public List<BudgetStatusResponse> status(@RequestParam int month, @RequestParam int year) {
        return budgetService.getStatus(month, year);
    }
}

package com.example.financeapi.dto.response;

import com.example.financeapi.entity.Budget;

import java.math.BigDecimal;

/** Thông tin một ngân sách (trả về sau khi đặt). */
public record BudgetResponse(
        Long id,
        Long categoryId,
        String categoryName,
        BigDecimal amountLimit,
        int month,
        int year
) {
    public static BudgetResponse from(Budget b) {
        return new BudgetResponse(
                b.getId(),
                b.getCategory().getId(),
                b.getCategory().getName(),
                b.getAmountLimit(),
                b.getMonth(),
                b.getYear());
    }
}

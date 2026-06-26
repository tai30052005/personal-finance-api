package com.example.financeapi.dto.response;

import com.example.financeapi.entity.RecurringTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringResponse(
        Long id,
        Long categoryId,
        String categoryName,
        BigDecimal amount,
        String note,
        int dayOfMonth,
        boolean active,
        LocalDate lastRunDate
) {
    public static RecurringResponse from(RecurringTransaction r) {
        return new RecurringResponse(
                r.getId(),
                r.getCategory().getId(),
                r.getCategory().getName(),
                r.getAmount(),
                r.getNote(),
                r.getDayOfMonth(),
                r.isActive(),
                r.getLastRunDate());
    }
}

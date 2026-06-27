package com.example.financeapi.dto.response;

import com.example.financeapi.entity.Category;
import com.example.financeapi.entity.CategoryType;
import com.example.financeapi.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dữ liệu giao dịch trả về cho client — kèm thông tin danh mục cho tiện hiển thị.
 */
public record TransactionResponse(
        Long id,
        BigDecimal amount,
        String note,
        LocalDate occurredAt,
        Long categoryId,
        String categoryName,
        CategoryType categoryType,
        String receiptUrl
) {
    public static TransactionResponse from(Transaction t) {
        Category c = t.getCategory();
        return new TransactionResponse(
                t.getId(),
                t.getAmount(),
                t.getNote(),
                t.getOccurredAt(),
                c.getId(),
                c.getName(),
                c.getType(),
                t.getReceiptUrl()
        );
    }
}

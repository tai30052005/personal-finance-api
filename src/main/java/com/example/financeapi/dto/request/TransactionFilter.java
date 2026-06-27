package com.example.financeapi.dto.request;

import java.math.BigDecimal;

/**
 * Bộ lọc tìm kiếm giao dịch — mọi trường đều TÙY CHỌN (null = bỏ qua).
 *   month/year/categoryId : lọc theo kỳ và danh mục (như cũ).
 *   keyword               : tìm trong ghi chú (không phân biệt hoa/thường).
 *   minAmount/maxAmount    : khoảng số tiền [min, max].
 */
public record TransactionFilter(
        Integer month,
        Integer year,
        Long categoryId,
        String keyword,
        BigDecimal minAmount,
        BigDecimal maxAmount
) {
}

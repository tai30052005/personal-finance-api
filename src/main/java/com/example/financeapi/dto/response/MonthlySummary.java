package com.example.financeapi.dto.response;

import java.math.BigDecimal;

/**
 * Tổng thu/chi/số dư của MỘT tháng — dùng cho biểu đồ xu hướng cả năm.
 */
public record MonthlySummary(
        int month,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance
) {
}

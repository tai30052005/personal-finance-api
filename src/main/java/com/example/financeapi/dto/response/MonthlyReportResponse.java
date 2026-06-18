package com.example.financeapi.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Báo cáo tháng:
 *   totalIncome  : tổng thu
 *   totalExpense : tổng chi
 *   balance      : số dư = thu - chi
 *   byCategory   : phân tích chi tiết theo từng danh mục
 */
public record MonthlyReportResponse(
        int month,
        int year,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        List<CategoryBreakdown> byCategory
) {
}

package com.example.financeapi.dto.response;

import java.math.BigDecimal;

/**
 * Phân tích/so sánh tháng hiện tại với tháng trước.
 *   *ChangePercent: % thay đổi so với tháng trước (null nếu tháng trước = 0, không tính được).
 *   topExpense*: danh mục chi nhiều nhất trong tháng.
 */
public record InsightsResponse(
        int month,
        int year,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance,
        BigDecimal prevIncome,
        BigDecimal prevExpense,
        BigDecimal prevBalance,
        Double incomeChangePercent,
        Double expenseChangePercent,
        String topExpenseCategory,
        BigDecimal topExpenseAmount
) {
}

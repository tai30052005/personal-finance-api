package com.example.financeapi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Trạng thái một ngân sách trong tháng — KÈM cảnh báo vượt.
 *   amountLimit     : hạn mức đặt ra
 *   spent           : tổng chi thực tế của danh mục trong tháng
 *   remaining       : còn lại = hạn mức - đã chi (có thể âm nếu vượt)
 *   isOverBudget    : true nếu đã chi vượt hạn mức
 *   overspentAmount : số tiền vượt (0 nếu chưa vượt)
 *
 * @JsonProperty("isOverBudget"): ép tên field JSON đúng là "isOverBudget".
 */
public record BudgetStatusResponse(
        Long budgetId,
        Long categoryId,
        String categoryName,
        BigDecimal amountLimit,
        BigDecimal spent,
        BigDecimal remaining,
        @JsonProperty("isOverBudget") boolean overBudget,
        BigDecimal overspentAmount
) {
}

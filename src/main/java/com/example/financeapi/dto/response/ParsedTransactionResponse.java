package com.example.financeapi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Kết quả phân tích để FRONTEND ĐIỀN SẴN form (không tự lưu).
 * categoryId = null nghĩa là Claude đề xuất một danh mục chưa có — user tự chọn/ tạo.
 */
public record ParsedTransactionResponse(
        BigDecimal amount,
        Long categoryId,
        String categoryName,
        String type,
        LocalDate occurredAt,
        String note
) {
}

package com.example.financeapi.dto.response;

import com.example.financeapi.entity.CategoryType;

import java.math.BigDecimal;

/**
 * Một dòng phân tích: tổng tiền của một danh mục trong kỳ báo cáo.
 * Dùng làm "đích" cho truy vấn projection (SELECT new ... trong JPQL).
 */
public record CategoryBreakdown(
        Long categoryId,
        String categoryName,
        CategoryType type,
        BigDecimal total
) {
}

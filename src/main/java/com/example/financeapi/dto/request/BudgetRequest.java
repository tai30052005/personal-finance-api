package com.example.financeapi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Dữ liệu đặt ngân sách tháng cho một danh mục chi.
 *   categoryId  : danh mục (phải thuộc user và là loại EXPENSE)
 *   amountLimit : hạn mức > 0
 *   month/year  : tháng (1..12) và năm áp dụng
 */
public record BudgetRequest(

        @NotNull(message = "categoryId là bắt buộc")
        Long categoryId,

        @NotNull(message = "Hạn mức là bắt buộc")
        @Positive(message = "Hạn mức phải lớn hơn 0")
        BigDecimal amountLimit,

        @NotNull(message = "month là bắt buộc")
        @Min(value = 1, message = "month phải >= 1")
        @Max(value = 12, message = "month phải <= 12")
        Integer month,

        @NotNull(message = "year là bắt buộc")
        @Min(value = 2000, message = "year không hợp lệ")
        Integer year
) {
}

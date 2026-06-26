package com.example.financeapi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Dữ liệu tạo/sửa một khoản định kỳ.
 *   dayOfMonth: ngày trong tháng (1..28) sẽ tự sinh giao dịch.
 *   active: bật/tắt (mặc định bật khi tạo).
 */
public record RecurringRequest(

        @NotNull(message = "categoryId là bắt buộc")
        Long categoryId,

        @NotNull(message = "Số tiền là bắt buộc")
        @Positive(message = "Số tiền phải lớn hơn 0")
        BigDecimal amount,

        @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
        String note,

        @NotNull(message = "Ngày trong tháng là bắt buộc")
        @Min(value = 1, message = "Ngày phải từ 1")
        @Max(value = 28, message = "Ngày tối đa là 28")
        Integer dayOfMonth,

        Boolean active
) {
}

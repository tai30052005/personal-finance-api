package com.example.financeapi.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dữ liệu tạo/sửa giao dịch.
 *   amount     : số tiền > 0 (@Positive). Dùng BigDecimal cho chính xác.
 *   categoryId : id danh mục — sẽ được kiểm tra phải thuộc về user hiện tại.
 *   note       : ghi chú (tùy chọn).
 *   occurredAt : ngày phát sinh, định dạng yyyy-MM-dd.
 */
public record TransactionRequest(

        @NotNull(message = "Số tiền là bắt buộc")
        @Positive(message = "Số tiền phải lớn hơn 0")
        BigDecimal amount,

        @NotNull(message = "categoryId là bắt buộc")
        Long categoryId,

        @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
        String note,

        @NotNull(message = "Ngày giao dịch là bắt buộc")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate occurredAt,

        @Size(max = 500, message = "Đường dẫn ảnh tối đa 500 ký tự")
        String receiptUrl
) {
}

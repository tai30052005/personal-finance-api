package com.example.financeapi.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Dữ liệu tạo/sửa mục tiêu tiết kiệm. */
public record SavingsGoalRequest(

        @NotBlank(message = "Tên mục tiêu không được để trống")
        @Size(max = 100, message = "Tên tối đa 100 ký tự")
        String name,

        @NotNull(message = "Số tiền mục tiêu là bắt buộc")
        @Positive(message = "Số tiền mục tiêu phải lớn hơn 0")
        BigDecimal targetAmount,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate deadline
) {
}

package com.example.financeapi.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Số tiền góp thêm vào một mục tiêu tiết kiệm. */
public record ContributeRequest(

        @NotNull(message = "Số tiền là bắt buộc")
        @Positive(message = "Số tiền phải lớn hơn 0")
        BigDecimal amount
) {
}

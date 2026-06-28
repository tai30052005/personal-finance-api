package com.example.financeapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Câu nhập tự nhiên cần Claude phân tích, ví dụ "cà phê 35k hôm qua". */
public record ParseRequest(

        @NotBlank(message = "Nội dung không được để trống")
        @Size(max = 500, message = "Nội dung tối đa 500 ký tự")
        String text
) {
}

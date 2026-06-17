package com.example.financeapi.dto.request;

import jakarta.validation.constraints.NotBlank;

/** DTO cho yêu cầu đăng nhập. */
public record LoginRequest(

        @NotBlank(message = "Email không được để trống")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        String password
) {
}

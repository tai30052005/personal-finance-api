package com.example.financeapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) cho yêu cầu đăng ký.
 *
 * Đây là một 'record' của Java — cách viết gọn cho lớp chỉ-chứa-dữ liệu
 * (tự sinh constructor, getter, equals, toString).
 *
 * Các annotation validation được kiểm tra khi controller nhận request (@Valid):
 *   @NotBlank : không null và không rỗng/toàn khoảng trắng
 *   @Email    : đúng định dạng email
 *   @Size     : độ dài trong khoảng cho phép
 * Nếu vi phạm -> tự trả 400 (GlobalExceptionHandler xử lý).
 */
public record RegisterRequest(

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự")
        String password
) {
}

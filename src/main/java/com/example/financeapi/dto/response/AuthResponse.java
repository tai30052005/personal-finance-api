package com.example.financeapi.dto.response;

/**
 * DTO trả về sau khi đăng ký/đăng nhập thành công.
 *   token     : chuỗi JWT để client gửi kèm ở các request sau
 *   tokenType : luôn là "Bearer" (cách dùng trong header Authorization)
 *   email     : email của user
 */
public record AuthResponse(
        String token,
        String tokenType,
        String email
) {
}

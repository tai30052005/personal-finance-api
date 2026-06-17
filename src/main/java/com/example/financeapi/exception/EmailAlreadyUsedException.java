package com.example.financeapi.exception;

/**
 * Ném ra khi đăng ký bằng email đã tồn tại.
 * Kế thừa RuntimeException -> không bắt buộc try/catch ở nơi gọi;
 * GlobalExceptionHandler sẽ bắt và trả về HTTP 409 Conflict.
 */
public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException(String email) {
        super("Email đã được sử dụng: " + email);
    }
}

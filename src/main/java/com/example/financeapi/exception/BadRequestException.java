package com.example.financeapi.exception;

/**
 * Ném ra khi yêu cầu sai về mặt logic (vd: lọc theo 'month' mà thiếu 'year').
 * GlobalExceptionHandler bắt và trả về HTTP 400 Bad Request.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

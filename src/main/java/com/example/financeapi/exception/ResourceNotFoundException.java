package com.example.financeapi.exception;

/**
 * Ném ra khi không tìm thấy tài nguyên (hoặc tài nguyên thuộc user khác).
 * GlobalExceptionHandler bắt và trả về HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " không tồn tại (id=" + id + ")");
    }
}

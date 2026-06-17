package com.example.financeapi.entity;

/**
 * Loại danh mục: thu nhập hay chi tiêu.
 * Dùng enum (kiểu liệt kê) thay cho chuỗi tự do để tránh nhập sai ("incom", "Expence"...).
 */
public enum CategoryType {
    INCOME,   // thu
    EXPENSE   // chi
}

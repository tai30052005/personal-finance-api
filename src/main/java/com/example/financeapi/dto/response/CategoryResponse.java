package com.example.financeapi.dto.response;

import com.example.financeapi.entity.Category;
import com.example.financeapi.entity.CategoryType;

/**
 * Dữ liệu trả về cho client.
 *
 * Vì sao không trả thẳng entity Category? Vì entity có tham chiếu tới User
 * (và sau này nhiều quan hệ khác) -> trả thẳng dễ lộ dữ liệu thừa / vòng lặp JSON.
 * DTO giúp ta KIỂM SOÁT đúng những gì muốn trả ra.
 */
public record CategoryResponse(
        Long id,
        String name,
        CategoryType type
) {
    // Hàm tiện ích chuyển từ entity sang DTO.
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getType());
    }
}

package com.example.financeapi.dto.request;

import com.example.financeapi.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dữ liệu đầu vào khi tạo/sửa danh mục.
 *   name : tên danh mục (bắt buộc, tối đa 100 ký tự)
 *   type : INCOME hoặc EXPENSE (bắt buộc)
 *
 * Lưu ý: ta KHÔNG nhận userId từ client — user được lấy từ token cho an toàn.
 */
public record CategoryRequest(

        @NotBlank(message = "Tên danh mục không được để trống")
        @Size(max = 100, message = "Tên danh mục tối đa 100 ký tự")
        String name,

        @NotNull(message = "Loại danh mục là bắt buộc (INCOME hoặc EXPENSE)")
        CategoryType type
) {
}

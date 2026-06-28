package com.example.financeapi.ai;

/**
 * Đích của structured output: Gemini trả JSON khớp record này (responseSchema).
 *   amount       : số tiền VND (số nguyên dương)
 *   categoryName : tên danh mục Claude/Gemini chọn (khớp danh sách hoặc đề xuất mới)
 *   type         : INCOME | EXPENSE
 *   occurredAt   : ngày yyyy-MM-dd
 *   note         : ghi chú ngắn
 */
public record ExtractedTransaction(
        long amount,
        String categoryName,
        String type,
        String occurredAt,
        String note
) {
}

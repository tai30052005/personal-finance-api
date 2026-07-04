package com.example.financeapi.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Yêu cầu chat: kỳ đang xem (tháng/năm) + toàn bộ lịch sử hội thoại.
 * persona (tùy chọn): "garden" -> trợ lý nhập vai "Bác Làm Vườn" (concept Vườn Xanh).
 */
public record ChatRequest(

        @NotNull(message = "Thiếu tháng")
        Integer month,

        @NotNull(message = "Thiếu năm")
        Integer year,

        @NotEmpty(message = "Chưa có câu hỏi")
        List<ChatMessage> messages,

        String persona
) {
}

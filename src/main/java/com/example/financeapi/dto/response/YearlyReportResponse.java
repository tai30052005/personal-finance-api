package com.example.financeapi.dto.response;

import java.util.List;

/**
 * Báo cáo cả năm: danh sách tổng thu/chi của 12 tháng (để vẽ biểu đồ cột).
 */
public record YearlyReportResponse(
        int year,
        List<MonthlySummary> months
) {
}

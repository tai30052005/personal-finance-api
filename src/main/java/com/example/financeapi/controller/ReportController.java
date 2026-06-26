package com.example.financeapi.controller;

import com.example.financeapi.dto.response.InsightsResponse;
import com.example.financeapi.dto.response.MonthlyReportResponse;
import com.example.financeapi.dto.response.YearlyReportResponse;
import com.example.financeapi.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * GET /api/reports/monthly?month=&year=
     * month, year là BẮT BUỘC (không có required=false) -> thiếu sẽ tự trả 400.
     */
    @GetMapping("/monthly")
    public MonthlyReportResponse monthly(@RequestParam int month, @RequestParam int year) {
        return reportService.monthly(month, year);
    }

    /** GET /api/reports/yearly?year= — tổng thu/chi của 12 tháng (cho biểu đồ cột). */
    @GetMapping("/yearly")
    public YearlyReportResponse yearly(@RequestParam int year) {
        return reportService.yearly(year);
    }

    /** GET /api/reports/insights?month=&year= — so sánh với tháng trước + danh mục chi nhiều nhất. */
    @GetMapping("/insights")
    public InsightsResponse insights(@RequestParam int month, @RequestParam int year) {
        return reportService.insights(month, year);
    }
}

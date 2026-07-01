package com.example.financeapi.service;

import com.example.financeapi.ai.GeminiClient;
import com.example.financeapi.dto.request.ChatMessage;
import com.example.financeapi.dto.response.CategoryBreakdown;
import com.example.financeapi.dto.response.ChatResponse;
import com.example.financeapi.dto.response.MonthlyReportResponse;
import com.example.financeapi.dto.response.MonthlySummary;
import com.example.financeapi.dto.response.YearlyReportResponse;
import com.example.financeapi.entity.CategoryType;
import com.example.financeapi.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Trợ lý CHAT phân tích chi tiêu: người dùng hỏi bằng tiếng Việt
 * ("tháng này tôi tiêu nhiều nhất vào đâu?"), backend gom SỐ LIỆU TÓM TẮT của kỳ
 * đang xem (tái dùng ReportService) rồi đưa vào prompt để Gemini trả lời.
 *
 * Cách "data-in-prompt": 1 lần gọi/câu hỏi, không cần tool-use — hợp free tier.
 */
@Service
public class FinancialChatService {

    private final GeminiClient gemini;
    private final ReportService reportService;

    public FinancialChatService(GeminiClient gemini, ReportService reportService) {
        this.gemini = gemini;
        this.reportService = reportService;
    }

    public ChatResponse chat(int month, int year, List<ChatMessage> messages) {
        if (!gemini.isEnabled()) {
            throw new BadRequestException("Tính năng AI chưa được cấu hình (thiếu GEMINI_API_KEY).");
        }
        if (messages == null || messages.isEmpty()) {
            throw new BadRequestException("Chưa có câu hỏi.");
        }

        String summary = buildSummary(month, year);

        // Lịch sử hội thoại -> định dạng contents của Gemini (user/model).
        List<Map<String, Object>> contents = new ArrayList<>();
        for (ChatMessage m : messages) {
            String role = "assistant".equalsIgnoreCase(m.role()) ? "model" : "user";
            String text = m.text() == null ? "" : m.text();
            contents.add(Map.of("role", role, "parts", List.of(Map.of("text", text))));
        }

        Map<String, Object> body = Map.of(
                "system_instruction", Map.of("parts", List.of(Map.of("text", buildSystemPrompt(summary)))),
                "contents", contents,
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "thinkingConfig", Map.of("thinkingBudget", 0),
                        "maxOutputTokens", 800
                )
        );

        JsonNode resp = gemini.generate(body);
        String answer = gemini.firstText(resp);
        if (answer == null || answer.isBlank()) {
            throw new BadRequestException("AI chưa trả lời được, hãy thử lại.");
        }
        return new ChatResponse(answer.trim());
    }

    /** Gom số liệu kỳ đang xem + 12 tháng thành một khối text ngắn cho AI. */
    private String buildSummary(int month, int year) {
        MonthlyReportResponse mr = reportService.monthly(month, year);
        YearlyReportResponse yr = reportService.yearly(year);

        StringBuilder sb = new StringBuilder();
        sb.append("Kỳ đang xem: tháng ").append(month).append("/").append(year).append(".\n");
        sb.append("Tổng thu: ").append(fmt(mr.totalIncome()))
          .append(" đ; Tổng chi: ").append(fmt(mr.totalExpense()))
          .append(" đ; Số dư: ").append(fmt(mr.balance())).append(" đ.\n");

        sb.append("Chi theo danh mục (tháng ").append(month).append("):\n");
        boolean anyExpense = false;
        for (CategoryBreakdown b : mr.byCategory()) {
            if (b.type() == CategoryType.EXPENSE) {
                sb.append("- ").append(b.categoryName()).append(": ").append(fmt(b.total())).append(" đ\n");
                anyExpense = true;
            }
        }
        if (!anyExpense) sb.append("- (chưa có khoản chi)\n");

        sb.append("Thu theo danh mục (tháng ").append(month).append("):\n");
        boolean anyIncome = false;
        for (CategoryBreakdown b : mr.byCategory()) {
            if (b.type() == CategoryType.INCOME) {
                sb.append("- ").append(b.categoryName()).append(": ").append(fmt(b.total())).append(" đ\n");
                anyIncome = true;
            }
        }
        if (!anyIncome) sb.append("- (chưa có khoản thu)\n");

        sb.append("Tổng thu/chi từng tháng năm ").append(year).append(" (chỉ tháng có phát sinh):\n");
        for (MonthlySummary ms : yr.months()) {
            if (ms.income().signum() != 0 || ms.expense().signum() != 0) {
                sb.append("- Tháng ").append(ms.month())
                  .append(": thu ").append(fmt(ms.income()))
                  .append(", chi ").append(fmt(ms.expense())).append("\n");
            }
        }
        return sb.toString();
    }

    private String buildSystemPrompt(String summary) {
        return """
                Bạn là trợ lý tài chính cá nhân, trả lời bằng tiếng Việt, ngắn gọn và thân thiện.
                CHỈ dựa vào SỐ LIỆU bên dưới để trả lời; TUYỆT ĐỐI không bịa số.
                Nếu câu hỏi vượt ngoài dữ liệu đang có, hãy nói rõ là chưa có dữ liệu đó.
                Tiền tệ là VND; khi nêu số tiền hãy thêm dấu phân cách nghìn cho dễ đọc (vd 1.500.000 đ).
                Có thể đưa 1 nhận xét hoặc gợi ý tiết kiệm ngắn nếu phù hợp.

                ===== SỐ LIỆU CỦA NGƯỜI DÙNG =====
                %s
                ==================================
                """.formatted(summary.trim());
    }

    /** In số tiền dạng số nguyên (đồng), bỏ phần thập phân .00. */
    private String fmt(BigDecimal v) {
        return v == null ? "0" : String.valueOf(v.longValue());
    }
}

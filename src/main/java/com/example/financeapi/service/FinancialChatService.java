package com.example.financeapi.service;

import com.example.financeapi.ai.GeminiClient;
import com.example.financeapi.dto.request.ChatMessage;
import com.example.financeapi.dto.response.CategoryBreakdown;
import com.example.financeapi.dto.response.ChatResponse;
import com.example.financeapi.dto.response.MonthlyReportResponse;
import com.example.financeapi.dto.response.MonthlySummary;
import com.example.financeapi.dto.response.YearlyReportResponse;
import com.example.financeapi.entity.CategoryType;
import com.example.financeapi.entity.User;
import com.example.financeapi.exception.BadRequestException;
import com.example.financeapi.repository.TransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    public FinancialChatService(GeminiClient gemini, ReportService reportService,
                                TransactionRepository transactionRepository,
                                CurrentUserService currentUserService) {
        this.gemini = gemini;
        this.reportService = reportService;
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
    }

    public ChatResponse chat(int month, int year, List<ChatMessage> messages, String persona) {
        if (!gemini.isEnabled()) {
            throw new BadRequestException("Tính năng AI chưa được cấu hình (thiếu GEMINI_API_KEY).");
        }
        if (messages == null || messages.isEmpty()) {
            throw new BadRequestException("Chưa có câu hỏi.");
        }

        String summary = buildSummary(month, year);
        boolean garden = "garden".equalsIgnoreCase(persona);

        // Lịch sử hội thoại -> định dạng contents của Gemini (user/model).
        List<Map<String, Object>> contents = new ArrayList<>();
        for (ChatMessage m : messages) {
            String role = "assistant".equalsIgnoreCase(m.role()) ? "model" : "user";
            String text = m.text() == null ? "" : m.text();
            contents.add(Map.of("role", role, "parts", List.of(Map.of("text", text))));
        }

        Map<String, Object> body = Map.of(
                "system_instruction", Map.of("parts", List.of(Map.of("text", buildSystemPrompt(summary, garden)))),
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

        // Vài giao dịch LỚN NHẤT của tháng (để trả lời câu hỏi ở cấp giao dịch, không nhầm với tổng danh mục).
        User user = currentUserService.getCurrentUser();
        LocalDate start = LocalDate.of(year, month, 1);
        var top = transactionRepository.topTransactions(user.getId(), start, start.plusMonths(1), PageRequest.of(0, 8));
        sb.append("Các giao dịch lớn nhất trong tháng ").append(month)
          .append(" (chỉ vài khoản lớn nhất, KHÔNG phải toàn bộ):\n");
        if (top.isEmpty()) {
            sb.append("- (chưa có giao dịch)\n");
        } else {
            for (var t : top) {
                sb.append("- ").append(fmt(t.getAmount())).append(" đ")
                  .append(" · ").append(t.getType() == CategoryType.INCOME ? "thu" : "chi")
                  .append(" · ").append(t.getCategory())
                  .append(" · \"").append(t.getNote() == null ? "" : t.getNote()).append("\"")
                  .append(" · ").append(t.getOccurredAt()).append("\n");
            }
        }
        return sb.toString();
    }

    private String buildSystemPrompt(String summary, boolean garden) {
        // Persona "Bác Làm Vườn" (concept Vườn Xanh): đổi GIỌNG, không đổi luật về số liệu.
        String personaBlock = !garden ? "" : """
                NHẬP VAI: bạn là "Bác Làm Vườn" — người trông coi khu vườn tài chính của người dùng,
                nơi mỗi mục tiêu tiết kiệm là một cái cây, chi tiêu là tưới nước, thu nhập là nắng.
                Xưng "bác", giọng hiền và ấm; thỉnh thoảng dùng ẩn dụ vườn tược (gieo hạt, tưới cây,
                mưa bão, mùa vụ) cho sinh động — nhưng MỌI CON SỐ vẫn phải nêu chính xác, rõ ràng.
                """;
        return personaBlock + """
                Bạn là trợ lý tài chính cá nhân, trả lời bằng tiếng Việt, ngắn gọn và thân thiện.
                CHỈ dựa vào SỐ LIỆU bên dưới để trả lời; TUYỆT ĐỐI không bịa số.
                PHÂN BIỆT RÕ: "chi theo danh mục" là TỔNG của cả danh mục (nhiều giao dịch cộng lại),
                KHÁC với một GIAO DỊCH riêng lẻ trong danh sách "giao dịch lớn nhất". Đừng nhầm tổng danh mục thành một giao dịch.
                Danh sách "giao dịch lớn nhất" chỉ gồm vài khoản lớn nhất, KHÔNG phải toàn bộ; nếu bị hỏi về giao dịch không có trong đó, hoặc cần ĐẾM số lần, hãy nói rõ bạn chỉ có các khoản lớn nhất chứ không có toàn bộ chi tiết.
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

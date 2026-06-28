package com.example.financeapi.service;

import com.example.financeapi.ai.ExtractedTransaction;
import com.example.financeapi.dto.response.ParsedTransactionResponse;
import com.example.financeapi.entity.Category;
import com.example.financeapi.entity.User;
import com.example.financeapi.exception.BadRequestException;
import com.example.financeapi.repository.CategoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Nhập giao dịch bằng NGÔN NGỮ TỰ NHIÊN: gửi câu chữ của người dùng cho Google Gemini,
 * nhận về dữ liệu CÓ CẤU TRÚC (structured output qua responseSchema) rồi map sang
 * danh mục của user. Kết quả chỉ để FRONTEND điền sẵn form — KHÔNG tự lưu giao dịch.
 *
 * Dùng REST trực tiếp (RestClient) để khỏi thêm thư viện; gọi free tier của Gemini.
 */
@Service
public class NaturalLanguageService {

    private static final Logger log = LoggerFactory.getLogger(NaturalLanguageService.class);
    private static final String GEMINI_BASE = "https://generativelanguage.googleapis.com/v1beta";

    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final boolean enabled;

    public NaturalLanguageService(CategoryRepository categoryRepository,
                                  CurrentUserService currentUserService,
                                  ObjectMapper objectMapper,
                                  @Value("${app.ai.api-key:}") String apiKey,
                                  @Value("${app.ai.model:gemini-2.0-flash}") String model) {
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.enabled = apiKey != null && !apiKey.isBlank();
        this.restClient = RestClient.builder().baseUrl(GEMINI_BASE).build();
    }

    /** Có cấu hình key hay chưa — frontend dùng để ẩn/hiện ô nhập nhanh. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Một câu có thể chứa NHIỀU giao dịch -> trả về danh sách (luôn ≥ 1 phần tử). */
    public List<ParsedTransactionResponse> parse(String text) {
        if (!enabled) {
            throw new BadRequestException("Tính năng AI chưa được cấu hình (thiếu GEMINI_API_KEY).");
        }
        User user = currentUserService.getCurrentUser();
        List<Category> categories = categoryRepository.findByUserIdOrderByIdAsc(user.getId());

        Map<String, Object> body = Map.of(
                "system_instruction", Map.of("parts", List.of(Map.of("text", buildSystemPrompt(categories)))),
                "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", text)))),
                "generationConfig", Map.of(
                        "temperature", 0,
                        // Tắt "thinking" của gemini-2.5-flash: việc trích xuất đơn giản không cần
                        // suy luận, mà thinking gây chậm (8s+), tốn token, lỗi chập chờn trên free tier.
                        "thinkingConfig", Map.of("thinkingBudget", 0),
                        "maxOutputTokens", 1024,   // đủ chỗ cho nhiều giao dịch
                        "responseMimeType", "application/json",
                        "responseSchema", responseSchema()
                )
        );

        ExtractedTransaction[] extracted;
        try {
            JsonNode response = postWithRetry(body);

            String json = response.path("candidates").path(0)
                    .path("content").path("parts").path(0).path("text").asText(null);
            if (json == null || json.isBlank()) {
                throw new BadRequestException("AI không hiểu được nội dung, hãy thử lại.");
            }
            extracted = objectMapper.readValue(json, ExtractedTransaction[].class);
        } catch (BadRequestException e) {
            throw e;
        } catch (HttpClientErrorException e) {
            log.warn("Gemini lỗi {}: {}", e.getStatusCode(), e.getMessage());
            if (e.getStatusCode().value() == 429) {
                throw new BadRequestException("Đã hết lượt phân tích AI miễn phí hôm nay — hãy nhập tay hoặc thử lại sau.");
            }
            throw new BadRequestException("Không phân tích được lúc này, hãy thử lại hoặc nhập tay.");
        } catch (HttpServerErrorException e) {
            log.warn("Gemini quá tải {}: {}", e.getStatusCode(), e.getMessage());
            throw new BadRequestException("Dịch vụ AI đang quá tải, hãy thử lại sau giây lát hoặc nhập tay.");
        } catch (Exception e) {
            log.warn("Gọi Gemini thất bại: {}", e.getMessage());
            throw new BadRequestException("Không phân tích được lúc này, hãy thử lại hoặc nhập tay.");
        }

        if (extracted.length == 0) {
            throw new BadRequestException("AI không tách được giao dịch nào, hãy thử lại.");
        }
        List<ParsedTransactionResponse> result = new ArrayList<>();
        for (ExtractedTransaction ex : extracted) {
            result.add(resolve(ex, categories));
        }
        return result;
    }

    /** Gọi Gemini; nếu gặp lỗi 5xx (vd 503 quá tải) thì thử lại 1 lần trước khi bỏ cuộc. */
    private JsonNode postWithRetry(Map<String, Object> body) {
        HttpServerErrorException last = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                return restClient.post()
                        .uri("/models/{model}:generateContent", model)
                        .header("x-goog-api-key", apiKey)
                        .header("Content-Type", "application/json")
                        .body(body)
                        .retrieve()
                        .body(JsonNode.class);
            } catch (HttpServerErrorException e) {
                last = e;   // 5xx tạm thời — thử lại
            }
        }
        throw last;
    }

    /** Map kết quả thô về danh mục của user + chuẩn hóa ngày/số tiền. */
    private ParsedTransactionResponse resolve(ExtractedTransaction ex, List<Category> categories) {
        Long categoryId = null;
        String categoryName = ex.categoryName() == null ? null : ex.categoryName().trim();
        if (categoryName != null) {
            for (Category c : categories) {
                if (c.getName().equalsIgnoreCase(categoryName)) {
                    categoryId = c.getId();
                    categoryName = c.getName();   // dùng đúng tên gốc trong DB
                    break;
                }
            }
        }

        LocalDate occurredAt;
        try {
            String d = ex.occurredAt();
            if (d != null && d.length() >= 10) {
                d = d.substring(0, 10);   // cắt phần giờ nếu model trả về dạng datetime
            }
            occurredAt = LocalDate.parse(d);
        } catch (RuntimeException e) {
            occurredAt = LocalDate.now();   // đường lùi nếu ngày sai định dạng
        }

        BigDecimal amount = ex.amount() > 0 ? BigDecimal.valueOf(ex.amount()) : null;
        String type = "INCOME".equalsIgnoreCase(ex.type()) ? "INCOME" : "EXPENSE";

        return new ParsedTransactionResponse(amount, categoryId, categoryName, type, occurredAt, ex.note());
    }

    /** JSON schema (kiểu OpenAPI của Gemini): MẢNG các giao dịch (mỗi câu có thể nhiều khoản). */
    private Map<String, Object> responseSchema() {
        Map<String, Object> item = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "amount", Map.of("type", "INTEGER",
                                "description", "Số tiền VND, số nguyên dương. '35k'=35000; '2 triệu'/'2 củ'=2000000."),
                        "categoryName", Map.of("type", "STRING",
                                "description", "Chọn ĐÚNG MỘT tên trong danh sách danh mục nếu phù hợp; nếu không, đề xuất một tên ngắn gọn."),
                        "type", Map.of("type", "STRING", "enum", List.of("INCOME", "EXPENSE")),
                        "occurredAt", Map.of("type", "STRING",
                                "description", "Ngày yyyy-MM-dd, suy từ 'hôm qua'/'thứ 6 tuần trước' dựa trên ngày hôm nay được cung cấp."),
                        "note", Map.of("type", "STRING", "description", "Ghi chú ngắn, ví dụ 'cà phê', 'ăn trưa'.")
                ),
                "required", List.of("amount", "categoryName", "type", "occurredAt", "note"),
                "propertyOrdering", List.of("amount", "categoryName", "type", "occurredAt", "note")
        );
        return Map.of("type", "ARRAY", "items", item);
    }

    private String buildSystemPrompt(List<Category> categories) {
        LocalDate today = LocalDate.now();
        StringBuilder cats = new StringBuilder();
        if (categories.isEmpty()) {
            cats.append("(người dùng chưa có danh mục nào)");
        } else {
            for (Category c : categories) {
                cats.append("- ").append(c.getName()).append(" (").append(c.getType()).append(")\n");
            }
        }

        return """
                Bạn là trợ lý trích xuất giao dịch tài chính từ câu nhập tiếng Việt tự nhiên.
                Hôm nay là %s (%s). Suy ra ngày dựa trên mốc này (vd: "hôm qua", "thứ 6 tuần trước").
                Tiền tệ là VND. Quy đổi: 'k'/'nghìn' = ×1000; 'tr'/'triệu'/'củ' = ×1000000.
                Nếu câu KHÔNG nêu số tiền cụ thể, đặt amount = 0; TUYỆT ĐỐI không bịa số.
                Một câu có thể chứa NHIỀU giao dịch (vd "cà phê 35k và taxi 80k") — tách thành nhiều phần tử trong mảng, mỗi khoản một phần tử. Nếu chỉ 1 khoản thì mảng có 1 phần tử.
                Danh mục hiện có của người dùng — hãy CHỌN ĐÚNG MỘT tên dưới đây nếu phù hợp:
                %s
                Nếu không có danh mục phù hợp, đề xuất một tên danh mục ngắn gọn, hợp lý.
                Chỉ trích xuất dữ liệu, không giải thích.
                """.formatted(today, weekdayVi(today.getDayOfWeek()), cats.toString().trim());
    }

    private String weekdayVi(DayOfWeek d) {
        return switch (d) {
            case MONDAY -> "Thứ Hai";
            case TUESDAY -> "Thứ Ba";
            case WEDNESDAY -> "Thứ Tư";
            case THURSDAY -> "Thứ Năm";
            case FRIDAY -> "Thứ Sáu";
            case SATURDAY -> "Thứ Bảy";
            case SUNDAY -> "Chủ Nhật";
        };
    }
}

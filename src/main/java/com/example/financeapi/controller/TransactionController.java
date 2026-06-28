package com.example.financeapi.controller;

import com.example.financeapi.dto.request.ParseRequest;
import com.example.financeapi.dto.request.TransactionFilter;
import com.example.financeapi.dto.request.TransactionRequest;
import com.example.financeapi.dto.response.ParsedTransactionResponse;
import com.example.financeapi.dto.response.TransactionResponse;
import com.example.financeapi.service.NaturalLanguageService;
import com.example.financeapi.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final NaturalLanguageService naturalLanguageService;

    public TransactionController(TransactionService transactionService,
                                 NaturalLanguageService naturalLanguageService) {
        this.transactionService = transactionService;
        this.naturalLanguageService = naturalLanguageService;
    }

    /**
     * GET /api/transactions?month=&year=&categoryId=
     * @RequestParam(required = false): các tham số lọc đều TÙY CHỌN (có thể bỏ trống).
     */
    @GetMapping
    public List<TransactionResponse> list(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount) {
        return transactionService.search(
                new TransactionFilter(month, year, categoryId, keyword, minAmount, maxAmount));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(request));
    }

    /**
     * POST /api/transactions/parse — phân tích câu nhập tự nhiên ("cà phê 35k hôm qua")
     * thành dữ liệu có cấu trúc để frontend ĐIỀN SẴN form. KHÔNG tự lưu giao dịch.
     */
    @PostMapping("/parse")
    public List<ParsedTransactionResponse> parse(@Valid @RequestBody ParseRequest request) {
        return naturalLanguageService.parse(request.text());
    }

    /** GET /api/transactions/parse-enabled — frontend dùng để ẩn/hiện ô nhập nhanh. */
    @GetMapping("/parse-enabled")
    public java.util.Map<String, Boolean> parseEnabled() {
        return java.util.Map.of("enabled", naturalLanguageService.isEnabled());
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        return transactionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/transactions/export?month=&year= — xuất giao dịch ra file CSV.
     * Trả về file đính kèm (attachment) để trình duyệt tải về.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount) {

        List<TransactionResponse> list = transactionService.search(
                new TransactionFilter(month, year, categoryId, keyword, minAmount, maxAmount));

        StringBuilder sb = new StringBuilder();
        sb.append("Date,Category,Type,Amount,Note\n");
        for (TransactionResponse t : list) {
            sb.append(t.occurredAt()).append(',')
              .append(csv(t.categoryName())).append(',')
              .append(t.categoryType()).append(',')
              .append(t.amount()).append(',')
              .append(csv(t.note())).append('\n');
        }

        // Prepend BOM (EF BB BF) để Excel mở file UTF-8 đúng tiếng Việt có dấu.
        byte[] csvBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] body = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, body, 0, bom.length);
        System.arraycopy(csvBytes, 0, body, bom.length, csvBytes.length);

        String filename = "transactions" + (month != null && year != null ? ("_" + year + "-" + month) : "") + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }

    /** Escape 1 ô CSV: nếu chứa dấu phẩy/nháy/xuống dòng thì bọc trong nháy kép. */
    private String csv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

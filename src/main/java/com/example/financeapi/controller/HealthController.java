package com.example.financeapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Controller kiểm tra "sức khỏe" của ứng dụng.
 *
 * @RestController     = @Controller + @ResponseBody:
 *     mọi giá trị trả về từ method sẽ được tự động chuyển thành JSON
 *     và ghi thẳng vào HTTP response body (không cần render view).
 *
 * @RequestMapping("/api") đặt tiền tố đường dẫn chung cho cả lớp.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * GET /api/health  ->  trả về trạng thái UP kèm thời điểm hiện tại.
     *
     * @GetMapping ánh xạ HTTP GET tới method này.
     * Trả về Map -> Spring (Jackson) tự serialize thành JSON:
     *   { "status": "UP", "service": "...", "timestamp": "..." }
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "personal-finance-api",
                "timestamp", Instant.now().toString()
        );
    }
}

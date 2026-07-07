package com.example.financeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/**
 * Điểm khởi đầu (entry point) của ứng dụng Spring Boot.
 *
 * @SpringBootApplication là annotation gộp 3 annotation:
 *   - @Configuration       : đánh dấu đây là lớp cấu hình
 *   - @EnableAutoConfiguration : Spring Boot tự cấu hình dựa trên dependency có trong classpath
 *   - @ComponentScan       : tự quét các @Controller, @Service, @Repository... trong package này
 *                            và các package con (com.example.financeapi.*)
 */
@SpringBootApplication
@EnableScheduling   // bật các tác vụ @Scheduled (giao dịch định kỳ chạy nền)
public class FinanceApiApplication {

    public static void main(String[] args) {
        // Chốt múi giờ toàn app về Việt Nam (UTC+7) NGAY TRƯỚC khi Spring/Hibernate khởi tạo.
        // Máy chủ triển khai (Render/Docker) mặc định chạy UTC — nếu để nguyên,
        // @CreationTimestamp, LocalDate.now() và cast(createdAt as date) sẽ tính theo UTC,
        // lệch ngày so với người dùng VN (VD ghi chép buổi tối bị đẩy sang hôm sau).
        // Phải đặt ở đây (không phải @PostConstruct) để chắc chắn chạy trước mọi bean.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        // Khởi động toàn bộ Spring context + nhúng Tomcat server (mặc định cổng 8080)
        SpringApplication.run(FinanceApiApplication.class, args);
    }
}

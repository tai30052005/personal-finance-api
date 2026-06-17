package com.example.financeapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cấu hình Spring Security.
 *
 * LƯU Ý: Đây là cấu hình TẠM THỜI cho Bước 1 — cho phép mọi request đi qua
 * để health-check và Swagger chạy được. Ở Bước 4 (Auth) ta sẽ thay bằng:
 *   - chặn các endpoint cần đăng nhập
 *   - thêm JwtAuthFilter để đọc JWT từ header
 *
 * @Configuration: đánh dấu lớp này chứa các @Bean cấu hình.
 */
@Configuration
public class SecurityConfig {

    /**
     * SecurityFilterChain định nghĩa luật bảo mật cho mọi HTTP request.
     * Spring Boot mặc định BẬT bảo mật (mọi endpoint cần đăng nhập + sinh mật khẩu ngẫu nhiên).
     * Ở đây ta ghi đè để tạm mở toàn bộ.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF: API REST dùng JWT (stateless), không dùng session/cookie nên không cần CSRF token
                .csrf(AbstractHttpConfigurer::disable)
                // Tạm thời: cho phép TẤT CẢ request mà không cần xác thực
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}

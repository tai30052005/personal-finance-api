package com.example.financeapi.config;

import com.example.financeapi.security.CustomUserDetailsService;
import com.example.financeapi.security.JwtAuthFilter;
import com.example.financeapi.security.JwtUtil;
import com.example.financeapi.security.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Cấu hình bảo mật THẬT (thay cho bản tạm permitAll ở Bước 1).
 *
 * @EnableWebSecurity: bật tính năng bảo mật web của Spring Security.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Danh sách origin được phép gọi API (CORS), đọc từ cấu hình.
     * Mặc định là các origin localhost; khi deploy chỉ cần đặt biến môi trường
     * CORS_ALLOWED_ORIGINS = domain frontend production (vd https://...vercel.app).
     */
    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    public SecurityConfig(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /** Định nghĩa luật bảo mật cho mọi HTTP request. */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Tự tạo filter JWT (không đánh @Component để tránh bị Spring đăng ký 2 lần)
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtUtil, userDetailsService);

        http
                // Cho phép frontend (React, cổng khác) gọi API — dùng cấu hình CORS bên dưới
                .cors(Customizer.withDefaults())
                // API REST dùng token, không dùng session/cookie -> tắt CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // STATELESS: server KHÔNG lưu phiên đăng nhập; mỗi request tự mang token
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Các endpoint công khai (không cần đăng nhập):
                        .requestMatchers("/api/auth/**", "/api/health", "/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Mọi endpoint còn lại đều BẮT BUỘC đăng nhập:
                        .anyRequest().authenticated())
                // Khi chưa xác thực mà gọi endpoint cần bảo vệ -> trả 401 (không phải 403)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new RestAuthenticationEntryPoint()))
                // Đặt filter JWT TRƯỚC filter đăng nhập username/password mặc định
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Cấu hình CORS: cho phép trình duyệt từ origin của frontend (React dev server)
     * gọi tới API. Nếu không có, trình duyệt sẽ chặn request cross-origin.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Dùng "OriginPatterns" để hỗ trợ cả origin chính xác lẫn wildcard
        // (vd "https://*.vercel.app" để bao cả các bản preview của Vercel).
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));   // cho phép mọi header (gồm Authorization)
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * BCrypt: thuật toán băm mật khẩu một chiều (không giải ngược được) + có "salt"
     * tự động -> 2 người cùng mật khẩu vẫn cho 2 chuỗi băm khác nhau. Chuẩn industry.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager: "bộ máy" kiểm tra email + mật khẩu khi login.
     * Spring tự ghép nó với CustomUserDetailsService + PasswordEncoder ở trên.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

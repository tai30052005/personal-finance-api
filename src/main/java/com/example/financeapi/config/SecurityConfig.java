package com.example.financeapi.config;

import com.example.financeapi.security.CustomUserDetailsService;
import com.example.financeapi.security.JwtAuthFilter;
import com.example.financeapi.security.JwtUtil;
import com.example.financeapi.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

package com.example.financeapi.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint cần đăng nhập: trả về thông tin user đang đăng nhập (lấy từ token).
 * Dùng để kiểm chứng luồng JWT hoạt động end-to-end, và cũng tiện cho client.
 *
 * Spring tự "tiêm" đối tượng Authentication — chính là danh tính mà JwtAuthFilter
 * đã đặt vào SecurityContext sau khi xác thực token thành công.
 */
@RestController
@RequestMapping("/api")
public class MeController {

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        // authentication.getName() = username trong UserDetails = email của user
        return Map.of("email", authentication.getName());
    }
}

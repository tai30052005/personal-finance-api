package com.example.financeapi.controller;

import com.example.financeapi.dto.request.LoginRequest;
import com.example.financeapi.dto.request.RegisterRequest;
import com.example.financeapi.dto.response.AuthResponse;
import com.example.financeapi.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller cho xác thực. Tầng "cửa khẩu": chỉ nhận request, gọi service, trả kết quả.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/register
     * @Valid     : kích hoạt kiểm tra các ràng buộc trong RegisterRequest.
     * @RequestBody: chuyển JSON trong body request thành đối tượng Java.
     * Trả 201 Created khi tạo thành công.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login
     * Trả 200 OK kèm token nếu đăng nhập đúng.
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}

package com.example.financeapi.service;

import com.example.financeapi.entity.User;
import com.example.financeapi.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Tiện ích lấy User ĐANG ĐĂNG NHẬP (dùng chung cho category/transaction/budget).
 *
 * JwtAuthFilter đã đặt danh tính vào SecurityContext sau khi xác thực token.
 * Ở đây ta đọc lại email từ đó rồi nạp User thật từ DB.
 *
 * Đây là "trái tim" của tính năng cô lập dữ liệu theo user: mọi truy vấn sau này
 * đều lọc theo user.getId() lấy từ token -> user A không thể chạm dữ liệu user B.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy user đã xác thực: " + email));
    }
}

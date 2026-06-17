package com.example.financeapi.security;

import com.example.financeapi.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Cầu nối giữa Spring Security và bảng users của chúng ta.
 *
 * Spring Security không biết user của bạn lưu ở đâu. Ta cài interface
 * UserDetailsService để dạy nó: "muốn lấy user theo email thì làm thế này".
 *
 * Spring sẽ tự dùng class này khi cần xác thực (vd lúc login).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Tải user theo "username" — ở đây username chính là email.
     * Trả về một đối tượng UserDetails (chuẩn của Spring Security) gồm:
     *   email + mật khẩu đã băm + danh sách quyền (tạm để rỗng).
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.example.financeapi.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return User.withUsername(user.getEmail())
                .password(user.getPasswordHash())          // mật khẩu đã băm BCrypt
                .authorities(Collections.emptyList())      // chưa phân quyền role
                .build();
    }
}

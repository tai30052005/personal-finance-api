package com.example.financeapi.service;

import com.example.financeapi.dto.request.LoginRequest;
import com.example.financeapi.dto.request.RegisterRequest;
import com.example.financeapi.dto.response.AuthResponse;
import com.example.financeapi.entity.User;
import com.example.financeapi.exception.EmailAlreadyUsedException;
import com.example.financeapi.repository.UserRepository;
import com.example.financeapi.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tầng Service: chứa LOGIC NGHIỆP VỤ cho đăng ký/đăng nhập.
 * Controller chỉ gọi xuống đây, không tự xử lý logic.
 *
 * @Service: đánh dấu Bean tầng service (Spring quản lý + tiêm vào controller).
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // Constructor injection: Spring tự truyền các Bean phụ thuộc vào đây.
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Đăng ký: kiểm tra email trùng -> băm mật khẩu -> lưu user -> phát token.
     *
     * @Transactional: nhóm các thao tác DB thành 1 giao dịch — hoặc thành công
     * trọn vẹn, hoặc rollback toàn bộ nếu có lỗi giữa chừng.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyUsedException(request.email());
        }

        // KHÔNG bao giờ lưu mật khẩu thô — luôn băm bằng BCrypt.
        String hashed = passwordEncoder.encode(request.password());
        User user = new User(request.email(), hashed);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, "Bearer", user.getEmail());
    }

    /**
     * Đăng nhập: nhờ AuthenticationManager kiểm tra email + mật khẩu.
     * Nếu sai -> ném BadCredentialsException -> GlobalExceptionHandler trả 401.
     * Nếu đúng -> phát token mới.
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        // Tới được đây nghĩa là email/mật khẩu hợp lệ.
        String token = jwtUtil.generateToken(request.email());
        return new AuthResponse(token, "Bearer", request.email());
    }
}

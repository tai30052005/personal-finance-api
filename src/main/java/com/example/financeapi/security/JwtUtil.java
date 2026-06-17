package com.example.financeapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Tiện ích tạo và xác thực JWT (JSON Web Token).
 *
 * JWT là một chuỗi gồm 3 phần ngăn bởi dấu chấm:  header.payload.signature
 *   - payload chứa "claims" (dữ liệu), ở đây ta để 'subject' = email user.
 *   - signature là chữ ký số tạo bằng khóa bí mật (secret). Server dùng khóa này
 *     để KIỂM TRA token có bị giả mạo/sửa đổi không.
 *
 * @Component: đánh dấu đây là một Bean để Spring quản lý và "tiêm" vào nơi cần.
 */
@Component
public class JwtUtil {

    private final SecretKey key;        // khóa bí mật để ký & xác thực
    private final long expirationMs;    // thời hạn token (mili-giây)

    /**
     * @Value("${app.jwt.secret}") đọc giá trị từ application.yml (mục app.jwt.secret).
     * Spring tự gọi constructor này và truyền giá trị cấu hình vào.
     */
    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-ms}") long expirationMs) {
        // Tạo khóa HMAC-SHA từ chuỗi secret (yêu cầu >= 32 ký tự).
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /** Tạo token mới cho một user (subject = email). */
    public String generateToken(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)                                   // ai sở hữu token
                .issuedAt(Date.from(now))                           // thời điểm tạo
                .expiration(Date.from(now.plusMillis(expirationMs)))// thời điểm hết hạn
                .signWith(key)                                      // ký bằng khóa bí mật
                .compact();                                         // xuất ra chuỗi
    }

    /** Lấy email (subject) từ token đã được xác thực. */
    public String extractUsername(String token) {
        return parse(token).getPayload().getSubject();
    }

    /** Kiểm tra token hợp lệ (đúng chữ ký + chưa hết hạn). */
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // chữ ký sai, token hỏng, hoặc đã hết hạn -> không hợp lệ
            return false;
        }
    }

    /** Giải mã + xác thực chữ ký. Ném exception nếu token không hợp lệ. */
    private Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }
}

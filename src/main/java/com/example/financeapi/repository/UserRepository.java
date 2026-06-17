package com.example.financeapi.repository;

import com.example.financeapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository = tầng "kho" nói chuyện với database.
 *
 * Điều kỳ diệu của Spring Data JPA: bạn chỉ cần khai báo INTERFACE,
 * KHÔNG cần viết code cài đặt. Spring tự sinh class thật lúc chạy.
 *
 * JpaRepository<User, Long> nghĩa là: kho cho entity User, khóa chính kiểu Long.
 * Kế thừa sẵn các method: save(), findById(), findAll(), delete()...
 *
 * Hai method dưới đây là "derived query": Spring đọc TÊN method và tự sinh SQL.
 *   findByEmail   -> SELECT * FROM users WHERE email = ?
 *   existsByEmail -> SELECT count(*) > 0 FROM users WHERE email = ?
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // Optional<User>: có thể có hoặc không (tránh trả về null gây lỗi NullPointer).
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}

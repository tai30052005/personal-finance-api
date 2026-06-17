package com.example.financeapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity = một bảng trong DB được ánh xạ thành một lớp Java.
 *
 * @Entity            : đánh dấu đây là entity JPA (Hibernate sẽ quản lý).
 * @Table(name="users"): ánh xạ tới bảng 'users' (nếu không ghi, mặc định lấy tên lớp).
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * @Id          : đây là khóa chính.
     * @GeneratedValue(strategy = IDENTITY): để DB tự sinh giá trị (cột BIGSERIAL của Postgres).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column tinh chỉnh cột: không cho null, giá trị duy nhất.
    @Column(nullable = false, unique = true)
    private String email;

    // Tên field (passwordHash) -> Spring tự đổi sang snake_case (password_hash);
    // ở đây ghi rõ name cho chắc chắn.
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * @CreationTimestamp: Hibernate tự gán thời điểm hiện tại khi bản ghi được TẠO.
     * updatable = false: không bao giờ bị thay đổi ở các lần update sau.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // JPA bắt buộc phải có constructor rỗng (để Hibernate khởi tạo đối tượng bằng reflection).
    protected User() {
    }

    public User(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // ===== Getters / Setters =====
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

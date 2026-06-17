package com.example.financeapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Quan hệ: NHIỀU category thuộc về MỘT user  -> @ManyToOne.
     *
     * fetch = LAZY : chỉ tải user khi thực sự cần (gọi getUser()), tránh query thừa.
     * optional = false : bắt buộc phải có user (không null).
     * @JoinColumn(name="user_id") : cột khóa ngoại trong bảng categories trỏ tới users.id.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * @Enumerated(EnumType.STRING): lưu enum dưới dạng CHUỖI ("INCOME"/"EXPENSE").
     * KHÔNG dùng ORDINAL (lưu số 0,1) vì nếu sau này thêm/đổi thứ tự enum sẽ sai dữ liệu cũ.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryType type;

    protected Category() {
    }

    public Category(User user, String name, CategoryType type) {
        this.user = user;
        this.name = name;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }
}

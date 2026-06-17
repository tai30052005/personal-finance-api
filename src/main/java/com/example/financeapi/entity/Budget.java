package com.example.financeapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(
        name = "budgets",
        // Ràng buộc: 1 user chỉ có 1 ngân sách cho 1 danh mục trong 1 tháng/năm.
        // Khai báo ở đây để khớp với UNIQUE constraint trong V1__init.sql.
        uniqueConstraints = @UniqueConstraint(
                name = "uq_budget",
                columnNames = {"user_id", "category_id", "month", "year"}
        )
)
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Hạn mức ngân sách — cũng là tiền nên dùng BigDecimal.
    @Column(name = "amount_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountLimit;

    @Column(nullable = false)
    private Integer month;   // 1..12

    @Column(nullable = false)
    private Integer year;

    protected Budget() {
    }

    public Budget(User user, Category category, BigDecimal amountLimit, Integer month, Integer year) {
        this.user = user;
        this.category = category;
        this.amountLimit = amountLimit;
        this.month = month;
        this.year = year;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getAmountLimit() {
        return amountLimit;
    }

    public void setAmountLimit(BigDecimal amountLimit) {
        this.amountLimit = amountLimit;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}

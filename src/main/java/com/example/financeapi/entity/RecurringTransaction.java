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
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * "Khoản định kỳ" — một quy tắc để tự động tạo giao dịch mỗi tháng.
 * Ví dụ: tiền nhà 3.000.000đ vào ngày 1 hàng tháng.
 *
 * Một scheduled job (chạy nền hàng ngày) sẽ kiểm tra: tới ngày 'dayOfMonth'
 * thì tự tạo một Transaction tương ứng.
 */
@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String note;

    /** Ngày trong tháng để tạo giao dịch (1..28 — giới hạn 28 để tháng nào cũng có). */
    @Column(name = "day_of_month", nullable = false)
    private Integer dayOfMonth;

    /** Đang bật hay tạm tắt. */
    @Column(nullable = false)
    private boolean active = true;

    /** Lần cuối đã sinh giao dịch — để tránh tạo trùng trong cùng một tháng. */
    @Column(name = "last_run_date")
    private LocalDate lastRunDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected RecurringTransaction() {
    }

    public RecurringTransaction(User user, Category category, BigDecimal amount,
                                String note, Integer dayOfMonth) {
        this.user = user;
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.dayOfMonth = dayOfMonth;
        this.active = true;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Integer getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(Integer dayOfMonth) { this.dayOfMonth = dayOfMonth; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDate getLastRunDate() { return lastRunDate; }
    public void setLastRunDate(LocalDate lastRunDate) { this.lastRunDate = lastRunDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

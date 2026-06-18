package com.example.financeapi.repository;

import com.example.financeapi.dto.response.CategoryBreakdown;
import com.example.financeapi.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Kho cho Transaction.
 *
 * JpaSpecificationExecutor: cho phép truyền vào một "Specification" — tức là
 * tập điều kiện lọc dựng ĐỘNG lúc chạy (Criteria API). Nhờ đó ta chỉ thêm
 * điều kiện nào thực sự cần, tránh được bẫy "tham số không xác định kiểu" của SQL.
 */
public interface TransactionRepository
        extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    // Lấy 1 giao dịch nhưng phải thuộc về đúng user (cô lập dữ liệu).
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    /**
     * Tổng tiền theo TỪNG DANH MỤC trong khoảng [start, end) cho 1 user.
     *
     * - JOIN t.category c: nối sang bảng danh mục để lấy tên/loại.
     * - GROUP BY: gom theo danh mục rồi SUM(amount) cho mỗi nhóm.
     * - SELECT new ...CategoryBreakdown(...): "projection" — map thẳng mỗi dòng
     *   kết quả vào DTO qua constructor (không cần xử lý Object[] thủ công).
     *
     * Từ kết quả này, tầng service tự cộng ra tổng thu / tổng chi / số dư.
     */
    @Query("""
            SELECT new com.example.financeapi.dto.response.CategoryBreakdown(
                       c.id, c.name, c.type, SUM(t.amount))
            FROM Transaction t
            JOIN t.category c
            WHERE t.user.id = :userId
              AND t.occurredAt >= :start
              AND t.occurredAt <  :end
            GROUP BY c.id, c.name, c.type
            ORDER BY c.type, SUM(t.amount) DESC
            """)
    List<CategoryBreakdown> breakdownByCategory(@Param("userId") Long userId,
                                                @Param("start") LocalDate start,
                                                @Param("end") LocalDate end);
}

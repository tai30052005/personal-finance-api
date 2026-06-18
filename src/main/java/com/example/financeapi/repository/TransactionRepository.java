package com.example.financeapi.repository;

import com.example.financeapi.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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
}

package com.example.financeapi.repository;

import com.example.financeapi.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    List<RecurringTransaction> findByUserIdOrderByIdAsc(Long userId);

    Optional<RecurringTransaction> findByIdAndUserId(Long id, Long userId);

    // Dùng cho scheduled job: lấy mọi khoản ĐANG BẬT có ngày trùng hôm nay (mọi user).
    List<RecurringTransaction> findByActiveTrueAndDayOfMonth(Integer dayOfMonth);

    // Xóa các khoản định kỳ thuộc một danh mục (khi xóa danh mục - cascade).
    void deleteByCategoryId(Long categoryId);
}

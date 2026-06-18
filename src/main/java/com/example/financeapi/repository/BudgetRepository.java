package com.example.financeapi.repository;

import com.example.financeapi.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Tất cả ngân sách của 1 user trong 1 tháng/năm (để tính trạng thái).
    List<Budget> findByUserIdAndMonthAndYearOrderByIdAsc(Long userId, Integer month, Integer year);

    // Tìm ngân sách hiện có của (user, danh mục, tháng, năm) — phục vụ "đặt/cập nhật".
    Optional<Budget> findByUserIdAndCategoryIdAndMonthAndYear(
            Long userId, Long categoryId, Integer month, Integer year);
}

package com.example.financeapi.service;

import com.example.financeapi.dto.response.CategoryBreakdown;
import com.example.financeapi.dto.response.MonthlyReportResponse;
import com.example.financeapi.entity.CategoryType;
import com.example.financeapi.entity.User;
import com.example.financeapi.exception.BadRequestException;
import com.example.financeapi.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    public ReportService(TransactionRepository transactionRepository,
                         CurrentUserService currentUserService) {
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Báo cáo cho một tháng: tổng thu, tổng chi, số dư + phân tích theo danh mục.
     * Chỉ cần 1 truy vấn (breakdown theo danh mục) rồi tự cộng ra các tổng.
     */
    @Transactional(readOnly = true)
    public MonthlyReportResponse monthly(int month, int year) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("month phải nằm trong khoảng 1..12");
        }
        User user = currentUserService.getCurrentUser();

        LocalDate start = LocalDate.of(year, month, 1);   // đầu tháng
        LocalDate end = start.plusMonths(1);              // đầu tháng kế tiếp

        List<CategoryBreakdown> byCategory =
                transactionRepository.breakdownByCategory(user.getId(), start, end);

        BigDecimal totalIncome = sumOfType(byCategory, CategoryType.INCOME);
        BigDecimal totalExpense = sumOfType(byCategory, CategoryType.EXPENSE);
        BigDecimal balance = totalIncome.subtract(totalExpense);

        return new MonthlyReportResponse(
                month, year, totalIncome, totalExpense, balance, byCategory);
    }

    /** Cộng tổng các danh mục cùng loại (INCOME hoặc EXPENSE). */
    private BigDecimal sumOfType(List<CategoryBreakdown> list, CategoryType type) {
        return list.stream()
                .filter(b -> b.type() == type)
                .map(CategoryBreakdown::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);   // mặc định 0 nếu không có
    }
}

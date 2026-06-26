package com.example.financeapi.service;

import com.example.financeapi.dto.response.CategoryBreakdown;
import com.example.financeapi.dto.response.MonthlyReportResponse;
import com.example.financeapi.dto.response.MonthlySummary;
import com.example.financeapi.dto.response.YearlyReportResponse;
import com.example.financeapi.entity.CategoryType;
import com.example.financeapi.entity.User;
import com.example.financeapi.exception.BadRequestException;
import com.example.financeapi.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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

    /**
     * Báo cáo cả năm: tổng thu/chi/số dư của từng tháng (1..12) — để vẽ biểu đồ cột.
     * Lấy dữ liệu gộp theo (tháng, loại) rồi dựng lại đủ 12 tháng (tháng không có giao dịch = 0).
     */
    @Transactional(readOnly = true)
    public YearlyReportResponse yearly(int year) {
        User user = currentUserService.getCurrentUser();
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = start.plusYears(1);

        // index 1..12 cho từng tháng
        BigDecimal[] income = new BigDecimal[13];
        BigDecimal[] expense = new BigDecimal[13];
        for (int m = 1; m <= 12; m++) {
            income[m] = BigDecimal.ZERO;
            expense[m] = BigDecimal.ZERO;
        }

        for (Object[] row : transactionRepository.monthlyTotalsByType(user.getId(), start, end)) {
            int month = ((Number) row[0]).intValue();
            CategoryType type = (CategoryType) row[1];
            BigDecimal sum = (row[2] instanceof BigDecimal bd) ? bd : new BigDecimal(row[2].toString());
            if (type == CategoryType.INCOME) {
                income[month] = income[month].add(sum);
            } else {
                expense[month] = expense[month].add(sum);
            }
        }

        List<MonthlySummary> months = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            months.add(new MonthlySummary(m, income[m], expense[m], income[m].subtract(expense[m])));
        }
        return new YearlyReportResponse(year, months);
    }

    /** Cộng tổng các danh mục cùng loại (INCOME hoặc EXPENSE). */
    private BigDecimal sumOfType(List<CategoryBreakdown> list, CategoryType type) {
        return list.stream()
                .filter(b -> b.type() == type)
                .map(CategoryBreakdown::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);   // mặc định 0 nếu không có
    }
}

package com.example.financeapi.service;

import com.example.financeapi.dto.response.CategoryBreakdown;
import com.example.financeapi.dto.response.InsightsResponse;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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

    /**
     * Insight: so sánh tháng hiện tại với tháng LIỀN TRƯỚC + tìm danh mục chi nhiều nhất.
     * Tái dùng monthly() cho cả 2 tháng, rồi tính phần trăm thay đổi.
     */
    @Transactional(readOnly = true)
    public InsightsResponse insights(int month, int year) {
        MonthlyReportResponse cur = monthly(month, year);

        int prevMonth = (month == 1) ? 12 : month - 1;
        int prevYear = (month == 1) ? year - 1 : year;
        MonthlyReportResponse prev = monthly(prevMonth, prevYear);

        Double incomeChange = percentChange(prev.totalIncome(), cur.totalIncome());
        Double expenseChange = percentChange(prev.totalExpense(), cur.totalExpense());

        // Danh mục CHI nhiều nhất trong tháng hiện tại
        CategoryBreakdown top = cur.byCategory().stream()
                .filter(b -> b.type() == CategoryType.EXPENSE)
                .max(Comparator.comparing(CategoryBreakdown::total))
                .orElse(null);

        return new InsightsResponse(
                month, year,
                cur.totalIncome(), cur.totalExpense(), cur.balance(),
                prev.totalIncome(), prev.totalExpense(), prev.balance(),
                incomeChange, expenseChange,
                top != null ? top.categoryName() : null,
                top != null ? top.total() : null);
    }

    /** % thay đổi từ 'prev' sang 'cur'. Trả null nếu prev = 0 (không tính được). */
    private Double percentChange(BigDecimal prev, BigDecimal cur) {
        if (prev == null || prev.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return cur.subtract(prev)
                .divide(prev, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /** Cộng tổng các danh mục cùng loại (INCOME hoặc EXPENSE). */
    private BigDecimal sumOfType(List<CategoryBreakdown> list, CategoryType type) {
        return list.stream()
                .filter(b -> b.type() == type)
                .map(CategoryBreakdown::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);   // mặc định 0 nếu không có
    }
}

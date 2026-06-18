package com.example.financeapi.service;

import com.example.financeapi.dto.request.BudgetRequest;
import com.example.financeapi.dto.response.BudgetResponse;
import com.example.financeapi.dto.response.BudgetStatusResponse;
import com.example.financeapi.dto.response.CategoryBreakdown;
import com.example.financeapi.entity.Budget;
import com.example.financeapi.entity.Category;
import com.example.financeapi.entity.CategoryType;
import com.example.financeapi.entity.User;
import com.example.financeapi.exception.BadRequestException;
import com.example.financeapi.exception.ResourceNotFoundException;
import com.example.financeapi.repository.BudgetRepository;
import com.example.financeapi.repository.CategoryRepository;
import com.example.financeapi.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    public BudgetService(BudgetRepository budgetRepository,
                         CategoryRepository categoryRepository,
                         TransactionRepository transactionRepository,
                         CurrentUserService currentUserService) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Đặt (hoặc cập nhật) ngân sách cho một danh mục trong tháng — thao tác idempotent:
     * nếu đã có ngân sách cho (user, danh mục, tháng, năm) thì cập nhật hạn mức,
     * chưa có thì tạo mới. Nhờ vậy gọi lại nhiều lần không bị lỗi trùng UNIQUE.
     */
    @Transactional
    public BudgetResponse setBudget(BudgetRequest request) {
        User user = currentUserService.getCurrentUser();

        // Danh mục phải thuộc về user
        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", request.categoryId()));

        // Ngân sách chỉ áp dụng cho danh mục CHI (EXPENSE)
        if (category.getType() != CategoryType.EXPENSE) {
            throw new BadRequestException("Chỉ đặt ngân sách cho danh mục chi tiêu (EXPENSE)");
        }

        Optional<Budget> existing = budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                user.getId(), category.getId(), request.month(), request.year());

        Budget budget;
        if (existing.isPresent()) {
            budget = existing.get();
            budget.setAmountLimit(request.amountLimit());   // cập nhật (dirty checking)
        } else {
            budget = new Budget(user, category, request.amountLimit(), request.month(), request.year());
            budgetRepository.save(budget);
        }
        return BudgetResponse.from(budget);
    }

    /**
     * Trạng thái ngân sách trong tháng + CẢNH BÁO VƯỢT.
     *
     * Cách tính:
     *   - Lấy tổng chi mỗi danh mục trong tháng (tái dùng query breakdown ở Bước 7).
     *   - Với từng ngân sách: spent = tổng chi của danh mục đó.
     *       isOverBudget    = spent > hạn mức
     *       overspentAmount = max(0, spent - hạn mức)
     *       remaining       = hạn mức - spent
     */
    @Transactional(readOnly = true)
    public List<BudgetStatusResponse> getStatus(int month, int year) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("month phải nằm trong khoảng 1..12");
        }
        User user = currentUserService.getCurrentUser();

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);

        // Map: categoryId -> tổng chi trong tháng
        Map<Long, BigDecimal> spentByCategory = transactionRepository
                .breakdownByCategory(user.getId(), start, end)
                .stream()
                .collect(Collectors.toMap(CategoryBreakdown::categoryId, CategoryBreakdown::total));

        List<Budget> budgets = budgetRepository
                .findByUserIdAndMonthAndYearOrderByIdAsc(user.getId(), month, year);

        return budgets.stream().map(budget -> {
            BigDecimal limit = budget.getAmountLimit();
            BigDecimal spent = spentByCategory.getOrDefault(budget.getCategory().getId(), BigDecimal.ZERO);

            boolean overBudget = spent.compareTo(limit) > 0;                 // so sánh BigDecimal bằng compareTo
            BigDecimal overspent = overBudget ? spent.subtract(limit) : BigDecimal.ZERO;
            BigDecimal remaining = limit.subtract(spent);

            return new BudgetStatusResponse(
                    budget.getId(),
                    budget.getCategory().getId(),
                    budget.getCategory().getName(),
                    limit, spent, remaining, overBudget, overspent);
        }).toList();
    }
}

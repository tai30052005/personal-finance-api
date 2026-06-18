package com.example.financeapi;

import com.example.financeapi.dto.request.BudgetRequest;
import com.example.financeapi.dto.response.BudgetStatusResponse;
import com.example.financeapi.entity.Category;
import com.example.financeapi.entity.CategoryType;
import com.example.financeapi.entity.Transaction;
import com.example.financeapi.entity.User;
import com.example.financeapi.exception.BadRequestException;
import com.example.financeapi.repository.CategoryRepository;
import com.example.financeapi.repository.TransactionRepository;
import com.example.financeapi.repository.UserRepository;
import com.example.financeapi.service.BudgetService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test BUSINESS LOGIC nổi bật: cảnh báo vượt ngân sách.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BudgetServiceTest {

    @Autowired private BudgetService budgetService;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private TransactionRepository transactionRepository;

    private User user;
    private Category expenseCategory;   // Ăn uống (EXPENSE)
    private Category incomeCategory;    // Lương (INCOME)

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("user@test.com", "hash"));
        expenseCategory = categoryRepository.save(new Category(user, "An uong", CategoryType.EXPENSE));
        incomeCategory = categoryRepository.save(new Category(user, "Luong", CategoryType.INCOME));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of()));
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    private void spend(String amount, LocalDate date) {
        transactionRepository.save(new Transaction(
                user, expenseCategory, new BigDecimal(amount), "chi", date));
    }

    @Test
    void overspending_triggersAlert() {
        // Ngân sách 1.000.000đ
        budgetService.setBudget(new BudgetRequest(
                expenseCategory.getId(), new BigDecimal("1000000"), 6, 2026));

        // Chi tổng 1.100.000đ -> VƯỢT 100.000đ
        spend("600000", LocalDate.of(2026, 6, 5));
        spend("500000", LocalDate.of(2026, 6, 15));

        List<BudgetStatusResponse> status = budgetService.getStatus(6, 2026);

        assertThat(status).hasSize(1);
        BudgetStatusResponse s = status.get(0);
        assertThat(s.spent()).isEqualByComparingTo("1100000");
        assertThat(s.overBudget()).isTrue();                       // isOverBudget = true
        assertThat(s.overspentAmount()).isEqualByComparingTo("100000");
    }

    @Test
    void spendingWithinLimit_noAlert() {
        budgetService.setBudget(new BudgetRequest(
                expenseCategory.getId(), new BigDecimal("1000000"), 6, 2026));

        spend("500000", LocalDate.of(2026, 6, 5));   // chỉ chi 500k < 1tr

        BudgetStatusResponse s = budgetService.getStatus(6, 2026).get(0);
        assertThat(s.overBudget()).isFalse();
        assertThat(s.overspentAmount()).isEqualByComparingTo("0");
        assertThat(s.remaining()).isEqualByComparingTo("500000");
    }

    @Test
    void budgetOnIncomeCategory_isRejected() {
        // Không cho đặt ngân sách cho danh mục thu (INCOME) -> BadRequestException (400).
        assertThatThrownBy(() -> budgetService.setBudget(new BudgetRequest(
                incomeCategory.getId(), new BigDecimal("1000000"), 6, 2026)))
                .isInstanceOf(BadRequestException.class);
    }
}

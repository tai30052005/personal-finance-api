package com.example.financeapi;

import com.example.financeapi.dto.response.ActivityDay;
import com.example.financeapi.entity.Category;
import com.example.financeapi.entity.CategoryType;
import com.example.financeapi.entity.Transaction;
import com.example.financeapi.entity.User;
import com.example.financeapi.exception.BadRequestException;
import com.example.financeapi.repository.CategoryRepository;
import com.example.financeapi.repository.TransactionRepository;
import com.example.financeapi.repository.UserRepository;
import com.example.financeapi.service.ReportService;
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
 * Test endpoint hoạt động ghi chép (heatmap "chăm vườn"):
 * đếm giao dịch theo ngày TẠO + kiểm tra biên tham số days.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReportActivityTest {

    @Autowired private ReportService reportService;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private TransactionRepository transactionRepository;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("activity@test.com", "hash"));
        category = categoryRepository.save(new Category(user, "An uong", CategoryType.EXPENSE));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of()));
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void activity_countsTransactionsByCreationDay() {
        // 3 giao dịch tạo trong lần chạy test này -> đều rơi vào NGÀY TẠO = hôm nay.
        for (int i = 0; i < 3; i++) {
            transactionRepository.save(new Transaction(
                    user, category, new BigDecimal("10000"), "chi", LocalDate.now()));
        }

        List<ActivityDay> activity = reportService.activity(28);

        long todayCount = activity.stream()
                .filter(a -> a.date().equals(LocalDate.now()))
                .mapToLong(ActivityDay::count)
                .sum();
        assertThat(todayCount).isEqualTo(3);
    }

    @Test
    void activity_rejectsOutOfRangeDays() {
        assertThatThrownBy(() -> reportService.activity(0))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> reportService.activity(91))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void activity_isEmptyForUserWithNoTransactions() {
        assertThat(reportService.activity(28)).isEmpty();
    }
}

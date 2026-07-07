package com.example.financeapi;

import com.example.financeapi.dto.request.SavingsGoalRequest;
import com.example.financeapi.dto.response.SavingsGoalResponse;
import com.example.financeapi.entity.User;
import com.example.financeapi.exception.ResourceNotFoundException;
import com.example.financeapi.repository.UserRepository;
import com.example.financeapi.service.SavingsGoalService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test mục tiêu tiết kiệm: góp tiền -> % tiến độ + cờ hoàn thành, và cách ly theo user.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SavingsGoalServiceTest {

    @Autowired private SavingsGoalService goalService;
    @Autowired private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("goal-owner@test.com", "hash"));
        loginAs(user);
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(User u) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(u.getEmail(), null, List.of()));
    }

    @Test
    void contributing_updatesProgressAndCompletion() {
        SavingsGoalResponse goal = goalService.create(
                new SavingsGoalRequest("Mua laptop", new BigDecimal("1000000"), null));
        assertThat(goal.progressPercent()).isEqualTo(0.0);
        assertThat(goal.completed()).isFalse();

        // Góp 500k -> 50%, chưa xong.
        SavingsGoalResponse half = goalService.contribute(goal.id(), new BigDecimal("500000"));
        assertThat(half.currentAmount()).isEqualByComparingTo("500000");
        assertThat(half.progressPercent()).isEqualTo(50.0);
        assertThat(half.completed()).isFalse();

        // Góp thêm 500k -> đủ 100%, hoàn thành.
        SavingsGoalResponse full = goalService.contribute(goal.id(), new BigDecimal("500000"));
        assertThat(full.currentAmount()).isEqualByComparingTo("1000000");
        assertThat(full.progressPercent()).isEqualTo(100.0);
        assertThat(full.completed()).isTrue();
    }

    @Test
    void goalsAreIsolatedPerUser() {
        SavingsGoalResponse mine = goalService.create(
                new SavingsGoalRequest("Quỹ khẩn cấp", new BigDecimal("2000000"), null));

        // Người khác đăng nhập -> không thấy và không góp được vào mục tiêu của user kia.
        User other = userRepository.save(new User("intruder@test.com", "hash"));
        loginAs(other);

        assertThat(goalService.listMine()).isEmpty();
        assertThatThrownBy(() -> goalService.contribute(mine.id(), new BigDecimal("100000")))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> goalService.delete(mine.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

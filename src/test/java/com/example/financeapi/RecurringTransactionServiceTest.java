package com.example.financeapi;

import com.example.financeapi.dto.request.RecurringRequest;
import com.example.financeapi.dto.response.RecurringResponse;
import com.example.financeapi.dto.response.TransactionResponse;
import com.example.financeapi.entity.Category;
import com.example.financeapi.entity.CategoryType;
import com.example.financeapi.entity.User;
import com.example.financeapi.exception.ResourceNotFoundException;
import com.example.financeapi.repository.CategoryRepository;
import com.example.financeapi.repository.UserRepository;
import com.example.financeapi.service.RecurringTransactionService;
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
 * Test khoản định kỳ: "Ghi ngay" tạo giao dịch thật, và cách ly theo user.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RecurringTransactionServiceTest {

    @Autowired private RecurringTransactionService recurringService;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;

    private User user;
    private Category rentCategory;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("recurring@test.com", "hash"));
        rentCategory = categoryRepository.save(new Category(user, "Tien nha", CategoryType.EXPENSE));
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
    void runNow_createsTransactionFromTemplate() {
        RecurringResponse r = recurringService.create(new RecurringRequest(
                rentCategory.getId(), new BigDecimal("3000000"), "Tiền thuê nhà", 1, true));

        TransactionResponse tx = recurringService.runNow(r.id());

        assertThat(tx.id()).isNotNull();
        assertThat(tx.amount()).isEqualByComparingTo("3000000");
        assertThat(tx.note()).isEqualTo("Tiền thuê nhà");
    }

    @Test
    void recurringIsIsolatedPerUser() {
        RecurringResponse r = recurringService.create(new RecurringRequest(
                rentCategory.getId(), new BigDecimal("500000"), "Internet", 5, true));

        // Người khác không được "ghi ngay" hay xóa khoản định kỳ của user kia.
        User other = userRepository.save(new User("stranger@test.com", "hash"));
        loginAs(other);

        assertThat(recurringService.listMine()).isEmpty();
        assertThatThrownBy(() -> recurringService.runNow(r.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> recurringService.delete(r.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

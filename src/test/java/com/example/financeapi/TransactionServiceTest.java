package com.example.financeapi;

import com.example.financeapi.dto.request.TransactionRequest;
import com.example.financeapi.dto.response.TransactionResponse;
import com.example.financeapi.entity.Category;
import com.example.financeapi.entity.CategoryType;
import com.example.financeapi.entity.User;
import com.example.financeapi.exception.ResourceNotFoundException;
import com.example.financeapi.repository.CategoryRepository;
import com.example.financeapi.repository.UserRepository;
import com.example.financeapi.service.TransactionService;
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
 * Test tầng service cho giao dịch — tập trung vào CÔ LẬP DỮ LIỆU theo user.
 *
 * @Transactional: mỗi test tự rollback sau khi chạy -> DB sạch giữa các test.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionServiceTest {

    @Autowired private TransactionService transactionService;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;

    private User alice;
    private User bob;
    private Category aliceCategory;
    private Category bobCategory;

    @BeforeEach
    void setUp() {
        alice = userRepository.save(new User("alice@test.com", "hash"));
        bob = userRepository.save(new User("bob@test.com", "hash"));
        aliceCategory = categoryRepository.save(new Category(alice, "An uong", CategoryType.EXPENSE));
        bobCategory = categoryRepository.save(new Category(bob, "Bob cat", CategoryType.EXPENSE));
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    /** Giả lập user đang đăng nhập (CurrentUserService đọc email từ đây). */
    private void loginAs(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }

    @Test
    void createValidTransaction_succeeds() {
        loginAs(alice.getEmail());
        TransactionRequest req = new TransactionRequest(
                new BigDecimal("50000"), aliceCategory.getId(), "Pho", LocalDate.of(2026, 6, 1));

        TransactionResponse res = transactionService.create(req);

        assertThat(res.id()).isNotNull();
        assertThat(res.amount()).isEqualByComparingTo("50000");
        assertThat(res.categoryName()).isEqualTo("An uong");
    }

    @Test
    void createTransaction_withForeignCategory_throwsNotFound() {
        // Alice cố gắn giao dịch vào danh mục của Bob -> bị từ chối (404).
        loginAs(alice.getEmail());
        TransactionRequest req = new TransactionRequest(
                new BigDecimal("1000"), bobCategory.getId(), "muon", LocalDate.of(2026, 6, 1));

        assertThatThrownBy(() -> transactionService.create(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void userCannotSeeOrModify_othersTransactions() {
        // Alice tạo 1 giao dịch
        loginAs(alice.getEmail());
        TransactionResponse aliceTx = transactionService.create(new TransactionRequest(
                new BigDecimal("99000"), aliceCategory.getId(), "Alice", LocalDate.of(2026, 6, 1)));

        // Chuyển sang Bob
        loginAs(bob.getEmail());

        // Bob không thấy giao dịch nào
        assertThat(transactionService.search(null, null, null)).isEmpty();

        // Bob không xóa được giao dịch của Alice -> 404
        assertThatThrownBy(() -> transactionService.delete(aliceTx.id()))
                .isInstanceOf(ResourceNotFoundException.class);

        // Bob không sửa được giao dịch của Alice -> 404
        TransactionRequest upd = new TransactionRequest(
                new BigDecimal("1"), bobCategory.getId(), "hack", LocalDate.of(2026, 6, 1));
        assertThatThrownBy(() -> transactionService.update(aliceTx.id(), upd))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

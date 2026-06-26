package com.example.financeapi.service;

import com.example.financeapi.dto.request.RecurringRequest;
import com.example.financeapi.dto.response.RecurringResponse;
import com.example.financeapi.dto.response.TransactionResponse;
import com.example.financeapi.entity.Category;
import com.example.financeapi.entity.RecurringTransaction;
import com.example.financeapi.entity.Transaction;
import com.example.financeapi.entity.User;
import com.example.financeapi.exception.ResourceNotFoundException;
import com.example.financeapi.repository.CategoryRepository;
import com.example.financeapi.repository.RecurringTransactionRepository;
import com.example.financeapi.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    public RecurringTransactionService(RecurringTransactionRepository recurringRepository,
                                       CategoryRepository categoryRepository,
                                       TransactionRepository transactionRepository,
                                       CurrentUserService currentUserService) {
        this.recurringRepository = recurringRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<RecurringResponse> listMine() {
        User user = currentUserService.getCurrentUser();
        return recurringRepository.findByUserIdOrderByIdAsc(user.getId())
                .stream().map(RecurringResponse::from).toList();
    }

    @Transactional
    public RecurringResponse create(RecurringRequest request) {
        User user = currentUserService.getCurrentUser();
        Category category = getOwnedCategory(request.categoryId(), user);

        RecurringTransaction r = new RecurringTransaction(
                user, category, request.amount(), request.note(), request.dayOfMonth());
        if (request.active() != null) {
            r.setActive(request.active());
        }
        recurringRepository.save(r);
        return RecurringResponse.from(r);
    }

    @Transactional
    public RecurringResponse update(Long id, RecurringRequest request) {
        User user = currentUserService.getCurrentUser();
        RecurringTransaction r = getOwnedOrThrow(id, user);
        Category category = getOwnedCategory(request.categoryId(), user);

        r.setCategory(category);
        r.setAmount(request.amount());
        r.setNote(request.note());
        r.setDayOfMonth(request.dayOfMonth());
        if (request.active() != null) {
            r.setActive(request.active());
        }
        return RecurringResponse.from(r);
    }

    @Transactional
    public void delete(Long id) {
        User user = currentUserService.getCurrentUser();
        recurringRepository.delete(getOwnedOrThrow(id, user));
    }

    /**
     * "Ghi ngay" — tạo ngay một giao dịch từ khoản định kỳ này (không chờ tới ngày).
     * Tiện cho người dùng + để demo tính năng.
     */
    @Transactional
    public TransactionResponse runNow(Long id) {
        User user = currentUserService.getCurrentUser();
        RecurringTransaction r = getOwnedOrThrow(id, user);
        Transaction t = createTransactionFrom(r, LocalDate.now());
        return TransactionResponse.from(t);
    }

    /**
     * Được scheduled job gọi (KHÔNG gắn user đang đăng nhập).
     * Quét mọi khoản đang bật có ngày = hôm nay, chưa sinh trong tháng này -> tạo giao dịch.
     * Trả về số giao dịch đã sinh.
     */
    @Transactional
    public int generateDueToday() {
        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        List<RecurringTransaction> due = recurringRepository.findByActiveTrueAndDayOfMonth(today.getDayOfMonth());

        int count = 0;
        for (RecurringTransaction r : due) {
            boolean alreadyThisMonth = r.getLastRunDate() != null && !r.getLastRunDate().isBefore(firstOfMonth);
            if (!alreadyThisMonth) {
                createTransactionFrom(r, today);
                count++;
            }
        }
        return count;
    }

    // ----- helpers -----

    private Transaction createTransactionFrom(RecurringTransaction r, LocalDate date) {
        Transaction t = new Transaction(r.getUser(), r.getCategory(), r.getAmount(), r.getNote(), date);
        transactionRepository.save(t);
        r.setLastRunDate(date);   // đánh dấu đã sinh
        return t;
    }

    private RecurringTransaction getOwnedOrThrow(Long id, User user) {
        return recurringRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Khoản định kỳ", id));
    }

    private Category getOwnedCategory(Long categoryId, User user) {
        return categoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", categoryId));
    }
}

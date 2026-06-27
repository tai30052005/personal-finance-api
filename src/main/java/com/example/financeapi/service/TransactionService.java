package com.example.financeapi.service;

import com.example.financeapi.dto.request.TransactionFilter;
import com.example.financeapi.dto.request.TransactionRequest;
import com.example.financeapi.dto.response.TransactionResponse;
import com.example.financeapi.entity.Category;
import com.example.financeapi.entity.Transaction;
import com.example.financeapi.entity.User;
import com.example.financeapi.exception.BadRequestException;
import com.example.financeapi.exception.ResourceNotFoundException;
import com.example.financeapi.repository.CategoryRepository;
import com.example.financeapi.repository.TransactionRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository,
                              CurrentUserService currentUserService) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Liệt kê giao dịch của user, lọc tùy chọn theo tháng/năm/danh mục.
     * Quy đổi (month, year) -> khoảng ngày [start, end) để lọc trên cột DATE.
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> search(TransactionFilter filter) {
        User user = currentUserService.getCurrentUser();
        Integer month = filter.month();
        Integer year = filter.year();

        // Kiểm tra logic bộ lọc trước khi dựng khoảng ngày
        if (month != null && (month < 1 || month > 12)) {
            throw new BadRequestException("month phải nằm trong khoảng 1..12");
        }
        if (month != null && year == null) {
            throw new BadRequestException("Cần cung cấp 'year' khi lọc theo 'month'");
        }
        if (filter.minAmount() != null && filter.maxAmount() != null
                && filter.minAmount().compareTo(filter.maxAmount()) > 0) {
            throw new BadRequestException("minAmount không được lớn hơn maxAmount");
        }

        LocalDate start = null;
        LocalDate end = null;
        if (year != null) {
            if (month != null) {
                start = LocalDate.of(year, month, 1);   // đầu tháng
                end = start.plusMonths(1);              // đầu tháng kế tiếp
            } else {
                start = LocalDate.of(year, 1, 1);       // đầu năm
                end = start.plusYears(1);               // đầu năm kế tiếp
            }
        }

        // Dựng bộ điều kiện ĐỘNG: chỉ thêm điều kiện nào có giá trị.
        Specification<Transaction> spec = buildFilter(user.getId(), filter, start, end);
        // Sắp xếp: mới nhất trước (theo ngày, rồi theo id).
        Sort sort = Sort.by(Sort.Order.desc("occurredAt"), Sort.Order.desc("id"));

        return transactionRepository.findAll(spec, sort)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    /**
     * Specification = một hàm dựng điều kiện WHERE bằng Criteria API.
     *   root : đại diện bảng transactions (để lấy cột: root.get("...")).
     *   cb   : "thợ xây" tạo các phép so sánh (equal, >=, < ...).
     * Ta gom các điều kiện vào danh sách rồi nối bằng AND. Điều kiện nào null thì bỏ qua
     * -> SQL sinh ra gọn, không dính bẫy "tham số không rõ kiểu".
     */
    private Specification<Transaction> buildFilter(Long userId, TransactionFilter filter,
                                                   LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));   // luôn lọc theo user
            if (filter.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.categoryId()));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThan(root.get("occurredAt"), end));
            }
            // Tìm theo từ khóa trong ghi chú (không phân biệt hoa/thường).
            if (filter.keyword() != null && !filter.keyword().isBlank()) {
                String like = "%" + filter.keyword().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("note")), like));
            }
            // Lọc theo khoảng số tiền.
            if (filter.minAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.minAmount()));
            }
            if (filter.maxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.maxAmount()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        User user = currentUserService.getCurrentUser();
        Category category = getOwnedCategory(request.categoryId(), user);   // validation chéo

        Transaction transaction = new Transaction(
                user, category, request.amount(), request.note(), request.occurredAt());
        transactionRepository.save(transaction);
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        User user = currentUserService.getCurrentUser();
        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Giao dịch", id));

        Category category = getOwnedCategory(request.categoryId(), user);
        transaction.setAmount(request.amount());
        transaction.setCategory(category);
        transaction.setNote(request.note());
        transaction.setOccurredAt(request.occurredAt());
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public void delete(Long id) {
        User user = currentUserService.getCurrentUser();
        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Giao dịch", id));
        transactionRepository.delete(transaction);
    }

    /**
     * Lấy danh mục theo id NHƯNG phải thuộc về user hiện tại.
     * Đây là "validation chéo": không cho gắn giao dịch vào danh mục của người khác.
     */
    private Category getOwnedCategory(Long categoryId, User user) {
        return categoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", categoryId));
    }
}

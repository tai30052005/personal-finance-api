package com.example.financeapi.service;

import com.example.financeapi.dto.request.CategoryRequest;
import com.example.financeapi.dto.response.CategoryResponse;
import com.example.financeapi.entity.Category;
import com.example.financeapi.entity.User;
import com.example.financeapi.exception.ResourceNotFoundException;
import com.example.financeapi.repository.BudgetRepository;
import com.example.financeapi.repository.CategoryRepository;
import com.example.financeapi.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logic nghiệp vụ cho danh mục. Mọi thao tác đều gắn với user đang đăng nhập.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final CurrentUserService currentUserService;

    public CategoryService(CategoryRepository categoryRepository,
                           TransactionRepository transactionRepository,
                           BudgetRepository budgetRepository,
                           CurrentUserService currentUserService) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Liệt kê danh mục của user hiện tại.
     * @Transactional(readOnly = true): chỉ đọc -> tối ưu, không cần ghi/rollback.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> listMine() {
        User user = currentUserService.getCurrentUser();
        return categoryRepository.findByUserIdOrderByIdAsc(user.getId())
                .stream()
                .map(CategoryResponse::from)   // chuyển từng entity -> DTO
                .toList();
    }

    /** Tạo danh mục mới cho user hiện tại. */
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        User user = currentUserService.getCurrentUser();
        Category category = new Category(user, request.name(), request.type());
        categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    /** Sửa danh mục — chỉ khi danh mục thuộc về user hiện tại. */
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getOwnedOrThrow(id);
        category.setName(request.name());
        category.setType(request.type());
        // Không cần gọi save(): trong @Transactional, Hibernate tự phát hiện thay đổi
        // (dirty checking) và cập nhật khi commit.
        return CategoryResponse.from(category);
    }

    /**
     * Xóa danh mục — chỉ khi thuộc về user hiện tại.
     * CASCADE: xóa luôn các giao dịch và ngân sách thuộc danh mục đó trước,
     * rồi mới xóa danh mục (tránh vi phạm khóa ngoại). Tất cả trong 1 transaction.
     */
    @Transactional
    public void delete(Long id) {
        Category category = getOwnedOrThrow(id);
        transactionRepository.deleteByCategoryId(id);
        budgetRepository.deleteByCategoryId(id);
        categoryRepository.delete(category);
    }

    /**
     * Lấy danh mục theo id NHƯNG bắt buộc thuộc về user hiện tại.
     * Nếu không có (hoặc của user khác) -> ném 404. Đây là chốt chặn cô lập dữ liệu.
     */
    private Category getOwnedOrThrow(Long id) {
        User user = currentUserService.getCurrentUser();
        return categoryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", id));
    }
}

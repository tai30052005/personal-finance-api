package com.example.financeapi.repository;

import com.example.financeapi.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Kho cho Category.
 *
 * Chú ý các method đều có "...AndUserId" / "ByUserId": đây là cách ép MỌI truy vấn
 * phải kèm điều kiện user_id -> đảm bảo cô lập dữ liệu theo từng người dùng.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Liệt kê danh mục của 1 user, sắp xếp theo id tăng dần.
    List<Category> findByUserIdOrderByIdAsc(Long userId);

    // Tìm 1 danh mục THEO id NHƯNG phải thuộc về đúng user đó.
    // Nếu id thuộc user khác -> trả về rỗng (sẽ thành 404), không lộ dữ liệu.
    Optional<Category> findByIdAndUserId(Long id, Long userId);
}

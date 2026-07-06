package com.example.financeapi.repository;

import com.example.financeapi.dto.response.CategoryBreakdown;
import com.example.financeapi.entity.CategoryType;
import com.example.financeapi.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Kho cho Transaction.
 *
 * JpaSpecificationExecutor: cho phép truyền vào một "Specification" — tức là
 * tập điều kiện lọc dựng ĐỘNG lúc chạy (Criteria API). Nhờ đó ta chỉ thêm
 * điều kiện nào thực sự cần, tránh được bẫy "tham số không xác định kiểu" của SQL.
 */
public interface TransactionRepository
        extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    // Lấy 1 giao dịch nhưng phải thuộc về đúng user (cô lập dữ liệu).
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    // Xóa tất cả giao dịch thuộc một danh mục (dùng khi xóa danh mục - cascade).
    void deleteByCategoryId(Long categoryId);

    /**
     * Tổng tiền theo TỪNG DANH MỤC trong khoảng [start, end) cho 1 user.
     *
     * - JOIN t.category c: nối sang bảng danh mục để lấy tên/loại.
     * - GROUP BY: gom theo danh mục rồi SUM(amount) cho mỗi nhóm.
     * - SELECT new ...CategoryBreakdown(...): "projection" — map thẳng mỗi dòng
     *   kết quả vào DTO qua constructor (không cần xử lý Object[] thủ công).
     *
     * Từ kết quả này, tầng service tự cộng ra tổng thu / tổng chi / số dư.
     */
    @Query("""
            SELECT new com.example.financeapi.dto.response.CategoryBreakdown(
                       c.id, c.name, c.type, SUM(t.amount))
            FROM Transaction t
            JOIN t.category c
            WHERE t.user.id = :userId
              AND t.occurredAt >= :start
              AND t.occurredAt <  :end
            GROUP BY c.id, c.name, c.type
            ORDER BY c.type, SUM(t.amount) DESC
            """)
    List<CategoryBreakdown> breakdownByCategory(@Param("userId") Long userId,
                                                @Param("start") LocalDate start,
                                                @Param("end") LocalDate end);

    /**
     * Tổng tiền theo TỪNG THÁNG và theo LOẠI (INCOME/EXPENSE) trong khoảng [start, end).
     * Dùng để vẽ biểu đồ thu/chi 12 tháng.
     *
     * EXTRACT(MONTH FROM ...) lấy số tháng (1..12). Mỗi dòng kết quả là:
     *   [ số tháng, loại danh mục, tổng tiền ]
     * (Trả Object[] vì gom 3 cột khác kiểu; tầng service sẽ dựng lại thành 12 tháng.)
     */
    @Query("""
            SELECT EXTRACT(MONTH FROM t.occurredAt), c.type, SUM(t.amount)
            FROM Transaction t
            JOIN t.category c
            WHERE t.user.id = :userId
              AND t.occurredAt >= :start
              AND t.occurredAt <  :end
            GROUP BY EXTRACT(MONTH FROM t.occurredAt), c.type
            """)
    List<Object[]> monthlyTotalsByType(@Param("userId") Long userId,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end);

    /**
     * Vài cặp "ghi chú -> tên danh mục" GẦN ĐÂY (chỉ giao dịch có ghi chú), mới nhất trước.
     * Dùng làm ví dụ để AI học thói quen phân loại của chính người dùng.
     */
    @Query("""
            SELECT t.note AS note, c.name AS category
            FROM Transaction t
            JOIN t.category c
            WHERE t.user.id = :userId
              AND t.note IS NOT NULL
              AND t.note <> ''
            ORDER BY t.occurredAt DESC, t.id DESC
            """)
    List<NoteCategoryView> recentNoteCategories(@Param("userId") Long userId, Pageable pageable);

    /** Projection: chỉ lấy ghi chú + tên danh mục (tránh tải lazy cả entity). */
    interface NoteCategoryView {
        String getNote();
        String getCategory();
    }

    /**
     * Các giao dịch LỚN NHẤT trong khoảng [start, end) của 1 user (số tiền giảm dần).
     * Dùng cho trợ lý chat trả lời câu hỏi ở cấp GIAO DỊCH ("giao dịch lớn nhất", "mua gì").
     */
    @Query("""
            SELECT t.amount AS amount, c.name AS category, c.type AS type,
                   t.note AS note, t.occurredAt AS occurredAt
            FROM Transaction t
            JOIN t.category c
            WHERE t.user.id = :userId
              AND t.occurredAt >= :start
              AND t.occurredAt <  :end
            ORDER BY t.amount DESC, t.id DESC
            """)
    List<TopTxView> topTransactions(@Param("userId") Long userId,
                                    @Param("start") LocalDate start,
                                    @Param("end") LocalDate end,
                                    Pageable pageable);

    /** Projection cho 1 giao dịch lớn (không tải cả entity). */
    interface TopTxView {
        BigDecimal getAmount();
        String getCategory();
        CategoryType getType();
        String getNote();
        LocalDate getOccurredAt();
    }

    /**
     * Số giao dịch GHI trong từng ngày (theo createdAt) kể từ :since — đo mức độ
     * "chăm ghi chép" của người dùng (heatmap chuỗi ngày chăm vườn, concept Vườn Xanh).
     */
    @Query("""
            SELECT cast(t.createdAt as date), COUNT(t.id)
            FROM Transaction t
            WHERE t.user.id = :userId
              AND t.createdAt >= :since
            GROUP BY cast(t.createdAt as date)
            """)
    List<Object[]> activityByDay(@Param("userId") Long userId,
                                 @Param("since") java.time.LocalDateTime since);
}

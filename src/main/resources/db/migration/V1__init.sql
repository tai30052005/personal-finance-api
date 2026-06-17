-- ============================================================
--  V1__init.sql  — Flyway migration đầu tiên: tạo toàn bộ bảng
--  Quy ước đặt tên file Flyway:  V<version>__<description>.sql
--  Flyway chạy các file theo thứ tự version (V1, V2, ...) đúng 1 lần,
--  và ghi lại lịch sử vào bảng flyway_schema_history.
-- ============================================================

-- Bảng người dùng
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,                 -- khóa chính tự tăng (bigint)
    email         VARCHAR(255) NOT NULL UNIQUE,          -- email là duy nhất
    password_hash VARCHAR(255) NOT NULL,                 -- lưu mật khẩu ĐÃ băm (BCrypt), không lưu plaintext
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- Bảng danh mục thu/chi, thuộc về 1 user
CREATE TABLE categories (
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,  -- xóa user -> xóa luôn danh mục
    name    VARCHAR(100) NOT NULL,
    type    VARCHAR(20)  NOT NULL          -- 'INCOME' | 'EXPENSE' (lưu dạng chuỗi)
);

-- Bảng giao dịch
CREATE TABLE transactions (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT        NOT NULL REFERENCES categories(id),
    amount      NUMERIC(15,2) NOT NULL CHECK (amount > 0),   -- tiền: dùng NUMERIC, ràng buộc > 0 ngay ở DB
    note        VARCHAR(255),
    occurred_at DATE          NOT NULL,                      -- ngày phát sinh giao dịch
    created_at  TIMESTAMP     NOT NULL DEFAULT now()
);

-- Bảng ngân sách theo tháng cho 1 danh mục
CREATE TABLE budgets (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id  BIGINT        NOT NULL REFERENCES categories(id),
    amount_limit NUMERIC(15,2) NOT NULL,
    month        INTEGER       NOT NULL CHECK (month BETWEEN 1 AND 12),
    year         INTEGER       NOT NULL,
    -- mỗi user chỉ có 1 ngân sách cho 1 danh mục trong 1 tháng/năm
    CONSTRAINT uq_budget UNIQUE (user_id, category_id, month, year)
);

-- Index tăng tốc các truy vấn hay dùng
CREATE INDEX idx_transactions_user_occurred ON transactions(user_id, occurred_at);
CREATE INDEX idx_categories_user            ON categories(user_id);
CREATE INDEX idx_budgets_user               ON budgets(user_id);

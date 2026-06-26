-- V2: Bảng "khoản định kỳ" — quy tắc tự sinh giao dịch hàng tháng.
CREATE TABLE recurring_transactions (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id   BIGINT        NOT NULL REFERENCES categories(id),
    amount        NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    note          VARCHAR(255),
    day_of_month  INTEGER       NOT NULL CHECK (day_of_month BETWEEN 1 AND 28),
    active        BOOLEAN       NOT NULL DEFAULT TRUE,
    last_run_date DATE,
    created_at    TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_recurring_user ON recurring_transactions(user_id);

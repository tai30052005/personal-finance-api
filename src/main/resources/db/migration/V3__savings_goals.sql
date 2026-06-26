-- V3: Bảng "mục tiêu tiết kiệm".
CREATE TABLE savings_goals (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name           VARCHAR(100)  NOT NULL,
    target_amount  NUMERIC(15,2) NOT NULL CHECK (target_amount > 0),
    current_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    deadline       DATE,
    created_at     TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_savings_user ON savings_goals(user_id);

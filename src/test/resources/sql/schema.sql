CREATE SCHEMA IF NOT EXISTS tricount_schema;

CREATE TABLE tricount_schema.users
(
    telegram_id BIGINT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL
);

-- Таблица групп
CREATE TABLE tricount_schema.groups
(
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(200) NOT NULL,
    created_by_user_id BIGINT       NOT NULL REFERENCES tricount_schema.users (telegram_id) ON DELETE CASCADE,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица участников групп
CREATE TABLE tricount_schema.group_memberships
(
    id        BIGSERIAL PRIMARY KEY,
    group_id  BIGINT NOT NULL REFERENCES tricount_schema.groups (id) ON DELETE CASCADE,
    user_id   BIGINT NOT NULL REFERENCES tricount_schema.users (telegram_id) ON DELETE CASCADE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (group_id, user_id)
);

-- Таблица расходов
CREATE TABLE tricount_schema.expenses
(
    id              BIGSERIAL PRIMARY KEY,
    group_id        BIGINT         NOT NULL REFERENCES tricount_schema.groups (id) ON DELETE CASCADE,
    paid_by_user_id BIGINT         NOT NULL REFERENCES tricount_schema.users (telegram_id) ON DELETE CASCADE,
    title           VARCHAR(200)   NOT NULL,
    amount          NUMERIC(10, 2) NOT NULL CHECK (amount > 0),
    date            TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица долей расходов
CREATE TABLE tricount_schema.expense_shares
(
    id         BIGSERIAL PRIMARY KEY,
    expense_id BIGINT         NOT NULL REFERENCES tricount_schema.expenses (id) ON DELETE CASCADE,
    user_id    BIGINT         NOT NULL REFERENCES tricount_schema.users (telegram_id) ON DELETE CASCADE,
    amount     NUMERIC(10, 2) NOT NULL CHECK (amount >= 0),
    UNIQUE (expense_id, user_id)
);

-- Индексы
CREATE INDEX idx_group_memberships_group ON tricount_schema.group_memberships (group_id);
CREATE INDEX idx_group_memberships_user ON tricount_schema.group_memberships (user_id);
CREATE INDEX idx_expenses_group ON tricount_schema.expenses (group_id);
CREATE INDEX idx_expenses_payer ON tricount_schema.expenses (paid_by_user_id);
CREATE INDEX idx_expense_shares_expense ON tricount_schema.expense_shares (expense_id);
CREATE INDEX idx_expense_shares_user ON tricount_schema.expense_shares (user_id);
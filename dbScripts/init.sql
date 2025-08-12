CREATE SCHEMA IF NOT EXISTS tricount_schema;
SET
search_path TO tricount_schema;

-- Создаем функции для предоставления прав
CREATE
OR REPLACE FUNCTION grant_privileges_to_user()
RETURNS void AS $$
DECLARE
db_user TEXT := 'tricount_admin';  -- Фиксированное имя пользователя
BEGIN
EXECUTE format('GRANT ALL PRIVILEGES ON SCHEMA tricount_schema TO %I', db_user);
EXECUTE format('GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA tricount_schema TO %I', db_user);
EXECUTE format('GRANT USAGE ON ALL SEQUENCES IN SCHEMA tricount_schema TO %I', db_user);
END;
$$
LANGUAGE plpgsql;

-- Вызываем функцию
SELECT grant_privileges_to_user();
-- Таблица пользователей
CREATE TABLE users
(
    telegram_id BIGINT PRIMARY KEY, -- Используем Telegram ID как PK
    name        VARCHAR(100) NOT NULL
);
-- Таблица групп
CREATE TABLE groups
(
    id                 VARCHAR(10) PRIMARY KEY,
    name               VARCHAR(200) NOT NULL,
    created_by_user_id BIGINT       NOT NULL REFERENCES users (telegram_id) ON DELETE CASCADE,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица участников групп (многие-ко-многим)
CREATE TABLE group_memberships
(
    id        BIGSERIAL PRIMARY KEY,
    group_id  BIGINT NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    user_id   BIGINT NOT NULL REFERENCES users (telegram_id) ON DELETE CASCADE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (group_id, user_id) -- Один пользователь - одна запись в группе
);

-- Таблица расходов
CREATE TABLE expenses
(
    id              BIGSERIAL PRIMARY KEY,
    group_id        BIGINT         NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    paid_by_user_id BIGINT         NOT NULL REFERENCES users (telegram_id) ON DELETE CASCADE,
    title           VARCHAR(200)   NOT NULL,
    amount          NUMERIC(10, 2) NOT NULL CHECK (amount > 0),
    date            TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица долей расходов
CREATE TABLE expense_shares
(
    id         BIGSERIAL PRIMARY KEY,
    expense_id BIGINT         NOT NULL REFERENCES expenses (id) ON DELETE CASCADE,
    user_id    BIGINT         NOT NULL REFERENCES users (telegram_id) ON DELETE CASCADE,
    amount     NUMERIC(10, 2) NOT NULL CHECK (amount >= 0),
    UNIQUE (expense_id, user_id) -- Одна доля на пользователя в расходе
);

-- Оптимизация производительности
CREATE INDEX idx_group_memberships_group ON group_memberships (group_id);
CREATE INDEX idx_group_memberships_user ON group_memberships (user_id);
CREATE INDEX idx_expenses_group ON expenses (group_id);
CREATE INDEX idx_expenses_payer ON expenses (paid_by_user_id);
CREATE INDEX idx_expense_shares_expense ON expense_shares (expense_id);
CREATE INDEX idx_expense_shares_user ON expense_shares (user_id);

-- Функция для автоматического добавления создателя группы в участники
CREATE
OR REPLACE FUNCTION add_creator_to_members()
RETURNS TRIGGER AS $$
BEGIN
INSERT INTO group_memberships (group_id, user_id)
VALUES (NEW.id, NEW.created_by_user_id);
RETURN NEW;
END;
$$
LANGUAGE plpgsql;

-- Триггер для автоматического добавления создателя
CREATE TRIGGER group_created_trigger
    AFTER INSERT
    ON groups
    FOR EACH ROW
    EXECUTE FUNCTION add_creator_to_members();

-- Функция для проверки, что плательщик состоит в группе
CREATE
OR REPLACE FUNCTION validate_expense_payer()
RETURNS TRIGGER AS $$
BEGIN
    IF
NOT EXISTS (
        SELECT 1 FROM group_memberships
        WHERE group_id = NEW.group_id AND user_id = NEW.paid_by_user_id
    ) THEN
        RAISE EXCEPTION 'Payer must be a member of the group';
END IF;
RETURN NEW;
END;
$$
LANGUAGE plpgsql;

-- Триггер для проверки плательщика
CREATE TRIGGER expense_payer_validation
    BEFORE INSERT OR
UPDATE ON expenses
    FOR EACH ROW
    EXECUTE FUNCTION validate_expense_payer();

-- Функция для проверки участников в долях
CREATE
OR REPLACE FUNCTION validate_share_membership()
RETURNS TRIGGER AS $$
BEGIN
    IF
NOT EXISTS (
        SELECT 1
        FROM expenses e
        JOIN group_memberships gm ON e.group_id = gm.group_id
        WHERE e.id = NEW.expense_id AND gm.user_id = NEW.user_id
    ) THEN
        RAISE EXCEPTION 'User must be a member of the expense group';
END IF;
RETURN NEW;
END;
$$
LANGUAGE plpgsql;

-- Триггер для проверки участников
CREATE TRIGGER share_membership_validation
    BEFORE INSERT OR
UPDATE ON expense_shares
    FOR EACH ROW
    EXECUTE FUNCTION validate_share_membership();
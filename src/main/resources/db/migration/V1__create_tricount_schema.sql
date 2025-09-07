-- Создаём схему, если не существует
CREATE SCHEMA IF NOT EXISTS tricount_schema;

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS tricount_schema.users
(
    telegram_id BIGINT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL
    );

-- Таблица групп
CREATE TABLE IF NOT EXISTS tricount_schema.groups
(
    id                 VARCHAR(10) PRIMARY KEY,
    name               VARCHAR(200) NOT NULL,
    created_by_user_id BIGINT       NOT NULL REFERENCES tricount_schema.users (telegram_id) ON DELETE CASCADE,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Таблица участников групп (многие-ко-многим)
CREATE TABLE IF NOT EXISTS tricount_schema.group_memberships
(
    id        BIGSERIAL PRIMARY KEY,
    group_id  VARCHAR(10) NOT NULL REFERENCES tricount_schema.groups (id) ON DELETE CASCADE,
    user_id   BIGINT NOT NULL REFERENCES tricount_schema.users (telegram_id) ON DELETE CASCADE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (group_id, user_id)
    );

-- Таблица расходов
CREATE TABLE IF NOT EXISTS tricount_schema.expenses
(
    id              BIGSERIAL PRIMARY KEY,
    group_id        VARCHAR(10)         NOT NULL REFERENCES tricount_schema.groups (id) ON DELETE CASCADE,
    paid_by_user_id BIGINT         NOT NULL REFERENCES tricount_schema.users (telegram_id) ON DELETE CASCADE,
    title           VARCHAR(200)   NOT NULL,
    amount          NUMERIC(10, 2) NOT NULL CHECK (amount > 0),
    date            TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Таблица долей расходов
CREATE TABLE IF NOT EXISTS tricount_schema.expense_shares
(
    id         BIGSERIAL PRIMARY KEY,
    expense_id BIGINT         NOT NULL REFERENCES tricount_schema.expenses (id) ON DELETE CASCADE,
    user_id    BIGINT         NOT NULL REFERENCES tricount_schema.users (telegram_id) ON DELETE CASCADE,
    amount     NUMERIC(10, 2) NOT NULL CHECK (amount >= 0),
    UNIQUE (expense_id, user_id)
    );

-- Индексы
CREATE INDEX IF NOT EXISTS idx_group_memberships_group ON tricount_schema.group_memberships (group_id);
CREATE INDEX IF NOT EXISTS idx_group_memberships_user ON tricount_schema.group_memberships (user_id);
CREATE INDEX IF NOT EXISTS idx_expenses_group ON tricount_schema.expenses (group_id);
CREATE INDEX IF NOT EXISTS idx_expenses_payer ON tricount_schema.expenses (paid_by_user_id);
CREATE INDEX IF NOT EXISTS idx_expense_shares_expense ON tricount_schema.expense_shares (expense_id);
CREATE INDEX IF NOT EXISTS idx_expense_shares_user ON tricount_schema.expense_shares (user_id);

-- Функция для автоматического добавления создателя группы в участники
CREATE OR REPLACE FUNCTION tricount_schema.add_creator_to_members()
RETURNS TRIGGER AS $$
BEGIN
INSERT INTO tricount_schema.group_memberships (group_id, user_id)
VALUES (NEW.id, NEW.created_by_user_id);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Триггер для автоматического добавления создателя
CREATE TRIGGER group_created_trigger
    AFTER INSERT
    ON tricount_schema.groups
    FOR EACH ROW
    EXECUTE FUNCTION tricount_schema.add_creator_to_members();

-- Функция для проверки, что плательщик состоит в группе
CREATE OR REPLACE FUNCTION tricount_schema.validate_expense_payer()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM tricount_schema.group_memberships
        WHERE group_id = NEW.group_id AND user_id = NEW.paid_by_user_id
    ) THEN
        RAISE EXCEPTION 'Payer must be a member of the group';
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Триггер для проверки плательщика
CREATE TRIGGER expense_payer_validation
    BEFORE INSERT OR UPDATE
                         ON tricount_schema.expenses
                         FOR EACH ROW
                         EXECUTE FUNCTION tricount_schema.validate_expense_payer();

-- Функция для проверки участников в долях
CREATE OR REPLACE FUNCTION tricount_schema.validate_share_membership()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM tricount_schema.expenses e
        JOIN tricount_schema.group_memberships gm ON e.group_id = gm.group_id
        WHERE e.id = NEW.expense_id AND gm.user_id = NEW.user_id
    ) THEN
        RAISE EXCEPTION 'User must be a member of the expense group';
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Триггер для проверки участников
CREATE TRIGGER share_membership_validation
    BEFORE INSERT OR UPDATE
                         ON tricount_schema.expense_shares
                         FOR EACH ROW
                         EXECUTE FUNCTION tricount_schema.validate_share_membership();

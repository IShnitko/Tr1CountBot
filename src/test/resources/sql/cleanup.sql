SET REFERENTIAL_INTEGRITY FALSE;

TRUNCATE TABLE tricount_schema.expense_shares RESTART IDENTITY;

TRUNCATE TABLE tricount_schema.expenses RESTART IDENTITY;

TRUNCATE TABLE tricount_schema.group_memberships RESTART IDENTITY;

TRUNCATE TABLE tricount_schema.groups RESTART IDENTITY;

TRUNCATE TABLE tricount_schema.users RESTART IDENTITY;

-- Включаем проверку внешних ключей обратно
SET REFERENTIAL_INTEGRITY TRUE;

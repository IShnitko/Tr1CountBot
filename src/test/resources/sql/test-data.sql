-- Только INSERT, без DELETE и ALTER SEQUENCE
INSERT INTO tricount_schema.users (telegram_id, name) VALUES
                                                          (123456789, 'Anna'),
                                                          (987654321, 'Boris'),
                                                          (555555555, 'Sveta'),
                                                          (111111111, 'Dima');

INSERT INTO tricount_schema.groups (name, created_by_user_id) VALUES
                                                                  ('Travelling', 123456789),
                                                                  ('Party', 987654321),
                                                                  ('House flipping', 111111111);

-- Ручное добавление создателей (так как триггеры отсутствуют)
INSERT INTO tricount_schema.group_memberships (group_id, user_id) VALUES
                                                                      (1, 123456789),
                                                                      (2, 987654321),
                                                                      (3, 111111111);

-- Дополнительные участники
INSERT INTO tricount_schema.group_memberships (group_id, user_id) VALUES
                                                                      (1, 987654321), (1, 555555555),
                                                                      (2, 555555555), (2, 111111111),
                                                                      (3, 123456789);

-- Расходы
INSERT INTO tricount_schema.expenses (group_id, paid_by_user_id, title, amount, date) VALUES
                                                                                          (1, 123456789, 'Tickets', 15000.00, NOW()),
                                                                                          (1, 987654321, 'Hotel', 35000.00, NOW()),
                                                                                          (1, 555555555, 'Food', 12000.00, NOW()),
                                                                                          (2, 987654321, 'Restaurant', 25000.00, NOW()),
                                                                                          (3, 111111111, 'Wallpapers', 15000.00, NOW()),
                                                                                          (3, 123456789, 'Colors', 8000.00, NOW());

-- Доли расходов
INSERT INTO tricount_schema.expense_shares (expense_id, user_id, amount) VALUES
                                                                             (1, 123456789, 5000.00), (1, 987654321, 5000.00), (1, 555555555, 5000.00),
                                                                             (2, 123456789, 11666.67), (2, 987654321, 11666.67), (2, 555555555, 11666.66),
                                                                             (3, 123456789, 6000.00), (3, 987654321, 6000.00),
                                                                             (4, 987654321, 8333.33), (4, 555555555, 8333.33), (4, 111111111, 8333.34),
                                                                             (5, 111111111, 7500.00), (5, 123456789, 7500.00),
                                                                             (6, 123456789, 8000.00);
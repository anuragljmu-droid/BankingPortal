insert into users (id, first_name, last_name, email, created_at, updated_at)
values (1, 'Demo', 'User', 'demo.user@example.com', current_timestamp, current_timestamp);

insert into accounts (id, user_id, account_number, currency, balance, version, created_at, updated_at)
values
    (1, 1, 'EE-DEMO-EUR-001', 'EUR', 1250.0000, 0, current_timestamp, current_timestamp),
    (2, 1, 'EE-DEMO-USD-001', 'USD', 780.0000, 0, current_timestamp, current_timestamp),
    (3, 1, 'EE-DEMO-SEK-001', 'SEK', 10350.0000, 0, current_timestamp, current_timestamp),
    (4, 1, 'EE-DEMO-GBP-001', 'GBP', 420.0000, 0, current_timestamp, current_timestamp),
    (5, 1, 'EE-DEMO-VND-001', 'VND', 21500000.0000, 0, current_timestamp, current_timestamp);

insert into transactions (
    id,
    account_id,
    type,
    status,
    amount,
    currency,
    balance_after,
    account_version_after,
    description,
    created_at,
    reference,
    related_transaction_id
)
values
    (1, 1, 'DEPOSIT', 'SUCCESS', 1500.0000, 'EUR', 1500.0000, 0, 'Initial salary payment', timestamp '2026-07-01 09:00:00', 'TXN-SEED-EUR-001', null),
    (2, 1, 'DEBIT', 'SUCCESS', 120.0000, 'EUR', 1380.0000, 0, 'Groceries', timestamp '2026-07-02 11:30:00', 'TXN-SEED-EUR-002', null),
    (3, 1, 'DEBIT', 'SUCCESS', 80.0000, 'EUR', 1300.0000, 0, 'Utilities', timestamp '2026-07-03 14:10:00', 'TXN-SEED-EUR-003', null),
    (4, 1, 'DEBIT', 'SUCCESS', 50.0000, 'EUR', 1250.0000, 0, 'Public transport card', timestamp '2026-07-04 08:45:00', 'TXN-SEED-EUR-004', null),
    (5, 2, 'DEPOSIT', 'SUCCESS', 800.0000, 'USD', 800.0000, 0, 'Initial USD funding', timestamp '2026-07-01 09:05:00', 'TXN-SEED-USD-001', null),
    (6, 2, 'DEBIT', 'SUCCESS', 20.0000, 'USD', 780.0000, 0, 'Streaming subscription', timestamp '2026-07-03 19:00:00', 'TXN-SEED-USD-002', null),
    (7, 3, 'DEPOSIT', 'SUCCESS', 10350.0000, 'SEK', 10350.0000, 0, 'Initial SEK funding', timestamp '2026-07-01 09:10:00', 'TXN-SEED-SEK-001', null),
    (8, 4, 'DEPOSIT', 'SUCCESS', 420.0000, 'GBP', 420.0000, 0, 'Initial GBP funding', timestamp '2026-07-01 09:15:00', 'TXN-SEED-GBP-001', null),
    (9, 5, 'DEPOSIT', 'SUCCESS', 21500000.0000, 'VND', 21500000.0000, 0, 'Initial VND funding', timestamp '2026-07-01 09:20:00', 'TXN-SEED-VND-001', null);

insert into exchange_rates (id, source_currency, target_currency, rate, created_at, updated_at)
values
    (1, 'EUR', 'USD', 1.08000000, current_timestamp, current_timestamp),
    (2, 'USD', 'EUR', 0.92592593, current_timestamp, current_timestamp),
    (3, 'EUR', 'SEK', 11.20000000, current_timestamp, current_timestamp),
    (4, 'SEK', 'EUR', 0.08928571, current_timestamp, current_timestamp),
    (5, 'EUR', 'GBP', 0.86000000, current_timestamp, current_timestamp),
    (6, 'GBP', 'EUR', 1.16279070, current_timestamp, current_timestamp),
    (7, 'EUR', 'VND', 27000.00000000, current_timestamp, current_timestamp),
    (8, 'VND', 'EUR', 0.00003704, current_timestamp, current_timestamp),
    (9, 'USD', 'SEK', 10.37000000, current_timestamp, current_timestamp),
    (10, 'SEK', 'USD', 0.09643202, current_timestamp, current_timestamp),
    (11, 'USD', 'GBP', 0.79629630, current_timestamp, current_timestamp),
    (12, 'GBP', 'USD', 1.25581395, current_timestamp, current_timestamp),
    (13, 'USD', 'VND', 25000.00000000, current_timestamp, current_timestamp),
    (14, 'VND', 'USD', 0.00004000, current_timestamp, current_timestamp),
    (15, 'SEK', 'GBP', 0.07678571, current_timestamp, current_timestamp),
    (16, 'GBP', 'SEK', 13.02325581, current_timestamp, current_timestamp),
    (17, 'SEK', 'VND', 2410.71428571, current_timestamp, current_timestamp),
    (18, 'VND', 'SEK', 0.00041481, current_timestamp, current_timestamp),
    (19, 'GBP', 'VND', 31395.34883721, current_timestamp, current_timestamp),
    (20, 'VND', 'GBP', 0.00003185, current_timestamp, current_timestamp);

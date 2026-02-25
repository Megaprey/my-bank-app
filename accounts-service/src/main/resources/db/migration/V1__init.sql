CREATE SCHEMA IF NOT EXISTS accounts;

CREATE TABLE accounts.account (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    birth_date DATE,
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00
);

INSERT INTO accounts.account (username, full_name, birth_date, balance) VALUES
    ('ivanov', 'Иванов Иван', '2001-01-01', 100.00),
    ('petrov', 'Петров Петр', '1995-05-15', 100.00);

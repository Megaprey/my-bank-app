CREATE SCHEMA IF NOT EXISTS notifications;

CREATE TABLE notifications.notification (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

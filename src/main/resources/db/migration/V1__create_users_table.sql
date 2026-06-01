CREATE TABLE users (
    id                    BIGINT          NOT NULL AUTO_INCREMENT,
    email                 VARCHAR(255)    NOT NULL,
    password              VARCHAR(255)    NOT NULL,
    first_name            VARCHAR(100)    NOT NULL,
    last_name             VARCHAR(100)    NOT NULL,
    phone                 VARCHAR(30),
    status                VARCHAR(30)     NOT NULL DEFAULT 'PENDING_ACTIVATION',
    failed_login_attempts INT             NOT NULL DEFAULT 0,
    locked_until          DATETIME,
    last_login_at         DATETIME,
    created_at            DATETIME        NOT NULL,
    updated_at            DATETIME,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

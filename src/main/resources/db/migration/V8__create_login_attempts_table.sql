CREATE TABLE login_attempts (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    user_id        BIGINT,
    email          VARCHAR(255) NOT NULL,
    ip_address     VARCHAR(50),
    successful     TINYINT(1)   NOT NULL DEFAULT 0,
    failure_reason VARCHAR(255),
    attempted_at   DATETIME     NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_login_attempts_email (email),
    INDEX idx_login_attempts_user_id (user_id),
    INDEX idx_login_attempts_attempted_at (attempted_at),
    CONSTRAINT fk_login_attempts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

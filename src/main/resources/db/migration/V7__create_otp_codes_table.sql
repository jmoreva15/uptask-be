CREATE TABLE otp_codes (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    code_hash  VARCHAR(64) NOT NULL,
    type       VARCHAR(30) NOT NULL,
    expires_at DATETIME    NOT NULL,
    used       TINYINT(1)  NOT NULL DEFAULT 0,
    attempts   INT         NOT NULL DEFAULT 0,
    created_at DATETIME    NOT NULL,
    used_at    DATETIME,
    PRIMARY KEY (id),
    INDEX idx_otp_codes_user_type (user_id, type),
    INDEX idx_otp_codes_used (used),
    CONSTRAINT fk_otp_codes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Additional composite indexes for common query patterns
ALTER TABLE users ADD INDEX idx_users_email_status (email, status);
ALTER TABLE refresh_tokens ADD INDEX idx_refresh_tokens_user_active (user_id, revoked, expires_at);
ALTER TABLE otp_codes ADD INDEX idx_otp_codes_user_type_active (user_id, type, used, expires_at);

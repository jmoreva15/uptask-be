CREATE TABLE project_invitations (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT       NOT NULL,
    email           VARCHAR(255) NOT NULL,
    project_role_id BIGINT       NOT NULL,
    token_hash      VARCHAR(64)  NOT NULL,
    invited_by      BIGINT       NOT NULL,
    status          ENUM('PENDING','ACCEPTED','DECLINED','EXPIRED') NOT NULL DEFAULT 'PENDING',
    expires_at      DATETIME(6)  NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    CONSTRAINT uk_invitation_token UNIQUE (token_hash),
    CONSTRAINT fk_invitations_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_invitations_role FOREIGN KEY (project_role_id) REFERENCES project_roles (id),
    CONSTRAINT fk_invitations_inviter FOREIGN KEY (invited_by) REFERENCES users (id)
);

CREATE INDEX idx_invitations_project ON project_invitations (project_id);
CREATE INDEX idx_invitations_email ON project_invitations (email);
CREATE INDEX idx_invitations_status ON project_invitations (status);

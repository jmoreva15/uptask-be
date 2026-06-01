CREATE TABLE project_members (
    id              BIGINT      AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT      NOT NULL,
    user_id         BIGINT      NOT NULL,
    project_role_id BIGINT      NOT NULL,
    invited_by      BIGINT,
    joined_at       DATETIME(6) NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    CONSTRAINT uk_project_member UNIQUE (project_id, user_id),
    CONSTRAINT fk_members_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_members_role FOREIGN KEY (project_role_id) REFERENCES project_roles (id),
    CONSTRAINT fk_members_invited_by FOREIGN KEY (invited_by) REFERENCES users (id)
);

CREATE INDEX idx_project_members_project ON project_members (project_id);
CREATE INDEX idx_project_members_user ON project_members (user_id);

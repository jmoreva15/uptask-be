CREATE TABLE project_roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id  BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_system   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6),
    CONSTRAINT uk_project_role_name UNIQUE (project_id, name),
    CONSTRAINT fk_project_roles_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
);

CREATE TABLE project_role_permissions (
    project_role_id       BIGINT NOT NULL,
    project_permission_id BIGINT NOT NULL,
    PRIMARY KEY (project_role_id, project_permission_id),
    CONSTRAINT fk_prp_role FOREIGN KEY (project_role_id) REFERENCES project_roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_prp_permission FOREIGN KEY (project_permission_id) REFERENCES project_permissions (id)
);

CREATE INDEX idx_project_roles_project ON project_roles (project_id);

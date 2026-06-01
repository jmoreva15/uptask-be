CREATE TABLE projects (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    `key`       VARCHAR(10)  NOT NULL,
    owner_id    BIGINT       NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6),
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    CONSTRAINT uk_projects_key UNIQUE (`key`),
    CONSTRAINT fk_projects_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE INDEX idx_projects_owner ON projects (owner_id);

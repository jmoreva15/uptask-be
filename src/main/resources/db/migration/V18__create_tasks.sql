CREATE TABLE tasks (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    project_id  BIGINT       NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    status      ENUM('TODO','IN_PROGRESS','REVIEW','DONE') NOT NULL DEFAULT 'TODO',
    priority    ENUM('LOW','MEDIUM','HIGH','CRITICAL')     NOT NULL DEFAULT 'MEDIUM',
    assignee_id BIGINT,
    reporter_id BIGINT       NOT NULL,
    due_date    DATE,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6),
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    CONSTRAINT fk_tasks_project  FOREIGN KEY (project_id)  REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_assignee FOREIGN KEY (assignee_id) REFERENCES users (id),
    CONSTRAINT fk_tasks_reporter FOREIGN KEY (reporter_id) REFERENCES users (id)
);

CREATE INDEX idx_tasks_project   ON tasks (project_id);
CREATE INDEX idx_tasks_assignee  ON tasks (assignee_id);
CREATE INDEX idx_tasks_status    ON tasks (status);
CREATE INDEX idx_tasks_priority  ON tasks (priority);

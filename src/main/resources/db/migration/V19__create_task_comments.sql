CREATE TABLE task_comments (
    id         BIGINT      AUTO_INCREMENT PRIMARY KEY,
    task_id    BIGINT      NOT NULL,
    author_id  BIGINT      NOT NULL,
    content    TEXT        NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_comments_task   FOREIGN KEY (task_id)   REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users (id)
);

CREATE INDEX idx_comments_task ON task_comments (task_id);

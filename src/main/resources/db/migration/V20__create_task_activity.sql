CREATE TABLE task_activity (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id     BIGINT      NOT NULL,
    actor_id    BIGINT      NOT NULL,
    action_type ENUM(
        'TASK_CREATED',
        'TITLE_CHANGED',
        'DESCRIPTION_CHANGED',
        'STATUS_CHANGED',
        'PRIORITY_CHANGED',
        'ASSIGNED',
        'UNASSIGNED',
        'DUE_DATE_CHANGED',
        'COMMENTED',
        'COMMENT_DELETED'
    ) NOT NULL,
    old_value   VARCHAR(255),
    new_value   VARCHAR(255),
    created_at  DATETIME(6) NOT NULL,
    CONSTRAINT fk_activity_task  FOREIGN KEY (task_id)  REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_actor FOREIGN KEY (actor_id) REFERENCES users (id)
);

CREATE INDEX idx_activity_task ON task_activity (task_id);
CREATE INDEX idx_activity_actor ON task_activity (actor_id);

CREATE TABLE project_permissions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    category    VARCHAR(50)  NOT NULL,
    CONSTRAINT uk_project_permissions_name UNIQUE (name)
);

INSERT INTO project_permissions (name, description, category) VALUES
    ('PROJECT_VIEW',         'View project details',           'PROJECT'),
    ('PROJECT_EDIT',         'Edit project details',           'PROJECT'),
    ('PROJECT_DELETE',       'Delete the project',             'PROJECT'),
    ('MEMBER_VIEW',          'View project members',           'MEMBER'),
    ('MEMBER_INVITE',        'Invite members to project',      'MEMBER'),
    ('MEMBER_REMOVE',        'Remove members from project',    'MEMBER'),
    ('MEMBER_ROLE_ASSIGN',   'Assign roles to members',        'MEMBER'),
    ('ROLE_CREATE',          'Create project roles',           'ROLE'),
    ('ROLE_EDIT',            'Edit project roles',             'ROLE'),
    ('ROLE_DELETE',          'Delete project roles',           'ROLE'),
    ('TASK_VIEW',            'View tasks',                     'TASK'),
    ('TASK_CREATE',          'Create tasks',                   'TASK'),
    ('TASK_EDIT',            'Edit any task',                  'TASK'),
    ('TASK_DELETE',          'Delete tasks',                   'TASK'),
    ('TASK_ASSIGN',          'Assign tasks to members',        'TASK'),
    ('TASK_STATUS_CHANGE',   'Change task status',             'TASK'),
    ('COMMENT_CREATE',       'Add comments to tasks',          'COMMENT'),
    ('COMMENT_DELETE_OWN',   'Delete own comments',            'COMMENT'),
    ('COMMENT_DELETE_ANY',   'Delete any comment',             'COMMENT');

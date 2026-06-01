-- Roles
INSERT INTO roles (name, description)
VALUES ('USER', 'Standard user'),
       ('ADMIN', 'Administrator');

-- Permissions
INSERT INTO permissions (name, description)
VALUES ('CATEGORY_CREATE', 'Create categories'),
       ('CATEGORY_READ', 'View categories'),
       ('CATEGORY_UPDATE', 'Update categories'),
       ('CATEGORY_DELETE', 'Delete categories'),
       ('USER_READ', 'View user list'),
       ('USER_MANAGE', 'Manage user status');

-- ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ADMIN';

-- USER gets read-only category access
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name = 'CATEGORY_READ'
WHERE r.name = 'USER';

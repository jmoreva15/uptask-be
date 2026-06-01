-- Remove category and admin permissions
DELETE FROM role_permissions
WHERE permission_id IN (SELECT id FROM permissions WHERE name IN (
    'CATEGORY_CREATE', 'CATEGORY_READ', 'CATEGORY_UPDATE', 'CATEGORY_DELETE',
    'USER_READ', 'USER_MANAGE'
));

DELETE FROM permissions WHERE name IN (
    'CATEGORY_CREATE', 'CATEGORY_READ', 'CATEGORY_UPDATE', 'CATEGORY_DELETE',
    'USER_READ', 'USER_MANAGE'
);

DROP TABLE IF EXISTS categories;

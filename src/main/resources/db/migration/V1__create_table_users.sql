CREATE TABLE users
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    name           VARCHAR(100)          NOT NULL,
    email          VARCHAR(150)          NOT NULL,
    deleted        BIT(1)                NOT NULL,
    created_at     datetime              NOT NULL,
    updated_at     datetime              NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

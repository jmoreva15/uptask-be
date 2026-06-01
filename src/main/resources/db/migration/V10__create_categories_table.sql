CREATE TABLE categories
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(100) NOT NULL UNIQUE,
    slug             VARCHAR(120) NOT NULL UNIQUE,
    description      VARCHAR(500),
    image_url        VARCHAR(500),
    parent_id        BIGINT,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    sort_order       INT          NOT NULL DEFAULT 0,
    meta_title       VARCHAR(100),
    meta_description VARCHAR(255),
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6),
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),

    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id)
        REFERENCES categories (id) ON DELETE SET NULL
);

CREATE INDEX idx_category_slug ON categories (slug);
CREATE INDEX idx_category_parent ON categories (parent_id);
CREATE INDEX idx_category_active ON categories (active);
CREATE INDEX idx_category_sort ON categories (sort_order);

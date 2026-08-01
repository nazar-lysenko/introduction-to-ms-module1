CREATE TABLE IF NOT EXISTS resources (
    id             BIGSERIAL    PRIMARY KEY,
    storage_path   VARCHAR(255) NOT NULL,
    storage_bucket VARCHAR(255) NOT NULL,
    storage_type   VARCHAR(50)  NOT NULL
);

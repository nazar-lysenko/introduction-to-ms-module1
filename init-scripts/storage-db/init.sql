CREATE TABLE IF NOT EXISTS storages (
    id           BIGSERIAL    PRIMARY KEY,
    storage_type VARCHAR(50)  NOT NULL,
    bucket       VARCHAR(255) NOT NULL,
    path         VARCHAR(255) NOT NULL
);

INSERT INTO storages (storage_type, bucket, path) VALUES ('STAGING',   'staging-bucket',   '/files');
INSERT INTO storages (storage_type, bucket, path) VALUES ('PERMANENT', 'permanent-bucket', '/files');

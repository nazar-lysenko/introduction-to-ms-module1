CREATE TABLE IF NOT EXISTS songs (
    id       BIGINT PRIMARY KEY,
    name     VARCHAR(255),
    artist   VARCHAR(255),
    album    VARCHAR(255),
    duration VARCHAR(255),
    year     VARCHAR(255)
);

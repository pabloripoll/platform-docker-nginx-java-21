CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(64) NOT NULL,
    password VARCHAR(128) NOT NULL,
    name VARCHAR(128) NOT NULL
);

INSERT INTO users (email, password, name) VALUES ('john-doe@example.com', '123456', 'John Doe');
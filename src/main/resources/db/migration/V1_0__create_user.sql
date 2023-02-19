CREATE TABLE _user
(
    id         BIGSERIAL    NOT NULL,
    country    VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT uq_user_email UNIQUE (email)
);

INSERT INTO _user(id, first_name, last_name, email, country, password)
VALUES (0,
        'admin',
        'admin',
        'admin@admin.com',
        'admin',
        '$2a$12$tbVMEjv2G61M5ucrHW0ljeHK6ZHxRj9qo2XRjbIzk5T7Zq3Ld/7Wy');
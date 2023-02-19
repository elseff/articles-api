CREATE TABLE role
(
    id   SERIAL       NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_role PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE (name)
);

INSERT INTO role (id, name)
VALUES (1, 'ROLE_USER');

INSERT INTO role (id, name)
VALUES (2, 'ROLE_ADMIN');
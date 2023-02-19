CREATE TABLE user_role
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES _user (id),
    CONSTRAINT fk_role_id FOREIGN KEY (role_id) REFERENCES role (id),
    CONSTRAINT users_roles_pkey PRIMARY KEY (user_id, role_id)
);

INSERT INTO user_role
VALUES (0, 1),
       (0, 2);
create table roles
(
    id   serial       not null
        constraint roles_pkey
            primary key,
    name varchar(255) not null
        constraint uc_roles_name
            unique
);

INSERT INTO public.roles (id, name)
VALUES (1, 'ROLE_USER');

INSERT INTO public.roles (id, name)
VALUES (2, 'ROLE_ADMIN');
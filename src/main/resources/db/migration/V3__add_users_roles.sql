create table users_roles
(
    user_id bigint not null
        constraint user_id_fk
            references users,
    role_id bigint not null
        constraint role_id_fk
            references roles,
    constraint users_roles_pkey
        primary key (user_id, role_id)
);

insert into public.users_roles
values (0, 1),
       (0, 2);
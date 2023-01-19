create table users
(
    id         bigserial    not null
        constraint users_pkey
            primary key,
    country    varchar(255) not null,
    email      varchar(255) not null unique,
    first_name varchar(255) not null,
    last_name  varchar(255) not null,
    password   varchar(255) not null
);

insert into users(id,first_name,last_name,email,country,password)
values (0,
        'admin',
        'admin',
        'admin@admin.com',
        'admin',
        '$2a$12$tbVMEjv2G61M5ucrHW0ljeHK6ZHxRj9qo2XRjbIzk5T7Zq3Ld/7Wy');
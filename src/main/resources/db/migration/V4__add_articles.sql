create table articles
(
    id          bigserial not null
        constraint pk_articles
            primary key,
    title       varchar(255) not null,
    description text         not null,
    date        varchar(255) not null,
    author_id   bigint
        constraint author_id_fk
            references users
);

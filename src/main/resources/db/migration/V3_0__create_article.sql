CREATE TABLE article
(
    id          BIGSERIAL    NOT NULL,
    title       VARCHAR(120) NOT NULL,
    description TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    author_id   BIGINT       NOT NULL,
    CONSTRAINT pk_article_id PRIMARY KEY (id),
    CONSTRAINT fk_author_id FOREIGN KEY (author_id) REFERENCES _user (id)
);

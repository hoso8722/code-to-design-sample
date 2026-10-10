-- 実験用:起動のたびに表を作り直す(本番では、マイグレーションのツールで管理する)
DROP TABLE IF EXISTS loans;
DROP TABLE IF EXISTS books;

CREATE TABLE books (
    id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title  TEXT NOT NULL UNIQUE,
    copies INT  NOT NULL CHECK (copies >= 0)
);

CREATE TABLE loans (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id   BIGINT NOT NULL REFERENCES books (id),
    borrower  TEXT   NOT NULL,
    loaned_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO books (title, copies) VALUES ('吾輩は猫である', 3), ('坊っちゃん', 2);

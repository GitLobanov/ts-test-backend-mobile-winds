CREATE TABLE author
(
    id         SERIAL PRIMARY KEY,
    full_name  TEXT                     NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

ALTER TABLE budget
    ADD COLUMN author_id INTEGER REFERENCES author (id);
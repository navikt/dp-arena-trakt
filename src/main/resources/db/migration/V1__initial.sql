CREATE TABLE IF NOT EXISTS arena_data
(
    id   BIGSERIAL PRIMARY KEY,
    data jsonb NOT NULL
);

CREATE INDEX datagin ON arena_data USING gin (data);

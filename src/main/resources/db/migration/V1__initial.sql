CREATE TABLE IF NOT EXISTS arena_data
(
    id   BIGSERIAL PRIMARY KEY,
    opprettet TIMESTAMP NOT NULL DEFAULT NOW(),
    data jsonb NOT NULL,
    hendelseid UUID
);

CREATE INDEX datagin ON arena_data USING gin (data);

CREATE TABLE IF NOT EXISTS arena_data
(
    id          BIGSERIAL PRIMARY KEY,
    opprettet   TIMESTAMP NOT NULL DEFAULT NOW(),
    data        jsonb     NOT NULL,
    hendelse_id uuid
);

CREATE INDEX IF NOT EXISTS i_datagin ON arena_data USING gin (data);

CREATE INDEX IF NOT EXISTS i_brukte_data ON arena_data (hendelse_id, opprettet)

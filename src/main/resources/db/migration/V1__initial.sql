CREATE TABLE IF NOT EXISTS arena_data
(
    id          uuid PRIMARY KEY,
    tabell      VARCHAR(250) NOT NULL,
    pos         VARCHAR(250) NOT NULL,
    mottatt     TIMESTAMP    NOT NULL DEFAULT NOW(),
    skjedde     TIMESTAMP    NOT NULL,
    replikert   TIMESTAMP    NOT NULL,
    data        jsonb        NOT NULL,
    behandlet   TIMESTAMP    NULL     DEFAULT NULL,
    hendelse_id uuid
);

CREATE INDEX IF NOT EXISTS i_data_table ON arena_data ((data ->> 'table'), (data ->> 'op_type'));
CREATE INDEX IF NOT EXISTS i_datagin ON arena_data USING gin (data);

CREATE UNIQUE INDEX IF NOT EXISTS ui_duplikat ON arena_data (tabell, pos);

CREATE INDEX IF NOT EXISTS i_behandlet ON arena_data (behandlet);

CREATE INDEX IF NOT EXISTS i_brukte_data ON arena_data (hendelse_id, mottatt)

CREATE TABLE IF NOT EXISTS hendelse
(
    hendelse_id SERIAL PRIMARY KEY,
    melding_id  uuid                    NOT NULL UNIQUE,
    sendt       TIMESTAMP DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS hendelse_vedtak
(
    melding_id uuid REFERENCES hendelse (melding_id) NOT NULL,
    vedtak_id  INT REFERENCES vedtak (vedtak_id)     NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_melding_id ON hendelse (melding_id);


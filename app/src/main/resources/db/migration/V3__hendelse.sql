CREATE TABLE IF NOT EXISTS hendelse
(
    hendelse_id SERIAL PRIMARY KEY,
    melding_id  uuid                    NOT NULL UNIQUE,
    sendt       TIMESTAMP DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS hendelse_vedtak
(
    melding_id uuid REFERENCES hendelse (melding_id) NOT NULL,
    vedtak_id  INT                                   NOT NULL,
    oppdatert  TIMESTAMP                             NOT NULL,
    FOREIGN KEY (vedtak_id, oppdatert) REFERENCES vedtak (vedtak_id, oppdatert)
);

-- CREATE INDEX IF NOT EXISTS idx_melding_id ON hendelse (melding_id);
CREATE INDEX IF NOT EXISTS idx_vedtak_id ON hendelse_vedtak (vedtak_id, oppdatert);

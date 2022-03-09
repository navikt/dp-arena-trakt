CREATE TABLE IF NOT EXISTS sak
(
    id           SERIAL PRIMARY KEY,
    sak_id       INT       NOT NULL UNIQUE,
    er_dagpenger BOOLEAN   NOT NULL,
    opprettet    TIMESTAMP NOT NULL,
    oppdatert    TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS vedtak
(
    id               SERIAL PRIMARY KEY,
    vedtak_id        INT                     NOT NULL,
    sak_id           INT                     NOT NULL,
    person_id        INT                     NOT NULL,
    vedtaktypekode   VARCHAR(10)             NOT NULL,
    utfallkode       VARCHAR(10)             NOT NULL,
    rettighetkode    VARCHAR(10)             NOT NULL,
    vedtakstatuskode VARCHAR(5)              NOT NULL,
    innlest          TIMESTAMP DEFAULT NOW() NOT NULL,
    opprettet        TIMESTAMP               NOT NULL,
    oppdatert        TIMESTAMP               NOT NULL,
    saknummer        TEXT                    NOT NULL,
    lopenummer       INT                     NOT NULL,
    UNIQUE (vedtak_id, oppdatert)
);

CREATE INDEX IF NOT EXISTS idx_vedtak_sak_id ON vedtak (sak_id)

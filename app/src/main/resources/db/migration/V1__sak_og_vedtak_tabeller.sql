CREATE TABLE IF NOT EXISTS sak
(
    sak_id       INT PRIMARY KEY,
    er_dagpenger BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS vedtak
(
    vedtak_id        INT PRIMARY KEY,
    sak_id           INT                     NOT NULL,
    person_id        INT                     NOT NULL,
    vedtaktypekode   VARCHAR(10)             NOT NULL,
    utfallkode       VARCHAR(10)             NOT NULL,
    rettighetkode    VARCHAR(10)             NOT NULL,
    vedtakstatuskode VARCHAR(5)              NOT NULL,
    innlest          TIMESTAMP DEFAULT NOW() NOT NULL
);
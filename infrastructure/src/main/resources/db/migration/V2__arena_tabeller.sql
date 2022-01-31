CREATE TABLE IF NOT EXISTS sak
(
    id   BIGSERIAL PRIMARY KEY,
    kode VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS vedtak
(
    id     BIGSERIAL PRIMARY KEY,
    sak_id BIGSERIAL NOT NULL REFERENCES sak
);

CREATE TABLE IF NOT EXISTS vedtakfakta
(
    vedtak_id BIGSERIAL,
    kode      VARCHAR(10),
    verdi     varchar(2000) NOT NULL,
    PRIMARY KEY (vedtak_id, kode)
);

CREATE TABLE IF NOT EXISTS beregningsledd
(
    id        BIGSERIAL PRIMARY KEY,
    vedtak_id BIGSERIAL   NOT NULL,
    kode      VARCHAR(30) NOT NULL
);
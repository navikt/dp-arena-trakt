CREATE TABLE IF NOT EXISTS sak
(
    sak_id       INT PRIMARY KEY,
    er_dagpenger BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS vedtak
(
    vedtak_id INT PRIMARY KEY,
    sak_id    INT NOT NULL REFERENCES sak (sak_id)
)
CREATE TABLE IF NOT EXISTS replikeringslogg
(
    replikering_id TEXT PRIMARY KEY        NOT NULL,
    operasjon      CHAR(1)                 NOT NULL,
    replikert      TIMESTAMP               NOT NULL,
    først_sett     TIMESTAMP DEFAULT NOW() NOT NULL,
    behandlet      TIMESTAMP               NULL,
    beskrivelse    TEXT                    NOT NULL
);

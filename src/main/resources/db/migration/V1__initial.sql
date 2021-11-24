CREATE TABLE IF NOT EXISTS vedtak
(
    id   BIGSERIAL PRIMARY KEY,
    data jsonb NOT NULL
);

CREATE TABLE IF NOT EXISTS beregningsledd
(
    id   BIGSERIAL PRIMARY KEY,
    data jsonb NOT NULL
);

CREATE TABLE IF NOT EXISTS vedtaksfakta
(
    id   BIGSERIAL PRIMARY KEY,
    data jsonb NOT NULL
);


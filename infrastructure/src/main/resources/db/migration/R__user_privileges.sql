DO
$$
    BEGIN
        IF EXISTS
            (SELECT 1 FROM pg_roles WHERE rolname = 'cloudsqliamuser')
        THEN
            GRANT SELECT ON ALL TABLES IN SCHEMA public TO cloudsqliamuser;
        END IF;
    END
$$;

-- Gi naisjob-brukeren skrivetilgang
DO
$$
    BEGIN
        IF EXISTS
            (SELECT 1 FROM pg_user WHERE usename = 'job')
        THEN
            GRANT INSERT ON arena_data TO job;
            GRANT USAGE, SELECT ON SEQUENCE arena_data_id_seq TO job;
            GRANT USAGE, SELECT ON SEQUENCE arena_data_sletterekkefolge_seq TO job;
        END IF;
    END
$$;

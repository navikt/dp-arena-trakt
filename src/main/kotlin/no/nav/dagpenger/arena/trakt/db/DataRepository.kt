package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.intellij.lang.annotations.Language
import java.time.Period

class DataRepository {
    @Language("PostgreSQL")
    private val lagreQuery = """INSERT INTO arena_data (data) VALUES(?::jsonb)"""

    fun lagre(json: String) {
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf(lagreQuery, json).asUpdate)
        }
    }

    @Language("PostgreSQL")
    private val slettQuery =
        """DELETE FROM arena_data WHERE opprettet < CURRENT_TIMESTAMP - INTERVAL '1 days' * ? AND hendelse_id IS NULL """

    fun slettUbrukteData(eldreEnn: Period) {
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    slettQuery,
                    eldreEnn.run {
                        // Legg til en dag så det blir *eldreEnn* og ikke eldreEnnOgIdag
                        plusDays(1)
                    }.days
                ).asUpdate
            )
        }
    }
}

package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder.dataSource
import org.intellij.lang.annotations.Language

class BeregningsleddRepository {
    fun insert(json: String) {
        @Language("PostgreSQL")
        val query = """INSERT INTO beregningsledd (data) VALUES(?::jsonb)"""

        using(sessionOf(dataSource)) { session ->
            session.run(queryOf(query, json).asUpdate)
        }
    }
}

package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.intellij.lang.annotations.Language

class DataRepository {
    @Language("PostgreSQL")
    private val query = """INSERT INTO data (data) VALUES(?::jsonb)"""

    fun lagre(json: String) {
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf(query, json).asUpdate)
        }
    }
}

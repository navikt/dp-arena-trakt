package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder.dataSource
import org.intellij.lang.annotations.Language

class BeregningsleddRepository {

    fun finn(navn: String, relatertObjektType: String, relatertObjektId: String): Boolean {
        val beregningsledd = using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(finnQuery, navn, relatertObjektType, relatertObjektId)
                    .map { row ->
                         row.string("id")
                    }.asSingle
            )
        }
        return beregningsledd != null
    }

    fun insert(json: String) {
        @Language("PostgreSQL")
        val query = """INSERT INTO beregningsledd (data) VALUES(?::jsonb)"""

        using(sessionOf(dataSource)) { session ->
            session.run(queryOf(query, json).asUpdate)
        }
    }

    @Language("PostgreSQL")
    private val finnQuery = """
        |SELECT data -> 'after' ->> 'BEREGNINGSLEDD_ID' AS id,
        |       data -> 'after' ->> 'VERDI'             AS verdi
        |FROM beregningsledd
        |WHERE data ->> 'op_type' = 'I'
        |  AND data -> 'after' ->> 'BEREGNINGSLEDDKODE' = ?
        |  AND data -> 'after' ->> 'TABELLNAVNALIAS_KILDE' = ?
        |  AND data -> 'after' ->> 'OBJEKT_ID_KILDE' = ?
        """.trimMargin()
}

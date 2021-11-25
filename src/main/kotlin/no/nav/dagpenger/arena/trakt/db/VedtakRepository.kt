package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.intellij.lang.annotations.Language

class VedtakRepository {
    fun insert(json: String) {
        @Language("PostgreSQL")
        val query = """INSERT INTO vedtak (data) VALUES(?::jsonb)"""
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf(query, json).asUpdate)
        }
    }

    fun finn(vedtakId: String): Boolean {
        val vedtaksfakta = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(finnVedtaksfaktaQuery, vedtakId)
                    .map { row ->
                        row.string("id")
                    }.asSingle
            )
        }
        return vedtaksfakta != null
    }

    @Language("PostgreSQL")
    private val finnVedtaksfaktaQuery = """
        |SELECT data -> 'after' ->> 'VEDTAK_ID' AS id,
        |    data -> 'after' ->> 'UTFALLKODE' AS utfall
        |FROM vedtak  
        |WHERE data ->> 'op_type' = 'I'
        |  AND data -> 'after' ->> 'VEDTAK_ID' = ?
        """.trimMargin()
}

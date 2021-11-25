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

    fun finn(vedtakfaktaKode: String, vedtakId: String): Boolean {
        val vedtaksfakta = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(finnVedtaksfaktaQuery, vedtakfaktaKode, vedtakId)
                    .map { row ->
                        row.string("id")
                    }.asSingle
            )
        }
        return vedtaksfakta != null
    }

    @Language("PostgreSQL")
    private val finnVedtaksfaktaQuery = """SELECT data -> 'after' ->> 'VEDTAK_ID' AS id,
            |       data -> 'after' ->> 'VEDTAKFAKTAKODE' AS verdi
            |FROM vedtak  
            |WHERE data ->> 'op_type' = 'I'
            |  AND data -> 'after' ->> 'VEDTAKFAKTAKODE' = ?
            |  AND data -> 'after' ->> 'VEDTAK_ID' = ?

            """.trimMargin()
}

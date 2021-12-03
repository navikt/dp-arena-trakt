package no.nav.dagpenger.arena.trakt.datakrav

import kotliquery.Row
import org.intellij.lang.annotations.Language

internal class Vedtaksfakta(private val navn: String) : Datakrav<String>(navn) {
    private val where
        @Language("JSON")
        get() = """
            |{
            |  "after": {
            |    "VEDTAKFAKTAKODE": "$navn",
            |    "VEDTAK_ID": ${hendelse.id}
            |  }
            |}""".trimMargin()

    override fun params() = mapOf(
        "where" to where
    )

    override fun mapper(row: Row) = row.string("verdi")

    @Language("PostgreSQL")
    override val query = """
        |SELECT data -> 'after' ->> 'VEDTAK_ID'       AS id,
        |       data -> 'after' ->> 'VEDTAKFAKTAKODE' AS verdi
        |FROM arena_data
        |WHERE data ->> 'table' = 'SIAMO.VEDTAKFAKTA'
        |  AND data ->> 'op_type' = 'I'
        |  AND data @> :where::jsonb
        """.trimMargin()
}

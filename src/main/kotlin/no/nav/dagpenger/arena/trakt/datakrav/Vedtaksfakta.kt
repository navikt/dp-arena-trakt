package no.nav.dagpenger.arena.trakt.datakrav

import kotliquery.Row
import org.intellij.lang.annotations.Language

internal class Vedtaksfakta(private val navn: String) : Datakrav<String>(navn) {
    override fun params() = mapOf(
        "navn" to navn,
        "vedtakId" to hendelse.id
    )

    override fun mapper(row: Row) = row.string("verdi")

    @Language("PostgreSQL")
    override val query = """
        |SELECT data -> 'after' ->> 'VEDTAK_ID'       AS id,
        |       data -> 'after' ->> 'VEDTAKFAKTAKODE' AS verdi
        |FROM data
        |WHERE data ->> 'table' = 'VEDTAKFAKTA'
        |  AND data ->> 'op_type' = 'I'
        |  AND data -> 'after' ->> 'VEDTAKFAKTAKODE' = :navn
        |  AND data -> 'after' ->> 'VEDTAK_ID' = :vedtakId
        """.trimMargin()
}

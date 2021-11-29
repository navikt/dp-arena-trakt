package no.nav.dagpenger.arena.trakt.datakrav

import kotliquery.Row
import no.nav.dagpenger.arena.trakt.Hendelse
import org.intellij.lang.annotations.Language

internal class Vedtaksfakta(navn: String, hendelse: Hendelse) : Datakrav<String>(navn, hendelse) {
    override val params = mapOf(
        "navn" to navn,
        "vedtakId" to hendelse.id
    )

    override fun mapper(row: Row) = row.string("verdi")

    @Language("PostgreSQL")
    override val query = """
        |SELECT data -> 'after' ->> 'VEDTAK_ID' AS id,
        |       data -> 'after' ->> 'VEDTAKFAKTAKODE' AS verdi
        |FROM data 
        |WHERE data ->> 'op_type' = 'I'
        |  AND data -> 'after' ->> 'VEDTAKFAKTAKODE' = :navn
        |  AND data -> 'after' ->> 'VEDTAK_ID' = :vedtakId
        """.trimMargin()
}

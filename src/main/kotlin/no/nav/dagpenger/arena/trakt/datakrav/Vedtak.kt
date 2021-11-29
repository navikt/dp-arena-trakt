package no.nav.dagpenger.arena.trakt.datakrav

import kotliquery.Row
import no.nav.dagpenger.arena.trakt.Hendelse
import org.intellij.lang.annotations.Language

internal class Vedtak(id: String, hendelse: Hendelse) : Datakrav<VedtakData>(id, hendelse) {
    override val params = mapOf(
        "vedtakId" to hendelse.id
    )

    override fun mapper(row: Row) = VedtakData(
        id = hendelse.id,
        utfall = row.string("utfall")
    )

    @Language("PostgreSQL")
    override val query = """
        |SELECT data -> 'after' ->> 'VEDTAK_ID' AS id,
        |    data -> 'after' ->> 'UTFALLKODE' AS utfall
        |FROM data
        |WHERE data ->> 'op_type' = 'I'
        |  AND data -> 'after' ->> 'VEDTAK_ID' = :vedtakId
        """.trimMargin()
}

internal data class VedtakData(val id: String, val utfall: String)

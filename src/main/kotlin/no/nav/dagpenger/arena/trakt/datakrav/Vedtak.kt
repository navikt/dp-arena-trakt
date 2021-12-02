package no.nav.dagpenger.arena.trakt.datakrav

import kotliquery.Row
import org.intellij.lang.annotations.Language

internal class Vedtak(id: String) : Datakrav<VedtakData>(id) {
    override fun params() = mapOf(
        "vedtakId" to hendelse.id
    )

    override fun mapper(row: Row) = VedtakData(
        id = row.string("id"),
        utfall = row.string("utfall"),
        vedtakstype = row.string("type")
    )

    @Language("PostgreSQL")
    override val query = """
        |SELECT data -> 'after' ->> 'VEDTAK_ID'        AS id,
        |       data -> 'after' ->> 'VEDTAKSTATUSKODE' AS utfall,
        |       data -> 'after' ->> 'VEDTAKTYPEKODE' AS type 
        |FROM data
        |WHERE data ->> 'table' = 'SIAMO.VEDTAK'
        |  AND data ->> 'op_type' = 'I'
        |  AND data -> 'after' ->> 'VEDTAK_ID' = :vedtakId
        """.trimMargin()
}

internal data class VedtakData(val id: String, val utfall: String, val vedtakstype: String)

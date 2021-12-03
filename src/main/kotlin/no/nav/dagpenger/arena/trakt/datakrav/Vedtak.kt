package no.nav.dagpenger.arena.trakt.datakrav

import kotliquery.Row
import org.intellij.lang.annotations.Language

internal class Vedtak(id: String) : Datakrav<VedtakData>(id) {
    private val where
        @Language("JSON")
        get() = """
            |{
            |  "after": {
            |    "VEDTAK_ID": ${hendelse.id}
            |  }
            |}""".trimMargin()

    override fun params() = mapOf(
        "where" to where
    )

    override fun mapper(row: Row) = Resultat(
        id = row.bigDecimal("id").toBigInteger(),
        data = VedtakData(
            id = row.string("id"),
            utfall = row.string("utfall"),
            vedtakstype = row.string("type")
        )
    )

    @Language("PostgreSQL")
    override val query = """
        |SELECT id,
        |       data -> 'after' ->> 'VEDTAK_ID'        AS vedtakId,
        |       data -> 'after' ->> 'VEDTAKSTATUSKODE' AS utfall,
        |       data -> 'after' ->> 'VEDTAKTYPEKODE' AS type 
        |FROM arena_data
        |WHERE data ->> 'table' = 'SIAMO.VEDTAK'
        |  AND data ->> 'op_type' = 'I'
        |  AND data @> :where::jsonb
        """.trimMargin()
}

internal data class VedtakData(val id: String, val utfall: String, val vedtakstype: String)

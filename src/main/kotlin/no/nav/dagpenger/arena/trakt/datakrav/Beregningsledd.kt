package no.nav.dagpenger.arena.trakt.datakrav

import kotliquery.Row
import org.intellij.lang.annotations.Language

internal class Beregningsledd(private val navn: String) : Datakrav<String>(navn) {
    private val where
        @Language("JSON")
        get() = """
            |{
            |  "after": {
            |    "BEREGNINGSLEDDKODE": "$navn",
            |    "TABELLNAVNALIAS_KILDE": "VEDTAK",
            |    "OBJEKT_ID_KILDE": ${hendelse.id}
            |  }
            |}""".trimMargin()

    override fun params() = mapOf(
        "where" to where,
    )

    override fun mapper(row: Row) = Resultat(row.bigDecimal("id").toBigInteger(), row.string("verdi"))

    @Language("PostgreSQL")
    override val query = """
        |SELECT id,
        |       data -> 'after' ->> 'VERDI'             AS verdi
        |FROM arena_data
        |WHERE data ->> 'table' = 'SIAMO.BEREGNINGSLEDD' 
        |  AND data ->> 'op_type' = 'I'
        |  AND data @> :where::jsonb
        """.trimMargin()
}

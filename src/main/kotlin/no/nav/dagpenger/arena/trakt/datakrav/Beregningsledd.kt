package no.nav.dagpenger.arena.trakt.datakrav

import kotliquery.Row
import org.intellij.lang.annotations.Language

internal class Beregningsledd(private val navn: String) : Datakrav<String>(navn) {
    override fun params() = mapOf(
        "navn" to navn,
        "objektType" to "VEDTAK",
        "objektId" to hendelse.id
    )

    override fun mapper(row: Row) = row.string("verdi")

    @Language("PostgreSQL")
    override val query = """
        |SELECT data -> 'after' ->> 'BEREGNINGSLEDD_ID' AS id,
        |       data -> 'after' ->> 'VERDI'             AS verdi
        |FROM data
        |WHERE data ->> 'table' = 'SIAMO.BEREGNINGSLEDD' 
        |  AND data ->> 'op_type' = 'I'
        |  AND data -> 'after' ->> 'BEREGNINGSLEDDKODE' = :navn
        |  AND data -> 'after' ->> 'TABELLNAVNALIAS_KILDE' = :objektType
        |  AND data -> 'after' ->> 'OBJEKT_ID_KILDE' = :objektId
        """.trimMargin()
}

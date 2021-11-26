package no.nav.dagpenger.arena.trakt.datakrav

import no.nav.dagpenger.arena.trakt.IverksattVedtak
import org.intellij.lang.annotations.Language

internal class Beregningsledd(private val navn: String) : Datakrav() {
    override fun oppfyltFor(vedtak: IverksattVedtak): Boolean {
        return finnData(
            mapOf(
                "navn" to navn,
                "objektType" to "VEDTAK",
                "objektId" to vedtak.id
            )
        )
    }

    @Language("PostgreSQL")
    override val query = """
        |SELECT data -> 'after' ->> 'BEREGNINGSLEDD_ID' AS id,
        |       data -> 'after' ->> 'VERDI'             AS verdi
        |FROM data
        |WHERE data ->> 'op_type' = 'I'
        |  AND data -> 'after' ->> 'BEREGNINGSLEDDKODE' = :navn
        |  AND data -> 'after' ->> 'TABELLNAVNALIAS_KILDE' = :objektType
        |  AND data -> 'after' ->> 'OBJEKT_ID_KILDE' = :objektId
        """.trimMargin()
}

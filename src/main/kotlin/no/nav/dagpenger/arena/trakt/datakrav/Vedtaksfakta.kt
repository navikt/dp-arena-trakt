package no.nav.dagpenger.arena.trakt.datakrav

import no.nav.dagpenger.arena.trakt.IverksattVedtak
import org.intellij.lang.annotations.Language

internal class Vedtaksfakta(private val navn: String) : Datakrav() {
    override fun oppfyltFor(vedtak: IverksattVedtak): Boolean {
        return finnData(mapOf("navn" to navn, "vedtakId" to vedtak.id))
    }

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

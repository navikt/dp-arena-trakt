package no.nav.dagpenger.arena.trakt.datakrav

import no.nav.dagpenger.arena.trakt.IverksattVedtak
import org.intellij.lang.annotations.Language

internal class Vedtak : Datakrav() {
    override fun oppfyltFor(vedtak: IverksattVedtak): Boolean {
        return finnData(mapOf("vedtakId" to vedtak.id))
    }

    @Language("PostgreSQL")
    override val query = """
        |SELECT data -> 'after' ->> 'VEDTAK_ID' AS id,
        |    data -> 'after' ->> 'UTFALLKODE' AS utfall
        |FROM data
        |WHERE data ->> 'op_type' = 'I'
        |  AND data -> 'after' ->> 'VEDTAK_ID' = :vedtakId
        """.trimMargin()
}

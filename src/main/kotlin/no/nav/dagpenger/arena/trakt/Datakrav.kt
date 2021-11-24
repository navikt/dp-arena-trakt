package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.db.BeregningsleddRepository
import org.intellij.lang.annotations.Language

internal abstract class Datakrav(val navn: String) {
    internal abstract fun oppfyltFor(vedtak: Vedtak): Boolean
}

internal class Beregningsledd(navn: String, val beregningsleddRepository: BeregningsleddRepository) : Datakrav(navn) {
    override fun oppfyltFor(vedtak: Vedtak): Boolean {
        return beregningsleddRepository.finn(navn, "vedtak", vedtak.id)
    }
}

internal class Vedtaksfakta(navn: String) : Datakrav(navn) {
    override fun oppfyltFor(vedtak: Vedtak): Boolean {
        @Language("PostgreSQL")
        val query = """SELECT data -> 'after' ->> 'BEREGNINGSLEDD_ID' AS id,
            |       data -> 'after' ->> 'VERDI'             AS verdi
            |FROM beregningsledd
            |WHERE data ->> 'op_type' = 'I'
            |  AND data -> 'after' ->> 'BEREGNINGSLEDDKODE' = 'BOAAP'
            |  AND data -> 'after' ->> 'TABELLNAVNALIAS_KILDE' = 'KVOTBR'
            |  AND data -> 'after' ->> 'OBJEKT_ID_KILDE' = '254588967'
            """.trimMargin()
        return false
    }
}

package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.db.BeregningsleddRepository
import no.nav.dagpenger.arena.trakt.db.VedtaksfaktaRepository
import org.intellij.lang.annotations.Language

internal abstract class Datakrav(val navn: String) {
    internal abstract fun oppfyltFor(vedtak: Vedtak): Boolean
}

internal class Beregningsledd(navn: String, val beregningsleddRepository: BeregningsleddRepository) : Datakrav(navn) {
    override fun oppfyltFor(vedtak: Vedtak): Boolean {
        // realtertObjektType + vedtakid er kobling til vedtak
        return beregningsleddRepository.finn(navn, "VEDTAK", vedtak.id)
    }
}

internal class Vedtaksfakta(navn: String, val vedtaksfaktaRepository: VedtaksfaktaRepository) : Datakrav(navn) {
    override fun oppfyltFor(vedtak: Vedtak): Boolean {
        return vedtaksfaktaRepository.finn(navn,vedtak.id)

    }
}

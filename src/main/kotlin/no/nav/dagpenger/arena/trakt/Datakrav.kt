package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.db.BeregningsleddRepository
import no.nav.dagpenger.arena.trakt.db.VedtakRepository
import no.nav.dagpenger.arena.trakt.db.VedtaksfaktaRepository

internal abstract class Datakrav() {
    internal abstract fun oppfyltFor(vedtak: IverksattVedtak): Boolean
}

internal class Beregningsledd(private val navn: String, private val beregningsleddRepository: BeregningsleddRepository) : Datakrav() {
    override fun oppfyltFor(vedtak: IverksattVedtak): Boolean {
        // realtertObjektType + vedtakid er kobling til vedtak
        return beregningsleddRepository.finn(navn, "VEDTAK", vedtak.id)
    }
}

internal class Vedtaksfakta(private val navn: String, private val vedtaksfaktaRepository: VedtaksfaktaRepository) : Datakrav() {
    override fun oppfyltFor(vedtak: IverksattVedtak): Boolean {
        return vedtaksfaktaRepository.finn(navn, vedtak.id)
    }
}

internal class Vedtak(private val vedtakRepository: VedtakRepository) : Datakrav() {
    override fun oppfyltFor(vedtak: IverksattVedtak): Boolean {
        return vedtakRepository.finn(vedtak.id)
    }
}

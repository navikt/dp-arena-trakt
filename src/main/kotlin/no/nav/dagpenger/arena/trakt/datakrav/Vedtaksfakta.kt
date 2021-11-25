package no.nav.dagpenger.arena.trakt.datakrav

import no.nav.dagpenger.arena.trakt.IverksattVedtak
import no.nav.dagpenger.arena.trakt.db.VedtaksfaktaRepository

internal class Vedtaksfakta(private val navn: String, private val vedtaksfaktaRepository: VedtaksfaktaRepository) : Datakrav() {
    override fun oppfyltFor(vedtak: IverksattVedtak): Boolean {
        return vedtaksfaktaRepository.finn(navn, vedtak.id)
    }
}
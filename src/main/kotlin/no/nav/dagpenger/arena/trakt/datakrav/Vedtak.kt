package no.nav.dagpenger.arena.trakt.datakrav

import no.nav.dagpenger.arena.trakt.IverksattVedtak
import no.nav.dagpenger.arena.trakt.db.VedtakRepository

internal class Vedtak(private val vedtakRepository: VedtakRepository) : Datakrav() {
    override fun oppfyltFor(vedtak: IverksattVedtak): Boolean {
        return vedtakRepository.finn(vedtak.id)
    }
}
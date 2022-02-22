package no.nav.dagpenger.arena.trakt.db

import no.nav.dagpenger.arena.trakt.db.HendelseRepository.Companion.fraVedtak
import no.nav.dagpenger.arena.trakt.db.VedtakRepository.VedtakObserver
import no.nav.dagpenger.arena.trakt.tjenester.VedtakService.Vedtak

internal class PubliserNyttVedtakObserver(
    private val hendelseRepository: HendelseRepository
) : VedtakObserver {
    override fun nyttDagpengeVedtak(vedtak: Vedtak) {
        hendelseRepository.publiser(fraVedtak(vedtak))
    }
}

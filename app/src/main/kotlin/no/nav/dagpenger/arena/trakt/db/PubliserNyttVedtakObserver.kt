package no.nav.dagpenger.arena.trakt.db

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.arena.trakt.db.HendelseRepository.Companion.fraVedtak
import no.nav.dagpenger.arena.trakt.db.VedtakRepository.VedtakObserver
import no.nav.dagpenger.arena.trakt.tjenester.VedtakSink.Vedtak

private val logger = KotlinLogging.logger {}

internal class PubliserNyttVedtakObserver(
    private val hendelseRepository: HendelseRepository
) : VedtakObserver {
    override fun nyttDagpengeVedtak(vedtak: Vedtak) {
        withLoggingContext(
            "sakId" to vedtak.sakId.toString(),
            "vedtakId" to vedtak.vedtakId.toString(),
        ) {
            logger.info { "Publiserer nytt dagpengevedtak" }
            hendelseRepository.publiser(fraVedtak(vedtak))
        }
    }
}

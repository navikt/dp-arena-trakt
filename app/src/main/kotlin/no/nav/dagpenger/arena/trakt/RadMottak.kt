package no.nav.dagpenger.arena.trakt

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.arena.trakt.db.HendelseRepository
import no.nav.dagpenger.arena.trakt.db.SakRepository
import no.nav.dagpenger.arena.trakt.db.VedtakRepository
import no.nav.dagpenger.arena.trakt.db.VedtakRepository.VedtakObserver
import no.nav.dagpenger.arena.trakt.meldinger.ReplikeringsMelding

private val logger = KotlinLogging.logger { }

// Behandler rader som tas i mot
internal class RadMottak(
    private val sakRepository: SakRepository,
    private val vedtakRepository: VedtakRepository,
    private val hendelseRepository: HendelseRepository,
) : IRadMottak, VedtakObserver {
    init {
        vedtakRepository.leggTilObserver(this)
    }

    override fun behandle(message: ReplikeringsMelding) {
        message.behandle(this)
    }

    override fun behandle(sak: Sak) {
        sakRepository.lagre(sak)
    }

    override fun behandle(vedtak: Vedtak) {
        vedtakRepository.lagre(vedtak)
    }

    override fun nyttDagpengeVedtak(vedtak: Vedtak) {
        withLoggingContext(
            "sakId" to vedtak.sakId.toString(),
            "vedtakId" to vedtak.vedtakId.toString(),
        ) {
            val vedtakHendelse = HendelseRepository.fraVedtak(vedtak)
            logger.info { "Publiserer nytt dagpengevedtak" }
            hendelseRepository.publiser(vedtakHendelse)
        }
    }
}

internal interface IRadMottak {
    fun behandle(message: ReplikeringsMelding)
    fun behandle(sak: Sak)
    fun behandle(vedtak: Vedtak)
}

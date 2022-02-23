package no.nav.dagpenger.arena.trakt.tjenester

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.arena.trakt.db.SakRepository
import no.nav.dagpenger.arena.trakt.db.VedtakRepository
import kotlin.concurrent.timer

val logger = KotlinLogging.logger { }

internal class PeriodiskUsendtVedtakSjekk(
    private val sakRepository: SakRepository,
    private val vedtakRepository: VedtakRepository
) {
    fun start() = timer(
        name = "periodiskSjekk",
        daemon = true,
        period = 10000L
    ) {
        logger.info { "Starter periodisk sjekk" }

        sakRepository.perSak {
            withLoggingContext("sakId" to sakId.toString()) {
                logger.info { "Antall vedtak i sak: ${vedtakRepository.antallVedtakMedSakId(sakId)}" }
                logger.info { "Sjekker etter usendte vedtak" }
                val vedtak = vedtakRepository.finnUsendteVedtakMedSak(sakId)
                logger.info { "Fant ${vedtak.size} usendte vedtak" }

                vedtak.forEach {
                    withLoggingContext("vedtakId" to it.vedtakId.toString()) {
                        logger.info { "Periodisk sjekk sender usendt vedtak." }
                    }
                }
            }
        }
    }
}

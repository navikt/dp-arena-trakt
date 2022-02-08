package no.nav.dagpenger.arena.trakt.db

import mu.KotlinLogging
import kotlin.concurrent.fixedRateTimer

private val logg = KotlinLogging.logger {}

internal class Sletterutine internal constructor(
    private val dataRepository: DataRepository,
    private val msFørSletterutineBegynner: Long = 1000L,
    private val msMellomSlettinger: Long = 10000L
) {
    private val batchStørrelse = 100000

    internal fun start() {
        fixedRateTimer(
            "Sletterutine",
            daemon = true, // JVMen avsluttes når det kun er daemon tråden som kjører
            initialDelay = msFørSletterutineBegynner,
            period = msMellomSlettinger
        ) {
            logg.info { "Sletterutine starter" }
            val raderSlettet = dataRepository.batchSlettDataSomIkkeOmhandlerDagpenger(batchStørrelse)
            logg.info { "Rader slettet: ${raderSlettet.sum()}" }
        }
    }
}

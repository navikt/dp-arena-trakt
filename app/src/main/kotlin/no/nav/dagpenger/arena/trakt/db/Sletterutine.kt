package no.nav.dagpenger.arena.trakt.db

import io.prometheus.client.Histogram
import mu.KotlinLogging
import kotlin.concurrent.timer

private val logg = KotlinLogging.logger {}

internal class Sletterutine internal constructor(
    private val dataRepository: DataRepository,
    private val msFørSletterutineBegynner: Long = 1000L,
    private val msMellomSlettinger: Long = 100L,
    private val batchStørrelse: Int = 1000
) {
    companion object {
        private val slettejobbLatency = Histogram.build()
            .namespace("dagpenger")
            .name("slettejobb_batch_seconds")
            .help("Antall sekunder det tar med en kjøring av sletterutina.")
            .register()
    }

    internal fun start() = timer(
        "Sletterutine",
        daemon = true, // JVMen avsluttes når det kun er sletterutine tråden som kjører
        initialDelay = msFørSletterutineBegynner,
        period = msMellomSlettinger,
    ) {
        slettejobbLatency.time {
            logg.info { "Sletterutine starter" }
            val raderSlettet = dataRepository.batchSlettDataSomIkkeOmhandlerDagpenger(batchStørrelse)
            logg.info { "Rader slettet: ${raderSlettet.sum()}" }
        }
    }
}

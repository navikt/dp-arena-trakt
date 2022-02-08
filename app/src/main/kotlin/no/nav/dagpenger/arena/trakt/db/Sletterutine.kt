package no.nav.dagpenger.arena.trakt.db

import mu.KotlinLogging
import java.util.Timer
import java.util.TimerTask

private val logg = KotlinLogging.logger {}

internal class Sletterutine internal constructor(
    private val dataRepository: DataRepository,
    private val msFørSletterutineBegynner: Long = 1000L,
    private val msMellomSlettinger: Long = 10000L
) {

    private class SletteTask(val dataRepository: DataRepository) : TimerTask() {
        override fun run() {
            val batchStørrelse = 100000
            val raderSlettet = dataRepository.batchSlettDataSomIkkeOmhandlerDagpenger(batchStørrelse)
            logg.info { "Rader slettet: ${raderSlettet.sum()}" }
        }
    }

    internal fun start() {
        Timer().schedule(
            SletteTask(dataRepository),
            msFørSletterutineBegynner,
            msMellomSlettinger
        )
    }
}

package no.nav.dagpenger.arena.trakt.db

import java.util.Timer
import java.util.TimerTask

internal class Sletterutine internal constructor(
    private val dataRepository: DataRepository,
    private val tidFørSletterutineBegynner: Long = 1000L,
    private val periodeMellomSlettinger: Long = 1000L
) {

    private class SletteTask(val dataRepository: DataRepository) : TimerTask() {
        override fun run() {
            dataRepository.slettDataSomIkkeOmhandlerDagpenger()
        }
    }

    internal fun start() {
        Timer().schedule(
            SletteTask(dataRepository),
            tidFørSletterutineBegynner,
            periodeMellomSlettinger
        )
    }
}

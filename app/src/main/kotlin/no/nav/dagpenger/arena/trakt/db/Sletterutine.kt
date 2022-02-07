package no.nav.dagpenger.arena.trakt.db

import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

internal class Sletterutine internal constructor(
    private val dataRepository: DataRepository,
    tidFørSletterutineBegynner: Long = 1000L,
    periodeMellomSlettinger: Long = 1000L
) {
    private val sletteRutine: TimerTask = Timer("Sletterutine").schedule(
        delay = tidFørSletterutineBegynner,
        period = periodeMellomSlettinger
    ) {
        dataRepository.slettDataSomIkkeOmhandlerDagpenger()
    }

    internal fun start() {
        sletteRutine.run()
    }
}

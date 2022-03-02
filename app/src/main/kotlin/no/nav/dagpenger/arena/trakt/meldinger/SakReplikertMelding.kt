package no.nav.dagpenger.arena.trakt.meldinger

import no.nav.dagpenger.arena.trakt.IRadMottak
import no.nav.dagpenger.arena.trakt.Sak
import no.nav.helse.rapids_rivers.JsonMessage
import java.time.LocalDateTime

// Kan tolke en replikert rad fra sak-tabellen
internal class SakReplikertMelding(packet: JsonMessage) : ReplikeringsMelding(packet) {
    private val sakId = packet["after.SAK_ID"].asInt()
    private val erDagpenger = packet["after.SAKSKODE"].asText() == "DAGP"
    private val sak
        get() = Sak(
            sakId,
            erDagpenger,
            LocalDateTime.now(),
            LocalDateTime.now()
        )

    override fun behandle(mediator: IRadMottak) {
        mediator.behandle(sak)
    }
}
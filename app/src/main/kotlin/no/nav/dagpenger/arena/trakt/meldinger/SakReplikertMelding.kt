package no.nav.dagpenger.arena.trakt.meldinger

import no.nav.dagpenger.arena.trakt.modell.Sak
import no.nav.dagpenger.arena.trakt.IHendelseMediator
import no.nav.helse.rapids_rivers.JsonMessage

internal class SakReplikertMelding(packet: JsonMessage) : ReplikeringsMelding(packet) {
    private val sakId = packet["after.SAK_ID"].asInt()
    private val erDagpenger = packet["after.SAKSKODE"].asText() == "DAGP"
    private val sak
        get() = Sak(
            sakId,
            erDagpenger
        )

    override fun behandle(mediator: IHendelseMediator) {
        mediator.behandle(this, sak)
    }
}

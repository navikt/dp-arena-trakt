package no.nav.dagpenger.arena.trakt.tjenester

import no.nav.dagpenger.arena.trakt.IMeldingMediator
import no.nav.dagpenger.arena.trakt.meldinger.SakReplikertMelding
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

internal class SakRiver(
    rapidsConnection: RapidsConnection,
    mediator: IMeldingMediator
) : ReplikeringsRiver(rapidsConnection, mediator) {
    override val tabell = "SIAMO.SAK"
    override val riverName = "sak"

    override fun validate(message: JsonMessage) {
        message.requireKey(
            "after.SAK_ID",
            "after.SAKSKODE",
        )
    }

    override fun opprettMelding(packet: JsonMessage) = SakReplikertMelding(packet)
}

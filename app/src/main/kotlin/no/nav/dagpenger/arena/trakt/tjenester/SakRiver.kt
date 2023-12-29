package no.nav.dagpenger.arena.trakt.tjenester

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.arena.trakt.IReplikeringMediator
import no.nav.dagpenger.arena.trakt.meldinger.SakReplikertMelding
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

internal class SakRiver(
    rapidsConnection: RapidsConnection,
    mediator: IReplikeringMediator,
) : ReplikeringsRiver(rapidsConnection, mediator) {
    override val tabell = "SIAMO.SAK"
    override val riverName = "sak"

    override fun validate(message: JsonMessage) {
        message.requireKey(
            "after.SAK_ID",
            "after.SAKSKODE",
        )
        message.require("after.REG_DATO", JsonNode::asArenaDato)
        message.require("after.MOD_DATO", JsonNode::asArenaDato)
    }

    override fun opprettMelding(packet: JsonMessage) = SakReplikertMelding(packet)
}

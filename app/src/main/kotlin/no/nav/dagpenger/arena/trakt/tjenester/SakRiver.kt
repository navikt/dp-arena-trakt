package no.nav.dagpenger.arena.trakt.tjenester

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import no.nav.dagpenger.arena.trakt.IReplikeringMediator
import no.nav.dagpenger.arena.trakt.meldinger.SakReplikertMelding

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

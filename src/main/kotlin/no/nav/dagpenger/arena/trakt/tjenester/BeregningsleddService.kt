package no.nav.dagpenger.arena.trakt.tjenester

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val sikkerlogg = KotlinLogging.logger("tjenestekall.beregningsledd")

class BeregningsleddService(rapidsConnection: RapidsConnection) : River.PacketListener {
    init {
        River(rapidsConnection).validate {
            it.demandValue("table", "SIAMO.BEREGNINGSLEDD")
            it.demandKey("@lagret FOOOOOOo")
            it.requireKey(
                "after.TABELLNAVNALIAS_KILDE",
                "after.OBJEKT_ID_KILDE"
            )
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        withLoggingContext(
            "tabell" to packet["table"].asText(),
        ) {
            sikkerlogg.info { packet.toJson() }
        }
    }
}

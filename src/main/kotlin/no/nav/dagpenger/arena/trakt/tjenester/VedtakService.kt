package no.nav.dagpenger.arena.trakt.tjenester

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val sikkerlogg = KotlinLogging.logger("tjenestekall.arena-topic")
private const val MAX_ANTALL_MELDINGER_LEST = 10000

class VedtakService(rapidsConnection: RapidsConnection) : River.PacketListener {
    var meldingerLest = 0

    init {
        River(rapidsConnection).validate {
            it.demandValue("table", "SIAMO.VEDTAK")
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        if (meldingerLest++ < MAX_ANTALL_MELDINGER_LEST) {
            withLoggingContext(
                "tabell" to packet["table"].asText(),
            ) {
                sikkerlogg.info { packet.toJson() }
            }
        }
    }
}

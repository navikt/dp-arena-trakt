package no.nav.dagpenger.arena.trakt.hendelser

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val sikkerlogg = KotlinLogging.logger("tjenestekall.arena-topic")
private const val MAX_ANTALL_MELDINGER_LEST = 10000

class BeregningsleddoppdateringService(rapidsConnection: RapidsConnection) : River.PacketListener {
    var meldingerLest = 0

    init {
        River(rapidsConnection).register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        if (meldingerLest++ < MAX_ANTALL_MELDINGER_LEST) {
            sikkerlogg.info { packet.toJson() }
        }
    }
}

package no.nav.dagpenger.arena.trakt.hendelser

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

val log = KotlinLogging.logger {}

const val MAX_ANTALL_MELDINGER_LEST = 1000

class BeregningsleddoppdateringService(rapidsConnection: RapidsConnection) : River.PacketListener {
    var meldingerLest = 0;

    init {
        River(rapidsConnection).apply {
            validate {
                it.requireKey("beregningsledd_id")
            }
        }
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        if(meldingerLest < MAX_ANTALL_MELDINGER_LEST) {
            log.info { packet }
            meldingerLest++
        }
    }
}

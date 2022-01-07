package no.nav.dagpenger.arena.trakt.tjenester

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall.data-mottak")

internal class DataMottakService(
    rapidsConnection: RapidsConnection,
    private val dataRepository: DataRepository
) : River.PacketListener {
    init {
        River(rapidsConnection).validate {
            it.requireKey(
                "table",
                "pos",
                "current_ts",
                "op_ts"
            )
        }.register(this)
    }

    companion object {
        private var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS]")
        private fun JsonNode.asArenaDato() =
            asText().let { LocalDateTime.parse(it, formatter) }

        private fun JsonNode.asOptionalArenaDato() =
            takeIf(JsonNode::isTextual)?.asText()?.takeIf(String::isNotEmpty)
                ?.let { LocalDateTime.parse(it, formatter) }
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val tabell = packet["table"].asText()
        val pos = packet["pos"].asText()
        val skjedde = packet["op_ts"].asArenaDato()
        val replikert = packet["current_ts"].asArenaDato()

        withLoggingContext(
            "tabell" to tabell,
        ) {
            logg.info { "Mottok data fra Arena" }

            dataRepository.lagre(tabell, pos, skjedde, replikert, packet.toJson())
            dataRepository.lagre(tabell + "1", pos, skjedde, replikert, packet.toJson())
            dataRepository.lagre(tabell + "2", pos, skjedde, replikert, packet.toJson())
        }
    }
}

package no.nav.dagpenger.arena.trakt.tjenester

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.arena.trakt.IReplikeringMediator
import no.nav.dagpenger.arena.trakt.meldinger.ReplikeringsMelding
import no.nav.dagpenger.arena.trakt.meldinger.ReplikeringsMelding.ReplikeringsId
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal abstract class ReplikeringsRiver(
    rapidsConnection: RapidsConnection,
    private val replikeringMediator: IReplikeringMediator
) : River.PacketValidation {
    private val river = River(rapidsConnection)
    protected abstract val tabell: String
    protected abstract val riverName: String

    init {
        RiverImpl(river)
    }

    private fun validateReplikering(packet: JsonMessage) {
        packet.demandValue("table", tabell)
        packet.requireKey("op_type", "pos")
        packet.require("op_ts", JsonNode::asArenaDato)
    }

    protected abstract fun opprettMelding(packet: JsonMessage): ReplikeringsMelding

    private inner class RiverImpl(river: River) : River.PacketListener {
        init {
            river.validate(::validateReplikering)
            river.validate(this@ReplikeringsRiver)
            river.register(this)
        }

        override fun onPacket(packet: JsonMessage, context: MessageContext) {
            val id = ReplikeringsId(tabell, packet["pos"].asText())
            withLoggingContext(
                "river_name" to riverName,
                "tabell" to tabell,
                "replikering_id" to id.toString()
            ) {
                try {
                    replikeringMediator.onRecognizedMessage(opprettMelding(packet), context)
                } catch (e: Exception) {
                    sikkerLogg.error("Klarte ikke å lese melding, innhold: ${packet.toJson()}", e)
                    if (packet["pos"].asText() != "00000000040155609387") throw e
                }
            }
        }

        override fun onError(problems: MessageProblems, context: MessageContext) {
            replikeringMediator.onRiverError(riverName, problems, context)
        }
    }

    companion object {
        private val sikkerLogg = KotlinLogging.logger("tjenestekall.ReplikeringsRiver")
    }
}

private var arenaDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS]")
fun JsonNode.asArenaDato(): LocalDateTime =
    asText().let { LocalDateTime.parse(it, arenaDateFormatter) }

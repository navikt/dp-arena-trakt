package no.nav.dagpenger.arena.trakt.tjenester

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.arena.trakt.IReplikeringMediator
import no.nav.dagpenger.arena.trakt.meldinger.ReplikeringsMelding
import no.nav.dagpenger.arena.trakt.meldinger.ReplikeringsMelding.ReplikeringsId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal abstract class ReplikeringsRiver(
    rapidsConnection: RapidsConnection,
    private val replikeringMediator: IReplikeringMediator,
) : River.PacketValidation {
    private val river = River(rapidsConnection)
    protected abstract val tabell: String
    protected abstract val riverName: String

    init {
        RiverImpl(river)
    }

    private fun validateReplikering(packet: JsonMessage) {
        packet.requireValue("table", tabell)
        packet.requireKey("op_type", "pos")
        packet.require("op_ts", JsonNode::asArenaDato)
    }

    protected abstract fun opprettMelding(packet: JsonMessage): ReplikeringsMelding

    private inner class RiverImpl(
        river: River,
    ) : River.PacketListener {
        init {
            river.validate(::validateReplikering)
            river.validate(this@ReplikeringsRiver)
            river.register(this)
        }

        override fun onPacket(
            packet: JsonMessage,
            context: MessageContext,
            metadata: MessageMetadata,
            meterRegistry: MeterRegistry,
        ) {
            val id = ReplikeringsId(tabell, packet["pos"].asText())
            withLoggingContext(
                "river_name" to riverName,
                "tabell" to tabell,
                "replikering_id" to id.toString(),
            ) {
                try {
                    replikeringMediator.onRecognizedMessage(opprettMelding(packet), context)
                } catch (e: Exception) {
                    sikkerLogg.error("Klarte ikke å lese melding, innhold: ${packet.toJson()}", e)
                    throw e
                }
            }
        }

        override fun onError(
            problems: MessageProblems,
            context: MessageContext,
            metadata: MessageMetadata,
        ) {
            replikeringMediator.onRiverError(riverName, problems, context)
        }
    }

    companion object {
        private val sikkerLogg = KotlinLogging.logger("tjenestekall.ReplikeringsRiver")
    }
}

private var arenaDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS]")

fun JsonNode.asArenaDato(): LocalDateTime = asText().let { LocalDateTime.parse(it, arenaDateFormatter) }

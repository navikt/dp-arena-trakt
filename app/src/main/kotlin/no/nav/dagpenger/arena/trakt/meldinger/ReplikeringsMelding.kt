package no.nav.dagpenger.arena.trakt.meldinger

import no.nav.dagpenger.arena.trakt.IHendelseMediator
import no.nav.dagpenger.arena.trakt.tjenester.asArenaDato
import no.nav.helse.rapids_rivers.JsonMessage
import org.slf4j.Logger

internal abstract class ReplikeringsMelding(private val packet: JsonMessage) {
    private val tabell = packet["table"].asText()
    private val posisjon = packet["pos"].asText()
    internal val id = ReplikeringsId(tabell, posisjon)
    internal val operasjon = packet["op_type"].asText()
    internal val operasjon_ts = packet["op_ts"].asArenaDato()
    internal open val skalDuplikatsjekkes = true

    internal abstract fun behandle(mediator: IHendelseMediator)

    internal fun logRecognized(logger: Logger) =
        logger.info("gjenkjente {} med ={}\n{}", this::class.simpleName, id, toJson())

    internal fun logDuplikat(logger: Logger) =
        logger.warn("Har mottatt duplikat {} med id={}", this::class.simpleName, id)

    internal fun toJson() = packet.toJson()

    internal class ReplikeringsId(internal val tabell: String, internal val posisjon: String) {
        override fun toString(): String {
            return "$tabell.$posisjon"
        }
    }
}

package no.nav.dagpenger.arena.trakt.meldinger

import no.nav.dagpenger.arena.trakt.IRadMottak
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

    internal abstract fun behandle(mediator: IRadMottak)
    internal abstract fun meldingBeskrivelse(): String

    internal fun logRecognized(logger: Logger) =
        logger.info("gjenkjente {} med id={} som={}", this::class.simpleName, id, meldingBeskrivelse())

    internal fun logDuplikat(logger: Logger) =
        logger.warn("har mottatt duplikat {} med id={}", this::class.simpleName, id)

    internal fun toJson() = packet.toJson()

    internal class ReplikeringsId(private val tabell: String, private val posisjon: String) {
        override fun toString(): String {
            return "$tabell.$posisjon"
        }
    }
}

package no.nav.dagpenger.arena.trakt.meldinger

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import no.nav.dagpenger.arena.trakt.IRadMottak
import no.nav.dagpenger.arena.trakt.tjenester.asArenaDato
import org.slf4j.Logger

@Suppress("ktlint:standard:property-naming")
internal abstract class ReplikeringsMelding(
    private val packet: JsonMessage,
) {
    private val tabell = packet["table"].asText()
    private val posisjon = packet["pos"].asText()
    internal val id = ReplikeringsId(tabell, posisjon)
    internal val operasjon = packet["op_type"].asText()
    internal val operasjon_ts = packet["op_ts"].asArenaDato()
    internal open val skalDuplikatsjekkes = true

    internal abstract fun behandle(mediator: IRadMottak)

    internal abstract fun meldingBeskrivelse(): String

    internal fun logRecognized(logger: Logger) =
        logger.info("gjenkjente {} med id={} som={}:\n{}", this::class.simpleName, id, meldingBeskrivelse(), toJson())

    internal fun logDuplikat(logger: Logger) = logger.warn("har mottatt duplikat {} med id={}", this::class.simpleName, id)

    internal fun toJson() = packet.toJson()

    internal class ReplikeringsId(
        private val tabell: String,
        private val posisjon: String,
    ) {
        override fun toString(): String = "$tabell.$posisjon"
    }
}

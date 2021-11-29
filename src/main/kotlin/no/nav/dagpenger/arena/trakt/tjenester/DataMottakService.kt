package no.nav.dagpenger.arena.trakt.tjenester

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.db.HendelseRepository
import no.nav.dagpenger.arena.trakt.serde.VedtakIverksattJsonBuilder
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val sikkerlogg = KotlinLogging.logger("tjenestekall.data-mottak")

internal class DataMottakService(
    rapidsConnection: RapidsConnection,
    private val dataRepository: DataRepository,
    private val hendelseRepository: HendelseRepository
) : River.PacketListener {
    init {
        River(rapidsConnection).validate {
            it.demandKey("table")
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        withLoggingContext(
            "tabell" to packet["table"].asText(),
        ) {
            sikkerlogg.info { "Mottok pakke fra Arena" }

            dataRepository.lagre(packet.toJson())

            hendelseRepository.finnFerdigeHendelser().forEach { ferdigHendelse ->
                when (ferdigHendelse.type) {
                    Hendelse.Type.VedtakIverksatt -> VedtakIverksattJsonBuilder(ferdigHendelse).resultat()
                    Hendelse.Type.VedtakEndret -> TODO()
                    Hendelse.Type.BeregningUtført -> TODO()
                }.also {
                    context.publish(it.toString())
                }
            }
        }
    }
}

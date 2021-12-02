package no.nav.dagpenger.arena.trakt.tjenester

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.arena.trakt.Hendelse.Companion.vedtak
import no.nav.dagpenger.arena.trakt.db.HendelseRepository
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val sikkerlogg = KotlinLogging.logger("tjenestekall.vedtaksfakta")

internal class VedtaksfaktaService(
    rapidsConnection: RapidsConnection,
    private val hendelseRepository: HendelseRepository
) : River.PacketListener {
    init {
        River(rapidsConnection).validate {
            it.demandValue("table", "SIAMO.VEDTAKFAKTA")
            it.requireKey(
                "after.VEDTAK_ID",
                "after.VEDTAKFAKTAKODE"
            )
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val navn = packet["after.VEDTAKFAKTAKODE"].asText()
        val vedtakId = packet["after.VEDTAK_ID"].asText()

        withLoggingContext(
            "tabell" to packet["table"].asText(),
            "vedtakId" to vedtakId,
            "navn" to navn
        ) {
            sikkerlogg.info { "Mottok vedtaksfakta ($navn)" }
            val vedtakHendelse = vedtak(vedtakId)

            if (hendelseRepository.leggPåKø(vedtakHendelse).isEmpty()) {
                sikkerlogg.info { "Har komplett datasett, publiserer hendelse" }
            } else {
                sikkerlogg.info { "Det mangler fortsatt data, vi må vente litt til" }
            }
        }
    }
}

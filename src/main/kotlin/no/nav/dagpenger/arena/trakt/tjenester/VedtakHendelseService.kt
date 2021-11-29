package no.nav.dagpenger.arena.trakt.tjenester

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.arena.trakt.Hendelse.Companion.vedtakEndret
import no.nav.dagpenger.arena.trakt.Hendelse.Companion.vedtakIverksatt
import no.nav.dagpenger.arena.trakt.db.HendelseRepository
import no.nav.dagpenger.arena.trakt.serde.VedtakIverksattJsonBuilder
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val sikkerlogg = KotlinLogging.logger("tjenestekall.vedtak")

internal class VedtakHendelseService(
    rapidsConnection: RapidsConnection,
    private val hendelseRepository: HendelseRepository
) : River.PacketListener {
    init {
        River(rapidsConnection).validate {
            it.demandValue("table", "SIAMO.VEDTAK")
            it.requireKey(
                "after.VEDTAK_ID",
                "after.VEDTAKTYPEKODE"
            )
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val vedtakId = packet["after.VEDTAK_ID"].asText()
        val type = packet["after.VEDTAKTYPEKODE"].asText()

        withLoggingContext(
            "tabell" to packet["table"].asText(),
            "vedtakId" to vedtakId,
            "type" to type
        ) {
            sikkerlogg.info { "Mottok lagret vedtak" }
            val vedtakHendelse = when (type) {
                "O" -> vedtakIverksatt(vedtakId)
                else -> vedtakEndret(vedtakId)
            }

            if (vedtakHendelse.komplett()) {
                sikkerlogg.info { "Har komplett datasett, publiserer hendelse" }
                context.publish(VedtakIverksattJsonBuilder(vedtakHendelse).resultat().toString())
            } else {
                hendelseRepository.leggTilVent(vedtakHendelse)
                sikkerlogg.info { "Det mangler fortsatt data, vi må vente litt til" }
            }
        }
    }
}

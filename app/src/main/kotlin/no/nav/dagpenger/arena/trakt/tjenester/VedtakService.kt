package no.nav.dagpenger.arena.trakt.tjenester

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.VEDTAK_TABELL
import no.nav.dagpenger.arena.trakt.db.HendelseRepository
import no.nav.dagpenger.arena.trakt.db.HendelseRepository.Companion.vedtak
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall.vedtak")

internal class VedtakService(
    rapidsConnection: RapidsConnection,
    private val hendelseRepository: HendelseRepository
) : River.PacketListener {
    init {
        River(rapidsConnection).validate {
            it.demandValue("table", VEDTAK_TABELL)
            it.requireKey(
                "after.VEDTAK_ID",
                "after.VEDTAKTYPEKODE"
            )
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val vedtakId = packet["after.VEDTAK_ID"].asText()

        withLoggingContext(
            "tabell" to packet["table"].asText(),
            "vedtakId" to vedtakId,
        ) {
            val vedtakHendelse = vedtak(vedtakId)
            logg.info { "Mottok vedtak med hendelseId: ${vedtakHendelse.hendelseId}" }

            if (hendelseRepository.leggPåKø(vedtakHendelse)) {
                logg.info { "Har komplett datasett, publiserer ${vedtakHendelse.hendelseId}" }
            } else {
                logg.info { "Det mangler fortsatt data for ${vedtakHendelse.hendelseId}, vi må vente litt til" }
            }
        }
    }
}

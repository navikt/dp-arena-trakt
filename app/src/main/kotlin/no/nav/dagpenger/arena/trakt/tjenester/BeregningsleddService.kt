package no.nav.dagpenger.arena.trakt.tjenester

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.arena.trakt.db.HendelseRepository
import no.nav.dagpenger.arena.trakt.db.HendelseRepository.Companion.vedtak
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall.beregningsledd")

internal class BeregningsleddService(
    rapidsConnection: RapidsConnection,
    private val hendelseRepository: HendelseRepository
) : River.PacketListener {
    init {
        River(rapidsConnection).validate {
            it.demandValue("table", "SIAMO.BEREGNINGSLEDD")
            it.demandValue("after.TABELLNAVNALIAS_KILDE", "VEDTAK")
            it.requireKey(
                "after.BEREGNINGSLEDDKODE",
                "after.OBJEKT_ID_KILDE"
            )
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error { problems.toExtendedReport() }
    }

    override fun onSevere(error: MessageProblems.MessageException, context: MessageContext) {
        sikkerlogg.error { error.toString() }
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val navn = packet["after.BEREGNINGSLEDDKODE"].asText()
        val vedtakId = packet["after.OBJEKT_ID_KILDE"].asText()

        withLoggingContext(
            "tabell" to packet["table"].asText(),
            "vedtakId" to vedtakId,
            "navn" to navn
        ) {
            val vedtakHendelse = vedtak(vedtakId)
            logg.info { "Mottok beregningsledd med hendelseId: ${vedtakHendelse.hendelseId}" }

            if (hendelseRepository.leggPåKø(vedtakHendelse)) {
                logg.info { "Har komplett datasett, publiserer ${vedtakHendelse.hendelseId}" }
            } else {
                logg.info { "Det mangler fortsatt data for ${vedtakHendelse.hendelseId}, vi må vente litt til" }
            }
        }
    }
}

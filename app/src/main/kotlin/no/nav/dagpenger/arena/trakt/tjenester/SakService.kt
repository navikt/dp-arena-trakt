package no.nav.dagpenger.arena.trakt.tjenester

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.arena.trakt.db.SakRepository
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall.vedtak")

internal class SakService(
    rapidsConnection: RapidsConnection,
    private val repository: SakRepository,
) : River.PacketListener {
    init {
        River(rapidsConnection).validate {
            it.demandValue("table", "SIAMO.SAK")
            it.requireKey(
                "after.SAK_ID",
                "after.SAKSKODE",
            )
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val sakId = packet["after.SAK_ID"].asInt()

        withLoggingContext(
            "sakId" to sakId.toString(),
        ) {
            val sak = Sak(
                sakId = sakId,
                erDagpenger = packet.erDagpenger()
            )
            logg.info { "Mottok sak. Var det dagpenger? ${if (packet.erDagpenger()) "Nei" else "Ja"}" }
            repository.lagre(sak)
        }
    }

    internal data class Sak(
        val sakId: Int,
        val erDagpenger: Boolean,
    )
}

private fun JsonMessage.erDagpenger() = this["after.SAKSKODE"].asText() == "DAGP"

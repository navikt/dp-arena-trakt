package no.nav.dagpenger.arena.trakt.tjenester

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.arena.trakt.db.VedtakRepository
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall.vedtak")

internal class VedtakService(
    rapidsConnection: RapidsConnection,
    private val vedtakRepository: VedtakRepository,
) : River.PacketListener {
    init {
        River(rapidsConnection).validate {
            it.demandValue("table", "SIAMO.VEDTAK")
            it.requireKey(
                "after.SAK_ID",
                "after.VEDTAK_ID",
                "after.PERSON_ID",
                "after.VEDTAKTYPEKODE",
                "after.UTFALLKODE",
                "after.RETTIGHETKODE",
                "after.VEDTAKSTATUSKODE"
            )
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val vedtakId = packet["after.VEDTAK_ID"].asInt()

        withLoggingContext(
            "tabell" to packet["table"].asText(),
            "vedtakId" to vedtakId.toString(),
        ) {
            val vedtak = Vedtak(
                sakId = packet.sakId(),
                vedtakId = vedtakId,
                personId = packet.personId(),
                vedtaktypekode = packet.vedtaktypekode(),
                utfallkode = packet.utfallkode(),
                rettighetkode = packet.rettighetkode(),
                vedtakstatuskode = packet.vedtakstatuskode()
            )
            logg.info { "Mottok vedtak" }
            sikkerlogg.info { "Mottok vedtak: $vedtak" }
            vedtakRepository.lagre(vedtak)
        }
    }

    internal data class Vedtak(
        val sakId: Int,
        val vedtakId: Int,
        val personId: Int,
        val vedtaktypekode: String,
        val utfallkode: String,
        val rettighetkode: String,
        val vedtakstatuskode: String,
    )
}

private fun JsonMessage.sakId() = this["after.SAK_ID"].asInt()
private fun JsonMessage.utfallkode() = this["after.UTFALLKODE"].asText()
private fun JsonMessage.rettighetkode() = this["after.RETTIGHETKODE"].asText()
private fun JsonMessage.vedtakstatuskode() = this["after.RETTIGHETKODE"].asText()
private fun JsonMessage.vedtaktypekode() = this["after.VEDTAKTYPEKODE"].asText()
private fun JsonMessage.personId() = this["after.PERSON_ID"].asInt()

package no.nav.dagpenger.arena.trakt.meldinger

import no.nav.dagpenger.arena.trakt.modell.Vedtak
import no.nav.dagpenger.arena.trakt.IHendelseMediator
import no.nav.helse.rapids_rivers.JsonMessage

internal class VedtakReplikertMelding(packet: JsonMessage) : ReplikeringsMelding(packet) {
    val vedtakId = packet["after.VEDTAK_ID"].asInt()
    private val sakId = packet["after.SAK_ID"].asInt()
    private val utfallkode = packet["after.UTFALLKODE"].asText()
    private val rettighetkode = packet["after.RETTIGHETKODE"].asText()
    private val vedtakstatuskode = packet["after.VEDTAKSTATUSKODE"].asText()
    private val vedtaktypekode = packet["after.VEDTAKTYPEKODE"].asText()
    private val personId = packet["after.PERSON_ID"].asInt()
    private val vedtak
        get() = Vedtak(
            sakId,
            vedtakId,
            personId,
            vedtaktypekode,
            utfallkode,
            rettighetkode,
            vedtakstatuskode
        )

    override fun behandle(mediator: IHendelseMediator) {
        mediator.behandle(this, vedtak)
    }
}

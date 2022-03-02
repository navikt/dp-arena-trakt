package no.nav.dagpenger.arena.trakt.meldinger

import no.nav.dagpenger.arena.trakt.IRadMottak
import no.nav.dagpenger.arena.trakt.Vedtak
import no.nav.dagpenger.arena.trakt.tjenester.asArenaDato
import no.nav.helse.rapids_rivers.JsonMessage

// Tolker innholdet i en replikert rad fra vedtakstabellen
internal class VedtakReplikertMelding(packet: JsonMessage) : ReplikeringsMelding(packet) {
    private val vedtakId = packet["after.VEDTAK_ID"].asInt()
    private val sakId = packet["after.SAK_ID"].asInt()
    private val utfallkode = packet["after.UTFALLKODE"].asText()
    private val rettighetkode = packet["after.RETTIGHETKODE"].asText()
    private val vedtakstatuskode = packet["after.VEDTAKSTATUSKODE"].asText()
    private val vedtaktypekode = packet["after.VEDTAKTYPEKODE"].asText()
    private val personId = packet["after.PERSON_ID"].asInt()
    private val opprettet = packet["after.REG_DATO"].asArenaDato()
    private val oppdatert = packet["after.MOD_DATO"].asArenaDato()
    private val løpenummer = packet["after.LOPENRVEDTAK"].asInt()
    private val vedtak
        get() = Vedtak(
            sakId,
            vedtakId,
            personId,
            vedtaktypekode,
            utfallkode,
            rettighetkode,
            vedtakstatuskode,
            opprettet,
            oppdatert
        )

    override fun behandle(mediator: IRadMottak) {
        mediator.behandle(vedtak)
    }

    override fun meldingBeskrivelse() = "Vedtak=$vedtakId, sak=$sakId, løpenummer=$løpenummer"
}

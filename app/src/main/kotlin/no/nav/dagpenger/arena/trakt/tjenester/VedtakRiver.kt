package no.nav.dagpenger.arena.trakt.tjenester

import no.nav.dagpenger.arena.trakt.IReplikeringMediator
import no.nav.dagpenger.arena.trakt.meldinger.VedtakReplikertMelding
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

// Fanger opp vedtak og tolker de
internal class VedtakRiver(
    rapidsConnection: RapidsConnection,
    mediator: IReplikeringMediator
) : ReplikeringsRiver(rapidsConnection, mediator) {
    override val tabell = "SIAMO.VEDTAK"
    override val riverName = "vedtak"

    override fun validate(message: JsonMessage) {
        message.requireKey(
            "after.SAK_ID",
            "after.VEDTAK_ID",
            "after.PERSON_ID",
            "after.VEDTAKTYPEKODE",
            "after.UTFALLKODE",
            "after.RETTIGHETKODE",
            "after.VEDTAKSTATUSKODE",
            "after.REG_DATO",
            "after.MOD_DATO",
            "after.LOPENRVEDTAK",
        )
    }

    override fun opprettMelding(packet: JsonMessage) = VedtakReplikertMelding(packet)
}

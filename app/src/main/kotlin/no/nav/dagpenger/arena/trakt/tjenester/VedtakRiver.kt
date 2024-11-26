package no.nav.dagpenger.arena.trakt.tjenester

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import no.nav.dagpenger.arena.trakt.IReplikeringMediator
import no.nav.dagpenger.arena.trakt.meldinger.VedtakReplikertMelding

// Fanger opp vedtak og tolker de
internal class VedtakRiver(
    rapidsConnection: RapidsConnection,
    mediator: IReplikeringMediator,
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
            "after.LOPENRSAK",
            "after.AAR",
        )
        message.forbidValue("after.RETTIGHETKODE", "AA115")
        message.forbidValue("after.RETTIGHETKODE", "AAP")
        message.forbidValue("after.RETTIGHETKODE", "BEHOV")
        message.forbidValue("after.RETTIGHETKODE", "TILTAK")
    }

    override fun opprettMelding(packet: JsonMessage) = VedtakReplikertMelding(packet)
}

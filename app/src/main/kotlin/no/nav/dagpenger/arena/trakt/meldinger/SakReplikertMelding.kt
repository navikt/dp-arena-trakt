package no.nav.dagpenger.arena.trakt.meldinger

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import mu.KotlinLogging
import no.nav.dagpenger.arena.trakt.IRadMottak
import no.nav.dagpenger.arena.trakt.Sak
import no.nav.dagpenger.arena.trakt.tjenester.asArenaDato

private val sikkerlogg = KotlinLogging.logger("tjenestekall.sak")

// Kan tolke en replikert rad fra sak-tabellen
internal class SakReplikertMelding(
    private val packet: JsonMessage,
) : ReplikeringsMelding(packet) {
    private val sakId = packet["after.SAK_ID"].asInt()
    private val erDagpenger = packet["after.SAKSKODE"].asText() == "DAGP"
    private val opprettet = packet["after.REG_DATO"].asArenaDato()
    private val oppdatert = packet["after.MOD_DATO"].asArenaDato()
    private val sak: Sak
        get() {
            return Sak(
                sakId,
                erDagpenger,
                opprettet,
                oppdatert,
            ).also {
                sikkerlogg.info { "Fikk en replikert sak: JSON=${packet.toJson()}" }
            }
        }

    override fun behandle(mediator: IRadMottak) {
        mediator.behandle(sak)
    }

    override fun meldingBeskrivelse() = "Sak: $sakId, erDagpenger: $erDagpenger"
}

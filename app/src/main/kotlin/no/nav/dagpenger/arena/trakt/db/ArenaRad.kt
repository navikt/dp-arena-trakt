package no.nav.dagpenger.arena.trakt.db

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.BEREGNINGSLEDD_TABELL
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.SAK_TABELL
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.VEDTAKFAKTA_TABELL
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.VEDTAK_TABELL

internal object ArenaKoder {
    const val DAGPENGE_SAK = "DAGP"
    const val SAK_TABELL = "SIAMO.SAK"
    const val VEDTAK_TABELL = "SIAMO.VEDTAK"
    const val VEDTAKFAKTA_TABELL = "SIAMO.VEDTAKFAKTA"
    const val BEREGNINGSLEDD_TABELL = "SIAMO.BEREGNINGSLEDD"
}

internal sealed class ArenaRad(val data: String) {
    companion object {
        private val objectMapper = ObjectMapper().reader()
        fun lagRad(tabell: String, data: String) =
            when (tabell) {
                SAK_TABELL -> SakRad(data)
                VEDTAK_TABELL -> VedtakRad(data)
                VEDTAKFAKTA_TABELL -> VedtakFaktaRad(data)
                BEREGNINGSLEDD_TABELL -> BeregningsleddRad(data)
                else -> throw IllegalArgumentException("Ukjent tabelltype")
            }
    }

    internal val json = objectMapper.readTree(data)
    open fun vedtakId(): Int? = null
    open fun sakId(): Int? = null
}

internal class SakRad(data: String) : ArenaRad(data) {
    override fun sakId() = json["after"]["SAK_ID"].asInt()
}

internal class VedtakFaktaRad(data: String) : ArenaRad(data) {
    override fun vedtakId() = json["after"]["VEDTAK_ID"].asInt()
}

internal class VedtakRad(data: String) : ArenaRad(data) {
    override fun vedtakId() = json["after"]["VEDTAK_ID"].asInt()
    override fun sakId() = json["after"]["SAK_ID"].asInt()
}

internal class BeregningsleddRad(data: String) : ArenaRad(data) {
    override fun vedtakId() = json["after"]["TABELLNAVNALIAS_KILDE"].asText()
        .takeIf { it == "VEDTAK" }
        ?.let { json["after"]["OBJEKT_ID_KILDE"].asInt() }
}

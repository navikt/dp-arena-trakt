package no.nav.dagpenger.arena.trakt.db

import com.fasterxml.jackson.databind.ObjectMapper

internal object ArenaKoder {
    const val DAGPENGE_SAK = "DAGP"
}

internal sealed class ArenaRad(val data: String) {
    companion object {
        private val objectMapper = ObjectMapper().reader()
        fun lagRad(tabell: String, data: String) =
            when (tabell) {
                "SIAMO.SAK" -> SakRad(data)
                "SIAMO.VEDTAK" -> VedtakRad(data)
                "SIAMO.VEDTAKFAKTA" -> VedtakFaktaRad(data)
                "SIAMO.BEREGNINGSLEDD" -> BeregningsleddRad(data)
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

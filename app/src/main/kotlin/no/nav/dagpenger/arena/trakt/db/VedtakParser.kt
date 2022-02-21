package no.nav.dagpenger.arena.trakt.db

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.BEREGNINGSLEDD_TABELL
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.VEDTAKFAKTA_TABELL
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.VEDTAK_TABELL

// Forstår json og skal produsere vedtak
internal class VedtakParser {
    private val objectMapper = ObjectMapper()
    fun parse(vedtaksData: List<String>): DagpengeVedtak {
        val dataFraVedtakTabell = vedtaksData.last { it.contains("\"${VEDTAK_TABELL}\"") }
        val vedtaksType = fastsettVedtakType(dataFraVedtakTabell)
        println(vedtaksType)

        return UfullstendigDagpengeVedtak
    }

    private fun fastsettVedtakType(dataFraVedtakTabell: String): String {
        val json = objectMapper.readTree(dataFraVedtakTabell)
        val vedtakTypeKode = hentFraVedtak("VEDTAKTYPEKODE", json)
        val utfallKode = hentFraVedtak("UTFALLKODE", json)
        val rettighetKode = hentFraVedtak("RETTIGHETKODE", json)
        val vedtakstatusKode = hentFraVedtak("VEDTAKSTATUSKODE", json)

        return when {
            erNyRettighet(vedtakTypeKode, utfallKode, rettighetKode, vedtakstatusKode) -> "Ny rettighet"
            else -> "Ukjent vedtakstype"
        }
    }

    private fun erNyRettighet(vedtakTypeKode: String, utfallKode: String, rettighetKode: String, vedtakstatusKode: String) =
        vedtakTypeKode == "O" &&
            utfallKode == "JA" &&
            listOf("DAGO", "PERM", "DEKS", "LONN", "FISK").contains(rettighetKode) &&
            listOf("IVERK", "AVSLU").contains(vedtakstatusKode)

    fun hentFraVedtak(feltnavn: String, json: JsonNode): String = json["after"][feltnavn].asText()

    private fun lagVedtak(vedtaksData: List<String>): DagpengeVedtak {
        val dpVedtak = FullstendigDagpengevedtak()
        println(vedtaksData)
        vedtaksData.forEach {
            val json = ObjectMapper().readTree(it)
            when (json["tabell"].asText()) {
                VEDTAK_TABELL -> println("vedtak funnet")
                VEDTAKFAKTA_TABELL -> println("vedtakfakta funnet")
                BEREGNINGSLEDD_TABELL -> println("beregningsledd funnet")
            }
        }
        return UfullstendigDagpengeVedtak
    }

    internal interface DagpengeVedtak
    internal class FullstendigDagpengevedtak : DagpengeVedtak
    internal object UfullstendigDagpengeVedtak : DagpengeVedtak
}

package no.nav.dagpenger.arena.trakt.db

import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.beregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.lagre
import no.nav.dagpenger.arena.trakt.helpers.vedtakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class VedtakParserTest {

    private val vedtakParser = VedtakParser()

    @Test
    fun `Vedtakparser produserer et inkomplett vedtak`() {
        assertFalse(vedtakParser.erAllDataTilstede(ukomplettVedtaksData()))
    }

    @Test
    fun `Vedtakparser produserer et komplett vedtak`() {
        assertTrue(vedtakParser.erAllDataTilstede(komplettVedtaksData()))
    }

    fun ukomplettVedtaksData(): List<String> {
        return listOf(vedtakJSON())
    }

    fun komplettVedtaksData(): List<String> {
        val dataRepository = DataRepository()
        var vedtaksData = mutableListOf<String>()
        withMigratedDb {
            val vedtakId = 123
            dataRepository.lagre(beregningsleddJSON(vedtakId))
            dataRepository.lagre(vedtaksfaktaJSON(vedtakId))
            dataRepository.lagre(vedtakJSON(vedtakId, 5))
            dataRepository.lagre(vedtakJSON(15345, 53))

            vedtaksData = dataRepository.hentVedtaksdata(vedtakId).toMutableList()
        }
        return vedtaksData
    }
}
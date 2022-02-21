package no.nav.dagpenger.arena.trakt.db

import no.nav.dagpenger.arena.trakt.db.VedtakParser.FullstendigDagpengevedtak
import no.nav.dagpenger.arena.trakt.db.VedtakParser.UfullstendigDagpengeVedtak
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.beregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.lagre
import no.nav.dagpenger.arena.trakt.helpers.nyRettighetVedtakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class VedtakParserTest {

    private val vedtakParser = VedtakParser()

    @Test
    fun `Vedtakparser produserer et ufullstendig dagpengevedtak`() {
        val dagpengevedtak = vedtakParser.parse(ufullstendigVedtaksData())
        assertTrue(dagpengevedtak is UfullstendigDagpengeVedtak)
    }

    @Test
    @Disabled
    fun `Vedtakparser produserer et fullstendig dagpengevedtak`() {
        val dagpengevedtak = vedtakParser.parse(listOf(nyRettighetVedtakJSON()))

        assertTrue(dagpengevedtak is FullstendigDagpengevedtak)
    }

    private fun ufullstendigVedtaksData() = listOf(nyRettighetVedtakJSON())

    private fun fullstendigVedtaksdata(): List<String> {
        val dataRepository = DataRepository()
        var vedtaksData = mutableListOf<String>()
        withMigratedDb {
            val vedtakId = 123
            dataRepository.lagre(beregningsleddJSON(vedtakId))
            dataRepository.lagre(vedtaksfaktaJSON(vedtakId))
            dataRepository.lagre(nyRettighetVedtakJSON(vedtakId, 5))

            vedtaksData = dataRepository.hentVedtaksdata(vedtakId).toMutableList()
        }
        return vedtaksData
    }
}

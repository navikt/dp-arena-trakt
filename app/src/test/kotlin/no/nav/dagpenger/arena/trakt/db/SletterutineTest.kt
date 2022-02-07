package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.beregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.lagre
import no.nav.dagpenger.arena.trakt.helpers.sakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SletterutineTest {
    private val dataRepository = DataRepository()

    @Test
    fun `Scheduler sletter data som ikke omhandler dagpenger`() {
        // TODO: Fix: Dette er en flaky test som kan feile. Juster Thread.sleep() opp for å være sikker
        withMigratedDb {
            val ikkeDpVedtak = 123
            val ikkeDpSak = 6789
            val msMellomSlettinger = 100L
            Sletterutine(dataRepository, msFørSletterutineBegynner = 0L , msMellomSlettinger).start()

            dataRepository.lagre(beregningsleddJSON(ikkeDpVedtak, kode = "IKKE_DP"))
            dataRepository.lagre(vedtaksfaktaJSON(ikkeDpVedtak, kode = "IKKE_DP"))
            dataRepository.lagre(vedtakJSON(ikkeDpVedtak, ikkeDpSak))
            dataRepository.lagre(sakJSON(ikkeDpSak))
            Thread.sleep(msMellomSlettinger * 10)

            assertEquals(0, antallRaderMedData())
        }
    }

    private fun antallRaderMedData() =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf("SELECT COUNT(id) FROM arena_data WHERE data is not null").map { it.int(1) }.asSingle)
        }
}

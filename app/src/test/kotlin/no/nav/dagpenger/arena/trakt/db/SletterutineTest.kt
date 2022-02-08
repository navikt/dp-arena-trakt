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
    fun `Sletterutine sletter data som ikke omhandler dagpenger`() {
        withMigratedDb {
            val ikkeDpVedtak = 123
            val ikkeDpSak = 6789
            val msMellomSlettinger = 100L
            Sletterutine(
                dataRepository,
                msFørSletterutineBegynner = 0L,
                msMellomSlettinger,
                batchStørrelse = 10
            ).start()

            dataRepository.lagre(beregningsleddJSON(ikkeDpVedtak, kode = "IKKE_DP"))
            dataRepository.lagre(vedtaksfaktaJSON(ikkeDpVedtak, kode = "IKKE_DP"))
            dataRepository.lagre(vedtakJSON(ikkeDpVedtak, ikkeDpSak))
            dataRepository.lagre(sakJSON(ikkeDpSak))
            Thread.sleep(msMellomSlettinger * 10)

            assertEquals(0, antallRaderMedData())
        }
    }

    @Test
    fun `Sletterutine håndterer rader med nullet data`() {
        withMigratedDb {
            genererNulletData(antallRader = 20)
            genererIkkeDagpengeData(antallRader = 30)

            Sletterutine(
                dataRepository,
                msFørSletterutineBegynner = 0L,
                msMellomSlettinger = 10L,
                batchStørrelse = 10
            ).start()

            Thread.sleep(1000)
            assertEquals(0, antallRaderMedData())
        }
    }

    private fun genererNulletData(antallRader: Int) {
        for (i in 1..antallRader) {
            val primærnøkkel = dataRepository.lagre("{}")
            using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
                session.run(
                    queryOf("UPDATE arena_data SET data=null, behandlet=now() WHERE id=?", primærnøkkel).asExecute
                )
            }
        }
    }

    private fun genererIkkeDagpengeData(antallRader: Int) {
        for (i in 1..antallRader) {
            dataRepository.lagre(sakJSON((0..antallRader).random(), saksKode = "AAP"))
        }
    }

    private fun antallRaderMedData() =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf("SELECT COUNT(id) FROM arena_data WHERE data is not null").map { it.int(1) }.asSingle)
        }
}

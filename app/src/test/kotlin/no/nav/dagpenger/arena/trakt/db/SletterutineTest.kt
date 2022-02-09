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
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class SletterutineTest {
    private val dataRepository = DataRepository()

    @Test
    fun `Sletterutine sletter data som ikke omhandler dagpenger`() {
        withMigratedDb {
            val ikkeDpVedtak = 123
            val ikkeDpSak = 6789
            val msMellomSlettinger = 100L
            Sletterutine(
                dataRepository, msFørSletterutineBegynner = 0L, msMellomSlettinger, batchStørrelse = 10
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
    fun `Sletterutine håndterer flere rader med data med uviss ytelse enn batchStørrelsen`() {
        withMigratedDb {
            val `antall saker som skal nulles av sletterutine` = 50

            genererNulletData(antallRader = 20)
            genererSakerUtenLinkTilSakTabell(antallRader = `antall saker som skal nulles av sletterutine`)
            genererNulletData(antallRader = 50)
            genererDataAvType("AAP", antallRader = 40)

            assertEquals(`antall saker som skal nulles av sletterutine`, antallRaderMedData())

            Sletterutine(
                dataRepository, msFørSletterutineBegynner = 0L, msMellomSlettinger = 10L, batchStørrelse = 10
            ).start()

            Thread.sleep(1000)
            assertEquals(0, antallRaderMedData())
        }
    }

    private fun genererDataFraUkjentYtelse(antallRader: Int, json: String = vedtaksfaktaJSON()) {
        for (i in 1..antallRader) {
            lagreData(json)
        }
    }

    private fun genererSakerUtenLinkTilSakTabell(antallRader: Int) {
        for (i in 1..antallRader) {
            lagreData(sakJSON(saksKode = "AAP"))
        }
    }

    private fun lagreData(json: String) {
        @Language("PostgreSQL")
        val lagreQuery = """INSERT INTO arena_data (tabell, pos, skjedde, replikert, data)
            |VALUES (?, ?, ?, ?, ?::jsonb)
            |ON CONFLICT DO NOTHING
            |""".trimMargin()

        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    lagreQuery,
                    "SIAMO.SAK",
                    UUID.randomUUID().toString(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    json
                ).asExecute
            )
        }
    }

    private fun genererNulletData(antallRader: Int) {
        for (i in 1..antallRader) {
            val primærnøkkel = dataRepository.lagre(
                """{
                |  "table": "BAR"
                }""".trimMargin()
            )
            using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
                session.run(
                    queryOf("UPDATE arena_data SET data=null, behandlet=now() WHERE id=?", primærnøkkel).asExecute
                )
            }
        }
    }

    private fun genererDataAvType(saksKode: String, antallRader: Int) {
        for (i in 1..antallRader) {
            dataRepository.lagre(sakJSON((0..antallRader).random(), saksKode = saksKode))
        }
    }

    private fun antallRaderMedData() = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
        session.run(queryOf("SELECT COUNT(id) FROM arena_data WHERE data is not null").map { it.int(1) }.asSingle)
    }
}

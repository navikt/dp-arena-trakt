package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.DAGPENGE_SAK
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.SAK_TABELL
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.beregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.lagre
import no.nav.dagpenger.arena.trakt.helpers.sakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class SletterutineTest {

    private val dataRepository = DataRepository()

    @Test
    fun `Ikke dagpenge relaterte data skal slettes`() {
        withMigratedDb {
            val ikkeDpVedtak = 123
            val ikkeDpSak = 6789
            val dpVedtak = 127
            val dpSak = 456

            dataRepository.lagre(beregningsleddJSON(ikkeDpVedtak, kode = "IKKE_DP"))
            dataRepository.lagre(vedtaksfaktaJSON(ikkeDpVedtak, kode = "IKKE_DP"))
            dataRepository.lagre(vedtakJSON(ikkeDpVedtak, ikkeDpSak))
            dataRepository.lagre(vedtakJSON(dpVedtak, dpSak))
            dataRepository.lagre(sakJSON(dpSak, saksKode = DAGPENGE_SAK))

            dataRepository.batchSlettDataSomIkkeOmhandlerDagpenger(10)

            assertEquals(5, antallRaderMedData())
            dataRepository.lagre(sakJSON(ikkeDpSak))
            dataRepository.batchSlettDataSomIkkeOmhandlerDagpenger(10)
            dataRepository.batchSlettDataSomIkkeOmhandlerDagpenger(10)
            assertEquals(2, antallRaderMedData())
        }
    }

    private fun antallRaderMedData() =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf("SELECT COUNT(id) FROM arena_data WHERE data is not null").map { it.int(1) }.asSingle)
        }

    @Test
    fun `Sletterutine spørringen som henter rader treffer indeks`() {
        withMigratedDb {
            genererDataFraUkjentYtelse(2000)
            val query = DataRepository.finnRaderTilSlettingQuery
            val batchsize = 200
            val plan: String = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
                session.run(
                    queryOf("EXPLAIN ANALYZE $query", batchsize).map {
                        it.string(1)
                    }.asList
                )
            }.joinToString("\n")

            assertTrue(plan.contains("Bitmap Index Scan on i_behandlet"))
        }
    }

    private fun genererDataFraUkjentYtelse(antallRader: Int, json: String = vedtaksfaktaJSON()) {
        for (i in 1..antallRader) {
            lagreData(json)
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
                    SAK_TABELL,
                    UUID.randomUUID().toString(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    json
                ).asExecute
            )
        }
    }
}

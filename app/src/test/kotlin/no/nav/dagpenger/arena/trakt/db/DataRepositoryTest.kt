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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class DataRepositoryTest {
    private val dataRepository = DataRepository()

    @Test
    fun `Kan lagre JSON blobber som kommer fra Arena`() {
        withMigratedDb {
            dataRepository.lagre(beregningsleddJSON(kode = "BL1"))
            dataRepository.lagre(vedtaksfaktaJSON(kode = "VF1"))
            assertEquals(2, antallRaderMedData())
        }
    }

    @Test
    fun `Lagre returnerer primærnøkkelen til nylig inserted element`() {
        withMigratedDb {
            val generertPrimærnøkkel = dataRepository.lagre(sakJSON(456, "DAGP"))
            assertEquals(1, generertPrimærnøkkel)
        }
    }

    @Test
    fun `Duplikat data ignoreres`() {
        val pos = UUID.randomUUID().toString()
        val skjedde = LocalDateTime.now()

        withMigratedDb {
            val primærNøkkel = dataRepository.lagre("SIAMO.VEDTAK", pos, LocalDateTime.now(), skjedde, vedtakJSON())
            assertEquals(1, primærNøkkel)
            val ignorert = dataRepository.lagre("SIAMO.VEDTAK", pos, LocalDateTime.now(), skjedde, vedtakJSON())
            assertNull(ignorert)
            assertEquals(1, antallRaderMedData())
        }
    }

    @Test
    fun `DpSak lagres, vurderes deretter til sletting, DpSak blir ikke slettet`() {
        withMigratedDb {
            val dpSak = 456
            dataRepository.lagre(sakJSON(dpSak, saksKode = "DAGP"))
            assertEquals(1, antallRaderMedData())
        }
    }

    @Test
    fun `Ikke DpSak lagres, vurderes deretter til sletting, blir slettet`() {
        withMigratedDb {
            val aapSak = 456
            dataRepository.lagre(sakJSON(aapSak, saksKode = "AAP"))
            assertEquals(0, antallRaderMedData())
        }
    }

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
            dataRepository.lagre(sakJSON(dpSak, saksKode = "DAGP"))

            dataRepository.batchSlettDataSomIkkeOmhandlerDagpenger(10)

            assertEquals(5, antallRaderMedData())
            dataRepository.lagre(sakJSON(ikkeDpSak))
            dataRepository.batchSlettDataSomIkkeOmhandlerDagpenger(10)
            dataRepository.batchSlettDataSomIkkeOmhandlerDagpenger(10)
            assertEquals(2, antallRaderMedData())
        }
    }

    private fun genererDataFraUkjentYtelse(antallRader: Int, json: String = vedtaksfaktaJSON()) {
        val data = mutableListOf<List<Any>>().apply {
            repeat(antallRader) {
                this.add(
                    listOf(
                        "SIAMO.SAK",
                        UUID.randomUUID().toString(),
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        json
                    )
                )
            }
        }

        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            //language=PostgreSQL
            session.batchPreparedStatement(
                """INSERT INTO arena_data (tabell, pos, skjedde, replikert, data)
                    |VALUES (?, ?, ?, ?, ?::jsonb)
                    |ON CONFLICT DO NOTHING
                    |""".trimMargin(),
                data
            )
        }
    }

    @Test
    fun `Finn all vedtaksdata`() {
        withMigratedDb {
            val vedtakId = 123
            assertEquals(0, dataRepository.hentVedtaksdata(vedtakId).size)

            genererDataFraUkjentYtelse(200, vedtaksfaktaJSON(1231231))
            dataRepository.lagre(beregningsleddJSON(vedtakId))
            dataRepository.lagre(vedtaksfaktaJSON(vedtakId))
            dataRepository.lagre(vedtakJSON(vedtakId, 5))
            dataRepository.lagre(vedtakJSON(15345, 53))
            val plan = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
                session.run(
                    queryOf(
                        """EXPLAIN ANALYSE
                        |SELECT data
                        |FROM arena_data
                        |WHERE data @> ?::jsonb
                        |   OR data @> ?::jsonb
                        |   OR data @> ?::jsonb
                        |""".trimMargin(),
                        """{ "table": "SIAMO.VEDTAK", "after": { "VEDTAK_ID": $vedtakId }}""",
                        """{ "table": "SIAMO.VEDTAKFAKTA", "after": { "VEDTAK_ID": $vedtakId }}""",
                        """{ "table": "SIAMO.BEREGNINGSLEDD", "after": { "TABELLNAVNALIAS_KILDE": "VEDTAK", "OBJEKT_ID_KILDE": $vedtakId }}"""
                    ).map {
                        it.string(1)
                    }.asList
                )
            }.joinToString("\n")
            println(plan)
            assertEquals(3, dataRepository.hentVedtaksdata(vedtakId).size)
        }
    }

    private fun antallRaderMedData() =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf("SELECT COUNT(id) FROM arena_data WHERE data is not null").map { it.int(1) }.asSingle)
        }
}

package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.beregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Period

internal class DataRepositoryTest {
    private val dataRepository = DataRepository()

    @Test
    fun `Kan lagre JSON blobber som kommer fra Arena`() {
        withMigratedDb {
            dataRepository.lagre(beregningsleddJSON("BL1"))
            dataRepository.lagre(vedtaksfaktaJSON("VF1"))

            assertEquals(2, antallRader())
        }
    }

    @Test
    fun `Kan slette JSON blobber som ikke har blitt brukt etter X tid`() {
        withMigratedDb {
            lagreMedDato(beregningsleddJSON("BL1"), LocalDate.now())
            lagreMedDato(vedtaksfaktaJSON("VF1"), LocalDate.now().minusDays(1))
            lagreMedDato(vedtaksfaktaJSON("VF1"), LocalDate.now().minusDays(10))
            lagreMedDato(vedtaksfaktaJSON("VF1"), LocalDate.now().minusDays(15))

            assertEquals(4, antallRader())

            dataRepository.slettUbrukteData(eldreEnn = Period.ofDays(10))
            assertEquals(3, antallRader())

            dataRepository.slettUbrukteData(eldreEnn = Period.ZERO)
            assertEquals(1, antallRader(), "Skal ikke slette rader opprettet i dag")
        }
    }

    @Test
    fun `Sletting bruker index`() {
        withMigratedDb {
            val query = DataRepository::class.java.getDeclaredField("slettQuery").also {
                it.trySetAccessible()
            }.get(dataRepository)
            val plan = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
                session.run(
                    queryOf("EXPLAIN ANALYZE $query", 5).map {
                        it.string(1)
                    }.asList
                )
            }

            assertFalse(plan[1].contains("Seq Scan"))
            assertTrue(plan[1].contains("Index Scan"))
        }
    }

    private fun lagreMedDato(json: String, dato: LocalDate) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf("INSERT INTO arena_data (data, opprettet) VALUES (?::jsonb, ?)", json, dato).asExecute)
        }

    private fun antallRader() =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf("SELECT COUNT(id) FROM arena_data").map { it.int(1) }.asSingle)
        }
}

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
import java.time.LocalDate

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
    fun `Sletter data riktig`() {
        withMigratedDb {
            dataRepository.lagre(beregningsleddJSON(vedtakId = 123, navn = "BL1"))
            dataRepository.lagre(vedtaksfaktaJSON(vedtakId = 123, navn = "VF1"))
            dataRepository.lagre(vedtakJSON(123, 6789))
            dataRepository.lagre(sakJSON(sakId = 456, saksKode = "DAGP"))
            dataRepository.lagre(vedtakJSON(127, 456))
            dataRepository.rydd()
            assertEquals(5, antallRader())
            dataRepository.lagre(sakJSON(6789))
            dataRepository.rydd()
            dataRepository.rydd()
            assertEquals(2, antallRader())
        }
    }

    private fun lagreMedDato(json: String, dato: LocalDate) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    """INSERT INTO arena_data (data, mottatt, tabell, pos, replikert, skjedde)
                        |VALUES (?::jsonb, ?, 't', RANDOM(), NOW(), NOW())""".trimMargin(),
                    json,
                    dato
                ).asExecute
            )
        }

    private fun antallRader() =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf("SELECT COUNT(id) FROM arena_data WHERE data is not null").map { it.int(1) }.asSingle)
        }
}

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
            dataRepository.lagre(beregningsleddJSON(kode = "BL1"))
            dataRepository.lagre(vedtaksfaktaJSON(kode = "VF1"))

            assertEquals(2, antallRaderMedData())
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

            dataRepository.rydd()
            assertEquals(5, antallRaderMedData())
            dataRepository.lagre(sakJSON(ikkeDpSak))
            dataRepository.rydd()
            dataRepository.rydd()
            assertEquals(2, antallRaderMedData())
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

    private fun antallRaderMedData() =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf("SELECT COUNT(id) FROM arena_data WHERE data is not null").map { it.int(1) }.asSingle)
        }
}

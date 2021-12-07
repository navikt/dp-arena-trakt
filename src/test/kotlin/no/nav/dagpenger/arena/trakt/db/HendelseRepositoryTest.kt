package no.nav.dagpenger.arena.trakt.db

import kotlinx.coroutines.runBlocking
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.beregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class HendelseRepositoryTest {
    private val testRapid = TestRapid()
    private val hendelseRepository = HendelseRepository(testRapid)
    private val vedtaksid = "123"
    private val dataRepository = DataRepository().also { it.addObserver(hendelseRepository) }

    @Test
    fun `Skal finne nye hendelser etter de lagres`() {
        withMigratedDb {
            val vedtak = Hendelse.testHendelse(vedtaksid)

            assertFalse(hendelseRepository.leggPåKø(vedtak))

            dataRepository.lagre(beregningsleddJSON("BL1"))
            dataRepository.lagre(vedtaksfaktaJSON("VF1"))
            dataRepository.lagre(vedtakJSON(vedtaksid.toInt()))

            runBlocking { hendelseRepository.start(null) }

            assertEquals(1, testRapid.inspektør.size)
            assertEquals(3, antallBrukteData())
        }
    }

    @Test
    fun `leggPåKø skal returnere true om hendelsen ble sendt eller allerede sendt`() {
        withMigratedDb {
            val vedtak = Hendelse.testHendelse(vedtaksid)

            dataRepository.lagre(beregningsleddJSON("BL1"))
            dataRepository.lagre(vedtaksfaktaJSON("VF1"))
            dataRepository.lagre(vedtakJSON(vedtaksid.toInt()))

            assertTrue(hendelseRepository.leggPåKø(vedtak))
            assertTrue(hendelseRepository.leggPåKø(vedtak))
            assertTrue(hendelseRepository.leggPåKø(vedtak))

            assertEquals(1, testRapid.inspektør.size)
            assertEquals(3, antallBrukteData())
        }
    }

    private fun antallBrukteData() =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            //language=PostgreSQL
            session.run(
                queryOf("SELECT COUNT(hendelse_id) FROM arena_data")
                    .map { it.int(1) }
                    .asSingle
            )
        }

    @Test
    fun `leggPåKø skal returnere true om hendelsen ikke er sendt`() {
        withMigratedDb {
            val vedtak = Hendelse.testHendelse(vedtaksid)

            assertFalse(hendelseRepository.leggPåKø(vedtak))

            assertEquals(0, testRapid.inspektør.size)
        }
    }
}

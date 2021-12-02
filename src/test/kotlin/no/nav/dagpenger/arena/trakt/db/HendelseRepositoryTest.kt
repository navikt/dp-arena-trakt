package no.nav.dagpenger.arena.trakt.db

import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.helpers.Postgres
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
    private val dataRepository = DataRepository()

    @Test
    fun `leggPåKø skal returnere true om hendelsen ble sendt eller allerede sendt`() {
        Postgres.withMigratedDb {
            val vedtak = Hendelse.vedtak(vedtaksid)

            dataRepository.lagre(beregningsleddJSON("BL1"))
            dataRepository.lagre(vedtaksfaktaJSON("VF1"))
            dataRepository.lagre(vedtakJSON(vedtaksid.toInt()))

            assertTrue(hendelseRepository.leggPåKø(vedtak))
            assertTrue(hendelseRepository.leggPåKø(vedtak))
            assertTrue(hendelseRepository.leggPåKø(vedtak))

            assertEquals(1, testRapid.inspektør.size)
        }
    }

    @Test
    fun `leggPåKø skal returnere true om hendelsen ikke er sendt`() {
        Postgres.withMigratedDb {
            val vedtak = Hendelse.vedtak(vedtaksid)

            assertFalse(hendelseRepository.leggPåKø(vedtak))

            assertEquals(0, testRapid.inspektør.size)
        }
    }
}

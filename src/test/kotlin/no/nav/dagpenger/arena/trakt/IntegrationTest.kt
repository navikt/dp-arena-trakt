package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.db.HendelseRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.beregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import no.nav.dagpenger.arena.trakt.tjenester.DataMottakService
import no.nav.dagpenger.arena.trakt.tjenester.VedtakHendelseService
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IntegrationTest {
    private val dataRepository: DataRepository = DataRepository()
    private val hendelseRepository = HendelseRepository()
    private val rapid = TestRapid().also {
        DataMottakService(it, dataRepository, hendelseRepository)
        VedtakHendelseService(it, hendelseRepository)
    }

    @BeforeEach
    internal fun setup() {
        rapid.reset()
    }

    @Test
    fun `rekkefølge 1`() {
        withMigratedDb {
            rapid.sendTestMessage(vedtaksfaktaJSON("VF1"))
            rapid.sendTestMessage(vedtakJSON(123))
            rapid.sendTestMessage(beregningsleddJSON("BL1"))

            with(rapid.inspektør) {
                assertEquals(1, size)
            }
        }
    }

    @Test
    fun `rekkefølge 2`() {
        withMigratedDb {
            rapid.sendTestMessage(vedtakJSON(123))
            rapid.sendTestMessage(vedtaksfaktaJSON("VF1"))
            rapid.sendTestMessage(beregningsleddJSON("BL1"))

            with(rapid.inspektør) {
                assertEquals(1, size)
            }
        }
    }

    @Test
    fun `rekkefølge 3`() {
        withMigratedDb {
            rapid.sendTestMessage(vedtaksfaktaJSON("VF1"))
            rapid.sendTestMessage(beregningsleddJSON("BL1"))
            rapid.sendTestMessage(vedtakJSON(123))

            with(rapid.inspektør) {
                assertEquals(1, size)
            }
        }
    }

    @Test
    fun `rekkefølge 4`() {
        withMigratedDb {
            rapid.sendTestMessage(beregningsleddJSON("BL1"))
            rapid.sendTestMessage(vedtakJSON(123))
            rapid.sendTestMessage(vedtaksfaktaJSON("VF1"))

            with(rapid.inspektør) {
                assertEquals(1, size)
            }
        }
    }

    @Test
    fun `rekkefølge 5 med andre ting`() {
        withMigratedDb {
            rapid.sendTestMessage(beregningsleddJSON("BL1"))
            rapid.sendTestMessage(beregningsleddJSON("BL2"))
            rapid.sendTestMessage(vedtakJSON(123))
            rapid.sendTestMessage(vedtakJSON(12345))
            rapid.sendTestMessage(vedtaksfaktaJSON("VF1"))

            with(rapid.inspektør) {
                assertEquals(1, size)
            }
        }
    }
}

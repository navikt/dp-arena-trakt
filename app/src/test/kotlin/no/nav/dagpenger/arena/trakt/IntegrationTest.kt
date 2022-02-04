package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.db.HendelseRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.beregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import no.nav.dagpenger.arena.trakt.tjenester.BeregningsleddService
import no.nav.dagpenger.arena.trakt.tjenester.DataMottakService
import no.nav.dagpenger.arena.trakt.tjenester.VedtakService
import no.nav.dagpenger.arena.trakt.tjenester.VedtaksfaktaService
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IntegrationTest {
    private val dataRepository: DataRepository = DataRepository()
    private var hendelseRepository: HendelseRepository
    private val rapid = TestRapid().also {
        hendelseRepository = HendelseRepository(it)
        DataMottakService(it, dataRepository)
        VedtakService(it, hendelseRepository)
        BeregningsleddService(it, hendelseRepository)
        VedtaksfaktaService(it, hendelseRepository)
    }

    @BeforeEach
    internal fun setup() {
        rapid.reset()
    }

    @Test
    fun `rekkefølge 1`() {
        withMigratedDb {
            rapid.sendTestMessage(vedtaksfaktaJSON(kode = "FDATO"))
            rapid.sendTestMessage(vedtakJSON(123))
            rapid.sendTestMessage(beregningsleddJSON(kode = "DPTEL"))

            with(rapid.inspektør) {
                assertEquals(1, size)
            }
        }
    }

    @Test
    fun `rekkefølge 2`() {
        withMigratedDb {
            rapid.sendTestMessage(vedtakJSON(123))
            rapid.sendTestMessage(vedtaksfaktaJSON(kode = "FDATO"))
            rapid.sendTestMessage(beregningsleddJSON(kode = "DPTEL"))

            with(rapid.inspektør) {
                assertEquals(1, size)
            }
        }
    }

    @Test
    fun `rekkefølge 3`() {
        withMigratedDb {
            rapid.sendTestMessage(vedtaksfaktaJSON(kode = "FDATO"))
            rapid.sendTestMessage(beregningsleddJSON(kode = "DPTEL"))
            rapid.sendTestMessage(vedtakJSON(123))

            with(rapid.inspektør) {
                assertEquals(1, size)
            }
        }
    }

    @Test
    fun `rekkefølge 4`() {
        withMigratedDb {
            rapid.sendTestMessage(beregningsleddJSON(kode = "DPTEL"))
            rapid.sendTestMessage(vedtakJSON(123))
            rapid.sendTestMessage(vedtaksfaktaJSON(kode = "FDATO"))

            with(rapid.inspektør) {
                assertEquals(1, size)
            }
        }
    }

    @Test
    fun `rekkefølge 5 med andre flere vedtak`() {
        withMigratedDb {
            rapid.sendTestMessage(beregningsleddJSON(123, "DPTEL"))
            rapid.sendTestMessage(beregningsleddJSON(123, "BL2"))
            rapid.sendTestMessage(beregningsleddJSON(12345, "DPTEL"))
            rapid.sendTestMessage(vedtakJSON(123))
            rapid.sendTestMessage(vedtakJSON(12345))
            rapid.sendTestMessage(vedtakJSON(555))
            rapid.sendTestMessage(vedtaksfaktaJSON(123, "FDATO"))
            rapid.sendTestMessage(vedtaksfaktaJSON(12345, "FDATO"))

            with(rapid.inspektør) {
                assertEquals(1, size)
            }
        }
    }

    @Test
    fun `Publiserer ingenting uten komplette datasett`() {
        withMigratedDb {
            rapid.sendTestMessage(beregningsleddJSON(12345, "DPTEL"))
            rapid.sendTestMessage(vedtakJSON(123))
            rapid.sendTestMessage(vedtakJSON(12345))
            rapid.sendTestMessage(vedtakJSON(555))
            rapid.sendTestMessage(vedtaksfaktaJSON(123, "FDATO"))

            with(rapid.inspektør) {
                assertEquals(0, size)
            }
        }
    }
}
